import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JpaGameHistoryRepository implements GameHistoryRepository {
    private final EntityManagerFactory entityManagerFactory;

    public JpaGameHistoryRepository() {
        this(Persistence.createEntityManagerFactory("uno-history"));
    }

    public JpaGameHistoryRepository(Map<String, String> properties) {
        this(Persistence.createEntityManagerFactory("uno-history", properties));
    }

    private JpaGameHistoryRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(GameResult result) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Map<String, PlayerEntity> players = loadPlayers(entityManager, result);
            PlayerEntity winner = players.get(result.getWinnerName());
            GameEntity game = new GameEntity(
                    result.getStartedAt(),
                    result.getCompletedAt(),
                    result.getRounds().size(),
                    winner
            );

            for (RoundResult round : result.getRounds()) {
                game.addRound(new RoundEntity(
                        round.getRoundNumber(),
                        players.get(round.getWinnerName()),
                        round.getPointsScored()
                ));
            }

            for (Map.Entry<String, Integer> entry : result.getFinalScores().entrySet()) {
                game.addScore(new ScoreEntity(players.get(entry.getKey()), entry.getValue().intValue()));
            }

            entityManager.persist(game);
            transaction.commit();
        } catch (RuntimeException error) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw error;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<GameSummary> recentGames(int limit) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<GameEntity> games = entityManager.createQuery(
                            "select g from GameEntity g join fetch g.winner order by g.completedAt desc",
                            GameEntity.class)
                    .setMaxResults(limit)
                    .getResultList();
            List<GameSummary> summaries = new ArrayList<GameSummary>();
            for (GameEntity game : games) {
                summaries.add(new GameSummary(
                        game.getId(),
                        game.getCompletedAt(),
                        game.getRoundsPlayed(),
                        game.getWinner().getName()
                ));
            }
            return summaries;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<PlayerWinCount> playerWinCounts() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<Object[]> rows = entityManager.createQuery(
                            "select g.winner.name, count(g) " +
                                    "from GameEntity g group by g.winner.name order by count(g) desc, g.winner.name",
                            Object[].class)
                    .getResultList();
            List<PlayerWinCount> counts = new ArrayList<PlayerWinCount>();
            for (Object[] row : rows) {
                counts.add(new PlayerWinCount((String) row[0], ((Long) row[1]).longValue()));
            }
            return counts;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<HighScore> highestScores(int limit) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<Object[]> rows = entityManager.createQuery(
                            "select s.player.name, s.finalScore, s.game.completedAt " +
                                    "from ScoreEntity s order by s.finalScore desc, s.game.completedAt desc",
                            Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            List<HighScore> scores = new ArrayList<HighScore>();
            for (Object[] row : rows) {
                scores.add(new HighScore((String) row[0], ((Integer) row[1]).intValue(), (java.time.LocalDateTime) row[2]));
            }
            return scores;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }

    private Map<String, PlayerEntity> loadPlayers(EntityManager entityManager, GameResult result) {
        Map<String, PlayerEntity> players = new LinkedHashMap<String, PlayerEntity>();
        for (String name : result.getPlayerNames()) {
            players.put(name, findOrCreatePlayer(entityManager, name));
        }
        for (String name : result.getFinalScores().keySet()) {
            if (!players.containsKey(name)) {
                players.put(name, findOrCreatePlayer(entityManager, name));
            }
        }
        return players;
    }

    private PlayerEntity findOrCreatePlayer(EntityManager entityManager, String name) {
        List<PlayerEntity> existing = entityManager.createQuery(
                        "select p from PlayerEntity p where p.name = :name",
                        PlayerEntity.class)
                .setParameter("name", name)
                .getResultList();
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        PlayerEntity player = new PlayerEntity(name);
        entityManager.persist(player);
        return player;
    }

    public static Map<String, String> inMemoryTestProperties(String databaseName) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
        return properties;
    }
}
