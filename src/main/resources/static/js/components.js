/**
 * GoodDeal 頁面共用元件載入器
 */
document.addEventListener("DOMContentLoaded", () => {
    renderNavbar();
    renderBottomNav();
	renderFooter();
    updateActiveNavItem();
	syncUserUI();
});

function renderNavbar() {
	// 檢查頁面是否已經有 nav，避免重複渲染
	if (document.querySelector('nav')) return;
    // 取得當前檔名
    const path = window.location.pathname;
    const page = path.split("/").pop();
    const isHomePage = (page === "index.html" || page === "");
	const user = typeof getCurrentUser === 'function' ? getCurrentUser() : null;

	// 2. 定義左側內容 (上一頁或 Logo)
	    const leftContent = isHomePage 
	        ? `<div class="logo" onclick="location.href='index.html'">GoodDeal</div>`
	        : `<div class="nav-back" onclick="history.back()"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg></div>`;

	    // 3. ⭐ 管理員快捷入口 (僅在管理員登入時顯示)
	    const adminLink = (user && user.role === "ADMIN") 
	        ? `<div class="admin-quick-link" onclick="location.href='admin/admin-dashboard.html'" style="cursor:pointer; background:#2563eb; color:white; padding:2px 8px; border-radius:4px; font-size:12px; margin-right:10px; font-weight:bold;">⚙️ 後台</div>`
	        : "";

	    const navbarHTML = `
	    <nav>
	        <div class="nav-container">
	            ${leftContent}
	            <div style="margin-left: auto; display: flex; align-items: center;">
	                ${adminLink}
	                <div class="nav-notification" onclick="handleNotificationClick()">
	                    <svg class="nav-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
	                    <div class="notification-dot" id="notiDot"></div>
	                </div>
	            </div>
	        </div>
	    </nav>`;
    
    document.body.insertAdjacentHTML('afterbegin', navbarHTML);
}

function renderBottomNav() {
    const bottomNavHTML = `
    <div class="bottom-nav">
        <a href="index.html" class="bottom-nav-item" id="nav-home">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span>首頁</span>
        </a>
        <a href="scanner.html" class="bottom-nav-item scan-protrude" id="nav-scan">
            <svg class="nav-icon scan-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 5V19H3V5H1Z" fill="currentColor"/>
                <path d="M5 5V19H6V5H5Z" fill="currentColor"/>
                <path d="M8 5V19H10V5H8Z" fill="currentColor"/>
                <path d="M12 5V19H13V5H12Z" fill="currentColor"/>
                <path d="M15 5V19H16V5H15Z" fill="currentColor"/>
                <path d="M18 5V19H20V5H18Z" fill="currentColor"/>
                <path d="M22 5V19H23V5H22Z" fill="currentColor"/>
                <path d="M0 12H24" stroke="#FF4500" stroke-width="2" stroke-linecap="round" class="scan-beam"/>
            </svg>
            <span>掃描</span>
        </a>
        <a href="login.html" id="bottomNavMember" class="bottom-nav-item">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>會員</span>
        </a>
    </div>`;
    document.body.insertAdjacentHTML('beforeend', bottomNavHTML);
}

function syncUserUI() {
    const user = typeof getCurrentUser === 'function' ? getCurrentUser() : null;
    const memberBtn = document.getElementById("bottomNavMember");
    
    if (user && memberBtn) {
        memberBtn.href = "profile.html";
        // 取得積分（實務上從 localStorage 或 API 取得，預設 0）
        const points = user.points || 0; 
        memberBtn.querySelector('span').innerHTML = `我的 <b style="color:#FF4500;">(${points}pt)</b>`;
    }
}

// 自動判斷當前頁面並加上 active class
function updateActiveNavItem() {
    const path = window.location.pathname;
    const page = path.split("/").pop();
    
    if (page === "index.html" || page === "") {
        document.getElementById("nav-home")?.classList.add("active");
    } else if (page === "scanner.html") {
        document.getElementById("nav-scan")?.classList.add("active");
    }
}

// 通用的通知點擊處理
window.handleNotificationClick = function() {
    alert("目前沒有新訊息");
    const dot = document.getElementById("notiDot");
    if(dot) dot.style.display = "none";
};

function renderFooter() {
    // 檢查頁面是否已經有 footer，避免重複渲染
    if (document.querySelector('footer')) return;

    const footerHTML = `
    <footer class="site-footer">
        <div class="footer-container">
            <div class="footer-info">
                <h3>GoodDeal 商品比價平台</h3>
                <p>整合全台量販數據，打造最精準的省錢工具</p>
            </div>
            <div class="footer-contact">
                <h4>商務合作聯絡</h4>
                <ul>
                    <li>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                        <span>0916-869-033 (陳先生)</span>
                    </li>
                    <li>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                        <a href="mailto:jjswatch@gmail.com">jjswatch@gmail.com</a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            © 2026 jjswatch
        </div>
    </footer>`;

    // 插入到 body 的最後面（但在 bottom-nav 之前）
    document.body.insertAdjacentHTML('beforeend', footerHTML);
}