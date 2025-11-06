package com.example.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${email.from}")
    private String fromEmail;

    private WebClient webClient;

    // Initialisation du WebClient après injection des valeurs
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        // Corps de la requête JSON
    	Map<String, Object> body = new HashMap<>();
    	body.put("from", fromEmail); // email vérifié dans Resend
    	body.put("to", toEmail);
    	body.put("subject", "Vérification de votre compte");
    	body.put("text", "Bonjour,\n\nVotre code de vérification est : " + verificationCode + "\n\nMerci d’utiliser notre application !");
    	body.put("html", "<p>Bonjour,</p><p>Votre code de vérification est : <b>" + verificationCode + "</b></p><p>Merci d’utiliser notre application !</p>");



        try {
            String response = webClient.post()
                    .uri("/emails")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Bloquant pour simplifier

            System.out.println("✅ Email envoyé à " + toEmail + " — Réponse : " + response);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l’envoi d’email : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
