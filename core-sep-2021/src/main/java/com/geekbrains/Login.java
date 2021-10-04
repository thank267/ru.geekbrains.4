package com.geekbrains;

import com.geekbrains.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Login implements Serializable {

	private LoginType type;

	private User user;

}
