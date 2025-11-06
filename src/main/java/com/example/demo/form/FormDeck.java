package com.example.demo.form;

import java.util.Set;

import com.example.demo.enums.EnumColor;
import com.example.demo.enums.EnumFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDeck {
		
	private String name;
	
	private String image;
	
	private EnumFormat format;
	
	private Set<EnumColor> colors;
	
}
