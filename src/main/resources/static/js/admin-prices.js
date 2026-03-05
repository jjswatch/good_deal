document.addEventListener("DOMContentLoaded", () => {
    initPage();
});

let allStores = [];

async function initPage() {
    allStores = await apiGet("/stores");
    // 填充下拉選單
    const options = '<option value="">--- 請選擇通路 ---</option>' + 
                    allStores.map(s => `<option value="${s.storeId}">${s.storeName}</option>`).join('');
    document.getElementById("batchStoreId").innerHTML = options;
    document.getElementById("editStoreId").innerHTML = options;
    
    loadAllExistingPrices(); // 載入下方列表
}

// ==============================
// 1. 批次新增邏輯 (Batch Add)
// ==============================

async function loadMissingProducts(storeId) {
    if (!storeId) {
        document.getElementById("missingProductsBody").innerHTML = '<tr><td colspan="2" style="text-align: center;">請先選擇通路</td></tr>';
        document.getElementById("batchSaveBtn").style.display = "none";
        return;
    }

    try {
        const missing = await apiGet(`/admin/prices/missing-products?storeId=${storeId}`);
        const tbody = document.getElementById("missingProductsBody");
        
        if (missing.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" style="text-align: center; color: #16a34a;">🎉 此通路所有商品皆已設定價格！</td></tr>';
            document.getElementById("batchSaveBtn").style.display = "none";
            return;
        }

        tbody.innerHTML = missing.map(p => `
            <tr class="batch-row" data-product-id="${p.productId}">
                <td><strong>${p.brand} ${p.productName}</strong> <small style="color:#666">(${p.spec})</small></td>
                <td><input type="number" class="batch-new-price" placeholder="請輸入金額" style="width: 100%; padding: 6px;"></td>
            </tr>
        `).join('');
        document.getElementById("batchSaveBtn").style.display = "block";
    } catch (err) {
        showToast("載入待定價清單失敗");
    }
}

async function saveBatchNewPrices() {
    const storeId = document.getElementById("batchStoreId").value;
    const rows = document.querySelectorAll(".batch-row");
    const newRecords = [];

    rows.forEach(row => {
        const productId = row.getAttribute("data-product-id");
        const price = row.querySelector(".batch-new-price").value;
        
        // 只收集有輸入價格的項目
        if (price && parseFloat(price) > 0) {
            newRecords.push({
                product: { productId: parseInt(productId) },
                store: { storeId: parseInt(storeId) },
                price: parseFloat(price),
                priceDate: new Date().toISOString().split('T')[0]
            });
        }
    });

    if (newRecords.length === 0) {
        showToast("⚠️ 請輸入至少一個商品的價格");
        return;
    }

    if (!confirm(`確定要一次新增 ${newRecords.length} 筆價格嗎？`)) return;

    try {
        // 這裡改用批次 API 路徑
        await apiPost("/admin/prices/batch", newRecords);
        
        alert(`✅ 成功批次新增 ${newRecords.length} 筆紀錄！`);
        
        // 重新整理 UI
        loadMissingProducts(storeId);
        loadAllExistingPrices();
    } catch (err) {
        alert("批次新增失敗：" + err.message);
    }
}

// ==============================
// 2. 編輯邏輯 (Modal Edit)
// ==============================

function openEditModal(existingRecord = null) {
    document.getElementById("editPriceForm").reset();
    document.getElementById("editPriceModal").style.display = "flex";
    document.getElementById("currentPriceInfo").style.display = "none";
    document.getElementById("editProductId").disabled = true;

    // 如果是從下方表格點擊「編輯」進來的
    if (existingRecord) {
        fillEditFields(existingRecord);
    }
}

function closeEditModal() {
    document.getElementById("editPriceModal").style.display = "none";
}

// 當 Modal 選擇通路後，載入該通路「已有價格」的商品
async function loadProductsForStore(storeId) {
    const pSelect = document.getElementById("editProductId");
    if (!storeId) {
        pSelect.disabled = true;
        return;
    }
    
    try {
        // 這邊需要一個 API 取得該店已有價格的商品
        const prices = await apiGet("/admin/prices");
        const storePrices = prices.filter(p => p.store.storeId == storeId);
        
        pSelect.innerHTML = '<option value="">--- 選擇要修改的商品 ---</option>' + 
            storePrices.map(p => `<option value="${p.product.productId}" data-record-id="${p.priceId}" data-price="${p.price}">
                ${p.product.brand}${p.product.productName}
            </option>`).join('');
        
        pSelect.disabled = false;
    } catch (err) {
        showToast("載入商品失敗");
    }
}

// 選擇商品後自動顯示當前價格
function fetchCurrentPrice() {
    const pSelect = document.getElementById("editProductId");
    const selectedOption = pSelect.options[pSelect.selectedIndex];
    
    if (selectedOption.value) {
        const recordId = selectedOption.getAttribute("data-record-id");
        const currentPrice = selectedOption.getAttribute("data-price");
        
        document.getElementById("editRecordId").value = recordId;
        document.getElementById("oldPriceValue").innerText = `$${Math.round(currentPrice)}`;
        document.getElementById("newPriceInput").value = Math.round(currentPrice);
        document.getElementById("currentPriceInfo").style.display = "block";
    }
}

document.getElementById("editPriceForm").onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById("editRecordId").value;
	const storeId = document.getElementById("editStoreId").value;      // 取得目前的通路 ID
	const productId = document.getElementById("editProductId").value;  // 取得目前的商品 ID
    const newPrice = document.getElementById("newPriceInput").value;

    try {
        const payload = {
			store: { storeId: parseInt(storeId) },
			product: { productId: parseInt(productId) },
            price: parseFloat(newPrice),
            priceDate: new Date().toISOString().split('T')[0]
        };
        await apiPut(`/admin/prices/${id}`, payload);
        alert("✅ 價格已更新");
        closeEditModal();
        loadAllExistingPrices();
    } catch (err) {
		console.error("更新失敗:", err);
		alert("❌ 更新失敗，請檢查控制台資訊");
    }
};

// ==============================
// 3. 基礎載入
// ==============================

async function loadAllExistingPrices() {
    const prices = await apiGet("/admin/prices");
    const tbody = document.getElementById("priceTableBody");
    
    tbody.innerHTML = prices.map(p => `
        <tr>
            <td>${p.product.brand} ${p.product.productName}</td>
            <td>${p.store.storeName}</td>
            <td><strong>$${Math.round(p.price)}</strong></td>
            <td>
                <button class="btn-edit-small" onclick="quickEdit(${p.priceId}, ${p.store.storeId}, ${p.product.productId}, ${p.price})">✏️ 編輯</button>
            </td>
        </tr>
    `).join('');
}

// 從表格快速跳轉到 Modal 編輯
async function quickEdit(id, storeId, productId, price) {
    openEditModal();
    document.getElementById("editStoreId").value = storeId;
    await loadProductsForStore(storeId);
    document.getElementById("editProductId").value = productId;
    fetchCurrentPrice();
}