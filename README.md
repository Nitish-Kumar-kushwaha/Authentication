# 🔐 Authentication Service

A Spring Boot-based authentication service with JWT, role-based access control, and comprehensive security features.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Make (optional, for easy commands)

### 1. Clone Repository
```bash
git clone <your-repo-url>
cd authentication
```

### 2. Start Services
```bash
# Using Make (recommended)
make build

# OR using Docker Compose directly
docker-compose up -d --build
```

### 3. Access Services

- **Application**: http://localhost:8080
- **pgAdmin**: http://localhost:5050 (admin@admin.com / admin)
- **PostgreSQL**: localhost:5432

## 🐳 Docker Commands

### Build and Run
```bash
# Build Docker image
docker build -t auth-app:latest .

# Run container with H2 database (dev mode)
docker run -d --name auth-app-dev \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  auth-app:latest

# Run with PostgreSQL
docker run -d --name auth-app-prod \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  auth-app:latest
```

### Container Management
```bash
# Check running containers
docker ps

# View logs
docker logs auth-app-dev

# Stop container
docker stop auth-app-dev

# Remove container
docker rm auth-app-dev

# Remove image
docker rmi auth-app:latest
```

### Health Checks
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check container health
docker inspect auth-app-dev | grep Health -A 10
```

## 🐙 Docker Compose Commands

### Using Make (Recommended)
```bash
make help          # Show all available commands
make up            # Start all services
make down          # Stop all services
make build         # Build and start services
make logs          # View logs
make status        # Check service status
make clean         # Clean up everything
```

### Direct Docker Compose
```bash
# Start services
docker-compose up -d

# Build and start
docker-compose up -d --build

# View logs
docker-compose logs -f

# Check status
docker-compose ps

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 🔧 Configuration

### Spring Profiles

### Spring Profiles
- **dev**: H2 in-memory database, detailed logging
- **prod**: PostgreSQL, minimal logging, file logging
- **test**: H2 in-memory database, test configuration

## 🧪 Testing Docker Locally

### Automated Testing Script
```bash
# Make script executable
chmod +x test-docker.sh

# Run automated test
./test-docker.sh
```

### Manual Testing
```bash
# 1. Build image
docker build -t auth-app:latest .

# 2. Run container
docker run -d --name auth-app-test \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  auth-app:latest

# 3. Wait for startup
sleep 30

# 4. Check health
curl http://localhost:8080/actuator/health

# 5. View logs
docker logs auth-app-test

# 6. Cleanup
docker stop auth-app-test && docker rm auth-app-test
```

## ☁️ Deploy to Render

### 1. Connect Repository
- Connect your GitHub repository to Render
- Render will automatically detect the Dockerfile

### 2. Environment Variables
Set these in Render dashboard:
```bash
SPRING_PROFILES_ACTIVE=prod
# Database and JWT settings are configured in application-prod.yml
# Override only if you need custom values
```

### 3. Deploy
- Render will build and deploy automatically
- No docker-compose needed for production

## 🏗️ Project Structure

```
authentication/
├── src/main/java/
│   └── com/security/authentication/
│       ├── config/          # Security & configuration
│       ├── controller/      # REST endpoints
│       ├── dto/            # Data transfer objects
│       ├── entity/         # JPA entities
│       ├── repository/     # Data access layer
│       ├── security/       # JWT & security
│       └── services/       # Business logic
├── src/main/resources/
│   ├── application.yml     # Global configuration
│   ├── application-dev.yml # Development profile
│   ├── application-prod.yml # Production profile
│   └── application-test.yml # Test profile
├── Dockerfile              # Container definition
├── docker-compose.yml      # Local development
├── Makefile               # Easy commands
└── test-docker.sh         # Docker testing script
```

## 🚨 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill process or use different port
docker run -p 8081:8080 auth-app:latest
```

#### Container Exits Immediately
```bash
# Check logs
docker logs <container-name>

# Check if database is accessible
docker exec -it <container-name> sh
```

#### Database Connection Issues
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres
```

### Reset Everything
```bash
# Stop and remove everything
make clean

# Rebuild from scratch
make build
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Render Documentation](https://render.com/docs)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with Docker
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
