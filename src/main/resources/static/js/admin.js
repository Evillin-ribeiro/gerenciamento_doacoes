// Painel administrativo: toda a protecao real acontece nas chamadas fetch() abaixo,
// que anexam o token JWT (guardado em sessionStorage apos o login) no header Authorization.

function getToken() { return sessionStorage.getItem('token'); }
function getRole() { return sessionStorage.getItem('role'); }
function getUsuarioId() { return sessionStorage.getItem('usuarioId'); }

function api(path, options) {
    options = options || {};
    options.headers = Object.assign({}, options.headers, { 'Authorization': 'Bearer ' + getToken() });
    return fetch(path, options).then(function (resp) {
        if (resp.status === 401) {
            sessionStorage.clear();
            window.location.href = '/admin/login';
            throw new Error('Sessao expirada');
        }
        return resp;
    });
}

function apiJson(path, method, body) {
    return api(path, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined
    });
}

function formatarData(iso) {
    if (!iso) return '';
    return iso.replace('T', ' ').substring(0, 16);
}

function escapeHtml(texto) {
    const div = document.createElement('div');
    div.textContent = texto == null ? '' : String(texto);
    return div.innerHTML;
}

// ---------- Helpers visuais compartilhados por todas as secoes ----------
function pageHead(titulo, subtext) {
    return '<div class="page-head"><div><h1>' + escapeHtml(titulo) + '</h1>' +
        (subtext ? '<p class="subtext">' + escapeHtml(subtext) + '</p>' : '') + '</div></div>';
}

const TIPO_ICONS = {
    ALIMENTO: '<path d="M4 3v18M4 3c3 0 3 3 6 3M4 9c3 0 3-3 6-3M20 3c-2.5 0-4 2-4 5s1.5 5 4 5M18 13v8"/>',
    FINANCEIRA: '<circle cx="12" cy="12" r="9"/><path d="M9 15c0 1.5 1.3 2 3 2s3-.7 3-2-1.3-1.8-3-2-3-.5-3-2 1.3-2 3-2 3 .5 3 2"/>',
    ROUPA: '<path d="M8 4l4 2 4-2 4 3-2 3-2-1v9H8V9L6 10 4 7z"/>',
    ITEM_DIVERSO: '<rect x="4" y="8" width="16" height="12" rx="1"/><path d="M4 8l8-4 8 4M12 4v16"/>'
};
const TIPO_LABELS = { ALIMENTO: 'Alimento', FINANCEIRA: 'Financeira', ROUPA: 'Roupa', ITEM_DIVERSO: 'Item diverso' };

function tipoCell(tipo) {
    return '<span class="tipo-cell det"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">' +
        (TIPO_ICONS[tipo] || '') + '</svg>' + (TIPO_LABELS[tipo] || tipo) + '</span>';
}

const STATUS_BADGES = { PENDENTE: 'pend', CONFIRMADA: 'conf', CANCELADA: 'canc', ENTREGUE: 'entregue' };

const DIA_SEMANA_LABELS = {
    MONDAY: 'Segunda-feira', TUESDAY: 'Terça-feira', WEDNESDAY: 'Quarta-feira',
    THURSDAY: 'Quinta-feira', FRIDAY: 'Sexta-feira', SATURDAY: 'Sábado', SUNDAY: 'Domingo'
};
function statusBadge(status) {
    const cls = STATUS_BADGES[status] || 'entregue';
    const label = status.charAt(0) + status.slice(1).toLowerCase();
    return '<span class="badge-status ' + cls + '">' + label + '</span>';
}

// ---------- Secao: Doacoes ----------
let doacoesCache = [];

