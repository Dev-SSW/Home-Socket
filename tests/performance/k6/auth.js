import http from 'k6/http';
import { BASE_URL, TEST_USERS } from './config.js';
// config.js에서 BASE_URL, TEST_USERS를 가져옴

// TEST_USERS에 있는 유저들을 모두 로그인하여 토큰을 발급
export function loginAllUsers() {
  const userTokens = [];

  // TEST_USERS에 들어있는 유저를 하나씩 꺼내기
  for (const user of TEST_USERS) {
    // 로그인 요청
    const res = http.post(
      `${BASE_URL}/public/login`,
      // 객체를 JSON으로 변환하여 전송
      JSON.stringify({
        username: user.username,
        password: user.password,
      }),
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );

    if (res.status !== 200) {
      throw new Error(`${user.username} 로그인 실패: ${res.status} - ${res.body}`);
    }

    // JSON을 js 객체로 변환
    const body = JSON.parse(res.body);

    // body.success가 true이고 body.data.token이 존재하는지 확인
    if (!body.success || !body.data?.token) {
      throw new Error(`${user.username} JWT 토큰 발급 실패: ${res.body}`);
    }
    userTokens.push(body.data.token);
  }

  return userTokens;
}

// 현재 VU에게 사용할 토큰 하나 골라주기 (__VU : 현재 가상 사용자 번호)
export function tokenForVu(tokens) {
  // 배열 인덱스는 0부터 시작하고, VU1 -> __VU = 1 이므로 -1 처리를 해준다
  // tokens.length는 VU 수가 토큰 수보다 많아도 (VU 설정은 10인데 token은 8개) 순환해서 사용하도록 하기 위함
  return tokens[(__VU - 1) % tokens.length];
}