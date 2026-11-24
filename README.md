# API de Alunos — Spring Boot

Projeto prático do **Trabalho Individual de Pesquisa e Aplicação — Aulas 10 e 11**, da disciplina **Backend Frameworks**.

**Aluno:** Luiz Gustavo Freire Barbosa  
**Data:** 23 de novembro de 2026  
**Ambiente de desenvolvimento:** Windows, Visual Studio Code e PowerShell

## 1. Objetivo

Implementar e demonstrar testes automatizados, empacotamento executável, configuração externa, conteinerização e observabilidade em uma API REST Spring Boot de cadastro de alunos.

## 2. Tecnologias

- Java 17 como versão-alvo de compilação (execução local validada com Java 21)
- Spring Boot 3.3.5
- Maven
- Spring Web e Spring Data JPA
- PostgreSQL 16
- JUnit 5, Mockito e MockMvc
- Docker e Docker Compose
- Spring Boot Actuator
- SLF4J para logs

## 3. Funcionalidades

A API oferece operações de cadastro, listagem, consulta por ID, atualização e exclusão de alunos. Os dados de cada aluno incluem `id`, `nome`, `email`, `curso` e `matricula`.

O serviço valida o preenchimento dos campos obrigatórios, utiliza `AlunoRepository` para persistência e lança `RecursoNaoEncontradoException` quando o aluno solicitado não existe. A aplicação registra logs informativos das operações de cadastro, atualização e remoção.

## 4. Estrutura do projeto

```text
api-alunos-entrega-completa/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/application.properties
│   └── test/java/
├── .gitignore
├── compose.yaml
├── Dockerfile
├── pom.xml
├── README.md
└── RELATORIO.md
```

A pasta `target/` é gerada automaticamente pelo Maven e não precisa ser incluída no ZIP de código-fonte, pois pode ser recriada com `mvn clean package`.

## 5. Pré-requisitos

- JDK 17 ou superior, compatível com o projeto
- Maven disponível no terminal
- Docker Desktop com Docker Compose, para execução conteinerizada
- PowerShell, para os exemplos de comandos no Windows

Para conferir as ferramentas:

```powershell
java -version
mvn -version
docker --version
docker compose version
```

## 6. Testes automatizados

Na pasta raiz do projeto:

```powershell
mvn clean test
```

Os testes utilizam JUnit 5, Mockito e MockMvc. O Service é testado com dependências simuladas, enquanto o Controller é testado com requisição HTTP simulada.

**Resultado confirmado em 23/11/2026:**

```text
AlunoControllerTest: 1 teste aprovado
AlunoServiceTest:    4 testes aprovados
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 7. Gerar o JAR executável

```powershell
mvn clean package
```

O comando compila a aplicação, executa os testes e produz:

```text
target/api-alunos-0.0.1-SNAPSHOT.jar
```

O JAR contém as dependências necessárias à aplicação Spring Boot e pode ser executado diretamente pela JVM. **O banco de dados continua sendo uma dependência externa.**

## 8. Executar com Docker Compose

Inicie o Docker Desktop e, na pasta raiz do projeto, execute:

```powershell
mvn clean package
docker compose up --build -d
```

O `compose.yaml` define os serviços `postgres` e `api`. A API aguarda o healthcheck do PostgreSQL antes de iniciar.

| Componente | Acesso no Windows | Acesso dentro da rede Docker |
|---|---|---|
| API Spring Boot | `http://localhost:8080` | `api:8080` |
| PostgreSQL | `localhost:5433` | `postgres:5432` |

O banco utilizado chama-se `backend_frameworks`. O volume `postgres_data` preserva seus dados após a recriação dos containers.

Verifique os serviços:

```powershell
docker compose ps
```

Acompanhe os logs:

```powershell
docker compose logs -f api
```

Verifique a saúde da aplicação:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Resultado observado durante a validação: `UP` (resposta JSON `{"status":"UP"}`).

Para parar e remover os containers sem apagar o volume:

```powershell
docker compose down
```

**Atenção:** `docker compose down -v` também remove o volume do banco e seus dados. Evite esse comando se desejar preservar os registros.

### Credenciais do ambiente de demonstração

O Compose usa banco `backend_frameworks`, usuário `postgres` e senha demonstrativa `postgres`. Esses valores servem **somente para desenvolvimento local**. Em ambientes reais, não mantenha senhas fixas no arquivo Compose; utilize gerenciamento apropriado de segredos e configurações externas.

## 9. Executar o JAR pelo PowerShell

O JAR foi validado fora da execução da IDE, utilizando o PostgreSQL disponibilizado pelo Docker. Com o serviço `postgres` ativo e a porta `5433` publicada, execute:

