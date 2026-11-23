# Trabalho Individual — Testes, Docker e Observabilidade em Spring Boot

**Aluno:** Luiz Gustavo Freire Barbosa  
**Disciplina:** Backend Frameworks  
**Projeto:** API de Alunos  
**Data:** 23 de novembro de 2026  
**Ambiente:** Windows, Visual Studio Code, PowerShell, Maven, Java e Docker Desktop

## 1. Introdução

Uma API não se torna adequada para diferentes ambientes apenas porque funciona no computador em que foi criada. Para que seu comportamento possa ser verificado e sua execução reproduzida, são necessários testes automatizados, configuração externa, empacotamento executável, infraestrutura de apoio e recursos de observabilidade.

Este trabalho apresenta uma pesquisa aplicada sobre os conteúdos das Aulas 10 e 11 da disciplina Backend Frameworks. O estudo foi desenvolvido a partir de uma API REST de cadastro de alunos construída com Spring Boot. O objetivo foi compreender e demonstrar testes com JUnit 5, Mockito e MockMvc; geração e execução de JAR; uso de variáveis de ambiente; conteinerização com Docker; orquestração com Docker Compose; logs; e monitoramento básico por Spring Boot Actuator.

## 2. Testes automatizados

Teste automatizado é um código que executa outro código e compara o comportamento observado com um resultado esperado. Diferentemente de testes manuais, que exigem repetição de passos por uma pessoa, testes automatizados podem ser executados novamente após mudanças na aplicação. Isso ajuda a identificar regressões e reduz a dependência de verificações exclusivamente manuais.

Uma organização comum é **Arrange, Act, Assert**:

1. **Arrange:** preparar os dados e as dependências necessárias;
2. **Act:** executar a ação testada;
3. **Assert:** verificar se o resultado corresponde ao esperado.

### 2.1 JUnit 5

O JUnit 5 oferece recursos para estruturar e executar testes em Java. Entre os elementos utilizados em testes estão `@Test`, para identificar métodos de teste; `assertEquals`, para comparar valores; e `assertThrows`, para verificar a ocorrência de exceções esperadas.

### 2.2 Mockito

O Mockito permite criar objetos simulados, denominados *mocks*, para substituir dependências durante testes unitários. Na API de Alunos, o `AlunoRepository` pode ser simulado para que a lógica de `AlunoService` seja avaliada sem depender da disponibilidade do PostgreSQL.

As anotações `@Mock` e `@InjectMocks` ajudam a criar a dependência simulada e a injetá-la na classe testada. A construção `when(...).thenReturn(...)` configura uma resposta do mock, enquanto `verify(...)` permite confirmar uma interação. Sem configuração específica, chamadas a métodos simulados podem retornar valores padrão, como `null`, `0` ou `false`, conforme o tipo de retorno.

### 2.3 Exemplo de teste de Service

O exemplo a seguir ilustra a estrutura de um teste unitário para cadastro:

```java
@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {
    @Mock
    private AlunoRepository repository;

    @InjectMocks
    private AlunoService service;

    @Test
    void deveCadastrarAlunoValido() {
        // Arrange: prepara o aluno e o retorno do repositório simulado.
        Aluno aluno = alunoValido();
        Aluno salvo = new Aluno(1L, "Ana Souza", "ana@email.com",
                "Ciência da Computação", "20260001");
        when(repository.save(aluno)).thenReturn(salvo);

        // Act: executa o método de negócio.
        Aluno resultado = service.salvar(aluno);

        // Assert: confere o resultado e a interação com o repositório.
        assertEquals(1L, resultado.getId());
        verify(repository).save(aluno);
    }
}
```

O exemplo demonstra a técnica de isolamento de dependências. No projeto, os testes de Service abrangem cadastro válido, busca de aluno existente, tentativa de cadastro com nome vazio e busca de aluno inexistente.

### 2.4 MockMvc e testes de Controller

Enquanto os testes de Service verificam a lógica de negócio, testes de Controller verificam o comportamento da camada HTTP, como rota, status e corpo da resposta. O MockMvc permite simular requisições à aplicação Spring MVC sem iniciar um servidor HTTP real.

No projeto, `AlunoControllerTest` verifica o cadastro por `POST /alunos`, com resposta HTTP `201 Created`.

A existência de testes automatizados não garante a ausência de defeitos. Mesmo uma alta cobertura de código não significa que todos os cenários relevantes foram avaliados. Por isso, testes unitários devem ser complementados por verificações funcionais e de integração quando necessário.

## 3. Build e JAR executável

O *build* reúne etapas de compilação, processamento de recursos, execução de testes e geração do artefato da aplicação. O Maven automatiza essas etapas a partir das configurações presentes no `pom.xml`.

