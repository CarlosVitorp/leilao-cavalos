import exception.CavaloNaoEncontradoException;
import exception.LanceInvalidoException;
import exception.LeilaoEncerradoException;
import model.Cavalo;
import model.Lance;
import model.Leilao;
import model.Participante;
import service.LeilaoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Aplicação de console para praticar Java básico, POO, arrays e exceções. */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LeilaoService service = new LeilaoService();
    private static HttpServer servidor;

    public static void main(String[] args) {
        carregarDadosIniciais();
        iniciarServidor();
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");
            try { executar(opcao); }
            catch (CavaloNaoEncontradoException | LanceInvalidoException | LeilaoEncerradoException e) { System.out.println("Erro: " + e.getMessage()); }
            catch (IllegalArgumentException e) { System.out.println("Entrada inválida: " + e.getMessage()); }
        } while (opcao != 0);
        scanner.close();
        if (servidor != null) servidor.stop(0);
        System.out.println("Programa encerrado.");
    }

    private static void iniciarServidor() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
            servidor.createContext("/api", Main::atenderApi);
            servidor.start();
            System.out.println("Frontend conectado em http://127.0.0.1:8000");
        } catch (IOException e) {
            System.out.println("Aviso: frontend indisponível (porta 8080 ocupada).");
        }
    }

    private static void atenderApi(HttpExchange troca) throws IOException {
        try {
            String metodo = troca.getRequestMethod();
            String caminho = troca.getRequestURI().getPath();
            String[] partes = caminho.split("/");
            if (metodo.equals("GET") && caminho.equals("/api/cavalos")) responder(troca, 200, cavalosJson());
            else if (metodo.equals("POST") && caminho.equals("/api/resetar")) { service.resetar(); carregarDadosIniciais(); responder(troca, 200, "{\"mensagem\":\"Leilão resetado.\"}"); }
            else if (metodo.equals("GET") && caminho.equals("/api/participantes")) responder(troca, 200, participantesJson());
            else if (metodo.equals("POST") && caminho.equals("/api/participantes")) {
                String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int id = service.getQuantidadeParticipantes() + 1;
                Participante participante = new Participante(id, campo(corpo, "nome"), campo(corpo, "cpf"));
                service.cadastrarParticipante(participante);
                responder(troca, 201, participanteJson(participante));
            }
            else if (partes.length == 4 && partes[3].matches("\\d+")) {
                int id = Integer.parseInt(partes[3]);
                if (metodo.equals("POST")) responder(troca, 201, leilaoJson(service.iniciarLeilao(id)));
                else if (metodo.equals("GET")) responder(troca, 200, leilaoJson(service.buscarLeilao(id)));
                else responder(troca, 405, "{\"erro\":\"Método não permitido.\"}");
            } else if (partes.length == 5 && partes[3].matches("\\d+") && partes[4].equals("lances") && metodo.equals("POST")) {
                Leilao leilao;
                try { leilao = service.buscarLeilao(Integer.parseInt(partes[3])); }
                catch (CavaloNaoEncontradoException inexistente) { leilao = service.iniciarLeilao(Integer.parseInt(partes[3])); }
                String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int participanteId = Integer.parseInt(campo(corpo, "participanteId"));
                double valor = Double.parseDouble(campo(corpo, "valor").replace(',', '.'));
                Participante participante = service.buscarParticipantePorId(participanteId);
                if (participante == null) throw new LanceInvalidoException("Participante não encontrado.");
                leilao.adicionarLance(new Lance(participante, valor));
                responder(troca, 201, leilaoJson(leilao));
            } else if (partes.length == 5 && partes[3].matches("\\d+") && metodo.equals("POST") && (partes[4].equals("encerrar") || partes[4].equals("reabrir"))) {
                Leilao leilao = service.buscarLeilao(Integer.parseInt(partes[3]));
                if (partes[4].equals("encerrar")) leilao.encerrarLeilao(); else leilao.reabrirLeilao();
                responder(troca, 200, leilaoJson(leilao));
            } else responder(troca, 404, "{\"erro\":\"Rota não encontrada.\"}");
        } catch (CavaloNaoEncontradoException | LanceInvalidoException | IllegalArgumentException e) {
            responder(troca, 400, "{\"erro\":" + json(e.getMessage()) + "}");
        } catch (Exception e) {
            responder(troca, 500, "{\"erro\":\"Erro interno.\"}");
        }
    }

    private static String campo(String corpo, String nome) {
        for (String item : corpo.split("&")) {
            String[] partes = item.split("=", 2);
            if (partes.length == 2 && partes[0].equals(nome)) return URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Campo obrigatório: " + nome);
    }

    private static String cavalosJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < service.getQuantidadeCavalos(); i++) { if (i > 0) json.append(','); json.append(cavaloJson(service.getCavalos()[i])); }
        return json.append(']').toString();
    }
    private static String participantesJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < service.getQuantidadeParticipantes(); i++) { if (i > 0) json.append(','); json.append(participanteJson(service.getParticipantes()[i])); }
        return json.append(']').toString();
    }
    private static String participanteJson(Participante p) { return "{\"id\":" + p.getId() + ",\"nome\":" + json(p.getNome()) + ",\"cpf\":" + json(p.getCpf()) + "}"; }
    private static String cavaloJson(Cavalo c) {
        Leilao leilao = null; try { leilao = service.buscarLeilao(c.getId()); } catch (CavaloNaoEncontradoException ignored) { }
        double atual = leilao == null || leilao.getMaiorLance() == null ? c.getValorInicial() : leilao.getMaiorLance().getValor();
        return "{\"id\":" + c.getId() + ",\"nome\":" + json(c.getNome()) + ",\"raca\":" + json(c.getRaca()) + ",\"idade\":" + c.getIdade() + ",\"sexo\":" + json(c.getSexo()) + ",\"valorInicial\":" + c.getValorInicial() + ",\"lanceAtual\":" + atual + ",\"vendido\":" + c.isVendido() + ",\"leilaoAtivo\":" + (leilao != null && !leilao.isEncerrado()) + ",\"leilaoEncerrado\":" + (leilao != null && leilao.isEncerrado()) + "}";
    }
    private static String leilaoJson(Leilao l) {
        StringBuilder json = new StringBuilder("{\"cavalo\":").append(cavaloJson(l.getCavalo())).append(",\"encerrado\":").append(l.isEncerrado()).append(",\"lances\":[");
        for (int i = 0; i < l.getQuantidadeLances(); i++) { if (i > 0) json.append(','); Lance lance = l.getLances()[i]; json.append("{\"participante\":").append(json(lance.getParticipante().getNome())).append(",\"valor\":").append(lance.getValor()).append('}'); }
        return json.append("]}").toString();
    }
    private static String json(String valor) {
        StringBuilder resultado = new StringBuilder("\"");
        for (int i = 0; i < valor.length(); i++) {
            char caractere = valor.charAt(i);
            if (caractere == '\\' || caractere == '"') resultado.append('\\').append(caractere);
            else if (caractere == '\n') resultado.append("\\n");
            else if (caractere == '\r') resultado.append("\\r");
            else if (caractere == '\t') resultado.append("\\t");
            else if (caractere < 32) resultado.append(String.format("\\u%04x", (int) caractere));
            else resultado.append(caractere);
        }
        return resultado.append('"').toString();
    }
    private static void responder(HttpExchange troca, int status, String corpo) throws IOException { byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8); troca.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); troca.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); troca.sendResponseHeaders(status, bytes.length); troca.getResponseBody().write(bytes); troca.close(); }

    private static void exibirMenu() {
        System.out.println("\n=== LEILÃO DE CAVALOS ===");
        System.out.println("1 - Listar cavalos | 2 - Pesquisar cavalo | 3 - Iniciar leilão");
        System.out.println("4 - Dar lance | 5 - Ver histórico | 6 - Encerrar leilão");
        System.out.println("7 - Reabrir leilão | 8 - Cadastrar participante | 0 - Sair");
    }

    private static void executar(int opcao) throws CavaloNaoEncontradoException, LanceInvalidoException, LeilaoEncerradoException {
        switch (opcao) {
            case 1 -> listarCavalos(); case 2 -> pesquisarCavalo(); case 3 -> iniciarLeilao();
            case 4 -> darLance(); case 5 -> exibirHistorico(); case 6 -> encerrarLeilao();
            case 7 -> reabrirLeilao(); case 8 -> cadastrarParticipante(); case 0 -> { }
            default -> System.out.println("Opção inexistente.");
        }
    }

    private static void listarCavalos() {
        for (int i = 0; i < service.getQuantidadeCavalos(); i++) {
            Cavalo c = service.getCavalos()[i]; String status = "disponível";
            try { Leilao l = service.buscarLeilao(c.getId()); status = l.isEncerrado() ? "encerrado" : "em andamento"; }
            catch (CavaloNaoEncontradoException ignored) { }
            System.out.printf("%d - %s | %s | R$ %.2f | %s%n", c.getId(), c.getNome(), c.getRaca(), c.getValorInicial(), status);
        }
    }

    private static void pesquisarCavalo() throws CavaloNaoEncontradoException { System.out.println(service.buscarCavalo(lerTexto("Nome do cavalo: "))); }

    private static void iniciarLeilao() throws CavaloNaoEncontradoException { Leilao l = service.iniciarLeilao(lerInteiro("ID do cavalo: ")); System.out.println("Leilão disponível para " + l.getCavalo().getNome() + "."); }

    private static void darLance() throws CavaloNaoEncontradoException, LanceInvalidoException, LeilaoEncerradoException {
        Leilao l = service.buscarLeilao(lerInteiro("ID do cavalo: ")); Participante p = service.buscarParticipantePorId(lerInteiro("ID do participante: "));
        if (p == null) throw new LanceInvalidoException("Participante não encontrado.");
        l.adicionarLance(new Lance(p, lerDouble("Valor do lance: "))); System.out.println("Lance registrado.");
    }

    private static void exibirHistorico() throws CavaloNaoEncontradoException {
        Leilao l = service.buscarLeilao(lerInteiro("ID do cavalo: ")); System.out.println("Histórico de " + l.getCavalo().getNome() + ":");
        if (l.getQuantidadeLances() == 0) { System.out.println("Nenhum lance registrado."); return; }
        for (int i = 0; i < l.getQuantidadeLances(); i++) System.out.println(l.getLances()[i]);
    }

    private static void encerrarLeilao() throws CavaloNaoEncontradoException { service.buscarLeilao(lerInteiro("ID do cavalo: ")).encerrarLeilao(); System.out.println("Leilão encerrado."); }
    private static void reabrirLeilao() throws CavaloNaoEncontradoException { service.buscarLeilao(lerInteiro("ID do cavalo: ")).reabrirLeilao(); System.out.println("Leilão reaberto; histórico mantido."); }

    private static void cadastrarParticipante() {
        int id = service.getQuantidadeParticipantes() + 1;
        service.cadastrarParticipante(new Participante(id, lerTexto("Nome: "), lerTexto("CPF (11 dígitos): ")));
        System.out.println("Participante cadastrado com ID " + id + ".");
    }

    private static int lerInteiro(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) return 0;
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("digite um número inteiro."); }
    }
    private static double lerDouble(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) throw new IllegalArgumentException("entrada encerrada.");
        try { return Double.parseDouble(scanner.nextLine().trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("digite um número válido."); }
    }
    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) throw new IllegalArgumentException("entrada encerrada.");
        String texto = scanner.nextLine().trim();
        if (texto.isEmpty()) throw new IllegalArgumentException("o campo não pode ficar vazio.");
        return texto;
    }

    private static void carregarDadosIniciais() {
        service.cadastrarCavalo(new Cavalo(1, "Trovão Negro", "Quarto de Milha", 5, "Macho", 10000));
        service.cadastrarCavalo(new Cavalo(2, "Relâmpago", "Mangalarga", 7, "Macho", 15000));
        service.cadastrarCavalo(new Cavalo(3, "Estrela", "Crioulo", 4, "Fêmea", 12500));
        service.cadastrarParticipante(new Participante(1, "João Silva", "52998224725"));
        service.cadastrarParticipante(new Participante(2, "Maria Santos", "11144477735"));
    }
}
