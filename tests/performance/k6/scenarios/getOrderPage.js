import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { loginAllUsers, tokenForVu } from '../auth.js';

export const options = COMMON_OPTIONS;

const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 20);

export function setup() {
  return {
    tokens: loginAllUsers(),
  };
}

export default function (data) {
  const token = tokenForVu(data.tokens);

  const response = http.get(`${BASE_URL}/user/order/getOrderPage?page=${PAGE}&size=${SIZE}`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    tags: {
      api: 'getOrderPage',
      suite: 'read',
      migration: __ENV.MIGRATION_VERSION || 'local',
    },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains order data': (r) =>
      r.body.includes('orderId') || r.body.includes('orders') || r.body.includes('[]') || r.body.includes('null'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}
