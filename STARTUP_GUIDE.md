# 服务启动指南

## 项目简介
这是一个基于 Spring Boot 3.5.6 和 Kotlin 1.9.25 开发的后端服务组件。

## 环境要求

### 必需环境
- **Java**: JDK 17 或更高版本
- **Maven**: 3.6+ (项目已包含 Maven Wrapper)
- **MySQL**: 5.7+ 或 8.0+
- **操作系统**: macOS / Linux / Windows

### 验证环境
```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本（如果已安装）
mvn -version
```

### 使用 jenv 切换 Java 版本
如果使用 jenv 管理 Java 版本：

```bash
# 确保 jenv 已初始化（添加到 ~/.zshrc 或 ~/.bashrc）
export PATH="$HOME/.jenv/bin:$PATH"
eval "$(jenv init -)"

# 为当前项目设置 Java 17
cd /Users/zhihu/AndroidStudioProjects/KtServerComponent
jenv local 17

# 验证版本
java -version
./mvnw -version
```

**注意**：项目根目录已有 `.java-version` 文件，jenv 会自动使用该文件中指定的版本。

## 数据库准备

### 1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS comeon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 创建用户表
执行项目中的 SQL 脚本：
```bash
mysql -u root -p comeon_db < src/main/resources/sql/users_table.sql
```

或者直接在 MySQL 客户端中执行 `src/main/resources/sql/users_table.sql` 文件内容。

### 3. 配置数据库连接
编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/comeon_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root        # 修改为你的数据库用户名
    password:             # 修改为你的数据库密码
```

## 启动服务

### 方式一：使用 Maven Wrapper（推荐）
```bash
# macOS/Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### 方式二：先编译再运行
```bash
# 编译项目
./mvnw clean package

# 运行 JAR 文件
java -jar target/component-0.0.1-SNAPSHOT.jar
```

### 方式三：使用 IDE
1. 使用 IntelliJ IDEA 或 Android Studio 打开项目
2. 找到 `ComeonApplication.kt` 文件
3. 右键点击 `main` 函数，选择 "Run 'ComeonApplicationKt'"

## 验证服务

### 检查服务是否启动成功
服务启动后，默认运行在 **http://localhost:9999**

可以通过以下方式验证：
```bash
# 检查服务是否响应
curl http://localhost:9999

# 或者访问测试接口（如果存在）
curl http://localhost:9999/api/test
```

### 查看日志
服务启动时会在控制台输出日志，包括：
- Spring Boot 启动信息
- 数据库连接状态
- MyBatis SQL 日志（开发环境）

## 常见问题

### 1. 端口被占用
如果启动时遇到错误：`Port 9999 was already in use`

#### 方法一：停止占用端口的进程（推荐）
```bash
# 查找占用 9999 端口的进程
lsof -i :9999

# 停止进程（将 PID 替换为实际的进程 ID）
kill <PID>

# 或者强制停止
kill -9 <PID>

# 验证端口是否已释放
lsof -i :9999
```

#### 方法二：修改应用端口
如果不想停止现有进程，可以修改 `application.yml` 中的端口：
```yaml
server:
  port: 8080  # 修改为其他端口
```

### 2. 数据库连接失败
- 检查 MySQL 服务是否启动
- 确认数据库名称、用户名、密码是否正确
- 确认数据库 `comeon_db` 已创建
- 检查防火墙设置

### 3. Java 版本不匹配
确保使用 JDK 17：

#### 使用 jenv 管理 Java 版本（推荐）
如果已安装 jenv，可以使用以下命令：

```bash
# 查看所有可用的 Java 版本
jenv versions

# 为当前项目设置 Java 17（会在项目根目录创建 .java-version 文件）
jenv local 17

# 或者全局设置 Java 17
jenv global 17

# 验证当前 Java 版本
java -version

# 确保 jenv 已正确初始化（添加到 ~/.zshrc 或 ~/.bashrc）
# 如果没有，添加以下内容：
# export PATH="$HOME/.jenv/bin:$PATH"
# eval "$(jenv init -)"
```

#### 手动切换 Java 版本
如果没有使用 jenv：

```bash
# macOS 上查看所有已安装的 Java 版本
/usr/libexec/java_home -V

# 设置 JAVA_HOME 环境变量
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 验证
java -version
```

### 4. Maven Wrapper 配置文件缺失
如果遇到错误：`cannot read distributionUrl property in ./.mvn/wrapper/maven-wrapper.properties`

说明缺少 Maven Wrapper 配置文件，需要创建 `.mvn/wrapper/maven-wrapper.properties` 文件：
```properties
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar
```

### 5. Maven 依赖下载失败
如果 Maven 依赖下载缓慢，可以配置国内镜像源，编辑 `~/.m2/settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

## 项目结构说明

```
KtServerComponent/
├── src/main/kotlin/com/comeon/component/
│   ├── ComeonApplication.kt      # 主启动类
│   ├── controller/               # 控制器层
│   ├── service/                  # 服务层
│   ├── mapper/                   # MyBatis Mapper
│   ├── model/                    # 数据模型
│   ├── dto/                      # 数据传输对象
│   ├── config/                   # 配置类
│   └── exception/                # 异常处理
├── src/main/resources/
│   ├── application.yml           # 应用配置
│   ├── mapper/                   # MyBatis XML 映射文件
│   └── sql/                      # SQL 脚本
└── pom.xml                       # Maven 依赖配置
```

## 开发建议

1. **开发环境**: 建议使用 IntelliJ IDEA 或 Android Studio，对 Kotlin 支持更好
2. **数据库工具**: 可以使用 MySQL Workbench、DBeaver 等工具管理数据库
3. **API 测试**: 可以使用 Postman、curl 或 httpie 测试 API 接口
4. **日志查看**: 开发环境已配置 MyBatis SQL 日志输出，便于调试

## 技术支持

如遇到问题，请检查：
1. 日志输出中的错误信息
2. 数据库连接状态
3. 端口占用情况
4. 依赖是否正确下载

---

**最后更新**: 2024

