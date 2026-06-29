# Home-Socket CI/CD 정리

Home-Socket은 GitHub Actions를 이용해 main 브랜치 push 시 자동 배포되도록 구성했습니다.

## CI/CD 흐름

![Home-Socket CI/CD](images/home-socket-ci-cd.png)

```text
main push
→ GitHub Actions
→ Gradle bootJar
→ JAR artifact 생성
→ SSH/SCP로 OCI App Server에 업로드
→ 기존 JAR 백업
→ Docker Compose로 app 컨테이너 재생성
→ Health check
→ 실패 시 이전 JAR로 rollback
```

## Build 단계

```bash
./gradlew clean bootJar -x test --no-daemon
```

생성되는 JAR:

```text
build/libs/practice-0.0.1-SNAPSHOT.jar
```

## Deploy 단계

GitHub Actions에서 JAR 파일을 OCI App Server로 전송합니다.

```text
practice-0.0.1-SNAPSHOT.jar
→ /home/ubuntu/home-socket/practice-0.0.1-SNAPSHOT.jar.new
```

서버에서는 기존 JAR를 백업한 뒤 새 JAR로 교체합니다.

```bash
if [ -f "$JAR_NAME" ]; then
  cp "$JAR_NAME" "${JAR_NAME}.bak"
fi

mv "${JAR_NAME}.new" "$JAR_NAME"
```

## Docker Compose 재배포

```bash
docker compose -f docker-compose.prod.yml up -d --build --force-recreate app
```

이 명령은 app 서비스만 대상으로 합니다. Redis와 Kafka 컨테이너는 재생성하지 않고 계속 실행 상태를 유지합니다.

## Health Check

배포 후 `/v3/api-docs` 엔드포인트가 정상 응답하는지 확인합니다.

```bash
curl --max-time 5 -f -s -o /dev/null http://127.0.0.1:8081/v3/api-docs
```

## Rollback

Health check가 실패하면 백업된 JAR로 되돌린 뒤 app 컨테이너를 다시 재생성합니다.

```bash
cp "${JAR_NAME}.bak" "$JAR_NAME"
docker compose -f docker-compose.prod.yml up -d --build --force-recreate app
```

## GitHub Secrets

배포에 필요한 정보는 GitHub Secrets로 관리합니다.

| Secret | 설명 |
|---|---|
| `OCI_HOST` | 배포 대상 서버 주소 |
| `OCI_USER` | SSH 접속 사용자 |
| `OCI_SSH_KEY` | 배포용 SSH private key |
| `APP_DIR` | 서버의 애플리케이션 디렉터리 |

## 현재 방식의 특징

현재 방식은 JAR를 서버에 업로드한 뒤, 서버에서 Docker image를 build하는 방식입니다.

| 항목 | 내용 |
|---|---|
| 배포 단위 | JAR |
| 이미지 빌드 위치 | 운영 서버 |
| 컨테이너 재생성 | Docker Compose |
| 장점 | 단순하고 registry가 필요 없음 |
| 단점 | 서버에서 build를 수행하므로 운영 서버에 부담이 있음 |

향후 Docker image registry 기반 배포로 전환하면 CI에서 image build/push를 수행하고, 서버는 image pull/restart만 담당하도록 개선할 수 있습니다.
