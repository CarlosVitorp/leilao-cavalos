package model;

public class Cavalo {
    private final int id;
    private String nome;
    private String raca;
    private int idade;
    private String sexo;
    private double valorInicial;
    private boolean vendido;

    public Cavalo(int id, String nome, String raca, int idade, String sexo, double valorInicial) {
        if (id <= 0 || nome == null || nome.isBlank() || raca == null || raca.isBlank()
                || idade < 0 || sexo == null || sexo.isBlank()) throw new IllegalArgumentException("Dados do cavalo inválidos.");
        if (!Double.isFinite(valorInicial) || valorInicial <= 0) throw new IllegalArgumentException("Valor inicial inválido.");
        this.id = id;
        this.nome = nome;
        this.raca = raca;
        this.idade = idade;
        this.sexo = sexo;
        this.valorInicial = valorInicial;
        this.vendido = false;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getRaca() { return raca; }
    public int getIdade() { return idade; }
    public String getSexo() { return sexo; }
    public double getValorInicial() { return valorInicial; }
    public boolean isVendido() { return vendido; }
    public void setVendido(boolean vendido) { this.vendido = vendido; }

    @Override
    public String toString() {
        return nome + " | Raça: " + raca + " | Idade: " + idade + " anos | Valor inicial: R$ " + valorInicial;
    }
}
