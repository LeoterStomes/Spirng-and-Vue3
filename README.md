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


## 7. 近期升级记录（医生平台 + 个性化AI + UI改版 + 本地知识库）

### 7.1 角色体系升级（新增医生端）

- 新增用户角色：`DOCTOR(3)`，并在注册、登录、路由守卫、后台用户管理中完整打通。
- 管理端支持角色切换（普通用户/管理员/医生）。
- 登录后按角色自动跳转：
  - 普通用户：前台
  - 医生：`/doctor/dashboard`
  - 管理员：`/back/dashboard`

### 7.2 医生工作台能力

- 新增医生端页面与布局：
  - `DoctorLayout`
  - `DoctorDashboard`
  - `DoctorPatients`
  - `DoctorPatientDetail`
  - `DoctorSidebar`
- 新增医生接口与服务：
  - 患者绑定/解绑
  - 我的患者分页与筛选
  - 患者档案查询与更新
  - 患者会话/日记/报告聚合查看
  - 临床备注新增与查询
  - 医生端统计看板（风险分布、活跃趋势）

### 7.3 新增医疗相关数据模型

- 新增实体与表映射：
  - `patient_profile`（患者档案）
  - `doctor_patient`（医生-患者绑定关系）
  - `clinical_note`（临床备注）
- 对应新增 Mapper 与业务层逻辑，用于患者管理与医生决策支持。

### 7.4 AI 个性化对话增强

- 在 `PsychologicalSupportService` 中引入患者档案与最近会话摘要，动态拼装系统提示词。
- `PromptManage` 新增个性化提示词构建能力：
  - 按沟通风格（`gentle/direct/encouraging`）调整回复风格
  - 注入风险等级、主诉、诊断摘要、治疗计划等信息
- 目标：让“小暖”对不同患者给出差异化、个性化建议。

### 7.5 心理测试链路修复与优化

- 修复“报告长期处于生成中”问题：异步任务在事务提交后触发，避免查不到新记录。
- 修复“提交超时”问题：接口先快速返回 `PROCESSING`，后台异步生成报告。
- 优化报告展示：
  - 增加 `testTypeDisplayName`、`statusDisplayName`
  - 前端展示中文名称而非枚举代码。

### 7.6 注册与接口稳定性修复

- 修复注册失败场景（手机号/邮箱/用户名唯一约束冲突）：
  - 前置唯一性校验
  - 更精准错误提示
  - 前端空字符串字段清洗为 `null`
- 修复医生端“连接超时/参数异常”问题：
  - 修正医生接口映射路径
  - 修正 GET 参数序列化方式
- 修复医生前端数据读取逻辑（请求成功后直接使用 `data`，不再依赖不存在的 `code` 字段）。

### 7.7 首页与前台 UI 改版

- 首页 `Home.vue`：
  - 首页文章封面支持本地图库映射，且“每次刷新重新分配图片”
  - 保留轮播、搜索、快捷入口的可读性与交互节奏
- AI咨询 `Consultation.vue`：
  - 聊天主区域层次优化（头部/消息流/输入区）
  - 气泡与头像视觉区分增强（AI/用户）
  - 整体配色由单一暖色升级为蓝紫系对话风格
- 心理测试 `PsychologicalTest.vue`：
  - 卡片、题目区、报告区视觉统一，选中态与重点信息更清晰
- 情绪日记 `EmotionDiary.vue`：
  - 日记卡、统计卡、分析卡视觉重构
  - 采用更轻盈的渐变背景与状态色，提升可读性

### 7.8 知识库“本地优先”能力（重点）

- 后端新增本地知识导入 DTO：`KnowledgeImportTextDTO`
- 新增接口：
  - `POST /knowledge/article/import-text`：导入纯文本知识并发布
  - `GET /knowledge/article/local-context`：按关键词返回本地知识命中片段
- `KnowledgeArticleService` 新增：
  - `importPlainTextKnowledge(...)`
  - `buildKnowledgeContextForAi(...)`
- `PromptManage` 新增 `buildKnowledgeFirstPrompt(...)`：
  - 明确“本地知识优先，联网检索兜底，冲突时以本地为准”
- `PsychologicalSupportService` 对话链路接入知识检索：
  - 先检索本地知识上下文
  - 本地不足时再使用联网兜底摘要
- 前端 `Knowledge.vue` 新增“导入本地知识”弹窗：
  - 支持粘贴文本
  - 支持导入 `.txt` 文件自动填充
  - 一键导入并发布

### 7.9 依赖与配置补充

- 新增依赖：`spring-boot-starter-validation`（用于 DTO 校验）
- 数据源配置优化：
  - MySQL 驱动更新为 `com.mysql.cj.jdbc.Driver`
  - Hikari 初始化失败策略优化，降低启动时数据库抖动导致的直接失败风险
- 关闭不必要的 Spring Data JDBC 自动仓库扫描，避免冲突启动报错。

### 7.10 前端全站视觉升级（动态 / 轻松 / 焕新）

- 全站视觉基线重构（`global.css`）：
  - 引入统一设计变量（主色、阴影、边框、背景层次）
  - 增加柔和动态背景（径向渐变 + 轻玻璃质感）
  - 增加统一滚动条风格与页面切换过渡动画
  - 新增 `prefers-reduced-motion` 兼容，自动降低动画强度
- 全局组件主题重构（`element-variables.scss`）：
  - Element Plus 主色切换为蓝紫轻盈风格
  - `el-card / el-dialog / el-input / el-button / el-table` 统一圆角、阴影与交互反馈
- 布局层统一升级：
  - `FrontendLayout`：导航栏玻璃化、品牌渐变字、胶囊导航高亮、页脚统一
  - `BackendLayout` / `DoctorLayout`：内容区背景与容器层次统一
  - `AuthLayout`：背景光斑与登录容器玻璃质感增强
  - `App.vue`：全局路由切换动画接入
- 页面层统一升级（前台核心页面）：
  - `Home.vue`：英雄区、文章卡、侧栏卡风格统一；刷新随机封面逻辑保留
  - `Knowledge.vue`：头部、筛选栏、侧栏与搜索交互统一
  - `Consultation.vue`：会话列表、聊天头、消息气泡、输入区与发送按钮动效统一
  - `PsychologicalTest.vue`：测试卡、题目卡、状态提示与报告区统一
  - `EmotionDiary.vue`：评分按钮、情绪卡、统计卡、空状态与主按钮统一
  - `ArticleDetail.vue` / `EmotionDiaryDetail.vue` / `MyFavorites.vue`：详情卡与操作区风格统一

### 7.11 前端内容渲染安全加固（XSS防护）

- 新增依赖：`dompurify`
- 新增工具：`src/utils/sanitizeHtml.js`
  - `sanitizeHtml(input)`：白名单清洗 HTML
  - `sanitizeTextWithBreaks(input)`：文本换行后再安全清洗
- 关键页面 `v-html` 输出接入安全过滤：
  - `Consultation.vue`（消息内容）
  - `PsychologicalTest.vue`（报告正文）
  - `EmotionDiaryDetail.vue`（日记内容）
  - `ArticleDetail.vue`（文章正文）
- 结果：保留富文本展示能力，同时减少注入风险并清理相关安全告警。

---

