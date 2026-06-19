import java.util.Optional;

final class DatabaseSettings {
    private static final String DEFAULT_URL = "jdbc:h2:file:./data/uno;AUTO_SERVER=TRUE";

    private DatabaseSettings() {
    }

    static String jdbcUrl() {
        return Optional.ofNullable(System.getenv("UNO_DB_URL")).filter(s -> !s.isBlank()).orElse(DEFAULT_URL);
    }

    static String jdbcUser() {
        return Optional.ofNullable(System.getenv("UNO_DB_USER")).filter(s -> !s.isBlank()).orElse("sa");
    }

    static String jdbcPassword() {
        return Optional.ofNullable(System.getenv("UNO_DB_PASSWORD")).orElse("");
    }
}
