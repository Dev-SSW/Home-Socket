import http from 'k6/http';
import { check, sleep } from 'k6';
import { CONFIG, log } from '../config.js';

// 전체 상품 조회 부하 테스트
export let options = {
  vus: 10,                                              // 동시 사용자 10명
  duration: '30s',                                  // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<500'],    // 95%의 요청이 500ms 미만
    http_req_failed: ['rate<0.1'],            // 실패율 10% 미만
  },
};

export default function () {
  // 전체 상품 조회 API 테스트 (실제 엔드포인트)
  let response = http.get('http://localhost:8081/public/item/getAllItem?page=0&size=20', {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'response body is not empty': (r) => r.body.length > 0,
  });
  
  // 1초 대기
  sleep(1);
}