```powershell
$env:DB_URL="jdbc:postgresql://127.0.0.1:5433/backend_frameworks"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
java -jar .\target\api-alunos-0.0.1-SNAPSHOT.jar --server.port=8081
```

Nesse modo, a API fica em `http://localhost:8081`, evitando conflito com a API do Docker, que utiliza a porta `8080`.

Para encerrar a aplicação iniciada pelo comando `java -jar`, pressione `Ctrl+C` no terminal correspondente **antes de executar novamente `mvn clean package`**. No Windows, um JAR ainda em execução pode impedir a exclusão do arquivo durante a fase `clean`.

O `application.properties` usa:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/backend_frameworks}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
```

O endereço `localhost:5432` é apenas o valor padrão da URL. Ao usar o PostgreSQL publicado pelo Compose no Windows, configure `DB_URL` para a porta `5433`, como no exemplo acima.

## 10. Endpoints

| Método | Rota | Finalidade | Resposta de sucesso |
|---|---|---|---|
| POST | `/alunos` | Cadastrar aluno | `201 Created` |
| GET | `/alunos` | Listar alunos | `200 OK` |
| GET | `/alunos/{id}` | Consultar aluno | `200 OK` |
| PUT | `/alunos/{id}` | Atualizar aluno | `200 OK` |
| DELETE | `/alunos/{id}` | Excluir aluno | `204 No Content` |
| GET | `/actuator/health` | Consultar saúde | `200 OK`, quando saudável |
| GET | `/actuator/info` | Consultar informações expostas | `200 OK` |

### Exemplos no PowerShell

Os exemplos a seguir usam a API do Docker na porta `8080`. Para testar o JAR independente, substitua `8080` por `8081`.

**Cadastrar:**

```powershell
$body = @{
    nome = "Ana Souza"
    email = "ana@email.com"
    curso = "Ciencia da Computacao"
    matricula = "20260001"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/alunos" -Method Post -ContentType "application/json" -Body $body
```

**Listar:**

```powershell
Invoke-RestMethod http://localhost:8080/alunos
```

**Consultar por ID:**

```powershell
Invoke-RestMethod http://localhost:8080/alunos/1
```

**Atualizar:**

```powershell
$atualizacao = @{
    nome = "Ana Souza Atualizada"
    email = "ana.atualizada@email.com"
    curso = "Engenharia de Software"
    matricula = "20260001"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/alunos/1" -Method Put -ContentType "application/json" -Body $atualizacao
```

**Excluir:**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/alunos/1" -Method Delete
```

Os exemplos de consulta, atualização e exclusão pressupõem que o aluno de ID `1` exista. Caso o banco já contenha outros registros, use o ID retornado no cadastro.

## 11. Logs e observabilidade

A classe `AlunoService` utiliza SLF4J para registrar eventos como:

```text
Aluno cadastrado com id=1
Aluno atualizado com id=1
Aluno removido com id=1
```

Esses logs registram a operação e o identificador, sem registrar credenciais. O Actuator expõe somente `health` e `info`; os detalhes de saúde estão configurados como ocultos:

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
```

O status `UP` informa que os indicadores de saúde disponíveis estão positivos, mas não substitui os testes funcionais e automatizados.

## 12. Resultados da validação

Em 23/11/2026, foram confirmados:

- `mvn clean package`: **BUILD SUCCESS**;
- 5 testes automatizados executados, sem falhas, erros ou testes ignorados;
- geração do JAR `api-alunos-0.0.1-SNAPSHOT.jar`;
- execução do JAR pelo PowerShell na porta `8081`;
- execução da API e do PostgreSQL pelo Docker Compose;
- resposta `UP` do Actuator;
- cadastro (`201`), consulta (`200`), atualização (`200`) e exclusão (`204`) de aluno;
- listagem final vazia (`[]`) após a exclusão.

## 13. Entrega

O trabalho será entregue como projeto local compactado em ZIP, contendo código-fonte, testes, `pom.xml`, `Dockerfile`, `compose.yaml`, `README.md` e `RELATORIO.md`. Não é necessária publicação no GitHub para reproduzir os comandos descritos aqui.

## Evidências de execução

As capturas de tela dos testes e da execução estão disponíveis no GitHub:

1. [Testes automatizados — 5 aprovados](evidencias/01-testes.png)
2. [Execução do JAR](evidencias/02-jar-executavel.png)
3. [Containers Docker e PostgreSQL](evidencias/03-docker-containers.png)
4. [Spring Boot Actuator — Health UP](evidencias/04-actuator-health.png)

