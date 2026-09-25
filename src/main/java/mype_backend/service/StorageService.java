package mype_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    
    /**
     * Sube un archivo y retorna la URL pública (o local) para accederlo.
     *
     * @param file El archivo a subir
     * @param folder La carpeta o ruta donde se debe guardar
     * @return URL del archivo subido
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Elimina un archivo dado su URL.
     *
     * @param fileUrl URL del archivo a eliminar
     */
    void deleteFile(String fileUrl);
}

