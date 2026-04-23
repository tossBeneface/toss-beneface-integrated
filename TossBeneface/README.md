# TossBeneface

프로필별 실행 방식:

1) `local`
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

`local` 프로필은 개발 편의를 위해 기본값 fallback을 유지합니다. 운영/개발 서버 성격의 `dev`, `prod`만 런타임 환경변수 주입을 강제합니다.

2) `dev` 또는 `prod`
`dev`와 `prod`는 리포지토리 내부 설정값을 사용하지 않고 런타임 환경변수로만 기동합니다.

필수 환경변수:
```bash
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_DATA_REDIS_HOST=
SPRING_DATA_REDIS_PORT=
SPRING_DATA_REDIS_PASSWORD=
TOKEN_SECRET=
COOKIE_ENCRYPTION_SECRET=
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
TOSS_TEST_CLIENT_API_KEY=
TOSS_TEST_SECRET_API_KEY=
TOSS_SUCCESS_URL=
TOSS_FAIL_URL=
FAST_API_HOST=
```

예시:
```bash
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL='jdbc:mysql://db-host:3306/toss_beneface?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul'
export SPRING_DATASOURCE_USERNAME='app_user'
export SPRING_DATASOURCE_PASSWORD='change-me'
export SPRING_DATA_REDIS_HOST='redis.internal'
export SPRING_DATA_REDIS_PORT='6379'
export SPRING_DATA_REDIS_PASSWORD=''
export TOKEN_SECRET='change-me-to-a-long-random-secret'
export COOKIE_ENCRYPTION_SECRET='change-me-cookie-secret-key-2026'
export AWS_ACCESS_KEY='change-me'
export AWS_SECRET_KEY='change-me'
export TOSS_TEST_CLIENT_API_KEY='change-me'
export TOSS_TEST_SECRET_API_KEY='change-me'
export TOSS_SUCCESS_URL='https://api.example.com/api/payment/success'
export TOSS_FAIL_URL='https://api.example.com/api/payment/fail'
export FAST_API_HOST='http://fastapi.internal:8000'
./gradlew bootRun --args='--spring.profiles.active=dev'
```

배포 기준:
`main` 브랜치 배포는 GitHub Actions가 AWS OIDC로 역할을 맡은 뒤, AWS Secrets Manager의 JSON 시크릿을 읽어서 `runtime.env`를 생성합니다.

필수 GitHub Repository Variables:
```bash
AWS_REGION=
AWS_DEPLOY_ROLE_ARN=
APP_RUNTIME_SECRET_ID=
APP_DEPLOY_SECRET_ID=
```

Secrets Manager 시크릿 값 형식:
앱 런타임 값은 `runtime-secret.example.json`, 배포 전용 값은 `deploy-secret.example.json` 구조를 사용하면 됩니다.

권장 AWS 권한:
```text
secretsmanager:GetSecretValue
```

OIDC로 AssumeRole 하려면 GitHub Actions용 IAM Role trust policy도 같이 설정해야 합니다.

IAM trust policy 예시:
`YOUR_GITHUB_ORG`, `YOUR_REPO`, `YOUR_AWS_ACCOUNT_ID`는 실제 값으로 바꿔야 합니다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::YOUR_AWS_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:YOUR_GITHUB_ORG/YOUR_REPO:ref:refs/heads/main"
        }
      }
    }
  ]
}
```

IAM role 생성 예시:
```bash
aws iam create-role \
  --role-name GitHubActionsTossBenefaceDeployRole \
  --assume-role-policy-document file://trust-policy.json
```

최소 권한 inline policy 예시:
`YOUR_SECRET_ARN`은 실제 Secrets Manager ARN으로 교체해야 합니다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ReadRuntimeSecret",
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "YOUR_SECRET_ARN"
    }
  ]
}
```

```bash
aws iam put-role-policy \
  --role-name GitHubActionsTossBenefaceDeployRole \
  --policy-name TossBenefaceReadRuntimeSecret \
  --policy-document file://permissions-policy.json
```

Secrets Manager 시크릿 생성 예시:
```bash
aws secretsmanager create-secret \
  --name toss-beneface/prod/runtime \
  --description "TossBeneface prod runtime configuration" \
  --secret-string file://runtime-secret.example.json
```

배포 전용 시크릿 생성 예시:
```bash
aws secretsmanager create-secret \
  --name toss-beneface/prod/deploy \
  --description "TossBeneface prod deploy credentials" \
  --secret-string file://deploy-secret.example.json
```

기존 시크릿 업데이트 예시:
```bash
aws secretsmanager update-secret \
  --secret-id toss-beneface/prod/runtime \
  --secret-string file://runtime-secret.example.json
```

```bash
aws secretsmanager update-secret \
  --secret-id toss-beneface/prod/deploy \
  --secret-string file://deploy-secret.example.json
```

현재 workflow와 연결할 값:
```bash
AWS_REGION=ap-northeast-2
AWS_DEPLOY_ROLE_ARN=arn:aws:iam::YOUR_AWS_ACCOUNT_ID:role/GitHubActionsTossBenefaceDeployRole
APP_RUNTIME_SECRET_ID=toss-beneface/prod/runtime
APP_DEPLOY_SECRET_ID=toss-beneface/prod/deploy
```

`deploy-secret.example.json`의 `EC2_PRIVATE_KEY` 값은 PEM 원문이 아니라 base64 인코딩 문자열을 넣는 전제로 workflow가 복원합니다.

참고:
브랜치 제한을 완화하고 싶으면 trust policy의 `sub`를 `repo:YOUR_GITHUB_ORG/YOUR_REPO:*` 형태로 넓힐 수 있지만, 운영 배포 역할은 `main` 브랜치 고정이 안전합니다.



### 회원가입 요청
```
POST http://localhost:8080/api/join
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "memberName": "Test User",
  "phoneNumber": "010-000-0000",
  "role": "USER",
  "gender": "FEMALE"
}
```

###
### 로그인 요청
```
POST http://localhost:8080/api/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```
###
### mysql db 설정
```
CREATE DATABASE toss_beneface;
CREATE USER 'toss_beneface_user'@'localhost' IDENTIFIED BY 'ai0310';
CREATE USER 'toss_beneface_user'@'%' IDENTIFIED BY 'ai0310';
GRANT ALL PRIVILEGES ON toss_beneface.* TO 'toss_beneface_user'@'localhost';
GRANT ALL PRIVILEGES ON toss_beneface.* TO 'toss_beneface_user'@'%';
```
