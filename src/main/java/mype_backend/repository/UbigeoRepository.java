package mype_backend.repository;

import mype_backend.entity.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbigeoRepository extends JpaRepository<Ubigeo, String> {

    @Query("""
                SELECT DISTINCT u.departamento
                FROM Ubigeo u
                ORDER BY u.departamento
            """)
    List<String> listarDepartamentos();

    @Query("""
                SELECT DISTINCT u.provincia
                FROM Ubigeo u
                WHERE u.departamento = :departamento
                ORDER BY u.provincia
            """)
    List<String> listarProvincias(
            @Param("departamento") String departamento);

    @Query("""
                SELECT u
                FROM Ubigeo u
                WHERE u.departamento = :departamento
                AND u.provincia = :provincia
                ORDER BY u.distrito
            """)
    List<Ubigeo> listarDistritos(
            @Param("departamento") String departamento,
            @Param("provincia") String provincia);
}
