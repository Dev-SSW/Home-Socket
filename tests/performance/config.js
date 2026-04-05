// 성능 테스트 설정
export const CONFIG = {
  // 로그 레벨: 'none', 'error', 'info', 'debug'
  LOG_LEVEL: 'none',  // 성능 테스트 시에는 'none'으로 설정
  
  // 결과 저장 여부
  SAVE_RESULTS: true,
  
  // 디버그 모드
  DEBUG_MODE: false
};

// 로그 함수
export function log(level, message) {
  // 완전히 비활성화하여 아무것도 출력하지 않음
  if (CONFIG.LOG_LEVEL === 'none') return;
  if (CONFIG.LOG_LEVEL === 'error' && level !== 'error') return;
  if (CONFIG.LOG_LEVEL === 'info' && level === 'debug') return;
  
  // 성능 테스트 시에 아무것도 출력하지 않음
  if (!CONFIG.DEBUG_MODE) return;
  
  console.log(message);
}
