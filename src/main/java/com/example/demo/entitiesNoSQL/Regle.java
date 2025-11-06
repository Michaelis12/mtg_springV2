package com.example.demo.entitiesNoSQL;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "regles")
public class Regle {
	
	@Id
    private String id;
    private String title;
    private String text;

}
