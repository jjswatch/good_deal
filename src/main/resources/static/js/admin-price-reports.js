document.addEventListener("DOMContentLoaded", loadReports);

function loadReports() {
  apiGet("/admin/price-reports/pending")
    .then(renderTable)
    .catch(err => {
      console.error(err);
      document.getElementById("reportBody").innerHTML =
        `<tr><td colspan="6" class="empty">載入失敗</td></tr>`;
    });
}

function renderTable(reports) {
  const tbody = document.getElementById("reportBody");
  const countSpan = document.getElementById("pendingCount");
  
  if (!reports || reports.length === 0) {
    countSpan.innerText = "0";
    tbody.innerHTML = `<tr><td colspan="6" class="empty">🎉 目前沒有待審核回報</td></tr>`;
    return;
  }

  countSpan.innerText = reports.length;

  tbody.innerHTML = reports.map(r => `
    <tr>
      <td data-label="商品資訊" style="padding-left:24px;">
        <div class="product-info-cell">
          <img src="${r.product.imageUrl || '../assets/placeholder.png'}" class="product-thumb">
          <div style="text-align:left">
            <div style="font-weight:600; color:var(--text-main)">${r.product.productName}</div>
            <div style="font-size:12px; color:var(--text-sub)">ID: ${r.product.productId}</div>
          </div>
        </div>
      </td>
      <td data-label="店家">${r.store.storeName}</td>
      <td data-label="回報價格"><span class="price-tag">$${r.reportedPrice}</span></td>
      <td data-label="回報者"><span class="user-tag">👤 ${r.user.username}</span></td>
      <td data-label="時間" class="time-tag">${new Date(r.reportedAt).toLocaleString('zh-TW', {hour12:false})}</td>
      <td data-label="操作" style="text-align:right; padding-right:24px;">
        <button class="approve btn-action" onclick="approve(${r.reportId}, this)">✓ 通過</button>
        <button class="reject btn-action" onclick="reject(${r.reportId}, this)">✕ 拒絕</button>
      </td>
    </tr>
  `).join('');
}

function approve(id, btn) {
  if (!confirm("確定通過此價格回報？")) return;

  apiPost(`/admin/price-reports/${id}/approve`)
    .then(() => removeRow(btn))
    .catch(() => alert("操作失敗"));
}

function reject(id, btn) {
  if (!confirm("確定拒絕此回報？")) return;

  apiPost(`/admin/price-reports/${id}/reject`)
    .then(() => removeRow(btn))
    .catch(() => alert("操作失敗"));
}

function removeRow(btn) {
  btn.closest("tr").remove();

  const tbody = document.getElementById("reportBody");
  if (tbody.children.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty">目前沒有待審核回報</td></tr>`;
  }
}
