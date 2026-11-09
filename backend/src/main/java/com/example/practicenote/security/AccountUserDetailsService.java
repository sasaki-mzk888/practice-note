package com.example.practicenote.security;

import com.example.practicenote.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AccountUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return users.findByLoginId(loginId.trim().toLowerCase(java.util.Locale.ROOT))
                .map(AccountPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("アカウントが見つかりません"));
    }
}
