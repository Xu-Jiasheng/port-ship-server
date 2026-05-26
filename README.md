# 🚢 船舶停靠出港管理系统

> Port Ship Docking & Departure Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D.svg)](https://vuejs.org/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.9-blue.svg)](https://baomidou.com/)
[![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.4-orange.svg)](https://easyexcel.opensource.alibaba.com/)
[![JDK](https://img.shields.io/badge/JDK-17-red.svg)](https://openjdk.org/)

基于 **Spring Boot + Vue 3** 的前后端分离船舶全生命周期管理系统。覆盖船舶到港、停靠、作业、离港的全流程数字化管理，核心解决**海量船舶数据的高性能 Excel 导入导出**问题。

---

## 目录

- [1. 项目背景](#1-项目背景)
- [2. 技术栈](#2-技术栈)
- [3. 项目架构](#3-项目架构)
- [4. 核心功能模块](#4-核心功能模块)
- [5. 重点亮点：百万级 Excel 导入导出](#5-重点亮点百万级-excel-导入导出)
- [6. 环境准备](#6-环境准备)
- [7. 部署与运行](#7-部署与运行)
- [8. 数据库表说明](#8-数据库表说明)
- [9. API 接口概览](#9-api-接口概览)
- [10. 项目截图](#10-项目截图)
- [11. 总结与收获](#11-总结与收获)

---

## 1. 项目背景

港口运营中每日产生大量船舶停靠/出港记录，传统人工录入效率低、易出错，且随着数据量增长，Excel 导入导出成为性能瓶颈（OOM、超时、数据丢失）。本系统针对这些痛点设计开发，提供：

- **船舶全生命周期管理**：从到港 → 停靠 → 作业 → 离港的数字化跟踪
- **海量数据高性能处理**：百万级 Excel 导入导出，并发批量入库，内存可控
- **精细化权限控制**：管理员 / 操作员 / 观察员三种角色的差异化访问
- **一站式运维**：文件归档、数据统计、操作日志记录

---

## 2. 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.7 | 主框架 |<img width="1280" height="679" alt="屏幕截图 2026-05-26 164303" src="https://github.com/user-attachments/assets/27d91ba4-dd18-47d0-8370-ad64c25d11b2" />

| MyBatis-Plus | 3.5.9 | ORM / 分页 / 批量操作 |
| MySQL | 8.0 | 关系型数据库 |
| Druid | 1.2.18 | 数据库连接池 |
| Redis | — | Token 会话缓存 + 验证码限流 |
| JWT (auth0) | 4.4.0 | 无状态身份认证 |
| EasyExcel | 3.3.4 | 高性能 Excel 读写 |
| MinIO | 8.5.9 | 对象存储（文件上传） |
| Spring Mail | — | QQ SMTP 邮件发送 |
| Lombok | — | 减少样板代码 |
| Jakarta Validation | — | 请求参数校验 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.13 | 前端框架 |
| Vue Router | 4.5.0 | 前端路由 |
| Element Plus | 2.9.0 | UI 组件库 |
| Axios | 1.7.9 | HTTP 请求 |
| Vite | 6.0.3 | 构建工具 |
| ECharts | 5.5.1 | 数据可视化（预留） |

### 中间件 / 工具

- **MinIO**：自建对象存储，存储船舶照片、作业文档
- **Redis**：JWT Token 缓存 + 验证码 60s 限流
- **QQ Mail SMTP**：邮箱验证码发送
- **Maven**：项目构建与依赖管理

---

## 3. 项目界面预览
<img width="1280" height="679" alt="屏幕截图 2026-05-26 164303" src="https://github.com/user-attachments/assets/faa42b89-fed1-4222-a70e-7940d0b07e82" />
**系统首页：**
这是智慧港口船舶信息管理系统的主界面，左侧整合了港口运营全维度数据看板，包含天气信息、港口核心数据、海侧作业数据与装卸动态四大模块，直观展示在港船只数量、泊位利用率、岸桥作业效率、装卸总量等关键指标，实现港口运行状态的实时监控与可视化呈现。

<img width="1280" height="681" alt="屏幕截图 2026-05-26 164337" src="https://github.com/user-attachments/assets/d396ebd1-294d-48d8-a72b-4ee0ca377463" />
**登录系统界面：**
提供管理员与普通用户两种角色的登录入口，支持邮箱+密码的身份校验，内置账号注册与返回首页功能。不同角色登录后将获得对应权限的系统操作界面，实现基于角色的访问控制。

<img width="1280" height="681" alt="屏幕截图 2026-05-26 164451" src="https://github.com/user-attachments/assets/dcd972f8-c430-4189-95df-50c5eeea7bad" />
**数据总览界面：**
系统核心的运营监控看板，直观展示港口关键运营指标，包括船舶总数、在港船舶数、作业中船舶数、今日作业数等核心数据。同时提供月度船舶到港趋势、船舶类型分布等可视化图表（开发中），并通过作业效率TOP10榜单，实时呈现船舶与岸桥的作业效率排名，辅助港口运营决策。


## 4. 核心功能模块

### 4.1 用户认证与权限

| 功能 | 说明 |
|------|------|
| 邮箱注册 | 验证码发送（60s Redis 限流防刷）、密码 MD5 摘要 |
| 双端登录 | 邮箱+密码 (`/auth/login`) / 用户名+密码 (`/user/login`) |
| JWT 认证 | auth0 java-jwt 签发，Redis 存储 6h 过期 |
| RBAC 权限 | ROLE_ADMIN / ROLE_OPERATOR / ROLE_VIEWER 三级角色 |
| 拦截器 | URI 前缀匹配白名单，`ThreadLocal` 传递用户上下文 |
| 修改密码 | 旧密码验证 + 新密码确认 |
| 密码重置 | 邮箱验证码验证 |

### 4.2 船舶管理（管理员）

| 功能 | 接口 |
|------|------|
| 新增船舶 | `POST /ship/addShip` |
| 编辑船舶 | `PUT /ship/updateShip` |
| 删除船舶 | `DELETE /ship/deleteShip/{id}` |
| 查询详情 | `GET /ship/getShipDetail/{id}` |
| 分页列表 | `POST /ship/getAllShips`（支持船名/航次号/状态/类型筛选） |
| 状态更新 | `PATCH /ship/updateShipStatus/{id}/{status}` |

### 4.3 作业数据管理

| 功能 | 接口 |
|------|------|
| 新增作业 | `POST /operation/addOperation` |
| 编辑作业 | `PUT /operation/updateOperation` |
| 删除作业 | `DELETE /operation/deleteOperation/{id}` |
| 分页列表 | `POST /operation/getAllOperations`（含船名关联查询） |

### 4.4 数据统计仪表盘

- 船舶总数 / 在港船舶 / 作业中 / 今日作业数 四维度统计卡片
- 月度到港趋势 & 船舶类型分布图表（预留 ECharts 接口）
- 作业效率 TOP10 排名表

### 4.5 文件管理

- MinIO 对象存储，启动自动创建 `portal-ship-data` bucket
- 支持图片/PDF/Excel 上传、关联船舶/作业记录、删除

### 4.6 用户管理（管理员）

- 用户列表分页查询 / 编辑 / 删除 / 批量删除
- 状态启用/禁用
- 角色变更

### 4.7 普通用户功能

| 模块 | 功能 |
|------|------|
| 接货管理 | 填写接货单 / 查询接货单 |
| 配车管理 | 车辆预约申请 / 车辆状态查询 |
| 到货管理 | 到货确认 / 异常上报 |
| 中转管理 | 中转货物跟踪 |
| 客户服务 | 在线咨询 |
| 个人中心 | 修改密码 / 个人信息维护 |

---

## 5. 重点亮点：百万级 Excel 导入导出

### 5.1 问题背景

港口每日产生的船舶停靠/出港记录可达到数十万条。使用传统 POI 或逐行 INSERT，存在三大痛点：

| 痛点 | 表现 |
|------|------|
| **OOM** | 一次性加载全量数据到内存，JVM 堆溢出 |
| **超时** | 单线程逐行插入，百万条耗时 30 分钟以上 |
| **数据丢失** | 中途失败无法追踪，缺乏进度反馈 |

### 5.2 导入方案

```
EasyExcel 逐行读取 (流式，不占内存)
        │
        ▼
ShipInfoImportListener.invoke(row)
        │  DTO → Entity 转换
        ▼
缓冲区 (List<ShipInfo>, 5000 行阈值)
        │  达到阈值 → 拷贝 → 清空
        ▼
CompletableFuture.runAsync() ──→ 自定义线程池 excelImportExecutor
                                         │
                                         ▼
                              MyBatis-Plus saveBatch(batch, 1000)
                              (1000 条/条 SQL 批量写入)
```
### 5.3 实现效果
<img width="278" height="131" alt="屏幕截图 2026-05-26 180248" src="https://github.com/user-attachments/assets/13257853-0ab0-4533-8cc3-ad6408e7756d" />

---


## 6. 总结与收获

### 技术收获

1. **EasyExcel 流式读写**：掌握了 3.3.x 版本的 `ReadListener` + `ExcelWriter` API，理解了其内部流式刷盘机制，能够处理百万级 Excel 文件而保持内存稳定。
2. **线程池工程设计**：实践了自定义 `ThreadPoolExecutor` 的完整配置——核心/最大线程数、有界队列、拒绝策略（CallerRunsPolicy）、线程命名、超时回收——并理解了导入与导出线程池隔离的意义。
3. **CompletableFuture 并发编排**：实现了滑动窗口预取模式，将 DB 查询与文件 I/O 并行化，在不增加内存压力的前提下提升吞吐量。
4. **RBAC 拦截器**：独立实现了基于 URI 前缀匹配的角色权限控制，区别于 Spring Security 的注解方式，更适合中小型项目的轻量级鉴权需求。
5. **前后端对接**：完整经历了从 API 设计 → DTO/VO 建模 → Controller 实现 → 前端 Axios 调用 → Element Plus 渲染的全流程。

### 工程能力

- 从 0 到 1 搭建 Spring Boot 3.x + Vue 3 前后端分离项目
- 实现了异步任务进度追踪 + 定时清理机制，避免了内存泄漏

---

## License

本项目仅供学习交流使用。

---

*最后更新：2026 年 5 月*
