package com.joao.adotec;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Startup {

    public static void main(String[] args) {
        try {
            // We try to load the .env file. If it's not found, it will throw an exception.
            Dotenv dotenv = Dotenv.configure().load();
            
            // Set all loaded properties as system properties for Spring to use
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            // This block will run if the .env file is missing from the project root.
            System.err.println("\n--- ERRO: Arquivo .env não encontrado ou erro ao carregar! ---\n");
            System.err.println("Por favor, certifique-se de que um arquivo chamado '.env' existe na raiz do seu projeto.");
            System.err.println("Caminho esperado: C:/Users/Joao/Documents/ws-sts/ProjetoIntegrador/AdoTEC/AdoTEC/.env");
            System.err.println("Detalhes do erro: " + e.getMessage());
            System.err.println("\nA aplicação será encerrada.\n");
            System.exit(1); // Exit the application
        }

        // If we get here, the .env file was loaded successfully.
        SpringApplication.run(Startup.class, args);
    }
}
