#!/bin/bash

echo "connecting to database"
~/tools/mysql/mysql-8.4.11-macos15-arm64/bin/mysqld_safe \
--datadir=/Users/aritra-pt8295/tools/mysql-data 

~/tools/mysql/mysql-8.4.11-macos15-arm64/bin/mysql -u root


sleep 2
echo "connected to database successfully"