#!/bin/bash

echo "🧪 Testing Dockerfile locally..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Function to log
log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Clean up any existing containers/images
log_info "Cleaning up existing containers..."
docker stop auth-test 2>/dev/null || true
docker rm auth-test 2>/dev/null || true
docker rmi auth-app:latest 2>/dev/null || true

# Build the image
log_info "Building Docker image..."
if docker build -t auth-app:latest .; then
    log_success "Image built successfully!"
else
    log_error "Image build failed!"
    exit 1
fi

# Run the container
log_info "Starting container..."
if docker run -d \
    --name auth-test \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=dev \
    -e DATABASE_URL=jdbc:h2:mem:testdb \
    -e DATABASE_USERNAME=sa \
    -e DATABASE_PASSWORD= \
    auth-app:latest; then
    log_success "Container started!"
else
    log_error "Container failed to start!"
    exit 1
fi

# Wait for app to start
log_info "Waiting for application to start..."
sleep 30

# Check container status
log_info "Checking container status..."
if docker ps | grep -q auth-test; then
    log_success "Container is running!"
else
    log_error "Container is not running!"
    docker logs auth-test
    exit 1
fi

# Test health endpoint
log_info "Testing health endpoint..."
if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    log_success "Health endpoint is responding!"
else
    log_warning "Health endpoint not responding, checking logs..."
    docker logs auth-test
fi

# Show container info
log_info "Container information:"
docker ps | grep auth-test
echo ""
log_info "Container logs (last 20 lines):"
docker logs --tail 20 auth-test

echo ""
log_success "🎉 Docker testing completed!"
echo ""
echo "📋 Next steps:"
echo "1. Test your API endpoints manually"
echo "2. Check application logs: docker logs auth-test"
echo "3. Stop container: docker stop auth-test"
echo "4. Clean up: docker rm auth-test && docker rmi auth-app:latest" 