import http from 'k6/http';
import { check, sleep } from 'k6';

// 아이템 리뷰 전체 조회 부하 테스트
export let options = {
  vus: 8,                                                       // 동시 사용자 8명
  duration: '30s',                                          // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],            // 95%의 요청이 400ms 미만
    http_req_failed: ['rate<0.02'],                  // 실패율 2% 미만
  },
};

// 다중 사용자 테스트용 사용자 정보
const TEST_USERS = [
  { username: 'user1', password: 'password123' },
  { username: 'user2', password: 'password123' },
  { username: 'user3', password: 'password123' },
  { username: 'user4', password: 'password123' },
  { username: 'user5', password: 'password123' },
  { username: 'user6', password: 'password123' },
  { username: 'user7', password: 'password123' },
  { username: 'user8', password: 'password123' }
];

// setup 함수: 테스트 시작 전에 한 번만 실행됨
export function setup() {
  console.log('=== 부하테스트 시작: 다중 사용자 로그인 ===');

  const userTokens = [];

  // 각 테스트 사용자별로 로그인
  for (let i = 0; i < TEST_USERS.length; i++) {
    const user = TEST_USERS[i];

    const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
      username: user.username,
      password: user.password
    }), {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (loginResponse.status !== 200) {
      throw new Error(`사용자 ${user.username} 로그인 실패: ${loginResponse.status} - ${loginResponse.body}`);
    }

    const responseBody = JSON.parse(loginResponse.body);
    if (!responseBody.success || !responseBody.data || !responseBody.data.token) {
      throw new Error(`사용자 ${user.username} JWT 토큰 발급 실패: ${loginResponse.body}`);
    }

    userTokens.push(responseBody.data.token);
    console.log(`사용자 ${user.username} 로그인 성공`);
  }

  console.log(`총 ${userTokens.length}명의 사용자 로그인 완료`);
  return {
    userTokens: userTokens
  };
}

// 메인 테스트 함수: 각 VU에서 반복 실행됨
export default function (data) {
  // 각 VU는 다른 사용자 토큰 사용 (VU ID 기반)
  const vuIndex = __VU - 1; // VU는 1부터 시작, 배열은 0부터
  const userToken = data.userTokens[vuIndex % data.userTokens.length];
  const currentUser = TEST_USERS[vuIndex % TEST_USERS.length];

  // 랜덤 아이템 ID (1-1000)
  let itemId = Math.floor(Math.random() * 1000) + 1;
  
  // 아이템 리뷰 전체 조회 API 호출
  let response = http.get(`http://localhost:8081/user/item/${itemId}/review/getItemReview/?page=0&size=20`, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`,
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 400ms': (r) => r.timings.duration <400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains review data': (r) => r.body.includes('comment') || r.body.includes('title') || r.body.includes('star') || r.body.includes('[]') || r.body.includes('null'),
  });

  sleep(0.5);
}
