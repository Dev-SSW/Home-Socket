import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { recordApiResult } from '../metrics.js';

export const options = COMMON_OPTIONS;

const CATEGORY_IDS = (__ENV.CATEGORY_IDS || '1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39')
  .split(',')
  .map((id) => Number(id.trim()))
  .filter((id) => Number.isFinite(id));

const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 20);

export default function () {
  const categoryId = CATEGORY_IDS[__ITER % CATEGORY_IDS.length];

  const response = http.get(
    `${BASE_URL}/public/item/getItemsByCategory/${categoryId}?page=${PAGE}&size=${SIZE}`,
    {
      headers: {
        'Content-Type': 'application/json',
      },
      tags: {
        api: 'getItemsByCategory',
        suite: 'read',
        migration: __ENV.MIGRATION_VERSION || 'local',
      },
    }
  );

  recordApiResult('getItemsByCategory', response);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains item data': (r) =>
      r.body.includes('id') ||
      r.body.includes('name') ||
      r.body.includes('itemPrice') ||
      r.body.includes('[]') ||
      r.body.includes('null'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
