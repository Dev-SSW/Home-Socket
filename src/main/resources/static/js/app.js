const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

const state = {
  currentView: "store",
  activeRequests: 0,
  itemPage: null,
  items: [],
  categories: [],
  cart: null,
  selectedCartIds: new Set(),
  checkoutCartItemIds: null,
  checkoutSubtotal: 0,
  checkoutCouponDiscount: 0,
  orderPage: null,
  adminCouponPage: null,
  orders: [],
  currentItem: null,
  wsClient: null,
};

const viewMeta = {
  store: { label: "Shop", title: "상품 탐색" },
  auth: { label: "Account", title: "로그인 / 회원가입" },
  detail: { label: "Product", title: "상품 상세" },
  cart: { label: "Cart", title: "장바구니" },
  checkout: { label: "Checkout", title: "주문 / 결제" },
  orders: { label: "Orders", title: "주문 내역" },
  account: { label: "My", title: "마이페이지" },
  admin: { label: "Admin", title: "관리자" },
  realtime: { label: "Notice", title: "실시간 알림" },
};

const apiBase = localStorage.getItem("apiBase") || "";

const storage = {
  get token() {
    return localStorage.getItem("accessToken") || "";
  },
  set token(value) {
    value ? localStorage.setItem("accessToken", value) : localStorage.removeItem("accessToken");
  },
  get refreshToken() {
    return localStorage.getItem("refreshToken") || "";
  },
  set refreshToken(value) {
    value ? localStorage.setItem("refreshToken", value) : localStorage.removeItem("refreshToken");
  },
  get username() {
    return localStorage.getItem("username") || "";
  },
  set username(value) {
    value ? localStorage.setItem("username", value) : localStorage.removeItem("username");
  },
  get role() {
    return localStorage.getItem("role") || "";
  },
  set role(value) {
    value ? localStorage.setItem("role", value) : localStorage.removeItem("role");
  },
};

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function money(value) {
  const number = Number(value || 0);
  return `${number.toLocaleString("ko-KR")}원`;
}

function asNumber(value) {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  return Number.isNaN(number) ? null : number;
}

function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function compactBody(body) {
  return Object.fromEntries(
    Object.entries(body).filter(([, value]) => value !== "" && value !== null && value !== undefined),
  );
}

function renderLoading(selector, message = "불러오는 중입니다.") {
  const target = $(selector);
  if (!target) return;
  target.innerHTML = `<div class="loading-state"><span></span>${escapeHtml(message)}</div>`;
}

function setNetworkStatus(status = "ready") {
  const badgeEl = $("#networkBadge");
  if (!badgeEl) return;
  badgeEl.classList.remove("busy", "error");
  if (status === "busy") {
    badgeEl.classList.add("busy");
    badgeEl.textContent = "Loading";
    return;
  }
  if (status === "error") {
    badgeEl.classList.add("error");
    badgeEl.textContent = "Error";
    return;
  }
  badgeEl.textContent = "Ready";
}

function updateClock() {
  const label = $("#clockLabel");
  if (!label) return;
  label.textContent = new Date().toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function updateViewHeader(view) {
  const meta = viewMeta[view] || viewMeta.store;
  $("#currentViewLabel").textContent = meta.label;
  $("#currentViewTitle").textContent = meta.title;
  document.body.dataset.view = view;
}

function badge(value) {
  const text = escapeHtml(value || "-");
  const red = ["CANCELLED", "PAYMENT_FAILED", "FAILED", "EXPIRED"].includes(value);
  const orange = ["PAYMENT_PENDING", "READY", "SHIPPING"].includes(value);
  const green = ["PAID", "COMPLETE", "APPROVED", "AVAILABLE"].includes(value);
  return `<span class="badge ${red ? "red" : orange ? "orange" : green ? "green" : ""}">${text}</span>`;
}

function toast(message, isError = false) {
  const box = $("#toast");
  box.textContent = message;
  box.style.background = isError ? "#991b1b" : "#182230";
  box.classList.remove("hidden");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => box.classList.add("hidden"), 3200);
}

async function api(path, options = {}) {
  state.activeRequests += 1;
  setNetworkStatus("busy");
  const headers = {
    Accept: "application/json",
    ...(options.headers || {}),
  };

  const init = {
    method: options.method || "GET",
    headers,
  };

  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(options.body);
  }

  if (storage.token && options.auth !== false) {
    headers.Authorization = `Bearer ${storage.token}`;
  }

  try {
    const requestPath = path.startsWith("http") ? path : `${apiBase}${path}`;
    const response = await fetch(requestPath, init);
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      throw new Error(payload?.message || `HTTP ${response.status}`);
    }

    if (payload && payload.success === false) {
      throw new Error(payload.message || payload.error || "요청에 실패했습니다.");
    }

    return payload && Object.prototype.hasOwnProperty.call(payload, "data") ? payload.data : payload;
  } catch (error) {
    setNetworkStatus("error");
    throw error;
  } finally {
    state.activeRequests = Math.max(0, state.activeRequests - 1);
    if (state.activeRequests === 0) {
      setTimeout(() => {
        if (state.activeRequests === 0 && !$("#networkBadge")?.classList.contains("error")) {
          setNetworkStatus("ready");
        }
      }, 500);
    }
  }
}

function requireLogin() {
  if (!storage.token) {
    location.hash = "#auth";
    toast("로그인이 필요한 기능입니다.", true);
    return false;
  }
  return true;
}

function isAdmin() {
  return storage.role === "ROLE_ADMIN";
}

function requireAdmin() {
  if (!requireLogin()) return false;
  if (!isAdmin()) {
    location.hash = "#store";
    toast("관리자 권한이 필요한 기능입니다.", true);
    return false;
  }
  return true;
}

function clearAuth() {
  storage.token = "";
  storage.refreshToken = "";
  storage.username = "";
  storage.role = "";
}

function updateAuthUi() {
  const signed = Boolean(storage.token);
  const admin = signed && isAdmin();
  const badgeEl = $("#authBadge");
  const logoutBtn = $("#logoutBtn");
  const loginLink = $(".primary-link");

  badgeEl.textContent = signed ? `${storage.username || "사용자"}${admin ? " · ADMIN" : ""}` : "로그인 필요";
  badgeEl.classList.toggle("signed", signed);
  logoutBtn.classList.toggle("hidden", !signed);
  loginLink.textContent = signed ? "계정" : "로그인";
  loginLink.href = signed ? "#account" : "#auth";
  $$("[data-auth='user']").forEach((node) => node.classList.toggle("hidden", !signed));
  $$("[data-auth='guest']").forEach((node) => node.classList.toggle("hidden", signed));
  $$("[data-auth='admin']").forEach((node) => node.classList.toggle("hidden", !admin));
}

function setActiveNav(view) {
  $$("[data-nav]").forEach((node) => node.classList.toggle("active", node.dataset.nav === view));
}

