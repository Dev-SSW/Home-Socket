import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, COMMON_OPTIONS } from '../config.js';
import { recordApiResult } from '../metrics.js';

export const options = {
  ...COMMON_OPTIONS,
  setupTimeout: __ENV.SETUP_TIMEOUT || '120s',
};

const WRITE_VUS = Number(__ENV.VUS || 8);
const DELETE_ITEMS_USER_START = Number(__ENV.DELETE_ITEMS_USER_START || 201);
const ITEMS_PER_REQUEST = Number(__ENV.DELETE_ITEMS_PER_REQUEST || 3);
const REQUIRED_CART_ITEMS_PER_USER = Number(__ENV.REQUIRED_CART_ITEMS_PER_USER || 180);

const DELETE_ITEMS_USERS = Array.from({ length: WRITE_VUS }, (_, index) => ({
  username: `user${DELETE_ITEMS_USER_START + index}`,
  password: 'password123',
}));

function loginUsers(users) {
  return users.map((user) => {
    const response = http.post(
      `${BASE_URL}/public/login`,
      JSON.stringify({ username: user.username, password: user.password }),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { api: 'loginForDeleteItems', suite: 'write-setup', migration: __ENV.MIGRATION_VERSION || 'local' },
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
    tags: { api: 'getCartForDeleteItemsSetup', suite: 'write-setup', migration: __ENV.MIGRATION_VERSION || 'local' },
  });

  if (response.status !== 200) {
    throw new Error(`${username} 장바구니 조회 실패: ${response.status} - ${response.body}`);
  }

  const body = JSON.parse(response.body);

  if (!body.success || !body.data) {
    throw new Error(`${username} 장바구니 응답 형식 오류: ${response.body}`);
  }

  const cartItemIds = (body.data.cartItemList || [])
    .map((cartItem) => cartItem.id)
    .filter((id) => id !== null && id !== undefined);

  if (cartItemIds.length < ITEMS_PER_REQUEST) {
    throw new Error(`${username} 삭제 테스트용 cartItem 부족: ${cartItemIds.length}개`);
  }

  if (cartItemIds.length < REQUIRED_CART_ITEMS_PER_USER) {
    console.log(`${username} 삭제 테스트용 cartItem 수가 권장량보다 적습니다. current=${cartItemIds.length}, recommended=${REQUIRED_CART_ITEMS_PER_USER}`);
  }

  return cartItemIds;
}

export function setup() {
  const tokens = loginUsers(DELETE_ITEMS_USERS);
  const userCartItemIds = tokens.map((token, index) => getCartItemIds(token, DELETE_ITEMS_USERS[index].username));

  return { tokens, userCartItemIds };
}

const nextIndexByVu = {};

export function deleteItems(data) {
  const vuIndex = (__VU - 1) % data.tokens.length;
  const token = data.tokens[vuIndex];
  const availableItemIds = data.userCartItemIds[vuIndex];

  if (nextIndexByVu[__VU] === undefined) {
    nextIndexByVu[__VU] = 0;
  }

  const start = nextIndexByVu[__VU];
  const end = start + ITEMS_PER_REQUEST;

  if (end > availableItemIds.length) {
    console.log(`VU${__VU}: 삭제 테스트용 cartItem 부족. remaining=${availableItemIds.length - start}`);
    sleep(Number(__ENV.SLEEP || 0.5));
    return;
  }

  const itemsToDelete = availableItemIds.slice(start, end);
  nextIndexByVu[__VU] = end;

  const response = http.del(
    `${BASE_URL}/user/cart/deleteItems`,
    JSON.stringify({ cartItemIds: itemsToDelete }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      tags: { api: 'deleteItems', suite: 'write', migration: __ENV.MIGRATION_VERSION || 'local' },
    }
  );

  recordApiResult('deleteItems', response);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is not empty': (r) => r.body.length > 0,
    'contains success data': (r) => r.body.includes('success') || r.body.includes('deleted') || r.body.includes('cartItems'),
  });

  sleep(Number(__ENV.SLEEP || 0.5));
}

export default function (data) {
  deleteItems(data);
}
