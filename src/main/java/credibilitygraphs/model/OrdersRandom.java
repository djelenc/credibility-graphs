package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;

import java.util.*;


public class OrdersRandom extends AbstractTrustModel<PairwiseOrder> {
    // the "starting number" of false opinions
    // prevents new reporters from having too much influence
    private static final double TRUTH_OFFSET = 5.0;

    // opinions
    private boolean[][][] opPairwise = new boolean[0][0][0];
    private boolean[][][] opClosures = new boolean[0][0][0];
    private double[][] rcvOpinions = new double[0][0];

    // experiences
    private double[][] xpPairwise = new double[0][0];
    private double[][] xpClosure = new double[0][0];

    // local history
    private Past[] local = new Past[0];

    static class Past {
        // time decay factor
        private static final double TF = 0.1;

        // the length of history
        private static final int HISTORY_LENGTH = 10;

        // list of experiences
        final Experience[] experiences = new Experience[HISTORY_LENGTH];

        // timestamps of when the agent was right
        final int[] wrongs = new int[HISTORY_LENGTH];

        // timestamps of when the agent was wrong
        final int[] rights = new int[HISTORY_LENGTH];

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

        private double weighted(int currentTime, int[] data) {
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

    private int time = 0;

    @Override
    public void initialize(Object... objects) {
        time = 0;
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
            local[e.agent].addExperience(e);
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
                final double[] experiences1 = local[agent1].weightedExperience(time);
                final double[] experiences2 = local[agent2].weightedExperience(time);

                if (experiences1[0] < experiences2[0]) {
                    xpPairwise[agent1][agent2] = Math.min(experiences1[1], experiences2[1]);
                }
            }
        }

        // compute closure over pairwise experience comparisons
        Matrices.strongestPaths(xpPairwise, xpClosure);

        // updating past accuracy
        for (Experience e : list) {
            for (int reporter = 0; reporter < opClosures.length; reporter++) {
                int right = 0, wrong = 0;

                for (int agent = 0; agent < opClosures.length; agent++) {
                    if (xpClosure[agent][e.agent] > 0) {
                        if (opClosures[reporter][agent][e.agent]) {
                            right++;
                        } else if (opClosures[reporter][e.agent][agent]) {
                            wrong++;
                        }
                    }
                }

                if (right > wrong) {
                    local[reporter].addRight(time);
                } else if (wrong > right) {
                    local[reporter].addWrong(time);
                }
            }
        }
    }

    @Override
    public void processOpinions(List<Opinion> list) {
        // clear all absolute opinions from previous ticks
        for (int i = 0; i < rcvOpinions.length; i++) {
            for (int j = 0; j < rcvOpinions.length; j++) {
                rcvOpinions[i][j] = Double.NaN;
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
                    if (!Double.isNaN(rcvOpinions[reporter][source]) && !Double.isNaN(rcvOpinions[reporter][target])) {
                        opPairwise[reporter][source][target] = rcvOpinions[reporter][source] < rcvOpinions[reporter][target];
                    }
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
                        final Past past = local[reporter];
                        final double right = past.weightedRights(time);
                        final double wrong = past.weightedWrongs(time);

                        support += 1d / (1d + Math.exp(TRUTH_OFFSET + wrong - right));
                    }
                }
                // consider statement only if support is larger than 0
                if (support > 0) {
                    statements.add(new Statement(source, target, support));
                }
            }
        }

        // sort statements by support: from most to least reputable
        Collections.shuffle(statements);

        // create matrices
        // start with experiences and add statements incrementally
        final double[][] adjacency = new double[opClosures.length][opClosures.length];
        final double[][] strongestPaths = new double[opClosures.length][opClosures.length];
        for (int i = 0; i < xpClosure.length; i++) {
            System.arraycopy(xpPairwise[i], 0, adjacency[i], 0, xpPairwise.length);
            System.arraycopy(xpClosure[i], 0, strongestPaths[i], 0, xpClosure.length);
        }

        // final double[][] adjacency = new double[opClosures.length][opClosures.length];
        // final double[][] strongestPaths = new double[opClosures.length][opClosures.length];
        // Matrices.strongestPaths(adjacency, strongestPaths);

        // debugging
        // int expansion = 0, revision = 0, skip = 0;
        // perform non-prioritized revision in the order of most supported statements
        for (Statement s : statements) {
            if (strongestPaths[s.target][s.source] == 0d) {
                // if there is no contradiction, expand the KB with this statement
                Matrices.expand(adjacency, s.source, s.target, s.support, strongestPaths);
                // expansion++;
            } else if (strongestPaths[s.target][s.source] < s.support) {
                // if there is a contradiction, but the support for the new statement
                // is stronger, contract the opposite statement from the KB, and
                // expand it with new statement
                Matrices.contract(adjacency, s.target, s.source, strongestPaths);
                Matrices.expand(adjacency, s.source, s.target, s.support, strongestPaths);
                // revision++;
            } /*else {
                // else, skip new data
                skip++;
            }*/
        }

        /*System.out.println("TIME: " + time);
        System.out.println(Matrices.printMatrix(xpClosure));
        System.out.println(Matrices.printMatrix(strongestPaths));*/


        final Map<Integer, PairwiseOrder> order = new HashMap<>();
        for (int agent = 0; agent < adjacency.length; agent++) {
            order.put(agent, new PairwiseOrder(agent, adjacency));
        }

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
        final int limit = list.stream().max(Integer::compareTo).orElse(0) + 1;

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

        // increase and initialize past experiences
        final Past[] _local = new Past[limit];
        System.arraycopy(local, 0, _local, 0, currentSize);
        for (int i = 0; i < limit; i++) {
            if (_local[i] == null) {
                _local[i] = new Past();
            }
        }
        local = _local;
    }

    @Override
    public String toString() {
        return "Credibility Dynamics (Random)";
    }
}
