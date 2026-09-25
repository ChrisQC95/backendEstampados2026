package mype_backend.repository;

import mype_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByFirebaseUid(String firebaseUid);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByRuc(String ruc);

    Optional<Usuario> findFirstByOrderByIdAsc();

    List<Usuario> findAllByOrderByIdAsc();

    long countByRolCodigoAndActivoTrue(String codigo);
}
