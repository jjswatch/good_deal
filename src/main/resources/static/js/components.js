/**
 * GoodDeal 頁面共用元件載入器
 * 優化重點：路徑彈性、防止重複執行、健全的用戶狀態檢查
 */
document.addEventListener("DOMContentLoaded", () => {
	// 1. 先渲染基礎 UI
	renderNavbar();
	renderBottomNav();
	renderFooter();

	// 2. 執行狀態更新
	updateActiveNavItem();
	syncUserUI();

	// 3. 給 body 增加底距，避免內容被 Bottom Nav 遮擋
	document.body.style.paddingBottom = "70px";
});

/**
 * 導覽列渲染：處理 Logo/返回鍵切換
 */
function renderNavbar() {
	if (document.querySelector('nav')) return;

	const path = window.location.pathname;
	const page = path.split("/").pop();
	const isHomePage = (page === "index.html" || page === "" || path.endsWith("/"));
	const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;

	const leftContent = isHomePage
		? `<div class="logo" onclick="location.href='index.html'" style="cursor:pointer">EcoSave</div>`
		: `<div class="nav-back" onclick="history.back()" style="cursor:pointer">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
           </div>`;

	const actionIcon = (user && user.role === "ADMIN") ? `
		<div class="nav-admin" onclick="location.href='admin/admin-dashboard.html'" style="cursor:pointer;"> 
			📊 
		</div>` : `
		<div class="nav-notification" onclick="handleNotificationClick()" style="cursor:pointer; position:relative;"> 
			<svg class="nav-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"> 
				<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path> 
				<path d="M13.73 21a2 2 0 0 1-3.46 0"></path> 
			</svg> 
			<div class="notification-dot" id="notiDot" style="position:absolute; top:2px; right:2px; width:8px; height:8px; background:red; border-radius:50%; display:none;"></div> 
		</div>`;

	const navbarHTML = ` 
		<nav class="main-nav"> 
			<div class="nav-container"> ${leftContent} 
				<div class="nav-search-bar" id="stickySearch"> 
					<input type="text" id="navKeyword" placeholder="搜尋商品..."> 
					<button onclick="handleNavSearch()"> 
						<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"> 
							<circle cx="11" cy="11" r="8"></circle> 
							<line x1="21" y1="21" x2="16.65" y2="16.65"></line> 
						</svg> 
					</button> 
				</div> 
				<div class="nav-actions"> ${actionIcon} </div> 
			</div> 
		</nav>`;
	
	const quickBarHTML = `
	    <div class="quick-nav-bar">
	        <div class="quick-nav-container">
	            <select id="quickStore" onchange="quickJump('store', this.value)">
	                <option value="">商家專區</option>
	            </select>
	            <select id="quickBrand" onchange="quickJump('q', this.value)">
	                <option value="">所有品牌</option>
	            </select>
	            <select id="quickCat" onchange="quickJump('cat', this.value)">
	                <option value="">所有分類</option>
	            </select>
	        </div>
	    </div>`;

	document.body.insertAdjacentHTML('afterbegin', navbarHTML); 
	document.body.insertAdjacentHTML('afterbegin', quickBarHTML);
	
	fetchQuickOptions();
	initScrollSearch();
}

function initScrollSearch() {
    const stickySearch = document.getElementById('stickySearch');
    window.addEventListener('scroll', () => {
        // 當捲動超過 200px 時顯示 (可根據 Hero 高度調整)
        if (window.scrollY > 250) {
            stickySearch.classList.add('visible');
        } else {
            stickySearch.classList.remove('visible');
        }
    });
}

window.handleNavSearch = function() {
    const key = document.getElementById("navKeyword").value.trim();
    if (key) {
        window.location.href = `search.html?q=${encodeURIComponent(key)}`;
    }
};

/**
 * 底部導覽列渲染
 */
