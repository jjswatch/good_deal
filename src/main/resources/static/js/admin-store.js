document.addEventListener("DOMContentLoaded", () => {
    loadStores();
});

async function loadStores() {
    try {
        const stores = await apiGet("/admin/stores");
        renderStoreTable(stores);
    } catch (err) {
        console.error("載入商店失敗", err);
    }
}

function renderStoreTable(stores) {
    const tbody = document.getElementById("storeTableBody");
    tbody.innerHTML = stores.map(s => `
        <tr>
		<td data-label="ID">${s.storeId}</td>
		            <td data-label="商店名稱"><strong>${s.storeName}</strong></td>
		            <td data-label="群組"><span class="group-badge">${s.storeGroup || '0'}</span></td>
		            <td data-label="地址/地點" class="text-break">${s.location || '-'}</td>
		            <td data-label="網站">${s.website ? `<a href="${s.website}" target="_blank" class="site-link">點擊前往</a>` : '-'}</td>
		            <td data-label="操作">
		                <div class="mobile-actions">
		                    <button class="btn-edit" onclick="openEditStore(${s.storeId})">編輯</button>
		                    <button class="btn-delete" onclick="deleteStore(${s.storeId})">刪除</button>
		                </div>
		            </td>
        </tr>
    `).join('');
}

function openCreateStore() {
    document.getElementById("editStoreId").value = "";
    document.getElementById("storeName").value = "";
    document.getElementById("storeGroup").value = ""; // 清空
    document.getElementById("location").value = "";
    document.getElementById("website").value = "";
    document.getElementById("modalTitle").innerText = "➕ 新增商店";
    document.getElementById("storeModal").style.display = "flex";
}

async function openEditStore(id) {
    try {
        const s = await apiGet(`/admin/stores/${id}`);
        document.getElementById("editStoreId").value = s.storeId;
        document.getElementById("storeName").value = s.storeName;
        document.getElementById("storeGroup").value = s.storeGroup || ""; // 填入群組
        document.getElementById("location").value = s.location;
        document.getElementById("website").value = s.website;
        document.getElementById("modalTitle").innerText = "✏️ 編輯商店";
        document.getElementById("storeModal").style.display = "flex";
    } catch (err) {
        alert("讀取商店資料失敗");
    }
}

async function saveStore() {
    const id = document.getElementById("editStoreId").value;
    const payload = {
        storeName: document.getElementById("storeName").value,
        storeGroup: parseInt(document.getElementById("storeGroup").value) || 0, // 轉為整數
        location: document.getElementById("location").value,
        website: document.getElementById("website").value
    };

    if (!payload.storeName) return alert("請輸入商店名稱");

    try {
        if (id) {
            await apiPut(`/admin/stores/${id}`, payload);
        } else {
            await apiPost("/admin/stores", payload);
        }
        closeStoreModal();
        loadStores();
    } catch (err) {
        alert("儲存失敗");
    }
}

async function deleteStore(id) {
    if (!confirm("確定要刪除此商店嗎？這可能會影響到關聯的價格紀錄。")) return;
    try {
        await apiDelete(`/admin/stores/${id}`);
        loadStores();
    } catch (err) {
        alert("刪除失敗");
    }
}

function closeStoreModal() {
    document.getElementById("storeModal").style.display = "none";
}