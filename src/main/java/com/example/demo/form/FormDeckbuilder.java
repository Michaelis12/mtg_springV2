package com.example.demo.form;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDeckbuilder {
		
	private String pseudo;
	
	private String avatar;
	
	private String email;
	
	private String password;
	
	private String bio;
	
	private String confirmPassword;
	
}
