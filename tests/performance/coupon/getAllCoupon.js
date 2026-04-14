import http from 'k6/http';
import { check, sleep } from 'k6';
import { CONFIG, log } from '../config.js';

// 모든 쿠폰 조회 부하 테스트
export let options = {
  vus: 10,                                                 // 동시 사용자 10명
  duration: '35s',                                      // 35초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],        // 95%의 요청이 400ms 미만
    http_req_failed: ['rate<0.03'],              // 실패율 3% 미만
  },
};

// JWT 토큰 저장
let jwtToken = null;

// 관리자 로그인 함수
function adminLogin() {
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: 'admin',
    password: '0000'
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (loginResponse.status === 200) {
    const responseBody = JSON.parse(loginResponse.body);
    if (responseBody.success && responseBody.data) {
      jwtToken = responseBody.data.token;
      log('info', '관리자 로그인 성공, JWT 토큰 발급됨');
      return true;
    }
  }
  
  log('error', '관리자 로그인 실패: ' + loginResponse.body);
  return false;
}

export default function () {
  // JWT 토큰이 없으면 관리자 로그인 시도
  if (!jwtToken) {
    if (!adminLogin()) {
      log('error', '관리자 로그인 실패로 테스트 중단');
      return;
    }
  }
  
  // 모든 쿠폰 조회 API 호출 (페이징)
  let response = http.get('http://localhost:8081/admin/coupon/getAllCoupon?page=0&size=50', {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`,
    },
  });
  
  log('debug', '모든 쿠폰 응답 상태: ' + response.status);
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 400ms': (r) => r.timings.duration < 400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains coupon data': (r) => r.body.includes('id') || r.body.includes('couponName') || r.body.includes('discountRate') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 0.8초 대기
  sleep(0.8);
}
