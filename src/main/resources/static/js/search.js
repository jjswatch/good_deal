const params = new URLSearchParams(window.location.search);
const keyword = params.get("q");

apiGet(`/products/search?keyword=${encodeURIComponent(keyword)}`)
  .then(result => {

    console.log("🔍 後端回傳資料:", result);

    // 如果後端不是回傳陣列 → 強制轉為空陣列，避免報錯
    let products = [];

    if (Array.isArray(result)) {
      products = result;
    } else {
      console.warn("⚠️ 後端回傳的不是陣列，實際為:", typeof result);
    }

    // 沒資料就顯示提示
    if (products.length === 0) {
      document.getElementById("resultList").innerHTML =
        "<li>查無商品</li>";
      return;
    }

    // 渲染商品清單
    let html = "";
    products.forEach(p => {
      html += `
        <li>
          <img src="${p.imageUrl || 'assets/placeholder.png'}" class="thumb">
          ${p.productName}
          <a href="product.html?id=${p.productId}">查看</a>
        </li>
      `;
    });

    document.getElementById("resultList").innerHTML = html;
  })
  .catch(err => {
    console.error("❌ API 錯誤:", err);
    document.getElementById("resultList").innerHTML =
      "<li>無法取得資料</li>";
  });
