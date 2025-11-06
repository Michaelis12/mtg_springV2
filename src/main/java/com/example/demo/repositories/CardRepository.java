package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.entities.Card;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;


@Repository
public interface CardRepository extends JpaRepository<Card, Long> {	
	
	
	List<Card> findAll();
	Optional<Card> findByApiID(String apiID);

	
	@Query("""
		    SELECT c FROM Card c
		    LEFT JOIN c.colors color
		    LEFT JOIN c.formats format
		    WHERE (:name IS NULL OR c.name LIKE %:name%)
		    AND (:text IS NULL OR c.text LIKE %:text%)
		    AND (:cmcMin IS NULL OR c.cmc >= :cmcMin)
		    AND (:cmcMax IS NULL OR c.cmc <= :cmcMax)
		    AND (:types IS NULL OR EXISTS (
		        SELECT t FROM c.types t WHERE t IN :types
		    ))
		    AND (:legendary IS NULL OR c.legendary = :legendary)
		    AND (:rarities IS NULL OR c.rarity IN :rarities)
		    AND (:formats IS NULL OR format IN :formats)
		     AND (:editions IS NULL OR c.edition IN :editions)
		    AND (:colors IS NULL OR NOT EXISTS (
		        SELECT col FROM c.colors col WHERE col NOT IN :colors
		    ))
		""")
		List<Card> findByAttributes(
		        @Param("name") String name,
		        @Param("text") String text,
		        @Param("cmcMin") Double cmcMin,
		        @Param("cmcMax") Double cmcMax,
		        @Param("types") List<String> types,
		        @Param("legendary") Boolean legendary,
		        @Param("rarities") List<String> rarities,
		        @Param("colors") List<String> colors,
		        @Param("formats") List<String> formats,
		        @Param("editions") List<String> editions
		);


	
/*	
	@Query("""
		    SELECT DISTINCT c FROM Card c
		    LEFT JOIN c.colors color
		    LEFT JOIN c.formats format
		    JOIN c.deckBuilders deckCreator
		    WHERE (:deckCreator IS NULL OR deckCreator = :deckCreator)
		    AND (:name IS NULL OR c.name LIKE %:name%)
		    AND (:text IS NULL OR c.text LIKE %:text%)
		    AND (:manaCostMin IS NULL OR c.manaCost >= :manaCostMin)
		    AND (:manaCostMax IS NULL OR c.manaCost <= :manaCostMax)
		    AND (:valueMin IS NULL OR c.value >= :valueMin)
		    AND (:valueMax IS NULL OR c.value <= :valueMax)
		    AND (:types IS NULL OR c.type IN :types)
		    AND (:legendary IS NULL OR c.legendary IN :legendary)
		    AND (:rarities IS NULL OR c.rarity IN :rarities)
		    AND (:editions IS NULL OR c.edition IN :editions)
		    AND (:formats IS NULL OR format IN :formats)
		    AND (:colors IS NULL OR NOT EXISTS (
		        SELECT col FROM c.colors col WHERE col NOT IN :colors
		    ))
		    """)
		List<Card> findByAttributeLikedByUser(
		        @Param("deckCreator") DeckCreator deckCreator,
		        @Param("name") String name,
		        @Param("text") String text,
		        @Param("manaCostMin") Long manaCostMin,
		        @Param("manaCostMax") Long manaCostMax,
		        @Param("valueMin") Float valueMin,
		        @Param("valueMax") Float valueMax,
		        @Param("types") List<CardType> types,
		        @Param("legendary") String legendary,
		        @Param("rarities") List<EnumRarity> rarities,
		        @Param("editions") List<EnumEdition> editions,
		        @Param("colors") List<Color> colors,
		        @Param("formats") List<Format> formats
		);

	
	


	@Query("SELECT c FROM Card c WHERE (:lastID IS NULL OR c.id > :lastID)"
    + " AND (:name IS NULL OR c.name LIKE %:name%)"
    + " AND (:text IS NULL OR c.text LIKE %:text%)"
    + " AND (:manaCostMin IS NULL OR c.manaCost >= :manaCostMin)"
    + " AND (:manaCostMax IS NULL OR c.manaCost <= :manaCostMax)"
    + " AND (:valueMin IS NULL OR c.value >= :valueMin)"
    + " AND (:valueMax IS NULL OR c.value <= :valueMax)"
    // Ajoute ici les autres filtres si besoin
    + " ORDER BY c.id ASC")
	
	
	List<Card> findNextNByFilterAfterId(
    @Param("lastID") Long lastID,
    Pageable pageable,
    @Param("name") String name,
    @Param("text") String text,
    @Param("manaCostMin") Long manaCostMin,
    @Param("manaCostMax") Long manaCostMax,
    @Param("valueMin") Float valueMin,
    @Param("valueMax") Float valueMax
    // Ajoute ici les autres paramètres si besoin
);
*/
}
