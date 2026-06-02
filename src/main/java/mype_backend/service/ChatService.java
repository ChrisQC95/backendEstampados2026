package mype_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// Importaciones de WebClient o clases de DTO según el LLM a usar
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.url:}")
    private String apiUrl;

    private static final String SYSTEM_PROMPT = "Eres el asistente virtual de FormaEasy, un sistema de facturación informativo y entrenamiento para MYPEs en Perú. Eres amable, conciso y ayudas con dudas sobre cómo usar notas de venta, boletas y conceptos básicos de formalización SUNAT. Responde de forma breve y profesional.";

    public String askChatbot(String userPrompt) {
        // En este punto, simularíamos la llamada a la API de un LLM.
        // Se debe preparar la estructura usando RestTemplate o WebClient.
        
        /* 
         * EJEMPLO DE IMPLEMENTACIÓN CON OPENAI (Comentado):
         * 
         * RestTemplate restTemplate = new RestTemplate();
         * HttpHeaders headers = new HttpHeaders();
         * headers.setContentType(MediaType.APPLICATION_JSON);
         * headers.setBearerAuth(apiKey);
         * 
         * String requestBody = "{\"model\": \"gpt-4o-mini\", \"messages\": [" +
         *     "{\"role\": \"system\", \"content\": \"" + SYSTEM_PROMPT + "\"}," +
         *     "{\"role\": \"user\", \"content\": \"" + userPrompt.replace("\"", "\\\"") + "\"}" +
         *     "]}";
         * 
         * HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
         * ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
         * 
         * return extraerMensaje(response.getBody());
         */

        log.info("Simulando llamada a LLM. SYSTEM PROMPT: [{}]. USER PROMPT: [{}]", SYSTEM_PROMPT, userPrompt);
        
        // Simulación temporal:
        return "Gracias por tu consulta sobre: '" + userPrompt + "'. Como asistente virtual de FormaEasy en entrenamiento, estoy aquí para guiarte en el proceso de facturación y formalización.";
    }
}
