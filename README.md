# Colmeia Girassol

Aplicação web para apoiar a rotina de uma escola infantil: apresenta a escola ao público e oferece áreas autenticadas para responsáveis, professores e administração acompanharem alunos, matrículas, turmas e atividades pedagógicas.

## Funcionalidades

- **Site institucional:** páginas de início e sobre a escola, com informações sobre a proposta pedagógica e as turmas.
- **Portal de acesso:** autenticação por CPF e senha, com redirecionamento para a área correspondente ao perfil.
- **Área do responsável:** gerenciamento de perfil, cadastro de crianças e pessoas autorizadas, consulta de informações e acompanhamento de matrícula.
- **Área do professor:** visualização de informações pedagógicas, planejamento de aulas, registro de frequência e diário de bordo.
- **Área administrativa:** gerenciamento de professores, turmas, alunos e matrículas.
- **Pré-matrícula:** envio de solicitação pelo formulário público.
- **Interface responsiva:** templates Thymeleaf, CSS e JavaScript servidos pela própria aplicação.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring MVC, Spring Security, Spring Data JPA e Thymeleaf
- Microsoft SQL Server e driver JDBC da Microsoft
- Maven Wrapper (Maven 3.9.16)
- HTML, CSS e JavaScript

## Requisitos

- JDK 21
- Docker com suporte a contêineres Linux **ou** uma instância do Microsoft SQL Server
- Acesso à internet para baixar dependências Maven na primeira compilação

## Como executar localmente

### 1. Inicie o SQL Server

Com Docker, inicie uma instância local do SQL Server 2022. Defina uma senha forte que atenda aos requisitos do SQL Server:

```powershell
docker run -e "ACCEPT_EULA=Y" `
  -e "MSSQL_SA_PASSWORD=<sua-senha-forte>" `
  -p 1433:1433 `
  --name sqlserver `
  -d mcr.microsoft.com/mssql/server:2022-latest
```

Se já houver um contêiner chamado `sqlserver`, inicie-o com `docker start sqlserver` em vez de criar outro.

### 2. Crie o banco e as tabelas

Execute o script [`banco_girassol _novo_16_05 (1).sql`](src/main/java/br/edu/fatec/projetointerdisciplinar/db/banco_girassol%20_novo_16_05%20(1).sql) em uma ferramenta compatível com scripts do SQL Server, como SQL Server Management Studio ou `sqlcmd`. O script cria o banco `banco_girassol` e seu esquema.

### 3. Configure a conexão

A configuração local padrão aponta para `localhost:1433`, banco `banco_girassol` e usuário `sa`. Informe a senha sem gravá-la no repositório. No PowerShell, por exemplo:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "<sua-senha-forte>"
```

Se a instância usar outro endereço, banco ou usuário, configure também `SPRING_DATASOURCE_URL` e `SPRING_DATASOURCE_USERNAME`. As variáveis valem para a sessão atual do PowerShell. A URL JDBC deve manter as opções TLS compatíveis com o ambiente (`encrypt` e `trustServerCertificate`).

### 4. (Opcional) Cadastre um administrador inicial

O arquivo [`insert_ADM.sql`](src/main/java/br/edu/fatec/projetointerdisciplinar/db/insert_ADM.sql) contém um exemplo de inserção de administrador. Revise e substitua os dados de exemplo antes de executá-lo; o script inclui um hash BCrypt, não uma senha em texto simples. Use um CPF e uma senha que você conhece e mantenha os dados reais fora do controle de versão.

O login da aplicação usa **CPF e senha**. Sem um usuário com perfil ativo e senha cadastrada, não será possível acessar as áreas protegidas.

### 5. Inicie a aplicação

Na raiz do repositório:

```powershell
.\mvnw.cmd spring-boot:run
```

Acesse [http://localhost:8080](http://localhost:8080). Para encerrar, use `Ctrl+C` no terminal.

## Testes e pacote executável

Execute os testes automatizados:

```powershell
.\mvnw.cmd test
```

Gere o WAR executável:

```powershell
.\mvnw.cmd clean package
```

O artefato é gerado em `target/projeto-interdisciplinar-0.0.1-SNAPSHOT.war`. Para executá-lo:

```powershell
java -jar target/projeto-interdisciplinar-0.0.1-SNAPSHOT.war
```

Configure as variáveis de conexão com o banco na mesma sessão antes de iniciar o WAR.

## Perfis de acesso

| Perfil | Área principal | Exemplos de recursos |
|---|---|---|
| Responsável | Portal da família | Perfil, crianças, matrícula e pessoas autorizadas |
| Professor | Painel pedagógico | Planejamentos, frequência e diário de bordo |
| Administrador | Painel administrativo | Professores, turmas e matrículas |
| Secretaria | Operações de secretaria | Consulta e atualização de pessoas autorizadas |

As permissões são aplicadas no servidor pelo Spring Security. Algumas rotas de API, como o envio de pré-matrícula, são públicas; as demais dependem do perfil e da operação autorizada. Consulte `SecurityConfig` e os controllers para os detalhes de cada rota.

## API

As APIs usam o prefixo `/api`. Entre os recursos disponíveis estão:

| Recurso | Prefixo |
|---|---|
| Pré-matrículas | `/api/pre-matriculas` |
| Alunos e pessoas | `/api/alunos`, `/api/pessoas` |
| Responsáveis e vínculos | `/api/responsaveis`, `/api/responsaveis-alunos` |
| Professores e turmas | `/api/professores`, `/api/turmas`, `/api/admin/professores` |
| Matrículas | `/api/matriculas` |
| Frequência e diário de bordo | `/api/frequencias`, `/api/diarios-bordo` |
| Planejamento de aulas | `/api/aulas-planejamentos`, `/api/professor/dashboard` |
| Portal do responsável | `/api/portal-responsavel` |
| Pessoas autorizadas | `/api/autorizados`, `/api/autorizados-busca` |

As rotas aceitam os métodos HTTP definidos em seus controllers; não presuma que todos os recursos oferecem operações CRUD completas.

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/br/edu/fatec/projetointerdisciplinar/
│   │   ├── config/        # Segurança e configuração
│   │   ├── controller/    # Páginas e endpoints HTTP
│   │   ├── dto/           # Objetos de entrada e saída
│   │   ├── model/         # Entidades JPA
│   │   ├── repository/    # Persistência
│   │   ├── service/       # Regras de negócio
│   │   └── db/            # Scripts SQL de criação e carga
│   └── resources/
│       ├── templates/     # Páginas Thymeleaf
│       ├── static/        # CSS, JavaScript e imagens
│       └── application.properties
└── test/                  # Testes automatizados
```

## Configurações e cuidados

- A configuração padrão é voltada ao desenvolvimento local. Não use credenciais de desenvolvimento em produção.
- Armazene senhas e demais segredos em variáveis de ambiente ou em um gerenciador de segredos; nunca os versione.
- A conexão SQL Server local está configurada com `trustServerCertificate=true`. Revise essa opção e a configuração TLS antes de expor a aplicação fora de um ambiente local.
- A interface carrega bibliotecas visuais de CDNs externos; a disponibilidade dessas bibliotecas depende do acesso à internet do navegador.
