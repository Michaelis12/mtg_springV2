package com.example.demo.services;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {
    
    private static final String UPLOAD_DIR = "uploads";
    
    /**
     * Supprime un fichier du dossier uploads
     * @param imagePath Le chemin relatif de l'image (ex: /uploads/filename.jpg)
     * @return true si le fichier a été supprimé, false sinon
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || !imagePath.startsWith("/uploads/")) {
            return false;
        }
        
        
        try {
            // Enlever le préfixe /uploads/ pour obtenir le nom du fichier
            String fileName = imagePath.substring("/uploads/".length());
            
            
            if (fileName.equalsIgnoreCase("default") || fileName.startsWith("default")) {
                return false;
            }
            
            
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression du fichier " + imagePath + ": " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Supprime une image seulement si elle n'est plus utilisée
     * @param oldImagePath L'ancien chemin de l'image
     * @param newImagePath Le nouveau chemin de l'image (peut être null)
     * @return true si l'ancienne image a été supprimée
     */
    public boolean deleteOldImageIfUnused(String oldImagePath, String newImagePath) {
        // Si l'ancienne image est la même que la nouvelle, ne rien faire
        if (oldImagePath != null && oldImagePath.equals(newImagePath)) {
            return false;
        }
        
        // Si l'ancienne image existe et est différente de la nouvelle, la supprimer
        if (oldImagePath != null && oldImagePath.startsWith("/uploads/")) {
            return deleteImage(oldImagePath);
        }
        
        return false;
    }
    
    /**
     * Récupère tous les chemins d'images utilisées dans la base de données
     * @param cardImages Images des cartes
     * @param deckImages Images des decks
     * @param userAvatars Avatars des utilisateurs
     * @return Set des chemins d'images utilisées
     */
    public Set<String> getUsedImagePaths(List<String> cardImages, List<String> deckImages, List<String> userAvatars) {
        Set<String> usedImages = new java.util.HashSet<>();
        
        if (cardImages != null) {
            usedImages.addAll(cardImages.stream()
                .filter(img -> img != null && img.startsWith("/uploads/"))
                .collect(Collectors.toSet()));
        }
        
        if (deckImages != null) {
            usedImages.addAll(deckImages.stream()
                .filter(img -> img != null && img.startsWith("/uploads/"))
                .collect(Collectors.toSet()));
        }
        
        if (userAvatars != null) {
            usedImages.addAll(userAvatars.stream()
                .filter(img -> img != null && img.startsWith("/uploads/"))
                .collect(Collectors.toSet()));
        }
        
        return usedImages;
    }
    
    /**
     * Nettoie les images orphelines du dossier uploads
     * @param usedImagePaths Set des chemins d'images utilisées
     * @return Nombre d'images supprimées
     */
    public int cleanupOrphanedImages(Set<String> usedImagePaths) {
        int deletedCount = 0;
        
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                return 0;
            }
            
            try (Stream<Path> paths = Files.walk(uploadPath, 1)) {
                List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
                
                for (Path file : files) {
                    String fileName = file.getFileName().toString();
                    String imagePath = "/uploads/" + fileName;
                    
                    if (!usedImagePaths.contains(imagePath)) {
                        if (deleteImage(imagePath)) {
                            deletedCount++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du nettoyage des images orphelines: " + e.getMessage());
        }
        
        return deletedCount;
    }
} 