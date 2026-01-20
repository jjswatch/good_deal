// 側邊欄手機版開關邏輯
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('sidebarOverlay');

if (menuToggle) {
  menuToggle.addEventListener('click', () => {
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
  });
}

if (overlay) {
  overlay.addEventListener('click', () => {
    sidebar.classList.remove('active');
    overlay.classList.remove('active');
  });
}

let categories = [];

async function loadCategoriesAdmin() {
  try {
    categories = await apiGet("/admin/categories");
    const tbody = document.getElementById("categoryTable");
    
    // 使用陣列拼接優化渲染效能
    tbody.innerHTML = categories.map(c => `
      <tr>
        <td><code style="color:#64748b;">#${c.categoryId}</code></td>
        <td><strong>${c.categoryName}</strong></td>
        <td>
          <button class="btn-edit" onclick="editCategory(${c.categoryId})">✏️ 編輯</button>
          <button class="btn-delete" onclick="deleteCategory(${c.categoryId})">🗑 刪除</button>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error("載入分類失敗", err);
  }
}

function openCreateCategory() {
  document.getElementById("editCategoryId").value = "";
  document.getElementById("editCategoryName").value = "";
  document.getElementById("modalTitle").textContent = "➕ 新增分類";
  document.getElementById("categoryModal").style.display = "flex";
}

function editCategory(id) {
  const c = categories.find(x => x.categoryId === id);
  if (!c) return;

  document.getElementById("editCategoryId").value = c.categoryId;
  document.getElementById("editCategoryName").value = c.categoryName;
  document.getElementById("modalTitle").textContent = "✏️ 編輯分類";
  document.getElementById("categoryModal").style.display = "flex";
}

async function saveCategory() {
  const id = document.getElementById("editCategoryId").value;
  const name = document.getElementById("editCategoryName").value.trim();
  if (!name) return alert("請輸入分類名稱");

  try {
    if (id) {
      await apiPut(`/admin/categories/${id}`, { categoryName: name });
    } else {
      await apiPost(`/admin/categories`, { categoryName: name });
    }
    closeCategoryModal();
    loadCategoriesAdmin();
  } catch (e) {
    alert(e.message);
  }
}

async function deleteCategory(id) {
  if (!confirm("確定刪除此分類？")) return;
  try {
    await apiFetch(`/admin/categories/${id}`, { method: "DELETE" });
    loadCategoriesAdmin();
  } catch (e) {
    alert(e.message);
  }
}

function closeCategoryModal() {
  document.getElementById("categoryModal").style.display = "none";
}

loadCategoriesAdmin();
