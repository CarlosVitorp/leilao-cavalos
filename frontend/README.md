# Frontend JavaFX

Este modulo substitui a interface web antiga por uma aplicacao desktop JavaFX. A interface web anterior foi preservada em `../frontend-web-legacy/`. O aplicativo consome a API em `http://127.0.0.1:8080/api`.

## Pre-requisitos

- JDK 17 ou superior;
- Maven 3.9 ou superior;
- backend compilado em `../backend/out`.

## Executar

Compile o backend uma vez, a partir desta pasta:

```powershell
$fontes = Get-ChildItem ..\backend\src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 --release 17 -d ..\backend\out $fontes
```

Depois execute:

```powershell
mvn javafx:run
```

O JavaFX verifica a API na porta 8080 e inicia automaticamente o backend compilado quando ele ainda nao estiver rodando. Se preferir, o backend tambem pode ser iniciado manualmente com `java -cp ..\backend\out Main`.

A aplicacao mantem as operacoes da interface antiga: cadastro de participantes, pesquisa de cavalos, registro de lances, historico, encerramento, reabertura e atualizacao do catalogo.
