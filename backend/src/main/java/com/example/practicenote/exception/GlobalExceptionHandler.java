package com.example.practicenote.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** エラーの返し方(API仕様書・基本設計書の表に対応) */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400: 必須未入力・文字数や数値の範囲外 → 項目ごとの理由を返す */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "入力内容に誤りがあります", "errors", fieldErrors));
    }

    /** 400: JSONの形式や日付の書式が正しくない */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "入力内容に誤りがあります"));
    }

    /** 404: 指定した記録が存在しない */
    @ExceptionHandler(PracticeRecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(PracticeRecordNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "記録が見つかりません"));
    }

    /** 500: その他の内部エラー(記録の中身はログに出さない) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "処理に失敗しました"));
    }
}
