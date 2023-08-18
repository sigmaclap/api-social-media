package com.example.user;

import com.example.user.dto.UserDto;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(
        properties = {"spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.url=jdbc:h2:mem:test", "spring.datasource.username=test",
                "spring.datasource.password=test"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ITUserServiceImplTest {
    private final UserService userService;
    private final EntityManager entityManager;
    private final UserRepository repository;
    private final EasyRandom generator = new EasyRandom();

    User user;

    @BeforeEach
    void setUp() {
        user = generator.nextObject(User.class);
        user.setId(1L);
        user.setEmail("email@gmail.com");
        user.setPassword("123");
        repository.save(user);
    }


    @Test
    void createUser_whenValidData_ThenReturnedCreateUser() {
        UserDto userDtoToSave = UserMapper.toDtoUser(user);

        userService.createUser(userDtoToSave);

        TypedQuery<User> query = entityManager
                .createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User actualUser = query.setParameter("id", userDtoToSave.getId()).getSingleResult();

        assertThat(actualUser.getId(), notNullValue());
        assertThat(actualUser.getUsername(), equalTo(user.getUsername()));
        assertThat(actualUser.getEmail(), equalTo(user.getEmail()));
    }
}