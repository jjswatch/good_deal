const params = new URLSearchParams(window.location.search);
const id = params.get("id");

apiGet(`/products/${id}`).then(p => {
	document.getElementById("productInfo").innerHTML = `
    <h2>${p.brand}${p.productName}</h2>
    <img src="${p.imageUrl || 'assets/placeholder.png'}" class="main-img">
    <p>${p.description || "無商品介紹"}</p>
	<p>規格：${p.spec}</p>
	<p>條碼：${p.barcode}</p>
  `;

	document.getElementById("compareBtn").href = `compare.html?id=${id}`;
	document.getElementById("historyBtn").href = `history.html?productId=${id}`;
});
