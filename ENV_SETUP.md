# Environment Variables Setup

This application uses environment variables to manage sensitive configuration data like database credentials and JWT secrets.

## Setup Instructions

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Update the `.env` file with your actual values:**
   ```env
   # Database Configuration
   DB_URL=jdbc:postgresql://your-host:port/your-database?sslmode=require
   DB_USERNAME=your_username
   DB_PASSWORD=your_password

   # JWT Configuration
   JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
   JWT_EXPIRATION=86400000

   # Default Admin User Configuration
   ADMIN_DEFAULT_USERNAME=admin
   ADMIN_DEFAULT_PASSWORD=your_secure_admin_password
   ADMIN_DEFAULT_ENABLED=true
   ```

## Important Notes

- The `.env` file is automatically excluded from version control via `.gitignore`
- Never commit sensitive data to your repository
- The JWT secret should be at least 256 bits (32 characters) long
- Change the default admin password in production
- The application will automatically load these variables on startup

## Environment Variables Reference

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | Database connection URL | - | Yes |
| `DB_USERNAME` | Database username | - | Yes |
| `DB_PASSWORD` | Database password | - | Yes |
| `JWT_SECRET` | JWT signing secret key | - | Yes |
| `JWT_EXPIRATION` | JWT token expiration time in milliseconds | 86400000 | No |
| `ADMIN_DEFAULT_USERNAME` | Default admin username | admin | No |
| `ADMIN_DEFAULT_PASSWORD` | Default admin password | admin123 | No |
| `ADMIN_DEFAULT_ENABLED` | Enable/disable default admin creation | true | No |

## Production Deployment

For production deployments, set these environment variables through your deployment platform (Docker, Kubernetes, cloud services, etc.) instead of using the `.env` file.