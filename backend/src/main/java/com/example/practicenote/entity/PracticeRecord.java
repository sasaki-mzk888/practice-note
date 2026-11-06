package com.example.practicenote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 練習記録(テーブル practice_records に対応) */
@Entity
@Table(name = "practice_records")
public class PracticeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_id")
    private Long practiceId;

    @Column(name = "practice_date", nullable = false)
    private LocalDate practiceDate;

    @Column(name = "content", nullable = false, length = 200)
    private String content;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "reflection", length = 500)
    private String reflection;

    @Column(name = "next_goal", length = 200)
    private String nextGoal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 登録時に登録日時・更新日時を自動で入れる */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 更新時に更新日時を自動で入れる */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getPracticeId() { return practiceId; }
    public LocalDate getPracticeDate() { return practiceDate; }
    public void setPracticeDate(LocalDate practiceDate) { this.practiceDate = practiceDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }
    public String getReflection() { return reflection; }
    public void setReflection(String reflection) { this.reflection = reflection; }
    public String getNextGoal() { return nextGoal; }
    public void setNextGoal(String nextGoal) { this.nextGoal = nextGoal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
