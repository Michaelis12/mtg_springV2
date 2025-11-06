package com.example.demo.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.CardType;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumEdition;
import com.example.demo.enums.EnumFormat;
import com.example.demo.enums.EnumRarity;
import com.example.demo.form.FormCedh;
import com.example.demo.form.FormDeck;
import com.example.demo.form.FormProfil;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.services.IAuthenticationService;
import com.example.demo.services.ICardService;
import com.example.demo.services.IDeckBuilderService;
import com.example.demo.services.IDeckService;

@RestController
@RequestMapping("f_user")
public class UserController {
	
	// Ici les méthodes déstiné à l'user connecté
	// créer, modifier, rechercher des decks accessibles par les users auth
	
	@Autowired
	private IAuthenticationService iAuthenticationService;
	@Autowired
	private IDeckBuilderService deckBuilderService;
	@Autowired
	private IDeckService iDeckService;
	@Autowired
	private ICardService iCardService;
	@Autowired
	private DeckBuilderRepository deckBuilderRepository;
		
	
	// Afficher sur le profil
	
	@GetMapping("getDeckBuilder")
	public ResponseEntity getDeckBuilder(Authentication authentication) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(deckBuilderService.getDeckBuilder(user.get()));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");

	}
	
	@GetMapping("getUserNotifs")
	public ResponseEntity getUserNotifs(Authentication authentication){
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		if(user.isPresent()) {
			return ResponseEntity.ok(deckBuilderService.getUserNotifs(user.get()));
		}
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}
	
	@DeleteMapping("deleteUserNotif")
	public ResponseEntity deleteUserNotif(Authentication authentication, Long notifID){
		
		return ResponseEntity.ok(deckBuilderService.deleteUserNotif(notifID));
		
		
	}
	
	
	
	@PutMapping("updateAccount")
	public ResponseEntity updateAccount(Authentication authentication, @RequestBody FormProfil userUpdate) {
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		//System.out.println(user.get());
		return ResponseEntity.ok(iAuthenticationService.updateAccount(user.get(), userUpdate));
	}
	
	@PutMapping("updatePassword")
	public ResponseEntity updatePassword(Authentication authentication, @RequestBody Map<String, String> request) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			String password = request.get("password");
		    String newPassword = request.get("newPassword");
			
			return ResponseEntity.ok(iAuthenticationService.updatePassword(user.get(), password, newPassword));
		}
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}
	
	
	@PutMapping("reacAccount")
	public ResponseEntity reactiveAccount(Authentication authentication) {
		
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iAuthenticationService.reactiveAccount(user.get().getId()));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification"); 
	}
	
	
	@PutMapping("desacAccount")
	public ResponseEntity desactiveAccount(Authentication authentication) {
		
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iAuthenticationService.desactiveAccount(user.get().getId()));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification"); 
	}
	
	
	
	@DeleteMapping("deleteAccount")
	public ResponseEntity deleteAccount(Authentication authentication) {
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		iAuthenticationService.deleteAccount(user.get());
		return ResponseEntity.ok("Compte supprimé");
	}
	// Appel de deleteDeckBuilder mais doit nécessité une auth par token pour que le dbID soit celui de l'user connecté
	
	
	// Liker des objets
	
		
	@PostMapping("likeDeck")
	public ResponseEntity likeDeck (Authentication authentication, @RequestParam Long deckId) {
			
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
			
				if(user.isPresent()) {
					iDeckService.likeDeck(user.get(), deckId);
					return ResponseEntity.ok("Deck ajouté");
				}
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
		}
		
	
	@DeleteMapping("dislikeDeck")
	public ResponseEntity dislikeDeck (Authentication authentication, @RequestParam Long deckId) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			iDeckService.dislikeDeck(user.get(), deckId);
			return ResponseEntity.ok("Deck retiré");
		}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
		
	}
	
	@GetMapping("getDecksLikedPaged")
	public ResponseEntity getDeckLiked (Authentication authentication, @RequestParam int page, @RequestParam int size) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		Page<GetDeck> pageResult = iDeckService.getDecksLikedPaged(user.get(), page, size);
		
		Map<String, Object> response = new HashMap<>();
	    response.put("content", pageResult.getContent());
	    response.put("currentPage", pageResult.getNumber());
	    response.put("totalItems", pageResult.getTotalElements());
	    response.put("totalPages", pageResult.getTotalPages());
	    response.put("size", pageResult.getSize());
	    response.put("hasNext", pageResult.hasNext());
	    response.put("hasPrevious", pageResult.hasPrevious());
	    response.put("isFirst", pageResult.isFirst());
	    response.put("isLast", pageResult.isLast());
	    
	    return ResponseEntity.ok(response);
	}
	
	
	@GetMapping("getDecksCreate")
	public ResponseEntity getDeckCreateFilter (Authentication authentication, @RequestParam int page, @RequestParam int size, @RequestParam String order,
			@RequestParam(required = false) String name,
		    @RequestParam(required = false) Float manaCostMin,
		    @RequestParam(required = false) Float manaCostMax,
		    @RequestParam(required = false) List<EnumFormat> formats,
		    @RequestParam(required = false) List<EnumColor> colors) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		Page<GetDeck> pageResult = iDeckService.getDecksCreateByFilterPaged(user.get(), page, size, order, name, manaCostMin, manaCostMax, formats, colors);
		
		Map<String, Object> response = new HashMap<>();
	    response.put("content", pageResult.getContent());
	    response.put("currentPage", pageResult.getNumber());
	    response.put("totalItems", pageResult.getTotalElements());
	    response.put("totalPages", pageResult.getTotalPages());
	    response.put("size", pageResult.getSize());
	    response.put("hasNext", pageResult.hasNext());
	    response.put("hasPrevious", pageResult.hasPrevious());
	    response.put("isFirst", pageResult.isFirst());
	    response.put("isLast", pageResult.isLast());
	    
	    return ResponseEntity.ok(response);
	}
	

	
	
	@GetMapping("getDecksLikedFilter")
	public ResponseEntity getDecksLikedFilter (Authentication authentication, @RequestParam int page, @RequestParam int size, @RequestParam String order,
			@RequestParam(required = false) String name,
		    @RequestParam(required = false) Float manaCostMin,
		    @RequestParam(required = false) Float manaCostMax,
		    @RequestParam(required = false) List<EnumFormat> formats,
		    @RequestParam(required = false) List<EnumColor> colors) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		Page<GetDeck> pageResult = iDeckService.getDecksLikedByFilterPaged(user.get(), page, size, order, name, manaCostMin, manaCostMax, formats, colors);
		
		Map<String, Object> response = new HashMap<>();
	    response.put("content", pageResult.getContent());
	    response.put("currentPage", pageResult.getNumber());
	    response.put("totalItems", pageResult.getTotalElements());
	    response.put("totalPages", pageResult.getTotalPages());
	    response.put("size", pageResult.getSize());
	    response.put("hasNext", pageResult.hasNext());
	    response.put("hasPrevious", pageResult.hasPrevious());
	    response.put("isFirst", pageResult.isFirst());
	    response.put("isLast", pageResult.isLast());
	    
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("getDecksLiked")
	public ResponseEntity getDeckLiked (Authentication authentication) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iDeckService.getDecksLiked(user.get()));
		}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}


	
	
	// Construire un deck
	
	
	@PostMapping("addDeck")
	public ResponseEntity addDeck( Authentication authentication, @RequestBody FormDeck deckRegister) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iDeckService.addDeck(user.get(), deckRegister));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");

	}
	
	@PostMapping("addCedh")
	public ResponseEntity addCedh( Authentication authentication, @RequestBody FormCedh deckRegister ) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iDeckService.addCedh(user.get(), deckRegister));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");

	}
	
	
	@DeleteMapping("deleteDeck")
	public ResponseEntity deleteDeck (Authentication authentication, @RequestParam Long deckID) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			iDeckService.deleteDeck(user.get(), deckID);
			return ResponseEntity.ok("Deck supprimé");
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}
	
	@PutMapping("updateDeck")
	public ResponseEntity updateDeck(Authentication authentication, @RequestParam Long deckID, @RequestBody FormDeck deckUpdate) {
		return ResponseEntity.ok(iDeckService.updateDeck(deckID, deckUpdate));
	}
	

	@PostMapping("addCardsOnDeck")
	public ResponseEntity addCardsOnDeck(Authentication authentication, @RequestBody List<Card> cardsApi, @RequestParam Long deckId) {
		return ResponseEntity.ok(iDeckService.addCardsOnDeck(cardsApi, deckId));
	}
	
	@PostMapping("duplicateCardsOnDeck")
	public ResponseEntity duplicateCardsOnDeck(Authentication authentication, @RequestParam List<Long> cardsId, @RequestParam Long deckId) {
		return ResponseEntity.ok(iDeckService.duplicateCardsOnDeck(cardsId, deckId));
	}
	
	@DeleteMapping("deleteCardOnDeck")
	public ResponseEntity deleteCardOnDeck(Authentication authentication, @RequestParam Long cardId, @RequestParam Long deckId) {
		return ResponseEntity.ok(iDeckService.deleteCardOnDeck(cardId, deckId));
	}
	
	
	@DeleteMapping("deleteCardsListOnDeck")
	public ResponseEntity deleteCardsOnDeck(Authentication authentication, @RequestParam List<Long> cardId, @RequestParam Long deckId) {
		return ResponseEntity.ok(iDeckService.deleteCardsOnDeck(cardId, deckId));
	}
	
	// Cette requete supprimer plusieurs cartes mais avec le meme id
	@DeleteMapping("deleteCardsOnDeck")
	public ResponseEntity deleteCardsOnDeck(Authentication authentication, @RequestParam Long cardId, @RequestParam Long deckId) {
		return ResponseEntity.ok(iDeckService.deleteCardsOnDeck(cardId, deckId));
	}
	
	
	
		
	@PutMapping("setNumberCardOnDeck")
	public String setNumberCardOnDeck(Authentication authentication, @RequestParam Long cardID,@RequestParam Long deckID,@RequestParam int number) {
		return iDeckService.setNumberCardOnDeck(cardID, deckID, number);
	}
	
	@PutMapping("deckPublic")
	public ResponseEntity publishDeck(Authentication authentication, @RequestParam Long deckID) {
		
	Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
				
	if(user.isPresent()) {
		return ResponseEntity.ok(iDeckService.publishDeck(user.get(), deckID));
		}
				
	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}
	
	@PutMapping("deckPrivate")
	public ResponseEntity privateDeck(Authentication authentication, Long deckID) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iDeckService.privateDeck(user.get(), deckID));
			}
					
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification");
	}
	
	// Méthodes d'affichage dans le deckbuilding

	
	@GetMapping("deckCost")
	public float getDeckManaCost(@RequestParam Long deckID) {
		return iDeckService.getDeckManaCost(deckID);
	}
	
	
	 


}
