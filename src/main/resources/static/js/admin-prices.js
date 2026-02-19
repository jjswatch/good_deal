document.addEventListener("DOMContentLoaded", () => {
    loadPrices();       // 載入表格資料
    loadStoreList();    // 載入店家選單資料
    
    // 核心連動：僅保留選店家連動商品
    document.getElementById("storeId").addEventListener("change", handleStoreChange);
});

let allStores = []; // 僅保留店家資料，減少記憶體佔用

// 1. 載入店家列表（初始化用）
async function loadStoreList() {
    try {
        allStores = await apiGet("/stores");
    } catch (err) {
        console.error("無法載入店家列表", err);
    }
}

// 2. 處理店家變更：單向流程「選店 -> 顯貨」
async function handleStoreChange() {
    const storeId = this.value;
    const pSelect = document.getElementById("productId");
    const isEditMode = !!document.getElementById("recordId").value;

    if (isEditMode) return; // 編輯模式不觸發連動

    if (!storeId) {
        pSelect.innerHTML = '<option value="">--- 請先選擇店家 ---</option>';
        pSelect.disabled = true;
        return;
    }

    pSelect.innerHTML = '<option>🔍 搜尋缺件商品中...</option>';
    pSelect.disabled = true;

    try {
        // 取得該店「尚未定價」的商品
        const missingProducts = await apiGet(`/admin/prices/missing-products?storeId=${storeId}`);
        
        if (missingProducts.length > 0) {
            pSelect.innerHTML = '<option value="">--- 請選擇商品 ---</option>' + 
                missingProducts.map(p => `<option value="${p.productId}">${p.brand} ${p.productName} ${p.spec}</option>`).join("");
            pSelect.disabled = false;
        } else {
            pSelect.innerHTML = `<option disabled>🎉 此店所有商品皆已定價</option>`;
        }
    } catch (err) {
        pSelect.innerHTML = `<option disabled>❌ 載入失敗</option>`;
    }
}

// 3. 打開新增 Modal
function openModal() {
    const form = document.getElementById("priceForm");
    form.reset();
    document.getElementById("recordId").value = "";
    document.getElementById("modalTitle").innerText = "➕ 新增價格紀錄";

    const sSelect = document.getElementById("storeId");
    const pSelect = document.getElementById("productId");

    sSelect.disabled = false;
    pSelect.disabled = true; // 預設禁用商品，直到選了店家
    pSelect.innerHTML = '<option value="">--- 請先選擇店家 ---</option>';

    // 填入所有店家
    sSelect.innerHTML = '<option value="">--- 請選擇店家 ---</option>' + 
        allStores.map(s => `<option value="${s.storeId}">${s.storeName}</option>`).join('');

    document.getElementById("priceModal").style.display = "flex";
}

// 4. 儲存紀錄 (新增或更新)
document.getElementById("priceForm").onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById("recordId").value;
    
    const payload = {
        product: { productId: parseInt(document.getElementById("productId").value) },
        store: { storeId: parseInt(document.getElementById("storeId").value) },
        price: parseFloat(document.getElementById("price").value),
        priceDate: new Date().toISOString().split('T')[0] // 取得今天日期
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

// --- 以下為維護功能 (載入、編輯、刪除、批次) ---

async function loadPrices() {
    try {
        const prices = await apiGet("/admin/prices");
        renderTable(prices);
    } catch (err) {
        console.error("載入價格失敗", err);
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
                       value="${Math.round(p.price)}" data-old="${Math.round(p.price)}"
                       style="width: 80px; padding: 4px; border: 1px solid #cbd5e1; border-radius: 4px;">
            </td>
            <td>
                <button class="btn-edit-small" onclick="editPrice(${pid})">編輯</button>
                <button onclick="deletePrice(${pid})" style="color:var(--danger); margin-left:8px;">刪除</button>
            </td>
        </tr>`;
    }).join('');
}

async function editPrice(id) {
    try {
        const p = await apiGet(`/admin/prices/${id}`);
        document.getElementById("recordId").value = p.id || p.priceId;
        document.getElementById("price").value = Math.round(p.price);

        // 編輯模式鎖定連動，直接填入單一選項
        const sSelect = document.getElementById("storeId");
        const pSelect = document.getElementById("productId");
        
        sSelect.innerHTML = `<option value="${p.store.storeId}">${p.store.storeName}</option>`;
        pSelect.innerHTML = `<option value="${p.product.productId}">${p.product.brand}${p.product.productName}</option>`;
        
        sSelect.disabled = true;
        pSelect.disabled = true;

        document.getElementById("modalTitle").innerText = "✏️ 編輯價格紀錄";
        document.getElementById("priceModal").style.display = "flex";
    } catch (err) {
        alert("載入資料失敗");
    }
}

async function deletePrice(id) {
    if (!confirm("確定要刪除這筆價格紀錄嗎？")) return;
    try { await apiDelete(`/admin/prices/${id}`); loadPrices(); } catch (err) { alert("刪除失敗"); }
}

function closeModal() { document.getElementById("priceModal").style.display = "none"; }

async function saveAllPrices() {
    const rows = document.querySelectorAll("#priceTableBody tr");
    const updates = [];
    rows.forEach(row => {
        const input = row.querySelector(".batch-price-input");
        if (!input) return;
        const priceId = row.getAttribute("data-id");
        const newPrice = parseFloat(input.value);
        const oldPrice = parseFloat(input.getAttribute("data-old"));
        if (newPrice !== oldPrice) updates.push({ priceId: parseInt(priceId), price: newPrice });
    });
    if (updates.length === 0) return alert("沒有價格變動");
    if (!confirm(`確定更新 ${updates.length} 筆價格？`)) return;
    try { await apiPut("/admin/prices/batch", updates); alert("批次儲存成功！"); loadPrices(); } catch (err) { alert("儲存失敗"); }
}