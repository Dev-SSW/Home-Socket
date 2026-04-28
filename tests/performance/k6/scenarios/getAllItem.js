import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';

// 공통 통과 기준
export const options = COMMON_OPTIONS;

// 기본 값은 0, 20
const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 20);

export default function () {
  const response = http.get(`${BASE_URL}/public/item/getAllItem?page=${PAGE}&size=${SIZE}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: {
      api: 'getAllItem',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
