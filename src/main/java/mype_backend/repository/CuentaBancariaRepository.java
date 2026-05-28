package mype_backend.repository;

import mype_backend.entity.CuentaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    // Listar todas las cuentas de la MYPE
    List<CuentaBancaria> findByUsuarioId(Long usuarioId);

    // Desactivar todas las cuentas de un usuario específico
    @Modifying
    @Query("UPDATE CuentaBancaria c SET c.activo = false WHERE c.usuarioId = :usuarioId")
    void desactivarTodasPorUsuario(@Param("usuarioId") Long usuarioId);
}