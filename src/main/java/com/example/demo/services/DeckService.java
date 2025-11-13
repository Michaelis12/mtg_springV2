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

@Service
public class DeckService implements IDeckService {
	
	@Autowired
	DeckBuilderRepository deckBuilderRepository;
	
	@Autowired
	DeckRepository deckRepository;
	
	@Autowired
	CardRepository cardRepository;
	
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private FileService fileService;
	
	
	
	@Override
	public String addCardsOnDeck(List<Card> cards, Long deckId) {

		
		Deck deckToTarget = deckRepository.findById(deckId)
	        .orElseThrow(() -> new RuntimeException("Deck non trouvé"));

	    if (deckToTarget.getCards().size() >= 300) {
	        throw new RuntimeException("Nombre maximum autorisé atteint");
	    }
	    
	    for (Card card : cards) {

	        // Vérifie si la carte existe déjà (évite doubleness si même API ID)
	        Optional<Card> existingCardOpt = cardRepository.findByApiID(card.getApiID());
	    		    		    	

	        if (existingCardOpt.isPresent()) {
	            card = existingCardOpt.get();
	            
	            // Si la carte est ajouté pour la 1ère fois dans un deck, augmente de 1 le nombre de decks associés
	            if(!deckToTarget.getCards().contains(card)) {
	            	card.setDecksNumber(card.getDecksNumber() + 1);
	            	
	            	cardRepository.save(card);
	            }
	            
	        } else {  
	        	// Si la carte n'a aucune couleur on lui donne la couleur "colorless"
	        	if(card.getColors().size() < 1) {
	        		card.getColors().add("colorless");
	        	}
	        	// Instacie le nombre de decks de la carte à 1
	        	card.setDecksNumber((long) 1);
	        	cardRepository.save(card);
	        }

	        // Ajout de la carte au deck
	        deckToTarget.getCards().add(card);
	    }
	    
	    deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));

        // Si le deck est public, on le passe en privé
        if (deckToTarget.getIsPublic()) {
            deckToTarget.setIsPublic(false);
        }

	    // Enregistre le deck mis à jour
	    deckRepository.save(deckToTarget);

	    return "carte ajoutée";
	}
	
	
	@Override
	public String duplicateCardsOnDeck(List<Long> cardsId, Long deckId) {
	    // Recherche le deck, lance une exception si non trouvé
	    Deck deckToTarget = deckRepository.findById(deckId)
	        .orElseThrow(() -> new RuntimeException("Deck non trouvé"));

	    // Vérifie si le deck peut encore recevoir des cartes
	    if (deckToTarget.getCards().size() >= 300) {
	        throw new RuntimeException("Nombre maximum autorisé atteint");
	    }

	    // Ajoute chaque carte si elle est compatible
	    for (Long cardId : cardsId) {
	        Card cardToAdd = cardRepository.findById(cardId)
	            .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

	        // Si la carte est compatible avec la couleur et le format
	            deckToTarget.getCards().add(cardToAdd);

	            // Recalcule la valeur et le coût en mana du deck
	            deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));
	            
	    }

	            // Si le deck est public, on le passe en privé
	            if (deckToTarget.getIsPublic()) {
	                deckToTarget.setIsPublic(false);	                        
	            } 
	            
	         // Sauvegarde du deck
	         deckRepository.save(deckToTarget);

	         return "cartes ajoutées";
	}

	
	
	// Méthodes sans authentification
	
	// homePage
		
		@Override
		public List<GetDeck> getTop3Decks() {
			
			List<Deck> decks = deckRepository.findAll();
			List<Deck> topDecks = new ArrayList<>();
			
			for (Deck deck : decks) {
				if(deck.getLikeNumber() != null && deck.getIsPublic()) {
					topDecks.add(deck);
				}
				
			}
			
			topDecks.sort(Comparator.comparingLong(Deck::getLikeNumber).thenComparingLong(Deck::getId).reversed());
			List<GetDeck> topGetDecks = new ArrayList<>();
			
			for (Deck deck : topDecks) {
				GetDeck testDeck = new GetDeck();
				testDeck.setId(deck.getId());
				testDeck.setName(deck.getName());
				testDeck.setImage(deck.getImage());
				testDeck.setDateCreation(deck.getDateCreation());
				testDeck.setManaCost(deck.getManaCost());
				testDeck.setColors(deck.getColors());
				testDeck.setFormat(deck.getFormat());
				testDeck.setLikeNumber(deck.getLikeNumber());
				testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());
				testDeck.setLikeNumber(deck.getLikeNumber());
				
				
				if(topGetDecks.size() == 3) {
					break; }
				topGetDecks.add(testDeck);
			}
			
			return topGetDecks;
		}
		

		@Override
		public Page<GetDeck> getDecksByFilterPaged(int page, int size, String order, String name, Float manaCostMin, Float manaCostMax,
		                                           List<EnumFormat> formats, List<EnumColor> colors) {
			
			// Transforme format en null si c'est une liste vide
			formats = (formats == null || formats.isEmpty()) ? null : formats;

		    // Récupérer tous les decks publics filtrés (sans pagination)
		    List<Deck> allFilteredDecks = deckRepository.findByAttributes(name, manaCostMin, manaCostMax, true, formats, colors);
		    
		    
		    if(order.equals("date")) {
		    // Trier par datePublication (croissant)
		    	allFilteredDecks.sort(Comparator.comparing(Deck::getDatePublication).thenComparingLong(Deck::getId).reversed());
		    }
		    
		    if(order.equals("like")) {
			    // Trier par nb de likes
			    	allFilteredDecks.sort(Comparator.comparingLong(Deck::getLikeNumber).thenComparingLong(Deck::getId).reversed());
			    }

		    // Appliquer manuellement la pagination
		    int start = page * size;
		    int end = Math.min(start + size, allFilteredDecks.size());

		    if (start >= allFilteredDecks.size()) {
		        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), allFilteredDecks.size());
		    }

		    List<Deck> paginatedDecks = allFilteredDecks.subList(start, end);

		    // Mapper vers GetDeck
		    List<GetDeck> getDeckList = paginatedDecks.stream().map(deck -> {
		        GetDeck testDeck = new GetDeck();
		        testDeck.setId(deck.getId());
		        testDeck.setName(deck.getName());
		        testDeck.setImage(deck.getImage());
		        testDeck.setDateCreation(deck.getDateCreation());
		        testDeck.setDatePublication(deck.getDatePublication());
		        testDeck.setManaCost(deck.getManaCost());
		        testDeck.setFormat(deck.getFormat());
		        testDeck.setColors(deck.getColors());
		        testDeck.setLikeNumber(deck.getLikeNumber());
		        testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());

		        

		        return testDeck;
		    }).collect(Collectors.toList());

		    return new PageImpl<>(getDeckList, PageRequest.of(page, size), allFilteredDecks.size());
		}
		
		
		@Override
		public Page<GetDeck> getDecksUserPaged(
		DeckCreator user, 
		int page, 
		int size, 
		String order) {


		    // Récupérer tous les decks publics filtrés (sans pagination)
		    List<Deck> allDecks = deckRepository.findByDeckBuilder(user);
		    
		    List<Deck> allFilteredDecks = allDecks.stream()
                    .filter(deck -> deck.getIsPublic())
                    .collect(Collectors.toList());
		    
		    
		    if(order.equals("date")) {
		    // Trier par datePublication (croissant)
		    	allFilteredDecks.sort(Comparator.comparing(Deck::getDatePublication));
		    }
		    
		    if(order.equals("like")) {
			    // Trier par nb de likes
			    	allFilteredDecks.sort(Comparator.comparingLong(Deck::getLikeNumber).reversed());
			    }

		    // Appliquer manuellement la pagination
		    int start = page * size;
		    int end = Math.min(start + size, allFilteredDecks.size());

		    if (start >= allFilteredDecks.size()) {
		        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), allFilteredDecks.size());
		    }

		    List<Deck> paginatedDecks = allFilteredDecks.subList(start, end);

		    // Mapper vers GetDeck
		    List<GetDeck> getDeckList = paginatedDecks.stream().map(deck -> {
		    
		        GetDeck testDeck = new GetDeck();
		        testDeck.setId(deck.getId());
		        testDeck.setName(deck.getName());
		        testDeck.setImage(deck.getImage());
		        testDeck.setDateCreation(deck.getDateCreation());
		        testDeck.setDatePublication(deck.getDatePublication());
		        testDeck.setManaCost(deck.getManaCost());
		        testDeck.setIsPublic(deck.getIsPublic());		        
		        testDeck.setFormat(deck.getFormat());
		        testDeck.setColors(deck.getColors());
		        testDeck.setLikeNumber(deck.getLikeNumber());
		        testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());

		        return testDeck;

		     	
		    }).collect(Collectors.toList());

		    return new PageImpl<>(getDeckList, PageRequest.of(page, size), allFilteredDecks.size());
		}
		
		
		@Override
		public Page<GetDeck> getDecksCreateByFilterPaged(
		DeckCreator user, 
		int page, 
		int size, 
		String order, 
		String name, 
		Float manaCostMin, 
		Float manaCostMax,
		List<EnumFormat> formats, 
		List<EnumColor> colors) {

			// Transforme format en null si c'est une liste vide
			formats = (formats == null || formats.isEmpty()) ? null : formats;

		    // Récupérer tous les decks publics filtrés (sans pagination)
		    List<Deck> allFilteredDecks = deckRepository.findByAttributeCreate(user, name, manaCostMin, manaCostMax, formats, colors);
		    
		    
		    if(order.equals("date")) {
		    // Trier par datePublication (croissant)
		    	allFilteredDecks.sort(Comparator.comparing(Deck::getDateCreation));
		    }
		    
		    if(order.equals("like")) {
			    // Trier par nb de likes
			    	allFilteredDecks.sort(Comparator.comparingLong(Deck::getLikeNumber).reversed());
			    }

		    // Appliquer manuellement la pagination
		    int start = page * size;
		    int end = Math.min(start + size, allFilteredDecks.size());

		    if (start >= allFilteredDecks.size()) {
		        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), allFilteredDecks.size());
		    }

		    List<Deck> paginatedDecks = allFilteredDecks.subList(start, end);

		    // Mapper vers GetDeck
		    List<GetDeck> getDeckList = paginatedDecks.stream().map(deck -> {
		        GetDeck testDeck = new GetDeck();
		        testDeck.setId(deck.getId());
		        testDeck.setName(deck.getName());
		        testDeck.setImage(deck.getImage());
		        testDeck.setDateCreation(deck.getDateCreation());
		        testDeck.setDatePublication(deck.getDatePublication());
		        testDeck.setManaCost(deck.getManaCost());
		        testDeck.setIsPublic(deck.getIsPublic());		        
		        testDeck.setFormat(deck.getFormat());
		        testDeck.setColors(deck.getColors());
		        testDeck.setLikeNumber(deck.getLikeNumber());
		        testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());


		        return testDeck;
		    }).collect(Collectors.toList());

		    return new PageImpl<>(getDeckList, PageRequest.of(page, size), allFilteredDecks.size());
		}
		
		
		@Override
		public Page<GetDeck> getDecksLikedByFilterPaged(
		DeckCreator user, 
		int page, 
		int size, 
		String order, 
		String name, 
		Float manaCostMin, 
		Float manaCostMax, 
		List<EnumFormat> formats, 
		List<EnumColor> colors) {
			
			// Transforme format en null si c'est une liste vide
			formats = (formats == null || formats.isEmpty()) ? null : formats;


		    // Récupérer tous les decks publics filtrés (sans pagination)
		    List<Deck> allFilteredDecks = deckRepository.findByAttributeLiked(user, name, manaCostMin, manaCostMax, true, formats, colors);
		    
		    
		    if(order.equals("date")) {
		    // Trier par datePublication (croissant)
		    	allFilteredDecks.sort(Comparator.comparing(Deck::getDatePublication));
		    }
		    
		    if(order.equals("like")) {
			    // Trier par nb de likes
			    	allFilteredDecks.sort(Comparator.comparingLong(Deck::getLikeNumber).reversed());
			    }

		    // Appliquer manuellement la pagination
		    int start = page * size;
		    int end = Math.min(start + size, allFilteredDecks.size());

		    if (start >= allFilteredDecks.size()) {
		        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), allFilteredDecks.size());
		    }

		    List<Deck> paginatedDecks = allFilteredDecks.subList(start, end);

		    // Mapper vers GetDeck
		    List<GetDeck> getDeckList = paginatedDecks.stream().map(deck -> {
		        GetDeck testDeck = new GetDeck();
		        testDeck.setId(deck.getId());
		        testDeck.setName(deck.getName());
		        testDeck.setImage(deck.getImage());
		        testDeck.setDateCreation(deck.getDateCreation());
		        testDeck.setDatePublication(deck.getDatePublication());
		        testDeck.setManaCost(deck.getManaCost());
		        testDeck.setFormat(deck.getFormat());
		        testDeck.setColors(deck.getColors());
		        testDeck.setLikeNumber(deck.getLikeNumber());
		        testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());

		        return testDeck;
		    }).collect(Collectors.toList());

		    return new PageImpl<>(getDeckList, PageRequest.of(page, size), allFilteredDecks.size());
		}
		
		
		@Override
		public Set<Long> getDecksLiked (DeckCreator user) {
			List<Deck> decks = user.getDecksLiked();
			Set<Long> decksReturn = new HashSet<>();
			
			for (Deck deck : decks) {
				
				decksReturn.add(deck.getId());
			}

			return decksReturn;
		}

		
		

		@Override
		public List<String> getAllDeckImages() {
			List<Deck> decks = deckRepository.findAll();
			return decks.stream()
				.map(Deck::getImage)
				.filter(image -> image != null && image.startsWith("/uploads/"))
				.collect(Collectors.toList());
		}


	@Override
	public GetDeck getDeckById(Long deckID) {
		 
		Optional<Deck> deck = deckRepository.findById(deckID);
		GetDeck deckReturn = new GetDeck();
		
		if(deck.isPresent()) {
			 Deck deckFind = deck.get();
			 
			 deckReturn.setId(deckFind.getId());
			 deckReturn.setName(deckFind.getName());
			 deckReturn.setImage(deckFind.getImage());
			 deckReturn.setDateCreation(deckFind.getDateCreation());
			 deckReturn.setManaCost(deckFind.getManaCost());
			 deckReturn.setFormat(deckFind.getFormat());
			 deckReturn.setColors(deckFind.getColors());
			 deckReturn.setLikeNumber(deckFind.getLikeNumber());
			 deckReturn.setIsPublic(deckFind.getIsPublic());
			 deckReturn.setDeckBuilderName(deckFind.getDeckBuilder().getPseudo());
			
				
				return deckReturn;
		 	}
		
			throw new RuntimeException("Deck non trouvé");
				
		}
			
	
	@Override
	public Long getDeckUser(Long deckID) {
				
		Optional<Deck> deck = deckRepository.findById(deckID);
		
		if(deck.isPresent()) {
			return deck.get().getDeckBuilder().getId();
		}
		
		throw new RuntimeException("Deck non trouvé");
		
	}
	
	// Cette méthode récupère une list de Deck recherché par l'user et trouve dans cette liste, l'id du deck qui suit celui de l'id entré
	@Override
	public Long getNextDeck(Long deckID, List<Long> decksID) {
	    for (int i = 0; i < decksID.size(); i++) {
	        if (decksID.get(i).equals(deckID)) {
	            if (i + 1 < decksID.size()) {
	                return decksID.get(i + 1);
	            } else {
	                throw new RuntimeException("Deck non trouvé");
	            }
	        }
	    }
	    throw new RuntimeException("Deck non trouvé");
	}

	
	@Override
	public Long getPrevDeck(Long deckID, List<Long> decksID) {
	    for (int i = 0; i < decksID.size(); i++) {
	        if (decksID.get(i).equals(deckID)) {
	            if (i - 1 >= 0) {          // Vérifie qu'on n'est pas avant le début
	                return decksID.get(i - 1);
	            } else {
	                throw new RuntimeException("Deck non trouvé"); // Premier deck => pas de précédent
	            }
	        }
	    }
	    throw new RuntimeException("Deck non trouvé"); // deckID absent de la liste
	}

	@Override
	public List<GetCard> getCardsOnDeck(Long deckID) {
		
		Optional<Deck> deckOptional = deckRepository.findById(deckID);
		
			if(deckOptional.isPresent()) {
			
				Deck deck = deckOptional.get();
				List<Card> CardsOnDeck = deck.getCards();				
				List<GetCard> CardsReturn = new ArrayList<>();
								
				
				for (Card cardFind : CardsOnDeck) {
										
				 
				 		GetCard cardReturn = new GetCard();
						 cardReturn.setId(cardFind.getId());
						 cardReturn.setApiID(cardFind.getApiID());
						 cardReturn.setName(cardFind.getName());
						 cardReturn.setImage(cardFind.getImage());
						 cardReturn.setManaCost(cardFind.getManaCost());
						 cardReturn.setCmc(cardFind.getCmc());
						 cardReturn.setDecksNumber(cardFind.getDecksNumber());
						 cardReturn.setLegendary(cardFind.isLegendary());
						 
						 
						 Set <String> colors = new HashSet<>();
						 for (String color : cardFind.getColors()) {
							 colors.add(color);
						}
						 cardReturn.setColors(colors);
						 
						 List <String> types = new ArrayList<>();
						 for (String type : cardFind.getTypes()) {
							 types.add(type);
						}
						 cardReturn.setTypes(types);
						 
						
						 cardReturn.setLegendary(cardFind.isLegendary());										 
						 
						 CardsReturn.add(cardReturn);
						 
			}	
				
			return CardsReturn;
		}
			
			throw new RuntimeException("Deck non trouvé");
	}
	
	@Override
	public List<GetCard> get7CardsOnDeck(Long deckID) {
		
		Optional<Deck> deckOptional = deckRepository.findById(deckID);
		
		if(deckOptional.isPresent()) {
		
			Deck deck = deckOptional.get();
			List<Card> cardsOnDeck = deck.getCards();
			
			Collections.shuffle(cardsOnDeck); 
	
		    List<Card> selectedCards = cardsOnDeck.stream()
		        .limit(7)
		        .collect(Collectors.toList());
		    
		    
			
			List<GetCard> CardsReturn = new ArrayList<>();
			
			for (Card cardFind : selectedCards) {
				 GetCard cardReturn = new GetCard();
					
				 cardReturn.setId(cardFind.getId());
				 cardReturn.setApiID(cardFind.getApiID());
				 cardReturn.setName(cardFind.getName());
				 cardReturn.setImage(cardFind.getImage());
				 				 
				 CardsReturn.add(cardReturn);
			}	
				return CardsReturn;
		}
		
		throw new RuntimeException("Deck non trouvé");
	}
	
	
	
	@Override
	public GetCard getCommandantOndeck (Long deckID) {
		
		Optional<Deck> deckOptional = deckRepository.findById(deckID);
		
		if(deckOptional.isPresent()) {
			
			Deck deck = deckOptional.get();
			Card commandant = deck.getCommander();
			
			GetCard commandantReturn = new GetCard();
			
			commandantReturn.setId(commandant.getId());
			commandantReturn.setApiID(commandant.getApiID());
			commandantReturn.setName(commandant.getName());
			commandantReturn.setImage(commandant.getImage());

			 
			 return commandantReturn;
		}
		
		throw new RuntimeException("Deck non trouvé");
		
	}
	
	@Override
	public int getNumberDecksByUser(DeckCreator dbuilder) {
		
		if(deckBuilderRepository.findAll().contains(dbuilder)) {
		
			List<Deck> decksUser = deckRepository.findByDeckBuilder(dbuilder);
			int decksNumber = decksUser.size();
			
			
			return decksNumber;
		}
		throw new RuntimeException("User introuvable");
	}
		
	// Méthodes avec authentification
	
	@Override
	public List<GetDeck> getDecksByUser(DeckCreator dbuilder) {
		
		if(deckBuilderRepository.findAll().contains(dbuilder)) {
		
			List<Deck> decksUser = deckRepository.findByDeckBuilder(dbuilder);
			List<GetDeck> decksReturn = new ArrayList<>();
			
			if(!decksUser.isEmpty()) {
					decksUser.sort(Comparator.comparing(Deck::getDateCreation));
			
					for (Deck deck : decksUser) {
							GetDeck testDeck = new GetDeck();
							testDeck.setId(deck.getId());
							testDeck.setName(deck.getName());
							testDeck.setImage(deck.getImage());
							testDeck.setDateCreation(deck.getDateCreation());
							testDeck.setManaCost(deck.getManaCost());
							testDeck.setFormat(deck.getFormat());
							testDeck.setColors(deck.getColors());
							testDeck.setIsPublic(deck.getIsPublic());
							testDeck.setLikeNumber(deck.getLikeNumber());
							testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());
							
								
							decksReturn.add(testDeck);			
					}
			}
			
			return decksReturn;
		}
		throw new RuntimeException("User introuvable");
	}
	
	
	
	@Override
	public void likeDeck (DeckCreator user, Long deckID) {
		Optional<Deck> deckOpt = deckRepository.findById(deckID);
		
		List <Deck> decksLiked = user.getDecksLiked();
		for (Deck deckLiked : decksLiked) {
			if (deckLiked.getId().equals(deckID)) {
				throw new RuntimeException("Deck déja liké");
			}
		}
		
		if(deckOpt.isPresent()) {
			Deck deck = deckOpt.get();
			user.getDecksLiked().add(deck);
			user.setDecksLikedNumber((long) user.getDecksLiked().size());
			deckBuilderRepository.save(user);
			
			deck.setLikeNumber(deck.getLikeNumber() +1);
			
			deckRepository.save(deck);
			
			if(!user.getId().equals(deck.getDeckBuilder().getId())) {
			
				Notification notif = new Notification();
				notif.setDate(LocalDateTime.now());
				notif.setDeck(deck);
				notif.setIssuer(user);
				notif.setReceivor(deck.getDeckBuilder());
				
				notificationRepository.save(notif);
			
			}
		}
		
		
	}
	
	@Override
	public void dislikeDeck (DeckCreator user, Long deckID) {
		Optional<Deck> deckOpt = deckRepository.findById(deckID);
		if(deckOpt.isPresent()) {
		Deck deck = deckOpt.get();
		user.getDecksLiked().remove(deck);
		user.setDecksLikedNumber((long) user.getDecksLiked().size());
		deckBuilderRepository.save(user);
		
		deck.setLikeNumber(deck.getLikeNumber() -1);
		
		deckRepository.save(deck);
		}
	}
	
	
	
	@Override
	public Page<GetDeck> getDecksLikedPaged (DeckCreator user, int page, int size) {
		
		
		List<Deck> decks = user.getDecksLiked().stream()
		    .filter(Deck::getIsPublic) 
		    .collect(Collectors.toList());
		
		// Appliquer manuellement la pagination
	    int start = page * size;
	    int end = Math.min(start + size, decks.size());

	    if (start >= decks.size()) {
	        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), decks.size());
	    }

	    List<Deck> paginatedDecks = decks.subList(start, end);

	    // Mapper vers GetDeck
	    List<GetDeck> getDeckList = paginatedDecks.stream().map(deck -> {
	        GetDeck testDeck = new GetDeck();
	        testDeck.setId(deck.getId());
	        testDeck.setName(deck.getName());
	        testDeck.setImage(deck.getImage());
	        testDeck.setDateCreation(deck.getDateCreation());
	        testDeck.setDatePublication(deck.getDatePublication());
	        testDeck.setManaCost(deck.getManaCost());
	        testDeck.setFormat(deck.getFormat());
	        testDeck.setColors(deck.getColors());
	        testDeck.setLikeNumber(deck.getLikeNumber());
	        testDeck.setDeckBuilderName(deck.getDeckBuilder().getPseudo());

	        return testDeck;
	    }).collect(Collectors.toList());

	    return new PageImpl<>(getDeckList, PageRequest.of(page, size), decks.size());

	}
	
	
	@Override
	public Long addDeck (DeckCreator dbuilder, FormDeck deckRegister ) {
		
		deckRegister.setName(Sanitizer.sanitizeName(deckRegister.getName()));
		deckRegister.setImage(Sanitizer.sanitize(deckRegister.getImage()));
		
		if(deckRegister.getName() != null &&
		   deckRegister.getFormat() != null && deckRegister.getColors() != null) {
						
			Deck deck = new Deck();
			deck.setName(deckRegister.getName());
			deck.setDateCreation(LocalDate.now());
			deck.setImage(deckRegister.getImage());
			deck.setFormat(deckRegister.getFormat());
			deck.setColors(deckRegister.getColors());
			/*
			if(!deck.getColors().contains(EnumColor.colorless)) {
				deck.getColors().add(EnumColor.colorless);
			}
			*/
			deck.setLikeNumber((long) 0);
			deck.setIsPublic(false);
			deck.setDeckBuilder(dbuilder);
			
			
				
			deckRepository.save(deck);
			
			dbuilder.setActivity(UserActivity.CREATOR);
			dbuilder.setDecksNumber((long)getNumberDecksByUser( dbuilder));
			deckBuilderRepository.save(dbuilder);
			
			
			return deck.getId();
			
		}

		throw new RuntimeException("Données manquantes");
			
	}
	
	
	@Override
	public Long addCedh (DeckCreator dbuilder, FormCedh deckRegister) {
		
	
		if(deckRegister.getName() != null && deckRegister.getImage() != null &&
				   deckRegister.getFormat() != null && deckRegister.getColors() != null ) { 
						new RuntimeException("Données manquantes");
			
				   }
		
		// Création du deck 		
		Deck deck = new Deck();
		deckRegister.setName(Sanitizer.sanitizeName(deckRegister.getName()));
		deckRegister.setImage(Sanitizer.sanitize(deckRegister.getImage()));
		
		deck.setName(deckRegister.getName());
		deck.setDateCreation(LocalDate.now());
		deck.setImage(deckRegister.getImage());
		deck.setFormat(deckRegister.getFormat());
		deck.setColors(deckRegister.getColors());
		deck.setLikeNumber((long) 0);
		deck.setIsPublic(false);
		deck.setDeckBuilder(dbuilder);
		
		// Ajout du commandant du deck
		 Optional<Card> existingCommandant = cardRepository.findByApiID(deckRegister.getCommandant().getApiID());
	    

	        if (existingCommandant.isPresent()) {
	        	Card commandant = existingCommandant.get();
	        	deck.setCommander(commandant);
	        	commandant.setCedhNumber((long) 1);
	        	cardRepository.save(commandant);
	            
	        } else {  
	        	Card commandant = deckRegister.getCommandant();
	        	// Si la carte n'a aucune couleur on lui donne la couleur "colorless"
	        	if(commandant.getColors().size() < 1) {
	        		commandant.getColors().add("colorless");
	        	}
	        	// Instacie le nombre de decks de la carte à 1
	        	deck.setCommander(commandant);
	        	commandant.setCedhNumber((long) 1);
	        	cardRepository.save(commandant);
	        }
				
				
					
				deckRepository.save(deck);
				
				dbuilder.setActivity(UserActivity.CREATOR);
				dbuilder.setDecksNumber((long)getNumberDecksByUser( dbuilder));
				deckBuilderRepository.save(dbuilder);
				
				
				return deck.getId();

		}
		
		

	

	
	
	@Override
	public void deleteDeck(DeckCreator user, Long deckID) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);

		if(deck.isPresent()) {
			
			Deck deckFind = deck.get();
			
			
			if(deckFind.getDeckBuilder() == user) {
				
				
				// Retire des decks likés et modifie le nb de decks likés des users qui ont liké
				List <DeckCreator> dbs = deckBuilderRepository.findAll();
				 
				for (DeckCreator deckCreator : dbs) {
					if (deckCreator.getDecksLiked().contains(deckFind)) {
						deckCreator.getDecksLiked().remove(deckFind);
						deckCreator.setDecksLikedNumber((long) deckCreator.getDecksLiked().size());
						deckBuilderRepository.save(deckCreator);
					}
				}
				
				// Supprimer l'image du deck
				fileService.deleteImage(deckFind.getImage());
				
				deckRepository.deleteById(deckID);
				
				// Modifie la nb de decks de l'user				
				user.setDecksNumber((long)getDecksByUser(user).size());
				deckBuilderRepository.save(user);
				
				return;
				
			}
			throw new RuntimeException("Authentification et deck incompatibles");
		}
		throw new RuntimeException("Deck non trouvé");
	}
	
	
	@Override 
	public String updateDeck (Long deckID, FormDeck deckUpdate) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		
		if(deck.isPresent()) {
			
			Deck newDeck = deck.get();
			String oldImage = newDeck.getImage(); // Sauvegarder l'ancienne image
			
			
			if(deckUpdate.getName() != null) {
				newDeck.setName(deckUpdate.getName()); }
			if(deckUpdate.getImage() != null) {
				newDeck.setImage(deckUpdate.getImage()); 
				// Supprimer l'ancienne image si elle est différente de la nouvelle
				fileService.deleteOldImageIfUnused(oldImage, deckUpdate.getImage());
			}
			
			newDeck.setIsPublic(false);
			deckRepository.save(newDeck);
			return "deck modifié";
		}
		throw new RuntimeException("Deck non trouvé");
	}
	
	
	// Ajoute une carte au deck si son format et sa couleurs sont compatibles avec ceux du deck
	// et si la list ne dépasse pas 100 cartes dans le cas d'un format commander
	// Appelle les fonctions getDeckManaCost et getDeckValue pour calculer ses valeurs une fois la carte ajoutée
	
	
	
	
	// Les cartes commanders ne peuvent etre que des créatures légendaires
	
	@Override
	public String deleteCardOnDeck(Long cardID, Long deckID) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		Optional<Card> card = cardRepository.findById(cardID); 

		if(deck.isPresent() && card.isPresent()) {
			Deck deckToTarget = deck.get();
			Card cardFind = card.get();
			
			
			deckToTarget.getCards().remove(cardFind);
			
			
			// On recalcule la value totale et le mancost moyen du deck
			deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));
			
			if(deckToTarget.getIsPublic()) {
				// On repasse le deck en privé
				privateDeck(deckToTarget.getDeckBuilder(), deckToTarget.getId());
			}
			
			deckRepository.save(deckToTarget);
			
			if(!deckToTarget.getCards().contains(cardFind)) {
				cardFind.setDecksNumber(cardFind.getDecksNumber()-1);
			}
					
			cardRepository.save(cardFind);
			
			return cardFind.getName() + " a été retiré du deck";
		}
		throw new RuntimeException("Deck ou carte non trouvé");
	}
	
	@Override
	public String deleteCardsOnDeck(List<Long> cardIDs, Long deckID) {

	    Optional<Deck> deck = deckRepository.findById(deckID);

	    if (deck.isPresent()) {
	        Deck deckToTarget = deck.get();
	        StringBuilder removedCardsNames = new StringBuilder();

	        for (Long cardID : cardIDs) {
	            Optional<Card> card = cardRepository.findById(cardID);
	            if (card.isPresent()) {
	                Card cardFind = card.get();
	                boolean removed = deckToTarget.getCards().remove(cardFind);
	                
	                if(!deckToTarget.getCards().contains(cardFind)) {
		                cardFind.setDecksNumber(cardFind.getDecksNumber()-1);
		                cardRepository.save(cardFind);
	                }
	                
	                if (removed) {
	                    removedCardsNames.append(cardFind.getName()).append(", ");
	                }
	            } else {
	                throw new RuntimeException("Carte avec ID " + cardID + " non trouvée");
	            }
	        }

	        deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));

	        if (deckToTarget.getIsPublic()) {
	            // On repasse le deck en privé
	            privateDeck(deckToTarget.getDeckBuilder(), deckToTarget.getId());
	        }

	        deckRepository.save(deckToTarget);
	        
	        

	        if (removedCardsNames.length() > 0) {
	            // On enlève la dernière virgule et l’espace
	            removedCardsNames.setLength(removedCardsNames.length() - 2);
	            return removedCardsNames + " ont été retirées du deck";
	        } else {
	            return "Aucune carte n'a été retirée du deck";
	        }
	    }

	    throw new RuntimeException("Deck non trouvé");
	}
	
	// Modifie plusieurs cartes avec un meme id
	@Override
	public String deleteCardsOnDeck(Long cardID, Long deckID) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		Optional<Card> card = cardRepository.findById(cardID); 

		if(deck.isPresent() && card.isPresent()) {
			Deck deckToTarget = deck.get();
			Card cardFind = card.get();
			
			
			deckToTarget.getCards().removeIf(c -> c.getId().equals(cardFind.getId()));
			
			deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));
			
			if(deckToTarget.getIsPublic()) {
				// On repasse le deck en privé
				deckToTarget.setIsPublic(false);
				privateDeck(deckToTarget.getDeckBuilder(), deckToTarget.getId());
			}
			
			deckRepository.save(deckToTarget);
			
			return cardFind.getName() + " a été retiré du deck";
		}
		throw new RuntimeException("Deck ou carte non trouvé");
	}
	
	
	
	

	
	
	
	
	
	@Override
	public String setNumberCardOnDeck (Long cardID, Long deckID, int number) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		Optional<Card> card = cardRepository.findById(cardID); 

		if(deck.isPresent() && card.isPresent()) {
			Deck deckToTarget = deck.get();
			Card cardFind = card.get();
			
			 // On récupère tous les exemplaires actuels de cette carte dans le deck
	        long currentCount = deckToTarget.getCards().stream()
	                                .filter(c -> c.getId().equals(cardID))
	                                .count();

	        // On retire ou ajoute en fonction de la différence
	        if (currentCount < number) {
	            for (int i = 0; i < number - currentCount; i++) {
	            	deckToTarget.getCards().add(cardFind);
	            }
	        } else if (currentCount > number) {
	            int toRemove = (int)(currentCount - number);
	            Iterator<Card> it = deckToTarget.getCards().iterator();
	            while (it.hasNext() && toRemove > 0) {
	                if (it.next().getId().equals(cardID)) {
	                    it.remove();
	                    toRemove--;
	                }
	            }
	        }
			
			
			deckToTarget.setManaCost(getDeckManaCost(deckToTarget.getId()));
			
			if(deckToTarget.getIsPublic()) {
				// On repasse le deck en privé
				deckToTarget.setIsPublic(false);
			}
			
			deckRepository.save(deckToTarget);
			
			return cardFind.getName() + " a été retiré du deck";
		}
		throw new RuntimeException("Deck ou carte non trouvé");
		
		
	}
	
	@Override
	public String publishDeck (DeckCreator user, Long deckID) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		
		if(deck.isPresent()) {
			
			Deck deckPresent = deck.get();
			
			if(deckPresent.getDeckBuilder() == user) {

					if(deckPresent.getFormat().equals(EnumFormat.commander) && deck.get().getCards().size() > 99 ||
						!deckPresent.getFormat().equals(EnumFormat.commander) && deck.get().getCards().size() > 39
							) {
						
						deckPresent.setIsPublic(true);
						deckPresent.setDatePublication(LocalDate.now());
						deckRepository.save(deckPresent);
						
						DeckCreator db = deckPresent.getDeckBuilder();
						db.setActivity(UserActivity.PUBLISHER);
						if(db.getDecksPublicNumber()!= null) {
							db.setDecksPublicNumber(db.getDecksPublicNumber() + 1);
							}
						else {
							db.setDecksPublicNumber(1L);
						}
						deckBuilderRepository.save(db);
						
						 return "Deck " + deckPresent.getName() + " publié !";
					}
					
					throw new RuntimeException("Ce deck ne contient pas suffisament de cartes pour etre jouable dans ce format");
			}
			
			throw new RuntimeException("Authentification ne correspond pas");
		}
		
		throw new RuntimeException("Deck non trouvé");
	}
	// Publie un deck après avoir vérifié s'il a le bon nom de cartes pour etre jouable
	// (100 pour un commander, 60 ou + pour les autres formats)
	
	
	@Override
	public String privateDeck (DeckCreator user, Long deckID) {
		
		Optional<Deck> deck = deckRepository.findById(deckID);
		
		if(deck.isPresent()) {
			Deck deckPresent = deck.get();
			
			if(deckPresent.getDeckBuilder() == user) {
				
				if(deckPresent.getIsPublic().equals(true)) {
					
					// Retire des decks likés et modifie le nb de decks likés des users qui ont liké
					List <DeckCreator> dbs = deckBuilderRepository.findAll();
					 
					for (DeckCreator deckCreator : dbs) {
						if (deckCreator.getDecksLiked().contains(deckPresent)) {
							deckCreator.setDecksPublicNumber(deckCreator.getDecksPublicNumber() - 1);
														
							deckBuilderRepository.save(deckCreator);
						}
					}
					
					deckPresent.setIsPublic(false);
					deckPresent.setDatePublication(null);
					deckRepository.save(deckPresent);
					return " deck " + deckPresent.getName() + " en privé";
				}
				
			}
			throw new RuntimeException("Authentification ne correspond pas");
		}
		throw new RuntimeException("Deck non trouvé");
	}
	
	
	// Fais une moyenne du cout en Mana des cartes du deck qui ne sont pas des terrains
	@Override
	public Float getDeckManaCost(Long deckID) {
		Optional<Deck> deck = deckRepository.findById(deckID);
		
		int deckManaCost = 0;
		float deckManaCostMoy = 0;
		int i = 0;
		
		if(deck.isPresent()) {
			 List<Card> cardsDeck = deck.get().getCards();
			 for (Card card : cardsDeck) {
				 Double cardValue = card.getCmc();
				 if(!card.getTypes().contains("Land") ) {
					 i++;
					 deckManaCost += cardValue;	
				 	deckManaCostMoy = deckManaCost / i;
				 }
			}
			
		return deckManaCostMoy;
		}
		throw new RuntimeException("Deck non trouvé");
	}


			

}
