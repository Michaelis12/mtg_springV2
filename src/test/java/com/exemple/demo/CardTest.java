package com.exemple.demo;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.example.demo.dto.GetCard;
import com.example.demo.entities.Card;
import com.example.demo.entitiesNoSQL.Regle;
import com.example.demo.enums.EnumColor;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.RegleRepository;
import com.example.demo.services.CardService;
import com.example.demo.services.RegleService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CardTest {

    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;

    // Objets de données de test (mocks de données)
    private Card card1;
    private Card card2;
    private Card card3;
    private Card card4;
    private List<Card> allCards;



    @BeforeEach
    void setUp() {
        // Initialisation de cartes pour les tests de classement et de filtre

        // Carte 1: Top Deck, Top cEDH
        card1 = Card.builder()
                .id(1L).apiID("API1").name("Card A").image("/uploads/a.jpg")
                .decksNumber(100L).cedhNumber(50L).cmc(3.0).legendary(true)
                .build();

        // Carte 2: Moins utilisée en Deck, Top cEDH
        card2 = Card.builder()
                .id(2L).apiID("API2").name("Card B").image("/uploads/b.jpg")
                .decksNumber(50L).cedhNumber(100L).cmc(4.0).legendary(false)
                .build();

        // Carte 3: Bas utilisée en Deck/cEDH
        card3 = Card.builder()
                .id(3L).apiID("API3").name("Card C").image("/uploads/c.jpg")
                .decksNumber(10L).cedhNumber(5L).cmc(2.0).legendary(false)
                .build();
                
        // Carte 4: Utilisée uniquement en Deck
        card4 = Card.builder()
                .id(4L).apiID("API4").name("Card D").image("external.jpg")
                .decksNumber(200L).cedhNumber(null).cmc(5.0).legendary(false)
                .build();
        
        allCards = Arrays.asList(card1, card2, card3, card4);
    }

    // --- Tests pour getAllCardImages ---

    @Test
    void testGetAllCardImages_ShouldFilterUploads() {
        // Arrange
        Card cardExternal = Card.builder().id(5L).image("https://external.com/img.jpg").build();
        Card cardNull = Card.builder().id(6L).image(null).build();
        
        List<Card> cardsWithMixedImages = Arrays.asList(card1, card4, cardExternal, cardNull);
        when(cardRepository.findAll()).thenReturn(cardsWithMixedImages);

        // Act
        List<String> result = cardService.getAllCardImages();

        // Assert
        assertEquals(1, result.size()); // Seul card1 est un '/uploads/' valide dans la liste
        assertTrue(result.contains("/uploads/a.jpg"));
        assertFalse(result.contains("external.jpg"));
    }
    
    @Test
    void testGetAllCardImages_ShouldReturnEmptyListIfNoImages() {
        // Arrange
        when(cardRepository.findAll()).thenReturn(List.of(
            Card.builder().id(1L).image("http://test.com/img.jpg").build(),
            Card.builder().id(2L).image(null).build()
        ));

        // Act
        List<String> result = cardService.getAllCardImages();

        // Assert
        assertTrue(result.isEmpty());
    }

    // --- Tests pour getTop3Cards (basé sur decksNumber) ---

    @Test
    void testGetTop3Cards_ShouldReturnTop3ByDecksNumber() {
        // Arrange
        when(cardRepository.findAll()).thenReturn(allCards);

        // Act
        List<GetCard> result = cardService.getTop3Cards();

        // Assert
        assertEquals(3, result.size());
        // Ordre attendu: Card D (200L), Card A (100L), Card B (50L)
        assertEquals("Card D", result.get(0).getName());
        assertEquals(200L, result.get(0).getDecksNumber());
        assertEquals("Card A", result.get(1).getName());
        assertEquals(100L, result.get(1).getDecksNumber());
        assertEquals("Card B", result.get(2).getName());
        assertEquals(50L, result.get(2).getDecksNumber());
        
        // Card C (10L) est ignorée
    }
    
    @Test
    void testGetTop3Cards_ShouldFilterOutNullDecksNumber() {
        // Arrange
        Card cardNullDeckNumber = Card.builder().id(5L).name("Card E").decksNumber(null).build();
        List<Card> cards = new ArrayList<>(Arrays.asList(card1, card2, cardNullDeckNumber));
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        List<GetCard> result = cardService.getTop3Cards();

        // Assert
        assertEquals(2, result.size()); // Card E est filtrée
        assertTrue(result.stream().noneMatch(c -> c.getName().equals("Card E")));
        
    }

    // --- Tests pour getTop3Cedh (basé sur cedhNumber) ---

    @Test
    void testGetTop3Cedh_ShouldReturnTop3ByCedhNumber() {
        // Arrange
        // Mocking avec une liste mutable (allCards est déjà mutable grâce à setUp)
    	when(cardRepository.findAll()).thenReturn(new ArrayList<>(allCards));

        // Act
        List<GetCard> result = cardService.getTop3Cedh();

        // Assert
        assertEquals(3, result.size());
        // Ordre attendu: Card B (100L), Card A (50L), Card C (5L)
        assertEquals("Card B", result.get(0).getName());
        assertEquals(100L, result.get(0).getCedhNumber());
        assertEquals("Card A", result.get(1).getName());
        assertEquals(50L, result.get(1).getCedhNumber());
        assertEquals("Card C", result.get(2).getName());
        assertEquals(5L, result.get(2).getCedhNumber());
    }
    
    @Test
    void testGetTop3Cedh_ShouldFilterOutNullCedhNumber() {
        // Arrange
        Card cardNullCedhNumber = Card.builder().id(5L).name("Card E").cedhNumber(null).build();
        List<Card> cards = new ArrayList<>(Arrays.asList(card1, card2, cardNullCedhNumber));
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        List<GetCard> result = cardService.getTop3Cedh();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(c -> c.getName().equals("Card E")));
    }


    // --- Tests pour getAllCardsRanked ---

    @Test
    void testGetAllCardsRanked_ByDeck() {
        // Arrange
        when(cardRepository.findAll()).thenReturn(allCards);

        // Act
        List<Long> result = cardService.getAllCardsRanked("deck");

        // Assert
        assertEquals(4, result.size());
        // Ordre attendu: Card D (200L), Card A (100L), Card B (50L), Card C (10L)
        assertEquals(4L, result.get(0));
        assertEquals(1L, result.get(1));
        assertEquals(2L, result.get(2));
        assertEquals(3L, result.get(3));
    }
    
    @Test
    void testGetAllCardsRanked_ByCedh() {
        // Arrange
        when(cardRepository.findAll()).thenReturn(allCards);

        // Act
        List<Long> result = cardService.getAllCardsRanked("cedh");

        // Assert
        assertEquals(3, result.size());
        // Ordre attendu: Card B (100L), Card A (50L), Card C (5L) - Card D est null et filtrée
        assertEquals(2L, result.get(0));
        assertEquals(1L, result.get(1));
        assertEquals(3L, result.get(2));
    }
    
    @Test
    void testGetAllCardsRanked_NoOrderSpecified() {
        // Arrange
        // Si l'ordre n'est ni "deck" ni "cedh", la liste retournée est celle de findAll()
        // et n'est pas triée par le service.
        when(cardRepository.findAll()).thenReturn(allCards);

        // Act
        List<Long> result = cardService.getAllCardsRanked("none");

        // Assert
        // Vérifie juste que tous les IDs sont présents dans l'ordre de findAll()
        assertEquals(4, result.size());
        assertEquals(1L, result.get(0)); 
    }

    // --- Tests pour getCardsByFilterPaged ---

    @Test
    void testGetCardsByFilterPaged_ShouldReturnPagedResultsAndRankByCedh() {
        // Arrange
        List<Card> filteredList = new ArrayList<>(Arrays.asList(card1, card2, card3)); // 100, 50, 10
        when(cardRepository.findByAttributes(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
              .thenReturn(filteredList);

        // Act: Page 0, size 2, order "cedh"
        Page<GetCard> resultPage = cardService.getCardsByFilterPaged(
            0, 2, "cedh", null, null, null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals(2, resultPage.getContent().size());
        assertEquals(3, resultPage.getTotalElements());
        assertEquals(0, resultPage.getNumber());

        // Ordre attendu par cEDH: Card B (100L), Card A (50L)
        assertEquals("Card B", resultPage.getContent().get(0).getName());
        assertEquals(100L, resultPage.getContent().get(0).getCedhNumber());
        assertEquals("Card A", resultPage.getContent().get(1).getName());
    }
    
    @Test
    void testGetCardsByFilterPaged_ShouldReturnEmptyPageIfStartIsOutOfBounds() {
        // Arrange
        List<Card> filteredList = new ArrayList<>(Arrays.asList(card1, card2, card3));
        when(cardRepository.findByAttributes(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
              .thenReturn(filteredList);

        // Act: Page 10, size 5 (bien au-delà des 3 cartes)
        Page<GetCard> resultPage = cardService.getCardsByFilterPaged(
            10, 5, "deck", null, null, null, null, null, null, null, null, null, null
        );

        // Assert
        assertTrue(resultPage.getContent().isEmpty());
        assertEquals(3, resultPage.getTotalElements()); // Doit retourner le total correct
    }
    
    @Test
    void testGetCardsByFilterPaged_ShouldMapAllFields() {
        // Arrange
        Card fullCard = Card.builder()
                .id(99L).apiID("API99").name("Full Card").text("Some text")
                .image("/uploads/full.jpg").manaCost("{2}UR").cmc(4.0)
                .decksNumber(1L).cedhNumber(1L).legendary(true)
                .rarity("Mythic").edition("ZNR") // ajout des champs manquants
                .types(List.of("Creature")).colors(Set.of("Blue", "Red"))
                .formats(List.of("standard")).build();
                
        // Mocking du repository pour retourner la carte complète
        when(cardRepository.findByAttributes(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
              .thenReturn(List.of(fullCard));

        // Act
        Page<GetCard> resultPage = cardService.getCardsByFilterPaged(
            0, 1, "none", null, null, null, null, null, null, null, null, null, null
        );

        // Assert
        GetCard result = resultPage.getContent().get(0);
        
        // Champs simples
        assertEquals(99L, result.getId());
        assertEquals("Full Card", result.getName());
        assertEquals("API99", result.getApiID());
        assertEquals("Some text", result.getText());
        assertEquals("{2}UR", result.getManaCost());
        assertEquals(4.0, result.getCmc());
        assertEquals(true, result.isLegendary());
        assertEquals(1L, result.getCedhNumber());
        assertEquals(1L, result.getDecksNumber());

        // Champs Collection/Set (Couleurs)
        assertEquals(2, result.getColors().size());
        assertTrue(result.getColors().contains("Blue"));
        assertTrue(result.getColors().contains("Red"));

        // Champs Collection (Types et Formats)
        assertEquals(1, result.getTypes().size());
        assertTrue(result.getTypes().contains("Creature"));
        assertEquals(1, result.getFormats().size());
        assertTrue(result.getFormats().contains("standard"));
    }
}

