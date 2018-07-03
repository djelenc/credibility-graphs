package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* TODO:
   - Vkljuci dejavnik odstotka preverjenih mnenj -- kaksen % prejetih mnenj se je dalo preveriti
       Ideja: vec mnenj ko se da preveriti, bolj zaupamo njegovim nepreverjenjim mnenjem
   - Vkljuci kredibilnost
   - Vkljuci druzbeno povezanost
   - Vkljuci casovno diskontiranje
 */
public class Schulze extends AbstractTrustModel<Order> {
    private static final int SIZE = 50;

    // opinions
    private boolean[][][] opPairwise = new boolean[SIZE][SIZE][SIZE];
    private boolean[][][] opClosures = new boolean[SIZE][SIZE][SIZE];
    private double[][] rcvOpinions = new double[SIZE][SIZE];

    // experiences
    private int[] xpCount = new int[SIZE];
    private double[] xpSum = new double[SIZE];
    private boolean[][] xpPairwise = new boolean[SIZE][SIZE];
    private boolean[][] xpClosure = new boolean[SIZE][SIZE];
    private int[] paRight = new int[SIZE];
    private int[] paWrong = new int[SIZE];

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
        final int currentSize = xpSum.length;
        final int limit = list.stream().max(Integer::compareTo).orElse(SIZE) + 1;

        if (limit <= currentSize) {
            return;
        }

        final boolean[][][] _opPairwise = new boolean[limit][limit][limit];
        final boolean[][][] _opClosures = new boolean[limit][limit][limit];

        final double[][] _rcvOpinions = new double[limit][limit];
        final boolean[][] _xpPairwise = new boolean[limit][limit];
        final boolean[][] _xpClosure = new boolean[limit][limit];

        for (int i = 0; i < currentSize; i++) {
            for (int j = 0; j < currentSize; j++) {
                System.arraycopy(opPairwise[i][j], 0, _opPairwise[i][j], 0, currentSize);
                System.arraycopy(opClosures[i][j], 0, _opClosures[i][j], 0, currentSize);
            }
            System.arraycopy(rcvOpinions[i], 0, _rcvOpinions[i], 0, currentSize);
            System.arraycopy(xpPairwise[i], 0, _xpPairwise[i], 0, currentSize);
            System.arraycopy(xpClosure[i], 0, _xpClosure[i], 0, currentSize);
        }

        opPairwise = _opPairwise;
        opClosures = _opClosures;

        rcvOpinions = _rcvOpinions;
        xpPairwise = _xpPairwise;
        xpClosure = _xpClosure;

        final int[] _xpCount = new int[limit];
        System.arraycopy(xpCount, 0, _xpCount, 0, currentSize);
        xpCount = _xpCount;

        final double[] _xpSum = new double[limit];
        System.arraycopy(xpSum, 0, _xpSum, 0, currentSize);
        xpSum = _xpSum;

        final int[] _paRight = new int[limit];
        System.arraycopy(paRight, 0, _paRight, 0, currentSize);
        paRight = _paRight;

