# Nexis paper desk

A simulated brokerage for learning fullstack Java, React, and containers on AWS. Orders fill immediately at a price the server invents. There is no live broker and no real money.

If you already build Android apps, the mapping is:

| Android | This API |
| --- | --- |
| Activity | Controller. It checks the request and picks a status code. |
| ViewModel / use case | Service. Trading rules live here. |
| Room DAO | Spring Data repository |
| Entity you send over the network | Do not. Controllers return records in `api`. |
| Shared in-memory state | Unsafe once two users exist. Postgres is the source of truth. |

## Run it on your machine

Two processes, plus Postgres:

```bash
docker compose up postgres -d
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

Open http://localhost:5173, register, and buy a symbol. The Vite dev server proxies `/api` to port 8080.

`bootRun` uses the defaults in `backend/src/main/resources/application.yml` (`nexis` / `nexis` on `localhost:5432`). To see the SQL for an order, start the API with the local profile:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## Run it as containers

```bash
docker compose up --build
```

The desk is at http://localhost:8088. Compose starts Postgres, the API, and an nginx image that serves the React build and proxies `/api` to the API container.

## What to read first

1. `backend/src/main/resources/db/migration/V1__init.sql` is the schema. Flyway applies it. Hibernate only checks that the Java entities still match (`ddl-auto: validate`).
2. `backend/src/main/java/com/nexis/trade/trading/TradingService.java` is the trading rule. A buy or sell locks the account row, checks cash or shares, writes the fill, and updates cash and the position in one transaction. Two buys at once cannot both spend the same cash.
3. `backend/src/main/java/com/nexis/trade/market/PriceSimulator.java` rewrites each quote on a schedule. The order code only reads the latest row, so a real quote feed can replace the simulator later.
4. `backend/src/main/java/com/nexis/trade/config/SecurityConfig.java` decides which routes are public. Register and login issue a JWT. Later requests send `Authorization: Bearer ...`.
5. `frontend/src/pages/DeskPage.tsx` is the client. TanStack Query polls instruments, the portfolio, and fills every five seconds.

Money is `BigDecimal`. Share counts are integers. A new account starts with $100,000 paper cash.

API docs, when the API is running: http://localhost:8080/swagger-ui.html

## Tests

```bash
cd backend && ./gradlew test
```

The trading tests use Testcontainers, so Docker has to be running. They cover a normal buy and sell, overspend, oversell, an unknown symbol, a second user who cannot see the first portfolio, and two buys racing for the same cash.

## Deploy on AWS

The stack is in `infra/terraform`. It builds a learning-sized layout:

- ECR repositories `nexis-api` and `nexis-web`
- ECS Fargate services on public subnets (so the tasks can pull images without a NAT gateway)
- an Application Load Balancer in front of the web service
- RDS Postgres, not publicly reachable, password stored by Secrets Manager
- a separate Secrets Manager secret for `JWT_SECRET`
- CloudWatch log groups `/ecs/nexis-api` and `/ecs/nexis-web`
- Cloud Map name `api.nexis.local`, which the web container proxies to

Fargate, RDS, and a load balancer cost money while they exist. Tear the stack down with `terraform destroy` when you are done.

This environment does not ship with your AWS credentials. Apply it from your own account:

```bash
cd infra/terraform
terraform init
terraform apply
```

The services will fail health checks until images exist. From the repo root, with Docker and the AWS CLI pointed at the same account and region:

```bash
AWS_REGION=us-east-1 ./scripts/push-images.sh
```

Then open the `alb_dns_name` output. The first deploy is this manual path. After it works, `.github/workflows/deploy.yml` can repeat the image push. Add repository variables `AWS_REGION` and `AWS_ROLE_ARN` (an IAM role GitHub Actions can assume with OIDC) before running that workflow. `.github/workflows/ci.yml` only runs tests and the web build.

If RDS rejects `engine_version = "16"`, set a version your region still offers:

```bash
terraform apply -var db_engine_version=16.8
```
