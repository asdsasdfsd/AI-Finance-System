# AI财务管理系统技术文档

## 1. 系统概述

AI财务管理系统是一个企业财务管理平台，可帮助企业自动化收支分类，生成财务报表，并提供基础财务数据分析。系统采用模块化设计，通过React前端框架和Spring Boot后端服务，结合关系型数据库，实现财务数据处理流程。

### 1.1 核心功能

- 公司信息注册与管理
- 收入与支出的智能分类
- 财务数据录入与管理
- 财务报表自动生成
- 用户权限管理

### 1.2 技术栈

- **前端**：
    - React + Ant Design
    - Axios（HTTP客户端）
    - React Router（路由管理）
    - Redux（状态管理）
    - Webpack/Vite（构建工具）
- **后端**：
    - Spring Boot（Java）
    - Spring Security（认证与授权）
    - Spring Data JPA（数据访问层）
    - OpenAPI（API文档管理）
- **数据库**：
    - MySQL（主数据库）
- **缓存**：
    - Redis（用于分类数据和用户会话）
- **API设计**：
    - RESTful API
- **认证**：
    - OAuth 2.0 / SSO
    - JWT
- **CI/CD**：
    - Jenkins（持续集成）
    - ArgoCD（持续部署）
    - GitHub Actions（GitOps流程）
- **基础设施**：
    - Kubernetes (AWS EKS)
    - Terraform（基础设施即代码）
    - AWS（云服务提供商）
- **代码质量与安全**：
    - SonarQube（代码质量检查）
    - ZAP（动态应用安全测试）
    - CodeQL（静态代码安全分析）
- **版本控制**：
    - GitHub（代码托管与版本控制）

## 2. 系统架构

### 2.1 整体架构图

```
┌───────────────────────────────────────┐
│                                       │
│    客户端应用 (React + Ant Design)     │
│    ┌───────────┐     ┌───────────┐    │
│    │ Axios     │     │ React     │    │
│    │ (HTTP客户端)│     │ Router    │    │
│    └───────────┘     └───────────┘    │
│                                       │
└───────────────┬───────────────────────┘
                │ HTTPS
                ▼
┌───────────────────────────────────────┐
│                                       │
│    API服务 (Spring Boot)              │
│    ┌───────────┐     ┌───────────┐    │
│    │ Spring    │     │ OpenAPI   │    │
│    │ Security  │     │ 文档      │    │
│    └───────────┘     └───────────┘    │
│                                       │
└───────────────┬───────────────────────┘
                │
                ▼
┌────────────────────────────────────────┐
│                                        │
│  数据层                                │
│  ┌──────────┐         ┌──────────┐     │
│  │          │         │          │     │
│  │ MySQL    │         │ Redis    │     │
│  │          │         │ 缓存     │     │
│  └──────────┘         └──────────┘     │
│                                        │
└────────────────┬───────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────┐
│                                        │
│    监控与日志                           │
│    ┌───────────┐      ┌───────────┐    │
│    │ 应用监控   │      │ 审计日志   │    │
│    └───────────┘      └───────────┘    │
│                                        │
└────────────────────────────────────────┘
```

### 2.2 前端架构

采用基于组件的架构，以Ant Design组件库为基础，主要模块包括：

- **核心模块**：
    - **布局与主题**：响应式页面布局，Ant Design Layout组件
    - **认证模块**：登录、注册，基于OAuth 2.0/JWT
    - **权限管理**：基于角色的组件渲染控制
    - **路由管理**：React Router处理页面导航
- **业务模块**：
    - 公司管理模块
    - 收入管理模块
    - 支出管理模块
    - 报表模块
- **共享组件**：
    - 数据表格组件（Ant Design Table）
    - 表单组件（Ant Design Form）
    - 可视化图表组件（Ant Design Charts）
    - 通知组件（Ant Design Notification）
- **状态管理**：
    - Redux用于全局状态
    - React Context用于主题设置
- **API通信**：
    - Axios处理HTTP请求
    - 请求拦截器处理认证和错误

### 2.3 后端架构

采用Spring Boot分层架构设计：

- **控制器层（Controller）**：RESTful API接口，使用OpenAPI文档
- **服务层（Service）**：业务逻辑实现
- **数据访问层（Repository）**：使用Spring Data JPA进行数据库操作
- **模型层（Entity/DTO）**：数据模型和传输对象
- **工具类（Util）**：共享功能和常量
- **配置类（Config）**：Spring配置和Bean定义

后端服务按功能模块划分：

- **用户与认证服务**：
    - 用户管理
    - 身份验证（OAuth 2.0 + JWT）
    - Spring Security授权管理
- **公司管理服务**：
    - 公司信息管理
    - 部门管理
- **财务交易服务**：
    - 收入记录管理
    - 支出记录管理
    - 收支分类管理
- **报表服务**：
    - 财务报表生成
    - 数据聚合和分析
    - 数据导出（CSV/PDF）

### 2.4 数据库架构

采用关系型数据库，主要包括以下数据模型：

- **用户与权限管理**
    - 用户表
    - 角色表
    - 用户角色关联表
- **公司信息管理**
    - 公司基本信息表
    - 部门表
