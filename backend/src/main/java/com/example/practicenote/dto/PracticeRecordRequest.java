package com.example.practicenote.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** 登録・更新で受け取る内容(入力ルールは要件整理と同じ) */
public record PracticeRecordRequest(

        @NotNull(message = "練習日を入力してください")
        LocalDate practiceDate,

        @NotBlank(message = "練習内容を入力してください")
        @Size(max = 200, message = "練習内容は200字以内で入力してください")
        String content,

        @NotNull(message = "練習時間を入力してください")
        @Min(value = 1, message = "練習時間は1〜999で入力してください")
        @Max(value = 999, message = "練習時間は1〜999で入力してください")
        Integer durationMin,

        @Size(max = 500, message = "課題・反省は500字以内で入力してください")
        String reflection,

        @Size(max = 200, message = "次回の目標は200字以内で入力してください")
        String nextGoal
) {
}
