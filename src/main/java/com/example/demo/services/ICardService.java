package com.example.demo.services;

import java.util.List;
import org.springframework.data.domain.Page;
import com.example.demo.dto.GetCard;


public interface ICardService {

	List<String> getAllCardImages();

	List<GetCard> getTop3Cards();
	
	List<GetCard> getTop3Cedh();
	
	List<Long> getAllCardsRanked(String order);

	Page<GetCard> getCardsByFilterPaged(int page, int size, String order, String name, String text, Double cmcMin,
			Double cmcMax, List<String> types, Boolean legendary, List<String> rarities, List<String> colors,
			List<String> formats, List<String> editions);

	


}
