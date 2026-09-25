package mype_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    // Nombre del bucket en Supabase Storage
    private final String bucketName = "logos";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }

        try {
            // 1. Generar nombre único con UUID para evitar colisiones
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 2. Construir la ruta dentro del bucket.
            // Si folder viene vacío o nulo, colocamos el archivo en la raíz del bucket.
            // Evitamos la ruta duplicada "logos/logos/archivo".
            String objectPath;
            if (folder == null || folder.isBlank()) {
                objectPath = uniqueFilename;
            } else {
                objectPath = folder + "/" + uniqueFilename;
            }

            // 3. Endpoint de Supabase Storage para subir
            // POST {supabaseUrl}/storage/v1/object/{bucketName}/{objectPath}
            String endpoint = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, bucketName, objectPath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            // Supabase requiere x-upsert=true para reemplazar en caso de nombre existente
            headers.set("x-upsert", "true");

            // Establecer el Content-Type correcto
            String contentType = file.getContentType();
            headers.setContentType(contentType != null
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Supabase respondió con error: " + response.getBody());
            }

            // 4. Retornar la URL pública (accesible sin autenticación porque el bucket es público)
            // Formato: {supabaseUrl}/storage/v1/object/public/{bucketName}/{objectPath}
            return String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, bucketName, objectPath);

        } catch (HttpClientErrorException e) {
            // Error HTTP de Supabase (4xx) con detalle del cuerpo
            throw new RuntimeException(
                    "Error al subir imagen a Supabase (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer bytes del archivo", e);
        } catch (RuntimeException e) {
            throw e; // Re-lanzar RuntimeException ya formateadas
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado en subida a Supabase", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        // Ignorar silenciosamente URLs locales (de versiones anteriores del sistema)
        if (fileUrl.startsWith("http://localhost")) {
            System.out.println("Ignorando URL local antigua, no se borra de Supabase: " + fileUrl);
            return;
        }

        // Validar que pertenezca a nuestro bucket de Supabase
        String publicPrefix = String.format("%s/storage/v1/object/public/%s/",
                supabaseUrl, bucketName);
        if (!fileUrl.startsWith(publicPrefix)) {
            System.err.println("URL a eliminar no pertenece al bucket '" + bucketName + "': " + fileUrl);
            return;
        }

        try {
            // Extraer la ruta relativa del objeto dentro del bucket (ej: "general/uuid.jpg")
            String objectPath = fileUrl.substring(publicPrefix.length());

            // DELETE {supabaseUrl}/storage/v1/object/{bucketName}/{objectPath}
            String endpoint = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, bucketName, objectPath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.DELETE, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Imagen eliminada de Supabase: " + objectPath);
            } else {
                System.err.println("Supabase respondió con error al borrar: " + response.getBody());
            }

        } catch (HttpClientErrorException e) {
            // 404 significa que el archivo ya no existe — no es crítico
            if (e.getStatusCode().value() == 404) {
                System.out.println("La imagen ya no existía en Supabase, no es necesario borrar.");
            } else {
                System.err.println("Error HTTP al borrar imagen en Supabase ("
                        + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            // No lanzamos excepción — el borrado es una operación de limpieza secundaria
            System.err.println("Error inesperado al intentar borrar imagen en Supabase: " + e.getMessage());
        }
    }
}

