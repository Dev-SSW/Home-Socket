import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { recordApiResult } from '../metrics.js';

export const options = {
  // 공통 옵션을 펼쳐서 가져옴
  /**
  {
    vus: 8,
    duration: '30s',
    thresholds: { ... },
    setupTimeout: '120s'
  }
  */
  ...COMMON_OPTIONS,
  // setup 함수 실행 가능 시간 제한
  setupTimeout: __ENV.SETUP_TIMEOUT || '120s',
};

const WRITE_VUS = Number(__ENV.VUS || 8);                                                                                                   // 유저 수
const CREATE_ORDER_USER_START = Number(__ENV.CREATE_ORDER_USER_START || 101);                           // 주문 테스트 시작 유저 번호 (101부터 8명이므로 id : 101~108)
const ITEMS_PER_ORDER = Number(__ENV.ITEMS_PER_ORDER || 3);                                                                // 주문 한 번에 사용할 CartItem 개수
const REQUIRED_CART_ITEMS_PER_USER = Number(__ENV.REQUIRED_CART_ITEMS_PER_USER || 180);          // 유저당 권장 CartItem 개수
const ADDRESS_ID = Number(__ENV.ADDRESS_ID || 1);                                                                                   // 주문 요청에 넣을 주소 ID (고정)

// 테스트 유저 배열 생성
const CREATE_ORDER_USERS = [];
for (let index = 0; index < WRITE_VUS; index++) {
  CREATE_ORDER_USERS.push({
    username: `user${CREATE_ORDER_USER_START + index}`,
    password: 'password123',
  });
}

function loginUsers(users) {
  return users.map((user) => {
    const response = http.post(
      `${BASE_URL}/public/login`,
      JSON.stringify({ username: user.username, password: user.password }),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { api: 'loginForCreateOrder', suite: 'write-setup', migration: __ENV.MIGRATION_VERSION || 'local' },
      }
    );

    if (response.status !== 200) {
      throw new Error(`${user.username} 로그인 실패: ${response.status} - ${response.body}`);
    }

    const body = JSON.parse(response.body);
    const token = body?.data?.token;

    if (!body.success || !token) {
      throw new Error(`${user.username} JWT 토큰 발급 실패: ${response.body}`);
    }

    return token;
  });
}

function getCartItemIds(token, username) {
  const response = http.get(`${BASE_URL}/user/cart/getCart`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    tags: { api: 'getCartForCreateOrderSetup', suite: 'write-setup', migration: __ENV.MIGRATION_VERSION || 'local' },
  });

  if (response.status !== 200) {
    throw new Error(`${username} 장바구니 조회 실패: ${response.status} - ${response.body}`);
  }

  const body = JSON.parse(response.body);

  if (!body.success || !body.data) {
    throw new Error(`${username} 장바구니 응답 형식 오류: ${response.body}`);
  }

  // 유저의 장바구니에서 cartItemId 리스트 가져오기 (리스트가 없으면 빈 배열( [] ) 사용 )
  const cartItemIds = (body.data.cartItemList || [])
    .map((cartItem) => cartItem.id)
    .filter((id) => id !== null && id !== undefined);

  if (cartItemIds.length < ITEMS_PER_ORDER) {
    throw new Error(`${username} 주문 테스트용 cartItem 부족: ${cartItemIds.length}개`);
  }

  if (cartItemIds.length < REQUIRED_CART_ITEMS_PER_USER) {
    console.log(`${username} 주문 테스트용 cartItem 수가 권장량보다 적습니다.`);
  }

  return cartItemIds;
}

export function setup() {
  // CREATE_ORDER_USERS 유저 배열로 로그인 후 토큰 받기
  const tokens = loginUsers(CREATE_ORDER_USERS);
  // 토큰 별로
  const userCartItemIds = [];

  for (let index = 0; index < tokens.length; index++) {
    // 유저 101~108까지 각자의 cartItem을 쓰도록 만듦
    const token = tokens[index];
    const username = CREATE_ORDER_USERS[index].username;
    // 해당 유저의 cartItemsIds 가져오기
    const cartItemIds = getCartItemIds(token, username);
    userCartItemIds.push(cartItemIds);
  }
  return { tokens, userCartItemIds };
}

//__VU 별 다음에 사용할 CartItem 위치 기억 객체
const nextIndexByVu = {};

export function createOrder(data) {
  // __VU가 많아도 토큰을 순환하게 사용하도록 함
  const vuIndex = (__VU - 1) % data.tokens.length;
  const token = data.tokens[vuIndex];
  const availableItemIds = data.userCartItemIds[vuIndex];

  if (nextIndexByVu[__VU] === undefined) {
    nextIndexByVu[__VU] = 0;
  }

  const start = nextIndexByVu[__VU];
  const end = start + ITEMS_PER_ORDER;

  if (end > availableItemIds.length) {
    console.log(`VU${__VU}: 주문 테스트용 cartItem 부족. remaining=${availableItemIds.length - start}`);
    sleep(Number(__ENV.SLEEP || 0.5));
    return;
  }

  const itemsToOrder = availableItemIds.slice(start, end);
  nextIndexByVu[__VU] = end;

  const response = http.post(
    `${BASE_URL}/user/order/createCartOrder`,
    JSON.stringify({ cartItemIds: itemsToOrder, addressId: ADDRESS_ID, couponPublishId: null }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      tags: { api: 'createOrder', suite: 'write', migration: __ENV.MIGRATION_VERSION || 'local' },
    }
  );

  recordApiResult('createOrder', response);

  check(response, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'body is not empty': (r) => r.body.length > 0,
    'contains order data': (r) => r.body.includes('orderId') || r.body.includes('order') || r.body.includes('success'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}

export default function (data) {
  createOrder(data);
}
