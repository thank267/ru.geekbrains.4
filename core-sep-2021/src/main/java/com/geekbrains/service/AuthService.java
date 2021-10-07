package com.geekbrains.service;

import com.geekbrains.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Created by Artem Kropotov on 24.05.2021
 */
public interface AuthService<T> extends CrudService<T, Long> {
    Optional<User> findByLoginAndPassword(String login, String password);
    Optional<User> findByLoginOrNick(String login, String nick);
    User updateNickByUser(User user, String newNick);
    User getUser();
    void setUser(User user);
}
