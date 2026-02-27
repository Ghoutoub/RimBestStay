package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.Role;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getMotDePasse(),
            mapRolesToAuthorities(user.getRoles())
        );
    }
    
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());
    }
}