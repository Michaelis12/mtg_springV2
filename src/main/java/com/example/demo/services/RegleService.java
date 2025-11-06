package com.example.demo.services;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entitiesNoSQL.Regle;
import com.example.demo.repositories.RegleRepository;
import com.example.demo.utils.Sanitizer;

@Service
public class RegleService {
	
	@Autowired
	private RegleRepository regleRepository;
		
	
	// All 
	
	public List<Regle> getRegles() {
		return regleRepository.findAll();
	}
	
	public Regle getRegleByID(String regleID) {
		
		Optional<Regle> regleSearch = regleRepository.findById(regleID);
		
		if (regleSearch.isPresent()) {
			return regleSearch.get();
			}
		
		throw new RuntimeException("Donnée non trouvée");
		
	}
	

	
	public String getPrevRegle( String regleID, List<String> reglesID) {
		
		// Vérifie si cardID est dans la liste usersID
	    int index = reglesID.indexOf(regleID);
	    if (index == -1) {
	        throw new RuntimeException("Id incorrect");
	    }
	    
	    // Si l'utilisateur est le premier dans la liste, il n'y a pas de précédent
	    if (index == 0) {
	        throw new RuntimeException("Aucun id précédent");
	    }
	    
	    // Retourne l'utilisateur précédant dans la liste
	    return reglesID.get(index - 1);
			

	}
	
	public String getNextRegle(String regleID, List<String> reglesID) {
	    if (reglesID.contains(regleID)) {
	        for (int i = 0; i < reglesID.size(); i++) {
	            if (reglesID.get(i).equals(regleID)) {
	                if (i + 1 < reglesID.size()) {
	                    return reglesID.get(i + 1);
	                } else {
	                    throw new RuntimeException("Aucun id suivant");
	                }
	            }
	        }
	    }
	    throw new RuntimeException("Id incorrect");
	}
	
	
	// Admin 
	
	public Regle addRegle (Regle regle) {
		regle.setTitle(Sanitizer.sanitize(regle.getTitle()));
		regle.setText(Sanitizer.sanitize(regle.getText()));
		return regleRepository.save(regle);
	}
	
	
	public String deleteRegle(String regleID) {
		
		Optional<Regle> regleSearch = regleRepository.findById(regleID);
		
		if (regleSearch.isPresent()) {
			regleRepository.deleteById(regleID);
			return "règle " + regleID + " supprimée";
			}
		
		throw new RuntimeException("Donnée non trouvée");
	}
	
	public Regle updateRegle(String regleID, String title, String text) {
		
		Optional<Regle> regleSearch = regleRepository.findById(regleID);
		
		if (regleSearch.isPresent()) {
			Regle regleFind = regleSearch.get();
			
			if (title != null) {
				regleFind.setTitle(Sanitizer.sanitize(title));
			}
			if (text != null) {
				regleFind.setText(Sanitizer.sanitize(text));
			}
				
			return regleRepository.save(regleFind);
		}
		
		throw new RuntimeException("Donnée non trouvée");
		
	}
	

}

