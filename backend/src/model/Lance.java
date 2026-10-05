package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lance {
    private final Participante participante;
    private final double valor;
    private final LocalDateTime data;

    public Lance(Participante participante, double valor) {
        this.participante = participante;
        this.valor = valor;
        this.data = LocalDateTime.now();
    }

    public Participante getParticipante() { return participante; }
    public double getValor() { return valor; }
    public LocalDateTime getData() { return data; }
    public String getDataFormatada() { return data.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); }

    @Override
    public String toString() { return String.format("%s ofereceu R$ %.2f em %s", participante.getNome(), valor, getDataFormatada()); }
}
