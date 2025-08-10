.PHONY: help up down build logs status clean

help:
	@echo "Authentication App - Docker Commands:"
	@echo ""
	@echo "  make up      - Start all services"
	@echo "  make down    - Stop all services"
	@echo "  make build   - Build and start services"
	@echo "  make logs    - View logs"
	@echo "  make status  - Check service status"
	@echo "  make clean   - Clean up Docker resources"

up:
	@echo "Starting services..."
	docker-compose up -d

down:
	@echo "Stopping services..."
	docker-compose down

build:
	@echo "Building and starting services..."
	docker-compose up -d --build

logs:
	@echo "Viewing logs..."
	docker-compose logs -f

status:
	@echo "Service status:"
	docker-compose ps

clean:
	@echo "Cleaning up Docker resources..."
	docker-compose down -v
	docker system prune -f 