package com.example.demo.Security;


import com.example.demo.DB.UserAPI.AppUser;
import com.example.demo.DB.UserAPI.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ApplicationUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser =userRepo.findByLogin(username).orElseThrow(()->new RuntimeException("User 404"));
        return new UserPrincipal(appUser);
    }

}