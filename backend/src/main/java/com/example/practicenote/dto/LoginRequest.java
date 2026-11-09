package com.example.practicenote.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "ログインIDを入力してください") String loginId,
        @NotBlank(message = "パスワードを入力してください") String password
) { }
