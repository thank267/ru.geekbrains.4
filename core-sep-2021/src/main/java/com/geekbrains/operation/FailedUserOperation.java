package com.geekbrains.operation;

import com.geekbrains.model.User;
import com.geekbrains.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class FailedUserOperation implements UserOperation{

	private User user;

	@Override
	public UserOperation execute(AuthService<User> authService) {
		authService.setUser(null);
		return null;
	}
}
