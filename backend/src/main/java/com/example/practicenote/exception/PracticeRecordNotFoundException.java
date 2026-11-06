package com.example.practicenote.exception;

/** 指定した練習記録が存在しないとき(404) */
public class PracticeRecordNotFoundException extends RuntimeException {

    public PracticeRecordNotFoundException(Long id) {
        super("記録が見つかりません(id=" + id + ")");
    }
}
