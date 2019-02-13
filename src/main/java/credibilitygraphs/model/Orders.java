package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;

import java.util.*;
import java.util.stream.Collectors;


public class Orders extends AbstractTrustModel<PairwiseOrder> {
    private static final int SIZE = 10;
    private static final int HISTORY_LENGTH = 10;
    private static final double TF = 0.1; // 0.01

    // opinions
    private boolean[][][] opPairwise = new boolean[SIZE][SIZE][SIZE];
    private boolean[][][] opClosures = new boolean[SIZE][SIZE][SIZE];
    private double[][] rcvOpinions = new double[SIZE][SIZE];

    // experiences
    private double[][] xpPairwise = new double[SIZE][SIZE];
    private double[][] xpClosure = new double[SIZE][SIZE];
    private int[] paRight = new int[SIZE];
    private int[] paWrong = new int[SIZE];


    static class Past {
        // list of experiences
        Experience[] experiences = new Experience[HISTORY_LENGTH];

        // timestamps of when the agent was right
        int[] wrongs = new int[HISTORY_LENGTH];

        // timestamps of when the agent was wrong
        int[] rights = new int[HISTORY_LENGTH];

        void addExperience(Experience element) {
            System.arraycopy(experiences, 0, experiences, 1, experiences.length - 1);
            experiences[0] = element;
        }

        void addRight(int time) {
            System.arraycopy(rights, 0, rights, 1, rights.length - 1);
            rights[0] = time;
        }

        void addWrong(int time) {
            System.arraycopy(wrongs, 0, wrongs, 1, wrongs.length - 1);
            wrongs[0] = time;
        }

        double[] weightedExperience(int currentTime) {
            double sum = 0, weights = 0;
            for (Experience e : experiences) {
                if (e == null) {
                    break;
                } else {
                    final double w = Math.exp(-TF * (currentTime - e.time));
                    sum += w * e.outcome;
                    weights += w;
                }
            }

            if (weights == 0) {
                return new double[]{0, 0};
            } else {
                return new double[]{sum / weights, weights};
            }
        }

        double weightedRights(int currentTime) {
            return weighted(currentTime, rights);
        }

        double weightedWrongs(int currentTime) {
            return weighted(currentTime, wrongs);
        }

        protected double weighted(int currentTime, int[] data) {
            double sum = 0;
            for (int time : data) {
                if (time == 0) {
                    break;
                } else {
                    sum += Math.exp(-TF * (currentTime - time));
                }
            }

            return sum;
        }
    }

    // local history
    private Map<Integer, Past> local = null;

    private int time = 0;

    @Override
    public void initialize(Object... objects) {
        time = 0;
        local = new LinkedHashMap<>();
    }

    @Override
    public void setCurrentTime(int i) {
        time = i;
    }

    @Override
    public void setServices(List<Integer> list) {
    }

    @Override
    public void calculateTrust() {
    }

