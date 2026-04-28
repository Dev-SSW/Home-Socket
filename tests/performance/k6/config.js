// 테스트 대상 서버 주소
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// 로그인할 기본 사용자 목록
export const TEST_USERS = [
  { username: 'user1', password: 'password123' },
  { username: 'user2', password: 'password123' },
  { username: 'user3', password: 'password123' },
  { username: 'user4', password: 'password123' },
  { username: 'user5', password: 'password123' },
  { username: 'user6', password: 'password123' },
  { username: 'user7', password: 'password123' },
  { username: 'user8', password: 'password123' },
];

// 공통 통과 기준 설정
export const COMMON_THRESHOLDS = {
  http_req_duration: ['p(95)<400'],
  http_req_failed: ['rate<0.02'],
};

// 가상 사용자 수 설정 (설정 후 실행 시에는 해당 설정을 따르고, 없을 경우 기본값을 사용)
export const COMMON_OPTIONS = {
  // 가상 사용자 수 설정, 기본 값은 8
  vus: Number(__ENV.VUS || 8),
  // 테스트 실행 시간
  duration: __ENV.DURATION || '30s',
  // 공통 통과 기준 적용
  thresholds: COMMON_THRESHOLDS,
};