# 1. JDK 17 이미지를 기반으로 설정
FROM eclipse-temurin:17-jdk-jammy as builder

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 필요한 파일 복사
COPY . .

# 4. gradlew 실행 권한 추가
RUN chmod +x gradlew

# 5. Gradle 빌드 수행 (테스트 건너뛰기)
RUN ./gradlew clean build -x test --no-daemon

# 5.1 빌드 산출물 확인 (출력 결과를 로그로 확인 가능)
RUN ls -al /app/build/libs

# 7. 실행 환경 설정 단계
FROM eclipse-temurin:17-jdk-jammy

# 8. 작업 디렉토리 설정
WORKDIR /app

# 9. 빌드된 JAR 파일 복사 (와일드카드 사용)
COPY --from=builder /app/build/libs/*.jar app.jar

# 10. 애플리케이션 실행 (한국 시간대 설정)
ENTRYPOINT ["sh", "-c", "java -Duser.timezone=Asia/Seoul -jar app.jar"]
