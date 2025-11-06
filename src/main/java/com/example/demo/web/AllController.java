package com.example.demo.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.GetAuthentification;
import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.dto.GetFormat;
import com.example.demo.dto.GetPage;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.CardType;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumEdition;
import com.example.demo.enums.EnumFormat;
import com.example.demo.enums.EnumRarity;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormDeckbuilder;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.security.JwtService;
import com.example.demo.services.CardService;
import com.example.demo.services.DeckBuilderService;
import com.example.demo.services.DeckService;
import com.example.demo.services.EmailService;
import com.example.demo.services.IAuthenticationService;
import com.example.demo.services.RegleService;
import aj.org.objectweb.asm.commons.TryCatchBlockSorter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@RestController
@RequestMapping("f_all")
public class AllController {
	
	// Contient les requetes nécessaires pour s'atuthentifier
	// Accessibles à tous
	
	
	@Autowired
	private IAuthenticationService iAuthenticationService;
	
	@Autowired
	private DeckBuilderService deckBuilderService;
	
	@Autowired
	private CardService cardService;
	
	@Autowired
	private DeckService deckService;
		
	@Autowired
	private RegleService regleService;
	
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	CardRepository cardRepository;
	
	@Autowired
	DeckRepository deckRepository;
	
	@Autowired
	DeckBuilderRepository deckBuilderRepository;
	
	
	@PostMapping("sendEmail")
	public void sendEmail( @RequestBody String email) {
		 String verificationCode = RandomStringUtils.randomAlphanumeric(6);
		 emailService.sendVerificationEmail(email, verificationCode);
		
	}
	
	
	@PostMapping("add1stAdmin")
	public DeckCreator addAdmin( @RequestBody FormDeckbuilder db) {
		return iAuthenticationService.addAdmin(db);
		
	}
	
	@DeleteMapping("deleteUserTemp")
	public String deleteAccount(@RequestParam Long dbID) {
		return deckBuilderService.deleteDeckBuilder(dbID);
	}
	
	@PostMapping("inscription")
	public DeckCreator inscription(@RequestBody FormDeckbuilder db) {
		return iAuthenticationService.inscription(db);
		
	}
	
