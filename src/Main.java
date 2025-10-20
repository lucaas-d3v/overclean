import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private TextArea logArea;
    private ProgressBar progressBar;
    private Label progressLabel;
    private boolean isRunningAction = false;
    private Button[] botoesAcaoUnica; // Parte 2
    private Button[] botoesAcaoCompleta; // Parte 1
    private Button[] todosBotoes; // Todos os botões juntos

    @Override
    public void start(Stage primaryStage) {
        // ---------- Ações Únicas ----------
        final String[] nomesAcaoUnica = {
                "Atualizar Sistema", "Reparar Sistema", "Limpar Cache",
                "Parar Processos Pesados", "Liberar Memória", "Desativar Serviços Não Usados"
        };

        botoesAcaoUnica = new Button[nomesAcaoUnica.length];
        final double BUTTON_WIDTH = 160;
        final double BUTTON_HEIGHT = 30;

        for (int i = 0; i < nomesAcaoUnica.length; i++) {
            final String nome = nomesAcaoUnica[i];
            botoesAcaoUnica[i] = new Button(nome);
            botoesAcaoUnica[i].setOnAction(e -> executarTarefa(nome));
            botoesAcaoUnica[i].setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            botoesAcaoUnica[i].setMaxWidth(Double.MAX_VALUE);
        }

        // ---------- Ações Completas ----------
        final String[] nomesAcaoCompleta = {
                "Desfragmentar Disco", "Verificar Erros de Disco",
                "Limpar Arquivos Temporários", "Reiniciar Serviços",
                "Atualizar Drivers", "Otimizar Inicialização"
        };

        botoesAcaoCompleta = new Button[nomesAcaoCompleta.length];
        for (int i = 0; i < nomesAcaoCompleta.length; i++) {
            final String nome = nomesAcaoCompleta[i];
            botoesAcaoCompleta[i] = new Button(nome);
            botoesAcaoCompleta[i].setOnAction(e -> executarTarefa(nome));
            botoesAcaoCompleta[i].setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            botoesAcaoCompleta[i].setMaxWidth(Double.MAX_VALUE);
        }

        // ---------- Unir todos os botões ----------
        todosBotoes = new Button[botoesAcaoUnica.length + botoesAcaoCompleta.length];
        System.arraycopy(botoesAcaoUnica, 0, todosBotoes, 0, botoesAcaoUnica.length);
        System.arraycopy(botoesAcaoCompleta, 0, todosBotoes, botoesAcaoUnica.length, botoesAcaoCompleta.length);

        // ---------- Labels ----------
        Label titulo = new Label("Overclean - Gerenciador de Performance");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        titulo.setAlignment(Pos.CENTER);

        Label labelCompletas = new Label("Ações Completas");
        labelCompletas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label labelUnicas = new Label("Ações Únicas");
        labelUnicas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // ---------- VBox Parte 1 ----------
        VBox parte1 = new VBox(10);
        parte1.setStyle("-fx-padding: 20;");
        parte1.getChildren().add(labelCompletas);
        parte1.getChildren().addAll(botoesAcaoCompleta);
        parte1.setAlignment(Pos.TOP_CENTER);

        // ---------- VBox Parte 2 ----------
        VBox parte2 = new VBox(10);
        parte2.setStyle("-fx-padding: 20;");
        parte2.getChildren().add(labelUnicas);
        parte2.getChildren().addAll(botoesAcaoUnica);
        parte2.setAlignment(Pos.TOP_CENTER);

        // ---------- Separador ----------
        Separator separador = new Separator(Orientation.VERTICAL);
        separador.setPrefWidth(5);
        separador.setMaxHeight(Double.MAX_VALUE);

        // ---------- HBox Top ----------
        HBox top = new HBox(20, parte1, separador, parte2);
        top.setAlignment(Pos.CENTER);
        HBox.setHgrow(parte1, Priority.ALWAYS);
        HBox.setHgrow(parte2, Priority.ALWAYS);

        // ---------- Log e ProgressBar ----------
        VBox logBox = criarLogBox();
        VBox progressBox = criarBarraProgresso();

        // ---------- Root ----------
        VBox root = new VBox(20, titulo, top, logBox, progressBox);
        root.setStyle("-fx-padding: 20;");
        root.setPrefSize(800, 600);

        VBox.setVgrow(top, Priority.ALWAYS);
        VBox.setVgrow(logBox, Priority.SOMETIMES);
        VBox.setVgrow(progressBox, Priority.NEVER);
        root.setFillWidth(true);
        root.setAlignment(Pos.CENTER);

        try {
            primaryStage.getIcons().addAll(
                    new Image("file:icons/logo_32.png"),
                    new Image("file:icons/logo_256.png"));
        } catch (Exception e) {
            System.out.println("Ícones não encontrados");
        }

        Scene scene = new Scene(root);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Overclean");
        primaryStage.show();
    }

    private VBox criarLogBox() {
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(5);
        logArea.setStyle("-fx-control-inner-background: #f4f4f4;");

        VBox logBox = new VBox(5);
        logBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
        logBox.getChildren().addAll(new Label("Log de Atividades"), logArea);
        logBox.setPrefHeight(100);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        return logBox;
    }

    private VBox criarBarraProgresso() {
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        progressLabel = new Label("Aguardando ação...");
        progressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        VBox progressBox = new VBox(5);
        progressBox.getChildren().addAll(progressLabel, progressBar);
        progressBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
        progressBox.setAlignment(Pos.CENTER_LEFT);

        return progressBox;
    }

    private void adicionarLog(String mensagem) {
        Platform.runLater(() -> {
            logArea.appendText(mensagem);
            logArea.positionCaret(logArea.getLength()); // autoscroll
        });
    }

    private void executarTarefa(String nomeTarefa) {
        if (isRunningAction) {
            adicionarLog("Já existe uma ação em execução. Aguarde a conclusão...\n");
            return;
        }

        isRunningAction = true;

        // Desabilitar todos os botões
        for (Button b : todosBotoes)
            b.setDisable(true);

        adicionarLog("Iniciando: " + nomeTarefa + "...\n");
        progressLabel.setText("Executando: " + nomeTarefa);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(30); // Simula execução
                    updateProgress(i, 100);
                    final int progresso = i;
                    Platform.runLater(() -> progressLabel.setText(nomeTarefa + " - " + progresso + "%"));
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> finalizarTarefa(nomeTarefa, true));
        task.setOnFailed(e -> finalizarTarefa(nomeTarefa, false));

        new Thread(task).start();
    }

    private void finalizarTarefa(String nomeTarefa, boolean sucesso) {
        isRunningAction = false;

        Platform.runLater(() -> {
            for (Button b : todosBotoes)
                b.setDisable(false);

            if (sucesso) {
                adicionarLog("✓ Concluído: " + nomeTarefa + "\n");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(1.0);
            } else {
                adicionarLog("✗ Erro: " + nomeTarefa + "\n");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            }

            // Resetar barra após 1 segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        progressBar.setProgress(0);
                        progressLabel.setText("Aguardando ação...");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }

    public void rodarScript(String caminho) {
        try {
            ProcessBuilder p = new ProcessBuilder("/usr/bin/bash", caminho);
            p.inheritIO();
            p.start();
        } catch (Exception e) {
            System.out.println("Erro ao executar script, erro: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
