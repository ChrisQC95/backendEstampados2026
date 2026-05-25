package mype_backend.controller;

import mype_backend.entity.Usuario;
import mype_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
// @CrossOrigin(origins = "*") // Para evitar el CORS
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // EL ENDPOINT QUE REACT ESTÁ BUSCANDO
    @GetMapping("/firebase/{uid}")
    public ResponseEntity<Usuario> obtenerPorFirebaseUid(@PathVariable String uid) {
        return usuarioRepository.findByFirebaseUid(uid)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    // (Opcional por ahora) Endpoint para registrar un usuario nuevo cuando se crea
    // la cuenta
    @PostMapping
    public Usuario registrar(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}