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
@Table(name = "games")
class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private PlayerEntity winner;

    protected GameEntity() {
    }

    GameEntity(LocalDateTime startedAt, LocalDateTime endedAt, PlayerEntity winner) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.winner = winner;
    }

    Long getId() {
        return id;
    }

    LocalDateTime getStartedAt() {
        return startedAt;
    }

    LocalDateTime getEndedAt() {
        return endedAt;
    }

    PlayerEntity getWinner() {
        return winner;
    }
}
