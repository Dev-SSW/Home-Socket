import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';

// 공통 통과 기준
export const options = COMMON_OPTIONS;

// 기본 값 설정 (지정하여 여러 번 실행해도 같은 환경이되도록 설정)
const PARENT_CATEGORY_IDS = (__ENV.PARENT_CATEGORY_IDS || '1,2,3,4,5,6,7,8,9,10,11,12')
  // 쉼표 기준 분리
  .split(',')
  // Number()로 문자열을 숫자로 바꾸며 trim으로 공백 제거
  .map((id) => Number(id.trim()))
  // isFinite()로 숫자로 변환된 값만 남기기 (숫자가 아니라면 제거됨)
  .filter((id) => Number.isFinite(id));

export default function () {
  // __ITER는 K6에서 제공하는 현재 __VU가 몇 번째 반복을 실행중인지 나타냄
  // 한 __VU가 12번 이상 실행하면 모든 카테고리를 돌고, 다시 0번째부터 실행함
  const parentId = PARENT_CATEGORY_IDS[__ITER % PARENT_CATEGORY_IDS.length];

  const response = http.get(`${BASE_URL}/public/category/getChildrenCategory/${parentId}`, {
    tags: {
      api: 'getChildrenCategory',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains category data': (r) =>
      r.body.includes('id') || r.body.includes('name') || r.body.includes('depth'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
