package com.example.demo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.example.demo.entities.Card;
import com.example.demo.entities.DeckCreator;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;
import com.example.demo.enums.UserActivity;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAuthentification {
		
		
		private String jwt;
		
		private Collection<? extends GrantedAuthority> authorities;



		



}