- **收支分类体系**
    - 收入分类表
    - 支出分类表
- **交易记录**
    - 收入记录表
    - 支出记录表
    - 交易标签表
- **审计日志**
    - 操作日志表

Redis缓存内容：
- 分类数据缓存
- 用户会话缓存
- 频繁访问的配置信息

## 3. 数据流

### 3.1 用户注册与登录流程

```
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
│          │    │           │    │           │    │          │
│  用户     │───►│ 登录页    │───►│ 认证服务  │───►│ 用户数据库│
│          │    │           │    │           │    │          │
└──────────┘    └───────────┘    └─────┬─────┘    └──────────┘
                                       │
                                       ▼
                                 ┌───────────┐
                                 │           │
                                 │ 生成JWT   │
                                 │           │
                                 └─────┬─────┘
                                       │
                                       ▼
                               ┌───────────────┐
                               │               │
                               │ 返回令牌和用户信息│
                               │               │
                               └───────────────┘
```

### 3.2 公司注册流程

```
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
│          │    │           │    │           │    │          │
│  用户     │───►│ 公司表单  │───►│ 公司服务  │───►│ 数据库   │
│          │    │           │    │           │    │          │
└──────────┘    └───────────┘    └─────┬─────┘    └──────────┘
                                       │
                                       ▼
                               ┌───────────────┐
                               │               │
                               │ 返回结果      │
                               │               │
                               └───────────────┘
```

### 3.3 收支记录流程

```
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
│          │    │           │    │           │    │          │
│  用户     │───►│ 交易表单  │───►│ 智能分类  │───►│ 推荐分类 │
│          │    │           │    │           │    │          │
└──────────┘    └───────────┘    └───────────┘    └────┬─────┘
                                                       │
                                                       ▼
                                                 ┌──────────┐
                                                 │          │
                                                 │ 确认分类 │
                                                 │          │
                                                 └────┬─────┘
                                                      │
                                                      ▼
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
│          │    │           │    │           │    │          │
│ 交易服务  │◄───┤ 数据验证  │◄───┤ API控制器 │◄───┤ 提交表单 │
│          │    │           │    │           │    │          │
└─────┬────┘    └───────────┘    └───────────┘    └──────────┘
      │
      ▼
┌──────────┐   
│          │  
│ 数据库   │  
│          │  
└──────────┘  
```

### 3.4 报表生成流程

```
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
│          │    │           │    │           │    │          │
│  用户     │───►│ 报表参数  │───►│ 报表服务  │───►│ 数据库   │
│          │    │           │    │           │    │          │
└──────────┘    └───────────┘    └─────┬─────┘    └──────────┘
                                       │
                                       ▼
                                 ┌───────────┐    
                                 │           │    
                                 │ 数据聚合  │
                                 │           │    
                                 └─────┬─────┘    
                                       │
                                       ▼
                               ┌───────────────┐
                               │               │
                               │ 返回结果      │
                               │               │
                               └───────────────┘
```

## 4. API接口设计

### 4.1 API设计原则

1. **版本控制**：所有API路径都包含版本信息，如`/api/v1/...`
2. **统一响应格式**：所有API返回统一的JSON格式
3. **标准HTTP状态码**：使用合适的HTTP状态码表示请求结果
4. **分页一致性**：所有返回列表的API采用统一的分页参数和响应格式
5. **语义化URL**：URL设计遵循RESTful原则
6. **严格验证**：所有输入都进行严格的验证

#### 4.1.1 API版本管理策略

1. **版本号规范**：
   - 主版本号(v1, v2)：不兼容的API变更
   - 次版本号(v1.1, v1.2)：向后兼容的功能性新增
   - 修订号(v1.1.1)：向后兼容的问题修正

2. **版本控制方法**：
   - URL路径版本控制：`/api/v1/resources`
   - 所有API变更必须维护变更日志(CHANGELOG.md)

3. **版本兼容性策略**：
   - 主版本更新：旧版API保留至少6个月并标记为废弃
   - 次版本更新：完全向后兼容，客户端无需修改
   - API废弃流程：标记废弃(Deprecated) → 发送警告 → 最终移除

4. **版本迁移支持**：
   - 提供版本迁移指南文档
   - 在响应头中包含可用的最新API版本信息
   - 自动化测试确保向后兼容性

### 4.2 统一响应格式

#### 4.2.1 成功响应

```json
{
  "status": "success",
  "data": {
    // 实际返回数据
  },
  "meta": {
    // 分页信息、总计数等元数据
  }
}
```

#### 4.2.2 错误响应

```json
{
  "status": "error",
  "error": {
    "code": "ERROR_CODE",
    "message": "用户友好的错误消息",
    "details": "错误详细信息（可选）",
    "fields": {
      // 字段级别错误（表单验证错误）
    }
  }
}
```
#### 4.2.3 错误码定义与处理机制

##### 错误码结构

错误码采用分层结构：`AAA-BBB-CCC`
- AAA: 服务标识符（AUTH, COMP, TRAN, REPT）
- BBB: 错误类型（VAL, BIZ, SYS, SEC）
- CCC: 具体错误序号（001-999）

##### 标准错误码列表

