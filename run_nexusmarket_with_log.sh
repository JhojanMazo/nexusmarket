#!/usr/bin/env bash
# Prompt for the SQL Server SA password (input hidden)
read -s -p "Enter DB password for user 'sa': " DB_PASSWORD
export DB_PASSWORD
echo
# Run Spring Boot and capture logs
log_file="$(pwd)/spring.log"
echo "Logging to $log_file"
# Use -Dspring-boot.run.arguments=--debug to get full stack traces
mvn spring-boot:run -Dspring-boot.run.arguments=--debug > "$log_file" 2>&1
