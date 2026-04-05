import http from 'k6/http';
import { check, sleep } from 'k6';

// 주소 전체 조회 부하 테스트
export let options = {
  vus: 8,                                                       // 동시 사용자 8명
  duration: '30s',                                          // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],            // 95%의 요청이 400ms 미만
    http_req_failed: ['rate<0.02'],                  // 실패율 2% 미만
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
  
  // 주소 전체 조회 API 호출
  let response = http.get('http://localhost:8081/user/address/getAllAddress', {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`, // JWT 토큰 사용
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 400ms': (r) => r.timings.duration < 400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains address data': (r) => r.body.includes('street') || r.body.includes('zipcode') || r.body.includes('detailStreet') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 0.6초 대기
  sleep(0.6);
}
