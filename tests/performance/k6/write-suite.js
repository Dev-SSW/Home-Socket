import { group } from 'k6';
import { COMMON_OPTIONS } from './config.js';

import createOrder, { setup as setupCreateOrder } from './scenarios/createOrder.js';
import deleteItems, { setup as setupDeleteItems } from './scenarios/deleteItems.js';

const WRITE_APIS = ['createOrder', 'deleteItems'];

function apiThresholds(apiNames) {
  const thresholds = {};
  for (const api of apiNames) {
    thresholds[`http_req_duration{api:${api}}`] = ['p(95)<400'];
    thresholds[`http_req_failed{api:${api}}`] = ['rate<0.02'];
    thresholds[`api_count{api:${api}}`] = ['count>=100'];
  }
  return thresholds;
}

export const options = {
  ...COMMON_OPTIONS,
  setupTimeout: __ENV.SETUP_TIMEOUT || '180s',
  thresholds: {
    ...(COMMON_OPTIONS.thresholds || {}),
    ...apiThresholds(WRITE_APIS),
  },
};

export function setup() {
  return {
    createOrder: setupCreateOrder(),
    deleteItems: setupDeleteItems(),
  };
}

export default function (data) {
  group('order-write', () => {
    createOrder(data.createOrder);
  });

  group('cart-write', () => {
    deleteItems(data.deleteItems);
  });
}
