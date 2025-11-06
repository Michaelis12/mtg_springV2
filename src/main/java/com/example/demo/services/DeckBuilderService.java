package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.dto.GetDeckBuilder;
import com.example.demo.dto.GetNotification;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.entities.Notification;
import com.example.demo.entitiesNoSQL.Regle;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormProfil;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.security.ConfigurePasswordEncoder;
import com.example.demo.services.FileService;

import jakarta.servlet.http.HttpSession;
@Service
//Obligatoire pour rendre le fichier visible
public class DeckBuilderService implements IDeckBuilderService, UserDetailsService  {
	
	@Autowired
	private DeckService deckService;
	
	@Autowired
	private DeckBuilderRepository deckBuilderRepository;
	
	@Autowired
	private DeckRepository deckRepository;
	
	@Autowired
	private CardRepository cardRepository;
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private ConfigurePasswordEncoder passwordEncoder;
	
	@Autowired
	private FileService fileService;
	
	
	// La méthode qui va rechercher si le mail entré pendant l'auth correspond à une valeur de la database
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return deckBuilderRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Cette adresse mail ne correpsond à aucun compte DeckBuilder"));
	}
	
	// Méthode f_all
	@Override
	public Long getUserLikes(DeckCreator deckBuilder){
		Long likeNumber = (long) 0;

		for (Deck deck : deckRepository.findAll()) {
			if(deck.getDeckBuilder().equals(deckBuilder)) {
				likeNumber += deck.getLikeNumber();
			}
		}
		return likeNumber; 
	}
	
	// Méthodes f_user
	
	
	// Méthode f_all
		@Override
		public Set<GetNotification> getUserNotifs(DeckCreator deckBuilder){
			
			Set<Notification> userNotifs = notificationRepository.findByReceivor(deckBuilder);	
			
			Set<GetNotification> userGetNotifs = new HashSet<>();
			
			for (Notification notif : userNotifs) {
				GetNotification getNotif = new GetNotification();
				getNotif.setId(notif.getId());
				getNotif.setDeckID(notif.getDeck().getId());
				getNotif.setIssuerID(notif.getIssuer().getId());
				getNotif.setReceivorID(notif.getReceivor().getId());
				getNotif.setDeckName(notif.getDeck().getName());
				getNotif.setIssuerPseudo(notif.getIssuer().getPseudo());
				getNotif.setReceivorPseudo(notif.getReceivor().getPseudo());
				getNotif.setDate(notif.getDate());
				
				userGetNotifs.add(getNotif);
			}
			
			
			return userGetNotifs;
		}
		
	@Override
	public String deleteUserNotif( Long notifID){
		
		Optional<Notification> notification = notificationRepository.findById(notifID);
		
		if(notification.isPresent()) {
		
			notificationRepository.deleteById(notifID);
			
			return "Notification supprimée";
		}
		
		throw new RuntimeException("Notification inexistante");
	}
	
	@Override
	public GetDeckBuilder getDeckBuilder (DeckCreator deckBuilder) {
		Optional <DeckCreator> deckbuilder = deckBuilderRepository.findById(deckBuilder.getId());
		if(deckbuilder.isPresent()) {
			DeckCreator deckbuilderFind = deckbuilder.get();
			GetDeckBuilder getDeckBuilder = new GetDeckBuilder();
			getDeckBuilder.setId(deckbuilderFind.getId());
			getDeckBuilder.setPseudo(deckbuilderFind.getPseudo());
			getDeckBuilder.setEmail(deckbuilderFind.getEmail());
			getDeckBuilder.setPassword(deckbuilderFind.getPassword());
			getDeckBuilder.setAvatar(deckbuilderFind.getAvatar());
			getDeckBuilder.setBio(deckbuilderFind.getBio());
			getDeckBuilder.setRoles(deckbuilderFind.getRoles());
			getDeckBuilder.setDateSign(deckbuilderFind.getDateSign());
			getDeckBuilder.setActivity(deckbuilderFind.getActivity());
			getDeckBuilder.setDecksLikedNumber(deckbuilderFind.getDecksLikedNumber());
			getDeckBuilder.setDecksNumber(deckbuilderFind.getDecksNumber());
			
			return getDeckBuilder;
		}
		throw new RuntimeException("Utilisateur non trouvé");

	}
	
	@Override
	public Long getNextDeckbuilder (Long userID, List<Long> usersID) {
	    if (usersID.contains(userID)) {
	        for (int i = 0; i < usersID.size(); i++) {
	            if (usersID.get(i).equals(userID)) {
	                if (i + 1 < usersID.size()) {
	                    return usersID.get(i + 1);
	                } else {
	                    throw new RuntimeException("Aucun id suivant");
	                }
	            }
	        }
	    }
	    throw new RuntimeException("Id incorrect");
	}

	
	@Override
	public Long getPrevDeckbuilder(Long userID, List<Long> usersID) {
	    // Vérifie si userID est dans la liste usersID
	    int index = usersID.indexOf(userID);
	    if (index == -1) {
	        throw new RuntimeException("Id incorrect");
	    }
	    
	    // Si l'utilisateur est le premier dans la liste, il n'y a pas de précédent
	    if (index == 0) {
	        throw new RuntimeException("Aucun id précédent");
	    }
	    
	    // Retourne l'utilisateur précédant dans la liste
	    return usersID.get(index - 1);
	}
	
	
	
	
	// Partie admin
	
	
	@Override
	public String deleteDeckBuilder(Long dbID) {
		
		Optional<DeckCreator> userSearch = deckBuilderRepository.findById(dbID);
	    
	    if (!userSearch.isPresent()) {
	        throw new RuntimeException("User non trouvé");
	    }
	    
	    DeckCreator user = userSearch.get();
	    
	    // Supprime les decks créés par l'user
	    
	    List<Deck> decksCreated = deckRepository.findByDeckBuilder(user);
	    for (Deck deck : decksCreated) {
	        deckService.deleteDeck(user, deck.getId());    
	    }
	    
	    
	    // Retirer les likes de l'user de tous les decks 
	    List<Deck> decksLiked = user.getDecksLiked();
	    for (Deck deck : decksLiked) {
	        deck.getDeckBuilders().remove(user);
	        deck.setLikeNumber((long)deck.getDeckBuilders().size());
	        deckRepository.save(deck);
	    }
	    
	    
	    // Supprimer l'avatar de l'utilisateur
	    fileService.deleteImage(user.getAvatar());
	            
	    deckBuilderRepository.deleteById(user.getId());
	    
	    return "Utilisateur supprimé";
	}



	@Override
	public Page<GetDeckBuilder> getDeckBuildersByFilterPaged(int page, int size, String pseudo, String email, List<UserActivity> activities) {
		
		List<DeckCreator> deckBuilders = deckBuilderRepository.findByOptionalAttribute(pseudo, email, activities);
		
		// Tri par ID (peut être adapté à autre chose si besoin)
		deckBuilders.sort(Comparator.comparingLong(DeckCreator::getId));
		
		// Pagination manuelle
		int start = page * size;
		int end = Math.min(start + size, deckBuilders.size());

		if (start >= deckBuilders.size()) {
			return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), deckBuilders.size());
		}

		List<DeckCreator> paginatedDeckBuilders = deckBuilders.subList(start, end);
		
		List<GetDeckBuilder> getDeckBuilders = new ArrayList<>();
		
		for (DeckCreator deckBuilder : paginatedDeckBuilders) {
			GetDeckBuilder getDeckBuilder = new GetDeckBuilder();
			getDeckBuilder.setId(deckBuilder.getId());
			getDeckBuilder.setPseudo(deckBuilder.getPseudo());
			getDeckBuilder.setEmail(deckBuilder.getEmail());
			getDeckBuilder.setDateSign(deckBuilder.getDateSign());
			getDeckBuilder.setPassword(deckBuilder.getPassword());
			getDeckBuilder.setAvatar(deckBuilder.getAvatar());
			getDeckBuilder.setBio(deckBuilder.getBio());
			getDeckBuilder.setActivity(deckBuilder.getActivity());
			
			getDeckBuilders.add(getDeckBuilder);
		}
		
		return new PageImpl<>(getDeckBuilders, PageRequest.of(page, size), deckBuilders.size());
	}
	
	
	
	@Override
	public String activeAccount(Long userID) {
		
		DeckCreator user = deckBuilderRepository.findById(userID)
		.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
		
		user.setSuspendedUntil(null);
		user.setEnabled(true);
		
		
		List <Deck> decksUser = deckRepository.findByDeckBuilder(user);
		if(decksUser.isEmpty()) {
			user.setActivity(UserActivity.VIEWVER);
		}
		else {
			user.setActivity(UserActivity.CREATOR);
		}
		
		
		deckBuilderRepository.save(user);
		return "Compte activé";
	
	}
	
	@Override
	public String suspendAccount(Long userID) {
		
		DeckCreator user = deckBuilderRepository.findById(userID)
		.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
		
		user.setEnabled(false);
		user.setActivity(UserActivity.BANNED);
		
		// Repasse les decks en privé de l'user désactivé
		List <Deck> decksUser = deckRepository.findByDeckBuilder(user);
		
		for (Deck deck : decksUser) {
			if(deck.getIsPublic()) {
				deck.setIsPublic(false);
				deckRepository.save(deck);
			}
		}
		
		deckBuilderRepository.save(user);
		return "Compte desactivé";
	
	}
	
	@Override
	public String suspendAccountTemporarily(Long userID, int days) {
	    DeckCreator user = deckBuilderRepository.findById(userID)
	        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	    // Calcul de la date de suspension
	    LocalDate suspensionDate = LocalDate.now().plusDays(days);
	    user.setSuspendedUntil(suspensionDate); // Suspension jusqu'à cette date
	    user.setEnabled(false); // Désactive le compte pendant la suspension
	    user.setActivity(UserActivity.BANNED);

	    deckBuilderRepository.save(user);
	    return "Compte suspendu temporairement jusqu'au " + suspensionDate;
	}

	@Override
	public List<String> getAllUserAvatars() {
		List<DeckCreator> users = deckBuilderRepository.findAll();
		return users.stream()
			.map(DeckCreator::getAvatar)
			.filter(avatar -> avatar != null && avatar.startsWith("/uploads/"))
			.collect(Collectors.toList());
	}

	/*
	@Override
	public String unsuspendAccount(Long userID) {
	    DeckCreator user = deckBuilderRepository.findById(userID)
	        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	    user.setSuspendedUntil(null); // Supprime la suspension
	    user.setEnabled(true); // Réactive le compte

	    deckBuilderRepository.save(user);
	    return "Compte réactivé";
	}
	
	
	@Override
	public void reactivateAccountIfSuspended(Long userID) {

		DeckCreator user = deckBuilderRepository.findById(userID)
	        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	    if (user.getSuspendedUntil() != null) {
	        if (LocalDate.now().isAfter(user.getSuspendedUntil())) {
	            user.setSuspendedUntil(null); 
	            user.setEnabled(true); 
	            deckBuilderRepository.save(user);
	        } 
	    } 
	}
	*/
	

}