	@PostMapping("/verification")
	public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
	    if (iAuthenticationService.verifyUser(email, code)) {
	        return ResponseEntity.ok("Compte vérifié avec succès");
	    } else {
	        return ResponseEntity.badRequest().body("! Code de vérification invalide");
	    }
	}
	
	@PostMapping("/verificationSign")
	public ResponseEntity<?> verifyUserSign(@RequestParam String email, @RequestParam String code) {
	    if (iAuthenticationService.verifyUserSign(email, code)) {
	        return ResponseEntity.ok("Compte vérifié avec succès");
	    } else {
	        return ResponseEntity.badRequest().body("! Code de vérification invalide");
	    }
	}
	
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, 
			HttpServletRequest request, HttpServletResponse response) {
	    GetAuthentification auth = iAuthenticationService.connexion(credentials);
	    
	    ResponseCookie cookie = ResponseCookie.from("jwt", auth.getJwt())
	            .httpOnly(true)
	            .secure(true) 
	            .path("/")
	            .maxAge(24 * 60 * 60) 
	            .sameSite("None")
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());    

	    //return ResponseEntity.ok(Map.of("authorities", auth.getAuthorities()));
	    return ResponseEntity.ok(auth);
	}
	
	
	
	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {
	    // Supprime le cookie JWT en utilisant ResponseCookie pour une cohérence avec la création
	    ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
	            .httpOnly(true)
	            .secure(true) // doit correspondre à la configuration du login
	            .path("/")
	            .maxAge(0) // expire immédiatement
	            .sameSite("None")
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
	    
	    return ResponseEntity.ok("Déconnexion réussie");
	}
	
	@PostMapping("/forgotPassword")
	public ResponseEntity sendCodePassword(@RequestParam String email) {
		
		 return ResponseEntity.ok(iAuthenticationService.sendCodePassword(email));
		
	}
	
	@PostMapping("/resetPassword")
	public ResponseEntity resetPassword(@RequestParam String email, @RequestParam String newPassword, @RequestParam String confirmPassword, @RequestParam String code ) {
				
		 return ResponseEntity.ok(iAuthenticationService.resetPassword(email, newPassword, confirmPassword, code ));
		
	}
	
	
	@GetMapping("getTop3Cards")
	public List<GetCard> getTop3Cards() {
		return cardService.getTop3Cards();
	}
	
	
	@GetMapping("getTop3Commandants")
	public List<GetCard> getTop3Cedh() {
		return cardService.getTop3Cedh();
	}
	
	
	@GetMapping("getTop3Decks")
	public List<GetDeck> getTop3Decks() {
		return deckService.getTop3Decks();
	}
	
	@GetMapping("getCardsRanked")
	public List<Long> getCardsRanked(@RequestParam String order) {
		return cardService.getAllCardsRanked(order);
	}
	
	
	@GetMapping("getCardsPaged")
	public ResponseEntity<Map<String, Object>> getCardsByFilterPaged(
	@RequestParam int page,
    @RequestParam int size,
    @RequestParam String order,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String text,
    @RequestParam(required = false) Double manaCostMin,
    @RequestParam(required = false) Double manaCostMax,
    @RequestParam(required = false) List<String> types,
    @RequestParam(required = false) Boolean legendary,
    @RequestParam(required = false) List<String> rarities,
    @RequestParam(required = false) List<String> colors,
    @RequestParam(required = false) List<String> formats,
    @RequestParam(required = false) List<String> editions) 
	{
	    Page<GetCard> pageResult = cardService.getCardsByFilterPaged(
	        page, size, order, name, text, manaCostMin, manaCostMax, types, legendary, rarities, colors, formats, editions
	    );
	    
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
	

	
	
	
	@GetMapping("getDeckID")
	public ResponseEntity getDeckById(@RequestParam Long deckID) {
		return ResponseEntity.ok(deckService.getDeckById(deckID));

	}
	
	@GetMapping("getDeckUser")
	public ResponseEntity getDeckUser(@RequestParam Long deckID) {
		return ResponseEntity.ok(deckService.getDeckUser(deckID));

	}
	
	
	@GetMapping("getDecks")
	public ResponseEntity<Map<String, Object>> getDecksByFilterPaged(
	    @RequestParam int page,
	    @RequestParam int size,
	    @RequestParam String order,
	    @RequestParam(required = false) String name,
	    @RequestParam(required = false) Float manaCostMin,
	    @RequestParam(required = false) Float manaCostMax,
	    @RequestParam(required = false) List<EnumFormat> formats,
	    @RequestParam(required = false) List<EnumColor> colors
	) {
		
		
	    
	    Page<GetDeck> pageResult = deckService.getDecksByFilterPaged(page, size, order, name, manaCostMin, manaCostMax, formats, colors);
	    
	    
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
	
	
	@GetMapping("getDecksUserPaged")
	public ResponseEntity getDeckUserPaged (@RequestParam Long userID, @RequestParam int page, @RequestParam int size, @RequestParam String order) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findById(userID);
		
		Page<GetDeck> pageResult = deckService.getDecksUserPaged(user.get(), page, size, order);
				
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
	    response.put("deckNumber", pageResult.getContent().isEmpty() ? 0 : pageResult.getContent().size());
	    
	    return ResponseEntity.ok(response);
	}
	
	/*
	@GetMapping("getDecksUsingCard")
	public ResponseEntity<Map<String, Object>> getDecksUsingCard(
	    @RequestParam Long cardID,
	    @RequestParam int page,
	    @RequestParam int size
	) {
	    // Créer l'objet Pageable avec page et size
	    Pageable pageable = PageRequest.of(page, size);
	    
	    // Appeler la méthode service avec le cardID et Pageable
	    Page<GetDeck> pageResult = cardService.getDecksUsingCard(cardID, pageable);
	    
	    // Construire la réponse paginée
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
	
	@GetMapping("getTopDecksUsingCard")
	public ResponseEntity<Map<String, Object>> getTopDecksUsingCard(
	    @RequestParam Long cardID,
	    @RequestParam int page,
	    @RequestParam int size
	) {
	    // Créer l'objet Pageable avec page et size
	    Pageable pageable = PageRequest.of(page, size);
	    
	    // Appeler la méthode service avec le cardID et Pageable
	    Page<GetDeck> pageResult = cardService.getTopDecksUsingCard(cardID, pageable);
	    
	    // Construire la réponse paginée
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
	
	
	@GetMapping("getNumberDecksUsingCard")
	public ResponseEntity getNumberDecksUsingCard(@RequestParam Long cardID) {
		return ResponseEntity.ok(cardService.getNumberDecksUsingCard(cardID));
	}
	*/
	@GetMapping("getNextDeck")
	public ResponseEntity getNextDeck(@RequestParam Long deckID, @RequestParam List<Long> decksID) {
		return ResponseEntity.ok(deckService.getNextDeck(deckID, decksID ));

	}
	
	@GetMapping("getPrevDeck")
	public ResponseEntity getPrevDeck(@RequestParam Long deckID, @RequestParam List<Long> decksID) {
		return ResponseEntity.ok(deckService.getPrevDeck(deckID, decksID ));

	}
	
	
	@GetMapping("getCardDeckID")
	public ResponseEntity getCardsOnDeckById(@RequestParam Long deckID) {
		return ResponseEntity.ok(deckService.getCardsOnDeck(deckID));
	}
	
	@GetMapping("get7CardsDeckID")
	public ResponseEntity get7CardsDeckID(@RequestParam Long deckID) {
		return ResponseEntity.ok(deckService.get7CardsOnDeck(deckID));
	}
	
	
	@GetMapping("getCedhDeckID")
	public ResponseEntity getCedhOnDeck(@RequestParam Long deckID) {
		return ResponseEntity.ok(deckService.getCommandantOndeck(deckID));
	}
	
	@GetMapping("getUserID")
	public ResponseEntity getDeckBuilder(@RequestParam Long userID) {
		
		return deckBuilderRepository.findById(userID)
		        .map(deckCreator -> {
		            if (!deckCreator.isEnabled() || deckCreator.getActivity() == UserActivity.INACTIVE) {
		                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                                     .body("Utilisateur non disponible");
		            }
		            return ResponseEntity.ok(deckBuilderService.getDeckBuilder(deckCreator));
		        })
		        .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                                       .body("Utilisateur non trouvé"));

	}
	
	
	@GetMapping("getUserLikes")
	public ResponseEntity getUserLikes(@RequestParam Long userID){
		
		Optional <DeckCreator> user = deckBuilderRepository.findById(userID);
		
		if(user.isPresent()) {
			return ResponseEntity.ok(deckBuilderService.getUserLikes(user.get()));
		}
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User non trouvé");
	}
	
	
	@GetMapping("getNextUser")
	public ResponseEntity getNextDeckbuilder(@RequestParam Long userID, @RequestParam List<Long> usersID) {
		return ResponseEntity.ok(deckBuilderService.getNextDeckbuilder(userID, usersID ));

	}
	
	@GetMapping("getPrevUser")
	public ResponseEntity getPrevDeckbuilder(@RequestParam Long userID, @RequestParam List<Long> usersID) {
		return ResponseEntity.ok(deckBuilderService.getPrevDeckbuilder(userID, usersID ));

	}
		

	@GetMapping("getRegles")
	public ResponseEntity getRegles() {
		return ResponseEntity.ok(regleService.getRegles());
	}
	
	@GetMapping("getRegleByID")
	public ResponseEntity getRegleByID(@RequestParam String regleID) {
		return ResponseEntity.ok(regleService.getRegleByID(regleID));
	}
	
	@GetMapping("getPrevRegle")
	public ResponseEntity getPrevRegle(@RequestParam String regleID, @RequestParam List<String> reglesID) {
		return ResponseEntity.ok(regleService.getPrevRegle(regleID, reglesID ));
	}
	
	@GetMapping("getNextRegle")
	public ResponseEntity getNextRegle(@RequestParam String regleID, @RequestParam List<String> reglesID) {
		return ResponseEntity.ok(regleService.getNextRegle(regleID, reglesID ));
	}
	
	
	@PostMapping("/uploadImage")
	public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
		try {
			// Vérifier que le fichier n'est pas vide
			if (file.isEmpty()) {
				return ResponseEntity.badRequest().body("Fichier vide");
			}
			
			// Vérifier le type de fichier (images seulement)
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				return ResponseEntity.badRequest().body("Type de fichier non supporté. Seules les images sont autorisées.");
			}
			
			// Créer le dossier uploads s'il n'existe pas
			String uploadDir = "uploads";
			File uploadFolder = new File(uploadDir);
			if (!uploadFolder.exists()) {
				uploadFolder.mkdirs();
			}
			
			// Générer un nom de fichier unique
			String originalFilename = file.getOriginalFilename();
			String fileExtension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
			
			// Sauvegarder le fichier
			Path filePath = Paths.get(uploadDir, uniqueFilename);
			Files.copy(file.getInputStream(), filePath);
			
			// Retourner le chemin relatif du fichier
			String relativePath = "/uploads/" + uniqueFilename;
			return ResponseEntity.ok(relativePath);
			
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload: " + e.getMessage());
		}
	}
	
	
	
}
