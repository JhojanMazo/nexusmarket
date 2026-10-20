#!/usr/bin/env bash
# Prompt for the SQL Server SA password (input hidden)
read -s -p "Enter DB password for user 'sa': " DB_PASSWORD
export DB_PASSWORD
echo
# Start the Spring Boot application
mvn spring-boot:run
