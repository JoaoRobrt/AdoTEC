package com.joao.adotec.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração global da documentação OpenAPI / Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AdoTEC API",
                version = "0.2.0",
                description = """
                        API do **AdoTEC** — Sistema de Adoção do Centro de Zoonoses.

                        Funcionalidades:
                        - Cadastro e listagem de pets disponíveis para adoção
                        - Agendamento de visitas (time slots dinâmicos)
                        - Painel administrativo com métricas e gestão de agendamentos
                        - Gestão de funcionários (CRUD)
                        - Upload de fotos de pets via Cloudinary
                        - Autenticação JWT com controle de acesso por roles (ADMIN, EMPLOYEE, ADOPTER)

                        ### Paginação
                        Endpoints paginados aceitam os parâmetros `page` (número da página, base 0), `size` (itens por página) e `sort` (campo,direção).

                        ### Ordenação
                        O parâmetro `sort` aceita o formato `campo,DIREÇÃO`. Exemplo: `sort=createdAt,DESC`.
                        Para agendamentos, o alias `petName` é aceito e convertido internamente para `pet.petName`.

                        ### Autenticação
                        Utilize o endpoint `POST /auth/login` para obter o token JWT. Inclua-o no header `Authorization: Bearer <token>`.
                        """,
                contact = @Contact(name = "João Roberto", url = "https://github.com/JoaoRobrt/AdoTEC")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido via POST /auth/login"
)
public class OpenApiConfig {
}
