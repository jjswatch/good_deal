const params = new URLSearchParams(window.location.search);
const id = params.get("id");

apiGet(`/history/product/${id}`).then(p => {
	document.getElementById("priceList").innerHTML = `
		<tr>
			<td>${p.changedAt}</td>
			<td>${p.storeName}</td>
			<td>${p.location}</td>
			<td class="price">$${p.newPrice}</td>
		</tr>
	`;
});
