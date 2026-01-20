async function loadDashboard() {
  try {
    const [stats, latest] = await Promise.all([
      apiGet("/admin/dashboard/stats"),
      apiGet("/admin/dashboard/latest-products")
    ]);

    animateNumber("productCount", stats.productCount);
    animateNumber("categoryCount", stats.categoryCount);
    animateNumber("updateCount", stats.todayCreated + stats.todayUpdated);

    renderLatest(latest);
  } catch (err) {
    console.error(err);
  }
}

function renderLatest(list) {
  const box = document.getElementById("latestProducts");

  if (!list || list.length === 0) {
    box.innerHTML = `<p style="padding:20px;color:#94a3b8">尚無資料</p>`;
    return;
  }

  box.innerHTML = list.map(p => `
    <div class="product-item" onclick="location.href='admin-products.html?id=${p.productId}'">
      <div class="product-icon">📦</div>
      <div class="product-info">
        <h4>${p.productName}</h4>
        <span>${p.brand || '無品牌'} · ${p.category.categoryName}</span>
      </div>
    </div>
  `).join("");
}

function animateNumber(id, target) {
  const el = document.getElementById(id);
  if (isNaN(target)) {
    el.textContent = "0";
    return;
  }

  let current = 0;
  const step = Math.max(1, Math.ceil(target / 20));

  const timer = setInterval(() => {
    current += step;
    if (current >= target) {
      el.textContent = target.toLocaleString();
      clearInterval(timer);
    } else {
      el.textContent = current.toLocaleString();
    }
  }, 30);
}

document.addEventListener("DOMContentLoaded", loadDashboard);
