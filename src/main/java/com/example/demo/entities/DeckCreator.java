package com.example.demo.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.enums.UserActivity;
import com.example.demo.enums.UserRole;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "deckbuilder")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeckCreator implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "pseudo", length = 25, nullable = false, unique = true)
	private String pseudo;
	
	@Column(name = "email", length = 32, nullable = false, unique = true)
	private String email;
	
	@Column(name = "password", length = 500, nullable = false)
	private String password;
	
	@Column(name = "date_sign")
	@Temporal(TemporalType.DATE)
	private LocalDate dateSign;
	
	@Column(name ="avatar", length = 2000 )
	private String avatar;
	
	@Column(name ="bio", length = 500 )
	private String bio;
	
	@Column(name = "activity", nullable = true)
	@Enumerated(EnumType.STRING)
	private UserActivity activity;

	
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
	private List<UserRole> roles = new ArrayList<>();
	
	
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Deckbuilder_Decks", 
        joinColumns = { @JoinColumn(name = "deckBuilder_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "deck_id") }
    )	
	private List<Deck> decksLiked;
	
	@Column(name = "decks_liked_number", nullable = true)
	private Long decksLikedNumber;
	
	@OneToMany(mappedBy = "deckBuilder", cascade = CascadeType.ALL)
	private Set<Deck> decks;
	
	@Column(name = "decks_number", nullable = true)
	private Long decksNumber;
	
	@Column(name = "decks_public_number", nullable = true)
	private Long decksPublicNumber;
	
	@Column(name = "decks_public_likes_number", nullable = true)
	private Long decksPublicLikesNumber;
	
	@OneToMany(mappedBy = "issuer", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Notification> notificationsSend;
	
	@OneToMany(mappedBy = "receivor", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Notification> notificationsReceive;
	

	// Transforme les roles de l'utilisateur en grantedAuthorities
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (UserRole userRole : roles) {
        	  GrantedAuthority authority = new SimpleGrantedAuthority(userRole.toString());
              grantedAuthorities.add(authority);
		}
		return grantedAuthorities;
	}


	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}
	
	// Code de vérification par email 
	
	@Column(name = "verification_code", length = 64)
	private String verificationCode;


	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Column(name = "enabled", nullable = true)
    private boolean enabled;
	
	@Column(name = "suspended_until")
    private LocalDate suspendedUntil;

    
    @Override
    public boolean isEnabled() {
    	if (suspendedUntil == null && enabled == false) {
    		return false;
    	}
        if (suspendedUntil != null && LocalDate.now().isBefore(suspendedUntil)) {
            return false; // Le compte est temporairement désactivé.
        }
        return enabled; // Sinon, l'utilisateur peut être activé.
    }
	

}
