import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int roundsPlayed;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_id")
    private PlayerEntity winner;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundEntity> rounds = new ArrayList<RoundEntity>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScoreEntity> scores = new ArrayList<ScoreEntity>();

    protected GameEntity() {
    }

    public GameEntity(LocalDateTime startedAt, LocalDateTime completedAt, int roundsPlayed, PlayerEntity winner) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.roundsPlayed = roundsPlayed;
        this.winner = winner;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public PlayerEntity getWinner() {
        return winner;
    }

    public List<RoundEntity> getRounds() {
        return rounds;
    }

    public List<ScoreEntity> getScores() {
        return scores;
    }

    public void addRound(RoundEntity round) {
        round.setGame(this);
        rounds.add(round);
    }

    public void addScore(ScoreEntity score) {
        score.setGame(this);
        scores.add(score);
    }
}
