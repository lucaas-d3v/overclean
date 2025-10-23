package br.com.overclean;

import java.sql.*;

public class ProcessosProtegidos {
    private static final String URL = "jdbc:sqlite:blacklist.db";

    public ProcessosProtegidos() {
        criarTabela();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void criarTabela() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS blacklist (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL UNIQUE
                    );
                """;
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public boolean addProcesso(String nome) {
        String sql = "INSERT INTO blacklist (nome) VALUES (?)";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar processo: " + e.getMessage());
            return false;
        }
    }

    public boolean removeProcesso(long id) {
        String sql = "DELETE FROM blacklist WHERE id = ?";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao remover processo: " + e.getMessage());
            return false;
        }
    }

    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM blacklist WHERE id = ?";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar ID: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByName(String nome) {
        String sql = "SELECT 1 FROM blacklist WHERE nome = ?";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar nome: " + e.getMessage());
            return false;
        }
    }

    public long getIdByName(String nome) {
        String sql = "SELECT id FROM blacklist WHERE nome = ?";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getLong("id");
        } catch (SQLException e) {
            System.err.println("Erro ao obter ID: " + e.getMessage());
        }
        return -1;
    }

    public void listarTodos() {
        String sql = "SELECT * FROM blacklist";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("nome"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar processos: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Cria a instância (a criação da tabela é executada no construtor)
        ProcessosProtegidos db = new ProcessosProtegidos();

        // Lista de processos (baseada no JSON que você enviou)
        String[] processos = {
                "systemd",
                "systemd-journal",
                "systemd-udevd",
                "systemd-timesyn",
                "avahi-daemon",
                "cron",
                "dbus-daemon",
                "snapd",
                "systemd-logind",
                "udisksd",
                "wpa_supplicant",
                "polkitd",
                "zerotier-one",
                "slim",
                "Xorg",
                "(sd-pam)",
                "pipewire",
                "wireplumber",
                "pipewire-pulse",
                "gnome-keyring-d",
                "xfce4-session",
                "at-spi-bus-laun",
                "at-spi2-registr",
                "gpg-agent",
                "xfwm4",
                "gvfsd",
                "gvfsd-fuse",
                "xfsettingsd",
                "upowerd",
                "xfce4-panel",
                "xfdesktop",
                "panel-5-whisker",
                "xfce4-power-man",
                "xiccd",
                "xfce4-notifyd",
                "polkit-gnome-au",
                "dhclient",
                "colord",
                "kdeconnectd",
                "dconf-service",
                "xbindkeys",
                "gvfs-udisks2-vo",
                "gvfs-afc-volume",
                "gvfs-mtp-volume",
                "gvfs-gphoto2-vo",
                "gvfs-goa-volume",
                "Thunar",
                "code",
                "xdg-desktop-por",
                "xdg-document-po",
                "xdg-permission-",
                "chrome_crashpad",
                "java",
                "midori",
                "GPU Process",
                "Socket Process",
                "Privileged Cont",
                "WebExtensions",
                "Utility Process",
                "Isolated Web Co",
                "RDD Process",
                "Web Content",
                "Discord",
                "gvfsd-metadata",
                "cool-retro-term",
                "bash",
                "listarProcessos",
                "xfconfd",
                "sudo"
        };

        criarTabela();

        // Insere em lote com uma única conexão (transação)
        try (Connection conn = db.connect()) {
            conn.setAutoCommit(false);
            String sql = "INSERT OR IGNORE INTO blacklist (nome) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int added = 0;
                for (String nome : processos) {
                    ps.setString(1, nome);
                    int rows = ps.executeUpdate();
                    if (rows > 0)
                        added++;
                }
                conn.commit();
                System.out.printf("Operação concluída — registros inseridos: %d (duplicatas ignoradas).%n", added);
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erro durante a inserção em lote, transação revertida: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao conectar/operar no banco: " + e.getMessage());
        }
    }

}
