# Apostila de revisão — Leilão de Cavalos

Esta apostila reúne os conceitos de Java usados no projeto e os principais assuntos das aulas: fundamentos, orientação a objetos, arrays, `String` e exceções.

## 1. Como executar o projeto

Na pasta `leilao-cavalos`, abra o terminal:

```powershell
$fontes = Get-ChildItem backend/src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 --release 17 -d backend/out $fontes
java -cp backend/out Main
```

O frontend é uma vitrine estática. Para abri-lo:

```powershell
cd frontend
python -m http.server 8000 --bind 127.0.0.1
```

Depois acesse `http://127.0.0.1:8000/`.

## 2. Fundamentos de Java

Um programa Java é compilado pelo `javac` para bytecode (`.class`) e executado pela JVM.

```java
public class Exemplo {
    public static void main(String[] args) {
        int idade = 20;
        if (idade >= 18) {
            System.out.println("Maior de idade");
        }
    }
}
```

Tipos comuns: `int`, `double`, `boolean` e `String`. Use `Scanner` para ler dados:

```java
Scanner scanner = new Scanner(System.in);
int numero = Integer.parseInt(scanner.nextLine());
```

O projeto usa `if`, `switch`, `for`, `do-while`, métodos e conversão de texto para números.

## 3. Classes e objetos

Uma classe é o modelo; um objeto é uma instância criada com `new`.

```java
public class Cavalo {
    private final int id;
    private String nome;

    public Cavalo(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
}

Cavalo cavalo = new Cavalo(1, "Trovão Negro");
```

No projeto, `Cavalo`, `Participante`, `Lance` e `Leilao` são classes do domínio.

## 4. Encapsulamento

Os atributos ficam `private` e o acesso ocorre por métodos públicos. Assim, a própria classe protege suas regras.

```java
private double valor;

public void alterarValor(double valor) {
    if (valor <= 0) {
        throw new IllegalArgumentException("Valor inválido");
    }
    this.valor = valor;
}
```

No projeto, `Leilao.adicionarLance` impede lances abaixo do valor inicial, lances repetidos, valores inválidos e lances depois do encerramento.

## 5. Composição

Um leilão possui um cavalo e vários lances. Isso é composição: uma classe usa objetos de outras classes.

```java
private final Cavalo cavalo;
private final Lance[] lances = new Lance[100];
```

## 6. Arrays

Arrays têm tamanho fixo e índices iniciados em zero:

```java
String[] nomes = new String[3];
nomes[0] = "Ana";

for (int i = 0; i < nomes.length; i++) {
    System.out.println(nomes[i]);
}
```

O serviço usa arrays para armazenar cavalos, participantes e leilões. As variáveis de quantidade indicam quais posições estão ocupadas.

## 7. Strings

Para comparar conteúdo, use `equals`; para pesquisar ignorando maiúsculas e minúsculas, use `toLowerCase`:

```java
if (nome.toLowerCase().contains(pesquisa.toLowerCase())) {
    System.out.println("Encontrado");
}
```

Não use `==` para comparar o conteúdo de duas `String`.

Métodos úteis: `isBlank`, `isEmpty`, `strip`, `contains`, `startsWith`, `replace` e `split`.

## 8. Exceções

Exceções representam situações que impedem a operação normal. O projeto possui exceções próprias:

- `CavaloNaoEncontradoException`;
- `LanceInvalidoException`;
- `LeilaoEncerradoException`.

Tratamento:

```java
try {
    leilao.adicionarLance(lance);
} catch (LanceInvalidoException e) {
    System.out.println(e.getMessage());
}
```

Use mensagens claras e trate a exceção no ponto em que é possível orientar o usuário.

## 9. Fluxo do sistema

1. O `Main` cria os dados iniciais.
2. O menu lê a opção com `Scanner`.
3. O `LeilaoService` procura cavalos, participantes e leilões nos arrays.
4. O objeto `Leilao` aplica as regras para registrar o lance.
5. As exceções informam entradas inválidas sem encerrar o programa.
6. O leilão pode ser encerrado e reaberto; o histórico de lances é mantido.

## 10. Perguntas para revisão

1. Qual é a diferença entre uma classe e um objeto?
2. Por que os atributos do projeto são `private`?
3. Por que o primeiro índice de um array é zero?
4. Qual a diferença entre `==` e `equals` para `String`?
5. O que acontece quando um lance é menor que o lance atual?
6. Qual a finalidade de `try` e `catch`?
7. Por que o serviço precisa de uma variável de quantidade para cada array?

## 11. Exercícios práticos

1. Adicione um campo `cor` à classe `Cavalo` e mostre a informação na listagem.
2. Crie uma opção para listar todos os participantes.
3. Crie um método que conte quantos cavalos estão vendidos.
4. Crie uma exceção `ParticipanteNaoEncontradoException` e use-a no cadastro de lances.
5. Faça uma busca por raça além da busca por nome.
6. Altere o histórico para mostrar também o horário formatado de cada lance.

## 12. Checklist antes da entrega

- [ ] O projeto compila com Java 17.
- [ ] O menu abre e aceita opções.
- [ ] Entradas inválidas mostram mensagem, sem derrubar o programa.
- [ ] O lance precisa ser maior que o valor atual.
- [ ] O lance não pode ser feito depois do encerramento.
- [ ] A reabertura mantém o histórico.
- [ ] Os atributos continuam encapsulados com `private`.
- [ ] O README explica como executar.
