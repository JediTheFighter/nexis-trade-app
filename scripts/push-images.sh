#!/usr/bin/env bash
set -euo pipefail

# Manual image publish after `terraform apply`. The GitHub Actions deploy
# workflow does the same thing once an AWS role is configured.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGION="${AWS_REGION:-us-east-1}"
ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
REGISTRY="${ACCOUNT}.dkr.ecr.${REGION}.amazonaws.com"

aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"

docker build -t "${REGISTRY}/nexis-api:latest" "$ROOT/backend"
docker build -t "${REGISTRY}/nexis-web:latest" "$ROOT/frontend"
docker push "${REGISTRY}/nexis-api:latest"
docker push "${REGISTRY}/nexis-web:latest"

aws ecs update-service --region "$REGION" --cluster nexis --service nexis-api --force-new-deployment >/dev/null
aws ecs update-service --region "$REGION" --cluster nexis --service nexis-web --force-new-deployment >/dev/null

echo "Pushed images and started a new deployment. ALB DNS:"
terraform -chdir="$ROOT/infra/terraform" output -raw alb_dns_name
echo