| 错误码 | 描述 | HTTP状态码 |
|-------|------|-----------|
| **认证相关** |||
| AUTH-SEC-001 | 无效的认证凭证 | 401 |
| AUTH-SEC-002 | 会话已过期 | 401 |
| AUTH-SEC-003 | 无访问权限 | 403 |
| AUTH-VAL-001 | 用户名或密码错误 | 400 |
| AUTH-BIZ-001 | 账户已锁定 | 403 |
| **公司相关** |||
| COMP-VAL-001 | 公司信息验证失败 | 400 |
| COMP-BIZ-001 | 公司已存在 | 409 |
| COMP-BIZ-002 | 公司不存在 | 404 |
| **交易相关** |||
| TRAN-VAL-001 | 交易数据验证失败 | 400 |
| TRAN-BIZ-001 | 交易记录不存在 | 404 |
| TRAN-BIZ-002 | 分类不存在 | 400 |
| **系统错误** |||
| SYS-SYS-001 | 内部服务器错误 | 500 |
| SYS-SYS-002 | 数据库操作失败 | 500 |
| SYS-SYS-003 | 第三方服务调用失败 | 502 |

##### 错误处理流程

1. **前端错误处理**：
   - 全局错误拦截器捕获所有API错误
   - 根据错误码展示适当的用户提示
   - 会话过期自动跳转到登录页
   - 表单错误自动映射到对应字段

2. **后端错误处理**：
   - 全局异常处理器(@ControllerAdvice)
   - 业务异常(BusinessException)转换为标准错误响应
   - 系统异常记录详细日志并返回通用错误信息
   - 输入验证异常自动映射到字段错误

3. **错误记录与监控**：
   - 所有500错误自动告警
   - 错误率超阈值触发系统警报
   - 错误日志包含请求ID便于追踪


### 4.3 认证API

#### 4.3.1 登录

- **URL**: `/api/v1/auth/login`
    
- **方法**: `POST`
    
- **请求体**:
    
    ```json
    {
      "username": "string",
      "password": "string",
      "remember_me": "boolean"
    }
    ```
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "token": "string",
        "token_type": "Bearer",
        "expires_in": "number",
        "user": {
          "id": "string",
          "username": "string",
          "email": "string",
          "full_name": "string",
          "role": "string"
        }
      }
    }
    ```
    

#### 4.3.2 注销

- **URL**: `/api/v1/auth/logout`
    
- **方法**: `POST`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "message": "注销成功"
      }
    }
    ```
    

### 4.4 公司API

#### 4.4.1 创建公司

- **URL**: `/api/v1/companies`
    
- **方法**: `POST`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **请求体**:
    
    ```json
    {
      "company_name": "string",
      "address": "string",
      "city": "string",
      "state_province": "string",
      "postal_code": "string",
      "country": "string",
      "phone_number": "string",
      "email": "string",
      "website": "string",
      "registration_number": "string",
      "tax_id": "string",
      "fiscal_year_start": "string (MM-DD)",
      "default_currency": "string"
    }
    ```
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "company_id": "string",
        "company_name": "string",
        "created_at": "datetime",
        "status": "string"
      }
    }
    ```
    

#### 4.4.2 获取公司信息

- **URL**: `/api/v1/companies/{company_id}`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "company_id": "string",
        "company_name": "string",
        "address": "string",
        "city": "string",
        "state_province": "string",
        "postal_code": "string",
        "country": "string",
        "phone_number": "string",
        "email": "string",
        "website": "string",
        "registration_number": "string",
        "tax_id": "string",
        "fiscal_year_start": "string",
        "default_currency": "string",
        "created_at": "datetime",
        "updated_at": "datetime",
        "status": "string"
      }
    }
    ```
    

#### 4.4.3 获取公司列表

- **URL**: `/api/v1/companies`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **查询参数**:
    
    - `page`: 页码（默认1）
    - `page_size`: 每页数量（默认20）
    - `search`: 搜索关键字（可选）
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": [
        {
          "company_id": "string",
          "company_name": "string",
          "country": "string",
          "status": "string",
          "created_at": "datetime"
        }
      ],
      "meta": {
        "total": "number",
        "page": "number",
        "page_size": "number",
        "total_pages": "number"
      }
    }
    ```
    

#### 4.4.4 更新公司信息

- **URL**: `/api/v1/companies/{company_id}`
- **方法**: `PUT`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**: 同创建公司
- **响应**: 同获取公司信息响应

#### 4.4.5 删除公司

- **URL**: `/api/v1/companies/{company_id}`
- **方法**: `DELETE`
- **请求头**: `Authorization: Bearer {token}`
- **响应**: `HTTP 204 No Content`

### 4.5 收入分类API

#### 4.5.1 获取收入分类列表

- **URL**: `/api/v1/companies/{company_id}/income-categories`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **查询参数**:
    
    - `parent_id`: 父分类ID（可选）
    - `include_system`: 是否包含系统分类（默认true）
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": [
        {
          "category_id": "string",
          "name": "string",
          "description": "string",
          "parent_category_id": "string",
          "is_system": "boolean",
          "created_at": "datetime",
          "updated_at": "datetime",
          "child_categories": [
            // 嵌套的子分类
          ]
        }
      ],
      "meta": {
        "total": "number"
      }
    }
    ```
    

