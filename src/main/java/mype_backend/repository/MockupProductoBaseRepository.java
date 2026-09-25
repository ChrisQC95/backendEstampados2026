package mype_backend.repository;

import mype_backend.entity.MockupProductoBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockupProductoBaseRepository extends JpaRepository<MockupProductoBase, Long> {

    List<MockupProductoBase> findByActivoTrueOrderByTipoProductoAscColorAscNombreAsc();

    List<MockupProductoBase> findAllByOrderByTipoProductoAscColorAscNombreAsc();
}
