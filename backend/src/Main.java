import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exception.CavaloNaoEncontradoException;
import exception.LanceInvalidoException;
import exception.LeilaoEncerradoException;
import model.Cavalo;
import model.Lance;
import model.Leilao;
import model.Participante;
import service.LeilaoService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final LeilaoService service = new LeilaoService();
    private static final Path FRONTEND = Path.of("../frontend");

    public static void main(String[] args) throws Exception {
        carregarDadosIniciais();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/api", Main::atenderApi);
        server.createContext("/", Main::servirFrontend);
        server.start();
        System.out.println("Leilão de Cavalos disponível em http://localhost:8080");
    }

    private static void carregarDadosIniciais() {
        service.cadastrarCavalo(new Cavalo(1, "Trovão Negro", "Quarto de Milha", 5, "Macho", 10000));
        service.cadastrarCavalo(new Cavalo(2, "Relâmpago", "Mangalarga", 7, "Macho", 15000));
        service.cadastrarCavalo(new Cavalo(3, "Estrela", "Crioulo", 4, "Fêmea", 12500));
        service.cadastrarParticipante(new Participante(1, "João Silva", "111.111.111-11"));
        service.cadastrarParticipante(new Participante(2, "Maria Santos", "222.222.222-22"));
        service.cadastrarParticipante(new Participante(3, "Ana Oliveira", "333.333.333-33"));
    }

    private static void atenderApi(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (method.equals("GET") && path.equals("/api/cavalos")) responder(exchange, 200, cavalosJson());
            else if (method.equals("GET") && path.equals("/api/participantes")) responder(exchange, 200, participantesJson());
            else if (method.equals("POST") && path.equals("/api/participantes")) {
                int id = service.getQuantidadeParticipantes() + 1;
                service.cadastrarParticipante(new Participante(id, campo(body, "nome"), campo(body, "cpf")));
                responder(exchange, 201, "{\"mensagem\":\"Participante cadastrado com sucesso.\"}");
            } else if (method.equals("POST") && path.matches("/api/leiloes/\\d+")) {
                int id = numeroFinal(path);
                Leilao leilao = service.iniciarLeilao(id);
                responder(exchange, 201, leilaoJson(leilao));
            } else if (method.equals("GET") && path.matches("/api/leiloes/\\d+")) responder(exchange, 200, leilaoJson(service.buscarLeilao(numeroFinal(path))));
            else if (method.equals("POST") && path.matches("/api/leiloes/\\d+/lances")) {
                Leilao leilao = service.buscarLeilao(numeroDaRota(path));
                Participante participante = service.buscarParticipantePorId(Integer.parseInt(campo(body, "participanteId")));
                if (participante == null) throw new LanceInvalidoException("Participante não encontrado.");
                leilao.adicionarLance(new Lance(participante, Double.parseDouble(campo(body, "valor"))));
                responder(exchange, 201, leilaoJson(leilao));
            } else if (method.equals("POST") && path.matches("/api/leiloes/\\d+/encerrar")) {
                Leilao leilao = service.buscarLeilao(numeroDaRota(path));
                leilao.encerrarLeilao();
                responder(exchange, 200, leilaoJson(leilao));
            } else responder(exchange, 404, "{\"erro\":\"Rota não encontrada.\"}");
        } catch (LanceInvalidoException | LeilaoEncerradoException | CavaloNaoEncontradoException | NumberFormatException e) {
            responder(exchange, 400, "{\"erro\":" + json(e.getMessage()) + "}");
        } catch (Exception e) {
            responder(exchange, 500, "{\"erro\":" + json(e.getMessage()) + "}");
        }
    }

    private static void servirFrontend(HttpExchange exchange) throws IOException {
        String nome = exchange.getRequestURI().getPath().equals("/") ? "index.html" : exchange.getRequestURI().getPath().substring(1);
        Path arquivo = FRONTEND.resolve(nome).normalize();
        if (!arquivo.startsWith(FRONTEND) || !Files.exists(arquivo) || Files.isDirectory(arquivo)) { responder(exchange, 404, "Página não encontrada"); return; }
        String tipo = nome.endsWith(".css") ? "text/css" : nome.endsWith(".js") ? "application/javascript" : "text/html";
        byte[] conteudo = Files.readAllBytes(arquivo);
        exchange.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, conteudo.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(conteudo); }
    }

    private static String cavalosJson() {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < service.getQuantidadeCavalos(); i++) { if (i > 0) s.append(','); Cavalo c = service.getCavalos()[i]; s.append(cavaloJson(c)); }
        return s.append(']').toString();
    }
    private static String participantesJson() {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < service.getQuantidadeParticipantes(); i++) { if (i > 0) s.append(','); Participante p = service.getParticipantes()[i]; s.append("{\"id\":" + p.getId() + ",\"nome\":" + json(p.getNome()) + ",\"cpf\":" + json(p.getCpf()) + "}"); }
        return s.append(']').toString();
    }
    private static String cavaloJson(Cavalo c) {
        Leilao l = null; try { l = service.buscarLeilao(c.getId()); } catch (Exception ignored) {}
        String lance = l == null || l.getMaiorLance() == null ? String.valueOf(c.getValorInicial()) : String.valueOf(l.getMaiorLance().getValor());
        return "{\"id\":" + c.getId() + ",\"nome\":" + json(c.getNome()) + ",\"raca\":" + json(c.getRaca()) + ",\"idade\":" + c.getIdade() + ",\"sexo\":" + json(c.getSexo()) + ",\"valorInicial\":" + c.getValorInicial() + ",\"vendido\":" + c.isVendido() + ",\"leilaoAtivo\":" + (l != null && !l.isEncerrado()) + ",\"lanceAtual\":" + lance + "}";
    }
    private static String leilaoJson(Leilao l) {
        StringBuilder s = new StringBuilder("{\"cavalo\":").append(cavaloJson(l.getCavalo())).append(",\"encerrado\":").append(l.isEncerrado()).append(",\"lances\":[");
        for (int i = 0; i < l.getQuantidadeLances(); i++) { if (i > 0) s.append(','); Lance x = l.getLances()[i]; s.append("{\"participante\":").append(json(x.getParticipante().getNome())).append(",\"valor\":").append(x.getValor()).append(",\"data\":").append(json(x.getDataFormatada())).append('}'); }
        return s.append("]}").toString();
    }
    private static String campo(String body, String nome) { Matcher m = Pattern.compile("\\\"" + nome + "\\\"\\s*:\\s*(?:\\\"([^\\\"]*)\\\"|([^,}]+))").matcher(body); if (!m.find()) throw new IllegalArgumentException("Campo obrigatório: " + nome); return m.group(1) != null ? m.group(1) : m.group(2).trim(); }
    private static int numeroFinal(String path) { return Integer.parseInt(path.substring(path.lastIndexOf('/') + 1)); }
    private static int numeroDaRota(String path) { return Integer.parseInt(path.split("/")[3]); }
    private static String json(String valor) { return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\""; }
    private static void responder(HttpExchange e, int status, String texto) throws IOException { byte[] bytes = texto.getBytes(StandardCharsets.UTF_8); e.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); e.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); e.sendResponseHeaders(status, bytes.length); try (OutputStream out = e.getResponseBody()) { out.write(bytes); } }
}