#### 4.5.2 创建收入分类

- **URL**: `/api/v1/companies/{company_id}/income-categories`
    
- **方法**: `POST`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **请求体**:
    
    ```json
    {
      "name": "string",
      "description": "string",
      "parent_category_id": "string",
      "is_active": "boolean"
    }
    ```
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "category_id": "string",
        "name": "string",
        "parent_category_id": "string",
        "created_at": "datetime"
      }
    }
    ```
    

#### 4.5.3 修改收入分类

- **URL**: `/api/v1/companies/{company_id}/income-categories/{category_id}`
- **方法**: `PUT`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**: 同创建收入分类
- **响应**: 同创建收入分类响应

#### 4.5.4 删除收入分类

- **URL**: `/api/v1/companies/{company_id}/income-categories/{category_id}`
- **方法**: `DELETE`
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
    - `move_transactions_to`: 将关联交易移动到的分类ID（可选）
- **响应**: `HTTP 204 No Content`

### 4.6 支出分类API

与收入分类API结构类似，将URL中的`income-categories`替换为`expense-categories`

### 4.7 收入交易API

#### 4.7.1 创建收入记录

- **URL**: `/api/v1/companies/{company_id}/income-transactions`
    
- **方法**: `POST`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **请求体**:
    
    ```json
    {
      "category_id": "string",
      "amount": "number",
      "currency": "string",
      "transaction_date": "date",
      "description": "string",
      "reference_number": "string",
      "payment_method": "string",
      "is_recurring": "boolean",
      "is_taxable": "boolean",
      "tags": ["string"]
    }
    ```
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "transaction_id": "string",
        "category_id": "string",
        "category_name": "string",
        "amount": "number",
        "transaction_date": "date",
        "description": "string",
        "created_at": "datetime"
      }
    }
    ```
    

#### 4.7.2 查询收入记录

- **URL**: `/api/v1/companies/{company_id}/income-transactions`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **查询参数**:
    - `start_date`: 开始日期
    - `end_date`: 结束日期
    - `category_id`: 分类ID（可选）
    - `search`: 搜索关键字（可选）
    - `page`: 页码（默认1）
    - `page_size`: 每页记录数（默认20）
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": [
        {
          "transaction_id": "string",
          "category_id": "string",
          "category_name": "string",
          "amount": "number",
          "currency": "string",
          "transaction_date": "date",
          "description": "string",
          "reference_number": "string",
          "payment_method": "string",
          "is_taxable": "boolean",
          "is_recurring": "boolean",
          "created_at": "datetime",
          "updated_at": "datetime",
          "tags": ["string"]
        }
      ],
      "meta": {
        "total": "number",
        "page": "number",
        "page_size": "number",
        "total_pages": "number",
        "total_amount": "number"
      }
    }
    ```
    

#### 4.7.3 获取收入记录详情

- **URL**: `/api/v1/companies/{company_id}/income-transactions/{transaction_id}`
- **方法**: `GET`
- **请求头**: `Authorization: Bearer {token}`
- **响应**: 同单个交易数据格式

#### 4.7.4 更新收入记录

- **URL**: `/api/v1/companies/{company_id}/income-transactions/{transaction_id}`
- **方法**: `PUT`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**: 同创建收入记录
- **响应**: 同创建收入记录响应

#### 4.7.5 删除收入记录

- **URL**: `/api/v1/companies/{company_id}/income-transactions/{transaction_id}`
- **方法**: `DELETE`
- **请求头**: `Authorization: Bearer {token}`
- **响应**: `HTTP 204 No Content`

### 4.8 支出交易API

与收入交易API结构类似，将URL中的`income-transactions`替换为`expense-transactions`

### 4.9 报表API

#### 4.9.1 收入支出汇总报表

- **URL**: `/api/v1/companies/{company_id}/reports/income-expense-summary`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **查询参数**:
    - `start_date`: 开始日期
    - `end_date`: 结束日期
    - `period`: 汇总周期（daily, weekly, monthly, quarterly, yearly）
    - `format`: 响应格式（json, csv, pdf）
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "period_info": {
          "start_date": "date",
          "end_date": "date",
          "period_type": "string"
        },
        "periods": [
          {
            "period_label": "string",
            "start_date": "date",
            "end_date": "date",
            "total_income": "number",
            "total_expense": "number",
            "net": "number"
          }
        ],
        "summary": {
          "total_income": "number",
          "total_expense": "number",
          "net": "number"
        },
        "chart_data": {
          "labels": ["string"],
          "datasets": [
            {
              "label": "Income",
              "data": ["number"]
            },
            {
              "label": "Expense",
              "data": ["number"]
            },
            {
              "label": "Net",
              "data": ["number"]
            }
          ]
        }
      },
      "meta": {
        "generated_at": "datetime"
      }
    }
    ```
    

#### 4.9.2 分类收入报表

- **URL**: `/api/v1/companies/{company_id}/reports/income-by-category`
    
