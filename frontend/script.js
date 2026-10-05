const api = 'http://127.0.0.1:8080/api';
let cavalos = [];
const moeda = v => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const escapar = v => String(v).replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));

async function requisitar(url, opcoes = {}) {
  const resposta = await fetch(url, opcoes);
  const dados = await resposta.json();
  if (!resposta.ok) throw Error(dados.erro || 'Erro na operação.');
  return dados;
}

async function carregar() {
  try { cavalos = await requisitar(`${api}/cavalos`); renderizar(); }
  catch (_) { document.querySelector('#catalogo').innerHTML = '<p>Backend offline. Execute o programa Java antes de usar o catálogo.</p>'; }
}

function renderizar() {
  const busca = document.querySelector('#busca').value.toLowerCase();
  const lista = cavalos.filter(c => c.nome.toLowerCase().includes(busca));
  document.querySelector('#catalogo').innerHTML = lista.map(c => `<article class="card"><h3>${escapar(c.nome)}</h3><p>${escapar(c.raca)} · ${c.idade} anos · ${escapar(c.sexo)}</p><div class="price">Lance atual: ${moeda(c.lanceAtual)}</div><button data-id="${c.id}">${c.leilaoEncerrado ? 'Ver histórico' : 'Abrir leilão'}</button></article>`).join('') || '<p>Nenhum cavalo encontrado.</p>';
  document.querySelectorAll('[data-id]').forEach(botao => botao.onclick = () => abrir(Number(botao.dataset.id)));
}

async function abrir(id) {
  try {
    const cavalo = cavalos.find(c => c.id === id);
    const leilao = await requisitar(`${api}/leiloes/${id}`, cavalo.leilaoAtivo || cavalo.leilaoEncerrado ? {} : { method: 'POST' });
    const entrada = prompt(`Leilão de ${leilao.cavalo.nome}\nLance atual: ${moeda(leilao.cavalo.lanceAtual)}\nDigite "historico" ou o valor do lance:`);
    if (entrada === null || entrada.toLowerCase() === 'historico') { alert(leilao.lances.map(x => `${x.participante}: ${moeda(x.valor)}`).join('\n') || 'Nenhum lance.'); return; }
    const participante = prompt('ID do participante:');
    await requisitar(`${api}/leiloes/${id}/lances`, { method: 'POST', headers: {'Content-Type':'application/x-www-form-urlencoded'}, body: `participanteId=${encodeURIComponent(participante)}&valor=${encodeURIComponent(entrada)}` });
    alert('Lance registrado.'); await carregar();
  } catch (erro) { alert(erro.message); }
}

document.querySelector('#busca').addEventListener('input', renderizar);
carregar();
