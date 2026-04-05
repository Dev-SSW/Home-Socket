import http from 'k6/http';
import { check, sleep } from 'k6';

// 루트 카테고리 조회 부하 테스트
export let options = {
  vus: 12,                                             // 동시 사용자 12명
  duration: '35s',                                 // 35초 동안
  thresholds: {
    http_req_duration: ['p(95)<300'],   // 95%의 요청이 300ms 미만
    http_req_failed: ['rate<0.03'],         // 실패율 3% 미만
  },
};

export default function () {
  // 루트 카테고리 조회 API 호출
  let response = http.get('http://localhost:8081/public/category/getRootCategory', {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 300ms': (r) => r.timings.duration < 300,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains category data': (r) => r.body.includes('id') || r.body.includes('name') || r.body.includes('depth') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 0.8초 대기
  sleep(0.8);
}
