# 项目鉴权原理与示例

## 一、鉴权原理概述

本系统采用 **ABAC（基于属性的访问控制）**：不直接判断「谁能不能访问什么」，而是根据**主体属性、资源属性、环境属性**和**操作**，与预先配置的**策略条件**做匹配，由**第一条命中的策略**决定允许（ALLOW）或拒绝（DENY）。

整体流程可以概括为：

```
访问请求（谁、对什么资源、做什么操作）
    ↓
加载主体属性、资源属性（从数据库）
    ↓
按优先级从高到低遍历「已启用」策略
    ↓
用策略里的 condition 与当前请求做匹配
    ↓
第一条命中的策略 → 按该策略的 effect 返回 Allow 或 Deny
    ↓
若没有任何策略命中 → 默认拒绝（Deny）
```

也就是说：**先按优先级排序策略，再逐条做条件匹配，命中即返回，未命中则默认拒绝。**

---

## 二、核心概念

| 概念 | 含义 | 在本项目中的体现 |
|------|------|------------------|
| **主体 (Subject)** | 发起访问的一方（用户、服务等） | `subjectId`，其属性存于 `subject_attribute` 表 |
| **资源 (Resource)** | 被访问的对象 | `resourceType` + `resourceId`，属性存于 `resource_attribute` 表 |
| **操作 (Action)** | 对资源执行的动作 | 如 `read`、`write`、`delete`，由请求传入 |
| **环境 (Environment)** | 请求时的上下文 | 如 `clientIp`、时间等，可选 |
| **策略 (Policy)** | 一条规则：在什么条件下执行什么效果 | `policy` 表：`effect`(ALLOW/DENY)、`priority`、`condition_json` |

**策略条件 (condition)** 是一段 JSON，描述「何时这条策略生效」：

- **subject**：主体属性需满足的条件（如 `department=IT` 或 `role` 在 `["admin","operator"]` 中）
- **resource**：资源属性需满足的条件（如 `type=order`）
- **action**：允许的操作（字符串或数组，如 `["read","write"]`）
- **environment**：可选，如请求 IP 需匹配某前缀

匹配规则：

- subject/resource：条件里的每个 key 必须在实际属性中存在，且值相等；若条件里是数组，则实际值只需在数组中即可。
- action：请求的 `action` 必须在策略的 `action` 列表（或单个值）中；若策略未写 `action`，表示不限制操作。
- 所有条件都满足，该策略才算「命中」。

---

## 三、代码中的执行流程（PDP）

鉴权入口是 **POST /api/abac/check**，由 `PdpService.check()` 处理：

1. **组装上下文**  
   根据请求中的 `subjectId`、`resourceType`、`resourceId` 从数据库加载主体属性和资源属性，得到两个 `Map<String, String>`；`action` 和 `environment` 直接从请求取。

2. **取策略并排序**  
   查询所有 `enabled = true` 的 `Policy`，按 `priority` **降序**（数值越大越优先）。

3. **逐条匹配**  
   对每条策略解析 `condition_json`，依次检查：  
   - subject 条件 vs 主体属性  
   - resource 条件 vs 资源属性  
   - action 条件 vs 请求的 action  
   - environment 条件 vs 请求的 environment  

   任一子条件不满足则本条不命中，继续下一条。

4. **返回决策**  
   - 若某条策略命中：返回该策略的 `effect`（ALLOW → `allowed: true`，DENY → `allowed: false`），并在响应中带上 `policyId`。  
   - 若没有任何策略命中：返回 `allowed: false`，`message: "No matching policy"`。

因此：**鉴权结果完全由「属性 + 策略条件 + 优先级」决定，先命中先生效，默认拒绝。**

---

## 四、举例说明

### 场景设定

- **主体**：用户 `user-001`，部门 IT，角色 admin。  
- **资源**：订单 `order-123`，类型 order，所属部门 IT。  
- **策略**：只配置两条——一条高优先级 DENY，一条低优先级 ALLOW。

