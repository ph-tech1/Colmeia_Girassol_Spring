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

Depois, execute [`procedures_operacionais.sql`](src/main/java/br/edu/fatec/projetointerdisciplinar/db/procedures_operacionais.sql) no mesmo SQL Server. Esse script cria ou atualiza as três procedures chamadas pela aplicação: decisão de matrícula, atribuição de turma e registro/atualização de frequência. Se o banco já existe, basta executar esse segundo script para instalar as procedures.

Execute também [`views_relatorios.sql`](src/main/java/br/edu/fatec/projetointerdisciplinar/db/views_relatorios.sql) no mesmo banco. Ele cria ou atualiza as três views consultadas pelo Java:

- `vw_resumo_frequencia_aluno`: consolida, por criança, dias com frequência registrada, presenças, faltas, faltas justificadas e percentual de presença. O percentual considera todas as frequências já registradas para a criança.
- `vw_alunos_por_turma`: lista matrículas aprovadas e atribuídas a uma turma, com turma, professor, criança e contato principal do responsável. Alimenta os quadros “Minha Turma” e “Diário de bordo” do professor.
- `vw_diario_bordo_aluno`: combina dados da criança, matrícula, turma, aula, professor, presença e informações de rotina (humor, alimentação, sono, banheiro/fralda e observações). Alimenta o portal da família e o painel de detalhes da criança.

No portal da família, as consultas Java restringem frequência e diário às crianças vinculadas ao responsável autenticado. No painel do professor, o roster e os indicadores ficam limitados às turmas atribuídas ao professor autenticado.

#### Como as views são usadas pela aplicação

As views são consultadas pelo Java por meio de [`DatabaseViewRepository.java`](src/main/java/br/edu/fatec/projetointerdisciplinar/repository/DatabaseViewRepository.java), usando consultas parametrizadas. Os controllers carregam esses resultados para as telas:

| View | Consulta Java | Tela / informação apresentada |
|---|---|---|
| `vw_resumo_frequencia_aluno` | Filtrada pelo professor ou pelo vínculo do responsável; também pode ser filtrada por criança vinculada | Painel do professor: resumo por aluno. Portal do responsável e painel da criança: percentual, presenças, faltas e faltas justificadas |
| `vw_alunos_por_turma` | Filtrada pelo código do professor autenticado | Painel do professor: lista de turmas e alunos ativos, com contato principal do responsável |
| `vw_diario_bordo_aluno` | Filtrada pelos vínculos do responsável autenticado e, no painel individual, pelo código da criança | Portal do responsável e painel da criança: data, atividade, turma, professor, presença e registros do dia |

O acesso às views não substitui a autenticação: o Java aplica os filtros com os identificadores obtidos da sessão autenticada, para que o responsável consulte apenas crianças vinculadas à sua conta e o professor apenas suas turmas. Para instalar ou atualizar as views em um banco já criado, execute novamente `views_relatorios.sql` no banco `banco_girassol`.

#### Fluxo de aprovação e atribuição de turma

O fluxo administrativo foi separado em duas operações:

1. A matrícula pendente (`status = 0`) é encaminhada para análise (`status = 1`).
2. Após a análise, ela pode ser aprovada (`status = 2`) ou rejeitada (`status = 3`). A rejeição exige um motivo.
3. Depois de aprovada, a turma é atribuída em uma operação própria. Portanto, **a aprovação não exige que a matrícula já tenha uma turma**.

Essa sequência é implementada por `dbo.sp_decidir_matricula` e `dbo.sp_atribuir_turma_matricula`, chamadas pelo Java. A procedure de decisão do script do projeto não contém a regra de exigir turma para aprovar.

Se, ao aprovar, o sistema apresentar `Não é possível aprovar uma matrícula sem turma` (no log observado, erro SQL Server `50016`), a instância do banco está executando outra definição — por exemplo, uma procedure desatualizada ou um trigger com essa validação. No SSMS, confirme que a janela está conectada ao banco `banco_girassol` e execute novamente `procedures_operacionais.sql`. Para localizar a definição que contém a mensagem:

```sql
USE banco_girassol;
GO

SELECT
    o.type_desc AS tipo_objeto,
    SCHEMA_NAME(o.schema_id) AS esquema,
    o.name AS objeto,
    m.definition AS definicao
FROM sys.sql_modules AS m
JOIN sys.objects AS o ON o.object_id = m.object_id
WHERE m.definition LIKE N'%50016%'
   OR m.definition LIKE N'%Não é possível aprovar uma matrícula sem turma%';
```

Se a consulta retornar uma procedure, atualize-a executando o script do projeto. Se retornar um trigger, revise a regra desse trigger: ele não deve impedir a aprovação sem turma, pois a turma é atribuída na etapa seguinte. Depois, repita o fluxo pela aplicação.

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
