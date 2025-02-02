# Etherscan application
This is a Kotlin Spring Boot application that:

- Uses Liquibase to manage and apply database migrations.
- Generates jOOQ classes based on the current database schema.
- Processes Ethereum block transactions using scheduled tasks.
- Connects to a PostgreSQL database.
- Uses Java 21 via Gradle’s toolchain.

## Functionality
- **Cron-Job**: Every minute, the application queries the Etherscan API, and saves transaction information for all blocks starting from block 19000000.
- **REST API**: An endpoint `/max-balance-change` that returns the address with the maximum balance change (the sum of incoming and outgoing transactions) over the last 100 blocks.

### 1. Start the Application

You should set your `ETHERSCAN_API_KEY` in docker-compose.yaml and than run:

```bash
docker-compose up
```

This command:
#### 1.Starts PostgreSQL container with:
- Database: postgres_db (or adjust if you modify the configuration)
- User: postgres_user 
- Password: postgres_password 
- The container’s internal port 5432 is mapped to host port 5430

#### 2.Builds and starts the application:
- Builds and starts Kotlin application using gradle




