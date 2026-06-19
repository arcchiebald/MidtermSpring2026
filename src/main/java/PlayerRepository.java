import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

final class PlayerRepository {
    PlayerEntity findOrCreate(EntityManager entityManager, String name) {
        try {
            return entityManager.createQuery(
                            "SELECT p FROM PlayerEntity p WHERE p.name = :name", PlayerEntity.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException ex) {
            PlayerEntity player = new PlayerEntity(name);
            entityManager.persist(player);
            return player;
        }
    }
}
