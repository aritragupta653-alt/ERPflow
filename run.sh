#!/bin/bash

echo "Building project..."
mvn clean package

echo "Stopping Tomcat..."
~/tools/tomcat/apache-tomcat-10.1.59/bin/shutdown.sh

sleep 2

echo "Removing old deployment..."
rm -rf ~/tools/tomcat/apache-tomcat-10.1.59/webapps/erpflow
rm -f ~/tools/tomcat/apache-tomcat-10.1.59/webapps/erpflow.war

echo "Deploying new WAR..."
cp target/erpflow.war \
~/tools/tomcat/apache-tomcat-10.1.59/webapps/

echo "Starting Tomcat..."
~/tools/tomcat/apache-tomcat-10.1.59/bin/startup.sh

echo ""
echo "ERPFlow is running!"
echo "http://localhost:8080/erpflow/items"
