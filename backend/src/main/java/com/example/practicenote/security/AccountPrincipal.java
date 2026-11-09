package com.example.practicenote.security;

import com.example.practicenote.entity.AppUser;
import com.example.practicenote.entity.AccountRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountPrincipal implements UserDetails {

    private final Long userId;
    private final String loginId;
    private final String passwordHash;
    private final String userName;
    private final AccountRole role;

    public AccountPrincipal(AppUser user) {
        this.userId = user.getUserId();
        this.loginId = user.getLoginId();
        this.passwordHash = user.getPasswordHash();
        this.userName = user.getUserName();
        this.role = user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return loginId; }
    public Long getUserId() { return userId; }
    public String getLoginId() { return loginId; }
    public String getUserName() { return userName; }
    public AccountRole getRole() { return role; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
