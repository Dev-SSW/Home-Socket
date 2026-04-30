import { Counter } from 'k6/metrics';

export const apiCount = new Counter('api_count');
export const apiFailedCount = new Counter('api_failed_count');

export function recordApiResult(apiName, response) {
  apiCount.add(1, { api: apiName });

  if (!response || response.status < 200 || response.status >= 400) {
    apiFailedCount.add(1, {
      api: apiName,
      status: response ? String(response.status) : 'NO_RESPONSE',
    });
  }
}