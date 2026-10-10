package com.kycportal.config;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.Role;
import com.kycportal.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates one demo account per role the first time the app starts. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String seedPassword;

    public DataSeeder(AppUserRepository users,
                      PasswordEncoder encoder,
                      @Value("${kyc.seed.password}") String seedPassword) {
        this.users = users;
        this.encoder = encoder;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        String hash = encoder.encode(seedPassword);
        users.save(new AppUser("applicant1", hash, "Asha Kulkarni", Role.APPLICANT));
        users.save(new AppUser("verifier1", hash, "Rohan Mehta", Role.VERIFIER));
        users.save(new AppUser("admin1", hash, "Meera Nair", Role.ADMIN));
    }
}
