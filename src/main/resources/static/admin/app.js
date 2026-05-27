const tokenKey = "elfaddouiAdminToken";
const maxUploadSizeBytes = 15 * 1024 * 1024;
const forceFreshLoginOnLoad = true;

const state = {
    token: localStorage.getItem(tokenKey) || "",
    me: null,
    dashboardSummary: null,
    categories: [],
    products: [],
    orders: [],
    customers: [],
    loyaltyCustomers: [],
    loyaltyGifts: [],
    selectedLoyaltyAccount: null,
    selectedLoyaltyCustomerId: null,
    deliverySettings: null,
    homeSettings: null,
    selectedOrderId: null,
    selectedCustomerId: null,
    expandedCategoryIds: new Set(),
    expandedProductIds: new Set()
};

const elements = {
    loginView: document.getElementById("loginView"),
    appView: document.getElementById("appView"),
    loginForm: document.getElementById("loginForm"),
    adminIdentity: document.getElementById("adminIdentity"),
    logoutBtn: document.getElementById("logoutBtn"),
    refreshAllBtn: document.getElementById("refreshAllBtn"),
    toast: document.getElementById("toast"),
    categoryForm: document.getElementById("categoryForm"),
    categoryList: document.getElementById("categoryList"),
    resetCategoryFormBtn: document.getElementById("resetCategoryFormBtn"),
    categoryCount: document.getElementById("categoryCount"),
    activeCategoryCount: document.getElementById("activeCategoryCount"),
    orderCount: document.getElementById("orderCount"),
    pendingOrderCount: document.getElementById("pendingOrderCount"),
    customerCount: document.getElementById("customerCount"),
    todayRevenue: document.getElementById("todayRevenue"),
    revenueTotal: document.getElementById("revenueTotal"),
    lowStockCount: document.getElementById("lowStockCount"),
    productForm: document.getElementById("productForm"),
    productList: document.getElementById("productList"),
    resetProductFormBtn: document.getElementById("resetProductFormBtn"),
    productSearch: document.getElementById("productSearch"),
    productCategoryFilter: document.getElementById("productCategoryFilter"),
    productActiveFilter: document.getElementById("productActiveFilter"),
    productPromoFilter: document.getElementById("productPromoFilter"),
    productBioFilter: document.getElementById("productBioFilter"),
    productNewFilter: document.getElementById("productNewFilter"),
    productPopularFilter: document.getElementById("productPopularFilter"),
    productMinDiscountFilter: document.getElementById("productMinDiscountFilter"),
    productMaxDiscountFilter: document.getElementById("productMaxDiscountFilter"),
    filterProductsBtn: document.getElementById("filterProductsBtn"),
    resetProductFiltersBtn: document.getElementById("resetProductFiltersBtn"),
    productCount: document.getElementById("productCount"),
    activeProductCount: document.getElementById("activeProductCount"),
    stockCount: document.getElementById("stockCount"),
    promoProductCount: document.getElementById("promoProductCount"),
    categoryImageUrl: document.getElementById("categoryImageUrl"),
    productImageUrl: document.getElementById("productImageUrl"),
    categoryImagePreview: document.getElementById("categoryImagePreview"),
    productImagePreview: document.getElementById("productImagePreview"),
    uploadCategoryImageBtn: document.getElementById("uploadCategoryImageBtn"),
    uploadProductImageBtn: document.getElementById("uploadProductImageBtn"),
    refreshOrdersBtn: document.getElementById("refreshOrdersBtn"),
    refreshCustomersBtn: document.getElementById("refreshCustomersBtn"),
    refreshLoyaltyBtn: document.getElementById("refreshLoyaltyBtn"),
    orderList: document.getElementById("orderList"),
    orderDetail: document.getElementById("orderDetail"),
    customerList: document.getElementById("customerList"),
    customerDetail: document.getElementById("customerDetail"),
    loyaltyCustomerQuery: document.getElementById("loyaltyCustomerQuery"),
    loyaltyCustomerResults: document.getElementById("loyaltyCustomerResults"),
    loyaltyCustomerId: document.getElementById("loyaltyCustomerId"),
    loyaltyAccountPreview: document.getElementById("loyaltyAccountPreview"),
    loyaltyPointsForm: document.getElementById("loyaltyPointsForm"),
    loyaltyVoucherForm: document.getElementById("loyaltyVoucherForm"),
    loyaltyGiftForm: document.getElementById("loyaltyGiftForm"),
    loyaltyGiftList: document.getElementById("loyaltyGiftList"),
    loyaltyGiftSubmitBtn: document.getElementById("loyaltyGiftSubmitBtn"),
    loyaltyGiftCancelEditBtn: document.getElementById("loyaltyGiftCancelEditBtn"),
    deliverySettingsForm: document.getElementById("deliverySettingsForm"),
    deliveryPreview: document.getElementById("deliveryPreview"),
    homeSettingsForm: document.getElementById("homeSettingsForm"),
    homeSettingsPreview: document.getElementById("homeSettingsPreview"),
    refreshHomeSettingsBtn: document.getElementById("refreshHomeSettingsBtn"),
    navLinks: Array.from(document.querySelectorAll("[data-section]")),
    jumpButtons: Array.from(document.querySelectorAll("[data-section-target]")),
    sections: {
        overviewSection: document.getElementById("overviewSection"),
        ordersSection: document.getElementById("ordersSection"),
        customersSection: document.getElementById("customersSection"),
        loyaltySection: document.getElementById("loyaltySection"),
        deliverySection: document.getElementById("deliverySection"),
        homeSection: document.getElementById("homeSection"),
        categoriesSection: document.getElementById("categoriesSection"),
        productsSection: document.getElementById("productsSection")
    }
};

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    boot();
});

function bindEvents() {
    elements.loginForm.addEventListener("submit", onLoginSubmit);
    elements.logoutBtn.addEventListener("click", logout);
    elements.refreshAllBtn.addEventListener("click", () => loadAdminData(true));
    elements.categoryForm.addEventListener("submit", onCategorySubmit);
    elements.productForm.addEventListener("submit", onProductSubmit);
    elements.resetCategoryFormBtn.addEventListener("click", resetCategoryForm);
    elements.resetProductFormBtn.addEventListener("click", resetProductForm);
    elements.filterProductsBtn.addEventListener("click", () => loadProducts(true));
    elements.resetProductFiltersBtn.addEventListener("click", resetProductFilters);
    elements.refreshOrdersBtn.addEventListener("click", () => loadOrders(true));
    elements.refreshCustomersBtn.addEventListener("click", () => loadCustomers(true));
    elements.refreshLoyaltyBtn.addEventListener("click", () => loadLoyaltyAdmin(true));
    elements.loyaltyPointsForm.addEventListener("submit", onLoyaltyPointsSubmit);
    elements.loyaltyVoucherForm.addEventListener("submit", onLoyaltyVoucherSubmit);
    elements.loyaltyGiftForm.addEventListener("submit", onLoyaltyGiftSubmit);
    if (elements.loyaltyGiftCancelEditBtn) {
        elements.loyaltyGiftCancelEditBtn.addEventListener("click", resetLoyaltyGiftForm);
    }
    elements.deliverySettingsForm.addEventListener("submit", onDeliverySettingsSubmit);
    elements.homeSettingsForm.addEventListener("submit", onHomeSettingsSubmit);
    elements.refreshHomeSettingsBtn.addEventListener("click", () => loadHomeSettings(true));
    document.getElementById("productPrice").addEventListener("input", syncDiscountFromPrices);
    document.getElementById("productOldPrice").addEventListener("input", syncDiscountFromPrices);
    elements.uploadCategoryImageBtn.addEventListener("click", () => onUploadImage("category"));
    elements.uploadProductImageBtn.addEventListener("click", () => onUploadImage("product"));
    elements.categoryImageUrl.addEventListener("input", () => renderImagePreview("category"));
    elements.productImageUrl.addEventListener("input", () => renderImagePreview("product"));
    document.getElementById("categoryImageFile").addEventListener("change", () => previewLocalFile("category"));
    document.getElementById("productImageFile").addEventListener("change", () => previewLocalFile("product"));
    elements.navLinks.forEach((button) => {
        button.addEventListener("click", () => showSection(button.dataset.section));
    });
    elements.jumpButtons.forEach((button) => {
        button.addEventListener("click", () => showSection(button.dataset.sectionTarget));
    });

    if (elements.loyaltyCustomerQuery) {
        let searchTimer = null;
        elements.loyaltyCustomerQuery.addEventListener("input", () => {
            window.clearTimeout(searchTimer);
            searchTimer = window.setTimeout(() => {
                searchLoyaltyCustomers(elements.loyaltyCustomerQuery.value, false);
            }, 250);
        });
    }
}

