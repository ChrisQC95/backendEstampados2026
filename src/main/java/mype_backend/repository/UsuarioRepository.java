package mype_backend.repository;

import mype_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Con solo nombrar el método así, Spring Boot crea la consulta SQL:
    // SELECT * FROM usuarios WHERE firebase_uid = ?
    Optional<Usuario> findByFirebaseUid(String firebaseUid);

    // Método útil para validar si un RUC ya está registrado
    Optional<Usuario> findByRuc(String ruc);
}