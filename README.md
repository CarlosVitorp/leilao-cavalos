# Leilão de Cavalos

Projeto acadêmico baseado no PDF fornecido, com backend Java puro e frontend HTML, CSS e JavaScript. Não utiliza banco de dados: os dados ficam em arrays durante a execução.

## Requisitos

- Java 17 ou superior
- Navegador moderno

## Executar

```bash
cd backend
javac -d out $(find src -name '*.java')
java -cp out Main
```

Abra <http://localhost:8080> no navegador. O servidor Java entrega o frontend e disponibiliza a API REST em `/api`.

## Conceitos demonstrados

- POO, encapsulamento, composição e sobrescrita de `toString()`.
- Arrays `Cavalo[]`, `Participante[]`, `Leilao[]` e `Lance[]`.
- Strings em pesquisas sem distinção entre maiúsculas e minúsculas.
- Exceções personalizadas: `LanceInvalidoException`, `LeilaoEncerradoException` e `CavaloNaoEncontradoException`.
- Cadastro de cavalos e participantes, catálogo, início/encerramento de leilões, lances e histórico.

## Estrutura

- `backend/src/model`: entidades do domínio.
- `backend/src/service`: regras de cadastro, pesquisa e controle dos leilões.
- `backend/src/exception`: exceções específicas.
- `backend/src/Main.java`: servidor HTTP, endpoints e dados iniciais.
- `frontend`: interface visual do sistema.
