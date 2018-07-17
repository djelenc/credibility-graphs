package credibilitygraphs.model;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;
import atb.trustmodel.AbstractTrustModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Orders extends AbstractTrustModel<Order> {
    private static final int SIZE = 50;

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
    public void calculateTrust() {
    }

    @Override
    public void processExperiences(List<Experience> list) {
        /*for (Experience experience : list) {
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
        }*/
    }

    @Override
    public void processOpinions(List<Opinion> list) {
        // clear all absolute opinions from previous ticks
        /*for (int i = 0; i < rcvOpinions.length; i++) {
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
        }*/
    }

    @Override
    public Map<Integer, Order> getTrust(int service) {
        // sum closures into preferences
        /*final double[][] preferences = computePreferences(opClosures);

        // adds experience counts to the matrix of preferences
        // XXX: It seems to not do much
        addExperiences(preferences, xpClosure, xpCount);

        // find the strongest paths
        final double[][] paths = findStrongestPaths(preferences);

        final Map<Integer, Order> order = new HashMap<>();
        for (int agent = 0; agent < paths.length; agent++) {
            order.put(agent, new Order(agent, paths));
        }

        return order;*/
        return new HashMap<>();
    }
}