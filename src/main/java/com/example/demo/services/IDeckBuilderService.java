package com.example.demo.services;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.dto.GetDeckBuilder;
import com.example.demo.dto.GetNotification;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormProfil;

public interface IDeckBuilderService {
	
	// All
	Long getUserLikes(DeckCreator deckBuilder);
	
	// User
	Set<GetNotification> getUserNotifs(DeckCreator deckBuilder);
	String deleteUserNotif(Long notifID);
	GetDeckBuilder getDeckBuilder(DeckCreator deckBuilder);
	
	
	// Admin
	Long getPrevDeckbuilder(Long userID, List<Long> usersID);
	Long getNextDeckbuilder(Long userID, List<Long> usersID);
	Page<GetDeckBuilder> getDeckBuildersByFilterPaged(int page, int size, String pseudo, String email, List<UserActivity> activities);
	String deleteDeckBuilder(Long dbID);	
	String activeAccount(Long userID);
	String suspendAccount(Long userID);
	String suspendAccountTemporarily(Long userID, int days);
	List<String> getAllUserAvatars();

	

	



	
	
	
	
}