async function boot() {
    if (forceFreshLoginOnLoad) {
        clearAuthState();
    }

    if (state.token && isTokenExpiredOrInvalid(state.token)) {
        clearAuthState();
    }

    if (!state.token) {
        showLogin();
        return;
    }

    try {
        await loadProfile();
        await loadAdminData();
        showApp();
    } catch (error) {
        logout();
        if (!isAuthError(error)) {
            showToast(error.message || "Session invalide");
        }
    }
}

async function onLoginSubmit(event) {
    event.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    try {
        const response = await fetchJson("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password }),
            headers: {
                Authorization: ""
            }
        });

        state.token = response.token;
        localStorage.setItem(tokenKey, state.token);

        await loadProfile();
        await loadAdminData();
        showApp();
        showToast("Connexion reussie");
    } catch (error) {
        if (isAuthError(error)) {
            showToast("Email ou mot de passe invalide");
            return;
        }
        showToast(error.message || "Connexion impossible");
    }
}

async function loadProfile() {
    const me = await fetchJson("/api/users/me");
    if (!me.roles || !me.roles.includes("ADMIN")) {
        throw new Error("Ce compte n'est pas admin");
    }
    state.me = me;
    elements.adminIdentity.textContent = `${me.fullName} · ${me.email}`;
}

async function loadAdminData(withToast = false) {
    await Promise.all([
        loadDashboardSummary(),
        loadCategories(),
        loadProducts(),
        loadOrders(),
        loadCustomers(),
        loadLoyaltyAdmin(),
        loadDeliverySettings(),
        loadHomeSettings()
    ]);
    updateStats();
    if (withToast) {
        showToast("Donnees actualisees");
    }
}

async function loadDashboardSummary() {
    state.dashboardSummary = await fetchJson("/api/admin/dashboard/summary");
}

async function loadCategories() {
    const page = await fetchJson("/api/admin/categories?page=0&size=100&sort=sortOrder,asc&sort=name,asc");
    state.categories = page.content || [];
    syncExpandedItems("category");
    renderCategoryOptions();
    renderCategories();
}

async function loadProducts(withToast = false) {
    const params = new URLSearchParams({
        page: "0",
        size: "100",
        sort: "updatedAt,desc"
    });

    const query = elements.productSearch.value.trim();
    const categoryId = elements.productCategoryFilter.value;
    const active = elements.productActiveFilter.value;
    const promoOnly = elements.productPromoFilter.value;
    const bioOnly = elements.productBioFilter.value;
    const newOnly = elements.productNewFilter.value;
    const popularOnly = elements.productPopularFilter.value;
    const minDiscountPct = elements.productMinDiscountFilter.value;
    const maxDiscountPct = elements.productMaxDiscountFilter.value;

    if (query) {
        params.set("query", query);
    }
    if (categoryId) {
        params.set("categoryId", categoryId);
    }
    if (active) {
        params.set("active", active);
    }
    if (promoOnly) {
        params.set("promoOnly", promoOnly);
    }
    if (bioOnly) {
        params.set("bioOnly", bioOnly);
    }
    if (newOnly) {
        params.set("newOnly", newOnly);
    }
    if (popularOnly) {
        params.set("popularOnly", popularOnly);
    }
    if (minDiscountPct) {
        params.set("minDiscountPct", minDiscountPct);
    }
    if (maxDiscountPct) {
        params.set("maxDiscountPct", maxDiscountPct);
    }

    const page = await fetchJson(`/api/admin/products?${params.toString()}`);
    state.products = page.content || [];
    syncExpandedItems("product");
    renderProducts();
    if (withToast) {
        showToast("Liste produits mise a jour");
    }
}

async function loadOrders(withToast = false) {
    const page = await fetchJson("/api/admin/orders?page=0&size=100&sort=createdAt,desc");
    state.orders = page.content || [];
    renderOrders();

    if (state.selectedOrderId) {
        await loadOrderDetail(state.selectedOrderId, false);
    }

    if (withToast) {
        showToast("Commandes actualisees");
    }
}

async function loadOrderDetail(id, withToast = false) {
    try {
        const order = await fetchJson(`/api/admin/orders/${id}`);
        state.selectedOrderId = id;
        renderOrderDetail(order);
        renderOrders();
        if (withToast) {
            showToast("Commande chargee");
        }
    } catch (error) {
        showToast(error.message || "Impossible de charger la commande");
    }
}

async function loadCustomers(withToast = false) {
    const page = await fetchJson("/api/admin/customers?page=0&size=100&sort=id,desc");
    state.customers = page.content || [];
    renderCustomers();

    if (state.selectedCustomerId) {
        await loadCustomerDetail(state.selectedCustomerId, false);
    }

    if (withToast) {
        showToast("Clients actualises");
    }
}

async function loadLoyaltyAdmin(withToast = false) {
    await Promise.all([
        searchLoyaltyCustomers(elements.loyaltyCustomerQuery?.value, false),
        loadLoyaltyGifts(false)
    ]);
    if (state.selectedLoyaltyCustomerId) {
        await loadLoyaltyAccount(state.selectedLoyaltyCustomerId);
    }
    if (withToast) {
        showToast("Fidelite actualisee");
    }
}

async function loadLoyaltyGifts(withToast = false) {
    state.loyaltyGifts = await fetchJson("/api/admin/loyalty/gifts");
    renderLoyaltyGifts();
    if (withToast) {
        showToast("Cadeaux actualises");
    }
}

async function searchLoyaltyCustomers(query, withToast = false) {
    const trimmed = (query ?? "").trim();
    const qs = trimmed ? `?query=${encodeURIComponent(trimmed)}` : "";
    state.loyaltyCustomers = await fetchJson(`/api/admin/loyalty/customers${qs}`);
    renderLoyaltyCustomerResults();
    if (withToast) {
        showToast("Clients fidelite actualises");
    }
}

async function loadLoyaltyAccount(customerId) {
    if (!customerId) {
        state.selectedLoyaltyAccount = null;
        renderLoyaltyAccountPreview();
        return;
    }
    try {
        state.selectedLoyaltyAccount = await fetchJson(`/api/admin/loyalty/customers/${customerId}`);
        renderLoyaltyAccountPreview();
    } catch (error) {
        showToast(error.message || "Impossible de charger la carte fidelite");
    }
}

async function loadCustomerDetail(id, withToast = false) {
    try {
        const customer = await fetchJson(`/api/admin/customers/${id}`);
        state.selectedCustomerId = id;
        renderCustomerDetail(customer);
        renderCustomers();
        if (withToast) {
            showToast("Client charge");
        }
    } catch (error) {
        showToast(error.message || "Impossible de charger le client");
    }
}

async function loadDeliverySettings() {
    state.deliverySettings = await fetchJson("/api/admin/delivery/settings");
    populateDeliverySettingsForm(state.deliverySettings);
    renderDeliverySettingsPreview(state.deliverySettings);
}

