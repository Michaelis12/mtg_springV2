package com.example.demo.form;
import java.util.HashSet;
import java.util.Set;

import com.example.demo.entities.Card;
import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormCedh {
		
	private String name;
	
	private String image;
	
	private EnumFormat format;
	
	private Set<EnumColor> colors = new HashSet<>();
	
	private Card commandant;
		
}
