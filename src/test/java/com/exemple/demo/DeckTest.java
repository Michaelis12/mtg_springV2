package com.exemple.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.argThat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.type.EnumType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;

import com.example.demo.dto.GetCard;
import com.example.demo.dto.GetDeck;
import com.example.demo.entities.Card;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.entities.Notification;
import com.example.demo.enums.CardType;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumEdition;
import com.example.demo.enums.EnumFormat;
import com.example.demo.enums.UserActivity;
import com.example.demo.form.FormCedh;
import com.example.demo.form.FormDeck;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.DeckBuilderRepository;
import com.example.demo.repositories.DeckRepository;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.services.DeckService;
import com.example.demo.services.FileService;

@ExtendWith(MockitoExtension.class)
public class DeckTest {
	
    @Mock
    private CardRepository cardRepository;
    
    @Mock
    private DeckRepository deckRepository;
    
    @Mock
    private DeckBuilderRepository deckBuilderRepository;
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private FileService fileService;
    
    @InjectMocks
    private DeckService deckService;

    // Objets de base pour les tests
    private DeckCreator userMock;
    private Card mockCard;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userMock = new DeckCreator();
        userMock.setId(1L);
        userMock.setPseudo("TestUser");
        userMock.setDecksLiked(new ArrayList<>());
        userMock.setDecks(new HashSet<>());
        userMock.setDecksNumber(0L);
        
