package com.gooddeal.exception;

public enum ApiErrorCode {
	DUPLICATE_TODAY("今日已回報過此商品"),
    SAME_PRICE_RECENT("相同價格剛剛已回報過"),
    TOO_FREQUENT("回報過於頻繁，請稍後再試"),
    PRODUCT_NOT_FOUND("商品不存在"),
    STORE_NOT_FOUND("店家不存在"),
    USER_NOT_FOUND("會員不存在");

    private final String message;

    ApiErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
