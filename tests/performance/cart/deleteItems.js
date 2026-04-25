import http from 'k6/http';
import { check, sleep } from 'k6';

// 장바구니 아이템 삭제 부하 테스트
export let options = {
  setupTimeout: '300s',                               // setup 시간 제한 5분으로 증가 (360개 아이템 생성)
  vus: 8,                                                      // 동시 사용자 8명
  duration: '30s',                                         // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<400'],            // 95%의 요청이 400ms 미만 (순수 삭제 API)
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

// setup 함수: 테스트 시작 전에 한 번만 실행됨
export function setup() {
  console.log('=== 부하테스트 시작: 다중 사용자 로그인 및 장바구니 아이템 준비 ===');

  // 일반 사용자 로그인
  const userTokens = [];
  const userCartItemIds = [];

  for (let i = 0; i < TEST_USERS.length; i++) {
    const user = TEST_USERS[i];

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

    userTokens.push(responseBody.data.token);
    console.log(`사용자 ${user.username} 로그인 성공`);

    // 장바구니 아이템 준비
    const cartItemIds = [];

    // 30초 동안 실행에 필요한 정확한 아이템 수 계산 (안전성을 위해 2배 증가)
    const executionTime = 30;           // 30초
    const waitTime = 0.5;                   // 0.5초 대기
    const itemsPerRequest = 3;          // 요청당 3개 아이템
    const exactItemsNeeded = Math.ceil(executionTime / waitTime) * itemsPerRequest * 2;

    console.log(`사용자 ${user.username}: ${exactItemsNeeded}개 장바구니 아이템 준비 시작`);

    // 기존 아이템(ID=1-180)을 장바구니에 추가하여 cartItemId 확보 (deleteItems 전용 범위)
    for (let j = 1; j <= exactItemsNeeded; j++) {
      const addItemResponse = http.post('http://localhost:8081/user/cart/addItem', JSON.stringify({
        itemId: j,  // 1, 2, 3, ..., 180 (deleteItems 전용 범위)
        quantity: 1
      }), {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userTokens[i]}`,
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

// 전역 변수로 삭제된 아이템 추적 (VU별로 관리)
const deletedItemsByVU = {};

// 메인 테스트 함수: 각 VU에서 반복 실행됨
export default function (data) {
  // 각 VU는 다른 사용자 토큰 사용
  const vuIndex = __VU - 1; // VU는 1부터 시작, 배열은 0부터
  const userToken = data.userTokens[vuIndex % data.userTokens.length];
  const currentUser = TEST_USERS[vuIndex % TEST_USERS.length];
  const availableItemIds = data.userCartItemIds[vuIndex % data.userCartItemIds.length];

  // VU별 삭제된 아이템 추적 초기화
  if (!deletedItemsByVU[__VU]) {
    deletedItemsByVU[__VU] = [];
  }
  const deletedItems = deletedItemsByVU[__VU];

  // 아직 삭제되지 않은 아이템 필터링
  const remainingItems = availableItemIds.filter(id => !deletedItems.includes(id));

  // 삭제할 아이템이 없는 경우
  if (remainingItems.length === 0) {
    console.log(`VU${__VU}(${currentUser.username}): 모든 장바구니 아이템 삭제됨, 테스트 건너뜀`);
    return;
  }

  // 3개 아이템 삭제
  if (remainingItems.length < 3) {
    console.log(`VU${__VU}(${currentUser.username}): 아이템 부족 (${remainingItems.length}개), 테스트 건너뜀`);
    return;
  }

  const itemsToDelete = remainingItems.slice(0, 3);
  
  // 삭제된 아이템 기록
  deletedItems.push(...itemsToDelete);

  // 장바구니 아이템 삭제 요청 데이터
  const deleteRequest = {
    cartItemIds: itemsToDelete
  };

  // 장바구니 아이템 삭제 API 호출
  let response = http.del('http://localhost:8081/user/cart/deleteItems', JSON.stringify(deleteRequest), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`,
    },
  });

  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 400ms': (r) => r.timings.duration < 400,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains success data': (r) => r.body.includes('success') || r.body.includes('deleted') || r.body.includes('cartItems'),
  }) || console.log(`VU${__VU}(${currentUser.username}) 3개 아이템 삭제 실패: ${response.status} - ${response.body}`);
  
  // 0.5초 대기
  sleep(0.5);
}
