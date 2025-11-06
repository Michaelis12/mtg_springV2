package com.example.demo.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCard {
	
    private Long id;

    private String apiID;

    private String name;
    
    private String text;
    
    private String image;

    private String manaCost;

    private Double cmc;

    private Set<String> colors = new HashSet<>();

    private List<String> types = new ArrayList<>();
    
    private List<String> formats = new ArrayList<>();
    
    private boolean legendary;
    
    private Long decksNumber;
    
    private Long cedhNumber;

}