- **方法**: `GET`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **查询参数**:
    - `start_date`: 开始日期
    - `end_date`: 结束日期
    - `format`: 响应格式（json, csv, pdf）
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "period_info": {
          "start_date": "date",
          "end_date": "date"
        },
        "categories": [
          {
            "category_id": "string",
            "category_name": "string",
            "amount": "number",
            "percentage": "number",
            "transaction_count": "number"
          }
        ],
        "total": "number",
        "chart_data": {
          "labels": ["string"],
          "datasets": [
            {
              "label": "Amount",
              "data": ["number"]
            }
          ]
        }
      },
      "meta": {
        "generated_at": "datetime"
      }
    }
    ```
    

#### 4.9.3 分类支出报表

- **URL**: `/api/v1/companies/{company_id}/reports/expense-by-category`
- **方法**: `GET`
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**: 同分类收入报表
- **响应**: 结构同分类收入报表

### 4.10 AI功能API

#### 4.10.1 智能分类

- **URL**: `/api/v1/companies/{company_id}/ai/classify-transaction`
    
- **方法**: `POST`
    
- **请求头**: `Authorization: Bearer {token}`
    
- **请求体**:
    
    ```json
    {
      "type": "income|expense",
      "amount": "number",
      "description": "string",
      "transaction_date": "date"
    }
    ```
    
- **响应**:
    
    ```json
    {
      "status": "success",
      "data": {
        "suggested_categories": [
          {
            "category_id": "string",
            "category_name": "string",
            "confidence": "number"
          }
        ],
        "similar_transactions": [
          {
            "description": "string",
            "amount": "number",
            "category_name": "string"
          }
        ]
      }
    }
    ```
    
### 4.11 API安全与限流策略

#### 4.11.1 API限流机制

API限流采用多层次策略以确保系统稳定性和公平使用：

1. **全局限流**：限制整个API网关的请求总量
    - 生产环境限制：10,000请求/分钟
    - 超限策略：请求排队，延迟响应
2. **用户级限流**：基于用户身份的限制
    - 普通用户：60请求/分钟
    - 高级用户：300请求/分钟
    - 超限策略：返回429状态码(Too Many Requests)
3. **IP级限流**：防止单一来源的过度请求
    - 未认证请求：30请求/分钟
    - 超限策略：临时禁止访问(5分钟)
4. **功能级限流**：对特定API端点的限制
    - 高资源消耗端点(如报表生成)：10请求/分钟
    - 标准CRUD操作：100请求/分钟

##### 技术实现：

```java
@Configuration
public class RateLimitingConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = getUserIdFromRequest(exchange.getRequest());
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
    
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = getClientIp(exchange.getRequest());
            return Mono.just(ip);
        };
    }
    
    @Bean
    public RateLimiter userRateLimiter() {
        return new RedisRateLimiter(60, 60);  // replenishRate, burstCapacity
    }
    
    @Bean
    public RateLimiter reportRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }
}
```


#### 4.11.2 防滥用策略

为防止API滥用和恶意攻击，实施以下安全措施：

1. **请求验证**：
    - 强制HTTPS
    - 验证Content-Type
    - 检查请求体大小限制
2. **异常检测**：
    - 监控异常请求模式
    - 识别高频失败尝试
    - 标记可疑行为模式
3. **自动防护**：
    - 自动限制异常流量
    - 针对可疑IP实施渐进式限制
    - 触发安全警报通知管理员
4. **身份验证强化**：
    - 失败登录尝试限制
    - 账户锁定机制
    - 敏感操作二次验证

#### 4.11.3 响应头安全配置

所有API响应包含以下安全相关HTTP头：

```txt
Cache-Control: no-store
Content-Security-Policy: default-src 'self'
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

#### 4.11.4 限流响应格式

当API请求超过限制时，返回标准429响应：

```json
{
  "status": "error",
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "请求频率超过限制，请稍后再试",
    "details": "当前限制: 60次/分钟，重置时间: 2023-04-15T10:15:30Z",
    "retry_after": 45
  }
}
```

#### 4.11.5 监控与告警

限流和安全事件触发以下监控流程：

1. 限流事件记录到专用日志
2. 限流计数器暴露为Prometheus指标
3. 设置阈值触发告警通知
4. 生成周期性安全报告
## 5. 前端组件设计

### 5.1 页面结构

```
┌──────────────────────────────────────────────────────────┐
│ 顶部导航栏（Logo、搜索、用户菜单）                         │
├──────────────┬───────────────────────────────────────────┤
│              │                                           │
│              │                                           │
│              │                                           │
│              │                                           │
│  侧边菜单    │             内容区域                       │
│              │                                           │
│              │                                           │
│              │                                           │
│              │                                           │
│              │                                           │
│              │                                           │
└──────────────┴───────────────────────────────────────────┘
```

### 5.2 主要页面

#### 5.2.1 仪表盘页面

- 财务概览卡片
- 收入支出趋势图
- 本月收支分类饼图
- 最近交易列表

#### 5.2.2 公司管理页面

- 公司信息表单
- 公司设置选项卡

#### 5.2.3 收入管理页面

- 收入分类管理
- 收入记录表格
- 添加收入记录表单
- 收入统计数据卡片

