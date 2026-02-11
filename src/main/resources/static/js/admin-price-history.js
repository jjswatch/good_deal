document.addEventListener("DOMContentLoaded", () => {
    initPage();
});

// 全域變數
let allProducts = [];
let allStores = [];
let isEditing = false;
let currentEditId = null;

async function initPage() {
    // 優先載入基礎資料，再載入清單
    await Promise.all([loadProducts(), loadStores()]);
    loadHistoryList();
}

// 1. 載入商品下拉選單
async function loadProducts() {
    try {
        allProducts = await apiGet("/products");
        const select = document.getElementById("productId");
        select.innerHTML = '<option value="">-- 請選擇商品 --</option>' + 
            allProducts.map(p => `<option value="${p.productId}">${p.brand}${p.productName} ${p.spec}</option>`).join("");
    } catch (err) { console.error("載入商品失敗", err); }
}

// 2. 載入店家下拉選單
async function loadStores() {
    try {
        allStores = await apiGet("/stores");
        const select = document.getElementById("storeId");
        select.innerHTML = '<option value="">-- 請選擇通路 --</option>' + 
            allStores.map(s => `<option value="${s.storeId}">${s.storeName}</option>`).join("");
    } catch (err) { console.error("載入店家失敗", err); }
}

// 3. 載入歷史價格列表
async function loadHistoryList() {
    const tbody = document.getElementById("priceTableBody");
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">載入中...</td></tr>`;

    try {
        // 取得所有歷史，建議後端實作分頁或 Top 100
        const historyList = await apiGet("/admin/history/latest?limit=100"); 
        renderTable(historyList);
    } catch (err) {
        console.warn("API 載入失敗，使用模擬數據", err);
        renderTable([]); // 這裡放你的 mockData
    }
}

function renderTable(data) {
    const tbody = document.getElementById("priceTableBody");
    if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">尚無資料</td></tr>`;
        return;
    }

    tbody.innerHTML = data.map(item => {
        const diff = item.newPrice - item.oldPrice;
        let trendHtml = `<span class="trend-tag trend-flat">持平</span>`;
        if (diff > 0) trendHtml = `<span class="trend-tag trend-up">▲ 漲 $${diff}</span>`;
        if (diff < 0) trendHtml = `<span class="trend-tag trend-down">▼ 降 $${Math.abs(diff)}</span>`;

        return `
            <tr>
                <td>#${item.historyId}</td>
                <td><strong>${item.brand}${item.productName || '未知商品'}</strong></td>
                <td>${item.storeName || '未知店家'}</td>
                <td>
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="color:#94a3b8; text-decoration:line-through; font-size:12px;">$${item.oldPrice}</span>
                        <span style="font-weight:bold;">$${item.newPrice}</span>
                        ${trendHtml}
                    </div>
                </td>
                <td>${formatDate(item.changedAt)}</td>
                <td>
                    <button class="btn-icon" onclick="openEditModal(${JSON.stringify(item).replace(/"/g, '&quot;')})" title="編輯">
                        <i class="fas fa-edit" style="color: #2563eb;"></i>
                    </button>
                    <button class="btn-icon btn-delete" onclick="deleteHistory(${item.historyId})">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

// 4. Modal 控制
function openModal() {
    isEditing = false;
    currentEditId = null;
    document.getElementById("modalTitle").innerText = "新增價格紀錄";
    document.getElementById("priceForm").reset();
    document.getElementById("currentPriceDisplay").style.display = 'none';
    document.getElementById("priceModal").style.display = "flex";
}

function closeModal() {
    document.getElementById("priceModal").style.display = "none";
}

function openEditModal(item) {
    isEditing = true;
    currentEditId = item.historyId;
    document.getElementById("modalTitle").innerText = "修改價格紀錄";
    
    // 填入現有資料
    document.getElementById("productId").value = item.productId;
    document.getElementById("storeId").value = item.storeId;
    document.getElementById("oldPrice").value = item.oldPrice;
    document.getElementById("newPrice").value = item.newPrice;
    
    document.getElementById("priceModal").style.display = "flex";
}

// 5. 自動偵測目前價格 (填入 oldPrice)
async function detectCurrentPrice() {
    const pid = document.getElementById("productId").value;
    const sid = document.getElementById("storeId").value;
    const display = document.getElementById("displayOldPrice");
    const input = document.getElementById("oldPrice");
    const container = document.getElementById("currentPriceDisplay");

    if (!pid || !sid) return;

    // 顯示載入中
    container.style.display = 'block';
    display.textContent = "...";

    try {
        // 這裡假設後端有 API 可以查單一商品在單一店家的目前價格
        // 例如: GET /prices?productId=1&storeId=2
        // 如果沒有，可能需要前端 filter (不建議) 或後端補 API
        const prices = await apiGet(`/prices/check?productId=${pid}&storeId=${sid}`);
        
        // 假設回傳格式 { price: 100 } 或 null
        if (prices && prices.price) {
            display.textContent = `$${prices.price}`;
            input.value = prices.price;
        } else {
            display.textContent = "尚無紀錄 (將視為 $0)";
            input.value = 0;
        }
    } catch (e) {
        display.textContent = "查詢失敗";
        input.value = 0;
    }
}

// 6. 送出表單 (新增紀錄)
async function handlePriceSubmit(e) {
    e.preventDefault();
    const requestData = {
        productId: parseInt(document.getElementById("productId").value),
        storeId: parseInt(document.getElementById("storeId").value),
        oldPrice: parseFloat(document.getElementById("oldPrice").value),
        newPrice: parseFloat(document.getElementById("newPrice").value)
    };

    try {
        if (isEditing) {
            await apiPut(`/admin/history/${currentEditId}`, requestData);
            alert("修改成功");
        } else {
            await apiPost("/admin/history", requestData);
            alert("新增成功");
        }
        closeModal();
        loadHistoryList();
    } catch (err) { alert("儲存失敗"); }
}

// 7. 刪除紀錄
async function deleteHistory(id) {
    if (!confirm("確定要刪除這筆價格紀錄嗎？這可能會影響價格趨勢圖的準確性。")) return;

    try {
        await apiDelete(`/admin/history/${id}`);
        loadHistoryList();
    } catch (err) {
        alert("刪除失敗");
    }
}

// 工具: 日期格式化
function formatDate(isoString) {
    if (!isoString) return "-";
    const d = new Date(isoString);
    return d.toLocaleString('zh-TW', { hour12: false });
}

// 點擊 Modal 外部關閉
window.onclick = function(event) {
    const modal = document.getElementById("priceModal");
    if (event.target == modal) {
        closeModal();
    }
}