O comando utilizado foi:

```powershell
mvn clean package
```

A fase `clean` remove os resultados de builds anteriores da pasta `target`. A fase `package` compila a aplicação, executa os testes e gera o JAR. Neste projeto, o Spring Boot Maven Plugin reorganiza o artefato para incluir as dependências necessárias à execução da aplicação.

O arquivo gerado foi:

```text
target/api-alunos-0.0.1-SNAPSHOT.jar
```

O JAR pode ser iniciado diretamente pela JVM:

```powershell
java -jar .\target\api-alunos-0.0.1-SNAPSHOT.jar
```

O Spring Boot utiliza um servidor web embutido, eliminando a necessidade de iniciar a aplicação pela IDE. Entretanto, a execução do JAR **não elimina a dependência do banco de dados**: a API continua precisando de uma instância PostgreSQL acessível.

A compilação foi configurada para Java 17, e a execução local foi validada utilizando Java 21.

## 4. Configuração externa e ambientes

Configuração externa permite que valores específicos do ambiente sejam informados sem alterações no código-fonte. Isso é especialmente importante para endereços de banco de dados, usuários e senhas.

No projeto, o arquivo `application.properties` utiliza:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/backend_frameworks}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
management.info.env.enabled=true
info.app.name=API de Alunos
info.app.description=API REST para cadastro de alunos

logging.level.com.exemplo.apialunos=INFO
```

As variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` são fornecidas pelo ambiente de execução. A URL e o usuário possuem valores padrão de desenvolvimento; a senha deve ser informada externamente.

Os *profiles* do Spring Boot são um recurso que permite separar configurações para ambientes como desenvolvimento, testes e produção. Neste projeto, a diferenciação efetivamente demonstrada foi feita por variáveis de ambiente e argumentos de inicialização, sem necessidade de afirmar que existem arquivos específicos de profiles.

## 5. Docker e Docker Compose

Uma **imagem Docker** contém as instruções e os arquivos necessários para criar containers. Um **container** é uma instância executável dessa imagem, isolada em relação a outros processos e serviços.

O `Dockerfile` da API é:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/api-alunos-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

A instrução `FROM` define a imagem-base Java 17; `WORKDIR` seleciona o diretório de trabalho; `COPY` adiciona o JAR gerado pelo Maven; `EXPOSE` documenta a porta utilizada pela aplicação; e `ENTRYPOINT` define o comando de inicialização.

Como o Dockerfile copia o JAR da pasta `target`, é necessário gerar o artefato com Maven antes de construir a imagem.

### 5.1 Orquestração com Compose

O arquivo `compose.yaml` organiza dois serviços: `postgres` e `api`. O PostgreSQL utiliza a imagem `postgres:16-alpine`, um volume persistente e um healthcheck com `pg_isready`. A API é construída a partir do Dockerfile e só inicia depois que o serviço de banco atende à condição `service_healthy`.

A configuração final utilizada foi:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: api-alunos-db
    environment:
      POSTGRES_DB: backend_frameworks
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d backend_frameworks"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build: .
    container_name: api-alunos
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/backend_frameworks
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

A API acessa o banco pelo endereço `postgres:5432`, porque `postgres` é o nome do serviço na rede do Compose. Dentro do container da API, `localhost` se refere ao próprio container, e não ao container do banco.

No computador Windows, o PostgreSQL foi publicado na porta `5433`, enquanto a aplicação Spring Boot foi publicada na porta `8080`. Essa separação evita conflitos com outros serviços locais.

### 5.2 Persistência

O volume nomeado `postgres_data` armazena os dados do PostgreSQL fora da camada gravável do container. Assim, remover e recriar os containers não apaga automaticamente o banco. O comando `docker compose down` remove os containers e a rede do projeto, preservando o volume. Já `docker compose down -v` remove também os volumes definidos pelo Compose, o que pode causar perda de dados.

### 5.3 Segurança das credenciais

O Compose utiliza a senha demonstrativa `postgres` para facilitar a reprodução local. Essa prática **não deve ser adotada em produção**. Em ambientes reais, segredos devem ser gerenciados externamente e protegidos por mecanismos apropriados de acesso.

## 6. Logs e Spring Boot Actuator

Logs são registros de eventos relevantes durante a execução. Eles auxiliam no diagnóstico de problemas e na compreensão do comportamento da aplicação.

A classe `AlunoService` utiliza `Logger` e `LoggerFactory`, da API SLF4J, e registra operações como:

```text
Aluno cadastrado com id=1
Aluno atualizado com id=1
Aluno removido com id=1
```

