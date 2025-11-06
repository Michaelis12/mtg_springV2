package com.example.demo.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deck")
@Builder
@Entity
public class Deck {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(name = "name", length = 25, nullable = false, unique = false)
	private String name;
	
	@Column(name = "date_creation")
	@Temporal(TemporalType.DATE)
	private LocalDate dateCreation;
	
	
	@Column( columnDefinition = "LONGTEXT")
	private String image;
	
	@Enumerated(EnumType.STRING)
	private EnumFormat format;
	
	@ElementCollection
    @CollectionTable(name = "deck_colors", joinColumns = @JoinColumn(name = "deck_id"))
    @Column(name = "colors")
    private Set<EnumColor> colors = new HashSet<>();
	
	@Column(name="mana_cost")
	private Float manaCost;
	
	
	@Column(name ="public", nullable = false)
	private Boolean isPublic;
	
	@Column(name = "date_publication", nullable = true)
	@Temporal(TemporalType.DATE)
	private LocalDate datePublication;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deckbuilder_id", nullable = true)
    private DeckCreator deckBuilder;
	 
	
	@ManyToMany(mappedBy = "decksLiked", fetch = FetchType.LAZY)
	private Set<DeckCreator> deckBuilders = new HashSet<>();
	
	@Column(name = "like_number")
	private Long likeNumber; 
	

	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Deck_Cards", 
        joinColumns = { @JoinColumn(name = "deck_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "card_id") }
    )	
	private List<Card> cards;
	

	@ManyToOne
	@JoinColumn (name= "commander_id", nullable = true)
	private Card commander;
	
	@OneToMany(mappedBy = "deck", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Notification> notifications;
	

}
