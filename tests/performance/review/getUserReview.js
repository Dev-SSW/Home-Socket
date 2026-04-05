import http from 'k6/http';
import { check, sleep } from 'k6';

// 유저 리뷰 전체 조회 부하 테스트
export let options = {
  vus: 12,                                                  // 동시 사용자 12명
  duration: '40s',                                       // 40초 동안
  thresholds: {
    http_req_duration: ['p(95)<700'],         // 95%의 요청이 700ms 미만
    http_req_failed: ['rate<0.05'],               // 실패율 5% 미만
  },
};

// 테스트용 사용자 정보 (다중 사용자)
const TEST_USERS = [
  { username: 'user1', password: 'password123' },
  { username: 'user2', password: 'password123' },
  { username: 'user3', password: 'password123' },
  { username: 'user4', password: 'password123' },
  { username: 'user5', password: 'password123' },
  { username: 'user6', password: 'password123' },
  { username: 'user7', password: 'password123' },
  { username: 'user8', password: 'password123' },
  { username: 'user9', password: 'password123' },
  { username: 'user10', password: 'password123' },
  { username: 'user11', password: 'password123' },
  { username: 'user12', password: 'password123' }
];

// JWT 토큰 저장 (각 VU별)
let jwtTokens = {};

// 로그인 함수
function login(userIndex) {
  const user = TEST_USERS[userIndex];
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: user.username,
    password: user.password
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (loginResponse.status === 200) {
    const responseBody = JSON.parse(loginResponse.body);
    if (responseBody.success && responseBody.data) {
      jwtTokens[userIndex] = responseBody.data.token;
      console.log(`로그인 성공: ${user.username}, JWT 토큰 발급됨`);
      return true;
    }
  }
  
  console.log(`로그인 실패: ${user.username}`, loginResponse.body);
  return false;
}

export default function () {
  // 현재 VU 인덱스 가져오기 (0-based) VU 1-12 → index 0-11
  const userIndex = __VU - 1;

  // JWT 토큰이 없으면 로그인 시도
  if (!jwtTokens[userIndex]) {
    if (!login(userIndex)) {
      console.log(`user${userIndex + 1}: 로그인 실패로 테스트 중단`);
      return;
    }
  }
  
  // 유저 리뷰 전체 조회 API 호출 (페이징)
  let response = http.get('http://localhost:8081/user/getUserReview/?page=0&size=20', {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtTokens[userIndex]}`, // JWT 토큰 사용
    },
  });
  
  console.log('유저 리뷰 응답 상태:', response.status);
  console.log('유저 리뷰 응답 본문:', response.body);
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 700ms': (r) => r.timings.duration < 700,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains review data': (r) => r.body.includes('comment') || r.body.includes('title') || r.body.includes('star') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 1.2초 대기
  sleep(1.2);
}
