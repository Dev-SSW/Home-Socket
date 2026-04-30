import { group } from 'k6';
import { COMMON_OPTIONS } from './config.js';
import { loginAllUsers } from './auth.js';

import getAllAddress from './scenarios/getAllAddress.js';
import getRootCategory from './scenarios/getRootCategory.js';
import getChildrenCategory from './scenarios/getChildrenCategory.js';
import getAllItem from './scenarios/getAllItem.js';
import getItemsByCategory from './scenarios/getItemsByCategory.js';
import getOrderList from './scenarios/getOrderList.js';
import getOrderPage from './scenarios/getOrderPage.js';
import getOrderDetail from './scenarios/getOrderDetail.js';
import getItemReview from './scenarios/getItemReview.js';
import getUserReview from './scenarios/getUserReview.js';

const READ_APIS = [
  'getAllAddress',
  'getRootCategory',
  'getChildrenCategory',
  'getAllItem',
  'getItemsByCategory',
  'getOrderList',
  'getOrderPage',
  'getOrderDetail',
  'getItemReview',
  'getUserReview',
];

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
  thresholds: {
    ...(COMMON_OPTIONS.thresholds || {}),
    ...apiThresholds(READ_APIS),
  },
};

export function setup() {
  return {
    tokens: loginAllUsers(),
  };
}

export default function (data) {
  group('category-read', () => {
    getRootCategory();
    getChildrenCategory();
  });

  group('item-read', () => {
    getAllItem();
    getItemsByCategory();
  });

  group('user-read', () => {
    getAllAddress(data);
    getUserReview(data);
  });

  group('order-read', () => {
    getOrderList(data);
    getOrderPage(data);
    getOrderDetail(data);
  });

  group('review-read', () => {
    getItemReview(data);
  });
}
