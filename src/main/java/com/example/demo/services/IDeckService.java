package com.example.demo.services;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;
import com.example.demo.form.FormCedh;
import com.example.demo.form.FormDeck;

public interface IDeckService {
	
		List<GetCard> getCardsOnDeck(Long deckID);
		String addCardsOnDeck(List<Card> cards, Long deckId);
		String duplicateCardsOnDeck(List<Long> cardsId, Long deckId);
		String deleteCardOnDeck(Long cardId, Long deckId);
		String deleteCardsOnDeck(Long cardId, Long deckId);
		String deleteCardsOnDeck(List<Long> cardId, Long deckId);
	
	
		// Methodes f_all		
		List<GetDeck> getTop3Decks();
		
		Page<GetDeck> getDecksByFilterPaged(int page, int size, String order, String name, Float manaCostMin,
				Float manaCostMax, List<EnumFormat> formats, List<EnumColor> colors);
		
		GetDeck getDeckById(Long deckID);
		Long getNextDeck( Long deckID, List<Long> decksID);
		Long getPrevDeck( Long deckID, List<Long> decksID);
		List<GetDeck> getDecksByUser(DeckCreator dbuilder);
		Long getDeckUser(Long deckID);
		List<GetCard> get7CardsOnDeck(Long deckID);
		GetCard getCommandantOndeck(Long deckID);
		int getNumberDecksByUser(DeckCreator dbuilder);
	
		// Methodes f_user
		
		Page<GetDeck> getDecksCreateByFilterPaged(DeckCreator user, int page, int size, String order, String name,
				Float manaCostMin, Float manaCostMax, List<EnumFormat> formats, List<EnumColor> colors);
		void likeDeck(DeckCreator user, Long deckID);
		void dislikeDeck(DeckCreator user, Long deckID);
		Set<Long> getDecksLiked(DeckCreator user);
		Page<GetDeck> getDecksLikedPaged (DeckCreator user, int page, int size);
		Page<GetDeck> getDecksLikedByFilterPaged(DeckCreator user, int page, int size, String order, String name,
				Float manaCostMin, Float manaCostMax, List<EnumFormat> formats, List<EnumColor> colors);
		Page<GetDeck> getDecksUserPaged(DeckCreator user, int page, int size, String order);	
		Long addDeck (DeckCreator dbuilder, FormDeck deckRegister );
		Long addCedh (DeckCreator dbuilder, FormCedh deckRegister);	
		void deleteDeck(DeckCreator user, Long deckID);
		String updateDeck(Long deckID, FormDeck deckUpdate);	
		String setNumberCardOnDeck(Long cardID, Long deckID, int number);	
		String publishDeck(DeckCreator user, Long deckID);
		String privateDeck(DeckCreator user, Long deckID);	
		Float getDeckManaCost(Long deckID);
		List<String> getAllDeckImages();
		





	


	


	

	

}
