package com.example.demo.repositories;



import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.DeckCreator;
import com.example.demo.entities.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	
	Set<Notification> findByReceivor(DeckCreator receivor);

}
