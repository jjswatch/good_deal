package com.gooddeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private Integer userId;
    private String username;
    private String email;
}
