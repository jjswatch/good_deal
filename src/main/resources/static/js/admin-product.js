let allProducts = []; // 存儲本地數據，方便編輯時查找

// 取得 Token
const token = localStorage.getItem("token");
if (!token) location.href = "../login.html";

function renderByCategory(products) {
  const tbody = document.getElementById("productTable");
  const htmlBuffer = []; // 使用陣列暫存字串

  const group = {};
  products.forEach(p => {
    const c = p.category;
    if (!group[c.categoryId]) {
      group[c.categoryId] = { name: c.categoryName, items: [] };
    }
    group[c.categoryId].items.push(p);
  });

  // 2. 生成 HTML 字串
  Object.keys(group).forEach(categoryId => {
    const cat = group[categoryId];

    // 分類標題列
	htmlBuffer.push(`
	      <tr class="category-row collapsed" onclick="toggleCategory(${categoryId}, this)">
	        <td colspan="5">
	          <div style="display:flex; justify-content:space-between; align-items:center;">
	            <div>
	              <span class="toggle-icon">▶</span>
	              📂 <strong>${cat.name}</strong> 
	              <span style="color:#64748b; font-size:13px; font-weight:400;">(${cat.items.length})</span>
	            </div>
	            <button class="btn-add-small" 
	              onclick="event.stopPropagation(); openCreateByCategory(${categoryId})">
	              ➕ 快速新增
	            </button>
	          </div>
	        </td>
	      </tr>
	    `);

    // 商品列
	cat.items.forEach(p => {
	      htmlBuffer.push(`
			<tr class="product-row is-hidden" data-cat-id="${categoryId}">
			    <td><code style="color:#64748b;">#${p.productId}</code></td>
			    <td>${p.brand || '-'}</td>
			    <td>
			      <strong>${p.productName}</strong><br>
			      <small style="color:#94a3b8;">${p.barcode || '無'}</small> 
				</td>
				<td><span style="color:#475569;">${p.spec || '-'}</span></td>
			    <td>
			      <button class="btn-edit" onclick="prepareEdit(${p.productId})">✏️ 編輯</button>
			      <button class="btn-delete" onclick="removeProduct(${p.productId})">🗑 刪除</button>
			    </td>
			  </tr>
	      `);
	    });
	  });

  // 3. 一次性寫入 DOM
  tbody.innerHTML = htmlBuffer.join('');
}

function toggleCategory(categoryId, rowElement) {
  // 1. 切換分類列本身的狀態（控制箭頭旋轉）
  rowElement.classList.toggle('collapsed');

  // 2. 找到所有屬於該分類的商品列並切換顯示狀態
  const productRows = document.querySelectorAll(`tr[data-cat-id="${categoryId}"]`);
  productRows.forEach(row => {
    row.classList.toggle('is-hidden');
  });
}

async function openCreateByCategory(categoryId) {
  document.getElementById("editProductId").value = "";
  document.getElementById("modalTitle").textContent = "✨ 新增商品";

  clearForm();

  // 載入分類並預選
  await loadCategories(categoryId);

  document.getElementById("productModal").style.display = "flex";
}

// 初始化：載入商品
async function loadProducts() {
  try {
    const res = await fetch("/api/admin/products", {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (!res.ok) throw new Error("權限不足或連線失敗");
    
    const data = await res.json();
    allProducts = data; 
    renderByCategory(data); // 使用分類渲染
  } catch (err) {
    console.error(err);
    document.getElementById("productTable").innerHTML = `<tr><td colspan="4" style="text-align:center; padding:40px; color:red;">${err.message}</td></tr>`;
  }
}

// 渲染表格 (使用 map 提高效能)
function renderTable(products) {
  const tbody = document.getElementById("productTable");
  tbody.innerHTML = products.map(p => `
    <tr>
      <td>#${p.productId}</td>
      <td><strong>${p.brand || '-'}</strong></td>
      <td>${p.productName}</td>
      <td><span class="badge">${p.category.categoryName}</span></td>
      <td>
        <button class="btn-edit" onclick="prepareEdit(${p.productId})">✏️</button>
        <button class="btn-delete" onclick="removeProduct(${p.productId})">🗑</button>
      </td>
    </tr>
  `).join('');
}

// 開啟新增視窗
async function openCreate() {
  document.getElementById("editProductId").value = ""; // 清空 ID 表示新增
  document.getElementById('modalTitle').textContent = "✨ 新增商品";
  clearForm();
  await loadCategories(); // 載入分類選單
  document.getElementById("productModal").style.display = "flex";
}

// 準備編輯
async function prepareEdit(id) {
  const p = allProducts.find(item => item.productId === id);
  if (!p) return;

  document.getElementById("editProductId").value = p.productId;
  document.getElementById("editProductName").value = p.productName;
  document.getElementById("editBarcode").value = p.barcode || "";
  document.getElementById("editBrand").value = p.brand || "";
  document.getElementById("editSpec").value = p.spec || "";
  document.getElementById("editImageUrl").value = p.imageUrl || "";
  
  document.getElementById('modalTitle').textContent = "✏️ 編輯商品";
  await loadCategories(p.category.categoryId);
  document.getElementById("productModal").style.display = "flex";
}

async function loadCategories(selectedId) {
  const categories = await apiGet("/admin/categories");

  const select = document.getElementById("editCategory");
  select.innerHTML = "";

  categories.forEach(c => {
    const opt = document.createElement("option");
    opt.value = c.categoryId;
    opt.textContent = c.categoryName;
    if (c.categoryId === selectedId) opt.selected = true;
    select.appendChild(opt);
  });
}

// 儲存邏輯 (判斷是 POST 還是 PUT)
async function saveProduct() {
  const id = document.getElementById("editProductId").value;
  const rawBarcode = document.getElementById("editBarcode").value.trim();
  const body = {
    productName: document.getElementById("editProductName").value,
	barcode: rawBarcode === "" ? null : rawBarcode,
	brand: document.getElementById("editBrand").value,
    spec: document.getElementById("editSpec").value,
    imageUrl: document.getElementById("editImageUrl").value,
    categoryId: Number(document.getElementById("editCategory").value)
  };

  try {
    if (id) {
      await apiPut(`/admin/products/${id}`, body);
    } else {
      await apiPost(`/admin/products`, body);
    }
    closeModal();
    loadProducts();
  } catch (err) {
	console.error(err);
	  alert("儲存失敗：" + err.message);
  }
}

function clearForm() {
  const fields = ["editProductName", "editBarcode", "editBrand", "editSpec", "editImageUrl"];
  fields.forEach(f => document.getElementById(f).value = "");
}

function closeModal() {
  document.getElementById("productModal").style.display = "none";
}

// 刪除邏輯
async function removeProduct(id) {
  if (!confirm("確定要永久刪除此商品嗎？")) return;
  
  await fetch(`/api/admin/products/${id}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` }
  });
  loadProducts();
}

// 啟動
loadProducts();