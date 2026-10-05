const cavalos=[{nome:'Trovão Negro',raca:'Quarto de Milha',idade:5,sexo:'Macho',valor:10000},{nome:'Relâmpago',raca:'Mangalarga',idade:7,sexo:'Macho',valor:15000},{nome:'Estrela',raca:'Crioulo',idade:4,sexo:'Fêmea',valor:12500}];
const moeda=v=>v.toLocaleString('pt-BR',{style:'currency',currency:'BRL'});
const escapar=v=>String(v).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function renderizar(){const busca=document.querySelector('#busca').value.toLowerCase();document.querySelector('#catalogo').innerHTML=cavalos.filter(c=>c.nome.toLowerCase().includes(busca)).map(c=>`<article class="card"><h3>${escapar(c.nome)}</h3><p>${escapar(c.raca)} · ${c.idade} anos · ${escapar(c.sexo)}</p><div class="price">Valor inicial: ${moeda(c.valor)}</div></article>`).join('')||'<p>Nenhum cavalo encontrado.</p>';}
document.querySelector('#busca').addEventListener('input',renderizar);renderizar();
