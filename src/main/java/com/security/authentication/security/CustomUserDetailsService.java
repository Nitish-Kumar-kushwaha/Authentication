package com.security.authentication.security;

import com.security.authentication.entity.AdminUsers;
import com.security.authentication.entity.Users;
import com.security.authentication.repository.AdminUserRepository;
import com.security.authentication.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userRepository;
    private final AdminUserRepository adminUserRepository;


    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the <code>UserDetails</code>
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param identifier the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String[] parts = identifier.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid identifier format");
        }
        String userName = parts[0];
        String userType = parts[1];

        switch (userType.toLowerCase()) {
            case "admin_user":
                AdminUsers admin = adminUserRepository.findByUsername(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("Admin user not found: " + userName));

                return CustomUserDetails.fromAdmin(admin);
            case "user":
                Users user =  userRepository.findByUsername(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));

                return CustomUserDetails.fromUser(user);
            default:
                throw new UsernameNotFoundException("Unknown user type: " + userType);
        }
    }
}
