// =============================================================================
// render.js — Renderiza dados do Database.js nos dashboards
// Todos os dados vêm do Database (sincronizado com App.java)
// =============================================================================

document.addEventListener("DOMContentLoaded", function () {

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    // Lê o nome salvo no sessionStorage (definido pelo login)
    const nomeSessao = (() => { try { return sessionStorage.getItem('cg_nome'); } catch(e) { return null; } })();
    const perfilSessao = (() => { try { return sessionStorage.getItem('cg_perfil'); } catch(e) { return null; } })();

    // Formata data dd/mm/aaaa
    function fmtData(iso) {
        if (!iso) return '—';
        const [y, m, d] = iso.split('-');
        return `${d}/${m}/${y}`;
    }

    // Mascara CPF parcialmente: 111.111.***-**
    function mascaraCpf(cpf) {
        if (!cpf) return '—';
        return cpf.replace(/(\d{3}\.\d{3})\.\d{3}-\d{2}/, '$1.***.** ').trim();
    }

    // Mascara telefone: (11) 9****-0003
    function mascaraTel(tel) {
        if (!tel) return '—';
        return tel.replace(/(\(\d{2}\)\s\d)\d{4}(-\d{4})/, '$1****$2');
    }

    // =========================================================================
    // DASHBOARD ADMIN
    // =========================================================================

    // --- Saudação dinâmica ---
    const saudacaoAdmin = document.getElementById('saudacao-admin');
    if (saudacaoAdmin) {
        saudacaoAdmin.textContent = 'Olá, ' + (nomeSessao || 'Administrador');
    }

    // --- Stats dinâmicos ---
    const statAlunos = document.getElementById('stat-alunos');
    if (statAlunos) statAlunos.textContent = Database.get('alunos').length;

    const statProfessores = document.getElementById('stat-professores');
    if (statProfessores) statProfessores.textContent = Database.get('professores').length;

    const statTurmas = document.getElementById('stat-turmas');
    if (statTurmas) statTurmas.textContent = Database.get('turmas').length;

    const statMatriculas = document.getElementById('stat-matriculas');
    if (statMatriculas) statMatriculas.textContent = Database.get('matriculas').length;

    // --- Select de professores no formulário de nova turma ---
    const selectProfTurma = document.getElementById('select-prof-turma');
    if (selectProfTurma) {
        const profs = Database.get('professores');
        selectProfTurma.innerHTML = profs.map(p =>
            `<option value="${p.id}">${p.nome}</option>`
        ).join('');
    }

    // --- Tabela de Professores ---
    const professorTable = document.querySelector('#cadastro-professor tbody');
    if (professorTable) {
        const professores = Database.get('professores');
        professorTable.innerHTML = professores.map(p => `
            <tr>
                <td>
                    <div class="d-flex align-items-center gap-3">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(p.nome)}&background=7EC8D9&color=fff" class="rounded-circle" style="width: 35px;" alt="${p.nome}">
                        <div>
                            <div class="fw-bold">${p.nome}</div>
                            <small class="text-muted">${p.email}</small>
                        </div>
                    </div>
                </td>
                <td>${p.formacao}</td>
                <td>${fmtData(p.dataAdmissao)}</td>
                <td><span class="badge badge-active px-3 py-2 rounded-pill">${StatusUtil.getDescricaoGeral(p.status)}</span></td>
                <td class="text-center">
                    <button class="btn btn-sm text-primary" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="btn btn-sm text-danger" title="Remover" onclick="removerProfessor(${p.id})"><i class="fa-solid fa-trash"></i></button>
                </td>
            </tr>
        `).join('');
    }

    // --- Grid de Turmas ---
    const turmaGrid = document.querySelector('#cadastro-turma .row.g-4');
    if (turmaGrid) {
        renderTurmas();
    }

    function renderTurmas() {
        const grid = document.querySelector('#cadastro-turma .row.g-4');
        if (!grid) return;
        const turmas = Database.getTurmasComProfessor();
        grid.innerHTML = turmas.map(t => `
            <div class="col-md-4 mb-4">
                <div class="stat-card border-start border-4 border-primary">
                    <h5 class="fw-bold text-dark-blue">${t.nomeTurma}</h5>
                    <p class="text-muted small mb-1"><i class="fa-solid fa-user-tie me-2"></i>Prof. ${t.professor}</p>
                    <p class="text-muted small mb-3"><i class="fa-solid fa-calendar me-2"></i>${t.ano} — ${t.grau}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="badge bg-light text-dark-blue rounded-pill px-3">${t.alunosCount} Aluno${t.alunosCount !== 1 ? 's' : ''}</span>
                        <div>
                            <button class="btn btn-sm text-primary me-2" onclick="verTurmaDetalhes(${t.id})"><i class="fa-solid fa-eye"></i></button>
                            <button class="btn btn-sm text-danger" onclick="removerTurma(${t.id})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // --- Tabela de Matrículas ---
    const matriculaTable = document.querySelector('#matriculas tbody');
    if (matriculaTable) {
        renderMatriculas();
    }

    function renderMatriculas() {
        const tbody = document.querySelector('#matriculas tbody');
        if (!tbody) return;
        const matriculas = Database.get('matriculas');
        tbody.innerHTML = matriculas.map(m => {
            const aluno = Database.get('alunos').find(a => a.id === m.alunoId);
            const turma = Database.getTurma(m.turmaId);
            return `
                <tr>
                    <td><span class="fw-bold">${m.nr}</span></td>
                    <td>${aluno?.nome ?? '—'}</td>
                    <td>${turma?.nomeTurma ?? '—'}</td>
                    <td>${fmtData(m.dataMatricula)}</td>
                    <td><span class="badge badge-active px-3 py-2 rounded-pill">${StatusUtil.getDescricaoGeral(m.status)}</span></td>
                    <td class="text-center">
                        <button class="btn btn-sm text-primary" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                        <button class="btn btn-sm text-danger" title="Remover" onclick="removerMatricula(${m.nr})"><i class="fa-solid fa-trash"></i></button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    // --- Tabela de Alunos (admin) ---
    const alunosTable = document.querySelector('#alunos-lista tbody');
    if (alunosTable) {
        renderAlunos();
    }

    function renderAlunos() {
        const tbody = document.querySelector('#alunos-lista tbody');
        if (!tbody) return;
        const alunos = Database.getAlunosCompleto();
        tbody.innerHTML = alunos.map(a => `
            <tr>
                <td>
                    <div class="d-flex align-items-center gap-2">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(a.nome)}&background=7EC8D9&color=fff" class="rounded-circle" style="width:32px;" alt="${a.nome}">
                        <div>
                            <div class="fw-bold">${a.nome}</div>
                            <small class="text-muted">${fmtData(a.dataNascimento)}</small>
                        </div>
                    </div>
                </td>
                <td>${a.turma?.nomeTurma ?? '—'}</td>
                <td>${a.responsavel?.nome ?? '—'}</td>
                <td><span class="badge badge-active px-3 py-2 rounded-pill">Ativo</span></td>
                <td class="text-center">
                    <button class="btn btn-sm text-primary"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="btn btn-sm text-danger" onclick="removerMatricula(${a.matriculaNr})"><i class="fa-solid fa-trash"></i></button>
                </td>
            </tr>
        `).join('');
    }

    // --- Tabela de Responsáveis ---
    const responsaveisTable = document.querySelector('#responsaveis-lista tbody');
    if (responsaveisTable) {
        renderResponsaveis();
    }

    function renderResponsaveis() {
        const tbody = document.querySelector('#responsaveis-lista tbody');
        if (!tbody) return;
        const responsaveis = Database.get('responsaveis');
        tbody.innerHTML = responsaveis.map(r => `
            <tr>
                <td><div class="fw-bold">${r.nome}</div><small class="text-muted">${r.estadoCivil}</small></td>
                <td>${r.cpf}</td>
                <td>${r.telefone}</td>
                <td>${r.email}</td>
                <td class="text-center">
                    <button class="btn btn-sm text-primary"><i class="fa-solid fa-pen-to-square"></i></button>
                </td>
            </tr>
        `).join('');
    }

    // --- Tabela de Diários (Admin) ---
    const diariosAdminTable = document.querySelector('#diarios-lista tbody');
    if (diariosAdminTable) {
        renderDiariosAdmin();
    }

    function renderDiariosAdmin() {
        const tbody = document.querySelector('#diarios-lista tbody');
        if (!tbody) return;
        const diarios = Database.getDiariosCompleto();
        tbody.innerHTML = diarios.map(d => `
            <tr>
                <td>${fmtData(d.dataRegistro)}</td>
                <td>${d.aluno?.nome ?? '—'}</td>
                <td><span class="badge bg-light text-dark-blue">${d.humor}</span></td>
                <td>${d.alimentacao}</td>
                <td>${d.professor?.nome ?? '—'}</td>
                <td class="text-center">
                    <button class="btn btn-sm text-primary"><i class="fa-solid fa-file-lines"></i></button>
                </td>
            </tr>
        `).join('');
    }

    // --- Ações Admin expostas globalmente ---
    window.removerProfessor = function() {
        alert('Esta função de exclusão está desativada para esta versão de demonstração.');
    };

    window.removerTurma = function() {
        alert('Esta função de exclusão está desativada para esta versão de demonstração.');
    };

    window.removerMatricula = function() {
        alert('Esta função de exclusão está desativada para esta versão de demonstração.');
    };

    window.verTurmaDetalhes = function(id) {
        const turma = Database.getTurma(id);
        if (!turma) return;
        document.getElementById('modalTurmaNome').textContent = 'Turma: ' + turma.nomeTurma;
        document.getElementById('modalTurmaProf').textContent = 'Prof. ' + turma.professor;
        document.getElementById('modalTurmaInfo').textContent = `${turma.ano} — ${turma.grau}`;
        
        const alunos = Database.getAlunosPorTurma(id);
        const tbody = document.getElementById('modalTurmaAlunos');
        tbody.innerHTML = alunos.map(a => `
            <tr>
                <td>${a.nome}</td>
                <td>${fmtData(a.dataNascimento)}</td>
                <td>${a.responsavel?.nome ?? '—'}</td>
            </tr>
        `).join('') || '<tr><td colspan="3" class="text-center">Nenhum aluno matriculado.</td></tr>';
        
        new bootstrap.Modal(document.getElementById('modalTurmaDetalhes')).show();
    };

    // =========================================================================
    // DASHBOARD PROFESSOR
    // =========================================================================

    // --- Saudação dinâmica ---
    const saudacaoProfEl = document.getElementById('saudacao-prof');
    if (saudacaoProfEl) {
        // Usa nome da sessão, ou pega o primeiro professor do DB como fallback
        const prof = nomeSessao
            ? Database.get('professores').find(p => p.nome === nomeSessao)
            : Database.get('professores')[0];
        const nomeProf = prof?.nome ?? nomeSessao ?? 'Professor(a)';
        saudacaoProfEl.textContent = 'Olá, Prof. ' + nomeProf.split(' ')[0];

        // Info no header
        const headerProfNome = document.getElementById('header-prof-nome');
        const headerProfInfo = document.getElementById('header-prof-info');
        const headerProfAvatar = document.getElementById('header-prof-avatar');
        if (prof) {
            const turmaProf = Database.get('turmas').find(t => t.professorId === prof.id);
            if (headerProfNome) headerProfNome.textContent = prof.nome;
            if (headerProfInfo) headerProfInfo.textContent = prof.formacao + (turmaProf ? ' — ' + turmaProf.nomeTurma : '');
            if (headerProfAvatar) headerProfAvatar.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(prof.nome)}&background=7EC8D9&color=fff`;
        }
    }

    // --- Stats do professor ---
    const statProfAlunos = document.getElementById('stat-prof-alunos');
    if (statProfAlunos) {
        const prof = Database.get('professores').find(p => p.nome === nomeSessao) ?? Database.get('professores')[0];
        const turmaProf = prof ? Database.get('turmas').find(t => t.professorId === prof.id) : null;
        const qtd = turmaProf ? Database.get('matriculas').filter(m => m.turmaId === turmaProf.id).length : 0;
        statProfAlunos.textContent = qtd;
    }
    const statProfPlanos = document.getElementById('stat-prof-planos');
    if (statProfPlanos) {
        const prof = Database.get('professores').find(p => p.nome === nomeSessao) ?? Database.get('professores')[0];
        const qtd = prof ? Database.get('planejamentos').filter(p => p.professorId === prof.id).length : 0;
        statProfPlanos.textContent = qtd;
    }

    // --- Select de alunos no Diário de Bordo ---
    const selectAlunoDiario = document.getElementById('select-aluno-diario');
    if (selectAlunoDiario) {
        const prof = Database.get('professores').find(p => p.nome === nomeSessao) ?? Database.get('professores')[0];
        const turmaProf = prof ? Database.get('turmas').find(t => t.professorId === prof.id) : null;
        const matriculas = turmaProf
            ? Database.get('matriculas').filter(m => m.turmaId === turmaProf.id)
            : Database.get('matriculas');
        const alunos = matriculas.map(m => Database.get('alunos').find(a => a.id === m.alunoId)).filter(Boolean);
        selectAlunoDiario.innerHTML = alunos.length
            ? alunos.map(a => `<option value="${a.id}">${a.nome}</option>`).join('')
            : '<option>Nenhum aluno na turma</option>';
    }

    // --- Grid de Planejamentos ---
    const planejamentoGrid = document.querySelector('#planejamento .row.g-4');
    if (planejamentoGrid) {
        const planos = Database.getPlanejamentosCompleto();
        planejamentoGrid.innerHTML = planos.length ? planos.map(p => `
            <div class="col-md-6 mb-4">
                <div class="stat-card">
                    <div class="d-flex justify-content-between mb-3">
                        <span class="badge ${p.status === 1 ? 'bg-green-light text-success' : 'bg-yellow-light text-warning'} rounded-pill px-3 py-2">${p.statusLabel}</span>
                        <span class="text-muted small">${fmtData(p.dataAula)}</span>
                    </div>
                    <h5 class="fw-bold text-dark-blue">${p.atividadeDinamica}</h5>
                    <p class="text-muted small">${p.descricao}</p>
                    <hr>
                    <div class="d-flex align-items-center gap-2">
                        <i class="fa-solid fa-users text-light-blue"></i>
                        <span class="small fw-bold">${p.turma}</span>
                        <div class="ms-auto">
                            <button class="btn btn-sm btn-outline-dark-blue rounded-pill px-3" onclick="abrirChamada(${p.id})">
                                <i class="fa-solid fa-clipboard-check me-1"></i>Chamada
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('') : '<div class="col-12"><p class="text-muted">Nenhum planejamento cadastrado.</p></div>';
    }

    window.abrirChamada = function(planoId) {
        const plano = Database.getPlanejamentosCompleto().find(p => p.id === planoId);
        if (!plano) return;
        
        document.getElementById('modalChamadaTitulo').textContent = 'Chamada: ' + plano.atividadeDinamica;
        const presencas = Database.getPresencasPorAula(planoId);
        const turmaAlunos = Database.getAlunosPorTurma(plano.turmaId);
        
        const tbody = document.getElementById('listaChamadaAlunos');
        tbody.innerHTML = turmaAlunos.map(aluno => {
            const presenca = presencas.find(p => p.aluno?.id === aluno.id);
            const isPresente = presenca ? presenca.status === 0 : true;
            return `
                <tr>
                    <td><div class="fw-bold">${aluno.nome}</div></td>
                    <td class="text-center">
                        <div class="form-check form-switch d-inline-block">
                            <input class="form-check-input" type="checkbox" ${isPresente ? 'checked' : ''} onchange="togglePresenca(${planoId}, ${aluno.matriculaNr}, this.checked)">
                        </div>
                    </td>
                    <td><span class="badge ${isPresente ? 'bg-green-light text-success' : 'bg-red-light text-danger'} rounded-pill">${isPresente ? 'Presente' : 'Falta'}</span></td>
                </tr>
            `;
        }).join('');
        
        new bootstrap.Modal(document.getElementById('modalChamada')).show();
    };

    window.togglePresenca = function(planoId, matriculaNr, isPresente) {
        let presencas = Database.get('presencas');
        const index = presencas.findIndex(p => p.planejamentoId === planoId && p.matriculaNr === matriculaNr);
        const status = isPresente ? 0 : 1;
        
        if (index > -1) {
            presencas[index].status = status;
        } else {
            presencas.push({ planejamentoId: planoId, matriculaNr, status });
        }
        
        Database.__dbState.presencas = presencas;
        Database.persist();
        abrirChamada(planoId); // Refresh modal
    };

    // --- Minhas Turmas (lista de alunos) ---
    const minhasTurmasEl = document.getElementById('minhas-turmas-lista');
    if (minhasTurmasEl) {
        const prof = Database.get('professores').find(p => p.nome === nomeSessao) ?? Database.get('professores')[0];
        const turmaProf = prof ? Database.get('turmas').find(t => t.professorId === prof.id) : null;
        if (turmaProf) {
            const matriculas = Database.get('matriculas').filter(m => m.turmaId === turmaProf.id);
            const alunos = matriculas.map(m => Database.getAlunosCompleto().find(a => a.id === m.alunoId)).filter(Boolean);
            minhasTurmasEl.innerHTML = `
                <h5 class="fw-bold text-dark-blue mb-3"><i class="fa-solid fa-chalkboard-user me-2"></i>${turmaProf.nomeTurma} — ${alunos.length} aluno${alunos.length !== 1 ? 's' : ''}</h5>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr><th>Aluno</th><th>Alergias</th><th>Necessidades</th><th>Responsável</th><th class="text-center">Ação</th></tr>
                        </thead>
                        <tbody>
                            ${alunos.map(a => `
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(a.nome)}&background=7EC8D9&color=fff" class="rounded-circle" style="width:32px;">
                                            <span class="fw-bold">${a.nome}</span>
                                        </div>
                                    </td>
                                    <td><span class="small">${a.alergias}</span></td>
                                    <td><span class="small">${a.necessidadesEspeciais}</span></td>
                                    <td><span class="small">${a.responsavel?.nome ?? '—'}</span></td>
                                    <td class="text-center">
                                        <button class="btn btn-sm btn-outline-primary rounded-pill px-3" onclick="abrirAutorizados(${a.id})">
                                            <i class="fa-solid fa-id-card me-1"></i>Autorizados
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } else {
            minhasTurmasEl.innerHTML = '<p class="text-muted">Nenhuma turma atribuída.</p>';
        }
    }

    window.abrirAutorizados = function(alunoId) {
        const aluno = Database.getAlunosCompleto().find(a => a.id === alunoId);
        if (!aluno) return;

        document.getElementById('modalAutorizadosTitulo').textContent = 'Autorizados a Buscar: ' + aluno.nome;
        const listaEl = document.getElementById('lista-autorizados-modal');
        
        // Simulação de busca de autorizados (Responsável + Autorizado específico)
        const autorizados = [];
        if (aluno.responsavel) autorizados.push({ ...aluno.responsavel, parentesco: aluno.grauParentesco || 'Responsável' });
        if (aluno.autorizado) autorizados.push({ ...aluno.autorizado, parentesco: aluno.autorizado.grauParentesco || 'Autorizado' });

        if (autorizados.length === 0) {
            listaEl.innerHTML = '<p class="text-center text-muted">Nenhum autorizado cadastrado.</p>';
        } else {
            listaEl.innerHTML = autorizados.map(aut => `
                <div class="d-flex align-items-center gap-3 mb-3 p-3 bg-light rounded-4">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(aut.nome)}&background=2D4459&color=fff" class="rounded-circle" style="width:48px;">
                    <div>
                        <div class="fw-bold">${aut.nome}</div>
                        <div class="small text-muted">${aut.parentesco} • CPF: ${aut.cpf}</div>
                        <div class="small text-primary"><i class="fa-solid fa-phone me-1"></i>${aut.telefone}</div>
                    </div>
                </div>
            `).join('');
        }

        new bootstrap.Modal(document.getElementById('modalAutorizados')).show();
    };

    // =========================================================================
    // DASHBOARD RESPONSÁVEL
    // =========================================================================

    // Pega o responsável logado (por nome de sessão ou primeiro do DB)
    const respLogado = nomeSessao
        ? Database.get('responsaveis').find(r => r.nome === nomeSessao)
        : Database.get('responsaveis')[0];

    // Encontra os alunos deste responsável
    const alunosDoResp = respLogado
        ? Database.getAlunosCompleto().filter(a => a.responsavelId === respLogado.id)
        : Database.getAlunosCompleto().slice(0, 1);

    const alunoAtual = alunosDoResp[0] ?? null;

    // --- Nome do responsável no header ---
    const headerRespNome = document.getElementById('header-resp-nome');
    if (headerRespNome) {
        headerRespNome.textContent = respLogado?.nome ?? 'Responsável';
    }
    const headerRespAvatar = document.getElementById('header-resp-avatar');
    if (headerRespAvatar && respLogado) {
        headerRespAvatar.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(respLogado.nome)}&background=F2D544&color=2D4459`;
        headerRespAvatar.alt = respLogado.nome;
    }

    // --- Card do aluno ---
    const alunoNomeEl = document.getElementById('aluno-nome');
    if (alunoNomeEl && alunoAtual) {
        alunoNomeEl.textContent = alunoAtual.nome;
    }
    const alunoTurmaEl = document.getElementById('aluno-turma-prof');
    if (alunoTurmaEl && alunoAtual) {
        const turma = alunoAtual.turma;
        const prof = turma ? Database.getProfessor(turma.professorId) : null;
        alunoTurmaEl.textContent = (turma?.nomeTurma ?? '—') + (prof ? ' — Prof. ' + prof.nome.split(' ')[0] : '');
    }
    const alunoAvatarEl = document.getElementById('aluno-avatar');
    if (alunoAvatarEl && alunoAtual) {
        alunoAvatarEl.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(alunoAtual.nome)}&background=7EC8D9&color=fff&size=128`;
    }
    const alunoAlergiasEl = document.getElementById('aluno-alergias');
    if (alunoAlergiasEl && alunoAtual) {
        alunoAlergiasEl.textContent = alunoAtual.alergias;
    }
    const alunoRestEl = document.getElementById('aluno-restricoes');
    if (alunoRestEl && alunoAtual) {
        alunoRestEl.textContent = alunoAtual.restricoesAlimentares;
    }
    const alunoNecEl = document.getElementById('aluno-necessidades');
    if (alunoNecEl && alunoAtual) {
        alunoNecEl.textContent = alunoAtual.necessidadesEspeciais;
    }

    // --- Diário de Bordo (responsável) ---
    const diarioContent = document.querySelector('#diario .form-card');
    if (diarioContent) {
        const diarios = Database.getDiariosCompleto();
        // Filtra pelo aluno do responsável, se possível
        const diariosFiltrados = alunoAtual
            ? diarios.filter(d => d.aluno?.id === alunoAtual.id)
            : diarios;
        const ultimoDiario = diariosFiltrados[diariosFiltrados.length - 1] ?? diarios[diarios.length - 1];

        if (ultimoDiario) {
            diarioContent.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h4 class="fw-bold text-dark-blue mb-0"><i class="fa-solid fa-star text-yellow-main me-2"></i>Diário de Bordo — ${ultimoDiario.aluno?.nome ?? '—'}</h4>
                    <span class="text-muted small">${fmtData(ultimoDiario.dataRegistro)}</span>
                </div>
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <div class="bg-light p-3 rounded-4 text-center">
                            <i class="fa-solid fa-face-smile fs-3 text-success mb-2"></i>
                            <h6 class="fw-bold mb-1">Humor</h6>
                            <p class="small mb-0">${ultimoDiario.humor}</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="bg-light p-3 rounded-4 text-center">
                            <i class="fa-solid fa-utensils fs-3 text-primary mb-2"></i>
                            <h6 class="fw-bold mb-1">Alimentação</h6>
                            <p class="small mb-0">${ultimoDiario.alimentacao}</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="bg-light p-3 rounded-4 text-center">
                            <i class="fa-solid fa-moon fs-3 text-warning mb-2"></i>
                            <h6 class="fw-bold mb-1">Sono</h6>
                            <p class="small mb-0">${ultimoDiario.sono}</p>
                        </div>
                    </div>
                </div>
                <div class="bg-light p-4 rounded-4 mb-0">
                    <h6 class="fw-bold text-dark-blue mb-2">Mensagem do Professor:</h6>
                    <p class="mb-0 text-secondary fst-italic">"${ultimoDiario.observacoes}"</p>
                </div>
            `;
        } else {
            diarioContent.innerHTML = `
                <div class="text-center py-4 text-muted">
                    <i class="fa-solid fa-book-open fs-1 mb-3 opacity-25"></i>
                    <p>Nenhum registro no diário ainda.</p>
                </div>`;
        }
    }

    // --- Tabela de Autorizados (responsável) ---
    const autorizadosTable = document.querySelector('#autorizados tbody');
    if (autorizadosTable) {
        // Filtra autorizados vinculados ao aluno do responsável logado
        let autorizados = Database.get('autorizados');
        if (alunoAtual?.autorizadoId) {
            // Mostra o autorizado do aluno + todos (simulação demo)
            const autPrincipal = autorizados.find(a => a.id === alunoAtual.autorizadoId);
            autorizados = autPrincipal ? [autPrincipal] : autorizados.slice(0, 3);
        } else {
            autorizados = autorizados.slice(0, 3);
        }

        autorizadosTable.innerHTML = autorizados.map(a => `
            <tr>
                <td>
                    <div class="fw-bold">${a.nome}</div>
                    <small class="text-muted">CPF: ${mascaraCpf(a.cpf)}</small>
                </td>
                <td>${a.grauParentesco}</td>
                <td>${mascaraTel(a.telefone)}</td>
                <td class="text-end">
                    <button class="btn btn-sm text-danger"><i class="fa-solid fa-trash"></i></button>
                </td>
            </tr>
        `).join('');
    }

    // --- Info do aluno (render.js antigo — compatibilidade) ---
    const alunoInfoEl = document.getElementById('aluno-info');
    if (alunoInfoEl && alunoAtual) {
        alunoInfoEl.innerHTML = `
            <p class="mb-1"><strong>Nome:</strong> ${alunoAtual.nome}</p>
            <p class="mb-1"><strong>Turma:</strong> ${alunoAtual.turma?.nomeTurma ?? '—'}</p>
            <p class="mb-1"><strong>Alergias:</strong> ${alunoAtual.alergias}</p>
            <p class="mb-1"><strong>Restrições Alimentares:</strong> ${alunoAtual.restricoesAlimentares}</p>
            <p class="mb-0"><strong>Necessidades Especiais:</strong> ${alunoAtual.necessidadesEspeciais}</p>
        `;
    }

    // =========================================================================
    // LÓGICA DE ABAS (TABS) - DASHBOARD ADMIN
    // =========================================================================
    const navLinks = document.querySelectorAll('.nav-link-custom[data-section]');
    const sections = document.querySelectorAll('.dashboard-section');

    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            const targetSectionId = this.getAttribute('data-section');
            
            // Remove active de todos os links e seções
            navLinks.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));

            // Adiciona active nos links clicados/correspondentes e na seção alvo
            navLinks.forEach(l => {
                if (l.getAttribute('data-section') === targetSectionId) {
                    l.classList.add('active');
                }
            });
            const targetSection = document.getElementById(targetSectionId);
            if (targetSection) {
                targetSection.classList.add('active');
                // Re-inicializa AOS para a nova seção visível
                if (window.AOS) window.AOS.refresh();
            }
        });
    });

    // =========================================================================
    // POPULAR SELECTS DOS FORMULÁRIOS ADMIN
    // =========================================================================
    function populateAdminSelects() {
        const profSelect = document.getElementById('select-prof-turma');
        if (profSelect) {
            const professores = Database.get('professores');
            profSelect.innerHTML = '<option value="" selected disabled>Escolha um Professor...</option>' + 
                professores.map(p => `<option value="${p.id}">${p.nome}</option>`).join('');
        }

        const respSelect = document.getElementById('select-aluno-resp');
        if (respSelect) {
            const responsaveis = Database.get('responsaveis');
            respSelect.innerHTML = '<option value="" selected disabled>Escolha um Responsável...</option>' + 
                responsaveis.map(r => `<option value="${r.id}">${r.nome}</option>`).join('');
        }

        const alunoMatSelect = document.getElementById('select-aluno-mat');
        const turmaMatSelect = document.getElementById('select-turma-mat');
        if (alunoMatSelect && turmaMatSelect) {
            const alunos = Database.get('alunos');
            const turmas = Database.get('turmas');
            alunoMatSelect.innerHTML = '<option value="" selected disabled>Escolha o Aluno...</option>' + 
                alunos.map(a => `<option value="${a.id}">${a.nome}</option>`).join('');
            turmaMatSelect.innerHTML = '<option value="" selected disabled>Escolha a Turma...</option>' + 
                turmas.map(t => `<option value="${t.id}">${t.nomeTurma}</option>`).join('');
        }
    }

    populateAdminSelects();

    window.enviarPreMatriculaFilho = function() {
        const nome = document.getElementById('filho-nome').value.trim();
        const nascimento = document.getElementById('filho-nascimento').value;
        
        let valido = true;
        if (!nome) {
            document.getElementById('filho-nome').classList.add('is-invalid');
            valido = false;
        }
        if (!nascimento) {
            document.getElementById('filho-nascimento').classList.add('is-invalid');
            valido = false;
        }

        if (!valido) {
            alert("Por favor, preencha o Nome e a Data de Nascimento.");
            return;
        }

        // Fecha o modal de pre-matricula
        const modalEl = document.getElementById('modalPreMatricula');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        // Mostra o modal de sucesso com mensagem personalizada
        const successModalEl = document.getElementById('modalSucesso');
        const msgEl = document.getElementById('modal-sucesso-msg');
        if (msgEl) msgEl.textContent = "Pré-matrícula enviada com sucesso!";
        
        const successModal = new bootstrap.Modal(successModalEl);
        successModal.show();

        // Limpa o formulario
        const form = document.getElementById('preMatriculaForm');
        if (form) form.reset();
    };

    window.salvarDiario = function() {
        const alunoSelect = document.getElementById('select-aluno-diario');
        const obsTextarea = document.getElementById('diario-obs');
        
        let valido = true;
        if (!alunoSelect || !alunoSelect.value) {
            if (alunoSelect) alunoSelect.classList.add('is-invalid');
            valido = false;
        } else {
            if (alunoSelect) alunoSelect.classList.remove('is-invalid');
        }

        if (!obsTextarea || !obsTextarea.value.trim()) {
            if (obsTextarea) obsTextarea.classList.add('is-invalid');
            valido = false;
        } else {
            if (obsTextarea) obsTextarea.classList.remove('is-invalid');
        }

        if (!valido) {
            alert("Por favor, preencha todos os campos obrigatórios em vermelho.");
            return;
        }

        // Mostra o modal de sucesso com mensagem personalizada
        const successModalEl = document.getElementById('modalSucesso');
        const msgEl = document.getElementById('modal-sucesso-msg');
        if (msgEl) msgEl.textContent = "Diário de bordo enviado com sucesso!";
        
        const successModal = new bootstrap.Modal(successModalEl);
        successModal.show();

        // Limpa observações e inputs adicionais
        if (obsTextarea) obsTextarea.value = "";
        const sonoInput = document.getElementById('diario-sono');
        if (sonoInput) sonoInput.value = "";
        const fraldasInput = document.getElementById('diario-fraldas');
        if (fraldasInput) fraldasInput.value = "";
    };

    window.salvarPlanejamento = function() {
        const tituloInput = document.getElementById('plano-titulo');
        const descTextarea = document.getElementById('plano-desc');
        const dataInput = document.getElementById('plano-data');
        
        let valido = true;
        
        if (!tituloInput || !tituloInput.value.trim()) {
            if (tituloInput) tituloInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (tituloInput) tituloInput.classList.remove('is-invalid');
        }

        if (!descTextarea || !descTextarea.value.trim()) {
            if (descTextarea) descTextarea.classList.add('is-invalid');
            valido = false;
        } else {
            if (descTextarea) descTextarea.classList.remove('is-invalid');
        }

        if (!dataInput || !dataInput.value) {
            if (dataInput) dataInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (dataInput) dataInput.classList.remove('is-invalid');
        }

        if (!valido) {
            alert("Por favor, preencha todos os campos obrigatórios em vermelho.");
            return;
        }

        // Fecha o modal de planejamento
        const modalEl = document.getElementById('modalPlanejamento');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        // Mostra o modal de sucesso com mensagem personalizada
        const successModalEl = document.getElementById('modalSucesso');
        const msgEl = document.getElementById('modal-sucesso-msg');
        if (msgEl) msgEl.textContent = "Planejamento salvo com sucesso!";
        
        const successModal = new bootstrap.Modal(successModalEl);
        successModal.show();

        // Limpa campos
        if (tituloInput) tituloInput.value = "";
        if (descTextarea) descTextarea.value = "";
        if (dataInput) dataInput.value = "";
    };

    window.salvarAutorizado = function() {
        const nomeInput = document.getElementById('aut-nome');
        const cpfInput = document.getElementById('aut-cpf');
        const parentescoInput = document.getElementById('aut-parentesco');
        const telInput = document.getElementById('aut-telefone');
        
        let valido = true;
        
        if (!nomeInput || !nomeInput.value.trim()) {
            if (nomeInput) nomeInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (nomeInput) nomeInput.classList.remove('is-invalid');
        }

        if (!cpfInput || !cpfInput.value.trim()) {
            if (cpfInput) cpfInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (cpfInput) cpfInput.classList.remove('is-invalid');
        }

        if (!parentescoInput || !parentescoInput.value.trim()) {
            if (parentescoInput) parentescoInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (parentescoInput) parentescoInput.classList.remove('is-invalid');
        }

        if (!telInput || !telInput.value.trim()) {
            if (telInput) telInput.classList.add('is-invalid');
            valido = false;
        } else {
            if (telInput) telInput.classList.remove('is-invalid');
        }

        if (!valido) {
            alert("Por favor, preencha todos os campos obrigatórios em vermelho.");
            return;
        }

        // Fecha o modal de autorizado
        const modalEl = document.getElementById('modalAutorizado');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        // Mostra o modal de sucesso com mensagem personalizada
        const successModalEl = document.getElementById('modalSucesso');
        const msgEl = document.getElementById('modal-sucesso-msg');
        if (msgEl) msgEl.textContent = "Nova pessoa autorizada com sucesso!";
        
        const successModal = new bootstrap.Modal(successModalEl);
        successModal.show();

        // Limpa campos
        if (nomeInput) nomeInput.value = "";
        if (cpfInput) cpfInput.value = "";
        if (parentescoInput) parentescoInput.value = "";
        if (telInput) telInput.value = "";
    };

    // Limpar validações vermelhas em tempo real ao interagir (para todos os painéis)
    document.querySelectorAll('input, select, textarea').forEach(el => {
        el.addEventListener('input', function() {
            if (this.value.trim()) this.classList.remove('is-invalid');
        });
        el.addEventListener('change', function() {
            if (this.value) this.classList.remove('is-invalid');
        });
    });
});
