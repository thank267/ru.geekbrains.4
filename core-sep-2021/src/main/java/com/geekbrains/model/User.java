package com.geekbrains.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
	private String login;
	private String password;
	private String nickname;
}
