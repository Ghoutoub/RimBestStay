package com.Rimbest.rimbest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Le fichier est vide");
        }
        
        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);
        
        // Copier le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner le chemin relatif pour le stockage en base
        return "/" + uploadDir + "/" + subDirectory + "/" + fileName;
    }
    
    public List<String> storeMultipleFiles(List<MultipartFile> files, String subDirectory) throws IOException {
        List<String> filePaths = new ArrayList<>();
        
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filePath = storeFile(file, subDirectory);
                    filePaths.add(filePath);
                }
            }
        }
        
        return filePaths;
    }
    
    public void deleteFile(String filePath) throws IOException {
        if (filePath != null && !filePath.isEmpty()) {
            // Enlever le slash initial si présent
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }
}