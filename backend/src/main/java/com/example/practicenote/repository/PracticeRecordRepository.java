package com.example.practicenote.repository;

import com.example.practicenote.entity.PracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {

    /** 練習日の新しい順(同じ日は登録日時の新しい順) */
    List<PracticeRecord> findAllByOrderByPracticeDateDescCreatedAtDesc();
}
