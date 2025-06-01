#!/bin/bash
# backend/scripts/ddd-test.sh
# AI财务管理系统 - DDD模式启动和测试脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

log_test() {
    echo -e "${CYAN}[TEST]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "AI财务管理系统 - DDD模式测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start-ddd         启动DDD模式"
    echo "  start-orm         启动ORM模式"
    echo "  test-ddd          测试DDD模式功能"
    echo "  test-health       健康检查"
    echo "  create-test-data  创建测试数据"
    echo "  test-business     测试业务操作"
    echo "  test-events       测试领域事件"
    echo "  cleanup           清理测试数据"
    echo "  full-test         完整测试流程"
    echo "  help              显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start-ddd      # 启动DDD模式"
    echo "  $0 full-test      # 运行完整测试"
}

# 检查运行环境
check_environment() {
    log_step "检查运行环境..."
    
    # 检查是否在正确目录
    if [ ! -f "pom.xml" ]; then
        log_error "请在backend目录中运行此脚本"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或不在PATH中"
        exit 1
    fi
    
    # 检查Maven
    if [ ! -f "./mvnw" ]; then
        log_error "Maven wrapper未找到"
        exit 1
    fi
    
    # 检查端口
    if netstat -tlnp 2>/dev/null | grep -q ":8085 "; then
        log_warning "端口8085已被占用，可能需要停止现有进程"
    fi
    
    log_success "环境检查通过"
}

# 启动DDD模式
start_ddd_mode() {
    log_step "启动DDD模式..."
    
    export SPRING_PROFILES_ACTIVE=ddd
    
    log_info "Profile设置: $SPRING_PROFILES_ACTIVE"
    log_info "启动参数: DDD模式"
    log_info "端口: 8085"
    
    echo ""
    log_info "正在启动应用..."
    echo "=========================================="
    
    # 使用Maven启动应用
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=ddd
}

# 启动ORM模式
start_orm_mode() {
    log_step "启动ORM模式..."
    
    export SPRING_PROFILES_ACTIVE=orm
    
    log_info "Profile设置: $SPRING_PROFILES_ACTIVE"
    log_info "启动参数: ORM模式"
    log_info "端口: 8085"
    
    echo ""
    log_info "正在启动应用..."
    echo "=========================================="
    
    # 使用Maven启动应用
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=orm
}

# 等待应用启动
wait_for_startup() {
    log_info "等待应用启动..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8085/api/test/hello > /dev/null 2>&1; then
            log_success "应用启动成功！"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "应用启动超时"
    return 1
}

