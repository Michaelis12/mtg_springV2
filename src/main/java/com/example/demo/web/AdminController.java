package com.example.demo.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeckBuilder;
import com.example.demo.entities.DeckCreator;
import com.example.demo.entitiesNoSQL.Regle;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormDeckbuilder;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.services.IAuthenticationService;
import com.example.demo.services.ICardService;
import com.example.demo.services.IDeckBuilderService;
import com.example.demo.services.IDeckService;
import com.example.demo.services.RegleService;
import com.example.demo.services.FileService;

@RestController
@RequestMapping("f_admin")
public class AdminController {
	
	// Les requetes seulement effectuées par l'admin
	
	@Autowired
	DeckBuilderRepository deckBuilderRepository;
	
	@Autowired
	private IDeckBuilderService iAccountService;
	
	@Autowired
	private IDeckService iDeckService;
	
	@Autowired
	private IAuthenticationService iAuthenticationService;
	
	@Autowired
	ICardService iCardService;
	
	@Autowired
	private RegleService regleService;
	
	@Autowired
	private FileService fileService;
	
	@PostMapping("addRegle")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity addRegle(Authentication authentication, @RequestBody Regle regle) {
		return  ResponseEntity.ok(regleService.addRegle(regle));
		
	}
	
	@DeleteMapping("deleteRegle")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity deleteRegle(Authentication authentication, @RequestParam String regleID) {
		return ResponseEntity.ok(regleService.deleteRegle(regleID));
		
	}
	
	@PutMapping("updateRegle")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity updateRegle(Authentication authentication, @RequestParam String regleID, @RequestBody Map<String, String> regleForm) {
		
		String title = regleForm.get("title");
        String text = regleForm.get("text");
        
		return ResponseEntity.ok(regleService.updateRegle(regleID, title, text));
		
	}
	
	
	@PostMapping("addAdmin")
	@PreAuthorize("hasAuthority('ADMIN')")
	public DeckCreator addAdmin(Authentication authentication, @RequestBody FormDeckbuilder formDeckBuilder) {
		return iAuthenticationService.addAdmin(formDeckBuilder);
		
	}
	
	@DeleteMapping("deleteUser")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String deleteAccount(Authentication authentication, @RequestParam Long dbID) {
		return iAccountService.deleteDeckBuilder(dbID);
	}
	
	@PutMapping("activeUser")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity activeAccount(Authentication authentication, @RequestParam Long userID) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iAccountService.activeAccount(userID));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification"); 
	}
	
	@PutMapping("desacUser")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity desactiveAccount(Authentication authentication, @RequestParam Long userID, @RequestBody String cause) {
		
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iAccountService.suspendAccount(userID));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification"); 
	}
	
	@PutMapping("desacUserTemporal")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity desactiveAccountTermporal(Authentication authentication, @RequestParam Long userID, @RequestParam int days, 
			@RequestBody String cause) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findByEmail(authentication.getName());
		
		if(user.isPresent()) {
			return ResponseEntity.ok(iAccountService.suspendAccountTemporarily(userID, days));
		}
		
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Echec de l'authentification"); 
	}
	
	
	
		
	
	@GetMapping("getUsersPaged")
    @PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Map<String, Object>> getDeckBuildersByFilter ( Authentication authentication,@RequestParam int page,
		    @RequestParam int size, @RequestParam(required=false) String pseudo, @RequestParam(required=false) String email,
		    @RequestParam(required=false) List<UserActivity> activities) {
		
		 Page<GetDeckBuilder> pageResult = iAccountService.getDeckBuildersByFilterPaged(page, size, pseudo, email, activities);
			    
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
	
	
	@GetMapping("getUserID")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity getDeckBuilder(Authentication authentication, @RequestParam Long userID) {
		
		Optional <DeckCreator> user = deckBuilderRepository.findById(userID);
		
		if(user.isPresent()) {
		  return ResponseEntity.ok(iAccountService.getDeckBuilder(user.get()));
		}
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Utilisateur introuvable");
	

	}
	
	
	@PostMapping("cleanupOrphanedImages")
    @PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> cleanupOrphanedImages(Authentication authentication) {
		try {
			// Récupérer toutes les images utilisées dans la base de données
			List<String> cardImages = iCardService.getAllCardImages();
			List<String> deckImages = iDeckService.getAllDeckImages();
			List<String> userAvatars = iAccountService.getAllUserAvatars();
			
			// Nettoyer les images orphelines
			Set<String> usedImages = fileService.getUsedImagePaths(cardImages, deckImages, userAvatars);
			int deletedCount = fileService.cleanupOrphanedImages(usedImages);
			
			return ResponseEntity.ok("Nettoyage terminé. " + deletedCount + " images orphelines supprimées.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Erreur lors du nettoyage: " + e.getMessage());
		}
	}
}
