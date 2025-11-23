#!/bin/bash

echo "=== MySQL 服务验证脚本 ==="
echo ""

# 1. 检查进程
echo "1. 检查 MySQL 进程:"
if ps aux | grep mysqld | grep -v grep > /dev/null; then
    echo "   ✅ MySQL 进程正在运行"
    ps aux | grep mysqld | grep -v grep | head -1
else
    echo "   ❌ MySQL 进程未运行"
fi
echo ""

# 2. 检查端口
echo "2. 检查端口 3306:"
if lsof -i :3306 > /dev/null 2>&1; then
    echo "   ✅ 端口 3306 正在监听"
    lsof -i :3306
else
    echo "   ❌ 端口 3306 未被监听"
fi
echo ""

# 3. 检查 PID 文件
echo "3. 检查 PID 文件:"
if [ -f /usr/local/mysql/data/mysqld.local.pid ]; then
    echo "   ✅ PID 文件存在"
    echo "   PID: $(cat /usr/local/mysql/data/mysqld.local.pid)"
else
    echo "   ❌ PID 文件不存在"
fi
echo ""

# 4. 测试连接
echo "4. 测试 MySQL 连接:"
if mysql -u root -e "SELECT VERSION();" 2>/dev/null; then
    echo "   ✅ MySQL 连接成功"
    echo ""
    echo "5. MySQL 版本信息:"
    mysql -u root -e "SELECT VERSION() as 'MySQL Version';" 2>/dev/null
    echo ""
    echo "6. 当前数据库列表:"
    mysql -u root -e "SHOW DATABASES;" 2>/dev/null
else
    echo "   ⚠️  无法连接 MySQL（可能需要密码或服务未启动）"
    echo "   请尝试: mysql -u root -p"
fi
echo ""

echo "=== 验证完成 ==="

