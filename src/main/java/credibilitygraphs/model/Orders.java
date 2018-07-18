package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class Orders extends AbstractTrustModel<Orders.Rank> {
    private static final int SIZE = 10;

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
                xpPairwise[agent1][agent2] = xpSum[agent1] / xpCount[agent1] < xpSum[agent2] / xpCount[agent2];
            }
        }

        // compute closure over pairwise experience comparisons
        Matrices.closure(xpPairwise, xpClosure);

        // checking past accuracy
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
                rcvOpinions[i][j] = 0;
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
            for (int source = 0; source < opPairwise.length; source++) {
                for (int target = 0; target < opPairwise.length; target++) {
                    opPairwise[reporter][source][target] = rcvOpinions[reporter][source] < rcvOpinions[reporter][target];
                }
            }
        }

        // compute closures over all pairwise comparisons
        for (int reporter = 0; reporter < opPairwise.length; reporter++) {
            Matrices.closure(opPairwise[reporter], opClosures[reporter]);
        }
    }

    @Override
    public Map<Integer, Orders.Rank> getTrust(int service) {
        final List<Statement> statements = new ArrayList<>();

        for (int source = 0; source < opClosures.length; source++) {
            for (int target = 0; target < opClosures.length; target++) {
                if (source == target) {
                    continue;
                }

                // compute support for source < target
                double support = 0;
                for (int reporter = 0; reporter < opClosures.length; reporter++) {
                    // support += opClosures[reporter][source][target] ? 1 : 0;
                    support += opClosures[reporter][source][target] ? 1d / (1d + Math.exp(paWrong[reporter] - paRight[reporter])) : 0;
                }
                statements.add(new Statement(source, target, support));
            }
        }

        // sort statements by support
        statements.sort(Comparator.comparingDouble(o -> -o.support));

        // create matrices
        final double[][] adjacency = new double[opClosures.length][opClosures.length];
        final double[][] closure = new double[opClosures.length][opClosures.length];
        Matrices.closure(adjacency, closure);

        // debugging
        int expansion = 0, revision = 0, skip = 0;

        // perform non-prioritized revision in the order of most supported statements
        for (Statement s : statements) {
            if (closure[s.target][s.source] == 0d) {
                // if there is no contradiction, expand the KB with this statement
                Matrices.expand(adjacency, s.source, s.target, s.support, closure);
                expansion++;
            } else if (closure[s.target][s.source] < s.support) {
                // if there is a contradiction, but the support for the new statement
                // is stronger, contract the opposite statement from the KB, and
                // expand it with new statement
                Matrices.contract(adjacency, s.target, s.source, closure);
                Matrices.expand(adjacency, s.source, s.target, s.support, closure);
                revision++;
            } else {
                // else, skip new data
                skip++;
            }
        }

        final Map<Integer, Orders.Rank> order = new HashMap<>();
        for (int agent = 0; agent < adjacency.length; agent++) {
            order.put(agent, new Orders.Rank(agent, adjacency));
        }

        // debugging
        /* System.out.printf("E = %d, R = %d, S = %d%n", expansion, revision, skip);
        Matrices.printMatrix(closure);
        System.out.println();
        System.out.println(Arrays.toString(paRight));
        System.out.println(Arrays.toString(paWrong));

        final List<String> pAcc = IntStream.range(0, paRight.length)
                .mapToObj(i -> String.format("%.2f", 1d / (1d + Math.exp(paWrong[i] - paRight[i]))))
                .collect(Collectors.toList());
        System.out.println(pAcc);
        System.out.println();
        System.out.println(Arrays.toString(xpCount));*/

        return order;
    }

    private static class Statement {
        final int source, target;
        final double support;

        private Statement(int source, int target, double support) {
            this.source = source;
            this.target = target;
            this.support = support;
        }

        @Override
        public String toString() {
            return String.format("[%d < %d, %.2f]", source, target, support);
        }
    }

    static class Rank implements Comparable<Rank> {
        private final double[][] matrix;
        private final int agent;

        private Rank(int agent, double[][] matrix) {
            this.agent = agent;
            this.matrix = matrix;
        }

        @Override
        public int compareTo(@NotNull Rank that) {
            return Double.compare(matrix[that.agent][this.agent], matrix[this.agent][that.agent]);
        }
    }

    @Override
    public void setAgents(List<Integer> list) {
        // expands all arrays when the number of agents increases

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
}