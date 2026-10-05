import exception.CavaloNaoEncontradoException;
import exception.LanceInvalidoException;
import exception.LeilaoEncerradoException;
import model.Cavalo;
import model.Lance;
import model.Leilao;
import model.Participante;
import service.LeilaoService;
import java.util.Scanner;

/** Aplicação de console para praticar Java básico, POO, arrays e exceções. */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LeilaoService service = new LeilaoService();

    public static void main(String[] args) {
        carregarDadosIniciais();
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");
            try { executar(opcao); }
            catch (CavaloNaoEncontradoException | LanceInvalidoException | LeilaoEncerradoException e) { System.out.println("Erro: " + e.getMessage()); }
            catch (IllegalArgumentException e) { System.out.println("Entrada inválida: " + e.getMessage()); }
        } while (opcao != 0);
        scanner.close();
        System.out.println("Programa encerrado.");
    }

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
