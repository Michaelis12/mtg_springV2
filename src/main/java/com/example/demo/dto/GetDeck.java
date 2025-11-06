package com.example.demo.dto;

import java.sql.Blob;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.demo.entities.Card;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDeck {
		
		private Long id;

		private String name;
		
		private LocalDate dateCreation;
		
		private LocalDate datePublication;
		
		private String image;
		
		private EnumFormat format;
		
		private Set<EnumColor> colors = new HashSet<>();
		
		private Float manaCost;
		
		private Float value;
		
		private Boolean isPublic;

	    private DeckCreator deckBuilder;
	    
	    private String deckBuilderName;
	    
		private List<Card> cards;
		
		private Card commander;
			
		private Long likeNumber; 


		



}