O conteúdo registrado identifica a operação e o ID do aluno, sem expor senhas ou tokens. O cuidado com dados pessoais e credenciais é importante porque logs podem ser armazenados, enviados para ferramentas de monitoramento ou consultados por equipes operacionais.

O **Spring Boot Actuator** fornece endpoints para acompanhamento operacional. Neste projeto, estão expostos `health` e `info`. A configuração `management.endpoint.health.show-details=never` impede a exibição pública de detalhes internos do estado de saúde.

A consulta ao endpoint:

```text
GET /actuator/health
```

retornou:

```json
{"status":"UP"}
```

Esse resultado indica que os indicadores de saúde disponíveis estavam positivos no momento da consulta. Não comprova, por si só, que todas as regras de negócio funcionam corretamente, razão pela qual os testes automatizados e funcionais continuam necessários.

## 7. Solução do estudo de caso

| Problema identificado | Solução aplicada | Cuidados e limitações |
|---|---|---|
| Regras de negócio sem verificação repetível | Testes JUnit 5 e Mockito no Service | Testar cenários de sucesso e erro |
| Comportamento HTTP não verificado | Teste de Controller com MockMvc | Conferir rota, status e resposta |
| Dependência de execução pela IDE | Maven e JAR executável | Manter Java e banco compatíveis |
| Configuração dependente da máquina | Variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` | Proteger credenciais |
| Diferenças entre ambientes | Dockerfile com Java 17 | Gerar o JAR antes da imagem |
| Dependência do PostgreSQL | Compose com serviços `api` e `postgres` | Usar nome do serviço na rede Docker |
| Risco de perda de dados | Volume `postgres_data` | Evitar `down -v` sem necessidade |
| Dificuldade de diagnóstico | Logs com SLF4J | Não registrar segredos |
| Falta de indicador operacional | Actuator `/actuator/health` | Não confundir saúde com correção funcional |

## 8. Evidências de execução e resultados

A aplicação foi compilada, testada e executada em 23 de novembro de 2026, no Windows, por meio do Visual Studio Code e do PowerShell. Os resultados a seguir foram obtidos durante a validação prática.

### 8.1 Testes automatizados

O comando final executado foi:

```powershell
mvn clean package
```

O Maven compilou sete arquivos-fonte da aplicação e dois arquivos-fonte de testes, executou a suíte automatizada e gerou o JAR executável.

**Resumo do resultado:**

```text
AlunoControllerTest: Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
AlunoServiceTest:    Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 6.765 s
Finished at: 2026-11-23T20:41:12-03:00
```

A suíte terminou sem falhas, erros ou testes ignorados. Foram exibidos avisos relacionados ao carregamento dinâmico do agente Byte Buddy, mas eles não impediram a execução nem o empacotamento.

### 8.2 Geração e execução do JAR

O artefato gerado foi:

```text
target/api-alunos-0.0.1-SNAPSHOT.jar
```

A aplicação foi iniciada pelo PowerShell com variáveis de ambiente, conectando-se ao PostgreSQL disponibilizado pelo Docker na porta `5433` e utilizando a porta HTTP `8081`:

```powershell
$env:DB_URL="jdbc:postgresql://127.0.0.1:5433/backend_frameworks"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
java -jar .\target\api-alunos-0.0.1-SNAPSHOT.jar --server.port=8081
```

A execução independente da IDE foi confirmada por respostas HTTP da API e pelo endpoint de saúde. O teste também demonstrou a importância de encerrar o processo Java antes de repetir `mvn clean package`: em uma tentativa, o Windows impediu a exclusão do JAR ainda em uso; após encerrar o processo, o build foi concluído com sucesso.

### 8.3 Docker Compose e PostgreSQL

Os serviços `api` e `postgres` foram iniciados pelo Docker Compose. O banco passou pela verificação de saúde, e a aplicação conseguiu estabelecer conexão com o PostgreSQL.

As portas finais foram:

| Serviço | Porta no computador | Porta interna |
|---|---|---|
| API Spring Boot | `8080` | `8080` |
| PostgreSQL | `5433` | `5432` |

O acesso interno da API ao banco foi realizado pelo endereço `jdbc:postgresql://postgres:5432/backend_frameworks`.

### 8.4 Testes funcionais das operações CRUD

Após a inicialização do JAR na porta `8081`, foram realizadas requisições HTTP para verificar o comportamento da API:

| Operação | Endpoint | Resultado observado |
|---|---|---|
| Listagem inicial | `GET /alunos` | `200 OK`, lista vazia |
| Cadastro | `POST /alunos` | `201 Created`, aluno com ID 1 |
| Consulta por ID | `GET /alunos/1` | `200 OK`, aluno encontrado |
| Atualização | `PUT /alunos/1` | `200 OK`, dados atualizados |
| Exclusão | `DELETE /alunos/1` | `204 No Content` |
| Listagem após exclusão | `GET /alunos` | `200 OK`, corpo `[]` |

