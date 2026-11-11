package com.petconnect.shared.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Inicializador de base de datos para entornos de desarrollo/local.
 *
 * Cuando la propiedad {@code app.db.reset-on-start} es {@code true}, este
 * runner ejecuta {@link Flyway#clean()} (borra {@code flyway_schema_history}
 * y todos los objetos del esquema) seguido de {@link Flyway#migrate()}
 * (vuelve a aplicar las migraciones, creando el esquema y los datos de
 * referencia que sigan activos en ellas).
 *
 * Flujo previsto:
 *  1. Activar {@code DB_RESET_ON_START=true} para un único arranque local
 *     deliberado: se borra y reconstruye el esquema.
 *  2. Dejar {@code DB_RESET_ON_START=false} en los arranques normales para
 *     conservar los datos existentes.
 */
@Component
public class DbResetInitializer implements CommandLineRunner {

    private final ObjectProvider<Flyway> flywayProvider;
    private final Environment env;

    public DbResetInitializer(ObjectProvider<Flyway> flywayProvider, Environment env) {
        this.flywayProvider = flywayProvider;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        boolean reset = env.getProperty("app.db.reset-on-start", Boolean.class, false);
        if (!reset) {
            return;
        }

        // Obtener el bean Flyway solo si existe (en tests con H2/embebida puede no estar).
        Flyway flyway = flywayProvider.getIfAvailable();
        if (flyway == null) {
            return;
        }

        // Borra flyway_schema_history y todos los objetos gestionados por Flyway.
        flyway.clean();

        // Vuelve a aplicar todas las migraciones (V1…Vxx), creando el esquema
        // y ejecutando los INSERT de seed de cada versión.
        flyway.migrate();
    }
}