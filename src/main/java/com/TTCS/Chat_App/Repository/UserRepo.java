package com.TTCS.Chat_App.Repository;

import com.TTCS.Chat_App.Model.User;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Registered
public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

}
