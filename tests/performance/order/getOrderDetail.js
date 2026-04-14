import http from 'k6/http';
import { check, sleep } from 'k6';
import { CONFIG, log } from '../config.js';

// 주문 상세 조회 부하 테스트
export let options = {
  vus: 10,                                              // 동시 사용자 10명
  duration: '40s',                                  // 40초 동안
  thresholds: {
    http_req_duration: ['p(95)<600'],    // 95%의 요청이 600ms 미만
    http_req_failed: ['rate<0.05'],          // 실패율 5% 미만
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
      jwtToken = responseBody.data.token;
      log('info', '로그인 성공, JWT 토큰 발급됨');
      return true;
    }
  }
  
  log('error', '로그인 실패: ' + loginResponse.body);
  return false;
}

export default function () {
  // JWT 토큰이 없으면 로그인
  if (!jwtToken) {
    if (!login()) {
      throw new Error('로그인 실패');
    }
  }
  
  // 랜덤 주문 ID (1-1000)
  let orderId = Math.floor(Math.random() * 1000) + 1;
  
  // 주문 상세 조회 API 호출
  let response = http.get(`http://localhost:8081/user/order/${orderId}/getOrderDetail`, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`, // JWT 토큰 사용
    },
  });
  
  // 성능 테스트 시에는 로그 비활성화
  log('debug', '주문 상세 응답 상태: ' + response.status);
  log('debug', '주문 상세 응답 본문: ' + response.body);
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'status is 404': (r) => r.status === 404,
    'response time < 600ms': (r) => r.timings.duration < 600,
    'response body is not empty': (r) => r.body.length > 0,
    '200 response contains order data': (r) => r.status === 200 && r.body.includes('"success":true'),
    '404 response contains error data': (r) => r.status === 404 && r.body.includes('ORDER_NOT_FOUND'),
  });
  
  // 상태 코드별 카운트 (성능 테스트 시에는 최소 로그)
  if (response.status === 200) {
    log('info', `200 성공 - orderId: ${orderId}`);
  } else if (response.status === 404) {
    log('info', `404 주문 없음 - orderId: ${orderId}`);
  }
  
  // 1초 대기
  sleep(1);
}
