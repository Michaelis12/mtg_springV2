package com.example.demo.repositories;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.entities.Deck;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
	
	Optional<Deck> findByName(String name);
	List<Deck> findByNameContaining(String nom);
	List<Deck> findByFormat(EnumFormat format);
	List<Deck> findByColors (EnumColor color);
	List<Deck> findByDeckBuilder(DeckCreator user);

	// Récupère les decks publics
	// Récupère les decks publics avec filtres dynamiques
	@Query("""
		    SELECT DISTINCT d FROM Deck d
		    LEFT JOIN d.colors color
		    WHERE (:name IS NULL OR d.name LIKE %:name%)
		    AND (:manaCostMin IS NULL OR d.manaCost >= :manaCostMin)
		    AND (:manaCostMax IS NULL OR d.manaCost <= :manaCostMax)
		    AND (:isPublic IS NULL OR d.isPublic = :isPublic)
		    AND (:formats IS NULL OR d.format IN :formats)
			AND (
	        :colors IS NULL 
	        OR NOT EXISTS (
	            SELECT c FROM Deck d2 JOIN d2.colors c 
	            WHERE d2 = d AND c NOT IN :colors
	        ))
		    """)
		List<Deck> findByAttributes(
		    @Param("name") String name,
		    @Param("manaCostMin") Float manaCostMin,
		    @Param("manaCostMax") Float manaCostMax,
		    @Param("isPublic") Boolean isPublic,
		    @Param("formats") List<EnumFormat> formats,
		    @Param("colors") List<EnumColor> colors
		);
	
	



	
	// Récupère les decks créés 
	@Query("""
		    SELECT DISTINCT d FROM Deck d
		    LEFT JOIN d.colors color
		    WHERE (:deckCreator IS NULL OR d.deckBuilder = :deckCreator)
		    AND (:name IS NULL OR d.name LIKE %:name%)
		    AND (:manaCostMin IS NULL OR d.manaCost >= :manaCostMin)
		    AND (:manaCostMax IS NULL OR d.manaCost <= :manaCostMax)
		    AND (:formats IS NULL OR d.format IN :formats)
		    AND (
	        :colors IS NULL 
	        OR NOT EXISTS (
	            SELECT c FROM Deck d2 JOIN d2.colors c 
	            WHERE d2 = d AND c NOT IN :colors
	        ))
		    """)
		List<Deck> findByAttributeCreate(
		    @Param("deckCreator") DeckCreator deckCreator,
		    @Param("name") String name,
		    @Param("manaCostMin") Float manaCostMin,
		    @Param("manaCostMax") Float manaCostMax,
		    @Param("formats") List<EnumFormat> formats,
		    @Param("colors") List<EnumColor> colors
		);

			


	
	// Récupère les decks likés
	@Query("""
		    SELECT DISTINCT d FROM Deck d
		    LEFT JOIN d.colors color
		    JOIN d.deckBuilders likedBy
		    WHERE (:deckCreator IS NULL OR likedBy = :deckCreator)
		    AND (:name IS NULL OR d.name LIKE %:name%)
		    AND (:manaCostMin IS NULL OR d.manaCost >= :manaCostMin)
		    AND (:manaCostMax IS NULL OR d.manaCost <= :manaCostMax)
		    AND (:isPublic IS NULL OR d.isPublic = :isPublic)
		    AND (:formats IS NULL OR d.format IN :formats)
		    AND (
	        :colors IS NULL 
	        OR NOT EXISTS (
	            SELECT c FROM Deck d2 JOIN d2.colors c 
	            WHERE d2 = d AND c NOT IN :colors
	        ))
		    """)
		List<Deck> findByAttributeLiked(
		    @Param("deckCreator") DeckCreator deckCreator,
		    @Param("name") String name,
		    @Param("manaCostMin") Float manaCostMin,
		    @Param("manaCostMax") Float manaCostMax,
		    @Param("isPublic") Boolean isPublic,
		    @Param("formats") List<EnumFormat> formats,
		    @Param("colors") List<EnumColor> colors
		);

	


}
