import http from 'k6/http';
import { check, sleep } from 'k6';      // 응답 검증과 요청 사이 대기
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { loginAllUsers, tokenForVu } from '../auth.js';

// 공통 통과 기준
export const options = COMMON_OPTIONS;

export function setup() {
  return { tokens: loginAllUsers() };
}

export default function (data) {
  const token = tokenForVu(data.tokens);

  const res = http.get(`${BASE_URL}/user/address/getAllAddress`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    tags: {
      api: 'getAllAddress',
      type: 'read',
    },
  });

  check(res, {
    'status is 200': r => r.status === 200,
    'body is not empty': r => r.body.length > 0,
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}