package model;

import exception.LanceInvalidoException;
import exception.LeilaoEncerradoException;

public class Leilao {
    private final Cavalo cavalo;
    private final Lance[] lances;
    private int quantidadeLances;
    private boolean encerrado;

    public Leilao(Cavalo cavalo) {
        if (cavalo == null || cavalo.isVendido()) throw new IllegalArgumentException("Informe um cavalo disponível.");
        this.cavalo = cavalo;
        this.lances = new Lance[100];
        this.quantidadeLances = 0;
        this.encerrado = false;
    }

    public Cavalo getCavalo() { return cavalo; }
    public boolean isEncerrado() { return encerrado; }
    public int getQuantidadeLances() { return quantidadeLances; }
    public Lance[] getLances() { return lances.clone(); }

    public void adicionarLance(Lance lance) throws LanceInvalidoException, LeilaoEncerradoException {
        if (encerrado) throw new LeilaoEncerradoException("O leilão já foi encerrado.");
        if (lance == null || lance.getParticipante() == null || !Double.isFinite(lance.getValor()) || lance.getValor() <= 0) {
            throw new LanceInvalidoException("Informe um participante e um valor positivo e finito.");
        }
        if (quantidadeLances >= lances.length) throw new LanceInvalidoException("Limite de lances atingido.");
        double centavos = lance.getValor() * 100;
        if (Math.abs(centavos - Math.rint(centavos)) > 0.0000001) throw new LanceInvalidoException("O lance pode ter no máximo duas casas decimais.");
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

    public void reabrirLeilao() {
        encerrado = false;
        cavalo.setVendido(false);
    }
}
