package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schulze extends AbstractTrustModel<Order> {
    private static final int SIZE = 100;

    private boolean[][][] pairwise = new boolean[SIZE][SIZE][SIZE];
    private boolean[][][] closures = new boolean[SIZE][SIZE][SIZE];
    private double[][] opinions = new double[SIZE][SIZE];

    @Override
    public void initialize(Object... objects) {
    }

    @Override
    public void setCurrentTime(int i) {
    }

    @Override
    public void setServices(List<Integer> list) {
    }

    @Override
    public void setAgents(List<Integer> list) {
    }

    @Override
    public void processOpinions(List<Opinion> list) {
        for (int i = 0; i < opinions.length; i++) {
            for (int j = 0; j < opinions.length; j++) {
                opinions[i][j] = 0d;
            }
        }

        for (int reporter = 0; reporter < pairwise.length; reporter++) {
            for (int agent1 = 0; agent1 < pairwise.length; agent1++) {
                for (int agent2 = 0; agent2 < pairwise.length; agent2++) {
                    pairwise[reporter][agent1][agent2] = false;
                }
            }
        }

        list.forEach(op -> opinions[op.agent1][op.agent2] = op.internalTrustDegree);

        for (int reporter = 0; reporter < pairwise.length; reporter++) {
            for (int agent1 = 0; agent1 < pairwise.length; agent1++) {
                for (int agent2 = 0; agent2 < pairwise.length; agent2++) {
                    pairwise[reporter][agent1][agent2] = opinions[reporter][agent1] > opinions[reporter][agent2];
                }
            }
        }
    }

    /**
     * Computes transitive closure over given adjacency matrix
     *
     * @param adjacency matrix
     * @return transitive closure
     */
    private boolean[][] closure(boolean[][] adjacency) {
        final boolean[][] closure = new boolean[adjacency.length][adjacency.length];

        for (int i = 0; i < adjacency.length; i++) {
            System.arraycopy(adjacency[i], 0, closure[i], 0, adjacency[0].length);
        }

        for (int k = 0; k < adjacency.length; k++) {
            for (int i = 0; i < adjacency.length; i++) {
                for (int j = 0; j < adjacency.length; j++) {
                    closure[i][j] = closure[i][j] || (closure[i][k] && closure[k][j]);
                }
            }
        }

        return closure;
    }

    @Override
    public void processExperiences(List<Experience> list) {
    }

    @Override
    public void calculateTrust() {
    }

    /**
     * Sums all closure into preference matrix
     *
     * @param closures an array of closure matrices
     * @return component-wise sum of all closure matrices
     */
    private int[][] computePreferences(boolean[][][] closures) {
        final int[][] preferences = new int[SIZE][SIZE];

        for (int agent1 = 0; agent1 < closures.length; agent1++) {
            for (int agent2 = 0; agent2 < closures.length; agent2++) {
                for (int reporter = 0; reporter < closures.length; reporter++) {
                    preferences[agent1][agent2] += closures[reporter][agent1][agent2] ? 1 : 0;
                }
            }
        }

        return preferences;
    }

    /**
     * Finds the strongest paths between each pair of agents using Floyd--Warshall algorithm.
     *
     * @param preferences a matrix of preferences between all pairs of agents
     * @return a matrix of strongest paths between all pairs of agents
     */
    private int[][] findStrongestPaths(int[][] preferences) {
        final int[][] paths = new int[SIZE][SIZE];

        for (int i = 0; i < preferences.length; i++) {
            System.arraycopy(preferences[i], 0, paths[i], 0, paths[0].length);
        }

        for (int k = 0; k < preferences.length; k++) {
            for (int i = 0; i < preferences.length; i++) {
                if (k == i) {
                    continue;
                }

                for (int j = 0; j < preferences.length; j++) {
                    if (j == k || i == j) {
                        continue;
                    }

                    paths[i][j] = Math.max(paths[i][j], Math.min(paths[i][k], paths[k][j]));
                }
            }
        }

        return paths;
    }

    @Override
    public Map<Integer, Order> getTrust(int service) {
        // compute closures over all opinions
        for (int reporter = 0; reporter < pairwise.length; reporter++) {
            closures[reporter] = closure(pairwise[reporter]);
        }

        // sum all closures into preferences
        final int[][] preferences = computePreferences(closures);

        // find the strongest paths
        final int[][] paths = findStrongestPaths(preferences);

        final Map<Integer, Order> order = new HashMap<>();
        for (int agent = 0; agent < SIZE; agent++) {
            order.put(agent, new Order(agent, paths));
        }

        return order;
    }
}

class Order implements Comparable<Order> {
    private final int[][] paths;
    private final int agent;

    Order(int agent, int[][] paths) {
        this.agent = agent;
        this.paths = paths;
    }

    @Override
    public int compareTo(@NotNull Order that) {
        return Integer.compare(paths[this.agent][that.agent], paths[that.agent][this.agent]);
    }
}