#### 5.2.4 支出管理页面

- 支出分类管理
- 支出记录表格
- 添加支出记录表单
- 支出统计数据卡片

#### 5.2.5 报表页面

- 报表类型选择卡片
- 时间范围选择
- 图表展示区域
- 数据表格展示区

### 5.3 核心组件

#### 5.3.1 布局组件

```jsx
<AppLayout>
  <AppHeader logo={logoUrl} search={true} userMenu={true} />
  <AppSidebar 
    menu={menuItems}
    collapsed={sidebarCollapsed}
    onCollapse={handleCollapse}
  />
  <AppContent>
    {children}
  </AppContent>
</AppLayout>
```

#### 5.3.2 交易表单组件

```jsx
<TransactionForm 
  type="income" // or "expense"
  categories={categories}
  paymentMethods={paymentMethods}
  currencies={currencies}
  onSubmit={handleSubmit}
  initialValues={transaction} // for editing
  onCancel={handleCancel}
  loading={isSubmitting}
  useAIClassification={true}
/>
```

#### 5.3.3 分类选择器组件

```jsx
<CategorySelector 
  type="income" // or "expense"
  companyId={companyId}
  value={selectedCategory}
  onChange={handleCategoryChange}
  allowAddNew={hasPermission('create_category')}
  searchable={true}
/>
```

#### 5.3.4 交易列表组件

```jsx
<TransactionList
  type="income" // or "expense"
  data={transactions}
  loading={loading}
  onEdit={handleEdit}
  onDelete={handleDelete}
  pagination={{
    current: page,
    pageSize: pageSize,
    total: totalItems,
    onChange: handlePageChange
  }}
  searchEnabled={true}
  onSearch={handleSearch}
/>
```

#### 5.3.5 图表组件

```jsx
<Chart
  type="bar" // bar, line, pie
  data={chartData}
  height={400}
  width="100%"
  showLegend={true}
  title="Monthly Revenue"
/>
```

## 6. AI功能实现

### 6.1 智能分类

系统使用基于规则和历史数据的算法实现交易自动分类：

#### 6.1.1 分类策略

1. **规则匹配**
    - 关键词匹配（描述中的关键词）
    - 金额范围匹配（特定金额区间对应特定分类）
    - 时间模式匹配（如月初、月末特定交易）
    
2. **历史交易分析**
    - 基于用户历史分类决策的相似度匹配
    - 常用分类的优先推荐
    - 最近使用分类的权重提升

#### 6.1.2 工作流程

1. 用户输入新交易信息
2. 系统提取交易的关键特征（描述、金额、日期）
3. 应用规则引擎进行初步分类匹配
4. 查找历史中相似的交易记录
5. 生成推荐分类列表及置信度
6. 用户确认或修改分类
7. 系统记录用户决策，用于改进未来分类

## 7. 安全设计

### 7.1 认证与授权

- OAuth 2.0 授权框架
- JWT (JSON Web Token) 实现无状态认证
- Spring Security配置
- 基于角色的访问控制（RBAC）
- 密码加密存储（使用BCrypt）
- 登录失败限制与账户锁定机制
#### 7.1.1 详细权限设计

##### 角色设计

1. **系统角色**：
   - 系统管理员(SYSTEM_ADMIN)：完全访问权限
   - 公司管理员(COMPANY_ADMIN)：管理单个公司的所有功能
   - 财务经理(FINANCE_MANAGER)：管理财务数据和报表
   - 财务操作员(FINANCE_OPERATOR)：录入和编辑基础财务数据
   - 报表查看者(REPORT_VIEWER)：只能查看报表，无编辑权限
   - 审计员(AUDITOR)：查看所有数据但无编辑权限

2. **权限矩阵**：

| 权限代码 | 系统管理员 | 公司管理员 | 财务经理 | 财务操作员 | 报表查看者 | 审计员 |
|---------|----------|----------|---------|----------|----------|-------|
| 用户管理  | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| 公司管理  | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| 分类管理  | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| 创建交易  | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| 编辑交易  | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| 删除交易  | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| 生成报表  | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| 导出数据  | ✓ | ✓ | ✓ | ✗ | ✗ | ✓ |
| 系统设置  | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 审计日志  | ✓ | ✓ | ✗ | ✗ | ✗ | ✓ |

##### 权限实现

1. **技术实现**：
   - Spring Security注解(@PreAuthorize)控制方法级访问
   - 数据库表：用户表、角色表、权限表、角色权限关联表
   - JWT令牌中包含用户角色信息

2. **UI权限控制**：
   - 基于用户角色动态渲染菜单项
   - 禁用/隐藏无权访问的按钮和功能
   - 路由守卫(Route Guards)拦截无权访问的页面

3. **特殊权限控制**：
   - 数据级权限：限制用户只能访问特定公司/部门数据
   - 功能级权限：特定功能的使用权限(如批量导入)
   - 字段级权限：敏感字段(如薪资数据)的访问控制

### 7.2 数据安全

- HTTPS传输加密 (TLS 1.2+)
- 敏感数据字段加密
- 参数化查询防SQL注入
- XSS防护 (内容安全策略)
- CSRF防护 (令牌验证)
- 安全响应头配置

