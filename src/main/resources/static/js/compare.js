const params = new URLSearchParams(window.location.search);
const id = params.get("id");

apiGet(`/products/${id}/prices`).then(prices => {
  const table = document.getElementById("priceTable");

  prices.forEach(p => {
    const row = `
      <tr>
        <td>${p.store.storeName}</td>
        <td>${p.price}</td>
        <td>${p.priceDate}</td>
      </tr>
    `;
    table.innerHTML += row;
  });
});
