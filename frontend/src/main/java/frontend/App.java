package frontend;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App extends Application {
    private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(BRAZIL);

    private final ApiClient api = new ApiClient();
    private final TextField searchField = new TextField();
    private final TextField participantNameField = new TextField();
    private final TextField participantCpfField = new TextField();
    private final FlowPane horseGrid = new FlowPane(18, 18);
    private final Label feedback = new Label();
    private final Label connectionStatus = new Label("Conectando ao backend...");
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Process backendProcess;
    private List<Horse> horses = List.of();
    private List<Participant> participants = List.of();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildCatalog());

        Scene scene = new Scene(root, 1220, 780);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setTitle("Haras Prime · Leilão de Cavalos");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setOnHidden(event -> stopBackend());
        stage.show();

        connectOrStartBackend();
    }

    private Node buildHeader() {
        Label logo = new Label("HARAS ");
        logo.getStyleClass().add("logo");
        Label logoAccent = new Label("PRIME");
        logoAccent.getStyleClass().add("logo-accent");
        HBox brand = new HBox(logo, logoAccent);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Catálogo de cavalos");
        subtitle.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        connectionStatus.getStyleClass().add("connection");
        HBox header = new HBox(18, brand, spacer, subtitle, connectionStatus);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 32, 18, 32));
        header.getStyleClass().add("header");
        return header;
    }

    private Node buildSidebar() {
        Label formTitle = new Label("Cadastrar participante");
        formTitle.getStyleClass().add("section-title");
        participantNameField.setPromptText("Nome completo");
        participantCpfField.setPromptText("CPF válido");
        Button saveButton = primaryButton("Salvar participante");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(event -> registerParticipant());

        Label searchTitle = new Label("Pesquisar catálogo");
        searchTitle.getStyleClass().add("section-title");
        searchField.setPromptText("Nome do cavalo");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> renderHorses());

        Button refreshButton = secondaryButton("Atualizar catálogo");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(event -> loadData());
        Button resetButton = secondaryButton("Resetar dados");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(event -> resetAndLoad());

        VBox sidebar = new VBox(12,
                formTitle, participantNameField, participantCpfField, saveButton,
                new Separator(), searchTitle, searchField,
                refreshButton, resetButton,
                new Separator(), feedback);
        sidebar.setPadding(new Insets(28, 22, 28, 32));
        sidebar.setPrefWidth(270);
        sidebar.getStyleClass().add("sidebar");
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback");
        return sidebar;
    }

    private Node buildCatalog() {
        Label eyebrow = new Label("LEILÃO DE CAVALOS");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("Encontre seu próximo campeão.");
        title.getStyleClass().add("hero-title");
        Label description = new Label("Cadastre participantes e acompanhe os lances em tempo real.");
        description.getStyleClass().add("hero-description");
        VBox hero = new VBox(8, eyebrow, title, description);
        hero.setPadding(new Insets(28, 32, 28, 32));
        hero.getStyleClass().add("hero");

        Label catalogTitle = new Label("Cavalos disponíveis");
        catalogTitle.getStyleClass().add("catalog-title");
        horseGrid.setPadding(new Insets(0, 32, 32, 32));
        horseGrid.setPrefWrapLength(850);
        horseGrid.getStyleClass().add("horse-grid");

        VBox content = new VBox(hero, catalogTitle, horseGrid);
        VBox.setVgrow(horseGrid, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("catalog-scroll");
        return scroll;
    }

    private void connectOrStartBackend() {
        setBusy("Verificando backend...");
        api.get("/cavalos").whenComplete((ignored, error) -> {
            if (error == null) {
                Platform.runLater(this::resetAndLoad);
                return;
            }
            if (startBackendProcess()) {
                retryBackendConnection(0);
            } else {
                Platform.runLater(() -> showOffline("Backend offline. Compile backend/src antes de iniciar o frontend."));
            }
        });
    }

    private void retryBackendConnection(int attempt) {
        if (attempt >= 15) {
            Platform.runLater(() -> showOffline("Não foi possível iniciar o backend na porta 8080."));
            return;
        }
        scheduler.schedule(() -> api.get("/cavalos").whenComplete((ignored, error) -> {
            if (error == null) {
                Platform.runLater(this::resetAndLoad);
            } else {
                retryBackendConnection(attempt + 1);
            }
        }), 300, TimeUnit.MILLISECONDS);
    }

    private boolean startBackendProcess() {
        Path backendOut = Paths.get("..", "backend", "out").toAbsolutePath().normalize();
        if (!Files.exists(backendOut.resolve("Main.class"))) {
            backendOut = Paths.get("backend", "out").toAbsolutePath().normalize();
        }
        if (!Files.exists(backendOut.resolve("Main.class"))) return false;

        String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java.exe").toString();
        try {
            backendProcess = new ProcessBuilder(javaExecutable, "-cp", backendOut.toString(), "Main")
                    .directory(backendOut.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void stopBackend() {
        scheduler.shutdownNow();
        if (backendProcess != null && backendProcess.isAlive()) {
            try {
                backendProcess.getOutputStream().close();
            } catch (IOException ignored) { }
            backendProcess.destroy();
        }
    }

    @Override
    public void stop() {
        stopBackend();
    }

    private void resetAndLoad() {
        setBusy("Resetando dados...");
        api.post("/resetar").handle((ignored, error) -> {
            Platform.runLater(this::loadData);
            return null;
        });
    }

    private void loadData() {
        setBusy("Carregando catálogo...");
        api.get("/cavalos").thenCombine(api.get("/participantes"), (horseJson, participantJson) -> {
            List<Horse> newHorses = toList(horseJson, Horse::fromJson);
            List<Participant> newParticipants = toList(participantJson, Participant::fromJson);
            return new CatalogData(newHorses, newParticipants);
        }).whenComplete((data, error) -> Platform.runLater(() -> {
            if (error != null) {
                connectionStatus.setText("Backend offline");
                connectionStatus.getStyleClass().removeAll("online");
                connectionStatus.getStyleClass().add("offline");
                showFeedback("Não foi possível conectar ao backend em 127.0.0.1:8080.", true);
                return;
            }
            horses = data.horses();
            participants = data.participants();
            connectionStatus.setText("Backend conectado");
            connectionStatus.getStyleClass().removeAll("offline");
            connectionStatus.getStyleClass().add("online");
            renderHorses();
            showFeedback("Catálogo atualizado.", false);
        }));
    }

    private void renderHorses() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(BRAZIL);
        horseGrid.getChildren().setAll(horses.stream()
                .filter(horse -> horse.name().toLowerCase(BRAZIL).contains(query))
                .sorted(Comparator.comparing(Horse::name))
                .map(this::createHorseCard)
                .collect(Collectors.toList()));

        if (horseGrid.getChildren().isEmpty()) {
            Label empty = new Label("Nenhum cavalo encontrado.");
            empty.getStyleClass().add("muted");
            horseGrid.getChildren().add(empty);
        }
    }

    private VBox createHorseCard(Horse horse) {
        Label name = new Label(horse.name());
        name.getStyleClass().add("horse-name");
        Label details = new Label(horse.details());
        details.getStyleClass().add("muted");
        Label price = new Label("Lance atual: " + money(horse.currentBid()));
        price.getStyleClass().add("price");
        Label status = new Label(statusText(horse));
        status.getStyleClass().addAll("status", statusClass(horse));

        VBox card = new VBox(10, name, details, price, status);
        card.setPrefWidth(260);
        card.setMinHeight(235);
        card.getStyleClass().add("horse-card");

        if (horse.auctionClosed()) {
            Button reopen = secondaryButton("Reabrir leilão");
            reopen.setOnAction(event -> executeAction(horse, "reabrir", "Leilão reaberto."));
            card.getChildren().add(reopen);
        } else {
            ComboBox<Participant> participantBox = new ComboBox<>();
            participantBox.getItems().setAll(participants);
            participantBox.setPromptText("Participante");
            participantBox.setMaxWidth(Double.MAX_VALUE);
            TextField bidField = new TextField();
            bidField.setPromptText("Valor do lance");
            bidField.setMaxWidth(Double.MAX_VALUE);
            Button bidButton = primaryButton("Dar lance");
            bidButton.setMaxWidth(Double.MAX_VALUE);
            bidButton.setOnAction(event -> placeBid(horse, participantBox.getValue(), bidField.getText()));
            HBox actions = new HBox(8);
            Button history = secondaryButton("Histórico");
            history.setOnAction(event -> showHistory(horse));
            Button close = secondaryButton("Encerrar");
            close.setOnAction(event -> executeAction(horse, "encerrar", "Leilão encerrado."));
            actions.getChildren().addAll(history, close);
            card.getChildren().addAll(participantBox, bidField, bidButton, actions);
        }
        return card;
    }

    private void registerParticipant() {
        String name = participantNameField.getText().trim();
        String cpf = participantCpfField.getText().trim();
        if (name.isBlank() || cpf.isBlank()) {
            showFeedback("Informe nome e CPF.", true);
            return;
        }
        setBusy("Salvando participante...");
        api.postForm("/participantes", "nome", name, "cpf", cpf).whenComplete((json, error) ->
                Platform.runLater(() -> {
                    if (error != null) {
                        showFeedback(errorMessage(error), true);
                        return;
                    }
                    participantNameField.clear();
                    participantCpfField.clear();
                    showFeedback("Participante cadastrado com ID " + json.get("id").asInt() + ".", false);
                    loadData();
                }));
    }

    private void placeBid(Horse horse, Participant participant, String rawValue) {
        if (participant == null) {
            showFeedback("Selecione um participante.", true);
            return;
        }
        try {
            double value = Double.parseDouble(rawValue.trim().replace(',', '.'));
            if (!Double.isFinite(value)) throw new NumberFormatException();
            setBusy("Registrando lance...");
            api.postForm("/leiloes/" + horse.id() + "/lances",
                    "participanteId", String.valueOf(participant.id()),
                    "valor", String.valueOf(value)).whenComplete((ignored, error) ->
                    Platform.runLater(() -> {
                        if (error != null) showFeedback(errorMessage(error), true);
                        else {
                            showFeedback("Lance registrado.", false);
                            loadData();
                        }
                    }));
        } catch (NumberFormatException e) {
            showFeedback("Informe um valor de lance válido.", true);
        }
    }

    private void executeAction(Horse horse, String action, String successMessage) {
        setBusy("Atualizando leilão...");
        api.post("/leiloes/" + horse.id() + "/" + action).whenComplete((ignored, error) ->
                Platform.runLater(() -> {
                    if (error != null) showFeedback(errorMessage(error), true);
                    else {
                        showFeedback(successMessage, false);
                        loadData();
                    }
                }));
    }

    private void showHistory(Horse horse) {
        setBusy("Consultando histórico...");
        api.get("/leiloes/" + horse.id()).whenComplete((json, error) -> Platform.runLater(() -> {
            if (error != null) {
                showFeedback(errorMessage(error), true);
                return;
            }
            ListView<String> history = new ListView<>();
            List<String> entries = new ArrayList<>();
            for (JsonNode bidNode : json.withArray("lances")) {
                Bid bid = Bid.fromJson(bidNode);
                entries.add(bid.participant() + " · " + money(bid.value()));
            }
            history.getItems().setAll(entries.isEmpty() ? List.of("Nenhum lance registrado.") : entries);
            history.setPrefHeight(Math.min(260, Math.max(80, entries.size() * 36 + 20)));
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Histórico de lances");
            dialog.setHeaderText(horse.name());
            dialog.getDialogPane().setContent(history);
            dialog.showAndWait();
            showFeedback("Histórico consultado.", false);
        }));
    }

    private void setBusy(String message) {
        connectionStatus.setText(message);
    }

    private void showFeedback(String message, boolean error) {
        feedback.setText(message);
        feedback.getStyleClass().removeAll("success", "error");
        feedback.getStyleClass().add(error ? "error" : "success");
    }

    private void showOffline(String message) {
        connectionStatus.setText("Backend offline");
        connectionStatus.getStyleClass().removeAll("online");
        connectionStatus.getStyleClass().add("offline");
        showFeedback(message, true);
    }

    private static String statusText(Horse horse) {
        if (horse.auctionClosed()) return "Encerrado";
        if (horse.auctionActive()) return "Leilão em andamento";
        return "Disponível";
    }

    private static String statusClass(Horse horse) {
        if (horse.auctionClosed()) return "status-closed";
        if (horse.auctionActive()) return "status-active";
        return "status-available";
    }

    private static String money(double value) {
        return CURRENCY.format(value);
    }

    private static String errorMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? "Erro na operação." : cause.getMessage();
    }

    private static <T> List<T> toList(JsonNode array, java.util.function.Function<JsonNode, T> mapper) {
        List<T> result = new ArrayList<>();
        array.forEach(node -> result.add(mapper.apply(node)));
        return result;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    private record CatalogData(List<Horse> horses, List<Participant> participants) { }

    public static void main(String[] args) {
        launch(args);
    }
}
