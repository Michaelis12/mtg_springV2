package com.example.demo.utils;

public class Sanitizer {
    public static String sanitize(String input) {
        if (input == null) return null;
        
        // Ne pas sanitizer les chemins d'images uploadées
        if (input.startsWith("/uploads/")) {
            return input;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
    
    /**
     * Sanitize spécifiquement pour les noms (cartes, decks, etc.)
     * Ne sanitize que les caractères vraiment dangereux pour l'HTML
     * mais garde les apostrophes qui sont normales dans les noms
     */
    public static String sanitizeName(String input) {
        if (input == null) return null;
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            // Ne pas encoder les apostrophes car elles sont normales dans les noms
            // .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
} 