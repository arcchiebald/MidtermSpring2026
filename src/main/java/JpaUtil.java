import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

final class JpaUtil {
    private static EntityManagerFactory entityManagerFactory;

    private JpaUtil() {
    }

    static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.url", DatabaseSettings.jdbcUrl());
            properties.put("jakarta.persistence.jdbc.user", DatabaseSettings.jdbcUser());
            properties.put("jakarta.persistence.jdbc.password", DatabaseSettings.jdbcPassword());
            entityManagerFactory = Persistence.createEntityManagerFactory("uno-pu", properties);
        }
        return entityManagerFactory;
    }

    static void configureForTests(String jdbcUrl) {
        close();
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        entityManagerFactory = Persistence.createEntityManagerFactory("uno-pu", properties);
    }

    static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }

    static void inTransaction(Consumer<EntityManager> work) {
        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();
        try {
            work.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    static <T> T query(Function<EntityManager, T> work) {
        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
        try {
            return work.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }
}
