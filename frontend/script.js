const api = '/api';
let cavalos = [];
let participantes = [];
const $ = (s) => document.querySelector(s);
const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const toast = (msg) => { const el = $('#toast'); el.textContent = msg; el.classList.add('show'); setTimeout(() => el.classList.remove('show'), 3200); };

function validarCPF(cpf) {
    cpf = cpf.replace(/[^\d]+/g, '');

    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }

    let soma = 0;
    let resto;

    for (let i = 1; i <= 9; i++) {
        soma += parseInt(cpf.substring(i - 1, i)) * (11 - i);
    }
    resto = (soma * 10) % 11;
    if ((resto === 10) || (resto === 11)) resto = 0;
    if (resto !== parseInt(cpf.substring(9, 10))) return false;

    soma = 0;
    // Validação do segundo dígito
    for (let i = 1; i <= 10; i++) {
        soma += parseInt(cpf.substring(i - 1, i)) * (12 - i);
    }
    resto = (soma * 10) % 11;
    if ((resto === 10) || (resto === 11)) resto = 0;
    if (resto !== parseInt(cpf.substring(10, 11))) return false;

    return true;
}

async function carregar() {
  try {
    [cavalos, participantes] = await Promise.all([
      fetch(`${api}/cavalos`).then(r => r.json()),
      fetch(`${api}/participantes`).then(r => r.json())
    ]);
    renderizar(cavalos);
  } catch (e) {
    $('#cavalos').innerHTML = '<p class="loading">Não foi possível conectar ao servidor Java.</p>';
  }
}

function renderizar(lista) {
  $('#cavalos').innerHTML = lista.length ? lista.map(c => `
    <article class="card">
      <div class="card-image">♞</div>
      <h3>${c.nome}</h3>
      <p class="meta">${c.raca} • ${c.idade} anos • ${c.sexo}</p>
      <div class="card-bottom">
        <div><span class="status ${c.vendido ? 'closed' : ''}">${c.vendido ? 'Leilão encerrado' : c.leilaoAtivo ? 'Leilão em andamento' : 'Disponível'}</span><div class="price">${moeda(c.lanceAtual)}</div></div>
        <button class="button primary small" onclick="abrirLeilao(${c.id})">${c.vendido ? 'Ver histórico' : 'Ver leilão'} <span>→</span></button>
      </div>
    </article>`).join('') : '<p class="loading">Nenhum cavalo encontrado.</p>';
}

function fecharModal() {
  const modal = $('#modal-leilao');
  if (modal) modal.remove();
}

function mostrarModal(leilao) {
  const temLances = leilao.lances && leilao.lances.length > 0;
  const historico = temLances ? leilao.lances.map(x => `<li><span>${x.participante}</span><strong>${moeda(x.valor)}</strong></li>`).join('') : '<li>Nenhum lance registrado.</li>';
  const participantesOptions = participantes.map(p => `<option value="${p.id}">${p.nome} — ${p.cpf}</option>`).join('');
  const modal = document.createElement('div');
  modal.id = 'modal-leilao';
  modal.className = 'modal-backdrop';
  modal.innerHTML = `<div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" aria-label="Fechar">×</button>
    <p class="eyebrow">LEILÃO DE CAVALO</p>
    <h2>${leilao.cavalo.nome}</h2>
    <p class="modal-meta">${leilao.cavalo.raca} • ${leilao.cavalo.idade} anos • ${leilao.cavalo.sexo}</p>
    <div class="modal-current"><span>Lance atual</span><strong>${moeda(leilao.cavalo.lanceAtual)}</strong></div>
    ${leilao.encerrado ? '<div class="closed-note">Este leilão está encerrado.</div>' : `<form id="form-lance"><label>Participante<select name="participanteId" required>${participantesOptions}</select></label><label>Seu lance<input name="valor" type="number" min="${leilao.cavalo.lanceAtual + 0.01}" step="0.01" placeholder="Digite um valor maior" required></label><button class="button primary" type="submit">Registrar lance <span>→</span></button></form>`}
    <div class="history"><h3>Histórico de lances</h3><ul>${historico}</ul></div>
  </div>`;
  document.body.appendChild(modal);
  modal.querySelector('.modal-close').onclick = fecharModal;
  modal.onclick = (e) => { if (e.target === modal) fecharModal(); };
  const form = modal.querySelector('#form-lance');
  if (form) form.onsubmit = async (e) => {
    e.preventDefault();
    const dados = Object.fromEntries(new FormData(form));
    const resposta = await fetch(`${api}/leiloes/${leilao.cavalo.id}/lances`, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ participanteId: Number(dados.participanteId), valor: Number(dados.valor) }) });
    const resultado = await resposta.json();
    if (!resposta.ok) return toast(resultado.erro);
    toast('Lance registrado com sucesso!');
    fecharModal();
    carregar();
  };
}

async function abrirLeilao(id) {
  try {
    const resposta = await fetch(`${api}/leiloes/${id}`, { method: 'POST' });
    const leilao = await resposta.json();
    if (!resposta.ok) throw new Error(leilao.erro);
    mostrarModal(leilao);
  } catch (e) { toast(e.message || 'Não foi possível abrir o leilão.'); }
}

$('#busca').addEventListener('input', e => renderizar(cavalos.filter(c => c.nome.toLowerCase().includes(e.target.value.toLowerCase()))));
$('#form-participante').addEventListener('submit', async (e) => {
  e.preventDefault();
  const dados = Object.fromEntries(new FormData(e.target));
  if (!validarCPF(dados.cpf)) return toast('CPF inválido. Confira os números digitados.');
  const r = await fetch(`${api}/participantes`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(dados) });
  const resposta = await r.json();
  if (!r.ok) return toast(resposta.erro);
  e.target.reset(); toast('Participante cadastrado com sucesso!'); carregar();
});
carregar();