@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ==========================================
REM 🚀 AI财务管理系统 - DDD模式测试工具 (Windows) - 修复版
REM ==========================================

echo ==========================================
echo 🚀 AI财务管理系统 - DDD模式测试工具 (Windows)
echo ==========================================
echo.

REM 检查参数
if "%~1"=="" (
    echo [ERROR] 请提供操作参数
    echo.
    echo 可用命令:
    echo   start         - 启动DDD模式 (默认端口8085)
    echo   test          - 测试DDD模式功能
    echo   test-api      - 测试API端点
    echo   test-ddd      - 完整DDD功能测试
    echo   clean         - 清理编译缓存
    echo   compile       - 编译项目
    echo   help          - 显示帮助信息
    echo.
    echo 示例: scripts\ddd-test.bat start
    goto :EOF
)

set "COMMAND=%~1"

REM 设置Maven参数
set "MAVEN_OPTS=-Xmx1024m -XX:MetaspaceSize=256m"

REM 设置应用端口
set "SERVER_PORT=8085"

REM 调试信息
echo [DEBUG] 接收到命令: "%COMMAND%"

REM 执行对应命令 - 使用 /i 进行大小写不敏感比较
if /i "%COMMAND%"=="start" (
    echo [DEBUG] 匹配到 start 命令
    goto :start_ddd
)
if /i "%COMMAND%"=="test" (
    echo [DEBUG] 匹配到 test 命令
    goto :test_ddd
)
if /i "%COMMAND%"=="test-api" (
    echo [DEBUG] 匹配到 test-api 命令
    goto :test_api
)
if /i "%COMMAND%"=="test-ddd" (
    echo [DEBUG] 匹配到 test-ddd 命令
    goto :test_comprehensive
)
if /i "%COMMAND%"=="compile" (
    echo [DEBUG] 匹配到 compile 命令
    goto :compile
)
if /i "%COMMAND%"=="clean" (
    echo [DEBUG] 匹配到 clean 命令
    goto :clean
)
if /i "%COMMAND%"=="help" (
    echo [DEBUG] 匹配到 help 命令
    goto :help
)

echo [ERROR] 未知命令: "%COMMAND%"
goto :help

:start_ddd
echo [STEP] 启动DDD模式...
echo.
echo [INFO] 配置信息:
echo [INFO] - 模式: DDD架构 (统一模式)
echo [INFO] - 端口: %SERVER_PORT%
echo [INFO] - JVM设置: %MAVEN_OPTS%
echo [INFO] - 数据库: MySQL testdb
echo.
echo [INFO] 正在启动应用...
echo ==========================================
echo.

REM 检查mvnw是否存在
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd 文件不存在，请确保在正确的目录中运行
    goto :EOF
)

REM 启动应用
echo [INFO] 执行命令: mvnw.cmd spring-boot:run -Dserver.port=%SERVER_PORT%
call mvnw.cmd spring-boot:run -Dserver.port=%SERVER_PORT%
goto :EOF

:compile
echo [STEP] 编译项目...
echo.

REM 检查mvnw是否存在
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd 文件不存在，请确保在正确的目录中运行
    goto :EOF
)

call mvnw.cmd clean compile
if errorlevel 1 (
    echo [ERROR] 编译失败
    goto :EOF
)
echo [SUCCESS] 编译成功 ✓
goto :EOF

:test_api
echo [STEP] 测试API端点...
echo.

REM 检查curl是否可用
where curl >nul 2>nul
if errorlevel 1 (
    echo [ERROR] curl 命令不可用，请安装curl或使用其他方式测试
    goto :EOF
)

REM 检查应用是否运行
curl -s http://localhost:%SERVER_PORT%/api/test/hello >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 应用未启动或无法访问，请先运行: scripts\ddd-test.bat start
    goto :EOF
)

echo [INFO] 测试基础API...
curl -s http://localhost:%SERVER_PORT%/api/test/hello
echo.

echo [INFO] 测试数据库连接...
curl -s http://localhost:%SERVER_PORT%/api/test/db
echo.

echo [INFO] 测试DDD健康检查...
curl -s http://localhost:%SERVER_PORT%/api/ddd-test/health
echo.

echo [SUCCESS] API测试完成 ✓
goto :EOF

:test_ddd
echo [STEP] 测试DDD模式基础功能...
echo.

REM 检查curl是否可用
where curl >nul 2>nul
if errorlevel 1 (
    echo [ERROR] curl 命令不可用，请安装curl或使用其他方式测试
    goto :EOF
)

REM 检查应用是否运行
curl -s http://localhost:%SERVER_PORT%/api/test/hello >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 应用未启动，请先运行: scripts\ddd-test.bat start
    goto :EOF
)

echo [INFO] 执行DDD健康检查...
curl -s -X GET http://localhost:%SERVER_PORT%/api/ddd-test/health
echo.
echo.

