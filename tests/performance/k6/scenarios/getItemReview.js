import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { loginAllUsers, tokenForVu } from '../auth.js';
import { recordApiResult } from '../metrics.js';

export const options = COMMON_OPTIONS;

const ITEM_IDS = (__ENV.ITEM_IDS || '1,2,3,4,5,6,7,8,9,10')
  .split(',')
  .map((id) => Number(id.trim()))
  .filter((id) => Number.isFinite(id));

const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 20);

export function setup() {
  return {
    tokens: loginAllUsers(),
  };
}

export default function (data) {
  const token = tokenForVu(data.tokens);
  const itemId = ITEM_IDS[__ITER % ITEM_IDS.length];

  const response = http.get(`${BASE_URL}/user/item/${itemId}/review/getItemReview/?page=${PAGE}&size=${SIZE}`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    tags: {
      api: 'getItemReview',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  recordApiResult('getItemReview', response);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains review data': (r) =>
      r.body.includes('comment') ||
      r.body.includes('title') ||
      r.body.includes('star') ||
      r.body.includes('[]') ||
      r.body.includes('null'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
