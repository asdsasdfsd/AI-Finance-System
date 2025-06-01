@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ==========================================
REM 🚀 AI财务管理系统 - DDD模式测试工具 (Windows)
REM ==========================================

echo ==========================================
echo 🚀 AI财务管理系统 - DDD模式测试工具 (Windows)
echo ==========================================
echo.

REM 检查参数
if "%1"=="" (
    echo [ERROR] 请提供操作参数
    echo.
    echo 可用命令:
    echo   start-ddd     - 启动DDD模式
    echo   start-orm     - 启动ORM模式  
    echo   test-ddd      - 测试DDD模式
    echo   clean         - 清理编译缓存
    echo   help          - 显示帮助信息
    echo.
    echo 示例: scripts\ddd-test.bat start-ddd
    goto :EOF
)

set "COMMAND=%1"

REM 设置Maven参数 (修复JVM参数兼容性)
set "MAVEN_OPTS=-Xmx1024m -XX:MetaspaceSize=256m"

REM 执行对应命令
if "%COMMAND%"=="start-ddd" goto :start_ddd
if "%COMMAND%"=="start-orm" goto :start_orm
if "%COMMAND%"=="test-ddd" goto :test_ddd
if "%COMMAND%"=="clean" goto :clean
if "%COMMAND%"=="help" goto :help

echo [ERROR] 未知命令: %COMMAND%
goto :help

:start_ddd
echo [STEP] 启动DDD模式...
echo.
echo [INFO] Profile设置: ddd
echo [INFO] 启动参数: DDD模式
echo [INFO] 端口: 8085
echo [INFO] JVM设置: %MAVEN_OPTS%
echo.
echo [INFO] 正在启动应用...
echo ==========================================
echo.
set "SPRING_PROFILES_ACTIVE=ddd"
set "DDD_ENABLED=true"
call mvnw spring-boot:run -Dspring.profiles.active=ddd -Dddd.enabled=true -Dserver.port=8085
goto :EOF

:start_orm
echo [STEP] 启动ORM模式...
echo.
echo [INFO] Profile设置: orm
echo [INFO] 启动参数: ORM模式
echo [INFO] 端口: 8080
echo [INFO] JVM设置: %MAVEN_OPTS%
echo.
echo [INFO] 正在启动应用...
echo ==========================================
echo.
set "SPRING_PROFILES_ACTIVE=orm"
set "DDD_ENABLED=false"
call mvnw spring-boot:run -Dspring.profiles.active=orm -Dddd.enabled=false -Dserver.port=8080
goto :EOF

:test_ddd
echo [STEP] 测试DDD模式...
echo.
echo [INFO] 编译项目...
call mvnw clean compile -q
if errorlevel 1 (
    echo [ERROR] 编译失败
    goto :EOF
)

echo [INFO] 运行DDD相关测试...
call mvnw test -Dspring.profiles.active=ddd -Dtest=*DDD*Test
if errorlevel 1 (
    echo [ERROR] DDD测试失败
    goto :EOF
)

echo [SUCCESS] DDD模式测试通过 ✓
goto :EOF

:clean
echo [STEP] 清理编译缓存...
echo.
call mvnw clean
echo [SUCCESS] 清理完成 ✓
goto :EOF

:help
echo ==========================================
echo 📖 AI财务管理系统 - 帮助信息
echo ==========================================
echo.
echo 可用命令:
echo.
echo   start-ddd     启动DDD模式 (端口8085)
echo                 - 使用领域驱动设计架构
echo                 - 聚合根、值对象、领域事件
echo                 - 应用服务协调业务逻辑
echo.
echo   start-orm     启动ORM模式 (端口8080) 
echo                 - 使用传统ORM架构
echo                 - 实体、服务、控制器
echo                 - 面向数据库设计
echo.
echo   test-ddd      运行DDD相关测试
echo                 - 测试聚合根业务逻辑
echo                 - 验证领域事件发布
echo                 - 检查数据一致性
echo.
echo   clean         清理Maven编译缓存
echo                 - 删除target目录
echo                 - 重新下载依赖
echo.
echo   help          显示此帮助信息
echo.
echo ==========================================
echo 🔧 技术规格:
echo ==========================================
echo.
echo JVM配置:
echo   ✓ 堆内存: 1024MB
echo   ✓ 元空间: 256MB
echo   ✓ Java版本: 21+
echo.
echo DDD模式特性:
echo   ✓ 聚合根边界清晰
echo   ✓ 领域事件驱动
echo   ✓ 业务逻辑封装
echo   ✓ 数据一致性保证
echo.
echo ORM模式特性:
echo   ✓ 快速开发
echo   ✓ 简单架构
echo   ✓ 传统MVC模式
echo   ✓ 易于理解
echo.
echo ==========================================
echo 💡 使用建议:
echo ==========================================
echo.
echo 1. 开发调试阶段 → 使用ORM模式
echo 2. 生产部署阶段 → 使用DDD模式
echo 3. 性能测试阶段 → 对比两种模式
echo 4. 团队培训阶段 → 从ORM到DDD渐进
echo.
echo ==========================================
echo 🛠️ 故障排除:
echo ==========================================
echo.
echo 如果启动失败，请尝试:
echo 1. scripts\ddd-test.bat clean
echo 2. 检查Java版本 (java -version)
echo 3. 检查数据库连接
echo 4. 查看完整日志输出
echo.
goto :EOF