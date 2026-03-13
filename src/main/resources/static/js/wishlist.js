function getWishlist() {

    const user = getCurrentUser();
    if (!user) return [];

    const uid = user.userId || user.id;

    let list = JSON.parse(localStorage.getItem(`wishlist_${uid}`)) || [];

    // 清除舊資料
    list = list.filter(i => i.productId);

    return list;
}

function saveWishlist(list) {

    const user = getCurrentUser();
    if (!user) return;

    const uid = user.userId || user.id;

    localStorage.setItem(`wishlist_${uid}`, JSON.stringify(list));
}

function isProductInWishlist(productId) {

    const list = getWishlist();

    return list.some(i => String(i.productId) === String(productId));
}

function addToWishlist(product) {

    let list = getWishlist();

    const index = list.findIndex(i =>
        String(i.productId) === String(product.productId)
    );

    if (index > -1) {

        list[index] = product;

    } else {

        list.push(product);
    }

    saveWishlist(list);
}

function removeFromWishlist(productId) {

    let list = getWishlist();

    list = list.filter(i =>
        String(i.productId) !== String(productId)
    );

    saveWishlist(list);
}