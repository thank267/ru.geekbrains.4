package com.geekbrains.operation;

import com.geekbrains.model.User;
import com.geekbrains.service.AuthService;

import java.io.Serializable;

@FunctionalInterface
public interface UserOperation extends Serializable {
	UserOperation execute(AuthService<User> authService);
}
