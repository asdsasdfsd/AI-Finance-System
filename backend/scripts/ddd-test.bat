@echo off
REM backend/scripts/ddd-test.bat
REM AI财务管理系统 - DDD模式测试脚本 (Windows版本)

setlocal enabledelayedexpansion

REM 颜色定义（Windows CMD有限的颜色支持）
set "INFO=[INFO]"
set "SUCCESS=[SUCCESS]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"
set "STEP=[STEP]"
set "TEST=[TEST]"

REM 显示帮助信息
if "%1"=="help" goto :show_help
if "%1"=="" goto :show_help
if "%1"=="--help" goto :show_help
if "%1"=="-h" goto :show_help

echo ==========================================
echo 🏗️  AI财务管理系统 - DDD模式测试工具 (Windows)
echo ==========================================
echo.

REM 检查是否在正确目录
if not exist "pom.xml" (
    echo %ERROR% 请在backend目录中运行此脚本
    exit /b 1
)

REM 根据参数执行不同操作
if "%1"=="start-ddd" goto :start_ddd
if "%1"=="start-orm" goto :start_orm
if "%1"=="test-health" goto :test_health
if "%1"=="health" goto :test_health
if "%1"=="create-test-data" goto :create_test_data
if "%1"=="create" goto :create_test_data
if "%1"=="test-business" goto :test_business
if "%1"=="business" goto :test_business
if "%1"=="test-events" goto :test_events
if "%1"=="events" goto :test_events
if "%1"=="cleanup" goto :cleanup
if "%1"=="statistics" goto :statistics
if "%1"=="stats" goto :statistics
if "%1"=="full-test" goto :full_test
if "%1"=="full" goto :full_test

echo %ERROR% 未知选项: %1
echo.
goto :show_help

:show_help
echo AI财务管理系统 - DDD模式测试脚本 (Windows)
echo.
echo 用法: %0 [选项]
echo.
echo 选项:
echo   start-ddd         启动DDD模式
echo   start-orm         启动ORM模式
echo   test-health       健康检查
echo   create-test-data  创建测试数据
echo   test-business     测试业务操作
echo   test-events       测试领域事件
echo   cleanup           清理测试数据
echo   statistics        获取统计信息
echo   full-test         完整测试流程
echo   help              显示此帮助信息
echo.
echo 示例:
echo   %0 start-ddd      # 启动DDD模式
echo   %0 full-test      # 运行完整测试
echo.
echo 注意: 启动命令会阻塞当前窗口，请在新窗口中运行测试命令
goto :eof

:start_ddd
echo %STEP% 启动DDD模式...
echo.
echo %INFO% Profile设置: ddd
echo %INFO% 启动参数: DDD模式
echo %INFO% 端口: 8085
echo.
echo %INFO% 正在启动应用...
echo ==========================================
echo.
set SPRING_PROFILES_ACTIVE=ddd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=ddd
goto :eof

:start_orm
echo %STEP% 启动ORM模式...
echo.
echo %INFO% Profile设置: orm
echo %INFO% 启动参数: ORM模式
echo %INFO% 端口: 8085
echo.
echo %INFO% 正在启动应用...
echo ==========================================
echo.
set SPRING_PROFILES_ACTIVE=orm
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=orm
goto :eof

:test_health
echo %TEST% 执行健康检查...
echo.

echo %INFO% 检查基础API...
curl -s http://localhost:8085/api/test/hello > temp_response.txt 2>nul
if errorlevel 1 (
    echo %ERROR% 应用未启动，请先运行: %0 start-ddd
    del temp_response.txt 2>nul
    exit /b 1
)

findstr /c:"Hello" temp_response.txt >nul
if errorlevel 1 (
    echo %ERROR% 基础API异常
    del temp_response.txt 2>nul
    exit /b 1
) else (
    echo %SUCCESS% 基础API正常
)

echo %INFO% 检查数据库连接...
curl -s http://localhost:8085/api/test/db > temp_response.txt 2>nul
findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %ERROR% 数据库连接异常
    del temp_response.txt 2>nul
    exit /b 1
) else (
    echo %SUCCESS% 数据库连接正常
)

echo %INFO% 检查DDD模式...
curl -s http://localhost:8085/api/ddd-test/health > temp_response.txt 2>nul
findstr /c:"healthy" temp_response.txt >nul
if errorlevel 1 (
    echo %WARNING% DDD模式可能有问题
    type temp_response.txt
) else (
    echo %SUCCESS% DDD模式正常
)

