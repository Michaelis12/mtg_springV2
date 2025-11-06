package com.example.demo.services;

import java.util.Map;

import com.example.demo.dto.GetAuthentification;
import com.example.demo.entities.DeckCreator;
import com.example.demo.form.FormDeckbuilder;
import com.example.demo.form.FormProfil;

public interface IAuthenticationService {

	DeckCreator inscription(FormDeckbuilder formDeckBuilder);
	DeckCreator addAdmin(FormDeckbuilder formDeckBuilder);
	boolean verifyUser(String email, String code);
	boolean verifyUserSign(String email, String code);
	GetAuthentification connexion(Map<String, String> request);
	String sendCodePassword(String email);
	String resetPassword(String email, String newPassword, String confirmPassword, String code);
	String updateAccount(DeckCreator user, FormProfil userUpdate);
	String updatePassword(DeckCreator user, String password, String newPassword);
	String reactiveAccount(Long userID);
	String desactiveAccount(Long userID);
	void deleteAccount(DeckCreator user);
	
	
	
	

}
