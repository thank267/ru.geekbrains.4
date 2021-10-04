package com.geekbrains.netty.service;

import com.geekbrains.user.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListAuthService implements AuthService<User> {

    private static ListAuthService INSTANCE;

    private final CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();

    private ListAuthService() {
        for (int i = 0; i <= 10; i++) {
            users.add(new User("login" + i, "pass" + i, "nick" + i));
        }
    }

    public static ListAuthService getInstance() {
        if (INSTANCE == null) {
            synchronized (ListAuthService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ListAuthService();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Optional<User> findByLoginAndPassword(final String login, final String password) {
        return users.stream().filter(user -> user.getLogin().equals(login) && user.getPassword().equals(password)).findFirst();
    }

    @Override
    public User findByLoginOrNick(String login, String nick) {
        for (User u : users) {
            if (u.getLogin().equals(login) || u.getNickname().equals(nick)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User updateNickByUser(User user, String newNick) {
        return null;
    }

    @Override
    public Optional<User> save(User user) {
        if (users.add(user)) return Optional.of(user);
        else {
            return Optional.empty();
        }
    }

    @Override
    public User remove(User user) {
        users.remove(user);
        return user;
    }

    // todo не надо
    @Override
    public User removeById(Long aLong) {
        return null;
    }

    // todo не надо
    @Override
    public User findById(Long aLong) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }
}
