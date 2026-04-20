# SoftwareTestingDemo

SoftwareTestingDemo 是一个基于 Spring Boot 的场馆预约管理系统，用于软件测试课程项目。系统包含普通用户端和管理员端，覆盖用户注册登录、场馆浏览、场馆预约、订单管理、留言板、新闻公告以及后台审核管理等功能。

## 技术栈

- Java 8
- Spring Boot 2.2.2
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven
- JUnit 5

## 项目结构

```text
SoftwareTestingDemo/
├── documents/                 # 测试相关文档
│   ├── 测试用例设计文档.md
│   └── 项目测试计划书.md
├── src/
│   ├── main/
│   │   ├── java/com/demo/
│   │   │   ├── controller/    # 用户端与管理员端控制器
│   │   │   ├── dao/           # JPA 数据访问层
│   │   │   ├── entity/        # 实体类与 VO
│   │   │   ├── service/       # 业务接口与实现
│   │   │   └── utils/         # 工具类
│   │   └── resources/
│   │       ├── templates/     # Thymeleaf 页面
│   │       ├── static/        # 静态资源
│   │       └── application.yml
│   └── test/                  # 测试代码目录
├── demo_db.sql                # MySQL 初始化数据
├── pom.xml
└── README.md
```

## 主要功能

### 用户端

- 用户注册、登录、退出
- 浏览首页、新闻列表、新闻详情
- 浏览场馆列表、场馆详情
- 提交场馆预约订单
- 查看、修改、删除个人订单
- 发布、修改、删除留言
- 查看已审核通过的留言
- 修改个人信息

### 管理端

- 管理普通用户信息
- 管理场馆信息
- 审核预约订单
- 管理新闻公告
- 审核和删除留言

## 数据库初始化

项目使用 MySQL 数据库，默认配置位于：

```text
src/main/resources/application.yml
```

初始化数据脚本：

```text
demo_db.sql
```

运行前需要创建 `demo_db` 数据库并导入脚本：

```sql
CREATE DATABASE IF NOT EXISTS demo_db DEFAULT CHARACTER SET utf8;
```

然后导入：

```bash
mysql demo_db < demo_db.sql
```

如本地 MySQL 用户名、密码或端口不同，需要同步修改 `application.yml` 中的 datasource 配置。

## 启动项目

在项目根目录执行：

```bash
mvn spring-boot:run
```

默认访问地址：

```text
http://localhost:8888/index
```

## 测试用例文档

测试用例设计文档统一放在 `documents` 目录下：

- `documents/测试用例设计文档.md`：Markdown 版本，便于后续转 PDF 或继续维护
- `documents/测试用例设计文档.docx`：Word 版本，按照课程模板整理

测试用例文档覆盖内容包括：

- 黑盒测试：等价类划分、边界值分析
- 白盒测试：语句覆盖、判定覆盖
- Service 层单元测试用例
- Controller 层集成测试用例
- 用户端和管理端核心业务流程
- 登录权限、分页、状态流转、异常输入等关键场景

## 测试范围

单元测试主要面向 `service` 层公开方法，包括：

- `UserService`
- `VenueService`
- `NewsService`
- `MessageService`
- `OrderService`
- `MessageVoService`
- `OrderVoService`

集成测试主要面向 `controller` 层接口，包括：

- 用户登录注册与个人信息维护
- 首页、新闻、场馆浏览
- 留言发布与审核
- 预约订单提交、修改、删除、审核
- 后台用户、场馆、新闻管理

## 说明

当前仓库中的测试用例文档属于测试设计成果，文档中的“测试结果”统一标记为设计阶段状态。实际执行结果应在后续编写并运行 JUnit/MockMvc 测试脚本后再更新。
