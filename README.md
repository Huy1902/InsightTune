# SpoTube

This is our project for Mobile Development Course in University of Engineer and Technology

## *Tutor*

-  Phd. Nguyen Duc Anh
-  Master Tran Manh Cuong

## Back end tutorial

### 1. Preliminaries:
- [Localstack](https://www.localstack.cloud/)
- [Aws-cli](https://aws.amazon.com/cli/)
- [Docker](https://www.docker.com/)

### 2. Main step (for local):
1. Take dataset from [this link](https://drive.google.com/drive/folders/13tPAT4skSkejj2Nw0Gg8mM_aPwNcRfQv?usp=drive_link)
for recommend system
2. Copy it to [this directory](rec-sys/dataset)3
3. Run `deploy.sh`

### 3. On aws:
1. Add config login for actual aws
2. Use [template](infrastructure/cdk.out/localstack.template.json) to build on aws
3. (Optional) You can adjust [this file](infrastructure/src/main/java/com/pm/stack/LocalStack.java)
to follow your desired config
