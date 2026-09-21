package model;

import exception.LanceInvalidoException;
import exception.LeilaoEncerradoException;

public class Leilao {
    private final Cavalo cavalo;
    private final Lance[] lances;
    private int quantidadeLances;
    private boolean encerrado;

    public Leilao(Cavalo cavalo) {
        this.cavalo = cavalo;
        this.lances = new Lance[100];
        this.quantidadeLances = 0;
        this.encerrado = false;
    }

    public Cavalo getCavalo() { return cavalo; }
    public boolean isEncerrado() { return encerrado; }
    public int getQuantidadeLances() { return quantidadeLances; }
    public Lance[] getLances() { return lances; }

    public void adicionarLance(Lance lance) throws LanceInvalidoException, LeilaoEncerradoException {
        if (encerrado) throw new LeilaoEncerradoException("O leilão já foi encerrado.");
        if (quantidadeLances >= lances.length) throw new LanceInvalidoException("Limite de lances atingido.");
        double valorMinimo = quantidadeLances == 0 ? cavalo.getValorInicial() : lances[quantidadeLances - 1].getValor();
        if (lance.getValor() <= valorMinimo) {
            throw new LanceInvalidoException(String.format("O lance precisa ser maior que R$ %.2f.", valorMinimo));
        }
        lances[quantidadeLances++] = lance;
    }

    public Lance getMaiorLance() { return quantidadeLances == 0 ? null : lances[quantidadeLances - 1]; }

    public void encerrarLeilao() {
        encerrado = true;
        if (quantidadeLances > 0) cavalo.setVendido(true);
    }
}
