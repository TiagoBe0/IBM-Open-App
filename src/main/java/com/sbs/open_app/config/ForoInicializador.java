package com.sbs.open_app.config;

import com.sbs.open_app.servicios.ForoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForoInicializador implements CommandLineRunner {

    private final ForoServicio foroServicio;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar categorías del foro si no existen
        foroServicio.inicializarCategoriasDefault();
        System.out.println("✅ Categorías del foro inicializadas correctamente");
    }
}
