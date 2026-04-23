# Google OAuth2 설정 가이드 (OAUTH2_SETUP.md)

이 문서는 Google OAuth2 연동을 위한 상세 설정 방법을 설명합니다.

## 1. Google Cloud Console 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에 접속하여 새로운 프로젝트를 생성하거나 기존 프로젝트를 선택합니다.
2. **API 및 서비스 > 사용자 인증 정보** 메뉴로 이동합니다.
3. **사용자 인증 정보 만들기 > OAuth 클라이언트 ID**를 클릭합니다.
4. 애플리케이션 유형을 **웹 애플리케이션**으로 선택합니다.
5. 이름을 지정합니다 (예: `TossBeneface Local`).
6. **승인된 리디렉션 URI** 섹션에서 다음 URI를 추가합니다.
   - `http://localhost/login/oauth2/code/google`
7. **만들기**를 클릭하면 `클라이언트 ID`와 `클라이언트 보안 비밀`이 발급됩니다.

## 2. 로컬 환경 변수 설정

발급받은 값을 프로젝트 루트의 `.env` 파일(또는 환경 변수)에 다음과 같이 설정합니다.

```bash
GOOGLE_CLIENT_ID=여러분의_클라이언트_ID
GOOGLE_CLIENT_SECRET=여러분의_클라이언트_보안_비밀
```

이 값들은 `application-local.yml`에서 다음과 같이 사용됩니다:
```yaml
security:
  oauth2:
    client:
      registration:
        google:
          client-id: ${GOOGLE_CLIENT_ID:}
          client-secret: ${GOOGLE_CLIENT_SECRET:}
```

## 3. 테스트 방법

1. 서버를 실행합니다 (`SPRING_PROFILES_ACTIVE=local`).
2. 브라우저에서 `http://localhost/oauth2/authorization/google`로 접속합니다.
3. Google 로그인 화면이 나타나면 인증을 완료합니다.
4. 성공 시 `OAuth2SuccessHandler`에 의해 내부 JWT가 발급되고 프론트엔드로 리다이렉트됩니다.