function showView(view) {
  state.currentView = view;
  $$(".view").forEach((node) => node.classList.add("hidden"));
  const target = $(`#view-${view}`);
  if (target) target.classList.remove("hidden");
  setActiveNav(view);
  updateViewHeader(view);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function route() {
  const hash = location.hash.replace("#", "") || "store";
  const [view, id] = hash.split("/");

  if (view === "detail") {
    showView("detail");
    if (id) await safeRun(() => loadItemDetail(id));
    return;
  }

  const protectedViews = new Set(["cart", "checkout", "orders", "account", "realtime"]);
  if (protectedViews.has(view) && !requireLogin()) return;
  if (view === "admin" && storage.token && !storage.role) {
    await loadCurrentUser({ silent: true });
  }
  if (view === "admin" && !requireAdmin()) return;

  const knownViews = ["store", "auth", "cart", "checkout", "orders", "account", "admin", "realtime"];
  showView(knownViews.includes(view) ? view : "store");

  if (view === "store" || !view) {
    if (!state.itemPage) await safeRun(loadItems);
  }
  if (view === "cart") await safeRun(loadCart);
  if (view === "checkout") await safeRun(loadOrderPage);
  if (view === "orders") await safeRun(loadOrders);
  if (view === "account") {
    await safeRun(loadProfile);
    await safeRun(loadAddresses);
  }
}

async function safeRun(fn) {
  try {
    await fn();
  } catch (error) {
    console.error(error);
    toast(error.message || "요청 처리 중 오류가 발생했습니다.", true);
  }
}

function productInitial(name) {
  return escapeHtml(String(name || "HS").slice(0, 2).toUpperCase());
}

function renderItems(page) {
  state.itemPage = page;
  state.items = page?.content || [];
  $("#metricItemCount").textContent = page?.totalElements ?? state.items.length;

  if (!state.items.length) {
    $("#itemGrid").innerHTML = `<div class="empty-state">등록된 상품이 없습니다.</div>`;
    $("#itemPager").innerHTML = "";
    return;
  }

  $("#itemGrid").innerHTML = state.items
    .map(
      (item) => {
        const inStock = Number(item.stock || 0) > 0;
        return `
        <article class="item-card">
          <div class="product-visual">
            <strong>${productInitial(item.name)}</strong>
            <span>${inStock ? `Stock ${escapeHtml(item.stock)}` : "Sold out"}</span>
          </div>
          <div>
            <h3>${escapeHtml(item.name)}</h3>
            <div class="meta-row">
              <span>평점 ${Number(item.avgStar || 0).toFixed(1)}</span>
              <span>${inStock ? badge("판매중") : badge("품절")}</span>
            </div>
            ${item.categoryName ? `<div class="subtle-text">${escapeHtml(item.categoryName)}</div>` : ""}
          </div>
          <div class="price">${money(item.itemPrice)}</div>
          <div class="item-actions">
            <a class="secondary-button" href="#detail/${item.id}">View</a>
            <button class="primary-button" type="button" data-action="add-cart" data-id="${item.id}" ${inStock ? "" : "disabled"}>Add</button>
          </div>
          ${isAdmin() ? `<button class="ghost-button" type="button" data-action="fill-admin-item" data-id="${item.id}">Admin edit</button>` : ""}
        </article>
      `;
      },
    )
    .join("");

  const pageNumber = page?.page ?? 0;
  $("#itemPager").innerHTML = `
    <button class="ghost-button" type="button" data-action="page-items" data-page="${Math.max(0, pageNumber - 1)}" ${page?.first ? "disabled" : ""}>이전</button>
    <button class="ghost-button" type="button" disabled>${pageNumber + 1} / ${Math.max(1, page?.totalPages || 1)}</button>
    <button class="ghost-button" type="button" data-action="page-items" data-page="${pageNumber + 1}" ${page?.last ? "disabled" : ""}>다음</button>
  `;
}

async function loadItems() {
  renderLoading("#itemGrid", "상품을 불러오는 중입니다.");
  const data = formData($("#storeFilterForm"));
  const params = new URLSearchParams();
  params.set("page", data.page || "0");
  params.set("size", data.size || "12");
  if (data.sort) params.append("sort", data.sort);

  const categoryId = asNumber(data.categoryId);
  const path = categoryId
    ? `/public/item/getItemsByCategory/${categoryId}?${params}`
    : `/public/item/getAllItem?${params}`;

  const page = await api(path, { auth: false });
  renderItems(page);
}

async function loadAllItems() {
  $("#storeFilterForm [name='page']").value = "0";
  $("#storeFilterForm [name='categoryId']").value = "";
  await loadItems();
}

function renderCategoryNode(category) {
  const children = category.children || [];
  return `
    <div class="tree-node">
      <button type="button" data-action="select-category" data-id="${category.id}">
        ${escapeHtml(category.name)} <span class="badge">#${category.id}</span>
      </button>
      <div class="table-actions" style="margin-top: 8px;">
        ${isAdmin() ? `<button class="small-button" type="button" data-action="fill-category-form" data-id="${category.id}">관리 편집</button>` : ""}
        <button class="small-button primary" type="button" data-action="load-child-category" data-id="${category.id}">하위 조회</button>
      </div>
      ${
        children.length
          ? `<div class="tree-children">${children.map(renderCategoryNode).join("")}</div>`
          : ""
      }
    </div>
  `;
}

async function loadCategories() {
  renderLoading("#categoryTree", "카테고리를 불러오는 중입니다.");
  const result = await api("/public/category/getRootCategory", { auth: false });
  state.categories = result?.categories || [];
  $("#categoryTree").classList.remove("empty-state");
  $("#categoryTree").innerHTML = state.categories.length
    ? state.categories.map(renderCategoryNode).join("")
    : `<div class="empty-state">카테고리가 없습니다.</div>`;
}

async function loadChildCategory(parentId) {
  renderLoading("#categoryTree", "하위 카테고리를 불러오는 중입니다.");
  const result = await api(`/public/category/getChildrenCategory/${parentId}`, { auth: false });
  const categories = result?.categories || [];
  $("#categoryTree").classList.remove("empty-state");
  $("#categoryTree").innerHTML = categories.length
    ? categories.map(renderCategoryNode).join("")
    : `<div class="empty-state">하위 카테고리가 없습니다.</div>`;
}

async function loadItemDetail(itemId) {
  $("#detailContainer").classList.remove("empty-state");
  renderLoading("#detailContainer", "상품 상세를 불러오는 중입니다.");
  const item = await api(`/public/item/getItem/${itemId}`, { auth: false });
  state.currentItem = item;

  $("#detailContainer").classList.remove("empty-state");
  $("#detailContainer").innerHTML = `
    <div class="detail-hero">
      <div class="product-visual detail-visual">
        <strong>${productInitial(item.name)}</strong>
        <span>Item #${item.id}</span>
      </div>
      <div class="detail-copy">
        <p class="eyebrow">Product detail</p>
        <h2>${escapeHtml(item.name)}</h2>
        <div class="meta-row">
          <span>${badge(item.stock > 0 ? "판매중" : "품절")}</span>
          <span>재고 ${escapeHtml(item.stock)}</span>
          <span>평점 ${Number(item.avgStar || 0).toFixed(1)}</span>
        </div>
        <div class="price">${money(item.itemPrice)}</div>
        <div class="quantity-control">
          <button class="ghost-button" type="button" data-action="detail-qty-down">-</button>
          <input id="detailQuantity" type="number" min="1" max="${Math.max(1, item.stock)}" value="1" />
          <button class="ghost-button" type="button" data-action="detail-qty-up">+</button>
        </div>
        <button class="primary-button" type="button" data-action="add-cart" data-id="${item.id}" data-quantity-source="#detailQuantity">장바구니 담기</button>
        ${isAdmin() ? `<button class="secondary-button" type="button" data-action="fill-admin-item" data-id="${item.id}">관리자 편집</button>` : ""}
      </div>
    </div>

    <div class="review-grid">
      <section>
        <h3>리뷰 작성/수정</h3>
        <form id="reviewForm" class="form-grid">
          <input name="reviewId" type="hidden" />
          <label>제목 <input name="title" required /></label>
          <label>내용 <textarea name="comment"></textarea></label>
          <label>별점 <input name="star" type="number" min="0" max="5" step="0.5" value="5" required /></label>
          <button class="primary-button full" type="submit">저장</button>
        </form>
      </section>
      <section>
        <div class="section-title compact">
          <div><p>Reviews</p><h2>상품 리뷰</h2></div>
          <button class="ghost-button" type="button" data-action="load-item-reviews" data-id="${item.id}">새로고침</button>
        </div>
        <div id="itemReviewList" class="review-list empty-state">로그인 후 리뷰를 조회할 수 있습니다.</div>
      </section>
    </div>
  `;

  if (storage.token) await safeRun(() => loadItemReviews(item.id));
}

async function loadItemReviews(itemId) {
  if (!requireLogin()) return;
  const reviews = await api(`/user/item/${itemId}/review/getItemReview/`);
  renderReviews("#itemReviewList", reviews || []);
}

function renderReviews(targetSelector, reviews) {
  const target = $(targetSelector);
  target.classList.remove("empty-state");
  if (!reviews.length) {
    target.innerHTML = `<div class="empty-state">리뷰가 없습니다.</div>`;
    return;
  }
  target.innerHTML = reviews
    .map(
      (review) => `
        <article class="review-card">
          <h4>${escapeHtml(review.title)}</h4>
          <div class="meta-row">
            <span>${escapeHtml(review.username || "")}</span>
            <span>★ ${escapeHtml(review.star)} · ${escapeHtml(review.reviewDate || "")}</span>
          </div>
          <p>${escapeHtml(review.comment || "")}</p>
          <div class="table-actions">
            <button class="small-button primary" type="button" data-action="edit-review" data-review='${escapeHtml(JSON.stringify(review))}'>수정</button>
            <button class="small-button danger" type="button" data-action="delete-review" data-id="${review.id}">삭제</button>
          </div>
        </article>
      `,
    )
    .join("");
}

async function addToCart(button) {
  if (!requireLogin()) return;
  const itemId = Number(button.dataset.id);
  const quantitySource = button.dataset.quantitySource;
  const quantity = quantitySource ? Number($(quantitySource)?.value || 1) : 1;
  await api("/user/cart/addItem", {
    method: "POST",
    body: { itemId, quantity },
  });
  toast("장바구니에 담았습니다.");
  await safeRun(loadCart);
}

async function loadCart() {
  if (!requireLogin()) return;
  renderLoading("#cartList", "장바구니를 불러오는 중입니다.");
  const cart = await api("/user/cart/getCart");
  state.cart = cart || { cartItemList: [], totalPrice: 0 };
  state.selectedCartIds = new Set((state.cart.cartItemList || []).map((item) => item.id));
  renderCart();
  $("#metricCartCount").textContent = state.cart.cartItemList?.length || 0;
}

function renderCart() {
  const items = state.cart?.cartItemList || [];
  $("#cartTotalPrice").textContent = money(state.cart?.totalPrice || 0);
  $("#cartSelectedCount").textContent = `${state.selectedCartIds.size}개`;

  if (!items.length) {
    $("#cartList").innerHTML = `<div class="empty-state">장바구니가 비어 있습니다.</div>`;
    return;
  }

  $("#cartList").innerHTML = `
      <div class="inline-actions" style="margin: 0 0 12px;">
        <button class="danger-button" type="button" data-action="delete-selected-cart">선택 삭제</button>
        <button class="secondary-button" type="button" data-action="go-checkout-selected">선택 상품 주문하기</button>
      </div>
    <table>
      <thead>
        <tr>
          <th>선택</th>
          <th>상품</th>
          <th>단가</th>
          <th>수량</th>
          <th>합계</th>
          <th>작업</th>
        </tr>
      </thead>
      <tbody>
        ${items
          .map(
            (item) => `
              <tr>
                <td><input class="row-check cart-check" type="checkbox" data-id="${item.id}" ${state.selectedCartIds.has(item.id) ? "checked" : ""} /></td>
                <td>${escapeHtml(item.itemName)} <span class="badge">#${item.itemId}</span></td>
                <td>${money(item.price)}</td>
                <td><input class="cart-quantity" data-id="${item.id}" type="number" min="1" value="${item.quantity}" /></td>
                <td>${money(item.totalPrice)}</td>
                <td>
                  <div class="table-actions">
                    <button class="small-button primary" type="button" data-action="update-cart-item" data-id="${item.id}">수정</button>
                    <button class="small-button danger" type="button" data-action="delete-cart-item" data-id="${item.id}">삭제</button>
                  </div>
                </td>
              </tr>
            `,
          )
          .join("")}
      </tbody>
    </table>
  `;
}

function syncCartSelection() {
  state.selectedCartIds = new Set(
    $$(".cart-check")
      .filter((input) => input.checked)
      .map((input) => Number(input.dataset.id)),
  );
  $("#cartSelectedCount").textContent = `${state.selectedCartIds.size}개`;
}

async function updateCartItem(button) {
  const cartItemId = Number(button.dataset.id);
  const quantity = Number($(`.cart-quantity[data-id="${cartItemId}"]`)?.value || 1);
  await api("/user/cart/updateItem", {
    method: "PUT",
    body: { cartItemId, quantity },
  });
  toast("수량을 변경했습니다.");
  await loadCart();
}

async function deleteCartItem(id) {
  await api(`/user/cart/${id}/deleteItem`, { method: "DELETE" });
  toast("상품을 삭제했습니다.");
  await loadCart();
}

async function deleteSelectedCart() {
  syncCartSelection();
  const ids = Array.from(state.selectedCartIds);
  if (!ids.length) return toast("삭제할 상품을 선택하세요.", true);
  await api("/user/cart/deleteItems", {
    method: "DELETE",
    body: { cartItemIds: ids },
  });
  toast("선택 상품을 삭제했습니다.");
  await loadCart();
}

async function clearCart() {
  await api("/user/cart/clearCart", { method: "DELETE" });
  toast("장바구니를 비웠습니다.");
  await loadCart();
}

function goToCheckoutWithSelected() {
  syncCartSelection();
  const ids = Array.from(state.selectedCartIds);
  if (!ids.length) {
    toast("주문할 상품을 선택하세요.", true);
    return;
  }
  state.checkoutCartItemIds = ids;
  location.hash = "#checkout";
}

async function loadOrderPage() {
  if (!requireLogin()) return;
  renderLoading("#checkoutAddressList", "배송지를 불러오는 중입니다.");
  renderLoading("#checkoutCouponList", "쿠폰을 불러오는 중입니다.");
  renderLoading("#checkoutCartItems", "주문 상품을 불러오는 중입니다.");
  const data = await api("/user/order/getOrderPage");
  state.orderPage = data;
  renderOrderPage(data);
}

function renderOrderPage(data) {
  const addresses = data?.addressResponses || [];
  const coupons = data?.couponPublishResponses || [];
  const allItems = data?.cartItemResponses || [];
  const requestedIds = state.checkoutCartItemIds?.length
    ? state.checkoutCartItemIds.map(Number)
    : allItems.map((item) => Number(item.id));
  const requestedIdSet = new Set(requestedIds);
  const items = allItems.filter((item) => requestedIdSet.has(Number(item.id)));
  const defaultAddress = addresses.find((address) => address.defaultAddress) || addresses[0];

  $("#checkoutAddressList").classList.remove("empty-state");
  $("#checkoutAddressList").innerHTML = addresses.length
    ? addresses
        .map(
          (address) => `
            <button class="option-card${defaultAddress?.id === address.id ? " selected" : ""}" type="button" data-action="select-checkout-address" data-id="${address.id}">
              <strong>#${address.id} ${escapeHtml(address.street)}</strong>
              <div>${escapeHtml(address.detailStreet)} (${escapeHtml(address.zipcode)})</div>
              ${address.defaultAddress ? '<span class="badge green">기본</span>' : ""}
            </button>
          `,
        )
        .join("")
    : `<div class="empty-state">등록된 배송지가 없습니다.</div>`;

  $("#checkoutCouponList").classList.remove("empty-state");
  $("#checkoutCouponList").innerHTML = coupons.length
    ? [
        `<button class="option-card selected" type="button" data-action="select-checkout-coupon" data-id="" data-discount="0">쿠폰 사용 안 함</button>`,
        ...coupons.map(
          (coupon) => `
            <button class="option-card" type="button" data-action="select-checkout-coupon" data-id="${coupon.id}" data-discount="${Number(coupon.discount || 0)}">
              <strong>#${coupon.id} ${escapeHtml(coupon.couponName)}</strong>
              <div>${money(coupon.discount)} · ${escapeHtml(coupon.validStart)} ~ ${escapeHtml(coupon.validEnd)}</div>
              ${badge(coupon.status)}
            </button>
          `,
        ),
      ].join("")
    : `<div class="empty-state">사용 가능한 쿠폰이 없습니다.</div>`;

  if (!items.length) {
    $("#checkoutCartItems").classList.add("empty-state");
    $("#checkoutCartItems").innerHTML = `<div class="empty-state">장바구니 상품이 없습니다.</div>`;
  } else {
    $("#checkoutCartItems").classList.remove("empty-state");
    $("#checkoutCartItems").innerHTML = `
      <table>
        <thead><tr><th>ID</th><th>상품</th><th>수량</th><th>합계</th></tr></thead>
        <tbody>${items
          .map(
            (item) => `
              <tr>
                <td>${item.id}</td>
                <td>${escapeHtml(item.itemName)}</td>
                <td>${item.quantity}</td>
                <td>${money(item.totalPrice)}</td>
              </tr>
            `,
          )
          .join("")}</tbody>
      </table>
    `;
  }

  if (defaultAddress) $("#orderForm [name='addressId']").value = defaultAddress.id;
  $("#orderForm [name='couponPublishId']").value = "";
  $("#orderForm [name='cartItemIds']").value = items.map((item) => item.id).join(",");
  state.checkoutSubtotal = items.reduce((sum, item) => sum + Number(item.totalPrice || 0), 0);
  state.checkoutCouponDiscount = 0;
  updateCheckoutSummary(items.length);
}

function updateCheckoutSummary(selectedCount = parseIdList($("#orderForm [name='cartItemIds']")?.value).length) {
  const discount = Math.min(state.checkoutSubtotal, state.checkoutCouponDiscount);
  $("#checkoutSelectedCount").textContent = `${selectedCount}개`;
  $("#checkoutSubtotal").textContent = money(state.checkoutSubtotal);
  $("#checkoutDiscount").textContent = discount ? `-${money(discount)}` : money(0);
  $("#checkoutTotalPrice").textContent = money(state.checkoutSubtotal - discount);
}

async function submitCheckout(event) {
  event.preventDefault();
  const data = formData(event.target);
  const cartItemIds = parseIdList(data.cartItemIds);
  const addressId = asNumber(data.addressId);
  if (!addressId) {
    toast("배송지를 선택하세요.", true);
    return;
  }
  if (!cartItemIds.length) {
    toast("주문할 상품이 없습니다.", true);
    return;
  }

  const order = await api("/user/order/createCartOrder", {
    method: "POST",
    body: {
      addressId,
      couponPublishId: asNumber(data.couponPublishId),
      cartItemIds,
    },
  });

  const payment = await api("/user/payments/confirm", {
    method: "POST",
    body: {
      orderId: order.id,
      amount: order.totalPrice,
      mockResult: data.mockResult || "SUCCESS",
    },
  });

  state.checkoutCartItemIds = null;
  await safeRun(loadCart);
  await safeRun(loadOrders);
  await safeRun(() => loadOrderDetail(order.id));
  location.hash = "#orders";
  toast(`주문 처리 결과: ${payment.orderStatus} / ${payment.paymentStatus}`);
}

function parseIdList(value) {
  return String(value || "")
    .split(",")
    .map((part) => Number(part.trim()))
    .filter((number) => Number.isFinite(number) && number > 0);
}

async function loadOrders() {
  if (!requireLogin()) return;
  renderLoading("#orderList", "주문 내역을 불러오는 중입니다.");
  const orders = await api("/user/order/getOrderList");
  state.orders = orders || [];
  $("#metricOrderCount").textContent = state.orders.length;
  renderOrders();
}

function renderOrders() {
  if (!state.orders.length) {
    $("#orderList").innerHTML = `<div class="empty-state">주문 내역이 없습니다.</div>`;
    return;
  }
  $("#orderList").innerHTML = `
    <table>
      <thead><tr><th>ID</th><th>일자</th><th>금액</th><th>주문</th><th>배송</th><th>작업</th></tr></thead>
      <tbody>
        ${state.orders
          .map(
            (order) => `
              <tr>
                <td>${order.id}</td>
                <td>${escapeHtml(order.orderDate || "")}</td>
                <td>${money(order.totalPrice)}</td>
                <td>${badge(order.orderStatus || "-")}</td>
                <td>${badge(order.deliveryStatus || "-")}</td>
                <td>
                  <div class="table-actions">
                    <button class="small-button primary" type="button" data-action="load-order-detail" data-id="${order.id}">상세</button>
                    <button class="small-button danger" type="button" data-action="cancel-order" data-id="${order.id}">취소</button>
                  </div>
                </td>
              </tr>
            `,
          )
          .join("")}
      </tbody>
    </table>
  `;
}

async function loadOrderDetail(id) {
  const detail = await api(`/user/order/${id}/getOrderDetail`);
  $("#orderDetailForm [name='orderId']").value = detail.id;
  renderOrderDetail(detail);
}

function renderOrderDetail(detail) {
  const delivery = detail.deliveryResponse;
  const items = detail.orderItemResponses || [];
  $("#orderDetail").classList.remove("empty-state");
  $("#orderDetail").innerHTML = `
    <div class="info-grid">
      <div><span>주문 ID</span><strong>${detail.id}</strong></div>
      <div><span>상태</span><strong>${badge(detail.orderStatus)}</strong></div>
      <div><span>총액</span><strong>${money(detail.totalPrice)}</strong></div>
      <div><span>배송 ID</span><strong>${delivery?.id || "-"}</strong></div>
    </div>
    <h3>상품</h3>
    ${items
      .map((item) => `<p>${escapeHtml(item.itemName)} · ${item.quantity}개 · ${money(item.totalPrice)}</p>`)
      .join("")}
    <h3>배송지</h3>
    <p>${escapeHtml(delivery?.addressResponse?.street || "")} ${escapeHtml(delivery?.addressResponse?.detailStreet || "")}</p>
  `;
}

async function cancelOrder(id) {
  await api(`/user/order/${id}/cancelOrder`, { method: "DELETE" });
  toast("주문을 취소했습니다.");
  await loadOrders();
}

async function loadCurrentUser({ silent = false } = {}) {
  if (!storage.token) return null;

  try {
    const profile = await api("/user/getUser");
    storage.username = profile.username;
    storage.role = profile.role;
    updateAuthUi();
    return profile;
  } catch (error) {
    if (!silent) throw error;
    clearAuth();
    updateAuthUi();
    return null;
  }
}

async function loadProfile() {
  renderLoading("#profileBox", "회원 정보를 불러오는 중입니다.");
  const profile = await loadCurrentUser();
  $("#profileBox").classList.remove("empty-state");
  $("#profileBox").innerHTML = `
    <div class="info-grid">
      <div><span>ID</span><strong>${profile.id}</strong></div>
      <div><span>아이디</span><strong>${escapeHtml(profile.username)}</strong></div>
      <div><span>이름</span><strong>${escapeHtml(profile.name)}</strong></div>
      <div><span>생년월일</span><strong>${escapeHtml(profile.birth)}</strong></div>
      <div><span>권한</span><strong>${escapeHtml(profile.role)}</strong></div>
    </div>
  `;
  $("#profileUpdateForm [name='name']").value = profile.name || "";
  $("#profileUpdateForm [name='birth']").value = profile.birth || "";
  updateAuthUi();
}

async function updateProfile(event) {
  event.preventDefault();
  const data = compactBody(formData(event.target));
  await api("/user/updateUser", {
    method: "PUT",
    body: data,
  });
  toast("회원 정보를 수정했습니다.");
  await loadProfile();
}

async function updatePassword(event) {
  event.preventDefault();
  await api("/user/updatePassword", {
    method: "PUT",
    body: formData(event.target),
  });
  event.target.reset();
  toast("비밀번호를 변경했습니다.");
}

async function deleteAccount() {
  if (!confirm("정말 탈퇴하시겠습니까?")) return;
  await api("/user/deleteUser", { method: "DELETE" });
  clearAuth();
  updateAuthUi();
  toast("회원 탈퇴가 완료되었습니다.");
  location.hash = "#store";
}

async function loadAddresses() {
  renderLoading("#addressList", "배송지 목록을 불러오는 중입니다.");
  const addresses = await api("/user/address/getAllAddress");
  renderAddresses(addresses || []);
}

function renderAddresses(addresses) {
  if (!addresses.length) {
    $("#addressList").innerHTML = `<div class="empty-state">배송지가 없습니다.</div>`;
    return;
  }
  $("#addressList").innerHTML = `
    <table>
      <thead><tr><th>ID</th><th>주소</th><th>우편번호</th><th>기본</th><th>작업</th></tr></thead>
      <tbody>
        ${addresses
          .map(
            (address) => `
              <tr>
                <td>${address.id}</td>
                <td>${escapeHtml(address.street)} ${escapeHtml(address.detailStreet)}</td>
                <td>${escapeHtml(address.zipcode)}</td>
                <td>${address.defaultAddress ? badge("기본") : "-"}</td>
                <td>
                  <div class="table-actions">
                    <button class="small-button primary" type="button" data-action="edit-address" data-address='${escapeHtml(JSON.stringify(address))}'>수정</button>
                    <button class="small-button" type="button" data-action="set-default-address" data-id="${address.id}">기본</button>
                    <button class="small-button danger" type="button" data-action="delete-address" data-id="${address.id}">삭제</button>
                  </div>
                </td>
              </tr>
            `,
          )
          .join("")}
      </tbody>
    </table>
  `;
}

async function saveAddress(event) {
  event.preventDefault();
  const form = event.target;
  const data = formData(form);
  const addressId = data.addressId;
  const body = {
    street: data.street,
    detailStreet: data.detailStreet,
    zipcode: data.zipcode,
    defaultAddress: form.elements.defaultAddress.checked,
  };
  if (addressId) {
    await api(`/user/address/${addressId}/updateAddress`, { method: "PUT", body });
    toast("배송지를 수정했습니다.");
  } else {
    await api("/user/address/createAddress", { method: "POST", body });
    toast("배송지를 등록했습니다.");
  }
  resetAddressForm();
  await loadAddresses();
}

function resetAddressForm() {
  $("#addressForm").reset();
  $("#addressForm [name='addressId']").value = "";
}

function fillAddressForm(address) {
  const form = $("#addressForm");
  form.elements.addressId.value = address.id;
  form.elements.street.value = address.street || "";
  form.elements.detailStreet.value = address.detailStreet || "";
  form.elements.zipcode.value = address.zipcode || "";
  form.elements.defaultAddress.checked = Boolean(address.defaultAddress);
}

async function setDefaultAddress(id) {
  await api(`/user/address/${id}/updateDefault`, { method: "PUT" });
  toast("기본 배송지를 변경했습니다.");
  await loadAddresses();
}

async function deleteAddress(id) {
  await api(`/user/address/${id}/deleteAddress`, { method: "DELETE" });
  toast("배송지를 삭제했습니다.");
  await loadAddresses();
}

async function publishCoupon(event) {
  event.preventDefault();
  const couponId = asNumber(formData(event.target).couponId);
  await api(`/user/coupon/${couponId}/couponPublish/publishCoupon`, { method: "POST" });
  toast("쿠폰을 발급했습니다.");
  await loadUserCoupons();
}

async function loadUserCoupons() {
  renderLoading("#userCouponList", "쿠폰 목록을 불러오는 중입니다.");
  const coupons = await api("/user/coupon/couponPublish/getCouponPublish");
  renderUserCoupons(coupons || []);
}

function renderUserCoupons(coupons) {
  if (!coupons.length) {
    $("#userCouponList").innerHTML = `<div class="empty-state">보유 쿠폰이 없습니다.</div>`;
    return;
  }
  $("#userCouponList").innerHTML = `
    <table>
      <thead><tr><th>ID</th><th>쿠폰</th><th>할인</th><th>기간</th><th>상태</th></tr></thead>
      <tbody>${coupons
        .map(
          (coupon) => `
            <tr>
              <td>${coupon.id}</td>
              <td>${escapeHtml(coupon.couponName)}</td>
              <td>${money(coupon.discount)}</td>
              <td>${escapeHtml(coupon.validStart)} ~ ${escapeHtml(coupon.validEnd)}</td>
              <td>${badge(coupon.status)}</td>
            </tr>
          `,
        )
        .join("")}</tbody>
    </table>
  `;
}

async function saveReview(event) {
  event.preventDefault();
  if (!state.currentItem) return;
  const form = event.target;
  const data = formData(form);
  const body = {
    title: data.title,
    comment: data.comment,
    star: Number(data.star),
  };
  if (data.reviewId) {
    await api(`/user/item/review/${data.reviewId}/updateReview/`, { method: "PUT", body });
    toast("리뷰를 수정했습니다.");
  } else {
    await api(`/user/item/${state.currentItem.id}/review/createReview/`, { method: "POST", body });
    toast("리뷰를 작성했습니다.");
  }
  form.reset();
  await loadItemReviews(state.currentItem.id);
}

async function loadUserReviews() {
  renderLoading("#userReviewList", "리뷰 목록을 불러오는 중입니다.");
  const reviews = await api("/user/getUserReview/");
  renderReviews("#userReviewList", reviews || []);
}

function fillReviewForm(review) {
  const form = $("#reviewForm");
  if (!form) {
    location.hash = `#detail/${review.itemId}`;
    setTimeout(() => fillReviewForm(review), 400);
    return;
  }
  form.elements.reviewId.value = review.id;
  form.elements.title.value = review.title || "";
  form.elements.comment.value = review.comment || "";
  form.elements.star.value = review.star || 5;
  form.scrollIntoView({ behavior: "smooth", block: "center" });
}

async function deleteReview(id) {
  await api(`/user/item/review/${id}/deleteReview/`, { method: "DELETE" });
  toast("리뷰를 삭제했습니다.");
  if (state.currentItem) await safeRun(() => loadItemReviews(state.currentItem.id));
  await safeRun(loadUserReviews);
}

async function login(event) {
  event.preventDefault();
  const data = formData(event.target);
  const result = await api("/public/login", {
    method: "POST",
    auth: false,
    body: data,
  });
  storage.token = result.token;
  storage.refreshToken = result.refreshToken;
  storage.username = data.username;
  updateAuthUi();
  await loadCurrentUser();
  toast("로그인했습니다.");
  location.hash = "#store";
}

async function signup(event) {
  event.preventDefault();
  await api("/public/signup", {
    method: "POST",
    auth: false,
    body: formData(event.target),
  });
  event.target.reset();
  toast("회원가입이 완료되었습니다. 로그인하세요.");
}

async function validateToken() {
  const token = storage.token;
  if (!token) return toast("검증할 토큰이 없습니다.", true);
  await api("/public/validateTest", {
    method: "POST",
    auth: false,
    body: { token },
  });
  toast("토큰이 유효합니다.");
}

async function renewToken() {
  if (!storage.refreshToken) return toast("refreshToken이 없습니다.", true);
  const result = await api("/public/tokenRenew", {
    method: "POST",
    auth: false,
    body: { token: storage.refreshToken },
  });
  storage.token = result.token;
  storage.refreshToken = result.refreshToken;
  await loadCurrentUser();
  updateAuthUi();
  toast("토큰을 재발급했습니다.");
}

function logout() {
  clearAuth();
  updateAuthUi();
  toast("로그아웃했습니다.");
  location.hash = "#store";
}

function fillAdminItem(button) {
  if (!requireAdmin()) return;
  const id = Number(button.dataset.id);
  const item = state.items.find((candidate) => candidate.id === id) || state.currentItem;
  if (!item) return;
  const form = $("#adminItemForm");
  form.elements.itemId.value = item.id;
  form.elements.name.value = item.name || "";
  form.elements.stock.value = item.stock ?? 0;
  form.elements.itemPrice.value = item.itemPrice ?? 0;
  location.hash = "#admin";
  toast("상품 편집 폼에 값을 채웠습니다. 카테고리 ID는 직접 확인해 주세요.");
}

function fillCategoryForm(button) {
  if (!requireAdmin()) return;
  const id = Number(button.dataset.id);
  const category = findCategoryById(state.categories, id);
  if (!category) return;
  const form = $("#adminCategoryForm");
  form.elements.categoryId.value = category.id;
  form.elements.name.value = category.name || "";
  form.elements.depth.value = category.depth ?? 0;
  form.elements.orderIndex.value = category.orderIndex ?? 0;
  form.elements.parentId.value = "";
  location.hash = "#admin";
}

function findCategoryById(categories, id) {
  for (const category of categories || []) {
    if (category.id === id) return category;
    const child = findCategoryById(category.children, id);
    if (child) return child;
  }
  return null;
}

async function saveAdminCategory(event) {
  event.preventDefault();
  if (!requireAdmin()) return;
  const data = formData(event.target);
  const id = data.categoryId;
  if (id) {
    await api(`/admin/category/updateCategory/${id}`, {
      method: "PUT",
      body: compactBody({
        name: data.name,
        orderIndex: asNumber(data.orderIndex),
      }),
    });
    toast("카테고리를 수정했습니다.");
  } else {
    await api("/admin/category/createCategory", {
      method: "POST",
      body: {
        name: data.name,
        depth: Number(data.depth || 0),
        orderIndex: Number(data.orderIndex || 0),
        parentId: asNumber(data.parentId),
      },
    });
    toast("카테고리를 생성했습니다.");
  }
  event.target.reset();
  await safeRun(loadCategories);
}

async function deleteCategory() {
  if (!requireAdmin()) return;
  const id = $("#adminCategoryForm [name='categoryId']").value || prompt("삭제할 카테고리 ID");
  if (!id) return;
  await api(`/admin/category/deleteCategory/${id}`, { method: "DELETE" });
  $("#adminCategoryForm").reset();
  toast("카테고리를 삭제했습니다.");
  await safeRun(loadCategories);
}

async function saveAdminItem(event) {
  event.preventDefault();
  if (!requireAdmin()) return;
  const data = formData(event.target);
  const body = {
    name: data.name,
    stock: Number(data.stock || 0),
    itemPrice: Number(data.itemPrice || 0),
  };
  if (data.itemId) {
    await api(`/admin/item/updateItem/${data.itemId}`, { method: "PUT", body });
    toast("상품을 수정했습니다.");
  } else {
    await api(`/admin/category/${data.categoryId}/item/createItem/`, { method: "POST", body });
    toast("상품을 생성했습니다.");
  }
  event.target.reset();
  await safeRun(loadItems);
}

async function deleteItem() {
  if (!requireAdmin()) return;
  const id = $("#adminItemForm [name='itemId']").value || prompt("삭제할 상품 ID");
  if (!id) return;
  await api(`/admin/item/deleteItem/${id}`, { method: "DELETE" });
  $("#adminItemForm").reset();
  toast("상품을 삭제했습니다.");
  await safeRun(loadItems);
}

async function loadAdminCoupons(page = 0) {
  if (!requireAdmin()) return;
  const pageNumber = Number.isFinite(Number(page)) ? Number(page) : 0;
  renderLoading("#adminCouponList", "쿠폰 목록을 불러오는 중입니다.");
  const result = await api(`/admin/coupon/getAllCoupon?page=${pageNumber}&size=20`);
  state.adminCouponPage = result;
  renderAdminCoupons(result || []);
}

function renderAdminCoupons(result) {
  const coupons = Array.isArray(result) ? result : result?.content || [];
  const pageNumber = Array.isArray(result) ? 0 : result.pageable?.pageNumber ?? result.number ?? 0;
  if (!coupons.length) {
    $("#adminCouponList").innerHTML = `<div class="empty-state">쿠폰이 없습니다.</div>`;
    return;
  }
  $("#adminCouponList").innerHTML = `
    <table>
      <thead><tr><th>ID</th><th>쿠폰</th><th>할인</th><th>기간</th><th>발급 후</th><th>작업</th></tr></thead>
      <tbody>${coupons
        .map(
          (coupon) => `
            <tr>
              <td>${coupon.id}</td>
              <td>${escapeHtml(coupon.name)}</td>
              <td>${money(coupon.discount)}</td>
              <td>${escapeHtml(coupon.validStart)} ~ ${escapeHtml(coupon.validEnd)}</td>
              <td>${escapeHtml(coupon.afterIssue)}일</td>
              <td><button class="small-button primary" type="button" data-action="edit-coupon" data-coupon='${escapeHtml(JSON.stringify(coupon))}'>편집</button></td>
            </tr>
          `,
        )
        .join("")}</tbody>
    </table>
    ${
      Array.isArray(result)
        ? ""
        : `<div class="pager">
            <button class="ghost-button" type="button" data-action="page-admin-coupons" data-page="${Math.max(0, pageNumber - 1)}" ${result.first ? "disabled" : ""}>이전</button>
            <button class="ghost-button" type="button" disabled>${pageNumber + 1} / ${Math.max(1, result.totalPages || 1)}</button>
            <button class="ghost-button" type="button" data-action="page-admin-coupons" data-page="${pageNumber + 1}" ${result.last ? "disabled" : ""}>다음</button>
          </div>`
    }
  `;
}

function fillCouponForm(coupon) {
  const form = $("#adminCouponForm");
  form.elements.couponId.value = coupon.id;
  form.elements.name.value = coupon.name || "";
  form.elements.discount.value = coupon.discount || 0;
  form.elements.validStart.value = coupon.validStart || "";
  form.elements.validEnd.value = coupon.validEnd || "";
  form.elements.afterIssue.value = coupon.afterIssue || "";
}

async function saveAdminCoupon(event) {
  event.preventDefault();
  if (!requireAdmin()) return;
  const data = formData(event.target);
  const body = {
    name: data.name,
    discount: Number(data.discount || 0),
    validStart: data.validStart,
    validEnd: data.validEnd,
    afterIssue: Number(data.afterIssue || 1),
  };
  if (data.couponId) {
    await api(`/admin/coupon/${data.couponId}/updateCoupon`, { method: "PUT", body });
    toast("쿠폰을 수정했습니다.");
  } else {
    await api("/admin/coupon/createCoupon", { method: "POST", body });
    toast("쿠폰을 생성했습니다.");
  }
  event.target.reset();
  await loadAdminCoupons();
}

async function deleteCoupon() {
  if (!requireAdmin()) return;
  const id = $("#adminCouponForm [name='couponId']").value || prompt("삭제할 쿠폰 ID");
  if (!id) return;
  await api(`/admin/coupon/${id}/deleteCoupon`, { method: "DELETE" });
  $("#adminCouponForm").reset();
  toast("쿠폰을 삭제했습니다.");
  await loadAdminCoupons();
}

async function updateDelivery(event) {
  event.preventDefault();
  if (!requireAdmin()) return;
  const data = formData(event.target);
  await api(`/admin/delivery/${data.deliveryId}/updateDeliveryStatus`, {
    method: "PUT",
    body: { status: data.status },
  });
  toast("배송 상태를 변경했습니다.");
}

async function loadUsers() {
  if (!requireAdmin()) return;
  renderLoading("#adminUserList", "회원 목록을 불러오는 중입니다.");
  const page = await api("/admin/getAllUser?page=0&size=30");
  const users = page?.content || page || [];
  if (!users.length) {
    $("#adminUserList").innerHTML = `<div class="empty-state">회원이 없습니다.</div>`;
    return;
  }
  $("#adminUserList").innerHTML = `
    <table>
      <thead><tr><th>ID</th><th>아이디</th><th>이름</th><th>생년월일</th><th>권한</th></tr></thead>
      <tbody>${users
        .map(
          (user) => `
            <tr>
              <td>${user.id}</td>
              <td>${escapeHtml(user.username)}</td>
              <td>${escapeHtml(user.name)}</td>
              <td>${escapeHtml(user.birth)}</td>
              <td>${badge(user.role)}</td>
            </tr>
          `,
        )
        .join("")}</tbody>
    </table>
  `;
}

function switchAdminTab(tab) {
  $$(".admin-tabs button").forEach((button) => button.classList.toggle("active", button.dataset.adminTab === tab));
  $$(".admin-section").forEach((section) => section.classList.add("hidden"));
  $(`#admin-${tab}`)?.classList.remove("hidden");
  if (tab === "coupon" && !state.adminCouponPage) {
    safeRun(() => loadAdminCoupons());
  }
}

function wsLog(message) {
  const box = $("#wsLog");
  const time = new Date().toLocaleTimeString("ko-KR");
  box.textContent += `[${time}] ${message}\n`;
  box.scrollTop = box.scrollHeight;
}

function connectWs() {
  const Stomp = window.StompJs;
  if (!Stomp) {
    wsLog("STOMP 라이브러리를 불러오지 못했습니다.");
    return;
  }
  const form = $("#wsForm");
  const data = formData(form);
  const token = data.token || storage.token;
  if (!token) {
    wsLog("JWT가 필요합니다.");
    return;
  }
  if (state.wsClient?.active) state.wsClient.deactivate();

  const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";
  const destination =
    data.subscribeType === "admin" ? "/user/queue/admin-notifications" : "/user/queue/notifications";

  state.wsClient = new Stomp.Client({
    brokerURL: `${wsProtocol}//${location.host}/ws`,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 0,
    onConnect: () => {
      wsLog(`connected: ${destination}`);
      state.wsClient.subscribe(destination, (message) => wsLog(message.body));
    },
    onStompError: (frame) => {
      wsLog(`STOMP ERROR: ${frame.headers?.message || "unknown"}`);
      if (frame.body) wsLog(frame.body);
    },
    onWebSocketError: () => wsLog("WebSocket error"),
    onWebSocketClose: () => wsLog("WebSocket closed"),
  });
  state.wsClient.activate();
}

function disconnectWs() {
  if (!state.wsClient) return;
  state.wsClient.deactivate();
  wsLog("disconnect requested");
}

const actions = {
  "load-store": loadAllItems,
  "clear-category-filter": loadAllItems,
  "load-categories": loadCategories,
  "select-category": async (button) => {
    $("#storeFilterForm [name='page']").value = "0";
    $("#storeFilterForm [name='categoryId']").value = button.dataset.id;
    await loadItems();
  },
  "load-child-category": (button) => loadChildCategory(button.dataset.id),
  "page-items": async (button) => {
    $("#storeFilterForm [name='page']").value = button.dataset.page;
    await loadItems();
  },
  "add-cart": addToCart,
  "detail-qty-down": () => {
    const input = $("#detailQuantity");
    input.value = Math.max(1, Number(input.value || 1) - 1);
  },
  "detail-qty-up": () => {
    const input = $("#detailQuantity");
    input.value = Math.min(Number(input.max || 9999), Number(input.value || 1) + 1);
  },
  "load-item-reviews": (button) => loadItemReviews(button.dataset.id),
  "load-cart": loadCart,
  "update-cart-item": updateCartItem,
  "delete-cart-item": (button) => deleteCartItem(button.dataset.id),
  "delete-selected-cart": deleteSelectedCart,
  "go-checkout-selected": goToCheckoutWithSelected,
  "clear-cart": clearCart,
  "load-order-page": loadOrderPage,
  "load-orders": loadOrders,
  "load-order-detail": (button) => loadOrderDetail(button.dataset.id),
  "cancel-order": (button) => cancelOrder(button.dataset.id),
  "load-profile": loadProfile,
  "load-addresses": loadAddresses,
  "reset-address-form": resetAddressForm,
  "edit-address": (button) => fillAddressForm(JSON.parse(button.dataset.address)),
  "set-default-address": (button) => setDefaultAddress(button.dataset.id),
  "delete-address": (button) => deleteAddress(button.dataset.id),
  "load-user-coupons": loadUserCoupons,
  "load-user-reviews": loadUserReviews,
  "delete-account": deleteAccount,
  "edit-review": (button) => fillReviewForm(JSON.parse(button.dataset.review)),
  "delete-review": (button) => deleteReview(button.dataset.id),
  "validate-token": validateToken,
  "renew-token": renewToken,
  "fill-admin-item": fillAdminItem,
  "fill-category-form": fillCategoryForm,
  "delete-category": deleteCategory,
  "delete-item": deleteItem,
  "load-admin-coupons": () => loadAdminCoupons(0),
  "page-admin-coupons": (button) => loadAdminCoupons(Number(button.dataset.page || 0)),
  "edit-coupon": (button) => fillCouponForm(JSON.parse(button.dataset.coupon)),
  "delete-coupon": deleteCoupon,
  "load-users": loadUsers,
  "connect-ws": connectWs,
  "disconnect-ws": disconnectWs,
};

document.addEventListener("click", (event) => {
  const actionNode = event.target.closest("[data-action]");
  if (!actionNode) return;
  const handler = actions[actionNode.dataset.action];
  if (!handler) return;
  event.preventDefault();
  safeRun(() => handler(actionNode, event));
});

document.addEventListener("change", (event) => {
  if (event.target.matches(".cart-check")) syncCartSelection();
});

$("#storeFilterForm").addEventListener("submit", (event) => {
  event.preventDefault();
  safeRun(loadItems);
});
$("#loginForm").addEventListener("submit", (event) => safeRun(() => login(event)));
$("#signupForm").addEventListener("submit", (event) => safeRun(() => signup(event)));
$("#logoutBtn").addEventListener("click", logout);
$("#orderForm").addEventListener("submit", (event) => safeRun(() => submitCheckout(event)));
$("#orderDetailForm").addEventListener("submit", (event) => {
  event.preventDefault();
  safeRun(() => loadOrderDetail(formData(event.target).orderId));
});
$("#profileUpdateForm").addEventListener("submit", (event) => safeRun(() => updateProfile(event)));
$("#passwordForm").addEventListener("submit", (event) => safeRun(() => updatePassword(event)));
$("#addressForm").addEventListener("submit", (event) => safeRun(() => saveAddress(event)));
$("#couponPublishForm").addEventListener("submit", (event) => safeRun(() => publishCoupon(event)));
$("#adminCategoryForm").addEventListener("submit", (event) => safeRun(() => saveAdminCategory(event)));
$("#adminItemForm").addEventListener("submit", (event) => safeRun(() => saveAdminItem(event)));
$("#adminCouponForm").addEventListener("submit", (event) => safeRun(() => saveAdminCoupon(event)));
$("#deliveryForm").addEventListener("submit", (event) => safeRun(() => updateDelivery(event)));

document.addEventListener("submit", (event) => {
  if (event.target.id === "reviewForm") {
    event.preventDefault();
    safeRun(() => saveReview(event));
  }
});

document.addEventListener("click", (event) => {
  const addressOption = event.target.closest("[data-action='select-checkout-address']");
  if (addressOption) {
    $("#orderForm [name='addressId']").value = addressOption.dataset.id;
    $$("#checkoutAddressList .option-card").forEach((node) => node.classList.remove("selected"));
    addressOption.classList.add("selected");
  }
  const couponOption = event.target.closest("[data-action='select-checkout-coupon']");
  if (couponOption) {
    $("#orderForm [name='couponPublishId']").value = couponOption.dataset.id;
    $$("#checkoutCouponList .option-card").forEach((node) => node.classList.remove("selected"));
    couponOption.classList.add("selected");
    state.checkoutCouponDiscount = Number(couponOption.dataset.discount || 0);
    updateCheckoutSummary();
  }
});

$$("[data-admin-tab]").forEach((button) => {
  button.addEventListener("click", () => switchAdminTab(button.dataset.adminTab));
});

window.addEventListener("hashchange", () => safeRun(route));

updateAuthUi();
$("#apiModeLabel").textContent = apiBase || "same-origin";
updateClock();
setInterval(updateClock, 30000);
safeRun(() => loadCurrentUser({ silent: true }));
safeRun(loadCategories);
safeRun(route);