### 步骤 1：配置主体属性

```http
PUT /api/subjects/user-001/attributes
Content-Type: application/json

{
  "department": "IT",
  "role": "admin"
}
```

即：当前请求里只要 `subjectId = user-001`，系统会加载到 `department=IT`、`role=admin`。

### 步骤 2：配置资源属性

```http
PUT /api/resources/order/order-123/attributes
Content-Type: application/json

{
  "type": "order",
  "department": "IT"
}
```

即：对 `resourceType=order`、`resourceId=order-123` 的访问，会加载到 `type=order`、`department=IT`。

### 步骤 3：配置两条策略

**策略 A（高优先级，DENY）：** 禁止非 admin 对 order 的写操作。

- condition：subject 的 role = "admin" 不满足时不允许写（这里用「反例」理解：我们写一条 DENY 策略，条件为 role 不是 admin 且 action 包含 write）。  
  为简单起见，改为：**DENY 策略——对 order 的 write，且 subject 的 department = HR**（即只拦 HR 写 order）。

实际可配置为：

```json
{
  "name": "禁止 HR 写订单",
  "effect": "DENY",
  "priority": 100,
  "enabled": true,
  "conditionJson": "{\"subject\":{\"department\":\"HR\"},\"resource\":{\"type\":\"order\"},\"action\":[\"write\"]}"
}
```

**策略 B（低优先级，ALLOW）：** IT 部门可读、写 order。

```json
{
  "name": "IT 可读写订单",
  "effect": "ALLOW",
  "priority": 10,
  "enabled": true,
  "conditionJson": "{\"subject\":{\"department\":\"IT\"},\"resource\":{\"type\":\"order\"},\"action\":[\"read\",\"write\"]}"
}
```

### 步骤 4：发起鉴权请求

**例 1：user-001 对 order-123 执行 read**

- 主体属性：department=IT, role=admin  
- 资源属性：type=order, department=IT  
- action：read  

策略按 priority 降序：先 100，再 10。

- 策略 A（priority 100）：condition 要求 subject.department=HR，实际是 IT → **不命中**。  
- 策略 B（priority 10）：subject.department=IT ✓，resource.type=order ✓，action 在 [read,write] ✓ → **命中**。  
- 结果：**allowed: true**，policyId 为策略 B 的 id。

**例 2：user-001 对 order-123 执行 write**

- 策略 A：仍要求 department=HR，不命中。  
- 策略 B：department=IT、type=order、action=write 均满足 → 命中。  
- 结果：**allowed: true**。

**例 3：假设存在用户 user-002，部门为 HR**

- 为 user-002 配置属性：`PUT /api/subjects/user-002/attributes` → `{"department":"HR"}`。  
- 对 order-123 执行 write 时：  
  - 策略 A：subject.department=HR ✓，resource.type=order ✓，action=write ✓ → **命中**，effect=DENY。  
  - 结果：**allowed: false**，message 为 "Denied by policy: 禁止 HR 写订单"。

**例 4：没有任何策略命中**

- 例如 user-001 对 resourceType=report、resourceId=rpt-1 执行 read，但只配置了上述两条 order 相关策略。  
- 两条策略的 resource 条件都是 type=order，与 report 不匹配，都不命中。  
- 结果：**allowed: false**，message 为 "No matching policy"。

---

## 五、小结

| 要点 | 说明 |
|------|------|
| 决策依据 | 主体属性 + 资源属性 + 操作（+ 可选环境）与策略条件匹配 |
| 匹配顺序 | 按策略 priority 从高到低，**先命中先返回** |
| 默认行为 | 没有任何策略命中时 **拒绝** |
| 条件写法 | subject/resource 为属性名到值（或数组「包含」）；action 为允许的操作；environment 可做 IP 等限制 |

因此，本项目的鉴权原理可以概括为：**用属性描述「谁」和「什么资源」，用策略描述「在什么条件下允许/拒绝」，通过优先级决定多条策略谁先生效，最终得到一次访问是允许还是拒绝。**
