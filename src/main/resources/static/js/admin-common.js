// 立即執行權限檢查，不必等 DOM 載入
if (localStorage.getItem("token")) {
    // 這裡可以檢查 role 是否為 ADMIN，邏輯視你的 auth.js 內容而定
    if (!isAdmin()) { location.href = "/login.html"; }
} else {
    location.href = "../login.html";
}
/**
 * Admin 通用組件邏輯
 * 包含：側邊欄注入、行動版選單切換、自動高亮當前頁面、登出功能
 */
document.addEventListener("DOMContentLoaded", () => {
    renderAdminLayout();
    initSidebarLogic();
    highlightActiveMenu();
});

// 1. 注入側邊欄與行動版標頭
function renderAdminLayout() {
    const currentPath = window.location.pathname;
    
    // 行動版標頭 HTML
    const mobileHeaderHTML = `
        <div class="mobile-header">
            <button class="menu-toggle" id="menuToggle">☰</button>
            <h3 style="margin: 0; font-size: 18px;">GoodDeal 管理系統</h3>
        </div>
        <div id="sidebarOverlay" class="sidebar-overlay"></div>
    `;

    // 側邊欄 HTML
    const sidebarHTML = `
        <aside class="sidebar" id="sidebar">
            <h2>🛠 Admin</h2>
            <a href="admin-dashboard.html" data-page="dashboard">📊 Dashboard</a>
			<a href="admin-stores.html" data-page="stores">🏪 商店管理</a>
			<a href="admin-categories.html" data-page="categories">📂 分類管理</a>
            <a href="admin-products.html" data-page="products">📦 商品管理</a>
            <a href="admin-prices.html" data-page="prices">🏷️ 商品價格管理</a>
			<a href="admin-price-history.html" data-page="price-history">📈 商品歷史價格管理</a>
            <a href="admin-price-reports.html" data-page="price-reports">💰 價格回報審核</a>
            <hr style="border-color: #1e293b; margin: 20px 0; opacity: 0.5;">
            <a href="../index.html" target="_blank" style="color: #10b981;">🏠 前往賣場首頁</a>
            <a href="#" onclick="logout()">🚪 登出</a>
        </aside>
    `;

    // 插入行動版標頭到 body 最前面
    document.body.insertAdjacentHTML('afterbegin', mobileHeaderHTML);

    // 插入側邊欄到 .admin-layout 的最前面
    const adminLayout = document.querySelector('.admin-layout');
    if (adminLayout) {
        adminLayout.insertAdjacentHTML('afterbegin', sidebarHTML);
    }
}

// 2. 側邊欄切換邏輯 (解決重複宣告問題)
function initSidebarLogic() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (menuToggle && sidebar && overlay) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
            overlay.classList.toggle('active');
        });

        overlay.addEventListener('click', () => {
            sidebar.classList.remove('active');
            overlay.classList.remove('active');
        });
    }
}

// 3. 自動根據網址加上 .active 樣式
function highlightActiveMenu() {
    const path = window.location.pathname;
    const menuLinks = document.querySelectorAll('.sidebar a');
    
    menuLinks.forEach(link => {
        if (path.includes(link.getAttribute('href'))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

// 4. 通用登出功能
function logout() {
    if (confirm("確定要登出嗎？")) {
        localStorage.clear();
        location.href = "../login.html";
    }
}