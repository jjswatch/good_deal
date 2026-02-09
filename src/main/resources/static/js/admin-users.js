// admin-users.js
const API_URL = '/api/admin/users';

document.addEventListener("DOMContentLoaded", () => {
    fetchUsers();
});

// 1. 取得所有使用者
async function fetchUsers() {
    try {
        const response = await fetch(API_URL, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
        });
        const users = await response.json();
        renderUserTable(users);
    } catch (error) {
        console.error("載入失敗:", error);
    }
}

// 2. 渲染表格
function renderUserTable(users) {
    const tbody = document.getElementById('userTableBody');
    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.userId}</td>
            <td>${user.username}</td>
            <td>${user.email}</td>
			<td><span class="role-badge ${user.role}">${user.role}</span></td>
            <td>
                <button class="btn btn-edit" onclick="openEditModal(${user.userId})">編輯</button>
                <button class="btn btn-delete" onclick="deleteUser(${user.userId})">刪除</button>
            </td>
        </tr>
    `).join('');
}

// 3. 開啟編輯視窗
async function openEditModal(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
        });
		
		if (!response.ok) throw new Error("取得資料失敗");
		
        const user = await response.json();
        
        document.getElementById('editUserId').value = user.userId;
        document.getElementById('editUsername').value = user.username;
        document.getElementById('editEmail').value = user.email;
		document.getElementById('editRole').value = user.role;
		document.getElementById('editPassword').value = ""; // 密碼欄位清空
        
		document.getElementById('userModal').classList.add('active');
		console.log("Modal 已開啟，目前處理使用者 ID:", id);
    } catch (error) {
        alert("讀取使用者資料失敗，請檢查網路或權限");
    }
}

function closeModal() {
    document.getElementById('userModal').classList.remove('active');
}

// 4. 儲存變更 (Update)
async function saveUser() {
    const id = document.getElementById('editUserId').value;
    const data = {
        username: document.getElementById('editUsername').value,
        email: document.getElementById('editEmail').value,
		role: document.getElementById('editRole').value
    };
    
    // 如果有輸入新密碼才傳送（註：後端也需要配合邏輯處理）
    const pwd = document.getElementById('editPassword').value;
    if (pwd) data.passwordHash = pwd; 

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem("token")}`
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            alert("更新成功");
            closeModal();
            fetchUsers();
        }
    } catch (error) {
        alert("更新失敗");
    }
}

// 5. 刪除使用者 (Delete)
async function deleteUser(id) {
    if (!confirm("確定要刪除此使用者嗎？此操作無法還原。")) return;

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
        });

        if (response.ok) {
            fetchUsers();
        }
    } catch (error) {
        alert("刪除失敗");
    }
}