        final int[] _paWrong = new int[limit];
        System.arraycopy(paWrong, 0, _paWrong, 0, currentSize);
        paWrong = _paWrong;
    }

    @Override
    public void calculateTrust() {
    }

    @Override
    public void processExperiences(List<Experience> list) {
        for (Experience experience : list) {
            xpSum[experience.agent] += experience.outcome;
            xpCount[experience.agent] += 1;
        }

        // clear all pairwise experience comparisons
        for (int agent1 = 0; agent1 < xpPairwise.length; agent1++) {
            for (int agent2 = 0; agent2 < xpPairwise.length; agent2++) {
                xpPairwise[agent1][agent2] = false;
            }
        }

        // fill the array of pairwise experience comparisons
        for (int agent1 = 0; agent1 < xpPairwise.length; agent1++) {
            for (int agent2 = 0; agent2 < xpPairwise.length; agent2++) {
                xpPairwise[agent1][agent2] = xpSum[agent1] / xpCount[agent1] > xpSum[agent2] / xpCount[agent2];
            }
        }

        // compute closure over pairwise experience comparisons
        xpClosure = closure(xpPairwise);

        for (Experience ex : list) {
            final int target = ex.agent;

            for (int agent = 0; agent < opClosures.length; agent++) {
                if (xpCount[agent] > 0) { // do we have an experience to compare this against
                    final boolean value = xpClosure[agent][target];

                    for (int reporter = 0; reporter < opClosures.length; reporter++) {
                        if (value == opClosures[reporter][agent][target]) {
                            paRight[reporter] += 1;
                        } else {
                            paWrong[reporter] += 1;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void processOpinions(List<Opinion> list) {
        // clear all absolute opinions from previous ticks
        for (int i = 0; i < rcvOpinions.length; i++) {
            for (int j = 0; j < rcvOpinions.length; j++) {
                rcvOpinions[i][j] = 0d;
            }
        }
        // clear all pairwise comparisons from previous ticks
        for (int reporter = 0; reporter < opPairwise.length; reporter++) {
            for (int agent1 = 0; agent1 < opPairwise.length; agent1++) {
                for (int agent2 = 0; agent2 < opPairwise.length; agent2++) {
                    opPairwise[reporter][agent1][agent2] = false;
                }
            }
        }

        // fill the array of absolute opinions using the received opinions
        for (Opinion opinion : list) {
            rcvOpinions[opinion.agent1][opinion.agent2] = opinion.internalTrustDegree;
        }

        // fill the array of pairwise opinion comparisons
        for (int reporter = 0; reporter < opPairwise.length; reporter++) {
            for (int agent1 = 0; agent1 < opPairwise.length; agent1++) {
                for (int agent2 = 0; agent2 < opPairwise.length; agent2++) {
                    opPairwise[reporter][agent1][agent2] = rcvOpinions[reporter][agent1] > rcvOpinions[reporter][agent2];
                }
            }
        }

        // compute closures over all pairwise comparisons
        for (int reporter = 0; reporter < opPairwise.length; reporter++) {
            opClosures[reporter] = closure(opPairwise[reporter]);
        }
    }

    /**
     * Computes a transitive closure over given adjacency matrix
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

    /**
     * Sums all closures into preference matrix
     *
     * @param closures an array of closure matrices
     * @return component-wise sum of all closure matrices
     */
    private double[][] computePreferences(boolean[][][] closures) {
        final double[][] preferences = new double[closures.length][closures.length];

        for (int reporter = 0; reporter < closures.length; reporter++) {
            for (int agent1 = 0; agent1 < closures.length; agent1++) {
                for (int agent2 = 0; agent2 < closures.length; agent2++) {
                    // preferences[agent1][agent2] += closures[reporter][agent1][agent2] ? 1 : 0;
                    if (closures[reporter][agent1][agent2]) {
                        preferences[agent1][agent2] += 1d / (1d + Math.exp(paWrong[reporter] - paRight[reporter]));
                    }
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
    private double[][] findStrongestPaths(double[][] preferences) {
        final double[][] paths = new double[preferences.length][preferences.length];

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
        // sum closures into preferences
        final double[][] preferences = computePreferences(opClosures);

        // adds experience counts to the matrix of preferences
        // XXX: It seems to not do much
        addExperiences(preferences, xpClosure, xpCount);

        // find the strongest paths
        final double[][] paths = findStrongestPaths(preferences);

        final Map<Integer, Order> order = new HashMap<>();
        for (int agent = 0; agent < paths.length; agent++) {
            order.put(agent, new Order(agent, paths));
        }

        return order;
    }

    /**
     * Adds the number of experiences to given matrix of opinion preferences
     *
     * @param preferences matrix of opinion preferences
     * @param xpClosure   matrix of closures as given by experiences
     * @param xpCount     an array of experience counts
     */
    private void addExperiences(double[][] preferences, boolean[][] xpClosure, int[] xpCount) {
        for (int source = 0; source < preferences.length; source++) {
            for (int target = 0; target < preferences.length; target++) {
                if (source != target && xpClosure[source][target]) {
                    preferences[source][target] += Math.min(xpCount[source], xpCount[target]);
                }
            }
        }
    }

    private void printMatrix(double[][] matrix) {
        final StringBuilder sb = new StringBuilder();

        for (int source = 0; source < matrix.length; source++) {
            sb.append("[");
            for (int target = 0; target < matrix.length; target++) {
                sb.append(String.format("%.2f", matrix[source][target]));

                if (target == matrix.length - 1) {
                    sb.append("]");
                } else {
                    sb.append(", ");
                }

            }
            sb.append(System.lineSeparator());
        }

        System.out.println(sb.toString());
    }

    private void printMatrix(boolean[][] matrix) {
        final StringBuilder sb = new StringBuilder();

        for (int source = 0; source < matrix.length; source++) {
            sb.append("[");
            for (int target = 0; target < matrix.length; target++) {
                sb.append(matrix[source][target] ? 1 : 0);

                if (target == matrix.length - 1) {
                    sb.append("]");
                } else {
                    sb.append(", ");
                }

            }
            sb.append(System.lineSeparator());
        }

        System.out.println(sb.toString());
    }
}

class Order implements Comparable<Order> {
    private final double[][] paths;
    private final int agent;

    Order(int agent, double[][] paths) {
        this.agent = agent;
        this.paths = paths;
    }

    @Override
    public int compareTo(@NotNull Order that) {
        return Double.compare(paths[this.agent][that.agent], paths[that.agent][this.agent]);
    }
}