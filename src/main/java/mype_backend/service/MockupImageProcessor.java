package mype_backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class MockupImageProcessor {

    public MultipartFile normalizarProductoBase(MultipartFile source) {
        try {
            BufferedImage original = ImageIO.read(source.getInputStream());
            if (original == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen base no se pudo leer.");
            }

            BufferedImage normalized = ensureArgb(original);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(normalized, "png", output);
            String filename = cleanName(source.getOriginalFilename()) + "-normalizado.png";
            return new InMemoryMultipartFile("imagenBase", filename, "image/png", output.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo procesar la imagen base.", e);
        }
    }

    private BufferedImage ensureArgb(BufferedImage source) {
        BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return output;
    }

    private String cleanName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "producto-base";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
    }
}