function renderDoacoes() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Doações', 'Confirme ou cancele as doações recebidas pela associação.') +
        '<div id="msgDoacoes"></div>' +
        '<div class="stats">' +
        '<div class="stat"><span class="dot pend"></span><b id="statPend">0</b>&nbsp;pendentes</div>' +
        '<div class="stat"><span class="dot conf"></span><b id="statConf">0</b>&nbsp;confirmadas</div>' +
        '<div class="stat"><span class="dot canc"></span><b id="statCanc">0</b>&nbsp;canceladas</div>' +
        '</div>' +
        '<div class="toolbar">' +
        '<input type="search" id="buscaDoacao" placeholder="Buscar por detalhe ou tipo…" aria-label="Buscar doações">' +
        '<select id="filtroTipo" aria-label="Filtrar por tipo"><option value="">Todos os tipos</option>' +
        Object.keys(TIPO_LABELS).map(function (t) { return '<option value="' + t + '">' + TIPO_LABELS[t] + '</option>'; }).join('') +
        '</select>' +
        '<select id="filtroStatus" aria-label="Filtrar por status"><option value="">Todos os status</option>' +
        '<option value="PENDENTE">Pendente</option><option value="CONFIRMADA">Confirmada</option>' +
        '<option value="ENTREGUE">Entregue</option><option value="CANCELADA">Cancelada</option></select>' +
        '</div>' +
        '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Tipo</th><th>Status</th><th>Data</th><th>Detalhe</th><th>Ações</th></tr></thead>' +
        '<tbody id="corpoDoacoes"></tbody></table></div>' +
        '<div class="foot-row"><span id="contadorDoacoes"></span></div>';

    document.getElementById('buscaDoacao').addEventListener('input', filtrarDoacoes);
    document.getElementById('filtroTipo').addEventListener('change', filtrarDoacoes);
    document.getElementById('filtroStatus').addEventListener('change', filtrarDoacoes);

    api('/api/doacoes').then(function (r) { return r.json(); }).then(function (doacoes) {
        doacoesCache = doacoes;
        filtrarDoacoes();
    });
}

function detalheDoacao(d) {
    return d.tipo === 'FINANCEIRA'
        ? ('R$ ' + (d.valor || '0'))
        : (d.itens || []).map(function (i) { return i.quantidade + ' ' + i.unidadeMedida + ' ' + i.descricaoItem; }).join(', ');
}

function filtrarDoacoes() {
    const q = document.getElementById('buscaDoacao').value.trim().toLowerCase();
    const tipo = document.getElementById('filtroTipo').value;
    const status = document.getElementById('filtroStatus').value;

    const filtradas = doacoesCache.filter(function (d) {
        const detalhe = detalheDoacao(d).toLowerCase();
        const matchQ = !q || detalhe.includes(q) || (TIPO_LABELS[d.tipo] || '').toLowerCase().includes(q);
        const matchTipo = !tipo || d.tipo === tipo;
        const matchStatus = !status || d.status === status;
        return matchQ && matchTipo && matchStatus;
    });

    document.getElementById('corpoDoacoes').innerHTML = filtradas.map(function (d) {
        return '<tr class="' + (d.status === 'CANCELADA' ? 'is-cancelled' : '') + '">' +
            '<td class="id-cell">' + d.id + '</td>' +
            '<td>' + tipoCell(d.tipo) + '</td>' +
            '<td>' + statusBadge(d.status) + '</td>' +
            '<td>' + formatarData(d.dataCriacao) + '</td>' +
            '<td class="det">' + escapeHtml(detalheDoacao(d)) + '</td>' +
            '<td>' + (d.status === 'PENDENTE'
                ? '<div class="actions"><button class="btn-confirm" onclick="confirmarDoacao(' + d.id + ')">Confirmar</button>' +
                    '<button class="btn-cancel" onclick="cancelarDoacao(' + d.id + ')">Cancelar</button></div>'
                : '') + '</td></tr>';
    }).join('');

    document.getElementById('contadorDoacoes').textContent = 'Mostrando ' + filtradas.length + ' de ' + doacoesCache.length + ' registros';
    document.getElementById('statPend').textContent = doacoesCache.filter(function (d) { return d.status === 'PENDENTE'; }).length;
    document.getElementById('statConf').textContent = doacoesCache.filter(function (d) { return d.status === 'CONFIRMADA'; }).length;
    document.getElementById('statCanc').textContent = doacoesCache.filter(function (d) { return d.status === 'CANCELADA'; }).length;
}

