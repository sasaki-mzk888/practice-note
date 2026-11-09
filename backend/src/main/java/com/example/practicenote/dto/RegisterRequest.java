package com.example.practicenote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.practicenote.entity.AccountRole;

public record RegisterRequest(
        @NotBlank(message = "ログインIDを入力してください")
        @Size(min = 3, max = 100, message = "ログインIDは3〜100文字で入力してください")
        String loginId,

        @NotBlank(message = "パスワードを入力してください")
        @Size(min = 8, max = 64, message = "パスワードは8〜64文字で入力してください")
        String password,

        @NotBlank(message = "表示名を入力してください")
        @Size(max = 80, message = "表示名は80文字以内で入力してください")
        String userName,

        @NotNull(message = "アカウント種別を選択してください")
        AccountRole role
) { }
