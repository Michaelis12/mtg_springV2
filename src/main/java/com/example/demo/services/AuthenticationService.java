package com.example.demo.services;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.GetAuthentification;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.UserActivity;
import com.example.demo.enums.UserRole;
import com.example.demo.form.FormDeckbuilder;
import com.example.demo.form.FormProfil;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.security.ConfigurePasswordEncoder;
import com.example.demo.security.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
// Le constructor est nécessaire dans cette class pour éviter les dépendances cycliques
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
	
	// Les méthodes register et login se retrouvent ici pour éviter des bugs liés aux dépendances cycliques
	
	@Autowired
	private DeckBuilderRepository deckBuilderRepository;
		
	@Autowired
	private DeckRepository deckRepository;
	
	@Autowired
	private CardRepository cardRepository;
	
	
	@Autowired
	private ConfigurePasswordEncoder passwordEncoder;
	// Appel de ConfigurePasswordEncoder pour avoir notre encodage personalisé plutot que le PasswordEncoder d'origine
	
	@Autowired
	private  JwtService jwtService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private DeckService deckService;
	
	
	
	@Autowired
	private FileService fileService;
	

	// Méthode d'inscription d'un utilisateur
	@Override
	public DeckCreator inscription (FormDeckbuilder formDeckBuilder) {
		
		
		if(formDeckBuilder.getPassword().equals(formDeckBuilder.getConfirmPassword()) ) {
			
			List <DeckCreator> existingDeckBuilder = deckBuilderRepository.findAll();
			
			boolean emailExists = existingDeckBuilder.stream()
			        .anyMatch(existing -> existing.getEmail().equals(formDeckBuilder.getEmail()));
			
			boolean pseudoExists = existingDeckBuilder.stream()
			        .anyMatch(existing -> existing.getPseudo().equals(formDeckBuilder.getPseudo()));
			 
			if(!emailExists) {
				
				if(!pseudoExists) {
					
					DeckCreator deckBuilder = new DeckCreator();
					
					// Générer un code de vérification
				    String verificationCode = RandomStringUtils.randomAlphanumeric(6);
				    
				    				    
				    deckBuilder.setPseudo(formDeckBuilder.getPseudo());
					deckBuilder.setAvatar(formDeckBuilder.getAvatar());
					deckBuilder.setEmail(formDeckBuilder.getEmail());
					deckBuilder.setPassword(passwordEncoder.encode(formDeckBuilder.getPassword()));
					deckBuilder.setBio(formDeckBuilder.getBio());
					deckBuilder.setDateSign(LocalDate.now());
					deckBuilder.setDecksNumber((long) 0);
					deckBuilder.setDecksLikedNumber((long) 0);
					deckBuilder.setDecksPublicNumber((long) 0);
					deckBuilder.setDecksPublicLikesNumber((long) 0);
					deckBuilder.setActivity(UserActivity.INACTIVE);
					deckBuilder.getRoles().add(UserRole.USER);
					deckBuilder.setVerificationCode(verificationCode);
					deckBuilder.setEnabled(false);
					
					
					// Envoyer le code par email
				    emailService.sendVerificationEmail(deckBuilder.getEmail(), verificationCode);
					
					return deckBuilderRepository.save(deckBuilder);
				
				}
				
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce pseudo est déja utilisé par un autre utilisateur");
			}
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un compte existe deja avec cette adresse");
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe et la confirmation ne correspondent pas");
	}
	
	// Méthode d'inscription d'un admin
			@Override
			public DeckCreator addAdmin (FormDeckbuilder formDeckBuilder) {
				
				
				if(formDeckBuilder.getPassword().equals(formDeckBuilder.getConfirmPassword()) ) {
					
					List <DeckCreator> existingDeckBuilder = deckBuilderRepository.findAll();
					
					boolean emailExists = existingDeckBuilder.stream()
					        .anyMatch(existing -> existing.getEmail().equals(formDeckBuilder.getEmail()));
					
					boolean pseudoExists = existingDeckBuilder.stream()
					        .anyMatch(existing -> existing.getPseudo().equals(formDeckBuilder.getPseudo()));
					 
					if(!emailExists) {
						
						if(!pseudoExists) {
							
							DeckCreator deckBuilder = new DeckCreator();
							
							// Générer un code de vérification
						    //String verificationCode = RandomStringUtils.randomAlphanumeric(6);
						    					    				    
						    deckBuilder.setPseudo(formDeckBuilder.getPseudo());
							deckBuilder.setAvatar(formDeckBuilder.getAvatar());
							deckBuilder.setEmail(formDeckBuilder.getEmail());
							deckBuilder.setPassword(passwordEncoder.encode(formDeckBuilder.getPassword()));
							deckBuilder.setBio(formDeckBuilder.getBio());
							deckBuilder.setDateSign(LocalDate.now());
							deckBuilder.setDecksNumber((long) 0);
							deckBuilder.setDecksLikedNumber((long) 0);
							deckBuilder.setDecksPublicNumber((long) 0);
							deckBuilder.setDecksPublicLikesNumber((long) 0);
							deckBuilder.setActivity(UserActivity.INACTIVE);
							deckBuilder.getRoles().add(UserRole.USER);
							deckBuilder.getRoles().add(UserRole.ADMIN);
							deckBuilder.setEnabled(true);
							
							
							// Envoyer le code par email
						    //emailService.sendVerificationEmail(deckBuilder.getEmail(), verificationCode);
							
							return deckBuilderRepository.save(deckBuilder);
						
						}
						
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "! Ce pseudo est déja utilisé par un autre utilisateur");
					}
					
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "! Un compte existe deja avec cette adresse");
				}
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "! Le mot de passe et la confirmation ne correspondent pas");
			}
	
	@Override
	public boolean verifyUser(String email, String code) {
	    DeckCreator user = deckBuilderRepository.findByEmail(email)
	        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	    
	    if (user.getVerificationCode().equals(code)) {
	        return true;
	    }
	    
	    return false;
	}
	
	
	@Override
	public boolean verifyUserSign (String email, String code) {
	    DeckCreator user = deckBuilderRepository.findByEmail(email)
	        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	    
	    if (user.getVerificationCode().equals(code)) {
	        user.setEnabled(true);
	        user.setActivity(UserActivity.VIEWVER);
	        user.setVerificationCode(null);
	        deckBuilderRepository.save(user);
	        return true;
	    }
	    
	    return false;
	}
	
	
	
	@Override
	public GetAuthentification connexion(Map<String, String> request) {
	    String email = request.get("email");
	    String password = request.get("password");

	    Optional<DeckCreator> optionalUser = deckBuilderRepository.findByEmail(email);

	    if (optionalUser.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ou mot de passe incorrect, veuillez réessayer");
	    }

	    DeckCreator user = optionalUser.get();
	    
	    if (user.getActivity().equals(UserActivity.BANNED)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compte suspendu");
	    }

	    
	    
	    if (!user.isEnabled()) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compte non activé");
	    }
	    
	    
	    
	    if (!passwordEncoder.matches(password, user.getPassword())) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ou mot de passe incorrect, veuillez réessayer");
	    }

	    String jwt = jwtService.generateToken(user);
	    

	    GetAuthentification auth = new GetAuthentification();
	    auth.setJwt(jwt); // Utilisé uniquement pour créer un cookie dans le contrôleur
	    auth.setAuthorities(user.getAuthorities());
	    

	    return auth;
	}
	
	@Override
	public String sendCodePassword (String email) {
		
		Optional<DeckCreator> optionalUser = deckBuilderRepository.findByEmail(email);
		
		 if (optionalUser.isEmpty()) {
		        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email inconnu, veuillez entrer une adresse valide");
		    }
		 
		 DeckCreator user = optionalUser.get();
		 
		 String verificationCode = RandomStringUtils.randomAlphanumeric(6);
		 
		 user.setVerificationCode(verificationCode);
		 deckBuilderRepository.save(user);
		 
		 emailService.sendVerificationEmail(email, verificationCode);
		 
		 
		 /*
		 // Générer le token de réinitialisation du mot de passe avec l'email
		  String resetToken = jwtService.generatePasswordResetToken(email);

		  // Envoyer le lien de réinitialisation à l'utilisateur
		  // Le lien inclut le token généré dans l'URL pour la réinitialisation
		  String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
		  
		  emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
		  */
		  return "Email envoyé à  l'adresse " + email;
		
	}
	
	@Override
	public String resetPassword(String email, String newPassword, String confirmPassword, String code) {
		
		if (!newPassword.equals(confirmPassword)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe et la confirmation ne correspondent pas");
	    }
		
		Optional<DeckCreator> optionalUser = deckBuilderRepository.findByEmail(email); 
		
		 if (optionalUser.isEmpty()) {
		        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email inconnu, veuillez entrer une adresse valide");
		    }
		 
		 DeckCreator user = optionalUser.get();
		 
		 if(!user.getVerificationCode().equals(code)) {
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code incorrect, veuillez rééssayer");
		 }
			 
			 user.setPassword(passwordEncoder.encode(newPassword));  // Encodage du nouveau mot de passe
			 user.setVerificationCode(null);

		    // Sauvegarder l'utilisateur avec le nouveau mot de passe
		    deckBuilderRepository.save(user);

		    return "Mot de passe réinitialisé avec succès.";
		
	}
	
	
	@Override
	public String updateAccount (DeckCreator user, FormProfil userUpdate) {
		
	Optional<DeckCreator> userSearch = deckBuilderRepository.findById(user.getId());
		
		if (userSearch.isPresent()) {
			String oldAvatar = user.getAvatar(); // Sauvegarder l'ancien avatar
			
			if(userUpdate.getPseudo() != null) {
				String pseudo = userUpdate.getPseudo();
				user.setPseudo(pseudo);
				}
			
			if(userUpdate.getBio() != null) {
				String bio = userUpdate.getBio();
				user.setBio(bio);
			}
			
			if(userUpdate.getAvatar() != null) {
				String avatar = userUpdate.getAvatar();
				user.setAvatar(avatar);
				// Supprimer l'ancien avatar si il est différent du nouveau
				fileService.deleteOldImageIfUnused(oldAvatar, avatar);
			}
							
			 deckBuilderRepository.save(user);
			 
			 return "modification effectuée";
		}
		
		throw new RuntimeException("User non trouvé");
	}
	
	
	@Override
	public String updatePassword (DeckCreator user, String password, String newPassword) {
		
		if (!passwordEncoder.matches(password, user.getPassword())) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe incorrect, veuillez réessayer");
	    }
		user.setPassword(passwordEncoder.encode(newPassword));
		
		deckBuilderRepository.save(user);
		
		return "Mot de passe modifié";
		
	}
	
	
	@Override
	public String desactiveAccount(Long userID) {
		
		DeckCreator user = deckBuilderRepository.findById(userID)
		.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
		
		user.setActivity(UserActivity.INACTIVE);
		
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
	public String reactiveAccount (Long userID) {
		
		DeckCreator user = deckBuilderRepository.findById(userID)
		.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
		
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
	public void deleteAccount(DeckCreator user) {
	    Optional<DeckCreator> userSearch = deckBuilderRepository.findById(user.getId());
	    
	    if (!userSearch.isPresent()) {
	        throw new RuntimeException("User non trouvé");
	    }
	    
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
	    
	    // Déconnecter l'utilisateur après suppression
	    SecurityContextHolder.clearContext();
	}
	
	
	

}
 



