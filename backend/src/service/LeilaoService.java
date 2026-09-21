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
        if (quantidadeCavalos >= cavalos.length) throw new IllegalStateException("Limite de cavalos atingido.");
        cavalos[quantidadeCavalos++] = cavalo;
    }
    public void cadastrarParticipante(Participante participante) {
        if (quantidadeParticipantes >= participantes.length) throw new IllegalStateException("Limite de participantes atingido.");
        participantes[quantidadeParticipantes++] = participante;
    }
    public Cavalo[] getCavalos() { return cavalos; }
    public int getQuantidadeCavalos() { return quantidadeCavalos; }
    public Participante[] getParticipantes() { return participantes; }
    public int getQuantidadeParticipantes() { return quantidadeParticipantes; }
    public Leilao[] getLeiloes() { return leiloes; }
    public int getQuantidadeLeiloes() { return quantidadeLeiloes; }

    public Cavalo buscarCavalo(String pesquisa) throws CavaloNaoEncontradoException {
        for (int i = 0; i < quantidadeCavalos; i++) {
            if (cavalos[i].getNome().toLowerCase().contains(pesquisa.toLowerCase())) return cavalos[i];
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
