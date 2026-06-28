import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rounds")
public class RoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @Column(nullable = false)
    private int roundNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_id")
    private PlayerEntity winner;

    @Column(nullable = false)
    private int pointsScored;

    protected RoundEntity() {
    }

    public RoundEntity(int roundNumber, PlayerEntity winner, int pointsScored) {
        this.roundNumber = roundNumber;
        this.winner = winner;
        this.pointsScored = pointsScored;
    }

    public Long getId() {
        return id;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public PlayerEntity getWinner() {
        return winner;
    }

    public int getPointsScored() {
        return pointsScored;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }
}
