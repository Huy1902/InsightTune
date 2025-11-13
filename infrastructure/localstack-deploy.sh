#!/bin/bash

set -euo pipefail

# Route ALL AWS services to LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_EC2_METADATA_DISABLED=true
export AWS_ENDPOINT_URL=http://localhost:4566

BUCKET="cf-templates-localstack"

echo ">> Ensuring S3 bucket $BUCKET exists in LocalStack..."
if ! aws s3 ls "s3://$BUCKET" >/dev/null 2>&1; then
  aws s3 mb "s3://$BUCKET"
fi

echo ">> Deploying CloudFormation stack 'insighttune'..."
aws cloudformation deploy \
  --stack-name insighttune \
  --template-file "./cdk.out/localstack.template.json" \
  --s3-bucket "$BUCKET" \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset

echo ">> ALB DNS (if present):"
aws elbv2 describe-load-balancers \
  --query "LoadBalancers[0].DNSName" \
  --output text || true
echo
