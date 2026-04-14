import http from 'k6/http';
import { check, sleep } from 'k6';
import { CONFIG, log } from '../config.js';

// 카테고리별 상품 조회 부하 테스트
export let options = {
  vus: 18,                                                      // 동시 사용자 18명
  duration: '50s',                                          // 50초 동안
  thresholds: {
    http_req_duration: ['p(95)<600'],             // 95%의 요청이 600ms 미만
    http_req_failed: ['rate<0.05'],                   // 실패율 5% 미만
  },
};

export default function () {
  // 랜덤 카테고리 ID (1-27)
  let categoryId = Math.floor(Math.random() * 27) + 1;
  
  // 카테고리별 상품 조회 API 호출 (페이징)
  let response = http.get(`http://localhost:8081/public/item/getItemsByCategory/${categoryId}?page=0&size=20`, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  log('debug', '카테고리 상품 응답 상태: ' + response.status);
  log('debug', '카테고리 상품 응답 본문: ' + response.body);
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 600ms': (r) => r.timings.duration < 600,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains item data': (r) => r.body.includes('id') || r.body.includes('name') || r.body.includes('itemPrice') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 1초 대기
  sleep(1);
}
