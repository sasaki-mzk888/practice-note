package com.example.practicenote.repository;

import com.example.practicenote.entity.PracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {

    /** 練習日の新しい順(同じ日は登録日時の新しい順) */
    List<PracticeRecord> findAllByOrderByPracticeDateDescCreatedAtDesc();

    @Modifying
    @Query("update PracticeRecord practice set practice.userId = :userId where practice.userId is null")
    int assignUnownedRecordsToUser(@Param("userId") Long userId);
}
