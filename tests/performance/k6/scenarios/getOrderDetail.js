import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { loginAllUsers, tokenForVu } from '../auth.js';

export const options = COMMON_OPTIONS;

const ORDER_IDS = (__ENV.ORDER_IDS || '2,3,4,5,6,7,8,9,10,11')
  .split(',')
  .map((id) => Number(id.trim()))
  .filter((id) => Number.isFinite(id));

export function setup() {
  return {
    tokens: loginAllUsers(),
  };
}

export default function (data) {
  const token = tokenForVu(data.tokens);
  const orderId = ORDER_IDS[__ITER % ORDER_IDS.length];

  const response = http.get(`${BASE_URL}/user/order/${orderId}/getOrderDetail`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    tags: {
      api: 'getOrderDetail',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains order data': (r) => r.status === 200 && r.body.includes('"success":true'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
