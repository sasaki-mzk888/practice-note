package com.example.practicenote.service;

import com.example.practicenote.dto.PracticeRecordRequest;
import com.example.practicenote.entity.PracticeRecord;
import com.example.practicenote.exception.PracticeRecordNotFoundException;
import com.example.practicenote.repository.PracticeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PracticeRecordService {

    private final PracticeRecordRepository repository;

    public PracticeRecordService(PracticeRecordRepository repository) {
        this.repository = repository;
    }

    /** 一覧取得(新しい日付順) */
    @Transactional(readOnly = true)
    public List<PracticeRecord> findAll() {
        return repository.findAllByOrderByPracticeDateDescCreatedAtDesc();
    }

    /** 詳細取得(なければ404) */
    @Transactional(readOnly = true)
    public PracticeRecord findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PracticeRecordNotFoundException(id));
    }

    /** 登録 */
    @Transactional
    public PracticeRecord create(PracticeRecordRequest request) {
        PracticeRecord record = new PracticeRecord();
        apply(record, request);
        return repository.save(record);
    }

    /** 更新(なければ404) */
    @Transactional
    public PracticeRecord update(Long id, PracticeRecordRequest request) {
        PracticeRecord record = findById(id);
        apply(record, request);
        return repository.save(record);
    }

    /** 削除(なければ404) */
    @Transactional
    public void delete(Long id) {
        PracticeRecord record = findById(id);
        repository.delete(record);
    }

    private void apply(PracticeRecord record, PracticeRecordRequest request) {
        record.setPracticeDate(request.practiceDate());
        record.setContent(request.content());
        record.setDurationMin(request.durationMin());
        record.setReflection(request.reflection());
        record.setNextGoal(request.nextGoal());
    }
}
