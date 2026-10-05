# Leilão de Cavalos

Projeto de console em Java para praticar classes, objetos, encapsulamento, construtores, arrays, `String`, `Scanner`, condicionais, laços e exceções personalizadas.

## Como executar no PowerShell

Na pasta `leilao-cavalos`:

```powershell
$fontes = Get-ChildItem backend/src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 --release 17 -d backend/out $fontes
java -cp backend/out Main
```

## Como usar

O programa mostra um menu no terminal. É possível listar e pesquisar cavalos, iniciar leilões, cadastrar participantes, registrar e consultar lances, além de encerrar e reabrir leilões.

Os dados ficam em arrays durante a execução e são reiniciados quando o programa termina. Não há servidor web, banco de dados, autenticação ou bibliotecas externas.

## Estrutura

- `backend/src/Main.java`: menu, entrada com `Scanner` e fluxo principal;
- `backend/src/model`: classes `Cavalo`, `Participante`, `Lance` e `Leilao`;
- `backend/src/service/LeilaoService.java`: cadastro e buscas usando arrays;
- `backend/src/exception`: exceções específicas do domínio.
