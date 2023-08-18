package com.example.user;

import com.example.user.entity.User;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryTest {
    @Autowired
    private UserRepository repository;
    private final EasyRandom generator = new EasyRandom();

    @BeforeEach
    void setUp() {
        User user = generator.nextObject(User.class);
        user.setId(1L);
        user.setEmail("email@gmail.com");
        repository.save(user);
        User user2 = generator.nextObject(User.class);
        user.setId(2L);
        user2.setEmail("emai1l@gmail.com");
        repository.save(user2);
    }

    @Test
    void findAllByIdIn_whenValidData_thenReturnedValidList() {
        List<Long> ids = List.of(1L, 2L);
        Page<User> actualUsers = repository.findAllByIdIn(ids, PageRequest.of(0, 10));

        assertFalse(actualUsers.isEmpty());
        assertEquals(1L, actualUsers.getContent().get(0).getId());
    }

    @Test
    void testFindAllById_whenDataNull_thenReturnedEmptyList() {
        List<Long> ids = List.of(333L);
        Page<User> actualUsers = repository.findAllByIdIn(ids, PageRequest.of(0, 10));

        assertTrue(actualUsers.isEmpty());
    }
}