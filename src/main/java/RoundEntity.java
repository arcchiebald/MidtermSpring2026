import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
class RoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winner_id", nullable = false)
    private PlayerEntity winner;

    @Column(name = "points_awarded", nullable = false)
    private int pointsAwarded;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    protected RoundEntity() {
    }

    RoundEntity(GameEntity game, int roundNumber, PlayerEntity winner, int pointsAwarded, LocalDateTime endedAt) {
        this.game = game;
        this.roundNumber = roundNumber;
        this.winner = winner;
        this.pointsAwarded = pointsAwarded;
        this.endedAt = endedAt;
    }

    int getRoundNumber() {
        return roundNumber;
    }

    PlayerEntity getWinner() {
        return winner;
    }

    int getPointsAwarded() {
        return pointsAwarded;
    }

    LocalDateTime getEndedAt() {
        return endedAt;
    }
}
