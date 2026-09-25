package mype_backend.controller;

import mype_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
// @CrossOrigin(origins = "*") // Usar si no hay configuración global de CORS
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askChatbot(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("response", "El prompt no puede estar vacío."));
        }

        String response = chatService.askChatbot(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }
}