function renderBottomNav() {
	if (document.querySelector('.bottom-nav')) return;

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
            <div class="scan-icon-wrapper">
                <svg class="nav-icon scan-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 5V19H3V5H1Z" fill="currentColor"/>
                    <path d="M5 5V19H6V5H5Z" fill="currentColor"/>
                    <path d="M8 5V19H10V5H8Z" fill="currentColor"/>
                    <path d="M12 5V19H13V5H12Z" fill="currentColor"/>
                    <path d="M18 5V19H20V5H18Z" fill="currentColor"/>
                    <path d="M0 12H24" stroke="#FF4500" stroke-width="2" stroke-linecap="round" class="scan-beam"/>
                </svg>
            </div>
            <span>掃描</span>
        </a>
        <a href="login.html" id="bottomNavMember" class="bottom-nav-item">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span id="memberText">會員</span>
        </a>
    </div>`;
	document.body.insertAdjacentHTML('beforeend', bottomNavHTML);
}

/**
 * 更新會員狀態與積分
 */
function syncUserUI() {
	const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
	const memberBtn = document.getElementById("bottomNavMember");
	const memberText = document.getElementById("memberText");

	if (user && memberBtn && memberText) {
		memberBtn.href = "profile.html";
		memberText.innerHTML = `我的 <b style="color:#FF4500;"></b>`;
	}
}

/**
 * 根據 URL 自動高亮對應的按鈕
 */
function updateActiveNavItem() {
	const path = window.location.pathname;
	// 移除所有 active
	document.querySelectorAll('.bottom-nav-item').forEach(el => el.classList.remove('active'));

	if (path.includes("index.html") || path.endsWith("/")) {
		document.getElementById("nav-home")?.classList.add("active");
	} else if (path.includes("scanner.html")) {
		document.getElementById("nav-scan")?.classList.add("active");
	} else if (path.includes("profile.html") || path.includes("login.html")) {
		document.getElementById("bottomNavMember")?.classList.add("active");
	}
}

/**
 * 通用頁尾渲染
 */
function renderFooter() {
	if (document.querySelector('.site-footer')) return;

	const footerHTML = `
    <footer class="site-footer">
        <div class="footer-container">
            <div class="footer-info">
                <h3>EcoSave 商品比價</h3>
                <p>一塊享優惠，一塊也能省</p>
            </div>
            <div class="footer-contact">
                <div class="contact-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                    <span>0916-869-033 (陳先生)</span>
                </div>
                <div class="contact-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                    <a href="mailto:jjswatch@gmail.com">jjswatch@gmail.com</a>
                </div>
            </div>
        </div>
        <div class="footer-bottom">© 2026 jjswatch</div>
    </footer>`;

	document.body.insertAdjacentHTML('beforeend', footerHTML);
}

// 通知點擊
window.handleNotificationClick = function() {
	alert("目前沒有新訊息");
	const dot = document.getElementById("notiDot");
	if (dot) dot.style.display = "none";
};

// 取得資料並填入下拉選單
async function fetchQuickOptions() {
    try {
        const data = await apiGetPublic('/common/quick-search-options');
		
		if (!data || !data.stores) {
			console.warn("API 回傳資料格式不正確", data);
			return;
		}

        const sSelect = document.getElementById('quickStore');
        const bSelect = document.getElementById('quickBrand');
        const cSelect = document.getElementById('quickCat');
		
		sSelect.innerHTML = '<option value="">商家專區</option>';
		bSelect.innerHTML = '<option value="">所有品牌</option>';
		cSelect.innerHTML = '<option value="">所有分類</option>';
		
		// 填充商家
		data.stores.forEach(s => {
			if (s) sSelect.add(new Option(s.name, s.id));
		});
		// 填充品牌
		data.brands.forEach(b => {
		    if(b) bSelect.add(new Option(b, b));
		});
		// 填充分類
		data.categories.forEach(c => {
		    if(c) cSelect.add(new Option(c.categoryName, c.categoryId));
		});
    } catch (err) {
        console.error("載入快捷選項失敗", err);
    }
}

// 自動跳轉函式
window.quickJump = function(paramName, value) {
    if (!value) return; // 如果選到預設提示項，不跳轉
    
    // 跳轉至 search.html 並帶上對應參數
    // 例如：search.html?store=家樂福 或 search.html?cid=5
    window.location.href = `search.html?${paramName}=${encodeURIComponent(value)}`;
};