### 7.3 代码与应用安全

- SonarQube代码质量与安全扫描
- 将SonarQube和CodeQL集成到GitHub流水线中
- 依赖项安全检查
- 定期漏洞评估
- 容器镜像安全扫描

### 7.4 日志与审计

- 分级日志系统 (INFO, WARN, ERROR)
- 用户操作审计日志
- 敏感操作双重验证
- 登录/登出及权限变更记录
- 异常访问监控与告警

### 7.5 数据备份与恢复

#### 7.5.1 备份策略

1. **备份类型与频率**：
   - 完整备份：每日一次（凌晨低峰期）
   - 增量备份：每小时一次
   - 事务日志备份：每15分钟一次
   - 数据定义导出：每次schema变更后

2. **备份内容**：
   - 数据库完整内容
   - 系统配置文件
   - 上传的附件和文档
   - 应用程序代码和依赖

3. **备份存储**：
   - 主备份：AWS S3标准存储
   - 副本备份：不同区域的S3存储
   - 关键数据：加密存储
   - 保留策略：
     * 日备份保留30天
     * 周备份保留3个月
     * 月备份保留1年

#### 7.5.2 恢复策略

1. **恢复目标**：
   - 恢复点目标(RPO)：15分钟内
   - 恢复时间目标(RTO)：关键业务2小时内

2. **恢复场景与流程**：
   - **单表/数据恢复**：
     1. 从最近备份中提取所需表/数据
     2. 应用事务日志恢复到指定时间点
     3. 导入到临时环境验证
     4. 确认后导入生产环境

   - **完整系统恢复**：
     1. 启动新的基础设施实例
     2. 恢复最近的完整备份
     3. 应用增量备份和事务日志
     4. 验证系统完整性
     5. 切换服务到恢复的实例

   - **灾难恢复**：
     1. 在备用区域启动基础设施
     2. 从备份存储恢复数据
     3. 更新DNS和负载均衡
     4. 验证系统功能和数据

## 8. 部署架构

### 8.1 开发环境

- 本地开发环境
    - 前端: Node.js + npm + Webpack/Vite
    - 后端: JDK 11/17 + Maven
    - 数据库: MySQL
    - Redis: Docker容器
    - Docker: 用于容器化开发环境
- 依赖管理
    - 前端: package.json (npm/yarn)
    - 后端: pom.xml (Maven)
- 开发工具
    - IDE: IntelliJ IDEA / VS Code
    - API测试: Postman / Swagger UI
    - 数据库管理: MySQL Workbench

### 8.2 测试环境

- 容器化部署: Docker Compose
- 应用服务器: Spring Boot内嵌Tomcat
- 数据库服务器: MySQL
- Redis服务: Redis容器
- CI/CD流水线: Jenkins
- 代码质量: SonarQube

### 8.3 生产环境 (AWS)

- 计算资源: AWS EC2 实例
- 数据库服务: AWS RDS MySQL
- 缓存服务: AWS ElastiCache (Redis)
- 静态资源托管: AWS S3
- 负载均衡: AWS ELB + Nginx
- 监控: CloudWatch
- 可选: 简化版Kubernetes集群 (EKS)

### 8.4 性能测试与优化

#### 8.4.1 性能指标与目标

| 指标类型 | 性能目标 | 测量方法 |
|---------|---------|---------|
| 页面加载时间 | < 2秒 | Lighthouse, WebPageTest |
| API响应时间 | < 300ms (P95) | JMeter, 应用监控 |
| 数据库查询时间 | < 100ms (P95) | 慢查询日志分析 |
| 并发用户支持 | 100用户/服务实例 | 负载测试 |
| 报表生成时间 | < 5秒 (复杂报表) | 功能测试 |
| 内存使用 | < 2GB/服务实例 | 应用监控 |
| CPU使用 | 平均<40% | 应用监控 |

#### 8.4.2 拟性能测试计划

1. **单元性能测试**：
   - 测试范围：关键算法和组件
   - 工具：JMH (Java Microbenchmark Harness)
   - 执行时机：每次提交前

2. **API性能测试**：
   - 测试范围：所有关键API端点
   - 工具：JMeter, Postman/Newman
   - 场景：常规负载、峰值负载、长时间运行
   - 执行时机：每次迭代结束

3. **前端性能测试**：
   - 测试范围：关键用户流程和页面
   - 工具：Lighthouse, WebPageTest
   - 执行时机：每次前端重大变更

4. **负载测试**：
   - 测试目标：系统容量和扩展性
   - 并发用户数：50-500
   - 工具：JMeter, Gatling
   - 执行时机：每次重大架构变更

#### 8.4.3 性能优化策略

1. **前端优化**：
   - 资源压缩与合并
   - 懒加载组件和路由
   - 图片优化与CDN分发
   - 组件缓存策略

2. **API优化**：
   - 合理设计API粒度
   - 分页/过滤/排序的统一实现
   - 缓存常用数据（Redis）
   - 异步处理耗时操作

3. **数据库优化**：
   - 索引优化
   - 查询优化
   - 表分区策略
   - 读写分离考虑

