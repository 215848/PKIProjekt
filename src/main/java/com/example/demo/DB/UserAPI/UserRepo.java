package com.example.demo.DB.UserAPI;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<AppUser,Integer> {
    Optional<AppUser> findByLogin(String login);
    AppUser findById(int id);
}