function confirmarDoacao(id) {
    apiJson('/api/doacoes/' + id + '/confirmar', 'POST', { usuarioId: Number(getUsuarioId()) })
        .then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderDoacoes();
        })
        .catch(function (e) { document.getElementById('msgDoacoes').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
}

function cancelarDoacao(id) {
    api('/api/doacoes/' + id + '/cancelar', { method: 'POST' })
        .then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderDoacoes();
        })
        .catch(function (e) { document.getElementById('msgDoacoes').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
}

// ---------- Secao: Estoque ----------
let estoqueCache = [];

function renderEstoque() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Estoque', 'Catálogo de itens e saldo atual disponível para distribuição.') +
        '<div id="msgEstoque"></div>' +
        '<div class="card-form">' +
        '<form id="formNovoItem" class="row g-2">' +
        '<div class="col-12 col-md-3"><input class="form-control" name="descricaoItem" placeholder="Descrição" required></div>' +
        '<div class="col-12 col-md-3"><input class="form-control" name="categoria" placeholder="Categoria" required></div>' +
        '<div class="col-12 col-md-3"><input class="form-control" name="unidadeMedida" placeholder="Unidade (kg, peça...)" required></div>' +
        '<div class="col-12 col-md-3"><button class="btn-confirm w-100" style="padding:9px;" type="submit">Adicionar item</button></div>' +
        '</form></div>' +
        '<div class="toolbar"><input type="search" id="buscaEstoque" placeholder="Buscar item…" aria-label="Buscar itens"></div>' +
        '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Descrição</th><th>Categoria</th><th>Unidade</th><th>Saldo</th></tr></thead>' +
        '<tbody id="corpoEstoque"></tbody></table></div>' +
        '<div class="foot-row"><span id="contadorEstoque"></span></div>';

    document.getElementById('buscaEstoque').addEventListener('input', filtrarEstoque);

    document.getElementById('formNovoItem').addEventListener('submit', function (ev) {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        apiJson('/api/estoque', 'POST', {
            descricaoItem: fd.get('descricaoItem'), categoria: fd.get('categoria'), unidadeMedida: fd.get('unidadeMedida')
        }).then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderEstoque();
        }).catch(function (e) { document.getElementById('msgEstoque').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
    });

    api('/api/estoque').then(function (r) { return r.json(); }).then(function (itens) {
        estoqueCache = itens;
        filtrarEstoque();
    });
}

function filtrarEstoque() {
    const q = document.getElementById('buscaEstoque').value.trim().toLowerCase();
    const filtrados = estoqueCache.filter(function (i) {
        return !q || i.descricaoItem.toLowerCase().includes(q) || i.categoria.toLowerCase().includes(q);
    });
    document.getElementById('corpoEstoque').innerHTML = filtrados.map(function (i) {
        return '<tr><td class="id-cell">' + i.id + '</td><td>' + escapeHtml(i.descricaoItem) + '</td><td>' + escapeHtml(i.categoria) +
            '</td><td>' + escapeHtml(i.unidadeMedida) + '</td><td>' + i.quantidadeAtual + '</td></tr>';
    }).join('');
    document.getElementById('contadorEstoque').textContent = 'Mostrando ' + filtrados.length + ' de ' + estoqueCache.length + ' itens';
}

