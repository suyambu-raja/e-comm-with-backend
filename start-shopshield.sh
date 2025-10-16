#!/bin/bash

# ShopShield - Complete Setup and Startup Script
# This script sets up the complete ShopShield environment

set -e

echo "🛡️  ShopShield - E-commerce Compliance Platform Setup"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker and Docker Compose are installed
check_dependencies() {
    print_status "Checking dependencies..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    print_success "All dependencies are installed"
}

# Clean up existing containers and volumes
cleanup() {
    print_status "Cleaning up existing containers..."
    docker-compose down --remove-orphans 2>/dev/null || true
    print_success "Cleanup completed"
}

# Build and start services
start_services() {
    print_status "Building and starting ShopShield services..."
    
    # Build all services
    print_status "Building services..."
    docker-compose build
    
    # Start database first
    print_status "Starting PostgreSQL database..."
    docker-compose up -d postgres-db
    
    # Wait for database to be ready
    print_status "Waiting for database to be ready..."
    sleep 10
    
    # Start microservices
    print_status "Starting microservices..."
    docker-compose up -d ocr-service cv-service
    
    # Wait for microservices
    sleep 5
    
    # Start backend
    print_status "Starting Spring Boot backend..."
    docker-compose up -d spring-backend
    
    # Wait for backend
    sleep 10
    
    # Start frontend
    print_status "Starting React frontend..."
    docker-compose up -d frontend
    
    print_success "All services started successfully!"
}

# Health check function
health_check() {
    print_status "Performing health checks..."
    
    # Check database
    if docker-compose exec -T postgres-db pg_isready -U admin -d shopshield &>/dev/null; then
        print_success "✓ Database is healthy"
    else
        print_warning "⚠ Database health check failed"
    fi
    
    # Check backend
    sleep 5
    if curl -f http://localhost:8080/api/health &>/dev/null; then
        print_success "✓ Backend API is healthy"
    else
        print_warning "⚠ Backend API health check failed"
    fi
    
    # Check OCR service
    if curl -f http://localhost:8000/health &>/dev/null; then
        print_success "✓ OCR Service is healthy"
    else
        print_warning "⚠ OCR Service health check failed"
    fi
    
    # Check CV service
    if curl -f http://localhost:8001/health &>/dev/null; then
        print_success "✓ CV Service is healthy"
    else
        print_warning "⚠ CV Service health check failed"
    fi
    
    # Check frontend
    if curl -f http://localhost:3000/health &>/dev/null; then
        print_success "✓ Frontend is healthy"
    else
        print_warning "⚠ Frontend health check failed"
    fi
}

# Display service information
show_services() {
    echo ""
    echo "🚀 ShopShield Services Status"
    echo "============================="
    echo "🗄️  Database:       http://localhost:5432"
    echo "⚙️  Backend API:     http://localhost:8080/api"
    echo "👁️  OCR Service:     http://localhost:8000"
    echo "🤖 CV Service:      http://localhost:8001"
    echo "🌐 Frontend:        http://localhost:3000"
    echo ""
    echo "📚 API Documentation:"
    echo "   - Backend:       http://localhost:8080/api/swagger-ui.html"
    echo "   - OCR Service:   http://localhost:8000/docs"
    echo "   - CV Service:    http://localhost:8001/docs"
    echo ""
    echo "🔐 Default Credentials:"
    echo "   - Username: admin"
    echo "   - Password: admin123"
    echo ""
    echo "📊 To view logs: docker-compose logs -f [service-name]"
    echo "🛑 To stop: docker-compose down"
    echo ""
}

# Main execution
main() {
    check_dependencies
    cleanup
    start_services
    sleep 15  # Give services time to fully start
    health_check
    show_services
    
    print_success "🎉 ShopShield is now running!"
    print_status "Access the application at: http://localhost:3000"
}

# Handle script interruption
trap 'print_error "Setup interrupted"; exit 1' INT

# Run main function
main "$@"