async function loadHomeSettings(withToast = false) {
    state.homeSettings = await fetchJson("/api/admin/home/settings");
    populateHomeSettingsForm(state.homeSettings);
    renderHomeSettingsPreview(state.homeSettings);
    if (withToast) {
        showToast("Reglages Home actualises");
    }
}

async function onCategorySubmit(event) {
    event.preventDefault();

    const id = document.getElementById("categoryId").value;
    const payload = {
        name: document.getElementById("categoryName").value.trim(),
        key: document.getElementById("categoryKey").value.trim(),
        displayName: document.getElementById("categoryDisplayName").value.trim(),
        imageUrl: document.getElementById("categoryImageUrl").value.trim(),
        sortOrder: Number(document.getElementById("categorySortOrder").value || 0),
        isActive: document.getElementById("categoryActive").checked,
        isPromo: document.getElementById("categoryPromo").checked,
        isBio: document.getElementById("categoryBio").checked,
        isNew: document.getElementById("categoryNew").checked,
        isPopular: document.getElementById("categoryPopular").checked,
        customTags: parseTags(document.getElementById("categoryCustomTags").value)
    };

    try {
        await fetchJson(id ? `/api/admin/categories/${id}` : "/api/admin/categories", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        resetCategoryForm();
        await loadCategories();
        updateStats();
        showToast(id ? "Categorie mise a jour" : "Categorie ajoutee");
    } catch (error) {
        showToast(error.message || "Erreur categorie");
    }
}

async function onProductSubmit(event) {
    event.preventDefault();

    const id = document.getElementById("productId").value;
    const oldPriceValue = document.getElementById("productOldPrice").value;
    const payload = {
        name: document.getElementById("productName").value.trim(),
        description: document.getElementById("productDescription").value.trim(),
        price: Number(document.getElementById("productPrice").value),
        oldPrice: oldPriceValue ? Number(oldPriceValue) : null,
        discountPct: Number(document.getElementById("productDiscountPct").value || 0),
        categoryId: Number(document.getElementById("productCategoryId").value),
        imageUrl: document.getElementById("productImageUrl").value.trim(),
        stockQty: Number(document.getElementById("productStockQty").value || 0),
        isActive: document.getElementById("productActive").checked,
        isPromo: document.getElementById("productPromo").checked,
        isBio: document.getElementById("productBio").checked,
        isNew: document.getElementById("productNew").checked,
        isPopular: document.getElementById("productPopular").checked,
        customTags: parseTags(document.getElementById("productCustomTags").value),
        promoLabel: document.getElementById("productPromoLabel").value.trim(),
        promoStartsAt: localDateTimeToInstant(document.getElementById("productPromoStartsAt").value),
        promoEndsAt: localDateTimeToInstant(document.getElementById("productPromoEndsAt").value),
        rating: Number(document.getElementById("productRating").value || 0),
        salesCount: Number(document.getElementById("productSalesCount").value || 0)
    };

    try {
        await fetchJson(id ? `/api/admin/products/${id}` : "/api/admin/products", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        resetProductForm();
        await loadProducts();
        updateStats();
        showToast(id ? "Produit mis a jour" : "Produit ajoute");
    } catch (error) {
        showToast(error.message || "Erreur produit");
    }
}

async function onLoyaltyPointsSubmit(event) {
    event.preventDefault();
    const customerId = Number(elements.loyaltyCustomerId.value);
    if (!customerId) {
        showToast("Choisissez un client");
        return;
    }

    try {
        state.selectedLoyaltyAccount = await fetchJson(`/api/admin/loyalty/customers/${customerId}/points`, {
            method: "POST",
            body: JSON.stringify({
                title: document.getElementById("loyaltyPointsTitle").value.trim(),
                points: Number(document.getElementById("loyaltyPointsValue").value)
            })
        });
        elements.loyaltyPointsForm.reset();
        renderLoyaltyAccountPreview();
        showToast("Points fidelite enregistres");
    } catch (error) {
        showToast(error.message || "Impossible d'enregistrer les points");
    }
}

async function onLoyaltyVoucherSubmit(event) {
    event.preventDefault();
    const customerId = Number(elements.loyaltyCustomerId.value);
    if (!customerId) {
        showToast("Choisissez un client");
        return;
    }

    try {
        await fetchJson(`/api/admin/loyalty/customers/${customerId}/vouchers`, {
            method: "POST",
            body: JSON.stringify({
                title: document.getElementById("loyaltyVoucherTitle").value.trim(),
                code: document.getElementById("loyaltyVoucherCode").value.trim(),
                description: document.getElementById("loyaltyVoucherDescription").value.trim(),
                expiresAt: localDateTimeToInstant(document.getElementById("loyaltyVoucherExpiresAt").value)
            })
        });
        elements.loyaltyVoucherForm.reset();
        showToast("Bon client ajoute");
    } catch (error) {
        showToast(error.message || "Impossible d'ajouter le bon");
    }
}

async function onLoyaltyGiftSubmit(event) {
    event.preventDefault();

    try {
        const giftId = Number(document.getElementById("loyaltyGiftId").value || 0);
        const payload = {
            title: document.getElementById("loyaltyGiftTitle").value.trim(),
            points: Number(document.getElementById("loyaltyGiftPoints").value),
            active: document.getElementById("loyaltyGiftActive").checked,
            sortOrder: Number(document.getElementById("loyaltyGiftSortOrder").value || 0)
        };

        await fetchJson(giftId ? `/api/admin/loyalty/gifts/${giftId}` : "/api/admin/loyalty/gifts", {
            method: giftId ? "PUT" : "POST",
            body: JSON.stringify({
                ...payload
            })
        });
        resetLoyaltyGiftForm();
        await loadLoyaltyGifts();
        showToast(giftId ? "Cadeau modifie" : "Cadeau ajoute");
    } catch (error) {
        showToast(error.message || "Impossible d'enregistrer le cadeau");
    }
}

function resetLoyaltyGiftForm() {
    if (!elements.loyaltyGiftForm) {
        return;
    }
    elements.loyaltyGiftForm.reset();
    document.getElementById("loyaltyGiftId").value = "";
    document.getElementById("loyaltyGiftActive").checked = true;
    document.getElementById("loyaltyGiftSortOrder").value = "0";
    if (elements.loyaltyGiftSubmitBtn) {
        elements.loyaltyGiftSubmitBtn.textContent = "Ajouter cadeau";
    }
    if (elements.loyaltyGiftCancelEditBtn) {
        elements.loyaltyGiftCancelEditBtn.classList.add("hidden");
    }
}

async function onUploadImage(kind) {
    const fileInput = document.getElementById(kind === "category" ? "categoryImageFile" : "productImageFile");
    const urlInput = document.getElementById(kind === "category" ? "categoryImageUrl" : "productImageUrl");
    const uploadButton = kind === "category" ? elements.uploadCategoryImageBtn : elements.uploadProductImageBtn;
    const file = fileInput.files[0];

    if (!file) {
        showToast("Choisissez une image d'abord");
        return;
    }

    if (!file.type || !file.type.startsWith("image/")) {
        showToast("Le fichier doit etre une image");
        return;
    }

    if (file.size > maxUploadSizeBytes) {
        showToast("Image trop grande. Taille max 15 MB");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
        uploadButton.disabled = true;
        uploadButton.textContent = "Upload...";
        const response = await fetch("/api/admin/uploads/image", {
            method: "POST",
            headers: authHeaders(false),
            body: formData
        });
        const rawBody = await response.text();
        let body = {};
        if (rawBody) {
            try {
                body = JSON.parse(rawBody);
            } catch (_) {
                body = { message: rawBody };
            }
        }
        if (!response.ok) {
            throw new Error(body.message || body.error || "Upload impossible");
        }
        urlInput.value = body.url;
        renderImagePreview(kind);
        fileInput.value = "";
        showToast("Image envoyee");
    } catch (error) {
        showToast(error.message || "Erreur upload");
    } finally {
        uploadButton.disabled = false;
        uploadButton.textContent = "Uploader image";
    }
}

function renderCategories() {
    if (!state.categories.length) {
        elements.categoryList.innerHTML = `<p class="muted">Aucune categorie pour le moment.</p>`;
        return;
    }

    elements.categoryList.innerHTML = state.categories.map((category) => `
        <article class="item-card ${isExpanded("category", category.id) ? "expanded" : ""}">
            <div class="item-top">
                <div>
                    <h3>${escapeHtml(category.displayName || category.name)}</h3>
                    <p class="item-meta">Cle: ${escapeHtml(category.key || "-")} · Ordre: ${category.sortOrder ?? 0}</p>
                    <div class="item-badges">
                        <span class="badge ${isActiveFlag(category) ? "active" : "inactive"}">${isActiveFlag(category) ? "Active" : "Inactive"}</span>
                        ${category.isPromo ? `<span class="badge promo">Promo</span>` : ""}
                        ${category.isBio ? `<span class="badge">Bio</span>` : ""}
                        ${category.isNew ? `<span class="badge">Nouveau</span>` : ""}
                        ${category.isPopular ? `<span class="badge">Mis en avant</span>` : ""}
                        ${(category.customTags || []).map((tag) => `<span class="badge custom">${escapeHtml(tag)}</span>`).join("")}
                        ${(category.tags || []).map((tag) => `<span class="badge">${escapeHtml(tag)}</span>`).join("")}
                    </div>
                </div>
                <div class="item-header-actions">
                    ${category.imageUrl ? `<img class="thumb" src="${escapeHtml(category.imageUrl)}" alt="">` : ""}
                    <button
                        type="button"
                        class="item-toggle ghost"
                        data-action="toggle-category-details"
                        data-id="${category.id}"
                        aria-expanded="${isExpanded("category", category.id)}"
                    >
                        <span class="item-toggle-icon" aria-hidden="true">${isExpanded("category", category.id) ? "v" : ">"}</span>
                        <span>${isExpanded("category", category.id) ? "Masquer" : "Afficher"}</span>
                    </button>
                </div>
            </div>
            <div class="item-details ${isExpanded("category", category.id) ? "" : "hidden"}">
                <div class="item-stats">
                    <div class="mini-stat"><span>Produits</span><strong>${category.productCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Actifs</span><strong>${category.activeProductCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Promos</span><strong>${category.promoProductCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Bio</span><strong>${category.bioProductCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Nouveau</span><strong>${category.newProductCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Mis en avant</span><strong>${category.popularProductCount ?? 0}</strong></div>
                    <div class="mini-stat"><span>Stock</span><strong>${category.totalStockQty ?? 0}</strong></div>
                    <div class="mini-stat"><span>Remise max</span><strong>${category.maxDiscountPct ?? 0}%</strong></div>
                    <div class="mini-stat"><span>Prix min</span><strong>${formatMoney(category.minPrice)}</strong></div>
                    <div class="mini-stat"><span>Prix max</span><strong>${formatMoney(category.maxPrice)}</strong></div>
                    <div class="mini-stat"><span>Note moy.</span><strong>${formatRating(category.averageRating)}</strong></div>
                </div>
            </div>
            <div class="item-actions">
                <button type="button" class="ghost" data-action="edit-category" data-id="${category.id}">Modifier</button>
                <button type="button" class="danger" data-action="delete-category" data-id="${category.id}">Supprimer</button>
            </div>
        </article>
    `).join("");

    elements.categoryList.querySelectorAll("[data-action='toggle-category-details']").forEach((button) => {
        button.addEventListener("click", () => toggleItemDetails("category", Number(button.dataset.id)));
    });
    elements.categoryList.querySelectorAll("[data-action='edit-category']").forEach((button) => {
        button.addEventListener("click", () => populateCategoryForm(Number(button.dataset.id)));
    });
    elements.categoryList.querySelectorAll("[data-action='delete-category']").forEach((button) => {
        button.addEventListener("click", () => deleteCategory(Number(button.dataset.id)));
    });
}

function renderProducts() {
    if (!state.products.length) {
        elements.productList.innerHTML = `<p class="muted">Aucun produit trouve.</p>`;
        return;
    }

    elements.productList.innerHTML = state.products.map((product) => `
        <article class="item-card ${isExpanded("product", product.id) ? "expanded" : ""}">
            <div class="item-top">
                <div>
                    <h3>${escapeHtml(product.name)}</h3>
                    <p class="item-meta">${escapeHtml(product.displayCategoryName || product.categoryName || "-")} · ${formatMoney(product.price)} · Stock ${product.stockQty}</p>
                    <div class="item-badges">
                        <span class="badge ${isActiveFlag(product) ? "active" : "inactive"}">${isActiveFlag(product) ? "Actif" : "Inactif"}</span>
                        ${product.discountPct > 0 ? `<span class="badge promo">Promo ${product.discountPct}%</span>` : ""}
                        ${product.isBio ? `<span class="badge">Bio</span>` : ""}
                        ${product.isNew ? `<span class="badge">Nouveau</span>` : ""}
                        ${product.isPopular ? `<span class="badge">Mis en avant</span>` : ""}
                        ${(product.customTags || []).map((tag) => `<span class="badge custom">${escapeHtml(tag)}</span>`).join("")}
                    </div>
                </div>
                <div class="item-header-actions">
                    ${product.imageUrl ? `<img class="thumb" src="${escapeHtml(product.imageUrl)}" alt="">` : ""}
                    <button
                        type="button"
                        class="item-toggle ghost"
                        data-action="toggle-product-details"
                        data-id="${product.id}"
                        aria-expanded="${isExpanded("product", product.id)}"
                    >
                        <span class="item-toggle-icon" aria-hidden="true">${isExpanded("product", product.id) ? "v" : ">"}</span>
                        <span>${isExpanded("product", product.id) ? "Masquer" : "Afficher"}</span>
                    </button>
                </div>
            </div>
            <div class="item-details ${isExpanded("product", product.id) ? "" : "hidden"}">
                <p class="item-meta">${escapeHtml(product.description || "Aucune description.")}</p>
            </div>
            <div class="item-actions">
                <button type="button" class="ghost" data-action="edit-product" data-id="${product.id}">Modifier</button>
                <button type="button" class="danger" data-action="delete-product" data-id="${product.id}">Supprimer</button>
            </div>
        </article>
    `).join("");

    elements.productList.querySelectorAll("[data-action='toggle-product-details']").forEach((button) => {
        button.addEventListener("click", () => toggleItemDetails("product", Number(button.dataset.id)));
    });
    elements.productList.querySelectorAll("[data-action='edit-product']").forEach((button) => {
        button.addEventListener("click", () => populateProductForm(Number(button.dataset.id)));
    });
    elements.productList.querySelectorAll("[data-action='delete-product']").forEach((button) => {
        button.addEventListener("click", () => deleteProduct(Number(button.dataset.id)));
    });
}

function renderOrders() {
    if (!state.orders.length) {
        elements.orderList.innerHTML = `<p class="muted">Aucune commande pour le moment.</p>`;
        return;
    }

    elements.orderList.innerHTML = state.orders.map((order) => `
        <article class="item-card ${state.selectedOrderId === order.id ? "selected-card" : ""}">
            <div class="item-top">
                <div>
                    <h3>${escapeHtml(order.reference || `Commande #${order.id}`)}</h3>
                    <p class="item-meta">${escapeHtml(order.customerName || "-")} · ${formatMoney(order.total)} · ${formatDateTime(order.createdAt)}</p>
                    <div class="item-badges">
                        <span class="badge status-${statusTone(order.status)}">${escapeHtml(order.status || "-")}</span>
                        <span class="badge">${escapeHtml(order.paymentMethod || "-")}</span>
                        <span class="badge">${escapeHtml(order.deliverySlot || "-")}</span>
                    </div>
                </div>
                <button type="button" class="ghost" data-action="view-order" data-id="${order.id}">Ouvrir</button>
            </div>
        </article>
    `).join("");

    elements.orderList.querySelectorAll("[data-action='view-order']").forEach((button) => {
        button.addEventListener("click", () => loadOrderDetail(Number(button.dataset.id), false));
    });
}

function renderOrderDetail(order) {
    const statusOptions = ["PENDING", "CONFIRMED", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED"]
        .map((status) => `<option value="${status}" ${order.status === status ? "selected" : ""}>${status}</option>`)
        .join("");

    elements.orderDetail.classList.remove("muted");
    elements.orderDetail.innerHTML = `
        <div class="detail-grid">
            <div class="detail-box">
                <span class="detail-label">Reference</span>
                <strong>${escapeHtml(order.reference || `Commande #${order.id}`)}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Client</span>
                <strong>${escapeHtml(order.customerName || "-")}</strong>
                <small>${escapeHtml(order.customerPhone || "-")} · ${escapeHtml(order.customerEmail || "-")}</small>
            </div>
            <div class="detail-box">
                <span class="detail-label">Adresse</span>
                <strong>${escapeHtml(joinAddress(order))}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Montants</span>
                <strong>${formatMoney(order.total)}</strong>
                <small>Sous-total ${formatMoney(order.subtotal)} · Livraison ${formatMoney(order.deliveryFee)}</small>
            </div>
        </div>
        <div class="detail-actions">
            <label>
                <span>Statut</span>
                <select id="orderStatusSelect">${statusOptions}</select>
            </label>
            <button type="button" id="saveOrderStatusBtn" class="primary">Enregistrer statut</button>
        </div>
        <div class="detail-actions">
            <label>
                <span>Livreur</span>
                <input id="orderCourierNameInput" type="text" value="${escapeHtmlAttr(order.courierName || "")}">
            </label>
            <label>
                <span>Telephone livreur</span>
                <input id="orderCourierPhoneInput" type="text" value="${escapeHtmlAttr(order.courierPhone || "")}">
            </label>
            <button type="button" id="saveOrderCourierBtn" class="ghost">Mettre a jour livreur</button>
        </div>
        <div class="detail-box">
            <span class="detail-label">Articles</span>
            <div class="detail-lines">
                ${(order.items || []).map((item) => `
                    <div class="detail-line">
                        <span>${escapeHtml(item.productName || "-")} x ${item.quantity ?? 0}</span>
                        <strong>${formatMoney(item.lineTotal)}</strong>
                    </div>
                `).join("") || `<p class="muted">Aucun article.</p>`}
            </div>
        </div>
    `;

    document.getElementById("saveOrderStatusBtn").addEventListener("click", async () => {
        try {
            const updated = await fetchJson(`/api/admin/orders/${order.id}/status`, {
                method: "PATCH",
                body: JSON.stringify({ status: document.getElementById("orderStatusSelect").value })
            });
            await loadOrders();
            renderOrderDetail(updated);
            updateStats();
            showToast("Statut commande mis a jour");
        } catch (error) {
            showToast(error.message || "Impossible de changer le statut");
        }
    });

    document.getElementById("saveOrderCourierBtn").addEventListener("click", async () => {
        try {
            const updated = await fetchJson(`/api/admin/orders/${order.id}/courier`, {
                method: "PATCH",
                body: JSON.stringify({
                    courierName: document.getElementById("orderCourierNameInput").value.trim(),
                    courierPhone: document.getElementById("orderCourierPhoneInput").value.trim()
                })
            });
            await loadOrders();
            renderOrderDetail(updated);
            showToast("Livreur mis a jour");
        } catch (error) {
            showToast(error.message || "Impossible de changer le livreur");
        }
    });
}

function renderCustomers() {
    if (!state.customers.length) {
        elements.customerList.innerHTML = `<p class="muted">Aucun client pour le moment.</p>`;
        return;
    }

    elements.customerList.innerHTML = state.customers.map((customer) => `
        <article class="item-card ${state.selectedCustomerId === customer.id ? "selected-card" : ""}">
            <div class="item-top">
                <div>
                    <h3>${escapeHtml(customer.fullName || "-")}</h3>
                    <p class="item-meta">${escapeHtml(customer.email || "-")} · ${escapeHtml(customer.phone || "-")}</p>
                    <div class="item-badges">
                        <span class="badge ${customer.enabled ? "active" : "inactive"}">${customer.enabled ? "Actif" : "Desactive"}</span>
                        <span class="badge">${customer.ordersCount ?? 0} commandes</span>
                        <span class="badge">${formatMoney(customer.totalSpent)}</span>
                    </div>
                </div>
                <button type="button" class="ghost" data-action="view-customer" data-id="${customer.id}">Voir fiche</button>
            </div>
        </article>
    `).join("");

    elements.customerList.querySelectorAll("[data-action='view-customer']").forEach((button) => {
        button.addEventListener("click", () => loadCustomerDetail(Number(button.dataset.id), false));
    });
}

function renderCustomerDetail(customer) {
    elements.customerDetail.classList.remove("muted");
    elements.customerDetail.innerHTML = `
        <div class="detail-grid">
            <div class="detail-box">
                <span class="detail-label">Nom</span>
                <strong>${escapeHtml(customer.fullName || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Email</span>
                <strong>${escapeHtml(customer.email || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Telephone</span>
                <strong>${escapeHtml(customer.phone || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Roles</span>
                <strong>${escapeHtml((customer.roles || []).join(", ") || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Adresse</span>
                <strong>${escapeHtml(customer.address || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Historique</span>
                <strong>${customer.ordersCount ?? 0} commandes</strong>
                <small>Total depense ${formatMoney(customer.totalSpent)} · Derniere ${formatDateTime(customer.lastOrderAt)}</small>
            </div>
        </div>
    `;
}

function renderLoyaltyCustomerResults() {
    if (!elements.loyaltyCustomerResults) {
        return;
    }

    if (!state.loyaltyCustomers.length) {
        elements.loyaltyCustomerResults.innerHTML = `<p class="muted">Aucun client trouve.</p>`;
        return;
    }

    elements.loyaltyCustomerResults.innerHTML = state.loyaltyCustomers.map((customer) => {
        const isSelected = state.selectedLoyaltyCustomerId === customer.customerId;
        const subtitle = [customer.customerEmail, customer.cardNumber].filter(Boolean).join(" · ");
        const points = customer.pointsBalance ?? 0;
        return `
            <article class="item-card ${isSelected ? "selected" : ""}">
                <div class="item-top">
                    <div>
                        <h3>${escapeHtml(customer.customerName || `Client #${customer.customerId}`)}</h3>
                        <p class="item-meta">${escapeHtml(subtitle || "-")}</p>
                        <div class="item-badges">
                            <span class="badge custom">${points} pts</span>
                        </div>
                    </div>
                    <button type="button" class="ghost" data-action="select-loyalty-customer" data-id="${customer.customerId}">Choisir</button>
                </div>
            </article>
        `;
    }).join("");

    elements.loyaltyCustomerResults.querySelectorAll("[data-action='select-loyalty-customer']").forEach((button) => {
        button.addEventListener("click", () => selectLoyaltyCustomer(Number(button.dataset.id)));
    });
}

async function selectLoyaltyCustomer(customerId) {
    state.selectedLoyaltyCustomerId = customerId || null;
    if (elements.loyaltyCustomerId) {
        elements.loyaltyCustomerId.value = customerId ? String(customerId) : "";
    }
    renderLoyaltyCustomerResults();
    await loadLoyaltyAccount(customerId);
}

function renderLoyaltyAccountPreview() {
    const account = state.selectedLoyaltyAccount;
    if (!account) {
        elements.loyaltyAccountPreview.classList.add("muted");
        elements.loyaltyAccountPreview.innerHTML = "Recherchez puis selectionnez un client.";
        return;
    }

    elements.loyaltyAccountPreview.classList.remove("muted");
    elements.loyaltyAccountPreview.innerHTML = `
        <div class="detail-grid">
            <div class="detail-box">
                <span class="detail-label">Client</span>
                <strong>${escapeHtml(account.customerName || "-")}</strong>
                <small>${escapeHtml(account.customerEmail || "-")}</small>
            </div>
            <div class="detail-box">
                <span class="detail-label">Carte</span>
                <strong>${escapeHtml(account.cardNumber || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Solde points</span>
                <strong>${account.pointsBalance ?? 0}</strong>
            </div>
        </div>
    `;
}

function renderLoyaltyGifts() {
    if (!state.loyaltyGifts.length) {
        elements.loyaltyGiftList.innerHTML = `<p class="muted">Aucun cadeau pour le moment.</p>`;
        return;
    }

    elements.loyaltyGiftList.innerHTML = state.loyaltyGifts.map((gift) => `
        <article class="item-card">
            <div class="item-top">
                <div>
                    <h3>${escapeHtml(gift.title || "-")}</h3>
                    <p class="item-meta">${gift.points ?? 0} points requis · ordre ${gift.sortOrder ?? 0}</p>
                    <div class="item-badges">
                        <span class="badge ${gift.active ? "active" : "inactive"}">${gift.active ? "Actif" : "Inactif"}</span>
                    </div>
                </div>
                <div class="row-actions">
                    <button type="button" class="ghost" data-action="edit-loyalty-gift" data-id="${gift.id}">Modifier</button>
                    <button type="button" class="ghost" data-action="toggle-loyalty-gift" data-id="${gift.id}">
                        ${gift.active ? "Desactiver" : "Activer"}
                    </button>
                </div>
            </div>
        </article>
    `).join("");

    elements.loyaltyGiftList.querySelectorAll("[data-action='edit-loyalty-gift']").forEach((button) => {
        button.addEventListener("click", () => startEditLoyaltyGift(Number(button.dataset.id)));
    });
    elements.loyaltyGiftList.querySelectorAll("[data-action='toggle-loyalty-gift']").forEach((button) => {
        button.addEventListener("click", () => toggleLoyaltyGift(Number(button.dataset.id)));
    });
}

function startEditLoyaltyGift(giftId) {
    const gift = state.loyaltyGifts.find((g) => g.id === giftId);
    if (!gift) {
        showToast("Cadeau introuvable");
        return;
    }
    document.getElementById("loyaltyGiftId").value = String(gift.id);
    document.getElementById("loyaltyGiftTitle").value = gift.title || "";
    document.getElementById("loyaltyGiftPoints").value = String(gift.points ?? 1);
    document.getElementById("loyaltyGiftSortOrder").value = String(gift.sortOrder ?? 0);
    document.getElementById("loyaltyGiftActive").checked = !!gift.active;
    if (elements.loyaltyGiftSubmitBtn) {
        elements.loyaltyGiftSubmitBtn.textContent = "Modifier cadeau";
    }
    if (elements.loyaltyGiftCancelEditBtn) {
        elements.loyaltyGiftCancelEditBtn.classList.remove("hidden");
    }
    elements.loyaltyGiftForm.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function toggleLoyaltyGift(giftId) {
    const gift = state.loyaltyGifts.find((g) => g.id === giftId);
    if (!gift) {
        showToast("Cadeau introuvable");
        return;
    }

    try {
        await fetchJson(`/api/admin/loyalty/gifts/${giftId}`, {
            method: "PUT",
            body: JSON.stringify({
                title: (gift.title || "").trim(),
                points: gift.points ?? 1,
                active: !gift.active,
                sortOrder: gift.sortOrder ?? 0
            })
        });
        await loadLoyaltyGifts();
        showToast(!gift.active ? "Cadeau active" : "Cadeau desactive");
    } catch (error) {
        showToast(error.message || "Impossible de changer le statut du cadeau");
    }
}

function populateDeliverySettingsForm(settings) {
    document.getElementById("deliveryCourierName").value = settings?.courierName || "";
    document.getElementById("deliveryCourierPhone").value = settings?.courierPhone || "";
    document.getElementById("deliveryStorePhone").value = settings?.storePhone || "";
    document.getElementById("deliveryFee").value = settings?.deliveryFee ?? "0";
    document.getElementById("deliveryEtaLabel").value = settings?.etaLabel || "";
}

function renderDeliverySettingsPreview(settings) {
    if (!settings) {
        elements.deliveryPreview.classList.add("muted");
        elements.deliveryPreview.textContent = "Les parametres livraison apparaitront ici.";
        return;
    }

    elements.deliveryPreview.classList.remove("muted");
    elements.deliveryPreview.innerHTML = `
        <div class="detail-grid">
            <div class="detail-box">
                <span class="detail-label">Livreur</span>
                <strong>${escapeHtml(settings.courierName || "-")}</strong>
                <small>${escapeHtml(settings.courierPhone || "-")}</small>
            </div>
            <div class="detail-box">
                <span class="detail-label">Magasin</span>
                <strong>${escapeHtml(settings.storePhone || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Frais</span>
                <strong>${formatMoney(settings.deliveryFee)}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">ETA</span>
                <strong>${escapeHtml(settings.etaLabel || "-")}</strong>
            </div>
        </div>
    `;
}

function populateHomeSettingsForm(settings) {
    document.getElementById("homeLocationLabel").value = settings?.locationLabel || "";
    document.getElementById("homeEtaLabel").value = settings?.etaLabel || "";
    document.getElementById("homeDeliveryAreas").value = (settings?.deliveryAreas || []).join(", ");
}

function renderHomeSettingsPreview(settings) {
    if (!settings) {
        elements.homeSettingsPreview.classList.add("muted");
        elements.homeSettingsPreview.textContent = "Les reglages Home apparaitront ici.";
        return;
    }

    elements.homeSettingsPreview.classList.remove("muted");
    elements.homeSettingsPreview.innerHTML = `
        <div class="detail-grid">
            <div class="detail-box">
                <span class="detail-label">Localisation</span>
                <strong>${escapeHtml(settings.locationLabel || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">ETA</span>
                <strong>${escapeHtml(settings.etaLabel || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Zones</span>
                <strong>${escapeHtml((settings.deliveryAreas || []).join(", ") || "-")}</strong>
            </div>
            <div class="detail-box">
                <span class="detail-label">Sources Home</span>
                <small>Catalogues = categories actives · Offres = produits promo actifs avec dates valides</small>
            </div>
        </div>
    `;
}

function renderCategoryOptions() {
    const selectedFilter = elements.productCategoryFilter.value;
    const selectedFormValue = document.getElementById("productCategoryId").value;
    const options = ['<option value="">Toutes les categories</option>']
        .concat(state.categories.map((category) => `<option value="${category.id}">${escapeHtml(category.displayName || category.name)}</option>`));
    elements.productCategoryFilter.innerHTML = options.join("");
    elements.productCategoryFilter.value = state.categories.some((category) => String(category.id) === selectedFilter) ? selectedFilter : "";

    const formOptions = state.categories.length
        ? state.categories.map((category) => `<option value="${category.id}">${escapeHtml(category.displayName || category.name)}</option>`).join("")
        : '<option value="">Ajoutez une categorie d abord</option>';
    document.getElementById("productCategoryId").innerHTML = formOptions;
    if (state.categories.some((category) => String(category.id) === selectedFormValue)) {
        document.getElementById("productCategoryId").value = selectedFormValue;
    } else if (state.categories.length) {
        document.getElementById("productCategoryId").value = String(state.categories[0].id);
    }
}

function populateCategoryForm(id) {
    const category = state.categories.find((item) => item.id === id);
    if (!category) {
        return;
    }
    document.getElementById("categoryId").value = category.id;
    document.getElementById("categoryName").value = category.name || "";
    document.getElementById("categoryKey").value = category.key || "";
    document.getElementById("categoryDisplayName").value = category.displayName || "";
    document.getElementById("categorySortOrder").value = category.sortOrder ?? 0;
    document.getElementById("categoryImageUrl").value = category.imageUrl || "";
    document.getElementById("categoryActive").checked = isActiveFlag(category);
    document.getElementById("categoryPromo").checked = Boolean(category.isPromo);
    document.getElementById("categoryBio").checked = Boolean(category.isBio);
    document.getElementById("categoryNew").checked = Boolean(category.isNew);
    document.getElementById("categoryPopular").checked = Boolean(category.isPopular);
    document.getElementById("categoryCustomTags").value = (category.customTags || []).join(", ");
    renderImagePreview("category");
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function populateProductForm(id) {
    try {
        const product = await fetchJson(`/api/admin/products/${id}`);
        document.getElementById("productId").value = product.id;
        document.getElementById("productName").value = product.name || "";
        document.getElementById("productDescription").value = product.description || "";
        document.getElementById("productPrice").value = product.price ?? "";
        document.getElementById("productOldPrice").value = product.oldPrice ?? "";
        document.getElementById("productDiscountPct").value = product.discountPct ?? 0;
        document.getElementById("productStockQty").value = product.stockQty ?? 0;
        document.getElementById("productRating").value = product.rating ?? 0;
        document.getElementById("productSalesCount").value = product.salesCount ?? 0;
        document.getElementById("productCategoryId").value = product.categoryId ?? "";
        document.getElementById("productImageUrl").value = product.imageUrl || "";
        document.getElementById("productActive").checked = isActiveFlag(product);
        document.getElementById("productPromo").checked = Boolean(product.isPromo);
        document.getElementById("productBio").checked = Boolean(product.isBio);
        document.getElementById("productNew").checked = Boolean(product.isNew);
        document.getElementById("productPopular").checked = Boolean(product.isPopular);
        document.getElementById("productCustomTags").value = (product.customTags || []).join(", ");
        document.getElementById("productPromoLabel").value = product.promoLabel || "";
        document.getElementById("productPromoStartsAt").value = instantToLocalDateTime(product.promoStartsAt);
        document.getElementById("productPromoEndsAt").value = instantToLocalDateTime(product.promoEndsAt);
        renderImagePreview("product");
        window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (error) {
        showToast(error.message || "Impossible de charger le produit");
    }
}

async function deleteCategory(id) {
    if (!window.confirm("Supprimer cette categorie ?")) {
        return;
    }
    try {
        await fetchJson(`/api/admin/categories/${id}`, { method: "DELETE" });
        resetCategoryForm();
        await loadCategories();
        updateStats();
        showToast("Categorie supprimee");
    } catch (error) {
        showToast(error.message || "Suppression impossible");
    }
}

async function deleteProduct(id) {
    if (!window.confirm("Supprimer ce produit ?")) {
        return;
    }
    try {
        await fetchJson(`/api/admin/products/${id}`, { method: "DELETE" });
        resetProductForm();
        await loadProducts();
        updateStats();
        showToast("Produit supprime");
    } catch (error) {
        showToast(error.message || "Suppression impossible");
    }
}

function resetCategoryForm() {
    elements.categoryForm.reset();
    document.getElementById("categoryId").value = "";
    document.getElementById("categorySortOrder").value = "0";
    document.getElementById("categoryActive").checked = true;
    document.getElementById("categoryPromo").checked = false;
    document.getElementById("categoryBio").checked = false;
    document.getElementById("categoryNew").checked = false;
    document.getElementById("categoryPopular").checked = false;
    document.getElementById("categoryCustomTags").value = "";
    renderImagePreview("category");
}

function resetProductForm() {
    elements.productForm.reset();
    document.getElementById("productId").value = "";
    document.getElementById("productDiscountPct").value = "0";
    document.getElementById("productStockQty").value = "0";
    document.getElementById("productRating").value = "0";
    document.getElementById("productSalesCount").value = "0";
    document.getElementById("productActive").checked = true;
    document.getElementById("productPromo").checked = false;
    document.getElementById("productBio").checked = false;
    document.getElementById("productNew").checked = false;
    document.getElementById("productPopular").checked = false;
    document.getElementById("productCustomTags").value = "";
    document.getElementById("productPromoLabel").value = "";
    document.getElementById("productPromoStartsAt").value = "";
    document.getElementById("productPromoEndsAt").value = "";
    if (state.categories.length) {
        document.getElementById("productCategoryId").value = String(state.categories[0].id);
    }
    renderImagePreview("product");
}

function syncDiscountFromPrices() {
    const price = Number(document.getElementById("productPrice").value);
    const oldPrice = Number(document.getElementById("productOldPrice").value);
    const discountField = document.getElementById("productDiscountPct");

    if (!price || !oldPrice || oldPrice <= price) {
        discountField.value = "0";
        return;
    }

    const discountPct = Math.round(((oldPrice - price) / oldPrice) * 100);
    discountField.value = String(Math.max(0, Math.min(100, discountPct)));
}

function resetProductFilters() {
    elements.productSearch.value = "";
    elements.productCategoryFilter.value = "";
    elements.productActiveFilter.value = "";
    elements.productPromoFilter.value = "";
    elements.productBioFilter.value = "";
    elements.productNewFilter.value = "";
    elements.productPopularFilter.value = "";
    elements.productMinDiscountFilter.value = "";
    elements.productMaxDiscountFilter.value = "";
    loadProducts(true);
}

function updateStats() {
    elements.categoryCount.textContent = String(state.categories.length);
    elements.activeCategoryCount.textContent = `${state.categories.filter(isActiveFlag).length} actives`;
    elements.productCount.textContent = String(state.products.length);
    elements.activeProductCount.textContent = `${state.products.filter(isActiveFlag).length} actifs`;
    elements.stockCount.textContent = String(state.products.reduce((sum, item) => sum + (item.stockQty || 0), 0));
    elements.promoProductCount.textContent = `${state.products.filter((item) => Boolean(item.isPromo) || (item.discountPct || 0) > 0).length} en promo`;
    elements.orderCount.textContent = String(state.dashboardSummary?.totalOrders ?? state.orders.length);
    elements.pendingOrderCount.textContent = `${state.dashboardSummary?.pendingOrders ?? 0} en attente`;
    elements.customerCount.textContent = String(state.dashboardSummary?.clientsCount ?? state.customers.length);
    elements.todayRevenue.textContent = `${formatMoney(state.dashboardSummary?.todayRevenue)} aujourd'hui`;
    elements.revenueTotal.textContent = formatMoney(state.dashboardSummary?.totalRevenue);
    elements.lowStockCount.textContent = `${state.dashboardSummary?.lowStockProductsCount ?? 0} stock faible`;
}

function syncExpandedItems(kind) {
    const items = kind === "category" ? state.categories : state.products;
    const expandedIds = kind === "category" ? state.expandedCategoryIds : state.expandedProductIds;
    const validIds = new Set(items.map((item) => item.id));
    Array.from(expandedIds).forEach((id) => {
        if (!validIds.has(id)) {
            expandedIds.delete(id);
        }
    });
}

function isExpanded(kind, id) {
    return (kind === "category" ? state.expandedCategoryIds : state.expandedProductIds).has(id);
}

function toggleItemDetails(kind, id) {
    const expandedIds = kind === "category" ? state.expandedCategoryIds : state.expandedProductIds;
    if (expandedIds.has(id)) {
        expandedIds.delete(id);
    } else {
        expandedIds.add(id);
    }

    if (kind === "category") {
        renderCategories();
    } else {
        renderProducts();
    }
}

function showLogin() {
    elements.loginView.classList.remove("hidden");
    elements.appView.classList.add("hidden");
}

function showApp() {
    elements.loginView.classList.add("hidden");
    elements.appView.classList.remove("hidden");
    showSection("overviewSection");
}

function logout() {
    clearAuthState();
    showLogin();
}

function clearAuthState() {
    state.token = "";
    state.me = null;
    state.dashboardSummary = null;
    state.categories = [];
    state.products = [];
    state.orders = [];
    state.customers = [];
    state.loyaltyGifts = [];
    state.selectedLoyaltyAccount = null;
    state.deliverySettings = null;
    state.homeSettings = null;
    state.selectedOrderId = null;
    state.selectedCustomerId = null;
    localStorage.removeItem(tokenKey);
}

function showSection(sectionId) {
    Object.entries(elements.sections).forEach(([key, section]) => {
        if (!section) {
            return;
        }
        section.classList.toggle("hidden", key !== sectionId);
    });

    elements.navLinks.forEach((button) => {
        button.classList.toggle("active", button.dataset.section === sectionId);
    });
}

async function fetchJson(url, options = {}, useJsonHeaders = true) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...authHeaders(useJsonHeaders),
            ...(options.headers || {})
        }
    });

    if (response.status === 204) {
        return null;
    }

    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
            const authError = new Error("AUTH_ERROR");
            authError.status = response.status;
            throw authError;
        }
        const error = new Error(body.message || "Erreur serveur");
        error.status = response.status;
        throw error;
    }
    return body;
}

function isAuthError(error) {
    if (!error) {
        return false;
    }
    const status = Number(error.status || 0);
    if (status === 401 || status === 403) {
        return true;
    }
    const message = (error.message || "").toLowerCase();
    return message.includes("auth_error")
        || message.includes("invalid or expired token")
        || message.includes("token invalide");
}

function isTokenExpiredOrInvalid(token) {
    if (!token || typeof token !== "string") {
        return true;
    }
    const parts = token.split(".");
    if (parts.length !== 3) {
        return true;
    }
    try {
        const payload = JSON.parse(atob(parts[1].replace(/-/g, "+").replace(/_/g, "/")));
        if (!payload || typeof payload.exp !== "number") {
            return false;
        }
        const nowSeconds = Math.floor(Date.now() / 1000);
        return payload.exp <= nowSeconds;
    } catch (_) {
        return true;
    }
}

function authHeaders(useJsonHeaders = true) {
    const headers = {};
    if (useJsonHeaders) {
        headers["Content-Type"] = "application/json";
    }
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }
    return headers;
}

function showToast(message) {
    if (shouldSuppressToast(message)) {
        return;
    }
    elements.toast.textContent = message;
    elements.toast.classList.remove("hidden");
    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(() => {
        elements.toast.classList.add("hidden");
    }, 2600);
}

function shouldSuppressToast(message) {
    if (!message) {
        return false;
    }
    const normalized = String(message).toLowerCase();
    if (normalized.includes("invalid or expired token")
        || normalized.includes("token invalide")
        || normalized.includes("token expired")
        || normalized.includes("unauthorized")
        || normalized.includes("forbidden")) {
        return true;
    }

    const loginVisible = !elements.loginView.classList.contains("hidden");
    if (!loginVisible) {
        return false;
    }
    return false;
}

function formatMoney(value) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) {
        return "-";
    }
    return `${Number(value).toFixed(2)} TND`;
}

function formatRating(value) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) {
        return "-";
    }
    return `${Number(value).toFixed(1)} / 5`;
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "-";
    }
    return date.toLocaleString("fr-FR");
}

function statusTone(status) {
    switch (status) {
        case "DELIVERED":
            return "success";
        case "CANCELLED":
            return "danger";
        case "SHIPPED":
        case "PREPARING":
            return "brand";
        default:
            return "warning";
    }
}

function joinAddress(order) {
    return [
        order.city,
        order.area,
        order.street,
        order.extra
    ].filter((value) => value && String(value).trim()).join(", ") || "-";
}

async function onDeliverySettingsSubmit(event) {
    event.preventDefault();

    try {
        state.deliverySettings = await fetchJson("/api/admin/delivery/settings", {
            method: "PUT",
            body: JSON.stringify({
                courierName: document.getElementById("deliveryCourierName").value.trim(),
                courierPhone: document.getElementById("deliveryCourierPhone").value.trim(),
                storePhone: document.getElementById("deliveryStorePhone").value.trim(),
                deliveryFee: Number(document.getElementById("deliveryFee").value || 0),
                etaLabel: document.getElementById("deliveryEtaLabel").value.trim()
            })
        });
        renderDeliverySettingsPreview(state.deliverySettings);
        showToast("Reglages livraison enregistres");
    } catch (error) {
        showToast(error.message || "Impossible d'enregistrer la livraison");
    }
}

async function onHomeSettingsSubmit(event) {
    event.preventDefault();

    try {
        state.homeSettings = await fetchJson("/api/admin/home/settings", {
            method: "PUT",
            body: JSON.stringify({
                locationLabel: document.getElementById("homeLocationLabel").value.trim(),
                etaLabel: document.getElementById("homeEtaLabel").value.trim(),
                deliveryAreas: document.getElementById("homeDeliveryAreas").value.trim()
            })
        });
        renderHomeSettingsPreview(state.homeSettings);
        showToast("Reglages Home enregistres");
    } catch (error) {
        showToast(error.message || "Impossible d'enregistrer la Home");
    }
}

function localDateTimeToInstant(value) {
    if (!value) {
        return null;
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

function instantToLocalDateTime(value) {
    if (!value) {
        return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "";
    }
    const pad = (n) => String(n).padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function isActiveFlag(item) {
    return Boolean(item.isActive ?? item.active);
}

function renderImagePreview(kind) {
    const urlInput = kind === "category" ? elements.categoryImageUrl : elements.productImageUrl;
    const preview = kind === "category" ? elements.categoryImagePreview : elements.productImagePreview;
    const url = (urlInput.value || "").trim();

    if (!url) {
        preview.classList.add("muted");
        preview.textContent = kind === "category" ? "Apercu image categorie" : "Apercu image produit";
        return;
    }

    preview.classList.remove("muted");
    preview.innerHTML = `<img src="${escapeHtml(url)}" alt="Apercu ${kind}"><span>${escapeHtml(url)}</span>`;
}

function previewLocalFile(kind) {
    const fileInput = document.getElementById(kind === "category" ? "categoryImageFile" : "productImageFile");
    const preview = kind === "category" ? elements.categoryImagePreview : elements.productImagePreview;
    const file = fileInput.files[0];

    if (!file) {
        renderImagePreview(kind);
        return;
    }

    if (!file.type || !file.type.startsWith("image/")) {
        preview.classList.remove("muted");
        preview.textContent = "Le fichier choisi n'est pas une image.";
        return;
    }

    if (file.size > maxUploadSizeBytes) {
        preview.classList.remove("muted");
        preview.textContent = "Image trop grande. Taille max 15 MB.";
        return;
    }

    const localUrl = URL.createObjectURL(file);
    preview.classList.remove("muted");
    preview.innerHTML = `<img src="${localUrl}" alt="Apercu local ${kind}"><span>${escapeHtml(file.name)}</span>`;
}

function escapeHtmlAttr(value) {
    return escapeHtml(value).replaceAll("`", "&#96;");
}

function parseTags(rawValue) {
    if (!rawValue || !rawValue.trim()) {
        return [];
    }
    return rawValue
        .split(",")
        .map((tag) => tag.trim())
        .filter((tag, index, array) => tag && array.indexOf(tag) === index);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
