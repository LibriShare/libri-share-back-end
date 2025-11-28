
# 🧱 LibriShare - Back-end

> Uma API REST robusta e escalável desenvolvida para gerenciar dados de usuários, acervo de livros, status de leitura e histórico de empréstimos, garantindo integridade e performance para a plataforma LibriShare.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D)](https://swagger.io/)

---

## ✨ Funcionalidades

Esta API fornece os endpoints necessários para o funcionamento completo do ecossistema LibriShare:

* **👤 Gestão de Usuários:** Cadastro, autenticação (suporte a OAuth2/Google) e perfis de usuário.
* **📚 Catálogo Global:** Cadastro e busca de livros no sistema, com validação de ISBN e Google Books ID.
* **🔖 Biblioteca Pessoal:** Gerenciamento de estantes (*Lendo, Lido, Para Ler*), avaliações (0-5 estrelas) e progresso de páginas.
* **🤝 Sistema de Empréstimos:** Controle total de quem está com seus livros, datas de devolução e status (*Ativo/Devolvido*).
* **🕰️ Histórico de Atividades:** Registro automático de ações (adicionou livro, emprestou, devolveu) para timeline do usuário.

---

## 🛠️ Tecnologias

O projeto segue uma arquitetura em camadas (Controller, Service, Repository) utilizando as melhores práticas do mercado:

-   **[Java 17](https://www.oracle.com/java/)**: Linguagem base (LTS).
-   **[Spring Boot 3](https://spring.io/projects/spring-boot)**: Framework principal (Web, Data JPA, Security, Validation).
-   **[PostgreSQL](https://www.postgresql.org/)**: Banco de dados relacional robusto.
-   **[Flyway](https://flywaydb.org/)**: Versionamento e migração segura de banco de dados.
-   **[SpringDoc / Swagger](https://springdoc.org/)**: Documentação viva e interativa da API.
-   **[JUnit 5 & Mockito](https://junit.org/junit5/)**: Testes unitários e de integração confiáveis.
-   **[Docker](https://www.docker.com/)**: Containerização completa da aplicação e banco de dados.

---

## 📖 Documentação da API

A API é auto-documentada utilizando o padrão **OpenAPI 3**.
Após iniciar a aplicação, você pode acessar a interface interativa do Swagger para testar os endpoints:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## 🚀 Como Rodar Localmente

A maneira mais simples de rodar o projeto é utilizando **Docker Compose**, que sobe tanto a API quanto o banco de dados PostgreSQL configurados automaticamente.

### Pré-requisitos

* Docker e Docker Compose instalados.
* (Opcional) Java 17 e Maven para rodar fora do Docker.

### Passo a Passo (Docker)

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/LibriShare/libri-share-back-end.git
    cd libri-share-back-end/backend
    ```

2.  **Suba os containers:**
    Isso irá compilar o projeto, criar a imagem e iniciar o banco de dados.
    ```bash
    docker compose up --build
    ```

3.  **Pronto!** A API estará rodando em `http://localhost:8080`.

### Passo a Passo (Desenvolvimento/Testes)

Se você quiser rodar os testes ou a análise estática de código localmente (sem subir o container da aplicação):

1.  **Rodar Testes (Unitários e Integração):**
    O projeto utiliza um banco H2 em memória para testes, então não precisa do Postgres rodando para isso.
    ```bash
    ./mvnw test
    ```

2.  **Verificar Qualidade do Código (Lint):**
    O projeto utiliza **Checkstyle**, **PMD** e **SpotBugs** para garantir o padrão "Nota 10".
    ```bash
    ./mvnw clean verify
    ```
    *Se o build passar, seu código está limpo e seguro!*

---

## 📂 Estrutura do Projeto

O código está organizado por módulos de domínio para facilitar a manutenção:

```bash
src/main/java/com/librishare/backend/
├── config/             # Configurações globais (Security, Mapper, Swagger)
├── exception/          # Tratamento global de erros (ControllerAdvice)
└── modules/            # Módulos de domínio
    ├── book/           # Entidades e lógica do Catálogo Global
    ├── history/        # Logs de atividade do usuário
    ├── library/        # Gestão da estante pessoal (vínculo User-Book)
    ├── loan/           # Regras de negócio de Empréstimos
    └── user/           # Gestão de contas e autenticação
````

-----

## 🧪 Qualidade de Código

Utilizamos ferramentas de análise estática configuradas no pipeline de build:

| Ferramenta | Função |
| :--- | :--- |
| **Checkstyle** | Garante a formatação (Google Style Guide). |
| **PMD** | Encontra "code smells" e complexidade desnecessária. |
| **SpotBugs** | Detecta bugs em potencial e falhas de segurança. |

Para gerar um relatório HTML detalhado das análises:

```bash
./mvnw clean site
```

*(Abra `target/site/index.html` no navegador)*
