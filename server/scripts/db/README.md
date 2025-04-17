# CompEduX Database Scripts

This directory contains database-related utility scripts for the CompEduX project.

## Available Scripts

### `init_db.py`

A script for initializing PostgreSQL users and databases for CompEduX microservices.

#### Usage

```bash
python init_db.py [OPTIONS]
```

#### Options

- `--host`: PostgreSQL host (default: "localhost")
- `--port`: PostgreSQL port (default: "5432")
- `--user`: PostgreSQL admin username (default: "postgres")
- `--password`: PostgreSQL admin password (default: "postgres")
- `--retry`: Number of connection retry attempts (default: 5)
- `--timeout`: Connection timeout in seconds (default: 10)

#### Features

- Creates database users and databases for all CompEduX services
- Sets appropriate permissions
- Configures database with proper encoding and locale settings
- Supports retry mechanism for database connection

## Adding New Scripts

When adding new database scripts to this directory, please follow these guidelines:

1. Use descriptive names for your scripts
2. Include proper documentation and help text
3. Follow the existing error handling and logging patterns
4. Update this README.md with information about the new script
