package com.example.demo.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "card")
public class Card {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id", nullable = false)
    private String apiID;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column( columnDefinition = "LONGTEXT")
	private String text;
    
    @Column(name = "image", length = 2000)
    private String image;

    @Column(name = "mana_cost")
    private String manaCost;

    @Column(name = "cmc")
    private Double cmc;
    
    @Column(name = "rarity")
    private String rarity;

    @ElementCollection
    @CollectionTable(name = "card_colors", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "color")
    private Set<String> colors = new HashSet<>();


    @ElementCollection
    @CollectionTable(name = "card_types", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "type")
    private List<String> types = new ArrayList<>();
    
    
    @ElementCollection
    @CollectionTable(name = "card_formats", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "format")
    private List<String> formats = new ArrayList<>();
    
    
    @Column(name = "legendary")
    private boolean legendary;
    
    @Column(name = "edition")
    private String edition;


    @ManyToMany(mappedBy = "cards")
    private Set<Deck> decks = new HashSet<>();
    
    @Column(name = "decks_number") 
    private Long decksNumber;
    
    @Column(name = "cedh_number") 
    private Long cedhNumber;
    
}
