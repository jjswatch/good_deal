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
	        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center;">目前無價格紀錄</td></tr>`;
	        return;
	    }

	    tbody.innerHTML = prices.map(p => {
	        const pid = p.id || p.priceId;
	        return `
	        <tr data-id="${pid}">
	            <td><strong>${p.product.brand}${p.product.productName}</strong></td>
	            <td>${p.store.storeName}</td>
	            <td>
	                <input type="number" class="batch-price-input" 
	                       value="${Math.round(p.price)}" 
	                       data-old="${Math.round(p.price)}"
	                       style="width: 80px; padding: 4px; border: 1px solid #cbd5e1; border-radius: 4px;">
	            </td>
	            <td>
	                <button onclick="deletePrice(${pid})" style="color:var(--danger);">刪除</button>
	            </td>
	        </tr>
	    `}).join('');
}

async function refreshMissingProducts() {
    const storeId = document.getElementById("storeId").value;
    const pSelect = document.getElementById("productId");
    
    if (!storeId) return;

    pSelect.innerHTML = '<option>載入中...</option>';

    try {
        // 呼叫新寫的 API
        const missingProducts = await apiGet(`/admin/prices/missing-products?storeId=${storeId}`);
        
        if (missingProducts.length > 0) {
            pSelect.innerHTML = missingProducts.map(p => 
                `<option value="${p.productId}">${p.brand} ${p.productName} ${p.spec}</option>`
            ).join("");
            pSelect.disabled = false;
        } else {
            pSelect.innerHTML = `<option disabled>此店所有商品皆已定價</option>`;
            pSelect.disabled = true;
        }
    } catch (err) {
        pSelect.innerHTML = `<option disabled>載入商品失敗</option>`;
    }
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
	const today = getTodayDateString();

    const payload = {
        product: { productId: parseInt(document.getElementById("productId").value) },
        store: { storeId: parseInt(document.getElementById("storeId").value) },
        price: parseFloat(document.getElementById("price").value),
		priceDate: today 
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

    // 1. 啟用欄位
    const pSelect = document.getElementById("productId");
    const sSelect = document.getElementById("storeId");
    pSelect.disabled = false;
    sSelect.disabled = false;
	try {
	        // 先載入「所有店面」供選擇
	        const stores = await apiGet("/stores");
	        sSelect.innerHTML = stores.map(s => 
	            `<option value="${s.storeId}">${s.storeName}</option>`
	        ).join("");

	        // 觸發第一個店面的商品載入
	        await refreshMissingProducts();
	        
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
	        
	        // 編輯模式：直接填入單一選項並鎖定
	        const sSelect = document.getElementById("storeId");
	        sSelect.innerHTML = `<option value="${p.store.storeId}">${p.store.storeName}</option>`;
	        
	        const pSelect = document.getElementById("productId");
	        pSelect.innerHTML = `<option value="${p.product.productId}">${p.product.brand}${p.product.productName}${p.product.spec}</option>`;
	        
	        document.getElementById("price").value = Math.round(p.price);

	        sSelect.disabled = true;
	        pSelect.disabled = true;

	        document.getElementById("modalTitle").innerText = "編輯價格紀錄";
	        document.getElementById("priceModal").style.display = "flex";
	    } catch (err) {
	        alert("載入資料失敗");
	    }
}

function getTodayDateString() {
    return new Date().toISOString().split('T')[0];
}

async function saveAllPrices() {
    const rows = document.querySelectorAll("#priceTableBody tr");
    const updates = [];


    rows.forEach(row => {
        const input = row.querySelector(".batch-price-input");
        if (!input) return;

        const priceId = row.getAttribute("data-id");
        const newPrice = parseFloat(input.value);
        const oldPrice = parseFloat(input.getAttribute("data-old"));

        // 只有價格變動才加入更新清單
        if (newPrice !== oldPrice) {
            updates.push({
                priceId: parseInt(priceId),
                price: newPrice
            });
        }
    });

    if (updates.length === 0) {
        alert("沒有偵測到任何價格變動");
        return;
    }

    if (!confirm(`確定要批次更新 ${updates.length} 筆價格嗎？`)) return;

    try {
        await apiPut("/admin/prices/batch", updates);
        alert("批次儲存成功！");
        loadPrices(); // 重新載入
    } catch (err) {
        alert("批次儲存失敗: " + err.message);
    }
}