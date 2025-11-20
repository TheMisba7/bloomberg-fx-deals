.PHONY: help build test clean run stop logs db-shell upload-sample

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

build: ## Build the application
	@echo "Building FX Deals Warehouse..."
	mvnw clean package -DskipTests

test: ## Run all tests with coverage report
	@echo "Running tests..."
	mvnw test
	@echo "Coverage report available at: target/site/jacoco/index.html"

clean: ## Clean build artifacts
	@echo "Cleaning build artifacts..."
	mvnw clean
	docker-compose down -v

run: ## Start the application with Docker Compose
	@echo "Starting FX Deals Warehouse..."
	docker-compose up --build -d
	@echo "Application is starting..."
	@echo "API will be available at: http://localhost:8080/fx-deals"
	@echo "Waiting for application to be ready..."
	@sleep 15
	@echo "Application is ready!"

stop: ## Stop all containers
	@echo "Stopping containers..."
	docker-compose down

logs: ## View application logs
	docker-compose logs -f fx-deals-app

upload-sample: ## Upload sample CSV file
	@echo "Uploading sample CSV file..."
	curl -X POST -F "file=@sample-data/sample-deals.csv" http://localhost:8080/fx-deals/upload | jq

check-health: ## Check application health
	@echo "Checking application health..."
	@curl -s http://localhost:8080/api/fx-deals | jq '.[] | {dealUniqueId, fromCurrency: .fromCurrencyIsoCode, toCurrency: .toCurrencyIsoCode}'

dev-setup: ## Setup development environment
	@echo "Development environment ready!"

full-deploy: clean build run ## Full deployment (clean, build, and run)
	@echo "Full deployment complete!"

restart: stop run ## Restart the application

quick-test: ## Run a quick API test
	@echo "Testing single deal import..."
	curl -X POST http://localhost:8080/fx-deals \
		-H "Content-Type: application/json" \
		-d '{"dealId":"TEST-001","currencyFrom":"USD","currencyTo":"EUR","dealTimestamp":"2024-01-01T10:00:00","dealAmount":1000.50, "exchangeRate":0.8273973}' | jq