// ---------- Secao: Distribuicoes ----------
function renderDistribuicoes() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Distribuições', 'Registre a saída de itens do estoque para os beneficiários.') +
        '<div id="msgDistribuicao"></div>' +
        '<div class="card-form">' +
        '<form id="formNovaDistribuicao">' +
        '<div class="row g-2 mb-2">' +
        '<div class="col-12 col-md-6"><input class="form-control" name="beneficiario" placeholder="Beneficiário" required></div>' +
        '<div class="col-12 col-md-6"><input class="form-control" name="observacao" placeholder="Observação"></div>' +
        '</div>' +
        '<div id="itensDistribuicao"></div>' +
        '<button class="btn-confirm mt-2" style="padding:9px 18px;" type="submit">Registrar distribuição</button>' +
        '</form></div>' +
        '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Data</th><th>Beneficiário</th><th>Itens</th></tr></thead>' +
        '<tbody id="listaDistribuicoes">Carregando...</tbody></table></div>';

    api('/api/estoque').then(function (r) { return r.json(); }).then(function (itensEstoque) {
        let opcoes = '<option value="">-- item --</option>';
        itensEstoque.forEach(function (i) { opcoes += '<option value="' + i.id + '">' + escapeHtml(i.descricaoItem) + ' (' + escapeHtml(i.unidadeMedida) + ', saldo ' + i.quantidadeAtual + ')</option>'; });

        let linhas = '';
        for (let n = 1; n <= 3; n++) {
            linhas += '<div class="row g-2 mb-2">' +
                '<div class="col-8"><select class="form-select" name="estoqueId' + n + '">' + opcoes + '</select></div>' +
                '<div class="col-4"><input type="number" step="0.001" min="0" class="form-control" name="quantidade' + n + '" placeholder="Quantidade"></div>' +
                '</div>';
        }
        document.getElementById('itensDistribuicao').innerHTML = linhas;
    });

    document.getElementById('formNovaDistribuicao').addEventListener('submit', function (ev) {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        const itens = [];
        for (let n = 1; n <= 3; n++) {
            const estoqueId = fd.get('estoqueId' + n);
            const quantidade = fd.get('quantidade' + n);
            if (estoqueId && quantidade) { itens.push({ estoqueId: Number(estoqueId), quantidade: Number(quantidade) }); }
        }
        apiJson('/api/distribuicoes', 'POST', {
            usuarioId: Number(getUsuarioId()), beneficiario: fd.get('beneficiario'), observacao: fd.get('observacao'), itens: itens
        }).then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderDistribuicoes();
        }).catch(function (e) { document.getElementById('msgDistribuicao').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
    });

    api('/api/distribuicoes').then(function (r) { return r.json(); }).then(function (lista) {
        let html = '';
        lista.forEach(function (d) {
            const itens = (d.itens || []).map(function (i) { return i.quantidade + ' ' + i.unidadeMedida + ' ' + i.descricaoItem; }).join(', ');
            html += '<tr><td class="id-cell">' + d.id + '</td><td>' + formatarData(d.data) + '</td><td>' + escapeHtml(d.beneficiario) + '</td><td class="det">' + escapeHtml(itens) + '</td></tr>';
        });
        document.getElementById('listaDistribuicoes').innerHTML = html || '<tr><td colspan="4" class="text-center text-muted">Nenhuma distribuição registrada.</td></tr>';
    });
}

// ---------- Secao: Relatorio ----------
function renderRelatorio() {
    const conteudo = document.getElementById('conteudo');
    const hoje = new Date().toISOString().substring(0, 10);
    conteudo.innerHTML = pageHead('Relatório de movimentações', 'Prestação de contas: entradas/saídas de estoque e total arrecadado no período.') +
        '<div class="card-form">' +
        '<form id="formRelatorio" class="row g-2">' +
        '<div class="col-12 col-md-4"><label class="form-label">De</label><input type="date" class="form-control" name="periodoInicio" value="' + hoje + '" required></div>' +
        '<div class="col-12 col-md-4"><label class="form-label">Até</label><input type="date" class="form-control" name="periodoFim" value="' + hoje + '" required></div>' +
        '<div class="col-12 col-md-4 d-flex align-items-end"><button class="btn-confirm w-100" style="padding:9px;" type="submit">Gerar</button></div>' +
        '</form></div>' +
        '<div id="resultadoRelatorio"></div>';

    document.getElementById('formRelatorio').addEventListener('submit', function (ev) {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        const qs = 'periodoInicio=' + fd.get('periodoInicio') + '&periodoFim=' + fd.get('periodoFim');
        api('/api/relatorios/movimentacoes?' + qs).then(function (r) { return r.json(); }).then(function (rel) {
            let html = '<div class="stats"><div class="stat"><span class="dot conf"></span>Total arrecadado (financeiro): <b>R$ ' + rel.totalArrecadadoFinanceiro + '</b></div></div>';
            html += '<div class="table-wrap"><table><thead><tr><th>Item</th><th>Entradas</th><th>Saídas</th><th>Saldo atual</th></tr></thead><tbody>';
            (rel.itensEstoque || []).forEach(function (i) {
                html += '<tr><td>' + escapeHtml(i.descricaoItem) + '</td><td>' + i.totalEntradas + '</td><td>' + i.totalSaidas + '</td><td>' + i.saldoAtual + '</td></tr>';
            });
            if (!rel.itensEstoque || rel.itensEstoque.length === 0) {
                html += '<tr><td colspan="4" class="text-center text-muted">Nenhuma movimentação no período.</td></tr>';
            }
            html += '</tbody></table></div>';
            document.getElementById('resultadoRelatorio').innerHTML = html;
        });
    });
}

