import http from 'k6/http';
import { check, sleep } from 'k6';

// 자식 카테고리 조회 부하 테스트
export let options = {
  vus: 8,                                                       // 동시 사용자 8명
  duration: '30s',                                          // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],            // 95%의 요청이 400ms 미만
    http_req_failed: ['rate<0.02'],                  // 실패율 2% 미만
  },
};

export default function () {
  // 랜덤 부모 카테고리 ID (1-12)
  let parentId = Math.floor(Math.random() * 12) + 1;

  // 자식 카테고리 조회 API 호출
  let response = http.get(`http://localhost:8081/public/category/getChildrenCategory/${parentId}`);

  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 400ms': (r) => r.timings.duration < 400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains category data': (r) => r.body.includes('id') || r.body.includes('name') || r.body.includes('depth'),
  });

  sleep(0.5);
}