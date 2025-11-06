package com.example.demo.entities;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "notification")
public class Notification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private LocalDateTime date;
	
	@ManyToOne
	@JoinColumn (name= "deck_id")
	private Deck deck;
	
	@ManyToOne
	@JoinColumn (name= "issuer_id")
	private DeckCreator issuer;
	
	@ManyToOne
	@JoinColumn (name= "receivor_id")
	private DeckCreator receivor;

}