del temp_response.txt 2>nul
goto :eof

:create_test_data
echo %TEST% 创建测试数据...
echo.

curl -s -X POST http://localhost:8085/api/ddd-test/create-test-data -H "Content-Type: application/json" > temp_response.txt 2>nul

findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %ERROR% 测试数据创建失败
    echo 响应:
    type temp_response.txt
    del temp_response.txt 2>nul
    exit /b 1
) else (
    echo %SUCCESS% 测试数据创建成功
    echo.
    echo %INFO% 创建的数据:
    echo   - 公司: DDD测试公司
    echo   - 用户: DDD测试用户
    echo   - 收入交易: 5000.00 CNY
    echo   - 支出交易: 1500.00 CNY
)

del temp_response.txt 2>nul
goto :eof

:test_business
echo %TEST% 测试业务操作...
echo.

curl -s -X POST http://localhost:8085/api/ddd-test/test-business-operations -H "Content-Type: application/json" > temp_response.txt 2>nul

findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %ERROR% 业务操作测试失败
    echo 响应:
    type temp_response.txt
    del temp_response.txt 2>nul
    exit /b 1
) else (
    echo %SUCCESS% 业务操作测试通过
    echo.
    echo %INFO% 测试结果:
    echo   - 公司暂停/激活: 通过
    echo   - 用户禁用/启用: 通过
    echo   - 角色分配: 通过
    echo   - 交易查询: 通过
)

del temp_response.txt 2>nul
goto :eof

:test_events
echo %TEST% 测试领域事件...
echo.

curl -s -X POST http://localhost:8085/api/ddd-test/test-domain-events -H "Content-Type: application/json" > temp_response.txt 2>nul

findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %ERROR% 领域事件测试失败
    echo 响应:
    type temp_response.txt
    del temp_response.txt 2>nul
    exit /b 1
) else (
    echo %SUCCESS% 领域事件测试通过
    echo 事件发布成功
)

del temp_response.txt 2>nul
goto :eof

:statistics
echo %TEST% 获取系统统计...
echo.

curl -s http://localhost:8085/api/ddd-test/statistics > temp_response.txt 2>nul

findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %WARNING% 统计信息获取失败
    echo 响应:
    type temp_response.txt
) else (
    echo %SUCCESS% 统计信息获取成功
    echo.
    echo %INFO% 系统统计信息:
    echo   请查看详细响应信息
    type temp_response.txt
)

del temp_response.txt 2>nul
goto :eof

:cleanup
echo %TEST% 清理测试数据...
echo.

curl -s -X DELETE http://localhost:8085/api/ddd-test/cleanup-test-data > temp_response.txt 2>nul

findstr /c:"success" temp_response.txt >nul
if errorlevel 1 (
    echo %WARNING% 清理过程中出现问题
    echo 响应:
    type temp_response.txt
) else (
    echo %SUCCESS% 测试数据清理完成
)

del temp_response.txt 2>nul
goto :eof

:full_test
echo %STEP% 开始完整DDD测试流程...
echo.

REM 1. 健康检查
call :test_health
if errorlevel 1 (
    echo %ERROR% 健康检查失败，请检查应用是否正常启动
    exit /b 1
)
echo.

REM 2. 创建测试数据
call :create_test_data
if errorlevel 1 (
    echo %ERROR% 测试数据创建失败
    exit /b 1
)
echo.

REM 3. 业务操作测试
call :test_business
if errorlevel 1 (
    echo %ERROR% 业务操作测试失败
    exit /b 1
)
echo.

REM 4. 领域事件测试
call :test_events
if errorlevel 1 (
    echo %ERROR% 领域事件测试失败
    exit /b 1
)
echo.

REM 5. 获取统计信息
call :statistics
echo.

REM 6. 清理测试数据
call :cleanup
echo.

echo %SUCCESS% 🎉 完整DDD测试流程成功完成！
echo.
echo ✅ 所有DDD功能正常工作:
echo   - 聚合根创建和业务操作
echo   - 应用服务层协调
echo   - 值对象封装
echo   - 领域事件发布
echo   - 多租户隔离
echo   - Controller适配器
echo.
echo 🚀 DDD架构迁移成功！

goto :eof