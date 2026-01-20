document.addEventListener("DOMContentLoaded", () => {
    loadPrices();
    loadDropdowns(); // 預先載入商品與店家選單
	
	document.getElementById("productId").addEventListener("change", refreshMissingStores);
});

let allProducts = [];
let allStores = [];

// 1. 載入所有價格
async function loadPrices() {
    try {
        const prices = await apiGet("/admin/prices");
        renderTable(prices);
    } catch (err) {
        console.error("載入價格失敗", err);
    }
}

// 2. 載入下拉選單資料
async function loadDropdowns() {
    try {
        [allProducts, allStores] = await Promise.all([
            apiGet("/products"),
            apiGet("/stores")
        ]);
        
        // Modal 打開前，先預填所有商品到選單
        const pSelect = document.getElementById("productId");
        pSelect.innerHTML = allProducts.map(p => 
            `<option value="${p.productId}">${p.productName}</option>`
        ).join('');
    } catch (err) {
        console.error("載入商品選單失敗", err);
    }
}

function renderTable(prices) {
    const tbody = document.getElementById("priceTableBody");
    if (!prices || prices.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">目前無價格紀錄</td></tr>`;
        return;
    }

    tbody.innerHTML = prices.map(p => `
        <tr>
            <td><strong>${p.product.productName}</strong></td>
            <td>${p.store.storeName}</td>
            <td class="price-tag">$${p.price}</td>
            <td>${new Date(p.priceDate).toLocaleDateString()}</td>
            <td>
                <button onclick="editPrice(${p.id || p.priceId})">編輯</button>
                <button onclick="deletePrice(${p.id || p.priceId})" style="color:var(--danger); margin-left:8px;">刪除</button>
            </td>
        </tr>
    `).join('');
}

async function refreshMissingStores() {
    const productId = document.getElementById("productId").value;
    const sSelect = document.getElementById("storeId");
    
    if (!productId) return;

    // 清空舊的店家選項，避免殘留
    sSelect.innerHTML = '<option>載入中...</option>';

    try {
        const missingStores = await apiGet(`/admin/prices/missing-stores?productId=${productId}`);
        
        if (missingStores.length > 0) {
            // 使用 innerHTML 直接覆蓋，確保乾淨
            sSelect.innerHTML = missingStores.map(s => 
                `<option value="${s.storeId}">${s.storeName}</option>`
            ).join("");
            sSelect.disabled = false;
        } else {
            sSelect.innerHTML = `<option disabled>此商品已無待填通路</option>`;
            sSelect.disabled = true;
        }
    } catch (err) {
        sSelect.innerHTML = `<option disabled>載入失敗</option>`;
    }
}

// 3. 儲存 (新增或更新)
document.getElementById("priceForm").onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById("recordId").value;

    const payload = {
        product: { productId: parseInt(document.getElementById("productId").value) },
        store: { storeId: parseInt(document.getElementById("storeId").value) },
        price: parseFloat(document.getElementById("price").value),
        priceDate: document.getElementById("priceDate").value || getTodayDateString()
    };

    try {
        if (id) {
            await apiPut(`/admin/prices/${id}`, payload);
        } else {
            await apiPost("/admin/prices", payload);
        }
        alert("儲存成功");
        closeModal();
        loadPrices();
    } catch (err) {
        alert("儲存失敗: " + err.message);
    }
};

// 4. 刪除
async function deletePrice(id) {
    if (!confirm("確定要刪除這筆價格紀錄嗎？這不會刪除歷史紀錄。")) return;
    try {
        await apiDelete(`/admin/prices/${id}`);
        loadPrices();
    } catch (err) {
        alert("刪除失敗");
    }
}

async function openModal() {
    document.getElementById("priceForm").reset();
    document.getElementById("recordId").value = "";
    document.getElementById("modalTitle").innerText = "新增價格紀錄";
	document.getElementById("priceDate").value = getTodayDateString();
    // 1. 啟用欄位
    const pSelect = document.getElementById("productId");
    const sSelect = document.getElementById("storeId");
    pSelect.disabled = false;
    sSelect.disabled = false;
	await refreshMissingStores(); 
	document.getElementById("priceModal").style.display = "flex";

    try {
        // 2. 先抓取「還沒填完價格」的商品
        const availableProducts = await apiGet("/admin/prices/available-products");
        
        if (availableProducts.length === 0) {
            pSelect.innerHTML = `<option disabled selected>所有商品皆已完成定價</option>`;
            pSelect.disabled = true;
            sSelect.disabled = true;
            return;
        }

        // 3. 渲染商品選單
        pSelect.innerHTML = availableProducts.map(p => 
            `<option value="${p.productId}">${p.productName}</option>`
        ).join('');

        // 4. 根據「第一個商品」自動觸發店家過濾
        await refreshMissingStores(); 
        
        document.getElementById("priceModal").style.display = "flex";
    } catch (err) {
        alert("初始化選單失敗");
    }
}

function closeModal() {
    document.getElementById("priceModal").style.display = "none";
}

async function editPrice(id) {
    try {
        const p = await apiGet(`/admin/prices/${id}`);

        document.getElementById("recordId").value = p.id || p.priceId;
        
        const pSelect = document.getElementById("productId");
        pSelect.innerHTML = `<option value="${p.product.productId}">${p.product.productName}</option>`;
        
        document.getElementById("storeId").value = p.store.storeId;
        document.getElementById("price").value = p.price;
        
        document.getElementById("priceDate").value = getTodayDateString();

        // 3. 鎖定關鍵欄位，僅允許修改價格
        document.getElementById("productId").disabled = true;
        document.getElementById("storeId").disabled = true;
        document.getElementById("priceDate").disabled = true;

        document.getElementById("modalTitle").innerText = "編輯價格 (僅限調整金額)";
        document.getElementById("priceModal").style.display = "flex";
    } catch (err) {
        alert("載入資料失敗：" + err.message);
    }
}

function getTodayDateString() {
    return new Date().toISOString().split('T')[0];
}