// ---------- Secao: Horarios (ADMIN) ----------
function renderHorarios() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Horários de Atendimento', 'Configure os horários e a capacidade de agendamento presencial.') +
        '<div id="msgHorarios"></div>' +
        '<div class="card-form">' +
        '<form id="formNovoHorario" class="row g-2">' +
        '<div class="col-12 col-md-3"><select class="form-select" name="diaSemana" required>' +
        Object.keys(DIA_SEMANA_LABELS).map(function (d) { return '<option value="' + d + '">' + DIA_SEMANA_LABELS[d] + '</option>'; }).join('') +
        '</select></div>' +
        '<div class="col-12 col-md-3"><input type="time" class="form-control" name="horaInicio" required></div>' +
        '<div class="col-12 col-md-3"><input type="time" class="form-control" name="horaFim" required></div>' +
        '<div class="col-12 col-md-3"><input type="number" min="1" class="form-control" name="capacidadeMaxima" placeholder="Capacidade" required></div>' +
        '<div class="col-12"><button class="btn-confirm" style="padding:9px 18px;" type="submit">Adicionar horário</button></div>' +
        '</form></div>' +
        '<div class="table-wrap"><table><thead><tr><th>Dia</th><th>Início</th><th>Fim</th><th>Capacidade</th></tr></thead>' +
        '<tbody id="listaHorarios">Carregando...</tbody></table></div>';

    document.getElementById('formNovoHorario').addEventListener('submit', function (ev) {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        apiJson('/api/horarios-atendimento', 'POST', {
            diaSemana: fd.get('diaSemana'), horaInicio: fd.get('horaInicio') + ':00', horaFim: fd.get('horaFim') + ':00',
            capacidadeMaxima: Number(fd.get('capacidadeMaxima'))
        }).then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderHorarios();
        }).catch(function (e) { document.getElementById('msgHorarios').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
    });

    api('/api/horarios-atendimento').then(function (r) { return r.json(); }).then(function (lista) {
        document.getElementById('listaHorarios').innerHTML = lista.map(function (h) {
            return '<tr><td>' + (DIA_SEMANA_LABELS[h.diaSemana] || h.diaSemana) + '</td><td>' + h.horaInicio + '</td><td>' + h.horaFim + '</td><td>' + h.capacidadeMaxima + '</td></tr>';
        }).join('') || '<tr><td colspan="4" class="text-center text-muted">Nenhum horário cadastrado.</td></tr>';
    });
}

