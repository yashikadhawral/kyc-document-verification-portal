package com.kycportal.security;

import com.kycportal.domain.AppUser;
import com.kycportal.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads users from the database so roles come from the app_user table. */
@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public PortalUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = users.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        return User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .build();
    }
}
