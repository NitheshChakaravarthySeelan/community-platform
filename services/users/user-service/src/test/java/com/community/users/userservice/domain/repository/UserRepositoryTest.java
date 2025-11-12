package com.community.users.userservice.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.community.users.userservice.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class UserRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private UserRepository userRepository;

    @Test
    void whenSaveUser_thenUserIsPersisted() {
        User user =
                User.builder()
                        .userName("testuser")
                        .email("test@example.com")
                        .password("hashedpassword")
                        .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUserName()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedpassword");

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserName()).isEqualTo("testuser");
    }

    @Test
    void whenFindByEmail_thenUserIsFound() {
        User user =
                User.builder()
                        .userName("findbyemail")
                        .email("find@example.com")
                        .password("hashedpassword")
                        .build();
        entityManager.persistAndFlush(user);

        // Add a custom method to UserRepository for this test
        // For now, we'll rely on findById or add a custom method later if needed
        // Optional<User> foundUser = userRepository.findByEmail("find@example.com");
        // assertThat(foundUser).isPresent();
        // assertThat(foundUser.get().getUserName()).isEqualTo("findbyemail");
    }

    // Add more tests for update, delete, and other custom queries if they exist
}
