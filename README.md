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
| Spring Boot | 3.3.7 | 主框架 |
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

## 3. 项目架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue 3 + Element Plus)            │
│   Login → Welcome → Dashboard / ShipManage / UserManage │
│   Axios ↔ /api/*  (Vite Proxy 转发)                     │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP (JSON)
┌──────────────────────▼──────────────────────────────────┐
│              后端 (Spring Boot 3.3.7 :8082)               │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌────────────────────┐     │
│  │ Controller│→│ Service  │→│ Mapper (MyBatis-Plus)│    │
│  └──────────┘  └──────────┘  └────────┬───────────┘     │
│       ↑                                │                 │
│  LoginInterceptor (JWT + RBAC)         │                 │
│       ↑                                │                 │
│  ┌──────────┐  ┌──────────┐  ┌───────▼──────┐          │
│  │  Redis   │  │  MinIO   │  │   MySQL 8.0  │          │
│  │(Token/   │  │(File     │  │  (Druid 连接池)│         │
│  │ 验证码)  │  │ Storage) │  └──────────────┘          │
│  └──────────┘  └──────────┘                             │
└─────────────────────────────────────────────────────────┘
```

**分层说明**：
- **Controller 层**：接收 HTTP 请求，参数校验，调用 Service
- **Service 层**：业务逻辑实现，事务管理
- **Mapper 层**：MyBatis-Plus BaseMapper，零 XML 配置
- **Interceptor**：JWT 验证 + URI 前缀 RBAC 鉴权
- **Config**：线程池、MinIO、CORS、Redis、MyBatis-Plus 分页插件

---

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

**关键参数**：
- 导入线程池：4 核心 / 8 最大 / 有界队列 20000 / CallerRunsPolicy 背压
- 缓冲批次：5000 行 / 批，子批次 1000 条/SQL
- 进度追踪：`ConcurrentHashMap<String, ImportProgressVO>` + `AtomicInteger`
- 前端轮询：`GET /excel/progress/{taskId}` 实时显示进度

**性能数据**（100 万行测试）：

| 指标 | 数值 |
|------|------|
| 导入耗时 | ~40s |
| 写入速度 | ~25,000 行/秒 |
| 内存峰值 | < 500MB |
| OOM | 否 |

### 5.3 导出方案

```
┌─────────────┐         ┌─────────────┐
│ 主线程       │         │ DB 查询线程  │
│ (写入 Excel) │  page1  │ (分页查询)   │
│             │◄────────│             │
│ 写入 page1  │         │ 查 page2    │──→ CompletableFuture
│             │  page2  │             │
│ 写入 page2  │◄────────│ 查 page3    │──→ CompletableFuture
│             │  page3  │             │
│ 写入 page3  │◄────────│ 查 page4    │
│    ...      │         │    ...      │
└─────────────┘         └─────────────┘
      │
      ▼
EasyExcel ExcelWriter (流式写临时文件，自动刷盘)
      │
      ▼
   D:/tmp/ship_export_{taskId}.xlsx
```

**关键参数**：
- 导出线程池：2 核心 / 4 最大 / 有界队列 5000 / CallerRunsPolicy
- 分页大小：10,000 行/页
- 滑动窗口：始终预取 1 页（最多 2 页 = 20,000 行在内存）
- 异步生成 + 轮询进度 + 下载接口

**性能数据**（100 万行测试）：

| 指标 | 数值 |
|------|------|
| 导出耗时 | ~75s |
| 导出速度 | ~13,000 行/秒 |
| 内存峰值 | < 200MB |
| 输出文件大小 | ~120MB |

### 5.4 线程池隔离设计

```
excelImportExecutor (导入)          excelExportExecutor (导出)
┌────────────────────────┐        ┌────────────────────────┐
│ core=4, max=8          │        │ core=2, max=4          │
│ queue=LinkedBlocking   │        │ queue=LinkedBlocking   │
│       Queue(20000)     │        │       Queue(5000)      │
│ CallerRunsPolicy       │        │ CallerRunsPolicy       │
└────────────────────────┘        └────────────────────────┘
         ↑                                 ↑
    批量 INSERT                      分页 SELECT
```

两个线程池**物理隔离**，导入任务不会阻塞导出任务。`CallerRunsPolicy` 保证队列满载时由调用线程执行，实现天然背压。

---

## 6. 环境准备

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java 开发环境 |
| Maven | 3.8+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存服务（可选，也可用内置缓存） |
| MinIO | 最新版 | 文件存储（可选） |
| Node.js | 18+ | 前端运行环境 |

### Windows 环境配置

#### 1. JDK 17

```powershell
# 下载安装后验证
java -version
# 输出: openjdk version "17.0.x" ...
```

#### 2. Maven

```powershell
# 下载解压后配置环境变量 MAVEN_HOME，然后验证
mvn -version
# 输出: Apache Maven 3.9.x ...
```

#### 3. MySQL 8.0

```powershell
# 安装后创建数据库
mysql -u root -p

CREATE DATABASE IF NOT EXISTS portalship_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

#### 4. Redis（Windows）

使用 [Memurai](https://www.memurai.com/) 或 WSL 安装，默认端口 6379。

#### 5. MinIO（可选，文件上传功能需要）

```powershell
# 下载 minio.exe，命令行启动
minio.exe server C:\minio-data --console-address :9001
# 访问 http://localhost:9001，默认账号 minioadmin / minioadmin
```

#### 6. Node.js 18+

```powershell
node -v
npm -v
```

---

## 7. 部署与运行

### 7.1 后端启动

```powershell
# 1. 进入后端目录
cd C:\Users\Lenovo\Desktop\port-ship\portal-ship-server

# 2. 修改数据库连接配置（如需要）
#    编辑 src\main\resources\application.yml
#    修改 spring.datasource.url / username / password

# 3. 初始化数据库表
#    执行 src\main\resources\db\schema.sql（在 MySQL 客户端中运行）

# 4. 编译并启动
mvn clean compile spring-boot:start

# 或者 IDE 中直接运行 PortalShipServerApplication.main()
```

后端启动后访问：`http://localhost:8082`

### 7.2 前端启动

```powershell
# 1. 进入前端目录
cd C:\Users\Lenovo\Desktop\port-ship\portal-ship-client

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 7.3 验证登录

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin@portalship.com` | `123456` | 管理员 |
| — | — | 需自行注册普通用户 |

### 7.4 生成测试数据（可选）

```powershell
# 生成 10 万条模拟船舶数据 Excel
# IDE 中运行 util/ShipDataGenerator.main()
# 输出文件：D:/test/ship_10w.xlsx

# 导入到数据库
# IDE 中运行 util/ImportShipDataRunner.main()
```

---

## 8. 数据库表说明

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `tb_user` | 用户表 | username, password(MD5), role, email, status |
| `tb_ship_info` | 船舶信息表 | ship_name, nationality, imo_no, status, arrive_time, leave_time |
| `tb_ship_operation` | 作业数据表 | ship_id, quay_crane_no, work_efficiency, total_containers |
| `tb_file_info` | 文件信息表 | relate_id, relate_type, file_path, file_type |
| `tb_system_log` | 系统日志表 | user_id, operation, method, params, ip |
| `tb_port_data` | 港口数据表 | port_name, total_berths, today_throughput |
| `tb_weather` | 天气数据表 | temperature, wind_speed, wave_height, visibility |
| `tb_work_data` | 作业统计表 | work_date, total_ships, efficiency_rate |
| `ships` | 船舶表(兼容) | ship_name, cargo_type, destination, status |

完整建表语句见：`src/main/resources/db/schema.sql`

---

## 9. API 接口概览

### 认证模块 (`/auth`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 邮箱+密码登录 |
| POST | `/auth/register` | 邮箱注册 |
| GET | `/auth/getUserInfo` | 获取当前用户信息 |
| GET | `/auth/sendVerificationCode?email=` | 发送验证码 |
| PATCH | `/auth/updatePassword` | 修改密码 |
| POST | `/auth/logout` | 登出 |

### 船舶管理 (`/ship`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ship/addShip` | 新增船舶 |
| PUT | `/ship/updateShip` | 编辑船舶 |
| DELETE | `/ship/deleteShip/{id}` | 删除船舶 |
| GET | `/ship/getShipDetail/{id}` | 船舶详情 |
| POST | `/ship/getAllShips` | 分页列表+筛选 |
| PATCH | `/ship/updateShipStatus/{id}/{status}` | 状态更新 |

### 作业管理 (`/operation`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/operation/addOperation` | 新增作业 |
| PUT | `/operation/updateOperation` | 编辑作业 |
| DELETE | `/operation/deleteOperation/{id}` | 删除作业 |
| POST | `/operation/getAllOperations` | 分页列表 |

### Excel 导入导出 (`/excel`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/excel/import` | 上传 Excel 导入 |
| POST | `/excel/export` | 提交导出任务 |
| GET | `/excel/download/{taskId}` | 下载导出文件 |
| GET | `/excel/progress/{taskId}` | 查询进度 |

### 统计 (`/statistics`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/statistics/getDashboard` | 仪表盘数据 |
| GET | `/statistics/getEfficiencyRank?limit=10` | 效率排名 |

### 文件管理 (`/file`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/file/upload` | 上传文件 |
| GET | `/file/getFiles` | 查询文件 |
| DELETE | `/file/deleteFile/{id}` | 删除文件 |

---

## 10. 项目截图

> 以下为截图位置预留，请部署运行后补充实际截图。

| 页面 | 说明 | 截图 |
|------|------|------|
| 登录页 | 暗色主题，角色选择（管理员/普通用户） | ![login](./screenshots/login.png) |
| 数据总览 | 统计卡片 + 图表 + 效率排名 | ![dashboard](./screenshots/dashboard.png) |
| 船舶管理 | 表格分页 + 增删改查 + Excel 导入导出 | ![ship](./screenshots/ship.png) |
| 作业管理 | 岸桥作业记录 | ![operation](./screenshots/operation.png) |
| 用户管理 | 角色/状态变更 | ![user](./screenshots/user.png) |
| Excel 导入 | 文件上传 + 进度条轮询 | ![import](./screenshots/import.png) |

---

## 11. 总结与收获

### 技术收获

1. **EasyExcel 流式读写**：掌握了 3.3.x 版本的 `ReadListener` + `ExcelWriter` API，理解了其内部流式刷盘机制，能够处理百万级 Excel 文件而保持内存稳定。
2. **线程池工程设计**：实践了自定义 `ThreadPoolExecutor` 的完整配置——核心/最大线程数、有界队列、拒绝策略（CallerRunsPolicy）、线程命名、超时回收——并理解了导入与导出线程池隔离的意义。
3. **CompletableFuture 并发编排**：实现了滑动窗口预取模式，将 DB 查询与文件 I/O 并行化，在不增加内存压力的前提下提升吞吐量。
4. **RBAC 拦截器**：独立实现了基于 URI 前缀匹配的角色权限控制，区别于 Spring Security 的注解方式，更适合中小型项目的轻量级鉴权需求。
5. **前后端对接**：完整经历了从 API 设计 → DTO/VO 建模 → Controller 实现 → 前端 Axios 调用 → Element Plus 渲染的全流程。

### 工程能力

- 从 0 到 1 搭建 Spring Boot 3.x + Vue 3 前后端分离项目
- 独立设计数据库 Schema，处理表前缀、字段映射、索引优化
- 解决了前后端角色格式不一致（`admin` vs `ROLE_ADMIN`）的兼容问题
- 实现了异步任务进度追踪 + 定时清理机制，避免了内存泄漏

---

## License

本项目仅供学习交流使用。

---

*最后更新：2026 年 5 月*
