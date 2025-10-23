package br.com.overclean;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
    private Button[] todosBotoes; // Todos os botões juntos

    @Override
    public void start(Stage primaryStage) {
        final double BUTTON_HEIGHT = 30;
        final double BUTTON_WIDTH = 160;
        final ProcessReader processReader = new ProcessReader();
        final ProcessosProtegidos processosProtegidos = new ProcessosProtegidos();

        // ---------- Ações Únicas ----------
        Button botaoAtualizarSistema = new Button("Atualizar Sistema");
        botaoAtualizarSistema.setOnAction(e -> executarTarefa("scripts/atualizar_sistema.sh"));
        botaoAtualizarSistema.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoAtualizarSistema.setMaxWidth(Double.MAX_VALUE);

        Button botaoRepararSistema = new Button("Reparar Sistema");
        botaoRepararSistema.setOnAction(e -> executarTarefa("scripts/reparar_sistema.sh"));
        botaoRepararSistema.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoRepararSistema.setMaxWidth(Double.MAX_VALUE);

        Button botaoLimparCache = new Button("Limpar Cache");
        botaoLimparCache.setOnAction(e -> executarTarefa("scripts/limpar_cache.sh"));
        botaoLimparCache.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoLimparCache.setMaxWidth(Double.MAX_VALUE);

        Button botaoPararProcessosPesados = new Button("Parar Processos Pesados");
        botaoPararProcessosPesados.setOnAction(e -> executarTarefa("scripts/pare_processos_pesados.sh"));
        botaoPararProcessosPesados.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoPararProcessosPesados.setMaxWidth(Double.MAX_VALUE);

        Button botaoLiberarMemoria = new Button("Liberar Memória");

        TabPane telaLimparRam = new TabPane();
        botaoLiberarMemoria.setOnAction(e -> {
            Stage popup = new Stage();
            popup.setTitle("Liberar Memória");
            popup.setResizable(true);

            VBox layout = new VBox(10);
            layout.setStyle("-fx-padding: 20; -fx-background-color: #f4f4f4;");

            JsonNode root = processReader.getJsonProcesses();
            if (root == null) {
                layout.getChildren().add(new Label("Erro ao obter processos."));
                Scene scene = new Scene(layout, 300, 100);
                popup.setScene(scene);
                popup.initOwner(botaoLiberarMemoria.getScene().getWindow());
                popup.show();
                return;
            }

            JsonNode processes = root.get("processes");
            CheckBox[] processosCheckBoxes = new CheckBox[processes.size()];

            VBox checkBoxContainer = new VBox(5);
            if (processes != null && processes.isArray()) {
                for (int i = 0; i < processes.size(); i++) {
                    JsonNode proc = processes.get(i);
                    String nome = proc.get("name").asText();

                    if (processosProtegidos.existsByName(nome)) {
                        continue; // pula processos protegidos
                    }

                    processosCheckBoxes[i] = new CheckBox(nome);
                    checkBoxContainer.getChildren().add(processosCheckBoxes[i]);
                }
            } else {
                System.err.println("JSON inválido: não encontrou 'processes'");
            }

            // ScrollPane para tornar rolável
            ScrollPane scrollPane = new ScrollPane(checkBoxContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(200); // altura visível antes de rolar

            Button limparButton = new Button("Limpar");
            limparButton.setOnAction(ev -> {
                for (CheckBox cb : processosCheckBoxes) {
                    if (cb.isSelected()) {
                        executarTarefa("scripts/liberar_memoria.sh " + cb.getText());
                    }
                }
                popup.close();
            });

            layout.getChildren().addAll(scrollPane, limparButton);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout, 300, 300);
            popup.setScene(scene);
            popup.initOwner(botaoLiberarMemoria.getScene().getWindow());
            popup.show();
        });

        botaoLiberarMemoria.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoLiberarMemoria.setMaxWidth(Double.MAX_VALUE);

        Button botaoDesativarServicos = new Button("Desativar Serviços Não Usados");
        botaoDesativarServicos.setOnAction(e ->

        executarTarefa("scripts/desativar_servicos.sh"));
        botaoDesativarServicos.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoDesativarServicos.setMaxWidth(Double.MAX_VALUE);

        // ---------- Ações Completas ----------
        Button botaoDesfragmentarDisco = new Button("Desfragmentar Disco");
        botaoDesfragmentarDisco.setOnAction(e -> executarTarefa("scripts/desfragmentar_disco.sh"));
        botaoDesfragmentarDisco.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoDesfragmentarDisco.setMaxWidth(Double.MAX_VALUE);

        Button botaoVerificarErrosDisco = new Button("Verificar Erros de Disco");
        botaoVerificarErrosDisco.setOnAction(e -> executarTarefa("scripts/verificar_erros_disco.sh"));
        botaoVerificarErrosDisco.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoVerificarErrosDisco.setMaxWidth(Double.MAX_VALUE);

        Button botaoLimparArquivosTemporarios = new Button("Limpar Arquivos Temporários");
        botaoLimparArquivosTemporarios.setOnAction(e -> executarTarefa("scripts/limpar_arquivos_temporarios.sh"));
        botaoLimparArquivosTemporarios.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoLimparArquivosTemporarios.setMaxWidth(Double.MAX_VALUE);

        Button botaoReiniciarServicos = new Button("Reiniciar Serviços");
        botaoReiniciarServicos.setOnAction(e -> executarTarefa("scripts/reiniciar_servicos.sh"));
        botaoReiniciarServicos.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoReiniciarServicos.setMaxWidth(Double.MAX_VALUE);

        Button botaoAtualizarDrivers = new Button("Atualizar Drivers");
        botaoAtualizarDrivers.setOnAction(e -> executarTarefa("scripts/atualizar_drivers.sh"));
        botaoAtualizarDrivers.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoAtualizarDrivers.setMaxWidth(Double.MAX_VALUE);

        Button botaoOtimizarInicializacao = new Button("Otimizar Inicialização");
        botaoOtimizarInicializacao.setOnAction(e -> executarTarefa("scripts/otimizar_inicializacao.sh"));
        botaoOtimizarInicializacao.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        botaoOtimizarInicializacao.setMaxWidth(Double.MAX_VALUE);

        Button botoesAcaoUnica[] = {
                botaoAtualizarSistema,
                botaoRepararSistema,
                botaoLimparCache,
                botaoPararProcessosPesados,
                botaoLiberarMemoria,
                botaoDesativarServicos
        };

        Button botoesAcaoCompleta[] = {
                botaoDesfragmentarDisco,
                botaoVerificarErrosDisco,
                botaoLimparArquivosTemporarios,
                botaoReiniciarServicos,
                botaoAtualizarDrivers,
                botaoOtimizarInicializacao
        };

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

        root.getChildren().add(telaLimparRam); // adiciona o TabPane à interface
        VBox.setVgrow(telaLimparRam, Priority.ALWAYS); // ocupa espaço restante

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

        Scene scene = new Scene(root, 800, 600);
        // minimos
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        primaryStage.setMaximized(false);

        // centralizar a janela (alternativa limpa ao centerOnScreen)
        primaryStage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - 800) / 2);
        primaryStage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - 600) / 2);

        primaryStage.setResizable(true);

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
