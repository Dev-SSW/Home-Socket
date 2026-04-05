import http from 'k6/http';
import { check, sleep } from 'k6';

// 자식 카테고리 조회 부하 테스트
export let options = {
  vus: 15,                                                  // 동시 사용자 15명
  duration: '40s',                                      // 40초 동안
  thresholds: {
    http_req_duration: ['p(95)<350'],        // 95%의 요청이 350ms 미만
    http_req_failed: ['rate<0.04'],              // 실패율 4% 미만
  },
};

export default function () {
  // 랜덤 부모 카테고리 ID (1-27)
  let parentId = Math.floor(Math.random() * 27) + 1;
  
  // 자식 카테고리 조회 API 호출
  let response = http.get(`http://localhost:8081/public/category/getChildrenCategory/${parentId}`, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 350ms': (r) => r.timings.duration < 350,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains category data': (r) => r.body.includes('id') || r.body.includes('name') || r.body.includes('depth') || r.body.includes('[]') || r.body.includes('null'),
  });
  
  // 1초 대기
  sleep(1);
}
