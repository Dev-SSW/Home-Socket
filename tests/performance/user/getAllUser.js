import http from 'k6/http';
import { check, sleep } from 'k6';
import { CONFIG, log } from '../config.js';

// 전체 유저 정보 조회 부하 테스트 (관리자용)
export let options = {
  vus: 5,                                                   // 동시 사용자 5명 (관리자 기능)
  duration: '35s',                                      // 35초 동안
  thresholds: {
    http_req_duration: ['p(95)<1000'],      // 95%의 요청이 1000ms 미만
    http_req_failed: ['rate<0.05'],             // 실패율 5% 미만
  },
};

// 관리자 계정 정보
const ADMIN_USER = {
  username: 'admin',
  password: '0000'
};

// JWT 토큰 저장
let jwtToken = null;

// 로그인 함수
function login() {
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: ADMIN_USER.username,
    password: ADMIN_USER.password
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
  // JWT 토큰이 없으면 로그인
  if (!jwtToken) {
    if (!login()) {
      throw new Error('관리자 로그인 실패');
    }
  }
  
  // 전체 유저 정보 조회 API 호출 (페이징)
  let response = http.get('http://localhost:8081/admin/getAllUser?page=0&size=50', {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`, // JWT 토큰 사용
    },
  });
  
  log('debug', '전체 유저 응답 상태: ' + response.status);
  log('debug', '전체 유저 응답 본문: ' + response.body);
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains user data': (r) => r.body.includes('username') || r.body.includes('email') || r.body.includes('users') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 2.5초 대기
  sleep(2.5);
}
