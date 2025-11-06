package com.example.demo.dto;


import java.time.LocalDateTime;

import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetNotification {
	
	private Long id;
	
    private Long deckID;
	
	private Long issuerID;
	
	private Long receivorID;
	
	private LocalDateTime date;
	
	private String deckName;
	
	private String issuerPseudo;
	
	private String receivorPseudo;
	
	
}
