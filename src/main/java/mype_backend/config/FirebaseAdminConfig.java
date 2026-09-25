package mype_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAdminConfig.class);

    @Value("${firebase.admin.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.admin.credentials.base64:}")
    private String credentialsBase64;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream credentials = resolveCredentials()) {
            if (credentials == null) {
                log.warn("Firebase Admin no fue inicializado. Configura firebase.admin.credentials.path o firebase.admin.credentials.base64 para crear usuarios desde el backend.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin inicializado correctamente.");
        } catch (IOException e) {
            log.warn("No se pudo inicializar Firebase Admin: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentials() throws IOException {
        if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64);
            return new ByteArrayInputStream(decoded);
        }
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            return new FileInputStream(credentialsPath);
        }
        return null;
    }
}
