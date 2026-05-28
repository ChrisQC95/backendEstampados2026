package mype_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando catálogos básicos si no existen...");

        try {
            jdbcTemplate.execute(
                    "INSERT INTO tipos_pago (id, descripcion, dias_credito, activo) " +
                            "VALUES (1, 'Contado', 0, true) " +
                            "ON CONFLICT (id) DO NOTHING");
            jdbcTemplate.execute(
                    "INSERT INTO tipos_pago (id, descripcion, dias_credito, activo) " +
                            "VALUES (2, 'Crédito a 30 días', 30, true) " +
                            "ON CONFLICT (id) DO NOTHING");

            jdbcTemplate.execute(
                    "INSERT INTO tipos_operacion (id, codigo_sunat, descripcion) VALUES (1, '0101', 'Venta Interna') ON CONFLICT (id) DO NOTHING");

            jdbcTemplate.execute(
                    "INSERT INTO tipos_comprobante (id, codigo_sunat, descripcion, requiere_cliente_ruc) VALUES (1, '01', 'Factura Electrónica', true) ON CONFLICT (id) DO NOTHING");
            jdbcTemplate.execute(
                    "INSERT INTO tipos_comprobante (id, codigo_sunat, descripcion, requiere_cliente_ruc) VALUES (2, '03', 'Boleta de Venta Electrónica', false) ON CONFLICT (id) DO NOTHING");

            jdbcTemplate.execute(
                    "INSERT INTO monedas (id, codigo_sunat, descripcion, simbolo) VALUES (1, 'PEN', 'Soles', 'S/') ON CONFLICT (id) DO NOTHING");
            jdbcTemplate.execute(
                    "INSERT INTO monedas (id, codigo_sunat, descripcion, simbolo) VALUES (2, 'USD', 'Dólares', '$') ON CONFLICT (id) DO NOTHING");

            log.info("Catálogos básicos inicializados correctamente.");
        } catch (Exception e) {
            log.warn(
                    "Ocurrió un error inicializando los catálogos (probablemente las tablas aún no existen o ya están pobladas): {}",
                    e.getMessage());
        }
    }
}