echo [SUCCESS] DDD基础测试完成 ✓
goto :EOF

:test_comprehensive
echo [STEP] 执行完整DDD功能测试...
echo.

REM 检查curl是否可用
where curl >nul 2>nul
if errorlevel 1 (
    echo [ERROR] curl 命令不可用，请安装curl或使用其他方式测试
    goto :EOF
)

REM 检查应用是否运行
curl -s http://localhost:%SERVER_PORT%/api/test/hello >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 应用未启动，请先运行: scripts\ddd-test.bat start
    goto :EOF
)

echo [INFO] 1. 健康检查...
curl -s -X GET http://localhost:%SERVER_PORT%/api/ddd-test/health
echo.
echo.

echo [INFO] 2. 创建测试数据...
curl -s -X POST http://localhost:%SERVER_PORT%/api/ddd-test/create-test-data -H "Content-Type: application/json"
echo.
echo.

echo [INFO] 3. 测试业务操作...
curl -s -X POST http://localhost:%SERVER_PORT%/api/ddd-test/test-business-operations -H "Content-Type: application/json"
echo.
echo.

echo [INFO] 4. 测试领域事件...
curl -s -X POST http://localhost:%SERVER_PORT%/api/ddd-test/test-domain-events -H "Content-Type: application/json"
echo.
echo.

echo [INFO] 5. 获取统计信息...
curl -s -X GET http://localhost:%SERVER_PORT%/api/ddd-test/statistics
echo.
echo.

echo [INFO] 6. 清理测试数据...
curl -s -X DELETE http://localhost:%SERVER_PORT%/api/ddd-test/cleanup-test-data
echo.
echo.

echo [SUCCESS] 完整DDD测试流程完成 ✓
goto :EOF

:clean
echo [STEP] 清理编译缓存...
echo.

REM 检查mvnw是否存在
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd 文件不存在，请确保在正确的目录中运行
    goto :EOF
)

call mvnw.cmd clean
echo [SUCCESS] 清理完成 ✓
goto :EOF

:help
echo ==========================================
echo 📖 AI财务管理系统 - 帮助信息 (修复版)
echo ==========================================
echo.
echo 🏗️ 架构说明:
echo   本系统现在使用统一的DDD架构，不再需要切换模式
echo   所有功能都基于领域驱动设计实现
echo.
echo 可用命令:
echo.
echo   start         启动DDD模式应用 (端口8085)
echo                 - 使用领域驱动设计架构
echo                 - 聚合根、值对象、领域事件
echo                 - 应用服务协调业务逻辑
echo                 - 多租户数据隔离
echo.
echo   test          基础DDD功能测试
echo                 - 健康检查
echo                 - 基础API验证
echo.
echo   test-api      API端点测试
echo                 - 测试所有基础API
echo                 - 验证服务可用性
echo.
echo   test-ddd      完整DDD功能测试
echo                 - 聚合根创建和操作
echo                 - 领域事件发布
echo                 - 业务逻辑验证
echo                 - 数据一致性检查
echo.
echo   compile       编译项目
echo                 - 编译所有Java代码
echo                 - 检查语法错误
echo.
echo   clean         清理Maven编译缓存
echo                 - 删除target目录
echo                 - 重新下载依赖
echo.
echo   help          显示此帮助信息
echo.
echo ==========================================
echo 🔧 环境要求:
echo ==========================================
echo.
echo 必需工具:
echo   ✓ Java 21+
echo   ✓ Maven (使用项目内的mvnw.cmd)
echo   ✓ MySQL 数据库
echo   ✓ curl (用于API测试)
echo.
echo 端口配置:
echo   ✓ 应用端口: 8085
echo   ✓ 数据库: MySQL 3306
echo.
echo ==========================================
echo 🚀 快速开始:
echo ==========================================
echo.
echo 1. 启动应用:     scripts\ddd-test.bat start
echo 2. 测试功能:     scripts\ddd-test.bat test-ddd
echo 3. 查看API:      http://localhost:8085/api/test/hello
echo 4. DDD测试:      http://localhost:8085/api/ddd-test/health
echo.
echo ==========================================
echo 🛠️ 故障排除:
echo ==========================================
echo.
echo 如果启动失败，请尝试:
echo 1. scripts\ddd-test.bat clean
echo 2. scripts\ddd-test.bat compile  
echo 3. 检查Java版本 (java -version)
echo 4. 检查MySQL数据库连接
echo 5. 检查端口8085是否被占用 (netstat -an ^| findstr 8085)
echo 6. 确保在backend目录中运行脚本
echo.
echo 常见错误:
echo - "mvnw.cmd 文件不存在" → 请在backend目录中运行
echo - "curl 命令不可用" → 请安装Git for Windows或独立curl
echo - "应用未启动" → 请先运行 start 命令
echo - "端口被占用" → 请停止占用8085端口的进程
echo.
goto :EOF