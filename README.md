# Librishare - API (Backend)

[](https://www.java.com)
[](https://spring.io/projects/spring-boot)
[](https://maven.apache.org/)
[](https://www.docker.com/)

Este repositório contém o código-fonte da API REST do projeto Librishare, um sistema de gerenciamento e compartilhamento de livros.

## Índice

  - [Pré-requisitos](#pré-requisitos)
  - [Como Rodar (Docker)](#como-rodar-com-docker-compose)
  - [Desenvolvimento Local](#desenvolvimento-local)
      - [Rodando Testes](#rodando-testes)
      - [Análise de Qualidade (Lint)](#análise-de-qualidade-e-lint)
  - [Tecnologias Utilizadas](#tecnologias-utilizadas)

## Pré-requisitos

Antes de começar, garanta que você tenha as seguintes ferramentas instaladas:

  * Java (JDK) 17 ou superior
  * Docker
  * Docker Compose

## Como Rodar com Docker Compose

Esta é a forma recomendada de executar o projeto, pois ela gerencia a API e o banco de dados PostgreSQL automaticamente.

1.  Clone este repositório:

    ```bash
    git clone git@github.com:LibriShare/libri-share-back-end.git
    cd libri-share-back-end
    ```

2.  Navegue até a pasta `backend`, que contém o arquivo `docker-compose.yml`:

    ```bash
    cd backend
    ```

3.  **(Primeira vez ou se houver mudanças no código)**
    Execute o `build` para construir a imagem da aplicação:

    ```bash
    docker compose build
    ```

4.  Inicie os serviços (API e Banco de Dados):

    ```bash
    docker compose up
    ```

A API estará disponível em [http://localhost:8080](http://localhost:8080).

Para parar todos os serviços, pressione `Ctrl + C` no terminal ou execute `docker compose down` em outro terminal.

## Desenvolvimento Local

Para rodar os comandos de teste ou verificação, certifique-se de estar na pasta `backend/`, onde o `mvnw` (Maven Wrapper) está localizado.

### Rodando Testes

Para executar a suíte de testes unitários e de integração, utilize o seguinte comando:

```bash
# Estando na pasta 'backend/'
./mvnw test
```

### Análise de Qualidade e Lint

Para garantir a qualidade e a padronização do código, o projeto está configurado com três ferramentas de análise estática (Lint):

  * **Checkstyle:** Garante o padrão de formatação e estilo (baseado no Google Style).
  * **PMD:** Encontra "maus cheiros" (code smells), como código duplicado, complexidade desnecessária ou variáveis não utilizadas.
  * **SpotBugs:** Detecta bugs em potencial, como possíveis NullPointerExceptions ou recursos não fechados.

#### Verificando o "Nota 10"

Para rodar todas as verificações (incluindo os testes), execute:

```bash
# Estando na pasta 'backend/'
./mvnw clean verify
```

Se o comando terminar com **`[INFO] BUILD SUCCESS`**, seu código passou em todas as verificações\!

Se ele falhar com **`[INFO] BUILD FAILURE`**, significa que um dos linters encontrou um problema que precisa ser corrigido.

#### Gerando Relatórios de Erros

Para ver um relatório detalhado em HTML de todas as violações de Lint, execute:

```bash
# Estando na pasta 'backend/'
./mvnw clean site
```

Após a execução, abra a pasta `backend/target/site/` no seu explorador de arquivos e abra os seguintes arquivos no seu navegador para ver os problemas:

  * `checkstyle.html` (Erros de estilo)
  * `pmd.html` (Erros de "code smells")
  * `spotbugs.html` (Bugs em potencial)

## Tecnologias Utilizadas

  * **Java 17**
  * **Spring Boot 3.5.5** (Web, Data JPA, Validation)
  * **PostgreSQL** (Banco de Dados)
  * **Maven** (Gerenciador de dependências)
  * **Docker** (Contêineres)
  * **Flyway** (Migrações de banco de dados)
  * **Lombok** (Redução de boilerplate)
  * **ModelMapper** (Mapeamento de DTOs)
  * **SpringDoc (Swagger)** (Documentação da API)
