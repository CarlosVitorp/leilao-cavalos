package service;

import exception.CavaloNaoEncontradoException;
import model.Cavalo;
import model.Leilao;
import model.Participante;

public class LeilaoService {
    private final Cavalo[] cavalos = new Cavalo[50];
    private final Participante[] participantes = new Participante[100];
    private final Leilao[] leiloes = new Leilao[50];
    private int quantidadeCavalos;
    private int quantidadeParticipantes;
    private int quantidadeLeiloes;

    public void cadastrarCavalo(Cavalo cavalo) {
        if (cavalo == null) throw new IllegalArgumentException("Informe o cavalo.");
        for (int i = 0; i < quantidadeCavalos; i++) if (cavalos[i].getId() == cavalo.getId()) throw new IllegalArgumentException("ID de cavalo já cadastrado.");
        if (quantidadeCavalos >= cavalos.length) throw new IllegalStateException("Limite de cavalos atingido.");
        cavalos[quantidadeCavalos++] = cavalo;
    }
    public void cadastrarParticipante(Participante participante) {
        if (participante == null || participante.getNome() == null || participante.getNome().isBlank()
                || participante.getCpf() == null || !participante.getCpf().matches("(?:[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})")) {
            throw new IllegalArgumentException("Informe o nome e um CPF com 11 dígitos.");
        }
        String cpf = participante.getCpf().replaceAll("[^0-9]", "");
        for (int i = 0; i < quantidadeParticipantes; i++) {
            if (participantes[i].getId() == participante.getId()) throw new IllegalArgumentException("ID de participante já cadastrado.");
            if (participantes[i].getCpf().replaceAll("[^0-9]", "").equals(cpf)) {
                throw new IllegalArgumentException("CPF já cadastrado.");
            }
        }
        if (quantidadeParticipantes >= participantes.length) throw new IllegalStateException("Limite de participantes atingido.");
        participantes[quantidadeParticipantes++] = participante;
    }
    public Cavalo[] getCavalos() { return cavalos.clone(); }
    public int getQuantidadeCavalos() { return quantidadeCavalos; }
    public Participante[] getParticipantes() { return participantes.clone(); }
    public int getQuantidadeParticipantes() { return quantidadeParticipantes; }
    public Leilao[] getLeiloes() { return leiloes.clone(); }
    public int getQuantidadeLeiloes() { return quantidadeLeiloes; }

    public Cavalo buscarCavalo(String pesquisa) throws CavaloNaoEncontradoException {
        if (pesquisa == null || pesquisa.isBlank()) throw new IllegalArgumentException("Informe o nome para pesquisar.");
        for (int i = 0; i < quantidadeCavalos; i++) {
            if (cavalos[i].getNome().toLowerCase(java.util.Locale.ROOT).contains(pesquisa.toLowerCase(java.util.Locale.ROOT))) return cavalos[i];
        }
        throw new CavaloNaoEncontradoException("Cavalo " + pesquisa + " não encontrado.");
    }
    public Cavalo buscarCavaloPorId(int id) throws CavaloNaoEncontradoException {
        for (int i = 0; i < quantidadeCavalos; i++) if (cavalos[i].getId() == id) return cavalos[i];
        throw new CavaloNaoEncontradoException("Cavalo de id " + id + " não encontrado.");
    }
    public Participante buscarParticipantePorId(int id) {
        for (int i = 0; i < quantidadeParticipantes; i++) if (participantes[i].getId() == id) return participantes[i];
        return null;
    }
    public Participante buscarParticipantePorCpf(String cpf) {
        String normalizado = cpf.replaceAll("[^0-9]", "");
        for (int i = 0; i < quantidadeParticipantes; i++) if (participantes[i].getCpf().equals(normalizado)) return participantes[i];
        return null;
    }
    public Leilao iniciarLeilao(int cavaloId) throws CavaloNaoEncontradoException {
        for (int i = 0; i < quantidadeLeiloes; i++) if (leiloes[i].getCavalo().getId() == cavaloId) return leiloes[i];
        if (quantidadeLeiloes >= leiloes.length) throw new IllegalStateException("Limite de leilões atingido.");
        Leilao leilao = new Leilao(buscarCavaloPorId(cavaloId));
        leiloes[quantidadeLeiloes++] = leilao;
        return leilao;
    }
    public Leilao buscarLeilao(int cavaloId) throws CavaloNaoEncontradoException {
        for (int i = 0; i < quantidadeLeiloes; i++) if (leiloes[i].getCavalo().getId() == cavaloId) return leiloes[i];
        throw new CavaloNaoEncontradoException("Não existe leilão para este cavalo.");
    }
}
