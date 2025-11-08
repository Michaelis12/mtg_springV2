package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import com.example.demo.entities.DeckCreator;
import com.example.demo.entities.Notification;
import com.example.demo.enums.CardType;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumEdition;
import com.example.demo.enums.EnumFormat;
import com.example.demo.enums.EnumRarity;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormCedh;
import com.example.demo.form.FormDeck;
import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.utils.Sanitizer;
import com.example.demo.services.FileService;

@Service
public class CardService implements ICardService {
	
	@Autowired
	CardRepository cardRepository;

		@Override
		public List<String> getAllCardImages() {
			List<Card> cards = cardRepository.findAll();
			return cards.stream()
				.map(Card::getImage)
				.filter(image -> image != null && image.startsWith("/uploads/"))
				.collect(Collectors.toList());
		}
		
		
		@Override
		public List<GetCard> getTop3Cards() {
			
			List<Card> topCards = cardRepository.findAll();					
			List<GetCard> topGetCards = new ArrayList<>();
			
			topCards.removeIf(card -> card.getDecksNumber() == null);
			topCards.removeIf(card -> card.getTypes().contains("Basic"));						
			topCards.sort(Comparator.comparingLong(Card::getDecksNumber).reversed());
			
			for (Card card : topCards) {
				
				if(topGetCards.size() == 3) {
					break; }
				
					 GetCard cardReturn  = new GetCard();
					 cardReturn.setId(card.getId());
					 cardReturn.setApiID(card.getApiID());
					 cardReturn.setName(card.getName());
					 cardReturn.setText(card.getText());
					 cardReturn.setImage(card.getImage());
					 cardReturn.setManaCost(card.getManaCost());
					 cardReturn.setCmc(card.getCmc());
					 cardReturn.setDecksNumber(card.getDecksNumber());
					 cardReturn.setLegendary(card.isLegendary());
					
					 topGetCards.add(cardReturn);				
			}
			
			return topGetCards;
		}
		
		
		@Override
		public List<GetCard> getTop3Cedh() {
			
			List<Card> topCards = cardRepository.findAll();					
			List<GetCard> topGetCards = new ArrayList<>();
						
			topCards.removeIf(card -> card.getCedhNumber() == null);
						
			topCards.sort(Comparator.comparingLong(Card::getCedhNumber).reversed());
			
			for (Card card : topCards) {
				
				if(topGetCards.size() == 3) {
					break; }
				
					 GetCard cardReturn  = new GetCard();
					 cardReturn.setId(card.getId());
					 cardReturn.setApiID(card.getApiID());
					 cardReturn.setName(card.getName());
					 cardReturn.setText(card.getText());
					 cardReturn.setImage(card.getImage());
					 cardReturn.setManaCost(card.getManaCost());
					 cardReturn.setCmc(card.getCmc());
					 cardReturn.setCedhNumber(card.getCedhNumber());
					 cardReturn.setLegendary(card.isLegendary());
					
					 topGetCards.add(cardReturn);				
			}
			
			return topGetCards;
		}
		
		
		@Override
		public List<Long> getAllCardsRanked (String order) {

		   
		    // Récupération de toutes les cartes correspondant aux filtres
			
		    List<Card> allCards = cardRepository.findAll();
		    
		    if(order.equals("cedh")) {
			    // Trier par nb d'utilsiation cedh
		    	List<Card> cedhCards = allCards.stream()
		    	        .filter(card -> card.getCedhNumber() != null)
		    	        .collect(Collectors.toList());
		    	allCards = cedhCards;
		    	allCards.sort(Comparator.comparingLong(Card::getCedhNumber).reversed());
		    	
			    }
		    
		    if(order.equals("deck")) {
			    // Trier par nb d'utilisation en deck
		    	List<Card> deckCards = allCards.stream()
		    	        .filter(card -> card.getDecksNumber() != null)
		    	        .collect(Collectors.toList());
		    	allCards = deckCards;
		    	allCards.removeIf(card -> card.getTypes().contains("Basic"));
		    	allCards.sort(Comparator.comparingLong(Card::getDecksNumber).reversed());
			    }
		   

		    List<Long> cardsID = allCards.stream()
		            .map(Card::getId) 
		            .collect(Collectors.toList());
		    		
		    return cardsID;

		}
		
		
		@Override
		public Page<GetCard> getCardsByFilterPaged (
		        int page,
		        int size,
		        String order,
		        String name,
		        String text,
		        Double cmcMin,
		        Double cmcMax,
		        List<String> types,
		        Boolean legendary,
		        List<String> rarities,
		        List<String> colors,
		        List<String> formats,
		        List<String> editions) {

		   
		    // Récupération de toutes les cartes correspondant aux filtres
			
		    List<Card> allFilteredCards = cardRepository.findByAttributes(
		      name, text, cmcMin, cmcMax, types, legendary, rarities, colors, formats, editions
		    );
		    
		    if(order.equals("cedh")) {
			    // Trier par nb d'utilsiation cedh
		    	List<Card> cedhCards = allFilteredCards.stream()
		    	        .filter(card -> card.getCedhNumber() != null)
		    	        .collect(Collectors.toList());
		    	allFilteredCards = cedhCards;
		    	allFilteredCards.sort(Comparator.comparingLong(Card::getCedhNumber).reversed());
		    	
			    }
		    
		    if(order.equals("deck")) {
			    // Trier par nb d'utilisation en deck
		    	List<Card> deckCards = allFilteredCards.stream()
		    	        .filter(card -> card.getDecksNumber() != null)
		    	        .collect(Collectors.toList());
		    	deckCards.removeIf(card -> card.getTypes().contains("Basic"));
		    	allFilteredCards = deckCards;
		    	allFilteredCards.sort(Comparator.comparingLong(Card::getDecksNumber).reversed());
			    }

		    // Pagination manuelle
		    int start = page * size;
		    int end = Math.min(start + size, allFilteredCards.size());

		    if (start >= allFilteredCards.size()) {
		        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), allFilteredCards.size());
		    }

		    List<Card> paginatedCards = allFilteredCards.subList(start, end);

		    // Mapping vers GetCard
		    List<GetCard> getCardList = paginatedCards.stream()
		            .map(card -> {
		                GetCard cardReturn = new GetCard();
						cardReturn.setId(card.getId());
						cardReturn.setApiID(card.getApiID());
						cardReturn.setName(card.getName());
						cardReturn.setText(card.getText());
						cardReturn.setImage(card.getImage());
						cardReturn.setManaCost(card.getManaCost());
						cardReturn.setCmc(card.getCmc());
						cardReturn.setCedhNumber(card.getCedhNumber());
						cardReturn.setDecksNumber(card.getDecksNumber());
						cardReturn.setTypes(card.getTypes());
						cardReturn.setColors(card.getColors());
						cardReturn.setFormats(card.getFormats());
						cardReturn.setLegendary(card.isLegendary());


		                return cardReturn;
		            })
		            .collect(Collectors.toList());

		    return new PageImpl<>(getCardList, PageRequest.of(page, size), allFilteredCards.size());
		}		

}
