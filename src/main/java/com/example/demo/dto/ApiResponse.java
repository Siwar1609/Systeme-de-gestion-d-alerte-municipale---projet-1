package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;

    public static ApiResponse success(String message) {
        return new ApiResponse(message, true);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(message, false);
    }
}