    @Override
    public void processExperiences(List<Experience> list) {
        for (Experience e : list) {
            Past past = local.get(e.agent);

            if (null == past) { // no history
                past = new Past();
                local.put(e.agent, past);
            }

            past.addExperience(e);
        }

        // clear all pairwise experience comparisons
        for (int agent1 = 0; agent1 < xpPairwise.length; agent1++) {
            for (int agent2 = 0; agent2 < xpPairwise.length; agent2++) {
                xpPairwise[agent1][agent2] = 0;
            }
        }

        // fill the array of pairwise experience comparisons
        for (int agent1 = 0; agent1 < xpPairwise.length; agent1++) {
            for (int agent2 = 0; agent2 < xpPairwise.length; agent2++) {
                Past past1 = local.get(agent1);
                if (past1 == null) {
                    past1 = new Past();
                    local.put(agent1, past1);
                }
                final double[] experiences1 = past1.weightedExperience(time);

                Past past2 = local.get(agent2);
                if (past2 == null) {
                    past2 = new Past();
                    local.put(agent2, past2);
                }

                final double[] experiences2 = past2.weightedExperience(time);

                if (experiences1[0] < experiences2[0]) {
                    xpPairwise[agent1][agent2] = Math.min(experiences1[1], experiences2[1]);
                }
            }
        }

        // compute closure over pairwise experience comparisons
        Matrices.strongestPaths(xpPairwise, xpClosure);

        /*
        // debugging output
        System.out.println("EXPERIENCES at time " + time);
        local.forEach((agent, past) -> {
            final List<String> experiences = Arrays.stream(past.experiences).filter(Objects::nonNull).map(e -> String.format("%d: %.2f", e.time, e.outcome)).collect(Collectors.toList());
            final double[] weighted = past.weightedExperience(time);
            System.out.printf("%d: %s: (%.2f, %.2f)%n", agent, experiences, weighted[0], weighted[1]);
        });
        System.out.println(Matrices.printMatrix(xpPairwise));
        System.out.println(Matrices.printMatrix(xpClosure));*/

        // checking past accuracy
        for (Experience e : list) {
            for (int agent = 0; agent < opClosures.length; agent++) {
                final Past past = local.get(agent);
                if (past != null) {
                    if (xpClosure[agent][e.agent] > 0) {
                        for (int reporter = 0; reporter < opClosures.length; reporter++) {
                            if (opClosures[reporter][agent][e.agent]) {
                                paRight[reporter] += xpClosure[agent][e.agent];
                            } else if (opClosures[reporter][e.agent][agent]) {
                                paWrong[reporter] += xpClosure[agent][e.agent];
                            }
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
    public Map<Integer, PairwiseOrder> getTrust(int service) {
        final List<Statement> statements = new ArrayList<>();

        for (int source = 0; source < opClosures.length; source++) {
            for (int target = 0; target < opClosures.length; target++) {
                if (source == target) {
                    continue;
                }

                // compute support for source < target
                double support = 0;
                for (int reporter = 0; reporter < opClosures.length; reporter++) {
                    if (opClosures[reporter][source][target]) {
                        support += 1d / (1d + Math.exp(paWrong[reporter] - paRight[reporter]));
                    }
                }
                statements.add(new Statement(source, target, support));
            }
        }

        // sort statements by support
        statements.sort(Comparator.comparingDouble(o -> -o.support)); // most to least reputable
        // statements.sort(Comparator.comparingDouble(o -> o.support)); // least to most reputable
        // Collections.shuffle(statements); // randomly

        // create matrices
        final double[][] adjacency = new double[opClosures.length][opClosures.length];
        final double[][] strongestPaths = new double[opClosures.length][opClosures.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        // debugging
        int expansion = 0, revision = 0, skip = 0;

        // perform non-prioritized revision in the order of most supported statements
        for (Statement s : statements) {
            if (strongestPaths[s.target][s.source] == 0d) {
                // if there is no contradiction, expand the KB with this statement
                Matrices.expand(adjacency, s.source, s.target, s.support, strongestPaths);
                expansion++;
            } else if (strongestPaths[s.target][s.source] < s.support) {
                // if there is a contradiction, but the support for the new statement
                // is stronger, contract the opposite statement from the KB, and
                // expand it with new statement
                Matrices.contract(adjacency, s.target, s.source, strongestPaths);
                Matrices.expand(adjacency, s.source, s.target, s.support, strongestPaths);
                revision++;
            } else {
                // else, skip new data
                skip++;
            }
        }

        final Map<Integer, PairwiseOrder> order = new HashMap<>();
        for (int agent = 0; agent < adjacency.length; agent++) {
            order.put(agent, new PairwiseOrder(agent, adjacency));
        }

        // debugging
        /* System.out.printf("E = %d, R = %d, S = %d%n", expansion, revision, skip);
        System.out.println(Matrices.printMatrix(strongestPaths));
        System.out.println();
        System.out.println("R: " + Arrays.toString(paRight));
        System.out.println("W: " + Arrays.toString(paWrong));

        final List<String> pAcc = IntStream.range(0, paRight.length)
                .mapToObj(i -> String.format("%.2f", 1d / (1d + Math.exp(paWrong[i] - paRight[i]))))
                .collect(Collectors.toList());
        System.out.println("pAcc: " + pAcc);
        System.out.println();
        System.out.println("EXP: " + local);
        System.out.println("----------------------------------");
        System.out.println();*/

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

    @Override
    public void setAgents(List<Integer> list) {
        // expands all arrays when the number of agents increases

        final int currentSize = opPairwise.length;
        final int limit = list.stream().max(Integer::compareTo).orElse(SIZE) + 1;

        if (limit <= currentSize) {
            return;
        }

        final boolean[][][] _opPairwise = new boolean[limit][limit][limit];
        final boolean[][][] _opClosures = new boolean[limit][limit][limit];

        final double[][] _rcvOpinions = new double[limit][limit];
        final double[][] _xpPairwise = new double[limit][limit];
        final double[][] _xpClosure = new double[limit][limit];

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

        /*final int[] _xpCount = new int[limit];
        System.arraycopy(xpCount, 0, _xpCount, 0, currentSize);
        xpCount = _xpCount;

        final double[] _xpSum = new double[limit];
        System.arraycopy(xpSum, 0, _xpSum, 0, currentSize);
        xpSum = _xpSum;*/

        final int[] _paRight = new int[limit];
        System.arraycopy(paRight, 0, _paRight, 0, currentSize);
        paRight = _paRight;

        final int[] _paWrong = new int[limit];
        System.arraycopy(paWrong, 0, _paWrong, 0, currentSize);
        paWrong = _paWrong;
    }
}
