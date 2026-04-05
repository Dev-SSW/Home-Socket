import http from 'k6/http';
import { check, sleep } from 'k6';

// 특정 상품 조회 부하 테스트
export let options = {
  vus: 20,                                             // 동시 사용자 20명
  duration: '60s',                                  // 60초 동안
  thresholds: {
    http_req_duration: ['p(95)<300'],    // 95%의 요청이 300ms 미만
    http_req_failed: ['rate<0.05'],          // 실패율 5% 미만
  },
};

export default function () {
  // 랜덤 상품 ID (1-1000)
  let itemId = Math.floor(Math.random() * 1000) + 1;
  
  let response = http.get(`http://localhost:8081/public/item/getItem/${itemId}`, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 300ms': (r) => r.timings.duration < 300,
    'response body is not empty': (r) => r.body.length > 0,
  });
  
  // 0.5초 대기
  sleep(0.5);
}
