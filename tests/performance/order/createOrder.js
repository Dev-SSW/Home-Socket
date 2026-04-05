import http from 'k6/http';
import { check, sleep } from 'k6';

// 장바구니로 주문 생성 부하 테스트
export let options = {
  vus: 8,                                                   // 동시 사용자 8명
  duration: '45s',                                      // 45초 동안
  thresholds: {
    http_req_duration: ['p(95)<1000'],      // 95%의 요청이 1000ms 미만
    http_req_failed: ['rate<0.05'],             // 실패율 5% 미만
  },
};

// 테스트용 사용자 정보 (랜덤 사용자)
const getRandomUser = () => {
  // user1 ~ user100
  const userId = Math.floor(Math.random() * 100) + 1;
  return {
    username: `user${userId}`,
    password: 'password123'
  };
};

// JWT 토큰 저장
let jwtToken = null;
let currentUser = null;

// 로그인 함수
function login() {
  currentUser = getRandomUser();
  const loginResponse = http.post('http://localhost:8081/public/login', JSON.stringify({
    username: currentUser.username,
    password: currentUser.password
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (loginResponse.status === 200) {
    const responseBody = JSON.parse(loginResponse.body);
    if (responseBody.success && responseBody.data) {
      jwtToken = responseBody.data.token;
      console.log(`로그인 성공: ${currentUser.username}, JWT 토큰 발급됨`);
      return true;
    }
  }
  
  console.log(`로그인 실패: ${currentUser.username}`, loginResponse.body);
  return false;
}

export default function () {
  // 매번 새로운 사용자로 로그인
  if (!login()) {
    throw new Error('로그인 실패');
  }
  
  // 장바구니 조회하여 아이템 ID 확인
  let cartResponse = http.get('http://localhost:8081/user/cart/getCart', {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`,
    },
  });
  
  let cartItemIds = [];
  if (cartResponse.status === 200) {
    const cartBody = JSON.parse(cartResponse.body);
    if (cartBody.success && cartBody.data && cartBody.data.cartItems) {
      cartItemIds = cartBody.data.cartItems.map(item => item.id);
      console.log(`${currentUser.username}의 장바구니 아이템 IDs:`, cartItemIds);
    }
  }
  
  // 장바구니에 아이템이 없으면 새 아이템 추가
  if (cartItemIds.length === 0) {
    console.log(`${currentUser.username}의 장바구니가 비어있어 새 아이템 추가`);
    addTestItems();
    return;
  }
  
  // 주문 생성 요청 데이터
  const orderRequest = {
    cartItemIds: cartItemIds.slice(0, 3),  // 최대 3개만 사용
    addressId: 1,                                    // 배송지 ID
    couponPublishId: null                       // 쿠폰 사용하지 않음
  };
  
  console.log(`${currentUser.username}의 주문 요청 데이터:`, JSON.stringify(orderRequest));
  
  // 주문 생성 API 호출
  let response = http.post('http://localhost:8081/user/order/createCartOrder', JSON.stringify(orderRequest), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`,
    },
  });
  
  console.log(`${currentUser.username}의 주문 생성 응답 상태:`, response.status);
  console.log(`${currentUser.username}의 주문 생성 응답 본문:`, response.body);
  
  // 응답 검증
  check(response, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
    'response body is not empty': (r) => r.body.length > 0,
    'response contains order data': (r) => r.body.includes('orderId') || r.body.includes('order') || r.body.includes('success'),
  });
  
  // 2초 대기
  sleep(2);
}

// 테스트용 아이템 추가 함수
function addTestItems() {
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
        'Authorization': `Bearer ${jwtToken}`,
      },
    });

    if (addItemResponse.status === 200) {
      console.log(`${currentUser.username} 장바구니 아이템 추가 성공 (itemId: ${itemId})`);
    }
  }
}
