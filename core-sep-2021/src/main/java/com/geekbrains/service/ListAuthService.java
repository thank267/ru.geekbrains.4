package com.geekbrains.service;

import com.geekbrains.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListAuthService implements AuthService<User> {

    private static User user;

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
    public Optional<User> findByLoginOrNick(String login, String nick) {
        return users.stream().filter(user -> user.getLogin().equals(login) || user.getNickname().equals(nick)).findFirst();
    }

    @Override
    public User updateNickByUser(User user, String newNick) {
        return null;
    }

    @Override
    public User getUser() {
        return user;
    }


    @Override
    public void setUser(User usr) {
        user = usr;
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
        return users;
    }
}
