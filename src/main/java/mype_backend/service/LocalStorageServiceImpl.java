package mype_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// @Service // Deshabilitado en favor de Supabase
public class LocalStorageServiceImpl implements StorageService {

    // Cambiar si se expone en otro puerto/host
    private final String baseUrl = "http://localhost:8080";
    private final String uploadDir = "./uploads";

    public LocalStorageServiceImpl() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }

        try {
            // Asegurar que la carpeta exista
            String folderPath = uploadDir + "/" + folder;
            File folderDir = new File(folderPath);
            if (!folderDir.exists()) {
                folderDir.mkdirs();
            }

            // Generar nombre único para evitar colisiones
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path path = Paths.get(folderPath, uniqueFilename);
            Files.write(path, file.getBytes());

            // Devolver URL accesible vía web
            return baseUrl + "/archivos/" + folder + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo localmente", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty() || !fileUrl.startsWith(baseUrl + "/archivos/")) {
            return;
        }

        try {
            // Extraer ruta relativa: "http://localhost:8080/archivos/folder/name.jpg" ->
            // "folder/name.jpg"
            String relativePath = fileUrl.replace(baseUrl + "/archivos/", "");
            Path path = Paths.get(uploadDir, relativePath);

            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error al intentar eliminar archivo local: " + e.getMessage());
        }
    }
}

