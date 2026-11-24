package frontend;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Locale;

record Horse(int id, String name, String breed, int age, String sex,
             double initialValue, double currentBid, boolean auctionActive,
             boolean auctionClosed, boolean sold) {
    static Horse fromJson(JsonNode node) {
        return new Horse(
                node.get("id").asInt(),
                node.get("nome").asText(),
                node.get("raca").asText(),
                node.get("idade").asInt(),
                node.get("sexo").asText(),
                node.get("valorInicial").asDouble(),
                node.get("lanceAtual").asDouble(),
                node.get("leilaoAtivo").asBoolean(),
                node.get("leilaoEncerrado").asBoolean(),
                node.get("vendido").asBoolean()
        );
    }

    String details() {
        return String.format(Locale.ROOT, "%s · %d anos · %s", breed, age, sex);
    }
}