O aluno de teste foi cadastrado com nome, e-mail, curso e matrícula; depois teve seus dados alterados e foi excluído. A listagem final vazia confirmou que o registro não permanecia na coleção consultada.

Essas verificações comprovam os cenários executados, mas não substituem testes adicionais para concorrência, segurança, carga ou validações mais abrangentes.

### 8.5 Observabilidade

O endpoint `/actuator/health` retornou `{"status":"UP"}` durante a execução da aplicação. Também foram observados logs do serviço, incluindo registro de cadastro de aluno com ID.

Os resultados demonstram que o Actuator e os logs estão disponíveis como mecanismos básicos de acompanhamento da aplicação.

### 8.6 Evidências disponíveis

Os resultados de execução foram registrados no terminal durante o desenvolvimento. A entrega pode ser acompanhada de capturas de tela do build, dos testes, dos containers e do Actuator, caso sejam solicitadas pelo professor. Não se afirma que capturas adicionais foram produzidas ou anexadas quando isso não foi confirmado.

## 9. Discussão e limitações

A combinação de testes automatizados, JAR executável e Docker Compose melhora a reprodutibilidade do projeto. O Maven oferece uma sequência padronizada de build; o Mockito permite verificar regras sem depender do banco; o Compose facilita a execução conjunta da API e do PostgreSQL; e o Actuator oferece uma indicação operacional rápida.

Entretanto, algumas limitações permanecem. As validações implementadas verificam principalmente o preenchimento dos campos, não todas as regras possíveis de formato ou unicidade. A suíte automatizada contém cinco testes, o que demonstra cenários importantes, mas não representa cobertura integral da aplicação. O ambiente Compose utiliza credenciais demonstrativas e não foi configurado como implantação de produção. O uso de `spring.jpa.hibernate.ddl-auto=update` é adequado à demonstração, mas ambientes de produção normalmente exigem controle explícito de migrações do esquema do banco.

Essas limitações não invalidam os objetivos do trabalho: elas delimitam o alcance dos resultados apresentados.

## 10. Conclusão

O desenvolvimento e a validação da API de Alunos permitiram aplicar os conteúdos das Aulas 10 e 11 em um projeto Spring Boot funcional. A suíte com JUnit 5, Mockito e MockMvc executou cinco testes sem falhas. O Maven gerou um JAR executável, iniciado diretamente pelo PowerShell. O Docker Compose disponibilizou a API e o PostgreSQL com verificação de saúde do banco e persistência por volume. As variáveis de ambiente permitiram adaptar a conexão entre a execução local e a rede Docker. Por fim, os logs e o Actuator ofereceram mecanismos básicos de observabilidade.

Os testes funcionais confirmaram as operações de cadastro, consulta, atualização e exclusão de alunos. O trabalho demonstrou, portanto, como práticas de teste, empacotamento, configuração e monitoramento contribuem para tornar uma API mais verificável, reproduzível e fácil de operar.

## 11. Referências

- SPRING. *Spring Boot Reference Documentation*. Disponível em: https://docs.spring.io/spring-boot/documentation.html. Acesso em: 23 nov. 2026.
- SPRING. *Testing in Spring Boot*. Disponível em: https://docs.spring.io/spring-boot/reference/testing/. Acesso em: 23 nov. 2026.
- JUNIT. *JUnit 5 User Guide*. Disponível em: https://junit.org/junit5/docs/current/user-guide/. Acesso em: 23 nov. 2026.
- MOCKITO. *Mockito documentation*. Disponível em: https://site.mockito.org/. Acesso em: 23 nov. 2026.
- DOCKER. *Docker Docs*. Disponível em: https://docs.docker.com/. Acesso em: 23 nov. 2026.

## 12. Identificação e forma de entrega

**Aluno:** Luiz Gustavo Freire Barbosa  
**Disciplina:** Backend Frameworks  
**Trabalho:** Trabalho Individual de Pesquisa e Aplicação — Aulas 10 e 11  
**Projeto:** API de Alunos  
**Data da validação:** 23 de novembro de 2026  
**Ferramentas:** Visual Studio Code, PowerShell, Maven, Java 21, Docker Desktop, PostgreSQL 16 e Spring Boot 3.3.5

**Forma de entrega:** projeto compactado em ZIP, contendo o código-fonte, os testes automatizados, o arquivo `pom.xml`, o `Dockerfile`, o `compose.yaml`, o `README.md` e este `RELATORIO.md`. Não há dependência de publicação no GitHub para a execução ou avaliação local do projeto.
