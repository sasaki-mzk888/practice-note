package com.example.practicenote.controller;

import com.example.practicenote.dto.PracticeRecordRequest;
import com.example.practicenote.entity.PracticeRecord;
import com.example.practicenote.service.PracticeRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.practicenote.security.AccountPrincipal;

import java.util.List;

/** 練習記録API(API仕様書の5本) */
@RestController
@RequestMapping("/api/practices")
public class PracticeRecordController {

    private final PracticeRecordService service;

    public PracticeRecordController(PracticeRecordService service) {
        this.service = service;
    }

    /** 練習記録一覧取得API: GET /api/practices */
    @GetMapping
    public List<PracticeRecord> list() {
        return service.findAll();
    }

    /** 練習記録詳細取得API: GET /api/practices/{id} */
    @GetMapping("/{id}")
    public PracticeRecord get(@PathVariable Long id) {
        return service.findById(id);
    }

    /** 練習記録登録API: POST /api/practices(成功時は201) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeRecord create(@Valid @RequestBody PracticeRecordRequest request,
                                 @AuthenticationPrincipal AccountPrincipal user) {
        return service.create(request, user.getUserId());
    }

    /** 練習記録更新API: PUT /api/practices/{id} */
    @PutMapping("/{id}")
    public PracticeRecord update(@PathVariable Long id, @Valid @RequestBody PracticeRecordRequest request) {
        return service.update(id, request);
    }

    /** 練習記録削除API: DELETE /api/practices/{id} */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
