package com.example.practicenote.service;

import com.example.practicenote.dto.RegisterRequest;
import com.example.practicenote.entity.AppUser;
import com.example.practicenote.exception.LoginIdAlreadyExistsException;
import com.example.practicenote.repository.AppUserRepository;
import com.example.practicenote.repository.PracticeRecordRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AccountService {

    private final AppUserRepository users;
    private final PracticeRecordRepository records;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository users, PracticeRecordRepository records,
                          PasswordEncoder passwordEncoder) {
        this.users = users;
        this.records = records;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public synchronized AppUser register(RegisterRequest request) {
        String loginId = request.loginId().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("パスワードはUTF-8で72バイト以内にしてください");
        }
        if (users.existsByLoginId(loginId)) {
            throw new LoginIdAlreadyExistsException();
        }

        boolean firstAccount = users.count() == 0;
        AppUser user = users.saveAndFlush(new AppUser(
                loginId,
                passwordEncoder.encode(request.password()),
                request.userName().trim(),
                request.role()));

        // 既存DBに登録済みの練習記録があれば、最初のアカウントに引き継ぐ
        if (firstAccount) {
            records.assignUnownedRecordsToUser(user.getUserId());
        }
        return user;
    }
}
