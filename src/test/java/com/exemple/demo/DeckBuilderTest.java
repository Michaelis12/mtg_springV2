package com.exemple.demo;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.GetDeckBuilder;
import com.example.demo.dto.GetNotification;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.UserActivity;
import com.example.demo.enums.UserRole;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.services.DeckBuilderService;
import com.example.demo.services.DeckService;
import com.example.demo.services.FileService;
import com.example.demo.security.ConfigurePasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.example.demo.entities.Notification;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DeckBuilderTest {

    // Mocks des repositories et services
    @Mock
    private DeckBuilderRepository deckBuilderRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;
    
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DeckService deckService;
    
    @Mock
    private FileService fileService;
    
    @Mock
    private ConfigurePasswordEncoder passwordEncoder;

    @InjectMocks
    private DeckBuilderService deckBuilderService;

    private DeckCreator deckCreator;
    private Deck deck1;
    private Deck deck2;
    private Card card1;
    private Card card2;
    private Notification notification1;

    @BeforeEach
    public void setUp() {
        // Création d'un objet DeckCreator avec les propriétés adaptées
        deckCreator = new DeckCreator();
        deckCreator.setId(1L);
        deckCreator.setPseudo("TestPseudonym");
        deckCreator.setEmail("test@example.com");
        deckCreator.setPassword("password123");
        deckCreator.setAvatar("/uploads/avatar.png");
        deckCreator.setBio("Test Bio");
        deckCreator.setRoles(Arrays.asList(UserRole.USER));
        deckCreator.setDateSign(LocalDate.of(2025, 6, 4));
        deckCreator.setActivity(UserActivity.VIEWVER);
        deckCreator.setDecksLikedNumber(5L);
        deckCreator.setDecksNumber(3L);
        deckCreator.setEnabled(true);
        deckCreator.setSuspendedUntil(null);

        // Initialisation des decks
        deck1 = new Deck();
        deck1.setId(1L);
        deck1.setName("Test Deck 1");
        deck1.setDeckBuilder(deckCreator);
        deck1.setLikeNumber(10L);
        deck1.setIsPublic(true);
        deck1.setDeckBuilders(new HashSet<>()); // Vide pour éviter StackOverflow

        deck2 = new Deck();
        deck2.setId(2L);
        deck2.setName("Test Deck 2");
        deck2.setDeckBuilder(deckCreator);
        deck2.setLikeNumber(20L);
        deck2.setIsPublic(false);
        deck2.setDeckBuilders(new HashSet<>()); // Vide pour éviter StackOverflow

        // Initialisation des cartes
        card1 = new Card();
        card1.setId(1L);
        card1.setName("Test Card 1");

        card2 = new Card();
        card2.setId(2L);
        card2.setName("Test Card 2");

        // Initialisation de la notification
        notification1 = new Notification();
        notification1.setId(1L);
        notification1.setDeck(deck1);
        notification1.setIssuer(deckCreator);
        notification1.setReceivor(deckCreator);
        notification1.setDate(LocalDateTime.now());

        // Définir les associations
        deckCreator.setDecks(new HashSet<>(Arrays.asList(deck1, deck2)));
        deckCreator.setDecksLiked(Arrays.asList(deck1, deck2));
    }




    
    // ========== TESTS POUR loadUserByUsername ==========
    
    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        String email = "test@example.com";
        when(deckBuilderRepository.findByEmail(email)).thenReturn(Optional.of(deckCreator));
        
        // Act
        UserDetails result = deckBuilderService.loadUserByUsername(email);
        
        // Assert
        assertNotNull(result);
        assertEquals(deckCreator, result);
        verify(deckBuilderRepository).findByEmail(email);
    }
    
    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(deckBuilderRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            deckBuilderService.loadUserByUsername(email);
        });
        
        assertEquals("Cette adresse mail ne correpsond à aucun compte DeckBuilder", exception.getMessage());
        verify(deckBuilderRepository).findByEmail(email);
    }

    // ========== TESTS POUR getUserLikes ==========
    
    @Test
    void testGetUserLikes_UserHasLikes() {
        // Arrange
        when(deckRepository.findAll()).thenReturn(Arrays.asList(deck1, deck2));

        // Act
        Long totalLikes = deckBuilderService.getUserLikes(deckCreator);

        // Assert
        assertEquals(30L, totalLikes); // 10 + 20
        verify(deckRepository).findAll();
    }
    
    @Test
    void testGetUserLikes_UserHasDecksWithNoLikes() {
        // Arrange
        deck1.setLikeNumber(0L);
        deck2.setLikeNumber(0L);
        when(deckRepository.findAll()).thenReturn(Arrays.asList(deck1, deck2));

        // Act
        Long totalLikes = deckBuilderService.getUserLikes(deckCreator);

        // Assert
        assertEquals(0L, totalLikes);
        verify(deckRepository).findAll();
    }
    
    @Test
    void testGetUserLikes_UserHasNoDecks() {
        // Arrange
        when(deckRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Long totalLikes = deckBuilderService.getUserLikes(deckCreator);

        // Assert
        assertEquals(0L, totalLikes);
        verify(deckRepository).findAll();
    }

    @Test
    void testGetUserLikes_UserHasDecksFromOtherUsers() {
        // Arrange
        DeckCreator otherUser = new DeckCreator();
        otherUser.setId(2L);
        Deck otherDeck = new Deck();
        otherDeck.setDeckBuilder(otherUser);
        otherDeck.setLikeNumber(50L);
        
        when(deckRepository.findAll()).thenReturn(Arrays.asList(deck1, deck2, otherDeck));

        // Act
        Long totalLikes = deckBuilderService.getUserLikes(deckCreator);

        // Assert
        assertEquals(30L, totalLikes); // Seulement les likes des decks de deckCreator
        verify(deckRepository).findAll();
    }

    // ========== TESTS POUR getUserNotifs ==========
    
    @Test
    void testGetUserNotifs_Success() {
        // Arrange
        // On crée un Notification mock pour éviter les problèmes de hashCode()
        Notification mockNotif = mock(Notification.class);
        when(mockNotif.getId()).thenReturn(notification1.getId());
        
        // On crée des DeckCreator minimaux pour issuer et receivor
        DeckCreator issuer = new DeckCreator();
        issuer.setId(notification1.getIssuer().getId());
        issuer.setPseudo(notification1.getIssuer().getPseudo());

        DeckCreator receivor = new DeckCreator();
        receivor.setId(notification1.getReceivor().getId());
        receivor.setPseudo(notification1.getReceivor().getPseudo());

        when(mockNotif.getIssuer()).thenReturn(issuer);
        when(mockNotif.getReceivor()).thenReturn(receivor);
        when(mockNotif.getDeck()).thenReturn(notification1.getDeck());
        when(mockNotif.getDate()).thenReturn(notification1.getDate());

        Set<Notification> notifications = new HashSet<>(List.of(mockNotif));
        when(notificationRepository.findByReceivor(deckCreator)).thenReturn(notifications);

        // Act
        Set<GetNotification> result = deckBuilderService.getUserNotifs(deckCreator);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GetNotification getNotif = result.iterator().next();
        assertEquals(mockNotif.getId(), getNotif.getId());
        assertEquals(mockNotif.getDeck().getId(), getNotif.getDeckID());
        assertEquals(mockNotif.getIssuer().getId(), getNotif.getIssuerID());
        assertEquals(mockNotif.getReceivor().getId(), getNotif.getReceivorID());
        assertEquals(mockNotif.getDeck().getName(), getNotif.getDeckName());
        assertEquals(mockNotif.getIssuer().getPseudo(), getNotif.getIssuerPseudo());
        assertEquals(mockNotif.getReceivor().getPseudo(), getNotif.getReceivorPseudo());
        assertEquals(mockNotif.getDate(), getNotif.getDate());

        verify(notificationRepository).findByReceivor(deckCreator);
    }
    
    
    @Test
    void testGetUserNotifs_NoNotifications() {
        // Arrange
        when(notificationRepository.findByReceivor(deckCreator)).thenReturn(new HashSet<>());

        // Act
        Set<GetNotification> result = deckBuilderService.getUserNotifs(deckCreator);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository).findByReceivor(deckCreator);
    }

    // ========== TESTS POUR deleteUserNotif ==========

    @Test
    void testDeleteUserNotif_Success() {
        // Arrange
        Long notifId = 1L;
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification1));
        
        // Act
        String result = deckBuilderService.deleteUserNotif(notifId);
        
        // Assert
        assertEquals("Notification supprimée", result);
        verify(notificationRepository).findById(notifId);
        verify(notificationRepository).deleteById(notifId);
    }

    @Test
    void testDeleteUserNotif_NotFound() {
        // Arrange
        Long notifId = 999L;
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.deleteUserNotif(notifId);
        });
        
        assertEquals("Notification inexistante", exception.getMessage());
        verify(notificationRepository).findById(notifId);
        verify(notificationRepository, never()).deleteById(notifId);
    }

    // ========== TESTS POUR getDeckBuilder ==========

    @Test
    void testGetDeckBuilder_Success() {
        // Arrange
        when(deckBuilderRepository.findById(1L)).thenReturn(Optional.of(deckCreator));

        // Act
        GetDeckBuilder result = deckBuilderService.getDeckBuilder(deckCreator);

        // Assert
        assertNotNull(result);
        assertEquals(deckCreator.getId(), result.getId());
        assertEquals(deckCreator.getPseudo(), result.getPseudo());
        assertEquals(deckCreator.getEmail(), result.getEmail());
        assertEquals(deckCreator.getPassword(), result.getPassword());
        assertEquals(deckCreator.getAvatar(), result.getAvatar());
        assertEquals(deckCreator.getBio(), result.getBio());
        assertEquals(deckCreator.getRoles(), result.getRoles());
        assertEquals(deckCreator.getDateSign(), result.getDateSign());
        assertEquals(deckCreator.getActivity(), result.getActivity());
        assertEquals(deckCreator.getDecksLikedNumber(), result.getDecksLikedNumber());
        assertEquals(deckCreator.getDecksNumber(), result.getDecksNumber());

        verify(deckBuilderRepository).findById(1L);
    }

    @Test
    void testGetDeckBuilder_UserNotFound() {
        // Arrange
        when(deckBuilderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.getDeckBuilder(deckCreator);
        });

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(deckBuilderRepository).findById(1L);
    }

    // ========== TESTS POUR getNextDeckbuilder ==========

    @Test
    void testGetNextDeckbuilder_Success() {
        // Arrange
        Long currentUserId = 2L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act
        Long result = deckBuilderService.getNextDeckbuilder(currentUserId, userIds);
        
        // Assert
        assertEquals(3L, result);
    }

    @Test
    void testGetNextDeckbuilder_LastUser() {
        // Arrange
        Long currentUserId = 3L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.getNextDeckbuilder(currentUserId, userIds);
        });
        
        assertEquals("Aucun id suivant", exception.getMessage());
    }

    @Test
    void testGetNextDeckbuilder_UserNotInList() {
        // Arrange
        Long currentUserId = 5L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.getNextDeckbuilder(currentUserId, userIds);
        });
        
        assertEquals("Id incorrect", exception.getMessage());
    }

    // ========== TESTS POUR getPrevDeckbuilder ==========

    @Test
    void testGetPrevDeckbuilder_Success() {
        // Arrange
        Long currentUserId = 2L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act
        Long result = deckBuilderService.getPrevDeckbuilder(currentUserId, userIds);
        
        // Assert
        assertEquals(1L, result);
    }

    @Test
    void testGetPrevDeckbuilder_FirstUser() {
        // Arrange
        Long currentUserId = 1L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.getPrevDeckbuilder(currentUserId, userIds);
        });
        
        assertEquals("Aucun id précédent", exception.getMessage());
    }

    @Test
    void testGetPrevDeckbuilder_UserNotInList() {
        // Arrange
        Long currentUserId = 5L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.getPrevDeckbuilder(currentUserId, userIds);
        });
        
        assertEquals("Id incorrect", exception.getMessage());
    }

    // ========== TESTS POUR deleteDeckBuilder ==========

    @Test
    void testDeleteDeckBuilder_Success() {
        // Arrange
        // DeckCreator réel pour le test
        deckCreator = new DeckCreator();
        deckCreator.setId(1L);
        deckCreator.setPseudo("TestPseudonym");
        deckCreator.setAvatar("/uploads/avatar.png");

        // Mocks des Decks
        deck1 = mock(Deck.class);
        deck2 = mock(Deck.class);
        when(deck1.getId()).thenReturn(1L);
        when(deck2.getId()).thenReturn(2L);
        when(deck1.getDeckBuilders()).thenReturn(new HashSet<>());
        when(deck2.getDeckBuilders()).thenReturn(new HashSet<>());


        // Associer les decks et cartes au deckCreator
        deckCreator.setDecksLiked(Arrays.asList(deck1, deck2));
        deckCreator.setDecks(new HashSet<>(Arrays.asList(deck1, deck2)));

        // Mocks des repositories et services
        when(deckBuilderRepository.findById(deckCreator.getId())).thenReturn(Optional.of(deckCreator));
        when(deckRepository.findByDeckBuilder(deckCreator)).thenReturn(Arrays.asList(deck1, deck2));

        doNothing().when(deckService).deleteDeck(eq(deckCreator), anyLong());
        when(deckRepository.save(any(Deck.class))).thenReturn(deck1);
        when(cardRepository.save(any(Card.class))).thenReturn(card1);
        when(fileService.deleteImage(deckCreator.getAvatar())).thenReturn(true);

        // Act
        String result = deckBuilderService.deleteDeckBuilder(deckCreator.getId());

        // Assert
        assertEquals("Utilisateur supprimé", result);

        // Vérifications des appels
        verify(deckBuilderRepository).findById(deckCreator.getId());
        verify(deckRepository).findByDeckBuilder(deckCreator);
        verify(deckService, times(2)).deleteDeck(eq(deckCreator), anyLong());
        verify(deckRepository, times(2)).save(any(Deck.class));
        verify(fileService).deleteImage(deckCreator.getAvatar());
        verify(deckBuilderRepository).deleteById(deckCreator.getId());
    }




    // ========== TESTS POUR getDeckBuildersByFilterPaged ==========

    @Test
    void testGetDeckBuildersByFilterPaged_Success() {
        // Arrange
        int page = 0;
        int size = 10;
        String pseudo = "Test";
        String email = "test@example.com";
        List<UserActivity> activities = Arrays.asList(UserActivity.VIEWVER);
        
        DeckCreator deckBuilder1 = new DeckCreator();
        deckBuilder1.setId(1L);
        deckBuilder1.setPseudo("TestUser1");
        deckBuilder1.setEmail("test1@example.com");
        deckBuilder1.setActivity(UserActivity.VIEWVER);
        
        DeckCreator deckBuilder2 = new DeckCreator();
        deckBuilder2.setId(2L);
        deckBuilder2.setPseudo("TestUser2");
        deckBuilder2.setEmail("test2@example.com");
        deckBuilder2.setActivity(UserActivity.CREATOR);
        
        List<DeckCreator> filteredUsers = Arrays.asList(deckBuilder1, deckBuilder2);
        when(deckBuilderRepository.findByOptionalAttribute(pseudo, email, activities))
            .thenReturn(filteredUsers);
        
        // Act
        Page<GetDeckBuilder> result = deckBuilderService.getDeckBuildersByFilterPaged(page, size, pseudo, email, activities);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        
        GetDeckBuilder firstUser = result.getContent().get(0);
        assertEquals(deckBuilder1.getId(), firstUser.getId());
        assertEquals(deckBuilder1.getPseudo(), firstUser.getPseudo());
        assertEquals(deckBuilder1.getEmail(), firstUser.getEmail());
        assertEquals(deckBuilder1.getActivity(), firstUser.getActivity());
        
        verify(deckBuilderRepository).findByOptionalAttribute(pseudo, email, activities);
    }

    @Test
    void testGetDeckBuildersByFilterPaged_EmptyResult() {
        // Arrange
        int page = 0;
        int size = 10;
        String pseudo = "NonExistent";
        String email = "nonexistent@example.com";
        List<UserActivity> activities = Arrays.asList(UserActivity.BANNED);
        
        when(deckBuilderRepository.findByOptionalAttribute(pseudo, email, activities))
            .thenReturn(Collections.emptyList());
        
        // Act
        Page<GetDeckBuilder> result = deckBuilderService.getDeckBuildersByFilterPaged(page, size, pseudo, email, activities);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(deckBuilderRepository).findByOptionalAttribute(pseudo, email, activities);
    }

    @Test
    void testGetDeckBuildersByFilterPaged_Pagination() {
        // Arrange
        int page = 1;
        int size = 2;
        String pseudo = null;
        String email = null;
        List<UserActivity> activities = null;
        
        DeckCreator deckBuilder1 = new DeckCreator();
        deckBuilder1.setId(1L);
        deckBuilder1.setPseudo("User1");
        
        DeckCreator deckBuilder2 = new DeckCreator();
        deckBuilder2.setId(2L);
        deckBuilder2.setPseudo("User2");
        
        DeckCreator deckBuilder3 = new DeckCreator();
        deckBuilder3.setId(3L);
        deckBuilder3.setPseudo("User3");
        
        List<DeckCreator> allUsers = Arrays.asList(deckBuilder1, deckBuilder2, deckBuilder3);
        when(deckBuilderRepository.findByOptionalAttribute(pseudo, email, activities))
            .thenReturn(allUsers);
        
        // Act
        Page<GetDeckBuilder> result = deckBuilderService.getDeckBuildersByFilterPaged(page, size, pseudo, email, activities);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size()); // Seulement le 3ème utilisateur
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        
        assertEquals(3L, result.getContent().get(0).getId());
        
        verify(deckBuilderRepository).findByOptionalAttribute(pseudo, email, activities);
    }

    // ========== TESTS POUR activeAccount ==========

    @Test
    void testActiveAccount_Success_UserHasDecks() {
        // Arrange
        Long userId = 1L;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.of(deckCreator));
        when(deckRepository.findByDeckBuilder(deckCreator)).thenReturn(Arrays.asList(deck1, deck2));
        when(deckBuilderRepository.save(deckCreator)).thenReturn(deckCreator);
        
        // Act
        String result = deckBuilderService.activeAccount(userId);
        
        // Assert
        assertEquals("Compte activé", result);
        assertNull(deckCreator.getSuspendedUntil());
        assertTrue(deckCreator.isEnabled());
        assertEquals(UserActivity.CREATOR, deckCreator.getActivity());
        
        verify(deckBuilderRepository).findById(userId);
        verify(deckRepository).findByDeckBuilder(deckCreator);
        verify(deckBuilderRepository).save(deckCreator);
    }

    @Test
    void testActiveAccount_Success_UserHasNoDecks() {
        // Arrange
        Long userId = 1L;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.of(deckCreator));
        when(deckRepository.findByDeckBuilder(deckCreator)).thenReturn(Collections.emptyList());
        when(deckBuilderRepository.save(deckCreator)).thenReturn(deckCreator);
        
        // Act
        String result = deckBuilderService.activeAccount(userId);
        
        // Assert
        assertEquals("Compte activé", result);
        assertNull(deckCreator.getSuspendedUntil());
        assertTrue(deckCreator.isEnabled());
        assertEquals(UserActivity.VIEWVER, deckCreator.getActivity());
        
        verify(deckBuilderRepository).findById(userId);
        verify(deckRepository).findByDeckBuilder(deckCreator);
        verify(deckBuilderRepository).save(deckCreator);
    }
    
    @Test
    void testActiveAccount_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.activeAccount(userId);
        });
        
        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(deckBuilderRepository).findById(userId);
        verify(deckBuilderRepository, never()).save(any());
    }

    // ========== TESTS POUR suspendAccount ==========

    @Test
    void testSuspendAccount_Success() {
        // Arrange
        Long userId = 1L;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.of(deckCreator));
        when(deckRepository.findByDeckBuilder(deckCreator)).thenReturn(Arrays.asList(deck1, deck2));
        when(deckBuilderRepository.save(deckCreator)).thenReturn(deckCreator);
        when(deckRepository.save(any(Deck.class))).thenReturn(deck1);
        
        // Act
        String result = deckBuilderService.suspendAccount(userId);
        
        // Assert
        assertEquals("Compte desactivé", result);
        assertFalse(deckCreator.isEnabled());
        assertEquals(UserActivity.BANNED, deckCreator.getActivity());
        
        verify(deckBuilderRepository).findById(userId);
        verify(deckRepository).findByDeckBuilder(deckCreator);
        verify(deckRepository).save(deck1); // Le deck public est rendu privé
        verify(deckBuilderRepository).save(deckCreator);
    }
    
    @Test
    void testSuspendAccount_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.suspendAccount(userId);
        });
        
        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(deckBuilderRepository).findById(userId);
        verify(deckBuilderRepository, never()).save(any());
    }

    // ========== TESTS POUR suspendAccountTemporarily ==========

    @Test
    void testSuspendAccountTemporarily_Success() {
        // Arrange
        Long userId = 1L;
        int days = 7;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.of(deckCreator));
        when(deckBuilderRepository.save(deckCreator)).thenReturn(deckCreator);
        
        // Act
        String result = deckBuilderService.suspendAccountTemporarily(userId, days);
        
        // Assert
        assertTrue(result.startsWith("Compte suspendu temporairement jusqu'au"));
        assertFalse(deckCreator.isEnabled());
        assertEquals(UserActivity.BANNED, deckCreator.getActivity());
        assertNotNull(deckCreator.getSuspendedUntil());
        assertEquals(LocalDate.now().plusDays(days), deckCreator.getSuspendedUntil());
        
        verify(deckBuilderRepository).findById(userId);
        verify(deckBuilderRepository).save(deckCreator);
    }

    @Test
    void testSuspendAccountTemporarily_UserNotFound() {
        // Arrange
        Long userId = 999L;
        int days = 7;
        when(deckBuilderRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckBuilderService.suspendAccountTemporarily(userId, days);
        });
        
        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(deckBuilderRepository).findById(userId);
        verify(deckBuilderRepository, never()).save(any());
    }

    // ========== TESTS POUR getAllUserAvatars ==========
    
    @Test
    void testGetAllUserAvatars_Success() {
        // Arrange
        DeckCreator user1 = new DeckCreator();
        user1.setAvatar("/uploads/avatar1.jpg");
        
        DeckCreator user2 = new DeckCreator();
        user2.setAvatar("/uploads/avatar2.png");
        
        DeckCreator user3 = new DeckCreator();
        user3.setAvatar(null); // Pas d'avatar
        
        DeckCreator user4 = new DeckCreator();
        user4.setAvatar("external-avatar.jpg"); // Avatar externe
        
        List<DeckCreator> allUsers = Arrays.asList(user1, user2, user3, user4);
        when(deckBuilderRepository.findAll()).thenReturn(allUsers);
        
        // Act
        List<String> result = deckBuilderService.getAllUserAvatars();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Seulement les avatars qui commencent par /uploads/
        assertTrue(result.contains("/uploads/avatar1.jpg"));
        assertTrue(result.contains("/uploads/avatar2.png"));
        assertFalse(result.contains(null));
        assertFalse(result.contains("external-avatar.jpg"));
        
        verify(deckBuilderRepository).findAll();
    }

    @Test
    void testGetAllUserAvatars_NoUsers() {
        // Arrange
        when(deckBuilderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        List<String> result = deckBuilderService.getAllUserAvatars();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(deckBuilderRepository).findAll();
    }
    
    @Test
    void testGetAllUserAvatars_NoUploadAvatars() {
        // Arrange
        DeckCreator user1 = new DeckCreator();
        user1.setAvatar("external-avatar1.jpg");
        
        DeckCreator user2 = new DeckCreator();
        user2.setAvatar(null);
        
        List<DeckCreator> allUsers = Arrays.asList(user1, user2);
        when(deckBuilderRepository.findAll()).thenReturn(allUsers);
        
        // Act
        List<String> result = deckBuilderService.getAllUserAvatars();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(deckBuilderRepository).findAll();
    }
}
