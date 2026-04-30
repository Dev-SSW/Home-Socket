import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { recordApiResult } from '../metrics.js';

export const options = COMMON_OPTIONS;

export default function () {
  const response = http.get(`${BASE_URL}/public/category/getRootCategory`, {
    tags: {
      api: 'getRootCategory',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  recordApiResult('getRootCategory', response);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains category data': (r) =>
      r.body.includes('id') || r.body.includes('name') || r.body.includes('depth'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