        mockCard = new Card();
        mockCard.setId(10L);
        mockCard.setApiID("card_api_10");
        mockCard.setName("Mock Card");
        mockCard.setCmc(2.0);
        mockCard.setTypes(new ArrayList<>(List.of("Creature")));
        mockCard.setColors(new HashSet<>(List.of("Red")));
    }
    
    // Helper pour créer un Deck mock
    private Deck createMockDeck(Long id, Long likes, boolean isPublic, String name, DeckCreator creator, EnumFormat format) {
        Deck deck = new Deck();
        deck.setId(id);
        deck.setName(name);
        deck.setLikeNumber(likes);
        deck.setIsPublic(isPublic);
        deck.setDeckBuilder(creator);
        deck.setFormat(format);
        deck.setDateCreation(LocalDate.now());
        deck.setCards(new ArrayList<>());
        deck.setColors(new HashSet<>());
        return deck;
    }


    // --- Tests pour addCardsOnDeck (Version du service fourni) ---

    @Test
    void testAddCardsOnDeck_Success_NewCard() {
        // Arrange
        Deck deck = createMockDeck(1L, 0L, true, "Test Deck", userMock, EnumFormat.standard);
        Card newCard = new Card();
        newCard.setApiID("new_api_id");
        newCard.setColors(new HashSet<>(List.of("Green"))); // Nouvelle carte sans couleur
        newCard.setCmc(3.0);
        
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(cardRepository.findByApiID("new_api_id")).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(newCard);

        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(3.0f).when(spyDeckService).getDeckManaCost(1L);
        
        // Act
        String result = spyDeckService.addCardsOnDeck(List.of(newCard), 1L);
        
        // Assert
        assertEquals("carte ajoutée", result);
        assertEquals(1, deck.getCards().size());
        assertTrue(deck.getCards().contains(newCard));
        assertEquals(1L, newCard.getDecksNumber()); // Doit être instancié à 1
        assertFalse(deck.getIsPublic());
        
        verify(cardRepository, times(1)).save(newCard);
        verify(deckRepository, times(1)).save(deck);
    }
    
    @Test
    void testAddCardsOnDeck_Success_ExistingCard_FirstTime() {
        // Arrange
        Deck deck = createMockDeck(1L, 0L, false, "Test Deck", userMock, EnumFormat.standard);
        Card existingCard = mockCard;
        existingCard.setDecksNumber(5L);
        
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(cardRepository.findByApiID(existingCard.getApiID())).thenReturn(Optional.of(existingCard));

        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(2.0f).when(spyDeckService).getDeckManaCost(1L);
        
        // Act
        spyDeckService.addCardsOnDeck(List.of(existingCard), 1L);
        
        // Assert
        assertEquals(6L, existingCard.getDecksNumber()); // DecksNumber doit être incrémenté
        verify(cardRepository, times(1)).save(existingCard);
    }
    
    @Test
    void testAddCardsOnDeck_MaximumCards() {
        // Arrange
        Deck deck = createMockDeck(1L, 0L, true, "Full Deck", userMock, EnumFormat.standard);
        deck.setCards(new ArrayList<>(Collections.nCopies(300, new Card()))); // 300 cartes

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.addCardsOnDeck(List.of(mockCard), 1L);
        });

        assertEquals("Nombre maximum autorisé atteint", exception.getMessage());
    }

    // --- Tests pour duplicateCardsOnDeck ---

    @Test
    void testDuplicateCardsOnDeck_Success() {
        // Arrange
        Long cardId = 10L;
        Long deckId = 1L;
        Deck deck = createMockDeck(deckId, 0L, true, "Test Deck", userMock, EnumFormat.standard);
        
        Card card = mockCard;
        card.setCmc(3.0);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        
        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(3.0f).when(spyDeckService).getDeckManaCost(deckId);

        // Act
        String result = spyDeckService.duplicateCardsOnDeck(List.of(cardId, cardId), deckId);
        
        // Assert
        assertEquals("cartes ajoutées", result);
        assertEquals(2, deck.getCards().size());
        assertEquals(3.0f, deck.getManaCost());
        assertFalse(deck.getIsPublic());
        verify(deckRepository, times(1)).save(deck);
    }
    
    @Test
    void testDuplicateCardsOnDeck_CardNotFound() {
        // Arrange
        Deck emptyDeck = new Deck();
        emptyDeck.setCards(new ArrayList<>()); 

        when(deckRepository.findById(1L)).thenReturn(Optional.of(emptyDeck));
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        // Le code du service devrait chercher la carte, ne pas la trouver,
        // puis lever l'exception "Carte non trouvée" avant d'atteindre le deckRepository.save
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            deckService.duplicateCardsOnDeck(List.of(999L), 1L));
        
        // On vérifie que la bonne exception est levée à cause de la carte non trouvée.
        assertEquals("Carte non trouvée", exception.getMessage());
        
        // On s'assure qu'aucun save n'a été appelé sur le deck, car l'exception est levée avant
        verify(deckRepository, never()).save(any()); 
    }

    // --- Tests pour getTop3Decks ---

    @Test
    void testGetTop3Decks() {
        // Arrange
        DeckCreator user1 = new DeckCreator(); user1.setPseudo("User1");
        DeckCreator user2 = new DeckCreator(); user2.setPseudo("User2");

        Deck deck1 = createMockDeck(1L, 100L, true, "Deck Rouge", user1, EnumFormat.standard);
        Deck deck2 = createMockDeck(2L, 50L, true, "Deck Bleu", user2, EnumFormat.standard);
        Deck deck3 = createMockDeck(3L, 75L, true, "Deck Vert", user1, EnumFormat.standard);
        Deck deck4 = createMockDeck(4L, 200L, false, "Deck Privé", user2, EnumFormat.standard); // Privé

        List<Deck> allDecks = new ArrayList<>(List.of(deck1, deck2, deck3, deck4));
        when(deckRepository.findAll()).thenReturn(allDecks);

        // Act
        List<GetDeck> result = deckService.getTop3Decks();

        // Assert
        assertEquals(3, result.size()); 
        assertEquals("Deck Rouge", result.get(0).getName()); // 100L
        assertEquals("Deck Vert", result.get(1).getName());  // 75L
        assertEquals("Deck Bleu", result.get(2).getName());  // 50L
    }

    // --- Tests pour les méthodes de filtrage paginé (Adapté) ---

    @Test
    void testGetDecksByFilterPaged_orderByLike() {
        // Arrange
        DeckCreator user = new DeckCreator(); user.setPseudo("testUser");
        
        Deck deck1 = createMockDeck(1L, 30L, true, "D2", user, EnumFormat.standard);
        Deck deck2 = createMockDeck(2L, 50L, true, "D1", user, EnumFormat.standard);
        Deck deck3 = createMockDeck(3L, 10L, true, "D3", user, EnumFormat.standard);
        
        List<Deck> filteredDecks = new ArrayList<>(List.of(deck1, deck2, deck3));

        when(deckRepository.findByAttributes(
                any(), any(), any(), eq(true), any(), any()
        )).thenReturn(filteredDecks);

        // Act: Page 0, size 2, order "like"
        Page<GetDeck> resultPage = deckService.getDecksByFilterPaged(
            0, 2, "like", null, null, null, null, null
        );

        // Assert
        assertEquals(2, resultPage.getContent().size()); 
        assertEquals(3, resultPage.getTotalElements());
        assertEquals("D1", resultPage.getContent().get(0).getName()); // 50 likes
        assertEquals("D2", resultPage.getContent().get(1).getName()); // 30 likes
    }

    @Test
    void testGetDecksUserPaged() {
        // Arrange
        DeckCreator user = userMock;

        Deck deck1 = createMockDeck(1L, 10L, true, "Public D1", user, EnumFormat.standard);
        Deck deck2 = createMockDeck(2L, 50L, true, "Public D2", user, EnumFormat.standard);
        Deck deck3 = createMockDeck(3L, 20L, false, "Private D3", user, EnumFormat.standard); // Ignoré
        
        List<Deck> allUserDecks = new ArrayList<>(List.of(deck1, deck2, deck3));
        
        when(deckRepository.findByDeckBuilder(user)).thenReturn(allUserDecks);

        // Act: order by "like", page 0, size 10
        Page<GetDeck> resultPage = deckService.getDecksUserPaged(user, 0, 10, "like");

        // Assert
        assertEquals(2, resultPage.getContent().size());
        assertEquals(2, resultPage.getTotalElements()); // Seuls les publics sont comptés
        assertEquals("Public D2", resultPage.getContent().get(0).getName()); // 50 likes
    }

    // --- Tests pour getDeckById ---

    @Test
    void testGetDeckById_success() {
        // Arrange
        Long deckId = 1L;
        DeckCreator creator = userMock;

        Deck deck = createMockDeck(deckId, 200L, true, "Deck Rouge", creator, EnumFormat.modern);
        deck.setColors(new HashSet<>(List.of(EnumColor.R)));
        deck.setDateCreation(LocalDate.of(2023, 5, 20));
        deck.setManaCost(5.0f);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        // Act
        GetDeck result = deckService.getDeckById(deckId);

        // Assertions
        assertEquals(deckId, result.getId());
        assertEquals("Deck Rouge", result.getName());
        assertEquals(EnumFormat.modern, result.getFormat());
        assertTrue(result.getColors().contains(EnumColor.R));
    }
    
    @Test
    void testGetDeckById_DeckNotFound() {
        // Arrange
        when(deckRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            deckService.getDeckById(99L));
        assertEquals("Deck non trouvé", exception.getMessage());
    }

    // --- Tests pour addDeck (Adapté) ---

    @Test
    void testAddDeck_success() {
        // Arrange
        FormDeck deckRegister = new FormDeck();
        deckRegister.setName("Nouveau Deck");
        deckRegister.setImage("image_url");
        deckRegister.setFormat(EnumFormat.modern);
        deckRegister.setColors(new HashSet<>(List.of(EnumColor.R, EnumColor.G)));
        
        // Simuler getNumberDecksByUser
        when(deckBuilderRepository.findAll()).thenReturn(List.of(userMock));
        when(deckRepository.findByDeckBuilder(userMock)).thenReturn(List.of()); 

        ArgumentCaptor<Deck> deckCaptor = ArgumentCaptor.forClass(Deck.class);
        doAnswer(invocation -> {
            Deck savedDeck = deckCaptor.getValue();
            savedDeck.setId(42L); 
            return savedDeck;
        }).when(deckRepository).save(deckCaptor.capture());

        // Act
        Long newDeckId = deckService.addDeck(userMock, deckRegister);

        // Assert
        assertEquals(42L, newDeckId);
        verify(deckRepository).save(any(Deck.class));
        
        Deck savedDeck = deckCaptor.getValue();
        assertEquals("Nouveau Deck", savedDeck.getName());
        assertTrue(savedDeck.getColors().containsAll(Set.of(EnumColor.R, EnumColor.G)));
    }
    
    @Test
    void testAddDeck_missingData_shouldThrowException() {
        // Arrange
        FormDeck formDeck = new FormDeck();
        formDeck.setName("Deck Incomplet");
        // Image est null (Données manquantes)

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.addDeck(userMock, formDeck);
        });

        assertEquals("Données manquantes", exception.getMessage());
        verify(deckRepository, never()).save(any());
    }

    // --- Tests pour addCedh (Adapté) ---

    @Test
    void testAddCedh_Success() {
        // Arrange
        FormCedh cedhRegister = new FormCedh();
        cedhRegister.setName("Deck cEDH");
        cedhRegister.setImage("image_url");
        cedhRegister.setFormat(EnumFormat.commander);
        cedhRegister.setColors(new HashSet<>(List.of(EnumColor.R, EnumColor.B)));
        
        Card commanderCard = new Card();
        commanderCard.setApiID("cmdr_api");
        commanderCard.setColors(new HashSet<>(List.of("Red", "Blue")));
        cedhRegister.setCommandant(commanderCard);
        
        // Simuler le commandant non existant en base
        when(cardRepository.findByApiID("cmdr_api")).thenReturn(Optional.empty()); 
        when(deckBuilderRepository.findAll()).thenReturn(List.of(userMock));
        when(deckRepository.findByDeckBuilder(userMock)).thenReturn(List.of());

        ArgumentCaptor<Deck> deckCaptor = ArgumentCaptor.forClass(Deck.class);
        doAnswer(invocation -> {
            Deck deckSaved = deckCaptor.getValue();
            deckSaved.setId(99L);
            return deckSaved;
        }).when(deckRepository).save(deckCaptor.capture());

        // Act
        Long returnedId = deckService.addCedh(userMock, cedhRegister);

        // Assert
        assertEquals(99L, returnedId);

        Deck savedDeck = deckCaptor.getValue();
        assertEquals("Deck cEDH", savedDeck.getName());
        assertEquals(EnumFormat.commander, savedDeck.getFormat());
        assertEquals(commanderCard, savedDeck.getCommander());
        assertEquals(1L, savedDeck.getCommander().getCedhNumber());
    }

    // --- Tests pour deleteDeck ---
    
    @Test
    void testDeleteDeck_success() {
        // Arrange
        Deck deck = createMockDeck(100L, 10L, false, "Deck to Delete", userMock, EnumFormat.standard);
        deck.setImage("/uploads/test.jpg");

        DeckCreator liker = new DeckCreator();
        liker.setId(2L);
        liker.setDecksLiked(new ArrayList<>(List.of(deck)));

        when(deckRepository.findById(100L)).thenReturn(Optional.of(deck));
        when(deckBuilderRepository.findAll()).thenReturn(List.of(userMock, liker));
        when(deckRepository.findByDeckBuilder(userMock)).thenReturn(List.of()); 

        // Act
        deckService.deleteDeck(userMock, 100L);

        // Assert
        verify(fileService).deleteImage("/uploads/test.jpg");
        verify(deckRepository).deleteById(100L);
        verify(deckBuilderRepository, times(2)).save(any(DeckCreator.class));
        assertEquals(0L, userMock.getDecksNumber());
    }

    // --- Tests pour updateDeck (Adapté pour FormDeck) ---
    
    @Test
    void testUpdateDeck_Success() {
        // Arrange
        Long deckId = 1L;
        Deck existingDeck = createMockDeck(deckId, 0L, true, "Old Name", userMock, EnumFormat.standard);
        existingDeck.setImage("oldImage.png");
        
        FormDeck deckUpdate = new FormDeck();
        deckUpdate.setName("New Name");
        deckUpdate.setImage("newImage.png");
        
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(existingDeck));

        // Act
        String result = deckService.updateDeck(deckId, deckUpdate);

        // Assertions
        assertEquals("deck modifié", result);
        verify(fileService).deleteOldImageIfUnused("oldImage.png", "newImage.png");
        verify(deckRepository).save(argThat(d -> 
            d.getName().equals("New Name") && 
            d.getImage().equals("newImage.png") &&
            !d.getIsPublic()
        ));
    }

    // --- Tests pour deleteCardOnDeck et deleteCardsOnDeck (Adapté) ---

    @Test
    void testDeleteCardOnDeck_Success_UpdatesDecksNumber() {
        // Arrange
        Long cardId = 10L;
        Long deckId = 1L;

        Card card = mockCard;
        card.setDecksNumber(5L);

        Deck deck = createMockDeck(deckId, 0L, true, "Test Deck", userMock, EnumFormat.standard);
        deck.setCards(new ArrayList<>(List.of(card))); // Un seul exemplaire de la carte
        deck.setDeckBuilder(userMock); // Nécessaire pour privateDeck

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(0.0f).when(spyDeckService).getDeckManaCost(deckId);
        doReturn("deck en privé").when(spyDeckService).privateDeck(any(), eq(deckId));

        // Act
        String result = spyDeckService.deleteCardOnDeck(cardId, deckId);

        // Assert
        assertEquals("Mock Card a été retiré du deck", result);
        assertEquals(0, deck.getCards().size());
        assertEquals(4L, card.getDecksNumber()); // Doit décrémenter
        verify(cardRepository).save(card);
        verify(deckRepository).save(deck);
    }
    
    @Test
    void testDeleteCardsOnDeck_List_Success() {
        // Arrange
        Long cardId1 = 10L;
        Long cardId2 = 11L;
        Long deckId = 1L;

        Card card1 = mockCard;
        Card card2 = new Card(); card2.setId(cardId2); card2.setName("Card 2"); card2.setCmc(1.0);
        
        // Simuler 1 exemplaire de chaque
        card1.setDecksNumber(1L);
        card2.setDecksNumber(1L);

        Deck deck = createMockDeck(deckId, 0L, true, "Test Deck", userMock, EnumFormat.standard);
        deck.setCards(new ArrayList<>(List.of(card1, card2))); 
        deck.setDeckBuilder(userMock);

        when(cardRepository.findById(cardId1)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(cardId2)).thenReturn(Optional.of(card2));
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(0.0f).when(spyDeckService).getDeckManaCost(deckId);
        doReturn("deck en privé").when(spyDeckService).privateDeck(any(), eq(deckId));

        // Act
        String result = spyDeckService.deleteCardsOnDeck(List.of(cardId1, cardId2), deckId);

        // Assert
        assertEquals("Mock Card, Card 2 ont été retirées du deck", result);
        assertTrue(deck.getCards().isEmpty());
        assertEquals(0L, card1.getDecksNumber()); 
        assertEquals(0L, card2.getDecksNumber()); 
    }

    // --- Tests pour setNumberCardOnDeck ---

    @Test
    void testSetNumberCardOnDeck_Increase() {
        // Arrange
        Long cardId = 10L;
        Long deckId = 1L;
        int targetNumber = 3;

        Card card = mockCard;

        Deck deck = createMockDeck(deckId, 0L, true, "Test Deck", userMock, EnumFormat.standard);
        deck.setCards(new ArrayList<>(List.of(card))); // 1 exemplaire actuellement

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        DeckService spyDeckService = Mockito.spy(deckService);
        doReturn(2.0f).when(spyDeckService).getDeckManaCost(deckId);

        // Act
        String result = spyDeckService.setNumberCardOnDeck(cardId, deckId, targetNumber);

        // Assert
        assertEquals("Mock Card a été retiré du deck", result);
        assertEquals(3, deck.getCards().size()); // 3 exemplaires maintenant
        assertFalse(deck.getIsPublic()); 
        verify(deckRepository).save(deck);
    }

    // --- Tests pour publishDeck ---

    @Test
    public void testPublishDeck_Success_CommanderFormat() {
        // Arrange
        Deck deck = createMockDeck(1L, 0L, false, "Commander Deck", userMock, EnumFormat.commander);
        for (int i = 0; i < 100; i++) deck.getCards().add(new Card()); // 100 cartes
        userMock.setDecksPublicNumber(0L);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        // Act
        String result = deckService.publishDeck(userMock, 1L);

        // Assert
        assertTrue(deck.getIsPublic());
        assertEquals(LocalDate.now(), deck.getDatePublication());
        assertEquals("Deck Commander Deck publié !", result);
        assertEquals(UserActivity.PUBLISHER, userMock.getActivity());
        assertEquals(1L, userMock.getDecksPublicNumber());
    }

    @Test
    public void testPublishDeck_ThrowsException_InsufficientCards() {
        // Arrange
        Deck deck = createMockDeck(1L, 0L, false, "Small Deck", userMock, EnumFormat.commander);
        // 0 cartes (Moins de 100)
        
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.publishDeck(userMock, 1L);
        });

        assertEquals("Ce deck ne contient pas suffisament de cartes pour etre jouable dans ce format", exception.getMessage());
    }
    
    // --- Tests pour privateDeck ---

    @Test
    void testPrivateDeck_Success() {
        // Arrange
        Long deckId = 1L;
        Deck deck = createMockDeck(deckId, 10L, true, "Mon Deck", userMock, EnumFormat.standard);
        deck.setDatePublication(LocalDate.now());

        DeckCreator otherUser = new DeckCreator();
        otherUser.setId(2L);
        otherUser.setDecksLiked(new ArrayList<>(List.of(deck)));
        otherUser.setDecksPublicNumber(1L);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(deckBuilderRepository.findAll()).thenReturn(List.of(userMock, otherUser));

        // Act
        String result = deckService.privateDeck(userMock, deckId);

        // Assert
        assertEquals(" deck Mon Deck en privé", result);
        assertFalse(deck.getIsPublic());
        assertNull(deck.getDatePublication());
        
        // Le compteur public de l'autre utilisateur est mis à jour
        assertEquals(0L, otherUser.getDecksPublicNumber()); 
        verify(deckBuilderRepository, times(1)).save(otherUser);
    }
    
    // --- Tests pour getDeckManaCost (Adapté) ---

    @Test
    public void testGetDeckManaCost_Success() {
        Deck deck = new Deck();
        List<Card> cards = new ArrayList<>();

        Card card1 = new Card();
        card1.setTypes(Arrays.asList("Sorcery"));
        card1.setCmc(2.0);
        cards.add(card1);

        Card card2 = new Card();
        card2.setTypes(Arrays.asList("Creature"));
        card2.setCmc(4.0);
        cards.add(card2);

        Card terrainCard = new Card();
        terrainCard.setTypes(Arrays.asList("Land")); // Ignoré
        terrainCard.setCmc(10.0); 
        cards.add(terrainCard);

        deck.setCards(cards);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        Float manaCost = deckService.getDeckManaCost(1L);

        assertEquals(3.0f, manaCost, 0.01f); // (2.0 + 4.0) / 2 = 3.0
    }
    
    @Test
    void testGetDeckManaCost_ShouldReturnZeroIfOnlyLands() {
        // Arrange
        Deck deck = new Deck();
        deck.setId(1L);
        
        // 💡 CORRECTION: Initialisation de la liste des cartes
        deck.setCards(new ArrayList<>()); 

        Card land1 = new Card();
        land1.setCmc(0.0);
        land1.setTypes(Arrays.asList("Land"));
        
        // L'appel addAll() fonctionne maintenant car deck.getCards() n'est plus null
        deck.getCards().addAll(Arrays.asList(land1));
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        
        // Act
        Float result = deckService.getDeckManaCost(1L);

        // Assert
        assertEquals(0.0f, result); 
    }

    
    // --- Autres méthodes (Simples et adaptées) ---
    
    @Test
    void testGetDeckUser_success() {
        // Arrange
        Deck deck = new Deck();
        deck.setDeckBuilder(userMock);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        // Act
        Long result = deckService.getDeckUser(1L);

        // Assert
        assertEquals(userMock.getId(), result);
    }
    
    @Test
    void testGetNextDeck() {
        // Arrange
        List<Long> deckIds = new ArrayList<>(List.of(10L, 20L, 30L));
        // Act
        Long result = deckService.getNextDeck(20L, deckIds);
        // Assert
        assertEquals(30L, result);
    }

    @Test
    void testGetPrevDeck() {
        // Arrange
        List<Long> deckIds = new ArrayList<>(List.of(10L, 20L, 30L));
        // Act
        Long result = deckService.getPrevDeck(20L, deckIds);
        // Assert
        assertEquals(10L, result);
    }
    
    @Test
    void testGetCardsOnDeck_success() {
        // Arrange (Adapté à Set<String> pour Card colors)
        Long deckId = 1L;
        Card card = mockCard;
        card.setApiID("api_10");
        card.setManaCost("2R");
        card.setCmc(3.0);
        card.setDecksNumber(5L);
        card.setColors(new HashSet<>(List.of("Red"))); 
        card.setTypes(new ArrayList<>(List.of("Instant", "Spell"))); 
        
        Deck deck = new Deck();
        deck.setId(deckId);
        deck.setCards(new ArrayList<>(List.of(card)));

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        // Act
        List<GetCard> result = deckService.getCardsOnDeck(deckId);

        // Assert
        assertEquals(1, result.size());
        GetCard returnedCard = result.get(0);

        assertEquals("api_10", returnedCard.getApiID());
        assertEquals(3.0, returnedCard.getCmc());
        assertTrue(returnedCard.getColors().contains("Red"));
        assertTrue(returnedCard.getTypes().contains("Instant"));
    }
    
    @Test
    void testGet7CardsOnDeck() {
        // Arrange
        Long deckId = 1L;
        Deck deck = new Deck();
        deck.setId(deckId);

        List<Card> cards = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Card card = new Card();
            card.setId((long) i);
            cards.add(card);
        }
        deck.setCards(cards);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        // Act
        List<GetCard> result = deckService.get7CardsOnDeck(deckId);

        // Assert
        assertEquals(7, result.size()); 
    }
    
    @Test
    void testGetCommandantOndeck_success() {
        // Arrange
        Long deckId = 1L;
        Card commander = mockCard;
        
        Deck deck = new Deck();
        deck.setId(deckId);
        deck.setCommander(commander);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        // Act
        GetCard result = deckService.getCommandantOndeck(deckId);

        // Assert
        assertEquals(mockCard.getId(), result.getId());
        assertEquals(mockCard.getName(), result.getName());
    }
    
    @Test
    void testGetNumberDecksByUser_success() {
        // Arrange
        DeckCreator user = userMock;

        Deck deck1 = new Deck(); deck1.setId(1L);
        Deck deck2 = new Deck(); deck2.setId(2L);

        when(deckBuilderRepository.findAll()).thenReturn(List.of(user));
        when(deckRepository.findByDeckBuilder(user)).thenReturn(new ArrayList<>(List.of(deck1, deck2)));

        // Act
        int result = deckService.getNumberDecksByUser(user);

        // Assert
        assertEquals(2, result);
    }
    
    @Test
    void testGetAllDeckImages() {
        // Arrange
        Deck deck1 = new Deck(); deck1.setImage("/uploads/deck1.jpg");
        Deck deck2 = new Deck(); deck2.setImage("external-image.jpg"); 
        Deck deck3 = new Deck(); deck3.setImage("/uploads/deck2.jpg"); 

        List<Deck> allDecks = new ArrayList<>(List.of(deck1, deck2, deck3));
        when(deckRepository.findAll()).thenReturn(allDecks);

        // Act
        List<String> result = deckService.getAllDeckImages();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("/uploads/deck1.jpg"));
        assertTrue(result.contains("/uploads/deck2.jpg"));
    }
}
