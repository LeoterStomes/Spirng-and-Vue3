# Spring and Vue3 - 心理AI助手

一个基于 `Spring Boot + Vue 3` 的心理健康辅助平台，覆盖用户账号、AI咨询、心理测试、情绪日记、知识库、收藏体系与后台管理。

---

## 1. 项目总体架构（专业归类）

### 1.1 架构风格

- **前后端分离**：前端 `Vue3` 独立应用，后端 `Spring Boot` 提供 REST API。
- **分层单体架构**（后端）：`Controller -> Service -> Mapper -> DB`。
- **多端路由域**（前端）：
  - 前台用户域（首页、知识库、咨询、测试、日记、收藏）
  - 后台管理域（数据分析、用户管理、知识文章管理、咨询管理等）
  - 认证域（登录/注册）

### 1.2 技术栈归类

#### 前端（`vue3`）

- **核心框架**：Vue 3（Composition API）
- **构建工具**：Vite
- **路由与状态**：Vue Router 4、Pinia
- **UI组件**：Element Plus、Font Awesome
- **网络请求**：Axios（统一请求封装）
- **可视化/编辑**：Chart.js、ECharts、WangEditor

#### 后端（`springboot`）

- **核心框架**：Spring Boot 3.4.x
- **数据访问**：MyBatis-Plus 3.5.x
- **数据库**：MySQL（主环境） + H2（测试）
- **缓存/中间件**：Redis
- **认证授权**：JWT + Spring Security
- **AI能力**：Spring AI（接入 OpenAI 兼容接口）
- **文件与工具**：文件上传业务模块、Hutool、Fastjson2
- **文档与调试**：SpringDoc + Knife4j

---

## 2. 后端实现方法分类（By Module）

### 2.1 用户与认证

- `UserController` + `UserService`：注册、登录、资料维护、密码更新。
- JWT 负责前后端会话态，前端存储 token 并在路由守卫中校验。
- Spring Security 负责认证链路与接口保护。

### 2.2 心理咨询（AI对话）

- `PsychologicalChatController`、`ConsultationSessionService`、`ConsultationMessageService`。
- 采用会话 + 消息模型，支持上下文对话落库。
- Spring AI 提供模型调用能力，业务层包装 Prompt 与结构化输出。

### 2.3 心理测试与报告

- `PsychologicalTestController`、`PsychologicalTestReportService`、`PsychologicalTestReportMapper`。
- 处理流程：
  1. 前端提交答案；
  2. 后端先创建报告记录；
  3. 异步生成 AI 报告（状态流转：`PROCESSING -> COMPLETED/FAILED`）；
  4. 前端轮询查看结果并可一键进入咨询。

### 2.4 情绪日记与AI分析队列

- `EmotionDiaryController` + `EmotionDiaryService`：记录、分页、统计、详情。
- `AiAnalysisTaskController` + `AiAnalysisTaskService`：异步分析任务编排与状态管理。
- 前端定时轮询分析结果，支持风险等级和建议展示。

### 2.5 知识库与收藏

- `KnowledgeArticleController` / `KnowledgeCategoryController`：
  - 文章 CRUD、状态发布/下线、分页筛选、统计。
  - 分类树与前台分类导航。
- `UserFavoriteController` / `UserFavoriteService`：收藏/取消收藏/状态查询。

### 2.6 文件管理

- `FileController`、`FileService`、`SimpleFileService`、`BussinessFileValidationService`。
- 支持业务关联文件（如头像、文章封面），并用于前端上传流程。

---

## 3. 前端实现方法分类（By Domain）

### 3.1 布局层

- `FrontendLayout`：前台导航 + 内容容器。
- `BackendLayout`：后台管理框架。
- `AuthLayout`：登录/注册模板页面（统一背景与特效）。

### 3.2 页面层

- **首页**：博客化布局，聚合最新/热门文章与快捷入口。
- **知识库**：搜索历史、分类筛选、分页、收藏联动、推荐阅读。
- **心理测试**：题库采集、提交、报告轮询、AI咨询跳转。
- **情绪日记**：评分、情绪选择、统计图、历史记录、AI分析展示。
- **个人中心**：资料修改、头像上传、密码修改、退出登录。

### 3.3 状态与权限层

- `Pinia(user)` 存储 token 和 userInfo，并做登录态持久化。
- 路由守卫按 `meta.requiresAuth` 与用户角色做访问控制（前后台隔离）。

### 3.4 API层

- `src/api/*` 按业务拆分接口模块。
- 统一请求工具处理消息提示、错误回调、拦截器逻辑。

---

## 4. 关键业务数据流（实现视角）

### 4.1 登录鉴权流

1. 前端提交账号密码；
2. 后端签发 token；
3. 前端存储 token 与用户信息；
4. 路由守卫和请求头携带 token 进行权限控制。

### 4.2 心理测试报告流

1. 前端提交测试答案；
2. 后端创建测试报告记录；
3. 后台异步调用 AI 生成结构化报告；
4. 前端轮询报告状态并渲染结果。

### 4.3 情绪日记分析流

1. 前端提交今日日记；
2. 后端落库并触发 AI 分析任务；
3. 任务队列处理完成后写回结果；
4. 前端查询并展示风险等级与建议。

---

## 5. 当前工程目录建议理解

- `springboot/`：后端 API 与业务实现
- `vue3/`：前端 SPA 应用
- 根目录：多模块聚合与协同开发入口

---

## 6. 运行方式（简版）

### 后端

- 进入 `springboot`
- 配置 `application.yml` 数据源、Redis、AI参数
- 启动 Spring Boot 应用

### 前端

- 进入 `vue3`
- 安装依赖并启动：
  - `npm install`
  - `npm run dev`

---

## 7. 后续建议（工程化）

- 引入环境变量与配置分层（dev/staging/prod），避免敏感信息写入仓库。
- 为高价值链路补充集成测试（鉴权、测试报告、日记分析、知识库导入）。
- 对 AI 调用增加重试、超时、熔断与审计日志。
- 推进统一设计系统（颜色、间距、组件规范）提升页面一致性。
