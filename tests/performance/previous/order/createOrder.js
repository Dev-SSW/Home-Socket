import http from 'k6/http';
import { check, sleep } from 'k6';

// 장바구니로 주문 생성 부하 테스트
export let options = {
  setupTimeout: '300s',                                 // setup 시간 제한 5분으로 증가 (360개 아이템 생성)
  vus: 8,                                                       // 동시 사용자 8명
  duration: '30s',                                          // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],            // 95%의 요청이 400ms 미만
    http_req_failed: ['rate<0.02'],                  // 실패율 2% 미만
  },
};

// 다중 사용자 테스트용 사용자 정보
const TEST_USERS = [
  { username: 'user1', password: 'password123' },
  { username: 'user2', password: 'password123' },
  { username: 'user3', password: 'password123' },
  { username: 'user4', password: 'password123' },
  { username: 'user5', password: 'password123' },
  { username: 'user6', password: 'password123' },
  { username: 'user7', password: 'password123' },
  { username: 'user8', password: 'password123' }
];

// 전역 변수로 주문된 아이템 추적 (VU별로 관리)
const orderedItemsByVU = {};

// setup 함수: 테스트 시작 전에 한 번만 실행됨
export function setup() {
  console.log('=== 주문 생성 부하테스트 시작: 다중 사용자 로그인 및 장바구니 준비 ===');

  const userTokens = [];
  const userCartItemIds = [];

  // 각 테스트 사용자별로 로그인 및 장바구니 아이템 준비
  for (let i = 0; i < TEST_USERS.length; i++) {
    const user = TEST_USERS[i];

    // 1. 로그인
    const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
      username: user.username,
      password: user.password
    }), {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (loginResponse.status !== 200) {
      throw new Error(`사용자 ${user.username} 로그인 실패: ${loginResponse.status} - ${loginResponse.body}`);
    }

    const responseBody = JSON.parse(loginResponse.body);
    if (!responseBody.success || !responseBody.data || !responseBody.data.token) {
      throw new Error(`사용자 ${user.username} JWT 토큰 발급 실패: ${loginResponse.body}`);
    }

    const userToken = responseBody.data.token;
    userTokens.push(userToken);

    // 2. 장바구니 아이템 준비 (주문에 사용할 아이템)
    const cartItemIds = [];
    const itemsPerOrder = 3; // 주문당 3개 아이템
    const ordersPerUser = 60; // 30초 동안 60번 주문 가능 (0.5초 간격)
    const exactItemsNeeded = ordersPerUser * itemsPerOrder * 2;

    // 기존 아이템(ID=181-360)을 장바구니에 추가하여 cartItemId 확보 (createOrder 전용 범위)
    for (let j = 1; j <= exactItemsNeeded; j++) {
      const addItemResponse = http.post('http://localhost:8081/user/cart/addItem', JSON.stringify({
        itemId: j + 180,  // 181, 182, 183, ..., 360 (createOrder 전용 범위)
        quantity: 1
      }), {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userToken}`,
        },
      });

      if (addItemResponse.status === 200) {
        const addItemData = JSON.parse(addItemResponse.body);
        if (addItemData.success && addItemData.data && addItemData.data.cartItemList) {
          // 가장 최근에 추가된 cartItemId를 사용
          const latestItem = addItemData.data.cartItemList[addItemData.data.cartItemList.length - 1];
          if (latestItem) {
            cartItemIds.push(latestItem.id);
          }
        }
      }

      // 아이템 추가 간 대기 증가 (DB 동기화를 위해)
      sleep(0.1);
    }

    userCartItemIds.push(cartItemIds);
    console.log(`사용자 ${user.username}: ${cartItemIds.length}개 장바구니 아이템 준비 완료`);
  }

  console.log(`총 ${userTokens.length}명의 사용자 로그인 및 장바구니 아이템 준비 완료`);
  return {
    userTokens: userTokens,
    userCartItemIds: userCartItemIds
  };
}

// 메인 테스트 함수: 각 VU에서 반복 실행됨
export default function (data) {
  // 각 VU는 다른 사용자 토큰 사용
  const vuIndex = __VU - 1; // VU는 1부터 시작, 배열은 0부터
  const userToken = data.userTokens[vuIndex % data.userTokens.length];
  const currentUser = TEST_USERS[vuIndex % TEST_USERS.length];
  const availableItemIds = data.userCartItemIds[vuIndex % data.userCartItemIds.length];

  // VU별 주문된 아이템 추적 초기화
  if (!orderedItemsByVU[__VU]) {
    orderedItemsByVU[__VU] = [];
  }
  const orderedItems = orderedItemsByVU[__VU];

  // 아직 주문되지 않은 아이템 필터링
  const remainingItems = availableItemIds.filter(id => !orderedItems.includes(id));

  // 주문할 아이템이 없는 경우
  if (remainingItems.length === 0) {
    console.log(`VU${__VU}(${currentUser.username}): 모든 장바구니 아이템 주문 완료, 테스트 건너뜀`);
    return;
  }

  // 3개 아이템 주문
  if (remainingItems.length < 3) {
    console.log(`VU${__VU}(${currentUser.username}): 아이템 부족 (${remainingItems.length}개), 테스트 건너뜀`);
    return;
  }

  const itemsToOrder = remainingItems.slice(0, 3);

  // 주문된 아이템 기록
  orderedItems.push(...itemsToOrder);

  // 주문 생성 요청 데이터
  const orderRequest = {
    cartItemIds: itemsToOrder,  // 3개 아이템 주문
    addressId: 1,              // 배송지 ID
    couponPublishId: null       // 쿠폰 사용하지 않음
  };

  // 주문 생성 API 호출
  let response = http.post('http://localhost:8081/user/order/createCartOrder', JSON.stringify(orderRequest), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`,
    },
  });

  // 응답 검증
  check(response, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response time < 400ms': (r) => r.timings.duration < 400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains order data': (r) => r.body.includes('orderId') || r.body.includes('order') || r.body.includes('success'),
  }) || console.log(`VU${__VU}(${currentUser.username}) 주문 생성 실패: ${response.status} - ${response.body}`);

  // 0.5초 대기
  sleep(0.5);
}
