package model;

public class Participante {
    private final int id;
    private String nome;
    private String cpf;

    public Participante(int id, String nome, String cpf) {
        if (id <= 0 || nome == null || nome.isBlank() || nome.strip().length() > 120) throw new IllegalArgumentException("Informe um nome de até 120 caracteres.");
        if (!cpfValido(cpf)) throw new IllegalArgumentException("CPF inválido.");
        this.id = id;
        this.nome = nome.strip();
        this.cpf = cpf.replaceAll("[^0-9]", "");
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("(?:[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})")) return false;
        String n = cpf.replaceAll("[^0-9]", "");
        if (n.chars().allMatch(c -> c == n.charAt(0))) return false;
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) soma += (n.charAt(i) - '0') * (tamanho + 1 - i);
            int digito = (soma * 10) % 11;
            if (digito == 10) digito = 0;
            if (digito != n.charAt(tamanho) - '0') return false;
        }
        return true;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }

    @Override
    public String toString() { return nome + " - CPF: " + cpf; }
}
