package frontend;

import com.fasterxml.jackson.databind.JsonNode;

record Bid(String participant, double value) {
    static Bid fromJson(JsonNode node) {
        return new Bid(node.get("participante").asText(), node.get("valor").asDouble());
    }
}