// ---------- Secao: Usuarios (ADMIN) ----------
function renderUsuarios() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Usuários', 'Voluntários e administradores com acesso ao painel.') +
        '<div id="msgUsuarios"></div>' +
        '<div class="card-form">' +
        '<form id="formNovoUsuario" class="row g-2">' +
        '<div class="col-12 col-md-3"><input class="form-control" name="nome" placeholder="Nome" required></div>' +
        '<div class="col-12 col-md-3"><input type="email" class="form-control" name="email" placeholder="E-mail" required></div>' +
        '<div class="col-12 col-md-3"><input type="password" class="form-control" name="senha" placeholder="Senha (mín. 6)" required></div>' +
        '<div class="col-12 col-md-2"><select class="form-select" name="role"><option value="VOLUNTARIO">Voluntário</option><option value="ADMIN">Admin</option></select></div>' +
        '<div class="col-12 col-md-1"><button class="btn-confirm w-100" style="padding:9px;" type="submit">+</button></div>' +
        '</form></div>' +
        '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Nome</th><th>E-mail</th><th>Papel</th></tr></thead>' +
        '<tbody id="listaUsuarios">Carregando...</tbody></table></div>';

    document.getElementById('formNovoUsuario').addEventListener('submit', function (ev) {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        apiJson('/api/usuarios', 'POST', {
            nome: fd.get('nome'), email: fd.get('email'), senha: fd.get('senha'), role: fd.get('role')
        }).then(function (r) {
            if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
            renderUsuarios();
        }).catch(function (e) { document.getElementById('msgUsuarios').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
    });

    api('/api/usuarios').then(function (r) { return r.json(); }).then(function (lista) {
        document.getElementById('listaUsuarios').innerHTML = lista.map(function (u) {
            return '<tr><td class="id-cell">' + u.id + '</td><td>' + escapeHtml(u.nome) + '</td><td>' + escapeHtml(u.email) + '</td><td>' + u.role + '</td></tr>';
        }).join('');
    });
}

// ---------- Secao: Dados Bancarios (ADMIN) ----------
function renderDadosBancarios() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = pageHead('Dados Bancários / Pix', 'Informações exibidas ao doador na doação financeira.') +
        '<div id="msgDadosBancarios"></div><div class="card-form" id="formDadosBancarios">Carregando...</div>';

    api('/api/dados-bancarios').then(function (r) { return r.json(); }).then(function (d) {
        document.getElementById('formDadosBancarios').innerHTML =
            '<form id="formDb" class="row g-2">' +
            '<div class="col-12 col-md-6"><label class="form-label">Banco</label><input class="form-control" name="banco" value="' + escapeHtml(d.banco) + '" required></div>' +
            '<div class="col-12 col-md-3"><label class="form-label">Agência</label><input class="form-control" name="agencia" value="' + escapeHtml(d.agencia) + '" required></div>' +
            '<div class="col-12 col-md-3"><label class="form-label">Conta</label><input class="form-control" name="conta" value="' + escapeHtml(d.conta) + '" required></div>' +
            '<div class="col-12 col-md-6"><label class="form-label">Chave Pix</label><input class="form-control" name="chavePix" value="' + escapeHtml(d.chavePix) + '" required></div>' +
            '<div class="col-12 col-md-6"><label class="form-label">Titular</label><input class="form-control" name="titular" value="' + escapeHtml(d.titular) + '" required></div>' +
            '<div class="col-12"><button class="btn-confirm mt-2" style="padding:9px 18px;" type="submit">Salvar</button></div>' +
            '</form>';

        document.getElementById('formDb').addEventListener('submit', function (ev) {
            ev.preventDefault();
            const fd = new FormData(ev.target);
            apiJson('/api/dados-bancarios', 'PUT', {
                banco: fd.get('banco'), agencia: fd.get('agencia'), conta: fd.get('conta'),
                chavePix: fd.get('chavePix'), titular: fd.get('titular')
            }).then(function (r) {
                if (!r.ok) { return r.json().then(function (e) { throw new Error(e.message); }); }
                document.getElementById('msgDadosBancarios').innerHTML = '<div class="alert alert-success">Salvo com sucesso.</div>';
            }).catch(function (e) { document.getElementById('msgDadosBancarios').innerHTML = '<div class="alert alert-danger">' + escapeHtml(e.message) + '</div>'; });
        });
    });
}

const SECOES = {
    doacoes: renderDoacoes, estoque: renderEstoque, distribuicoes: renderDistribuicoes,
    relatorio: renderRelatorio, horarios: renderHorarios, usuarios: renderUsuarios, dadosBancarios: renderDadosBancarios
};

function irParaSecao(secao) {
    document.querySelectorAll('#menuAdmin a').forEach(function (link) {
        link.classList.toggle('active', link.dataset.secao === secao);
    });
    SECOES[secao]();
}

document.addEventListener('DOMContentLoaded', function () {
    if (!document.getElementById('conteudo')) { return; } // pagina de login nao tem dashboard

    if (!getToken()) {
        window.location.href = '/admin/login';
        return;
    }

    document.getElementById('usuarioLogado').innerHTML = escapeHtml(sessionStorage.getItem('nome')) + ' <b>(' + escapeHtml(getRole()) + ')</b>';
    if (getRole() === 'ADMIN') {
        document.querySelectorAll('.admin-only').forEach(function (el) { el.style.display = ''; });
    }

    document.getElementById('btnSair').addEventListener('click', function () {
        sessionStorage.clear();
        window.location.href = '/admin/login';
    });

    document.querySelectorAll('#menuAdmin a').forEach(function (link) {
        link.addEventListener('click', function (ev) {
            ev.preventDefault();
            irParaSecao(link.dataset.secao);
        });
    });

    irParaSecao('doacoes');
});
