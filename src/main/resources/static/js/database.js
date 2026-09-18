// =============================================================================
// BANCO DE DADOS SIMULADO - SINCRONIZADO COM App.java
// Estrutura fiel às entidades Java do projeto Colmeia Girassol
// =============================================================================

// Utilitários de Data Dinâmica para manter o sistema atualizado em 2026 e nos anos seguintes
const currentYearStr = new Date().getFullYear().toString();
const getRelativeDate = (offsetDays) => {
    const d = new Date();
    d.setDate(d.getDate() + offsetDays);
    return d.toISOString().split('T')[0];
};

window.db_mock = {

    responsaveis: [
        { id: 1,  nome: "Silmara",         cpf: "111.111.1", rg: "1111-1", dataNascimento: "1990-05-10", cep: "08000-000", cidade: "São Paulo", uf: "SP", email: "silmara@email.com",   telefone: "(11) 99999-9999", localTrabalho: "Escola Girassol",  telTrabalho: "(11) 3333-3333", status: 0, estadoCivil: "Casada" },
        { id: 2,  nome: "João Silva",       cpf: "222.222.2", rg: "2222-2", dataNascimento: "1985-03-15", cep: "08000-001", cidade: "São Paulo", uf: "SP", email: "joao@email.com",     telefone: "(11) 98888-8888", localTrabalho: "Indústria X",      telTrabalho: "(11) 3333-4444", status: 0, estadoCivil: "Casado" },
        { id: 3,  nome: "Maria Oliveira",   cpf: "333.333.3", rg: "3333-3", dataNascimento: "1992-07-20", cep: "08000-002", cidade: "São Paulo", uf: "SP", email: "maria@email.com",    telefone: "(11) 97777-7777", localTrabalho: "Hospital Y",       telTrabalho: "(11) 3333-5555", status: 0, estadoCivil: "Solteira" },
        { id: 4,  nome: "Pedro Santos",     cpf: "444.444.4", rg: "4444-4", dataNascimento: "1980-11-05", cep: "08000-003", cidade: "São Paulo", uf: "SP", email: "pedro@email.com",    telefone: "(11) 96666-6666", localTrabalho: "Comércio Z",       telTrabalho: "(11) 3333-6666", status: 0, estadoCivil: "Casado" },
        { id: 5,  nome: "Ana Souza",        cpf: "555.555.5", rg: "5555-5", dataNascimento: "1988-01-30", cep: "08000-004", cidade: "São Paulo", uf: "SP", email: "ana@email.com",      telefone: "(11) 95555-5555", localTrabalho: "Banco W",          telTrabalho: "(11) 3333-7777", status: 0, estadoCivil: "Divorciada" },
        { id: 6,  nome: "Carlos Lima",      cpf: "666.666.6", rg: "6666-6", dataNascimento: "1975-09-12", cep: "08000-005", cidade: "São Paulo", uf: "SP", email: "carlos@email.com",   telefone: "(11) 94444-4444", localTrabalho: "Oficina K",        telTrabalho: "(11) 3333-8888", status: 0, estadoCivil: "Casado" },
        { id: 7,  nome: "Fernanda Costa",   cpf: "777.777.7", rg: "7777-7", dataNascimento: "1995-05-25", cep: "08000-006", cidade: "São Paulo", uf: "SP", email: "fernanda@email.com", telefone: "(11) 93333-3333", localTrabalho: "Estúdio M",        telTrabalho: "(11) 3333-9999", status: 0, estadoCivil: "Solteira" },
        { id: 8,  nome: "Ricardo Pereira",  cpf: "888.888.8", rg: "8888-8", dataNascimento: "1982-12-10", cep: "08000-007", cidade: "São Paulo", uf: "SP", email: "ricardo@email.com",  telefone: "(11) 92222-2222", localTrabalho: "Escritório L",     telTrabalho: "(11) 3333-0000", status: 0, estadoCivil: "Casado" },
        { id: 9,  nome: "Juliana Rocha",    cpf: "999.999.9", rg: "9999-9", dataNascimento: "1990-08-05", cep: "08000-008", cidade: "São Paulo", uf: "SP", email: "juliana@email.com",  telefone: "(11) 91111-1111", localTrabalho: "Clínica P",        telTrabalho: "(11) 3333-1111", status: 0, estadoCivil: "Casada" },
        { id: 10, nome: "Lucas Mendes",     cpf: "000.000.0", rg: "0000-0", dataNascimento: "1987-04-18", cep: "08000-009", cidade: "São Paulo", uf: "SP", email: "lucas@email.com",    telefone: "(11) 90000-0000", localTrabalho: "Startup Q",        telTrabalho: "(11) 3333-2222", status: 0, estadoCivil: "Solteiro" }
    ],

    autorizados: [
        { id: 1,  nome: "Tio Barnabé",           cpf: "123.456.7", rg: "1234-5", dataNascimento: "1960-05-10", cep: "08000-100", cidade: "São Paulo", uf: "SP", email: "barnabe@email.com",  telefone: "(11) 91111-0001", grauParentesco: "Tio" },
        { id: 2,  nome: "Dona Benta",             cpf: "234.567.8", rg: "2345-6", dataNascimento: "1950-03-15", cep: "08000-101", cidade: "São Paulo", uf: "SP", email: "benta@email.com",    telefone: "(11) 91111-0002", grauParentesco: "Avó" },
        { id: 3,  nome: "Tia Nastácia",           cpf: "345.678.9", rg: "3456-7", dataNascimento: "1955-07-20", cep: "08000-102", cidade: "São Paulo", uf: "SP", email: "nastacia@email.com", telefone: "(11) 91111-0003", grauParentesco: "Tia" },
        { id: 4,  nome: "Visconde de Sabugosa",   cpf: "456.789.0", rg: "4567-8", dataNascimento: "1970-11-05", cep: "08000-103", cidade: "São Paulo", uf: "SP", email: "visconde@email.com", telefone: "(11) 91111-0004", grauParentesco: "Primo" },
        { id: 5,  nome: "Emília",                 cpf: "567.890.1", rg: "5678-9", dataNascimento: "2010-01-30", cep: "08000-104", cidade: "São Paulo", uf: "SP", email: "emilia@email.com",   telefone: "(11) 91111-0005", grauParentesco: "Irmã" },
        { id: 6,  nome: "Zeca Pagodinho",         cpf: "678.901.2", rg: "6789-0", dataNascimento: "1965-09-12", cep: "08000-105", cidade: "São Paulo", uf: "SP", email: "zeca@email.com",     telefone: "(11) 91111-0006", grauParentesco: "Vizinho" },
        { id: 7,  nome: "Luciana Gimenez",        cpf: "789.012.3", rg: "7890-1", dataNascimento: "1980-05-25", cep: "08000-106", cidade: "São Paulo", uf: "SP", email: "luciana@email.com",  telefone: "(11) 91111-0007", grauParentesco: "Madrinha" },
        { id: 8,  nome: "Fernando Pessoa",        cpf: "890.123.4", rg: "8901-2", dataNascimento: "1975-12-10", cep: "08000-107", cidade: "São Paulo", uf: "SP", email: "fernando@email.com", telefone: "(11) 91111-0008", grauParentesco: "Padrinho" },
        { id: 9,  nome: "Beatriz Segall",         cpf: "901.234.5", rg: "9012-3", dataNascimento: "1968-08-05", cep: "08000-108", cidade: "São Paulo", uf: "SP", email: "beatriz@email.com",  telefone: "(11) 91111-0009", grauParentesco: "Amiga da Família" },
        { id: 10, nome: "Jorge Ben",              cpf: "012.345.6", rg: "0123-4", dataNascimento: "1972-04-18", cep: "08000-109", cidade: "São Paulo", uf: "SP", email: "jorge@email.com",    telefone: "(11) 91111-0010", grauParentesco: "Motorista" }
    ],

    professores: [
        { id: 1,  nome: "Marcos Silva",    cpf: "111.222.333-01", rg: "12345-1", dataNascimento: "1980-05-10", cep: "08000-201", cidade: "São Paulo", uf: "SP", email: "marcos@escola.com",  telefone: "(11) 92222-1111", dataAdmissao: "2015-02-01", formacao: "Pedagogia",       status: 0 },
        { id: 2,  nome: "Sandra Oliveira", cpf: "222.333.444-02", rg: "22345-2", dataNascimento: "1985-03-15", cep: "08000-202", cidade: "São Paulo", uf: "SP", email: "sandra@escola.com",  telefone: "(11) 92222-2222", dataAdmissao: "2016-03-01", formacao: "Pedagogia",       status: 0 },
        { id: 3,  nome: "Roberto Santos",  cpf: "333.444.555-03", rg: "32345-3", dataNascimento: "1975-07-20", cep: "08000-203", cidade: "São Paulo", uf: "SP", email: "roberto@escola.com", telefone: "(11) 92222-3333", dataAdmissao: "2014-01-01", formacao: "Pedagogia",       status: 0 },
        { id: 4,  nome: "Cláudia Souza",   cpf: "444.555.666-04", rg: "42345-4", dataNascimento: "1982-11-05", cep: "08000-204", cidade: "São Paulo", uf: "SP", email: "claudia@escola.com", telefone: "(11) 92222-4444", dataAdmissao: "2017-05-01", formacao: "Pedagogia",       status: 0 },
        { id: 5,  nome: "Paulo Lima",      cpf: "555.666.777-05", rg: "52345-5", dataNascimento: "1990-01-30", cep: "08000-205", cidade: "São Paulo", uf: "SP", email: "paulo@escola.com",   telefone: "(11) 92222-5555", dataAdmissao: "2018-08-01", formacao: "Pedagogia",       status: 0 },
        { id: 6,  nome: "Regina Costa",    cpf: "666.777.888-06", rg: "62345-6", dataNascimento: "1978-09-12", cep: "08000-206", cidade: "São Paulo", uf: "SP", email: "regina@escola.com",  telefone: "(11) 92222-6666", dataAdmissao: "2015-06-01", formacao: "Pedagogia",       status: 0 },
        { id: 7,  nome: "Fábio Pereira",   cpf: "777.888.999-07", rg: "72345-7", dataNascimento: "1988-05-25", cep: "08000-207", cidade: "São Paulo", uf: "SP", email: "fabio@escola.com",   telefone: "(11) 92222-7777", dataAdmissao: "2019-02-01", formacao: "Natação",         status: 0 },
        { id: 8,  nome: "Márcia Rocha",    cpf: "888.999.000-08", rg: "82345-8", dataNascimento: "1983-12-10", cep: "08000-208", cidade: "São Paulo", uf: "SP", email: "marcia@escola.com",  telefone: "(11) 92222-8888", dataAdmissao: "2016-10-01", formacao: "Educação Física", status: 0 },
        { id: 9,  nome: "André Mendes",    cpf: "999.000.111-09", rg: "92345-9", dataNascimento: "1992-08-05", cep: "08000-209", cidade: "São Paulo", uf: "SP", email: "andre@escola.com",   telefone: "(11) 92222-9999", dataAdmissao: "2020-01-01", formacao: "Espanhol",        status: 0 },
        { id: 10, nome: "Sônia Castro",    cpf: "000.111.222-10", rg: "02345-0", dataNascimento: "1970-04-18", cep: "08000-210", cidade: "São Paulo", uf: "SP", email: "sonia@escola.com",   telefone: "(11) 92222-0000", dataAdmissao: "2013-02-01", formacao: "Inglês",          status: 0 }
    ],

    // Campos espelhando AlunoEntity: alergias, restricoesAlimentares, necessidadesEspeciais
    alunos: [
        { id: 1,  nome: "Joãozinho Silva",    dataNascimento: "2018-05-10", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Nenhuma", responsavelId: 1,  autorizadoId: 1,  grauParentesco: "Mãe" },
        { id: 2,  nome: "Mariazinha Oliveira",dataNascimento: "2019-03-15", alergias: "Poeira",   restricoesAlimentares: "Lactose",  necessidadesEspeciais: "Nenhuma", responsavelId: 2,  autorizadoId: 2,  grauParentesco: "Pai" },
        { id: 3,  nome: "Pedrinho Santos",    dataNascimento: "2017-07-20", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "TDAH",    responsavelId: 3,  autorizadoId: 3,  grauParentesco: "Mãe" },
        { id: 4,  nome: "Aninha Souza",       dataNascimento: "2020-11-05", alergias: "Amendoim", restricoesAlimentares: "Amendoim", necessidadesEspeciais: "Nenhuma", responsavelId: 4,  autorizadoId: 4,  grauParentesco: "Pai" },
        { id: 5,  nome: "Chiquinho Lima",     dataNascimento: "2018-01-30", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Nenhuma", responsavelId: 5,  autorizadoId: 5,  grauParentesco: "Mãe" },
        { id: 6,  nome: "Belinha Costa",      dataNascimento: "2019-09-12", alergias: "Nenhuma",  restricoesAlimentares: "Glúten",   necessidadesEspeciais: "Nenhuma", responsavelId: 6,  autorizadoId: 6,  grauParentesco: "Pai" },
        { id: 7,  nome: "Zequinha Pereira",   dataNascimento: "2017-05-25", alergias: "Gatos",    restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Nenhuma", responsavelId: 7,  autorizadoId: 7,  grauParentesco: "Mãe" },
        { id: 8,  nome: "Dudinha Rocha",      dataNascimento: "2018-12-10", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Autismo", responsavelId: 8,  autorizadoId: 8,  grauParentesco: "Pai" },
        { id: 9,  nome: "Gui Mendes",         dataNascimento: "2020-08-05", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Nenhuma", responsavelId: 9,  autorizadoId: 9,  grauParentesco: "Mãe" },
        { id: 10, nome: "Lala Castro",        dataNascimento: "2019-04-18", alergias: "Nenhuma",  restricoesAlimentares: "Nenhuma",  necessidadesEspeciais: "Nenhuma", responsavelId: 10, autorizadoId: 10, grauParentesco: "Pai" }
    ],

    turmas: [
        { id: 1,  nomeTurma: "Berçário I",    ano: currentYearStr, grau: "Berçário", professorId: 1  },
        { id: 2,  nomeTurma: "Berçário II",   ano: currentYearStr, grau: "Berçário", professorId: 2  },
        { id: 3,  nomeTurma: "Maternal I-A",  ano: currentYearStr, grau: "Infantil", professorId: 3  },
        { id: 4,  nomeTurma: "Maternal I-B",  ano: currentYearStr, grau: "Infantil", professorId: 4  },
        { id: 5,  nomeTurma: "Maternal II-A", ano: currentYearStr, grau: "Infantil", professorId: 5  },
        { id: 6,  nomeTurma: "Maternal II-B", ano: currentYearStr, grau: "Infantil", professorId: 6  },
        { id: 7,  nomeTurma: "Etapa 1-A",     ano: currentYearStr, grau: "Infantil", professorId: 7  },
        { id: 8,  nomeTurma: "Etapa 1-B",     ano: currentYearStr, grau: "Infantil", professorId: 8  },
        { id: 9,  nomeTurma: "Etapa 2-A",     ano: currentYearStr, grau: "Infantil", professorId: 9  },
        { id: 10, nomeTurma: "Etapa 2-B",     ano: currentYearStr, grau: "Infantil", professorId: 10 }
    ],

    // MatriculaEntity: nr, dataMatricula, status, alunoId, turmaId
    // Turma 1 (Berçário I): matrículas 101-105 | Turma 2 (Berçário II): matrículas 106-110
    matriculas: [
        { nr: 101, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 1,  turmaId: 1 },
        { nr: 102, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 2,  turmaId: 1 },
        { nr: 103, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 3,  turmaId: 1 },
        { nr: 104, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 4,  turmaId: 1 },
        { nr: 105, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 5,  turmaId: 1 },
        { nr: 106, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 6,  turmaId: 2 },
        { nr: 107, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 7,  turmaId: 2 },
        { nr: 108, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 8,  turmaId: 2 },
        { nr: 109, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 9,  turmaId: 2 },
        { nr: 110, dataMatricula: currentYearStr + "-01-01", status: 0, alunoId: 10, turmaId: 2 }
    ],

    // AulasPlanejamentoEntity: status 0=Agendado, 1=Concluído
    planejamentos: [
        { id: 1,  dataAula: getRelativeDate(-1), atividadeDinamica: "Pintura a Dedo",          descricao: "Atividade criativa com tintas",          status: 1, professorId: 1,  turmaId: 1  },
        { id: 2,  dataAula: getRelativeDate(0),  atividadeDinamica: "Roda de Cantiga",         descricao: "Cantar músicas infantis",                status: 0, professorId: 2,  turmaId: 2  },
        { id: 3,  dataAula: getRelativeDate(1),  atividadeDinamica: "Contação de Histórias",   descricao: "Leitura de contos clássicos",            status: 0, professorId: 3,  turmaId: 3  },
        { id: 4,  dataAula: getRelativeDate(2),  atividadeDinamica: "Brincadeiras de Roda",    descricao: "Interação social com música",            status: 0, professorId: 4,  turmaId: 4  },
        { id: 5,  dataAula: getRelativeDate(3),  atividadeDinamica: "Aula de Natureza",        descricao: "Exploração do jardim",                  status: 0, professorId: 5,  turmaId: 5  },
        { id: 6,  dataAula: getRelativeDate(4),  atividadeDinamica: "Matemática Divertida",    descricao: "Contagem com objects",                   status: 0, professorId: 6,  turmaId: 6  },
        { id: 7,  dataAula: getRelativeDate(5),  atividadeDinamica: "Expressão Corporal",      descricao: "Dança e movimentos",                    status: 0, professorId: 7,  turmaId: 7  },
        { id: 8,  dataAula: getRelativeDate(6),  atividadeDinamica: "Iniciação ao Inglês",     descricao: "Cores e saudações em inglês",            status: 0, professorId: 8,  turmaId: 8  },
        { id: 9,  dataAula: getRelativeDate(7),  atividadeDinamica: "Experiências Científicas",descricao: "Mistura de cores e texturas",            status: 0, professorId: 9,  turmaId: 9  },
        { id: 10, dataAula: getRelativeDate(8),  atividadeDinamica: "Atividade Sensorial",     descricao: "Explorando diferentes materiais",       status: 0, professorId: 10, turmaId: 10 }
    ],

    // DiarioBordoEntity: matriculaNr referencia MatriculaEntity
    diarios: [
        { id: 1,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu tudo",      sono: "Dormiu 1h",   humor: "Feliz",      fraldas: "Troca normal", observacoes: "Muito participativo", matriculaNr: 101, professorId: 1 },
        { id: 2,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Recusou legumes", sono: "Dormiu pouco", humor: "Agitado",    fraldas: "Troca normal", observacoes: "Brincou muito no parque", matriculaNr: 102, professorId: 2 },
        { id: 3,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu bem",       sono: "Não dormiu",   humor: "Calmo",      fraldas: "Troca normal", observacoes: "Concentrado na pintura", matriculaNr: 103, professorId: 3 },
        { id: 4,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu tudo",      sono: "Dormiu 2h",   humor: "Radiante",   fraldas: "Troca normal", observacoes: "Adora a roda de música", matriculaNr: 104, professorId: 4 },
        { id: 5,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Lanchou bem",      sono: "Dormiu 1h30", humor: "Feliz",      fraldas: "Troca normal", observacoes: "Interagiu com os colegas", matriculaNr: 105, professorId: 5 },
        { id: 6,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu tudo",      sono: "Dormiu 1h",   humor: "Preguiçoso", fraldas: "Troca normal", observacoes: "Ficou quietinho hoje", matriculaNr: 106, professorId: 6 },
        { id: 7,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Recusou a fruta",  sono: "Não dormiu",   humor: "Choroso",    fraldas: "Troca normal", observacoes: "Sentiu falta da mamãe", matriculaNr: 107, professorId: 7 },
        { id: 8,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu bem",       sono: "Dormiu 1h",   humor: "Feliz",      fraldas: "Troca normal", observacoes: "Muito interessado na história", matriculaNr: 108, professorId: 8 },
        { id: 9,  dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu tudo",      sono: "Dormiu 2h",   humor: "Energético", fraldas: "Troca normal", observacoes: "Correu muito no pátio", matriculaNr: 109, professorId: 9 },
        { id: 10, dataRegistro: getRelativeDate(-1), refeicao: 1, alimentacao: "Comeu bem",       sono: "Dormiu 1h",   humor: "Tranquilo",  fraldas: "Troca normal", observacoes: "Desenhou a família", matriculaNr: 110, professorId: 10 }
    ],

    // MatriculaAulasEntity: planejamentoId, matriculaNr, status (0=Presente, 1=Falta)
    presencas: [
        { planejamentoId: 1, matriculaNr: 101, status: 0 },
        { planejamentoId: 1, matriculaNr: 102, status: 1 },
        { planejamentoId: 2, matriculaNr: 103, status: 0 },
        { planejamentoId: 2, matriculaNr: 104, status: 0 },
        { planejamentoId: 3, matriculaNr: 105, status: 0 },
        { planejamentoId: 3, matriculaNr: 106, status: 1 },
        { planejamentoId: 4, matriculaNr: 107, status: 0 },
        { planejamentoId: 4, matriculaNr: 108, status: 0 },
        { planejamentoId: 5, matriculaNr: 109, status: 0 },
        { planejamentoId: 5, matriculaNr: 110, status: 1 }
    ]
};

// =============================================================================
// StatusUtil — espelho do StatusUntil.java
// =============================================================================
window.StatusUtil = {
    getDescricaoGeral(status) {
        const map = { 0: "Ativo", 1: "Inativo", 2: "Pendente", 3: "Cancelado" };
        return map[status] ?? "Desconhecido";
    },
    getDescricaoPresenca(status) {
        const map = { 0: "Presente", 1: "Falta", 2: "Justificado" };
        return map[status] ?? "Sem Registro";
    },
    getDescricaoAula(status) {
        const map = { 0: "Agendado", 1: "Concluído", 2: "Cancelado" };
        return map[status] ?? "Desconhecido";
    }
};

// =============================================================================
// Database — dados em memória (sem localStorage, sem banco externo)
// =============================================================================
window.__dbState = {};

window.Database = {
    init() {
        Object.keys(window.db_mock).forEach(key => {
            window.__dbState[key] = JSON.parse(JSON.stringify(window.db_mock[key]));
        });
        console.log("[Colmeia Girassol] Dados inicializados em memória — resetados a cada carregamento.");
    },

    get(key) {
        return window.__dbState[key] ?? [];
    },

    save(key, data) {
        const collection = this.get(key);
        const ids = collection.map(i => i.id ?? i.nr ?? 0);
        const newId = ids.length > 0 ? Math.max(...ids) + 1 : 1;
        const record = key === "matriculas" ? { nr: 100 + newId, ...data } : { id: newId, ...data };
        collection.push(record);
        window.__dbState[key] = collection;
        return record;
    },

    remove(key, idOrNr) {
        const collection = window.__dbState[key] ?? [];
        window.__dbState[key] = collection.filter(i => (i.id ?? i.nr) !== idOrNr);
    },

    // ---- Helpers JOIN ----
    getProfessor(id)    { return this.get("professores").find(p => p.id === id) ?? null; },
    getResponsavel(id)  { return this.get("responsaveis").find(r => r.id === id) ?? null; },
    getAutorizado(id)   { return this.get("autorizados").find(a => a.id === id) ?? null; },

    getTurma(id) {
        const t = this.get("turmas").find(t => t.id === id);
        if (!t) return null;
        return { ...t, professor: this.getProfessor(t.professorId)?.nome ?? "—" };
    },

    getMatricula(nr) {
        const m = this.get("matriculas").find(m => m.nr === nr);
        if (!m) return null;
        const aluno = this.get("alunos").find(a => a.id === m.alunoId) ?? null;
        return { ...m, aluno, turma: this.getTurma(m.turmaId) };
    },

    getTurmasComProfessor() {
        return this.get("turmas").map(t => ({
            ...t,
            professor: this.getProfessor(t.professorId)?.nome ?? "—",
            alunosCount: this.get("matriculas").filter(m => m.turmaId === t.id).length
        }));
    },

    getAlunosCompleto() {
        return this.get("alunos").map(a => {
            const responsavel = this.getResponsavel(a.responsavelId);
            const autorizado  = a.autorizadoId ? this.getAutorizado(a.autorizadoId) : null;
            const matricula   = this.get("matriculas").find(m => m.alunoId === a.id);
            const turma       = matricula ? this.getTurma(matricula.turmaId) : null;
            return { ...a, responsavel, autorizado, turma, matriculaNr: matricula?.nr ?? null };
        });
    },

    getPlanejamentosCompleto() {
        return this.get("planejamentos").map(p => ({
            ...p,
            statusLabel: StatusUtil.getDescricaoAula(p.status),
            professor: this.getProfessor(p.professorId)?.nome ?? "—",
            turma: this.get("turmas").find(t => t.id === p.turmaId)?.nomeTurma ?? "—"
        }));
    },

    getDiariosCompleto() {
        return this.get("diarios").map(d => {
            const matricula = this.getMatricula(d.matriculaNr);
            const professor = this.getProfessor(d.professorId);
            return { ...d, aluno: matricula?.aluno ?? null, professor };
        });
    },

    getPresencasPorAula(planejamentoId) {
        return this.get("presencas").filter(p => p.planejamentoId === planejamentoId).map(p => {
            const matricula = this.getMatricula(p.matriculaNr);
            return { ...p, aluno: matricula?.aluno ?? null };
        });
    },

    getAlunosPorTurma(turmaId) {
        const matriculas = this.get("matriculas").filter(m => m.turmaId === turmaId);
        return matriculas.map(m => this.getAlunosCompleto().find(a => a.id === m.alunoId)).filter(Boolean);
    }
};

window.Database.init();
