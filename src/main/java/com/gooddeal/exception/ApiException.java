package com.gooddeal.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ApiErrorCode code;

    public ApiException(ApiErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
