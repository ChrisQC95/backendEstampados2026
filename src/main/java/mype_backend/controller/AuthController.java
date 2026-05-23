package mype_backend.controller;

import mype_backend.entity.Usuario;
import mype_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permitir llamadas desde tu React
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Usuario login(@RequestBody Usuario loginRequest) {
        // Por ahora, recibimos el UID directamente para probar.
        // Luego lo cambiaremos por la validación real del Token.
        return authService.verificarOSubsanarUsuario(
                loginRequest.getFirebaseUid(),
                loginRequest.getEmail(),
                loginRequest.getNombre());
    }
}