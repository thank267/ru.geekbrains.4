package com.geekbrains.operation;

import com.geekbrains.model.Command;
import com.geekbrains.model.User;
import com.geekbrains.service.AuthService;
import com.geekbrains.utils.FileHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class LoginUserOperation implements UserOperation{

	private User user;

	@Override
	public UserOperation execute(AuthService<User> authService) {

		Optional<User> optionalUser = authService.findByLoginAndPassword(user.getLogin(), user.getPassword());


		if (optionalUser.isPresent()) {
			return new OkUserOperation(optionalUser.get());
		} else {
			return new FailedUserOperation(null);
		}
	}
}
