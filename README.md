# Leilao de Cavalos

Projeto de console em Java para praticar classes, objetos, encapsulamento, construtores, arrays, `String`, `Scanner`, condicionais, lacos e excecoes personalizadas.

## Como executar o backend no PowerShell

Na pasta `leilao-cavalos`:

```powershell
$fontes = Get-ChildItem backend/src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 --release 17 -d backend/out $fontes
java -cp backend/out Main
```

O backend inicia uma API HTTP na porta 8080. Ele tambem continua exibindo o menu de console para as operacoes do dominio.

## Frontend JavaFX

O frontend principal desta branch esta em `frontend/` e usa JavaFX para consumir a API HTTP do backend. Consulte [`frontend/README.md`](frontend/README.md) para executar com Maven.

A interface web anterior foi preservada em `frontend-web-legacy/` como referencia.

## Como usar

A aplicacao JavaFX permite cadastrar participantes, pesquisar cavalos, registrar lances, consultar historicos, encerrar e reabrir leiloes. Os dados ficam em arrays durante a execucao e sao reiniciados quando o backend termina ou quando a opcao de reset e acionada.

## Estrutura

- `backend/src/Main.java`: menu, API HTTP, entrada com `Scanner` e fluxo principal;
- `backend/src/model`: classes `Cavalo`, `Participante`, `Lance` e `Leilao`;
- `backend/src/service/LeilaoService.java`: cadastro e buscas usando arrays;
- `backend/src/exception`: excecoes especificas do dominio;
- `frontend`: aplicacao desktop JavaFX;
- `frontend-web-legacy`: interface web antiga preservada para referencia.