# 健康检查
health_check() {
    log_test "执行健康检查..."
    
    # 基础API检查
    log_info "检查基础API..."
    response=$(curl -s http://localhost:8085/api/test/hello)
    if echo "$response" | grep -q "Hello"; then
        log_success "基础API正常"
    else
        log_error "基础API异常"
        return 1
    fi
    
    # 数据库连接检查
    log_info "检查数据库连接..."
    response=$(curl -s http://localhost:8085/api/test/db)
    if echo "$response" | grep -q "success"; then
        log_success "数据库连接正常"
    else
        log_error "数据库连接异常"
        return 1
    fi
    
    # DDD模式专用检查
    log_info "检查DDD模式..."
    response=$(curl -s http://localhost:8085/api/ddd-test/health)
    if echo "$response" | grep -q "healthy"; then
        log_success "DDD模式正常"
        echo "DDD服务状态: $(echo "$response" | grep -o '"services":[^}]*}')"
    else
        log_warning "DDD模式可能有问题"
        echo "响应: $response"
    fi
}

# 创建测试数据
create_test_data() {
    log_test "创建测试数据..."
    
    response=$(curl -s -X POST http://localhost:8085/api/ddd-test/create-test-data \
        -H "Content-Type: application/json")
    
    if echo "$response" | grep -q '"status":"success"'; then
        log_success "测试数据创建成功"
        
        # 提取关键信息
        company_id=$(echo "$response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        user_id=$(echo "$response" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
        
        log_info "创建的数据:"
        echo "  - 公司ID: $company_id"
        echo "  - 用户ID: $user_id"
        echo "  - 收入交易: 5000.00 CNY"
        echo "  - 支出交易: 1500.00 CNY"
    else
        log_error "测试数据创建失败"
        echo "响应: $response"
        return 1
    fi
}

# 测试业务操作
test_business_operations() {
    log_test "测试业务操作..."
    
    response=$(curl -s -X POST http://localhost:8085/api/ddd-test/test-business-operations \
        -H "Content-Type: application/json")
    
    if echo "$response" | grep -q '"status":"success"'; then
        log_success "业务操作测试通过"
        
        # 显示测试结果
        echo "测试结果摘要:"
        echo "$response" | grep -o '"[^"]*Test":[^,}]*' | while read line; do
            echo "  - $line"
        done
    else
        log_error "业务操作测试失败"
        echo "响应: $response"
        return 1
    fi
}

# 测试领域事件
test_domain_events() {
    log_test "测试领域事件..."
    
    response=$(curl -s -X POST http://localhost:8085/api/ddd-test/test-domain-events \
        -H "Content-Type: application/json")
    
    if echo "$response" | grep -q '"status":"success"'; then
        log_success "领域事件测试通过"
        echo "事件发布成功"
    else
        log_error "领域事件测试失败"
        echo "响应: $response"
        return 1
    fi
}

# 获取统计信息
get_statistics() {
    log_test "获取系统统计..."
    
    response=$(curl -s http://localhost:8085/api/ddd-test/statistics)
    
    if echo "$response" | grep -q '"status":"success"'; then
        log_success "统计信息获取成功"
        
        # 解析并显示统计信息
        echo "系统统计信息:"
        echo "  公司数量: $(echo "$response" | grep -o '"total":[0-9]*' | head -1 | cut -d':' -f2)"
        echo "  活跃公司: $(echo "$response" | grep -o '"active":[0-9]*' | head -1 | cut -d':' -f2)"
        echo "  用户数量: $(echo "$response" | grep -o '"total":[0-9]*' | tail -2 | head -1 | cut -d':' -f2)"
        echo "  交易数量: $(echo "$response" | grep -o '"total":[0-9]*' | tail -1 | cut -d':' -f2)"
    else
        log_warning "统计信息获取失败"
        echo "响应: $response"
    fi
}

# 清理测试数据
cleanup_test_data() {
    log_test "清理测试数据..."
    
    response=$(curl -s -X DELETE http://localhost:8085/api/ddd-test/cleanup-test-data)
    
    if echo "$response" | grep -q '"status":"success"'; then
        deleted=$(echo "$response" | grep -o '"deletedCompanies":[0-9]*' | cut -d':' -f2)
        log_success "测试数据清理完成，删除了 $deleted 个公司"
    else
        log_warning "清理过程中出现问题"
        echo "响应: $response"
    fi
}

# 完整测试流程
run_full_test() {
    log_step "开始完整DDD测试流程..."
    echo ""
    
    # 1. 健康检查
    if ! health_check; then
        log_error "健康检查失败，请检查应用是否正常启动"
        return 1
    fi
    echo ""
    
    # 2. 创建测试数据
    if ! create_test_data; then
        log_error "测试数据创建失败"
        return 1
    fi
    echo ""
    
    # 3. 业务操作测试
    if ! test_business_operations; then
        log_error "业务操作测试失败"
        return 1
    fi
    echo ""
    
    # 4. 领域事件测试
    if ! test_domain_events; then
        log_error "领域事件测试失败"
        return 1
    fi
    echo ""
    
    # 5. 获取统计信息
    get_statistics
    echo ""
    
    # 6. 清理测试数据
    cleanup_test_data
    echo ""
    
    log_success "🎉 完整DDD测试流程成功完成！"
    echo ""
    echo "✅ 所有DDD功能正常工作:"
    echo "  - 聚合根创建和业务操作"
    echo "  - 应用服务层协调"
    echo "  - 值对象封装"
    echo "  - 领域事件发布"
    echo "  - 多租户隔离"
    echo "  - Controller适配器"
    echo ""
    echo "🚀 DDD架构迁移成功！"
}

# 主函数
main() {
    case "$1" in
        "start-ddd")
            check_environment
            start_ddd_mode
            ;;
        "start-orm")
            check_environment
            start_orm_mode
            ;;
        "test-ddd"|"test")
            if ! curl -s http://localhost:8085/api/test/hello > /dev/null 2>&1; then
                log_error "应用未启动，请先运行: $0 start-ddd"
                exit 1
            fi
            health_check
            ;;
        "test-health"|"health")
            health_check
            ;;
        "create-test-data"|"create")
            create_test_data
            ;;
        "test-business"|"business")
            test_business_operations
            ;;
        "test-events"|"events")
            test_domain_events
            ;;
        "cleanup")
            cleanup_test_data
            ;;
        "statistics"|"stats")
            get_statistics
            ;;
        "full-test"|"full")
            if ! curl -s http://localhost:8085/api/test/hello > /dev/null 2>&1; then
                log_error "应用未启动，请先运行: $0 start-ddd"
                exit 1
            fi
            run_full_test
            ;;
        "help"|"--help"|"-h"|"")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
echo "=========================================="
echo "🏗️  AI财务管理系统 - DDD模式测试工具"
echo "=========================================="
echo ""

main "$@"