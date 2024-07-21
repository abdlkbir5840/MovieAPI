package com.movieflex.auth.repositories;

import com.movieflex.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByUsername(String username);
}
