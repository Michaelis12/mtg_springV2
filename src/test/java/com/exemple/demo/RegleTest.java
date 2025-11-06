package com.exemple.demo;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entitiesNoSQL.Regle;
import com.example.demo.repositories.RegleRepository;
import com.example.demo.services.RegleService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)  // Active Mockito avec JUnit 5
public class RegleTest {

    // Mock du repository
    @Mock
    private RegleRepository regleRepository;

    // Injecte le mock dans le service
    @InjectMocks
    private RegleService regleService;

    @BeforeEach
    public void setUp() {
        // Pas besoin d'initialiser les mocks avec MockitoExtension
    }

    @Test
    public void testAddRegle() {
        // Créer un objet "Regle" avec id, title, et text
        Regle regle = new Regle();
        regle.setId("1");
        regle.setTitle("Titre de la règle");
        regle.setText("Texte de la règle");

        // Simuler la méthode save du repository
        when(regleRepository.save(any(Regle.class))).thenReturn(regle);

        // Appeler la méthode du service
        Regle result = regleService.addRegle(regle);

        // Vérifier les résultats
        assertNotNull(result);
        assertEquals("1", result.getId());  // Vérification de l'id
        assertEquals("Titre de la règle", result.getTitle());  // Vérification du titre
        assertEquals("Texte de la règle", result.getText());  // Vérification du texte

        // Vérifier que save a été appelé une seule fois
        verify(regleRepository, times(1)).save(regle);
    }

    @Test
    public void testGetRegles() {
        // Créer des objets Regle simulés avec id, title, et text
        Regle regle1 = new Regle();
        regle1.setId("1");
        regle1.setTitle("Titre 1");
        regle1.setText("Texte de la règle 1");

        Regle regle2 = new Regle();
        regle2.setId("2");
        regle2.setTitle("Titre 2");
        regle2.setText("Texte de la règle 2");

        List<Regle> regles = Arrays.asList(regle1, regle2);

        // Simuler la méthode findAll du repository
        when(regleRepository.findAll()).thenReturn(regles);

        // Appeler la méthode du service
        List<Regle> result = regleService.getRegles();

        // Vérifier les résultats
        assertNotNull(result);
        assertEquals(2, result.size());  // Vérifier qu'il y a 2 éléments dans la liste
        assertEquals("Titre 1", result.get(0).getTitle());  // Vérifier le titre de la première règle
        assertEquals("Texte de la règle 1", result.get(0).getText());  // Vérifier le texte de la première règle
        assertEquals("Titre 2", result.get(1).getTitle());  // Vérifier le titre de la deuxième règle
        assertEquals("Texte de la règle 2", result.get(1).getText());  // Vérifier le texte de la deuxième règle

        // Vérifier que findAll a été appelé une seule fois
        verify(regleRepository, times(1)).findAll();
    }
    
    @Test
    public void deleteRegleTest_Success() {
        // Créer un objet Regle simulé avec id, title, et text
        String regleID = "1";
        Regle regle = new Regle();
        regle.setId(regleID);
        regle.setTitle("Titre de la règle");
        regle.setText("Texte de la règle");

        // Simuler la méthode findById du repository pour qu'elle renvoie un Optional avec la règle
        when(regleRepository.findById(regleID)).thenReturn(Optional.of(regle));

        // Appeler la méthode deleteRegle du service
        String result = regleService.deleteRegle(regleID);

        // Vérifier les résultats
        assertEquals("règle 1 supprimée", result);

        // Vérifier que deleteById a été appelé une seule fois
        verify(regleRepository, times(1)).deleteById(regleID);
    }

    @Test
    public void deleteRegleTest_Error() {
        // Créer un id de règle qui n'existe pas
        String regleID = "999";

        // Simuler la méthode findById du repository pour qu'elle renvoie un Optional vide
        when(regleRepository.findById(regleID)).thenReturn(Optional.empty());

        // Appeler la méthode deleteRegle du service et vérifier qu'une exception est levée
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            regleService.deleteRegle(regleID);
        });

        // Vérifier le message de l'exception
        assertEquals("Donnée non trouvée", exception.getMessage());

        // Vérifier que deleteById n'a pas été appelé
        verify(regleRepository, times(0)).deleteById(regleID);
    }
    
    @Test
    public void updateRegleTest_Success() {
        // ID de la règle à mettre à jour
        String regleID = "1";
        
        // Créer une règle existante
        Regle regleExistante = new Regle();
        regleExistante.setId(regleID);
        regleExistante.setTitle("Ancien Titre");
        regleExistante.setText("Ancien Texte");

        // Nouvelle valeur de titre et texte
        String nouveauTitle = "Nouveau Titre";
        String nouveauText = "Nouveau Texte";

        // Simuler la méthode findById du repository pour renvoyer la règle existante
        when(regleRepository.findById(regleID)).thenReturn(Optional.of(regleExistante));

        // Simuler la méthode save pour renvoyer la règle mise à jour
        when(regleRepository.save(any(Regle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Appeler la méthode updateRegle
        Regle result = regleService.updateRegle(regleID, nouveauTitle, nouveauText);

        // Vérifier les résultats
        assertNotNull(result);
        assertEquals(regleID, result.getId());
        assertEquals(nouveauTitle, result.getTitle());  // Vérifier le nouveau titre
        assertEquals(nouveauText, result.getText());    // Vérifier le nouveau texte

        // Vérifier que save a bien été appelé une seule fois
        verify(regleRepository, times(1)).save(regleExistante);
    }

    @Test
    public void updateRegleTest_Error() {
        // ID de la règle à mettre à jour
        String regleID = "999";

        // Simuler la méthode findById du repository pour renvoyer un Optional vide (règle non trouvée)
        when(regleRepository.findById(regleID)).thenReturn(Optional.empty());

        // Appeler la méthode updateRegle et vérifier qu'une exception est levée
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            regleService.updateRegle(regleID, "Nouveau Titre", "Nouveau Texte");
        });

        // Vérifier le message de l'exception
        assertEquals("Donnée non trouvée", exception.getMessage());

        // Vérifier que save n'a pas été appelé
        verify(regleRepository, times(0)).save(any(Regle.class));
    }

    @Test
    public void testUpdateRegle_PartielleMiseAJour() {
        // ID de la règle à mettre à jour
        String regleID = "1";
        
        // Créer une règle existante
        Regle regleExistante = new Regle();
        regleExistante.setId(regleID);
        regleExistante.setTitle("Ancien Titre");
        regleExistante.setText("Ancien Texte");

        // Simuler la méthode findById pour renvoyer la règle existante
        when(regleRepository.findById(regleID)).thenReturn(Optional.of(regleExistante));

        // Simuler la méthode save pour renvoyer la règle mise à jour
        when(regleRepository.save(any(Regle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Test 1: Appeler la méthode updateRegle avec seulement un titre à mettre à jour
        String nouveauTitle = "Nouveau Titre";
        Regle result = regleService.updateRegle(regleID, nouveauTitle, null);

        // Vérifier les résultats
        assertNotNull(result);
        assertEquals(regleID, result.getId());
        assertEquals(nouveauTitle, result.getTitle());  // Vérifier le nouveau titre
        assertEquals("Ancien Texte", result.getText());  // Le texte doit rester inchangé

    }

}


