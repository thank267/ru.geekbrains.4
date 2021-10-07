package com.geekbrains.operation;

import com.geekbrains.model.User;
import com.geekbrains.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class RegisterUserOperation implements UserOperation{

	private User user;

	@Override
	public UserOperation execute(AuthService<User> authService) {

		if (authService.findByLoginOrNick(user.getLogin(), user.getNickname()).isPresent()) {
			return new FailedUserOperation(null);
		}

		else {
			Optional<User> optionalUser = authService.save(user);

			if (optionalUser.isPresent()) {
				return new OkUserOperation(optionalUser.get());
			} else {
				return new FailedUserOperation(null);
			}
		}


	}
}
