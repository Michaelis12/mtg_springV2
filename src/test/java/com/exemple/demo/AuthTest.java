package com.exemple.demo;

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
import com.example.demo.security.ConfigurePasswordEncoder;
import com.example.demo.security.JwtService;
import com.example.demo.services.AuthenticationService;
import com.example.demo.services.DeckService;
import com.example.demo.services.EmailService;
import com.example.demo.services.FileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AuthTest {

    @Mock
    private DeckBuilderRepository deckBuilderRepository;
    
    @Mock
    private CardRepository cardRepository;
    
    @Mock
	private DeckRepository deckRepository;

    @Mock
    private ConfigurePasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;
    
    @Mock
    private FileService fileService;


    @Mock
    private DeckService deckService;
    

    private DeckCreator user;

    @InjectMocks
    private AuthenticationService authenticationService;

    private FormDeckbuilder form;
    

    @BeforeEach
    void setUp() {
        form = new FormDeckbuilder();
        form.setPseudo("testUser");
        form.setEmail("test@test.com");
        form.setPassword("password");
        form.setConfirmPassword("password");
        form.setAvatar("avatar.png");
        form.setBio("bio");
        
        user = DeckCreator.builder()
                .email(form.getEmail())
                .password("encodedPassword")
                .enabled(true)
                .activity(UserActivity.INACTIVE)
                .roles(new ArrayList<>())
                .build();
    }


    @Test
    void testInscription_Success() {
        when(deckBuilderRepository.findAll()).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode("password")).thenReturn("encodedPwd");

        DeckCreator savedUser = DeckCreator.builder()
                .id(1L)
                .email(form.getEmail())
                .pseudo(form.getPseudo())
                .password("encodedPwd")
                .roles(new ArrayList<>())
                .enabled(false)
                .build();

        when(deckBuilderRepository.save(any())).thenReturn(savedUser);

        DeckCreator result = authenticationService.inscription(form);

        assertEquals(form.getEmail(), result.getEmail());
        assertEquals("encodedPwd", result.getPassword());
        verify(emailService, times(1)).sendVerificationEmail(eq(form.getEmail()), anyString());
    }

    @Test
    void testInscription_EmailAlreadyExists() {
        DeckCreator existing = new DeckCreator();
        existing.setEmail("test@test.com");
        existing.setPseudo("otherPseudo"); // 👈 IMPORTANT sinon NPE
        when(deckBuilderRepository.findAll()).thenReturn(List.of(existing));

        assertThrows(ResponseStatusException.class,
                () -> authenticationService.inscription(form));
    }

    @Test
    void testInscription_PseudoAlreadyExists() {
        DeckCreator existing = new DeckCreator();
        existing.setEmail("other@test.com"); // 👈 IMPORTANT sinon NPE
        existing.setPseudo("testUser");
        when(deckBuilderRepository.findAll()).thenReturn(List.of(existing));

        assertThrows(ResponseStatusException.class,
                () -> authenticationService.inscription(form));
    }


    @Test
    void testInscription_PasswordMismatch() {
        form.setConfirmPassword("wrong");

        assertThrows(ResponseStatusException.class,
                () -> authenticationService.inscription(form));
    }

   
    // ---------- VERIFY USER ----------
    @Test
    void testVerifyUser_Success() {
        DeckCreator user = new DeckCreator();
        user.setVerificationCode("123456");
        when(deckBuilderRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        boolean result = authenticationService.verifyUser("test@test.com", "123456");

        assertTrue(result);
    }

    @Test
    void testVerifyUser_InvalidCode() {
        DeckCreator user = new DeckCreator();
        user.setVerificationCode("654321");
        when(deckBuilderRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        boolean result = authenticationService.verifyUser("test@test.com", "123456");

        assertFalse(result);
    }

    // ---------- VERIFY USER SIGN ----------
    @Test
    void testVerifyUserSign_Success() {
        DeckCreator user = new DeckCreator();
        user.setVerificationCode("123456");
        when(deckBuilderRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        boolean result = authenticationService.verifyUserSign("test@test.com", "123456");

        assertTrue(result);
        assertNull(user.getVerificationCode());
        assertEquals(UserActivity.VIEWVER, user.getActivity());
        verify(deckBuilderRepository).save(user);
    }

    // ---------- CONNEXION ----------
    @Test
    void testConnexion_Success() {
        Map<String, String> request = Map.of("email", form.getEmail(), "password", form.getPassword());

        when(deckBuilderRepository.findByEmail(form.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(form.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        GetAuthentification result = authenticationService.connexion(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getJwt());
        assertEquals(user.getAuthorities(), result.getAuthorities());

        verify(deckBuilderRepository, times(1)).findByEmail(form.getEmail());
        verify(passwordEncoder, times(1)).matches(form.getPassword(), "encodedPassword");
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void testConnexion_WrongPassword() {
        Map<String, String> request = Map.of("email", form.getEmail(), "password", "wrongPassword");

        when(deckBuilderRepository.findByEmail(form.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authenticationService.connexion(request));

        assertEquals("Email ou mot de passe incorrect, veuillez réessayer", exception.getReason());

        verify(deckBuilderRepository, times(1)).findByEmail(form.getEmail());
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
        verify(jwtService, never()).generateToken(any());
    }
    
    
    
    @Test
    void testConnexion_UserNotEnabled() {
    	user.setEnabled(false);

        Map<String, String> request = Map.of("email", form.getEmail(), "password", form.getPassword());

        when(deckBuilderRepository.findByEmail(form.getEmail())).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authenticationService.connexion(request));

        assertEquals("Compte non activé", exception.getReason());
    }

    @Test
    void testConnexion_UserBanned() {
    	user.setActivity(UserActivity.BANNED);

        Map<String, String> request = Map.of("email", form.getEmail(), "password", form.getPassword());

        when(deckBuilderRepository.findByEmail(form.getEmail())).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authenticationService.connexion(request));

        assertEquals("Compte suspendu", exception.getReason());
    }
    
    // ---------- SEND CODE PASSWORD ----------
    @Test
    void testSendCodePassword_Success() {
        DeckCreator user = new DeckCreator();
        user.setEmail("test@test.com");
        when(deckBuilderRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        String result = authenticationService.sendCodePassword("test@test.com");

        assertTrue(result.contains("Email envoyé"));
        verify(emailService).sendVerificationEmail(eq("test@test.com"), anyString());
        verify(deckBuilderRepository).save(user);
    }

    // ---------- RESET PASSWORD ----------
    @Test
    void testResetPassword_Success() {
        DeckCreator user = new DeckCreator();
        user.setEmail("test@test.com");
        user.setVerificationCode("123456");

        when(deckBuilderRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPwd")).thenReturn("encodedNewPwd");

        String result = authenticationService.resetPassword("test@test.com", "newPwd", "newPwd", "123456");

        assertEquals("Mot de passe réinitialisé avec succès.", result);
        assertEquals("encodedNewPwd", user.getPassword());
        assertNull(user.getVerificationCode());
        verify(deckBuilderRepository).save(user);
    }
    
 // ---------------------- updateAccount ----------------------
    @Test
    void testUpdateAccount_Success() {
        // Préparer l’utilisateur existant
        user.setId(1L);
        user.setAvatar("oldAvatar.png");
        when(deckBuilderRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Préparer les données de mise à jour
        FormProfil updateForm = new FormProfil();
        updateForm.setPseudo("newPseudo");
        updateForm.setBio("newBio");
        updateForm.setAvatar("newAvatar.png");

        // Appeler la méthode
        String result = authenticationService.updateAccount(user, updateForm);

        // Vérifier les modifications
        assertEquals("newPseudo", user.getPseudo());
        assertEquals("newBio", user.getBio());
        assertEquals("newAvatar.png", user.getAvatar());
        assertEquals("modification effectuée", result);

        // Vérifier les appels aux services
        verify(deckBuilderRepository, times(1)).save(user);
        verify(fileService, times(1)).deleteOldImageIfUnused("oldAvatar.png", "newAvatar.png");
    }



    // ---------------------- updatePassword ----------------------
    @Test
    void testUpdatePassword_Success() {
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPwd");
        when(deckBuilderRepository.save(user)).thenReturn(user);

        String result = authenticationService.updatePassword(user, "password", "newPassword");

        assertEquals("Mot de passe modifié", result);
        assertEquals("newEncodedPwd", user.getPassword());
        verify(deckBuilderRepository).save(user);
    }

    @Test
    void testUpdatePassword_WrongPassword() {
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authenticationService.updatePassword(user, "wrong", "newPassword"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Mot de passe incorrect, veuillez réessayer", exception.getReason());
        verify(deckBuilderRepository, never()).save(user);
    }
    
    
    @Test
    void testReactiveAccount_Success() {
        // Création de l'utilisateur avec un état initial
        DeckCreator user = new DeckCreator();
        user.setId(1L);
        user.setActivity(UserActivity.INACTIVE); // Initialisation de l'activité

        // Création de quelques decks pour l'utilisateur
        Deck deck1 = mock(Deck.class);
        Deck deck2 = mock(Deck.class);

        // Simuler que l'utilisateur a des decks
        when(deckRepository.findByDeckBuilder(user)).thenReturn(List.of(deck1, deck2));
        when(deckBuilderRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Appel de la méthode
        String result = authenticationService.reactiveAccount(user.getId());

        // Vérifications
        assertEquals("Compte activé", result);
        assertEquals(UserActivity.CREATOR, user.getActivity()); // Vérifie que l'activité a bien été mise à jour à CREATOR
        
        // Vérifier que l'utilisateur a été sauvegardé
        verify(deckBuilderRepository).save(user); // Le changement d'activité de l'utilisateur doit être sauvegardé
        verify(deckRepository, never()).save(any()); // Aucun deck ne doit être sauvegardé
    }



    // ---------------------- desactiveAccount ----------------------
    @Test
    void testDesactiveAccount_Success() {
        Deck deck1 = mock(Deck.class);
        Deck deck2 = mock(Deck.class);
        when(deck1.getIsPublic()).thenReturn(true);
        when(deck2.getIsPublic()).thenReturn(false);
        when(deckRepository.findByDeckBuilder(user)).thenReturn(List.of(deck1, deck2));
        when(deckBuilderRepository.findById(user.getId())).thenReturn(Optional.of(user));

        String result = authenticationService.desactiveAccount(user.getId());

        assertEquals("Compte desactivé", result);
        assertEquals(UserActivity.INACTIVE, user.getActivity());
        verify(deckRepository).save(deck1);
        verify(deckRepository, never()).save(deck2);
        verify(deckBuilderRepository).save(user);
    }

 

    // ---------------------- deleteAccount ----------------------
    @Test
    void testDeleteAccount_Success() {
        Deck deck = Deck.builder().id(0L).build(); // un seul deck créé
        List<Deck> decksCreated = List.of(deck);

        when(deckBuilderRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(deckRepository.findByDeckBuilder(user)).thenReturn(decksCreated);
        
        user.setDecksLiked(new ArrayList<>());

        authenticationService.deleteAccount(user);

        verify(deckService, times(1)).deleteDeck(user, 0L); // correspond maintenant à un seul élément
        verify(deckBuilderRepository).deleteById(user.getId());
    }



   

}
