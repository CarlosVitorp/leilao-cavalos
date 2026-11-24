package frontend;

import com.fasterxml.jackson.databind.JsonNode;

record Participant(int id, String name, String cpf) {
    static Participant fromJson(JsonNode node) {
        return new Participant(node.get("id").asInt(), node.get("nome").asText(), node.get("cpf").asText());
    }

    @Override
    public String toString() {
        return id + " · " + name;
    }
}
