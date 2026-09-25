package mype_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.url:}")
    private String apiUrl;

    private static final String SYSTEM_PROMPT = "Eres el asistente virtual de FormaEasy, un sistema de facturación informativo y entrenamiento para MYPEs en Perú. Eres amable, conciso y ayudas con dudas sobre cómo usar notas de venta, boletas y conceptos básicos de formalización SUNAT. Responde de forma breve y profesional.";

    public String askChatbot(String userPrompt) {
        log.info("LLM pendiente de integración. apiUrl configurada: {}", !apiUrl.isBlank());
        return "Gracias por tu consulta sobre: '" + userPrompt + "'. Como asistente virtual de FormaEasy en entrenamiento, estoy aquí para guiarte en el proceso de facturación y formalización.";
    }
}
