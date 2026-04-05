import http from 'k6/http';
import { check, sleep } from 'k6';

// 아이템 리뷰 전체 조회 부하 테스트
export let options = {
  vus: 15,                                                  // 동시 사용자 15명
  duration: '40s',                                      // 40초 동안
  thresholds: {
    http_req_duration: ['p(95)<700'],        // 95%의 요청이 700ms 미만
    http_req_failed: ['rate<0.05'],             // 실패율 5% 미만
  },
};

// 테스트용 사용자 정보
const TEST_USER = {
  username: 'user1',
  password: 'password123'
};

// JWT 토큰 저장
let jwtToken = null;

// 로그인 함수
function login() {
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: TEST_USER.username,
    password: TEST_USER.password
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (loginResponse.status === 200) {
    const responseBody = JSON.parse(loginResponse.body);
    if (responseBody.success && responseBody.data) {
      jwtToken = responseBody.data.token;  // "token" 필드로 수정
      console.log('로그인 성공, JWT 토큰 발급됨');
      return true;
    }
  }
  
  console.log('로그인 실패:', loginResponse.body);
  return false;
}

export default function () {
  // JWT 토큰이 없으면 로그인
  if (!jwtToken) {
    if (!login()) {
      throw new Error('로그인 실패');
    }
  }
  
  // 랜덤 아이템 ID (1-1000)
  let itemId = Math.floor(Math.random() * 1000) + 1;
  
  // 아이템 리뷰 전체 조회 API 호출
  let response = http.get(`http://localhost:8081/user/item/${itemId}/review/getItemReview/?page=0&size=20`, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`, // JWT 토큰 사용
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 700ms': (r) => r.timings.duration < 700,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains review data': (r) => r.body.includes('comment') || r.body.includes('title') || r.body.includes('star') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 1초 대기
  sleep(1);
}
