# ABAC 最小可用版 — 接口与数据表设计

## 1. 接口设计

### 1.1 鉴权决策（PDP）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/abac/check` | 请求体为访问上下文，返回 Allow/Deny |

**请求体示例：**
```json
{
  "subjectId": "user-001",
  "resourceType": "order",
  "resourceId": "order-123",
  "action": "read",
  "environment": {
    "clientIp": "192.168.1.1",
    "timestamp": "2026-02-24T10:00:00Z"
  }
}
```

**响应示例：**
```json
{
  "allowed": true,
  "policyId": "policy-001",
  "message": null
}
```

### 1.2 主体属性

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/subjects/{subjectId}/attributes` | 查询主体全部属性 |
| PUT | `/api/subjects/{subjectId}/attributes` | 全量设置属性（key-value 对象） |
| POST | `/api/subjects/{subjectId}/attributes` | 追加/覆盖单个或多个属性 |

### 1.3 资源属性

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/resources/{resourceType}/{resourceId}/attributes` | 查询资源全部属性 |
| PUT | `/api/resources/{resourceType}/{resourceId}/attributes` | 全量设置属性 |

### 1.4 策略管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/policies` | 分页列表，支持按 name、enabled 过滤 |
| GET | `/api/policies/{id}` | 策略详情 |
| POST | `/api/policies` | 创建策略 |
| PUT | `/api/policies/{id}` | 更新策略 |
| DELETE | `/api/policies/{id}` | 删除策略 |
| PATCH | `/api/policies/{id}/enabled` | 启用/停用（body: `{"enabled": true}`） |

**策略 JSON 结构：**
- `name`: 策略名称  
- `effect`: `ALLOW` | `DENY`  
- `priority`: 整数，越大越优先；同优先级时 DENY 优先  
- `enabled`: 是否参与评估  
- `condition`: 条件对象，见下  

**condition 结构（最小可用）：**
- `subject`: 主体属性条件，key 为属性名，value 为要求（字符串相等或数组包含）
- `resource`: 资源属性条件，同上  
- `action`: 允许的操作列表，如 `["read","write"]`，空或不传表示不限制  
- `environment`: 可选，如 `ipPrefix` 等  

示例：
```json
{
  "subject": { "department": "IT", "role": ["admin", "operator"] },
  "resource": { "type": "order" },
  "action": ["read", "write"]
}
```

---

## 2. 数据表设计要点

### 2.1 主体属性表 `subject_attribute`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| subject_id | VARCHAR(64) NOT NULL | 主体标识（用户/服务 ID） |
| attr_key | VARCHAR(128) NOT NULL | 属性名，如 department, role |
| attr_value | VARCHAR(512) NOT NULL | 属性值 |
| created_at | DATETIME | 创建时间 |

- 唯一约束：`(subject_id, attr_key)`  
- 索引：`subject_id`（按主体查属性）

### 2.2 资源属性表 `resource_attribute`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| resource_type | VARCHAR(64) NOT NULL | 资源类型，如 order, report |
| resource_id | VARCHAR(128) NOT NULL | 资源实例 ID |
| attr_key | VARCHAR(128) NOT NULL | 属性名 |
| attr_value | VARCHAR(512) NOT NULL | 属性值 |
| created_at | DATETIME | 创建时间 |

- 唯一约束：`(resource_type, resource_id, attr_key)`  
- 索引：`(resource_type, resource_id)`（按资源查属性）

### 2.3 策略表 `policy`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| name | VARCHAR(255) NOT NULL | 策略名称 |
| effect | VARCHAR(16) NOT NULL | ALLOW / DENY |
| priority | INT NOT NULL DEFAULT 0 | 优先级，数大优先 |
| condition_json | TEXT NOT NULL | 条件 JSON，见上 condition 结构 |
| enabled | TINYINT(1) NOT NULL DEFAULT 1 | 是否启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

- 索引：`enabled`、`priority DESC`（评估时按启用+优先级排序）

### 2.4 审计日志表（可选，最小可用可后续加）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| subject_id | VARCHAR(64) | 主体 |
| resource_type | VARCHAR(64) | 资源类型 |
| resource_id | VARCHAR(128) | 资源 ID |
| action | VARCHAR(64) | 操作 |
| decision | VARCHAR(16) | ALLOW / DENY |
| policy_id | BIGINT | 命中策略 ID |
| requested_at | DATETIME | 请求时间 |

---

## 3. 评估逻辑（PDP）要点

1. 根据 `subjectId` 查 `subject_attribute` 得到主体属性 Map。  
2. 根据 `resourceType` + `resourceId` 查 `resource_attribute` 得到资源属性 Map。  
3. 取 `enabled = 1` 的 `policy`，按 `priority DESC` 排序。  
4. 逐条策略：用请求的 subject/resource/action 及可选的 environment 与 `condition_json` 匹配；  
   - 条件满足则立即返回该条策略的 `effect`（DENY 优先可先处理 DENY 或按优先级约定）。  
5. 若均不命中则默认拒绝（Deny）。

---

## 4. 技术栈

- Java 8  
- Spring Boot 2.7.x（兼容 Java 8）  
- Spring Data JPA + 单数据源  
- H2（开发）/ MySQL（生产可替换）  
- REST JSON API  

以上为最小可用 ABAC 的接口与数据表设计要点，可直接用于实现与建表。
