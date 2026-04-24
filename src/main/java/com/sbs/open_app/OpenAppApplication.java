package com.sbs.open_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenAppApplication.class, args);
        System.out.println("\n=========================================");
        System.out.println("  Sistema de Turnos Médicos - Iniciado");
        System.out.println("  Puerto: 8081");
        System.out.println("  Calendario: http://localhost:8081/turnos");
        System.out.println("  Admin:      http://localhost:8081/admin");
        System.out.println("=========================================\n");
    }
}
