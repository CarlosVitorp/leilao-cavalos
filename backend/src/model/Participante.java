package model;

public class Participante {
    private final int id;
    private String nome;
    private String cpf;

    public Participante(int id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }

    @Override
    public String toString() { return nome + " - CPF: " + cpf; }
}
