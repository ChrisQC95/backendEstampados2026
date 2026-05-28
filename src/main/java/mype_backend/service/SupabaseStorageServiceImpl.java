package mype_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Primary
public class SupabaseStorageServiceImpl implements StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    // El usuario indicó que el bucket se llama 'logos'
    private final String bucketName = "logos";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }

        try {
            // 1. Generar nombre único
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            // Si el nombre tiene espacios u otros caracteres, supabase podría requerir URL encode, 
            // pero con UUID es seguro.
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Ruta completa dentro del bucket: folder/filename (ej: general/abc.jpg)
            String objectPath = folder + "/" + uniqueFilename;

            // 2. Hacer POST a Supabase
            // Endpoint: POST {supabaseUrl}/storage/v1/object/{bucketName}/{objectPath}
            String endpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, objectPath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            
            // Establecer el Content-Type correcto según el archivo
            String contentType = file.getContentType();
            if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al subir imagen a Supabase: " + response.getBody());
            }

            // 3. Retornar la URL pública
            // Endpoint público: {supabaseUrl}/storage/v1/object/public/{bucketName}/{objectPath}
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, objectPath);

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo para subir a Supabase", e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado en subida a Supabase", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // Validar que sea una URL de nuestro bucket
        String publicPrefix = String.format("%s/storage/v1/object/public/%s/", supabaseUrl, bucketName);
        if (!fileUrl.startsWith(publicPrefix)) {
            System.err.println("La URL a eliminar no pertenece a nuestro bucket Supabase: " + fileUrl);
            return;
        }

        try {
            // Extraer el objectPath: folder/filename
            String objectPath = fileUrl.substring(publicPrefix.length());

            // Endpoint para borrar: DELETE {supabaseUrl}/storage/v1/object/{bucketName}/{objectPath}
            String endpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, objectPath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Error al borrar la imagen en Supabase: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Error intentando borrar la imagen en Supabase: " + e.getMessage());
        }
    }
}