4. **服务器优化**：
   - JVM调优
   - 连接池配置
   - 线程池优化
   - 云资源自动伸缩配置

### 8.5 基础设施即代码(IaC)实践

#### 8.5.1 Terraform架构

AI财务管理系统采用Terraform管理AWS基础设施，实现基础设施版本化和自动化部署。代码仓库结构如下：

```
infrastructure/
├── modules/               # 可重用的基础设施模块
│   ├── networking/        # VPC, 子网, 安全组等
│   ├── eks/               # Kubernetes集群配置
│   ├── rds/               # MySQL数据库配置
│   ├── elasticache/       # Redis缓存配置
│   └── monitoring/        # 监控和日志聚合
├── environments/          # 环境特定配置
│   ├── dev/
│   ├── test/
│   ├── staging/
│   └── prod/
└── variables/             # 环境变量定义
```
### 8.5.2 核心基础设施模块

#### 1. 网络模块示例

```hcl
# modules/networking/main.tf
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "3.14.0"

  name = "${var.project_name}-${var.environment}"
  cidr = var.vpc_cidr

  azs             = var.availability_zones
  private_subnets = var.private_subnet_cidrs
  public_subnets  = var.public_subnet_cidrs

  enable_nat_gateway   = true
  single_nat_gateway   = var.environment != "prod"
  enable_dns_hostnames = true

  tags = {
    Environment = var.environment
    Project     = var.project_name
    Terraform   = "true"
  }
}
```
#### 2. EKS集群模块示例

```hcl
# modules/eks/main.tf
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "18.26.3"

  cluster_name    = "${var.project_name}-${var.environment}"
  cluster_version = var.kubernetes_version

  vpc_id     = var.vpc_id
  subnet_ids = var.private_subnet_ids

  eks_managed_node_groups = {
    main = {
      desired_size = var.desired_nodes
      min_size     = var.min_nodes
      max_size     = var.max_nodes

      instance_types = ["t3.medium"]
      capacity_type  = "ON_DEMAND"
    }
  }

  node_security_group_additional_rules = {
    ingress_self_all = {
      description = "Allow nodes to communicate with each other"
      protocol    = "-1"
      from_port   = 0
      to_port     = 0
      type        = "ingress"
      self        = true
    }
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    Terraform   = "true"
  }
}
```
#### 3. RDS数据库模块示例

```hcl
# modules/rds/main.tf
module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "4.2.0"

  identifier = "${var.project_name}-${var.environment}"

  engine               = "mysql"
  engine_version       = "8.0.27"
  family               = "mysql8.0"
  major_engine_version = "8.0"
  instance_class       = var.db_instance_class

  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage

  db_name  = var.db_name
  username = var.db_username
  port     = 3306

  multi_az               = var.environment == "prod"
  subnet_ids             = var.database_subnet_ids
  vpc_security_group_ids = [var.db_security_group_id]

  maintenance_window      = "Mon:00:00-Mon:03:00"
  backup_window           = "03:00-06:00"
  backup_retention_period = var.environment == "prod" ? 30 : 7

  enabled_cloudwatch_logs_exports = ["audit", "error", "general", "slowquery"]

  tags = {
    Environment = var.environment
    Project     = var.project_name
    Terraform   = "true"
  }
}
```
### 8.5.3 环境配置管理

每个环境使用独立的配置文件和状态文件，确保环境间的隔离。

```hcl
# environments/prod/main.tf
provider "aws" {
  region = "us-west-2"
}

terraform {
  backend "s3" {
    bucket         = "finance-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-west-2"
    dynamodb_table = "finance-terraform-locks"
    encrypt        = true
  }
}

module "networking" {
  source            = "../../modules/networking"
  environment       = "prod"
  project_name      = "finance"
  vpc_cidr          = "10.0.0.0/16"
  availability_zones = ["us-west-2a", "us-west-2b", "us-west-2c"]
  private_subnet_cidrs = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  public_subnet_cidrs  = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"]
}

module "eks" {
  source           = "../../modules/eks"
  environment      = "prod"
  project_name     = "finance"
  vpc_id           = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids
  kubernetes_version = "1.23"
  desired_nodes    = 3
  min_nodes        = 3
  max_nodes        = 10
}

module "rds" {
  source              = "../../modules/rds"
  environment         = "prod"
  project_name        = "finance"
  db_instance_class   = "db.t3.large"
  allocated_storage   = 100
  max_allocated_storage = 500
  db_name             = "finance"
  db_username         = "admin"
  database_subnet_ids = module.networking.database_subnet_ids
  db_security_group_id = module.networking.db_security_group_id
}
```
### 8.5.4 CI/CD集成

基础设施更新通过CI/CD流水线自动部署，包含以下阶段：

1. **计划阶段(Plan)**：
    - 执行`terraform plan`
    - 生成变更报告
    - 存储变更计划用于后续应用
2. **审批阶段(Approval)**：
    - 针对生产环境变更需要手动审批
    - 审核变更的安全性和必要性
3. **应用阶段(Apply)**：
    - 执行`terraform apply`
    - 应用基础设施变更
4. **验证阶段(Verify)**：
    - 执行基础设施测试确认部署成功
    - 检查关键服务的可用性
