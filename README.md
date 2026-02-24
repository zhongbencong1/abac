# ABAC 最小可用版 (Java 8)

基于属性的访问控制（ABAC）最小实现：Spring Boot 2.7 + JPA + H2。

## 快速开始

```bash
mvn spring-boot:run
```

- 应用: http://localhost:8080  
- H2 控制台: http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:mem:abac`，用户名 `sa`，密码空）

## 接口说明

详见 [docs/ABAC-MVP-Design.md](docs/ABAC-MVP-Design.md)。

| 功能 | 方法 | 路径 |
|------|------|------|
| 鉴权检查 | POST | `/api/abac/check` |
| 主体属性 | GET/PUT/POST | `/api/subjects/{subjectId}/attributes` |
| 资源属性 | GET/PUT | `/api/resources/{resourceType}/{resourceId}/attributes` |
| 策略 CRUD | GET/POST/PUT/DELETE | `/api/policies` |
| 策略启用/停用 | PATCH | `/api/policies/{id}/enabled` |

## 示例：配置后发起鉴权

```bash
# 1. 设置主体属性
curl -X PUT http://localhost:8080/api/subjects/user-001/attributes \
  -H "Content-Type: application/json" \
  -d '{"department":"IT","role":"admin"}'

# 2. 设置资源属性
curl -X PUT http://localhost:8080/api/resources/order/order-123/attributes \
  -H "Content-Type: application/json" \
  -d '{"type":"order","ownerId":"user-001"}'

# 3. 创建策略：IT 部门可读 order
curl -X POST http://localhost:8080/api/policies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "IT read order",
    "effect": "ALLOW",
    "priority": 10,
    "enabled": true,
    "conditionJson": "{\"subject\":{\"department\":\"IT\"},\"resource\":{\"type\":\"order\"},\"action\":[\"read\"]}"
  }'

# 4. 鉴权检查
curl -X POST http://localhost:8080/api/abac/check \
  -H "Content-Type: application/json" \
  -d '{"subjectId":"user-001","resourceType":"order","resourceId":"order-123","action":"read"}'
```

预期返回：`{"allowed":true,"policyId":1,"message":null}`

## 数据表

- `subject_attribute`：主体属性  
- `resource_attribute`：资源属性  
- `policy`：策略（含 condition_json）  

建表语句见 `src/main/resources/schema.sql`（生产可改用 MySQL 执行）。
