export function renderProductList(products, containerId) {
	const el = document.getElementById(containerId);

	if (!products || products.length === 0) {
		el.innerHTML = "<li>查無商品</li>";
		return;
	}

	el.innerHTML = products.map(p => `
		<li class="item">
			<img src="${p.imageUrl || 'assets/placeholder.png'}">
			<div class="item-name">${p.brand || ""}${p.productName}</div>
			<a href="product.html?id=${p.productId}">查看</a>
		</li>
	`).join("");
}
