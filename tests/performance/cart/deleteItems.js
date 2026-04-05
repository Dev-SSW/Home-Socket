import http from 'k6/http';
import { check, sleep } from 'k6';

// 장바구니 아이템 삭제 부하 테스트 (순수 삭제 성능)
export let options = {
  vus: 6,                                                      // 동시 사용자 6명
  duration: '30s',                                         // 30초 동안
  thresholds: {
    http_req_duration: ['p(95)<500'],           // 95%의 요청이 500ms 미만 (삭제만 측정)
    http_req_failed: ['rate<0.05'],                 // 실패율 5% 미만
  },
};

// 테스트용 사용자 정보 (다중 사용자)
const TEST_USERS = [
  { username: 'user1', password: 'password123' },
  { username: 'user2', password: 'password123' },
  { username: 'user3', password: 'password123' },
  { username: 'user4', password: 'password123' },
  { username: 'user5', password: 'password123' },
  { username: 'user6', password: 'password123' }
];

// JWT 토큰 저장 (각 VU별)
let jwtTokens = {};

// 로그인 함수
function login(userIndex) {
  const user = TEST_USERS[userIndex];
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: user.username,
    password: user.password
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (loginResponse.status === 200) {
    const responseBody = JSON.parse(loginResponse.body);
    if (responseBody.success && responseBody.data) {
      jwtTokens[userIndex] = responseBody.data.token;
      console.log(`로그인 성공: ${user.username}, JWT 토큰 발급됨`);
      return true;
    }
  }
  
  console.log(`로그인 실패: ${user.username}`, loginResponse.body);
  return false;
}

export default function () {
  // 현재 VU 인덱스 가져오기 (0-based) VU 1-6 → index 0-5
  const userIndex = __VU - 1;

  // JWT 토큰이 없으면 로그인 시도
  if (!jwtTokens[userIndex]) {
    if (!login(userIndex)) {
      console.log(`user${userIndex + 1}: 로그인 실패로 테스트 중단`);
      return;
    }
  }

  // 장바구니 조회하여 현재 아이템 목록 가져오기
  const cartResponse = http.get('http://localhost:8081/user/cart/getCart', {
    headers: {
      'Authorization': `Bearer ${jwtTokens[userIndex]}`,
    },
  });

  if (cartResponse.status !== 200) {
    console.log(`user${userIndex + 1}: 장바구니 조회 실패 (${cartResponse.status})`);
    return;
  }

  const cartData = JSON.parse(cartResponse.body);
  if (!cartData.success || !cartData.data || !cartData.data.cartItemList || cartData.data.cartItemList.length === 0) {
    // 장바구니가 비어있으면 새 아이템 추가
    console.log(`user${userIndex + 1}: 장바구니가 비어있어 새 아이템 추가`);
    addTestItems(userIndex);
    return; // 다음 반복에서 삭제 시도
  }

  const currentItems = cartData.data.cartItemList;
  const deleteCount = Math.min(Math.floor(Math.random() * 2) + 1, currentItems.length);
  const itemsToDelete = currentItems.slice(0, deleteCount).map(item => item.id);

  console.log(`user${userIndex + 1}: 삭제할 아이템 ID: [${itemsToDelete.join(', ')}]`);

  // 장바구니 아이템 삭제 요청 데이터
  const deleteRequest = {
    cartItemIds: itemsToDelete
  };

  // 장바구니 아이템 삭제 API 호출 (순수 삭제 성능 측정)
  let response = http.del('http://localhost:8081/user/cart/deleteItems', JSON.stringify(deleteRequest), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtTokens[userIndex]}`,
    },
  });

  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'status is 204': (r) => r.status === 204,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'response body is not empty': (r) => r.body.length > 0,
    '200 response contains success data': (r) => r.status === 200 && (r.body.includes('success') || r.body.includes('deleted') || r.body.includes('cartItems')),
    '204 response is empty': (r) => r.status === 204 && r.body.length === 0,
  });
  
  // 상태 코드별 카운트
  if (response.status === 200) {
    console.log(`200 삭제 성공 - user${userIndex + 1}: 아이템 [${itemsToDelete.join(', ')}]`);
  } else if (response.status === 204) {
    console.log(`204 삭제 성공 (No Content) - user${userIndex + 1}: 아이템 [${itemsToDelete.join(', ')}]`);
  } else {
    console.log(`기타 응답 - user${userIndex + 1}: 상태 ${response.status}, 본문: ${response.body}`);
  }

  // 0.5초 대기 (순수 삭제 테스트를 위해 대기 시간 단축)
  sleep(0.5);
}

// 테스트용 아이템 추가 함수
function addTestItems(userIndex) {
  // 3-4개의 아이템 추가
  for (let j = 0; j < 3; j++) {
    const itemId = Math.floor(Math.random() * 1000) + 1;
    const quantity = Math.floor(Math.random() * 3) + 1;

    const addItemResponse = http.post('http://localhost:8081/user/cart/addItem', JSON.stringify({
      itemId: itemId,
      quantity: quantity
    }), {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtTokens[userIndex]}`,
      },
    });

    if (addItemResponse.status === 200) {
      console.log(`user${userIndex + 1} 장바구니 아이템 추가 성공 (itemId: ${itemId})`);
    }
  }
}

// 테스트 후 정리
export function teardown(data) {
  console.log('=== 테스트 정리 시작 ===');
  // 필요시 추가 정리 로직 구현
  console.log('=== 테스트 정리 완료 ===');
}
