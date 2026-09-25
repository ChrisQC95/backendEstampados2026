package mype_backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

@Service
public class MockupImageProcessor {

    private static final int BACKGROUND_TOLERANCE = 42;
    private static final int EDGE_ALPHA_THRESHOLD = 245;

    public MultipartFile normalizarProductoBase(MultipartFile source) {
        try {
            BufferedImage original = ImageIO.read(source.getInputStream());
            if (original == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen base no se pudo leer.");
            }

            BufferedImage normalized = hasUsefulAlpha(original)
                    ? ensureArgb(original)
                    : removeBackgroundFromEdges(original);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(normalized, "png", output);
            String filename = cleanName(source.getOriginalFilename()) + "-normalizado.png";
            return new InMemoryMultipartFile("imagenBase", filename, "image/png", output.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo procesar la imagen base.", e);
        }
    }

    private boolean hasUsefulAlpha(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return false;
        }
        int stepX = Math.max(1, image.getWidth() / 80);
        int stepY = Math.max(1, image.getHeight() / 80);
        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xff;
                if (alpha < EDGE_ALPHA_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    private BufferedImage ensureArgb(BufferedImage source) {
        BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        output.getGraphics().drawImage(source, 0, 0, null);
        return output;
    }

    private BufferedImage removeBackgroundFromEdges(BufferedImage source) {
        BufferedImage image = ensureArgb(source);
        int width = image.getWidth();
        int height = image.getHeight();
        int background = estimateBackgroundColor(image);
        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            enqueueIfBackground(image, visited, queue, x, 0, background);
            enqueueIfBackground(image, visited, queue, x, height - 1, background);
        }
        for (int y = 0; y < height; y++) {
            enqueueIfBackground(image, visited, queue, 0, y, background);
            enqueueIfBackground(image, visited, queue, width - 1, y, background);
        }

        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        while (!queue.isEmpty()) {
            int[] point = queue.poll();
            int x = point[0];
            int y = point[1];
            image.setRGB(x, y, image.getRGB(x, y) & 0x00ffffff);

            for (int[] direction : directions) {
                int nx = x + direction[0];
                int ny = y + direction[1];
                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                    enqueueIfBackground(image, visited, queue, nx, ny, background);
                }
            }
        }

        softenBorderAlpha(image);
        return image;
    }

    private void enqueueIfBackground(BufferedImage image, boolean[][] visited, Queue<int[]> queue, int x, int y, int background) {
        if (visited[y][x]) {
            return;
        }
        visited[y][x] = true;
        if (colorDistance(image.getRGB(x, y), background) <= BACKGROUND_TOLERANCE) {
            queue.add(new int[] { x, y });
        }
    }

    private int estimateBackgroundColor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int marginX = Math.max(1, width / 20);
        int marginY = Math.max(1, height / 20);
        long r = 0;
        long g = 0;
        long b = 0;
        int count = 0;

        int[][] samples = {
                { 0, 0, marginX, marginY },
                { width - marginX, 0, width, marginY },
                { 0, height - marginY, marginX, height },
                { width - marginX, height - marginY, width, height }
        };

        for (int[] sample : samples) {
            for (int y = sample[1]; y < sample[3]; y++) {
                for (int x = sample[0]; x < sample[2]; x++) {
                    Color color = new Color(image.getRGB(x, y), true);
                    r += color.getRed();
                    g += color.getGreen();
                    b += color.getBlue();
                    count++;
                }
            }
        }

        if (count == 0) {
            return image.getRGB(0, 0);
        }
        return new Color((int) (r / count), (int) (g / count), (int) (b / count)).getRGB();
    }

    private void softenBorderAlpha(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xff;
                if (alpha == 255 && hasTransparentNeighbor(image, x, y)) {
                    image.setRGB(x, y, (150 << 24) | (image.getRGB(x, y) & 0x00ffffff));
                }
            }
        }
    }

    private boolean hasTransparentNeighbor(BufferedImage image, int x, int y) {
        for (int ny = y - 1; ny <= y + 1; ny++) {
            for (int nx = x - 1; nx <= x + 1; nx++) {
                int alpha = (image.getRGB(nx, ny) >>> 24) & 0xff;
                if (alpha == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private double colorDistance(int rgbA, int rgbB) {
        Color a = new Color(rgbA, true);
        Color b = new Color(rgbB, true);
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private String cleanName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "producto-base";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
    }
}
