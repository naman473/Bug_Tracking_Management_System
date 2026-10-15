package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.AppUserDetails;
import com.startechies.bugtracker.model.User;
import com.startechies.bugtracker.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));

        // Authority string comes from the Role enum — consistent with SecurityConfig
        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User " + email + " has no role assigned");
        }
        String authority = user.getRole().authority;

        // AppUserDetails carries userName so close-bug and comment handlers need no extra DB query
        return new AppUserDetails(
                user.getEmailId(), user.getPassword(), user.getUserName(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority(authority)));
    }
}
