import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "scores", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "player_id"}))
class ScoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    protected ScoreEntity() {
    }

    ScoreEntity(GameEntity game, PlayerEntity player, int totalScore) {
        this.game = game;
        this.player = player;
        this.totalScore = totalScore;
    }

    PlayerEntity getPlayer() {
        return player;
    }

    int getTotalScore() {
        return totalScore;
    }
}
