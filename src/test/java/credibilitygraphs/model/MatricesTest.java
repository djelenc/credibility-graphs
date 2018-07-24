package credibilitygraphs.model;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;


public class MatricesTest {

    @Test
    public void closure() {
        final double[][] adjacency = new double[][]{
                {0, 2, 2, 2, 0},
                {0, 0, 3, 0, 1},
                {0, 0, 0, 0, 2},
                {0, 0, 3, 0, 4},
                {0, 0, 0, 0, 0}
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        final double[][] expected = new double[][]{
                {0, 2, 2, 2, 2},
                {0, 0, 3, 0, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 3, 0, 4},
                {0, 0, 0, 0, 0}
        };

        Matrices.strongestPaths(adjacency, strongestPaths);

        Assert.assertArrayEquals(expected, strongestPaths);
    }

    @Test
    public void expansionOK() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 2, 0},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        Matrices.expand(adjacency, 1, 3, 2, strongestPaths);

        Assert.assertEquals(adjacency[1][3], 2, 0.0001);
        final double[][] expectedStrongestPaths = new double[][]{
                {0, 2, 1, 2, 2},
                {0, 0, 1, 2, 2},
                {0, 0, 0, 2, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedStrongestPaths, strongestPaths);
    }

    @Test(expected = Error.class)
    public void expansionAborted() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 2, 0},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        Matrices.expand(adjacency, 1, 0, 3, strongestPaths);
    }

    @Test
    public void contractionOK() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 1, 2, 0, 1.25},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 1.5, 0, 1, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0}
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        Matrices.contract(adjacency, 0, 4, strongestPaths);

        final double[][] expectedAdjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 1.25},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        final double[][] expectedStrongestPaths = new double[][]{
                {0, 2, 0, 2, 0, 1.25},
                {0, 0, 0, 2, 0, 1.25},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedAdjacency, adjacency);
        Assert.assertArrayEquals(expectedStrongestPaths, strongestPaths);
    }

    @Test
    public void contractionRemoveAllEdges() {
        final double[][] adjacency = new double[][]{
                {0, 1, 0},
                {0, 0, 1},
                {0, 0, 0},
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        Matrices.contract(adjacency, 0, 2, strongestPaths);

        // both matrices should contain only zeros
        final double[][] expectedAdjacency = new double[adjacency.length][adjacency.length];
        final double[][] expectedStrongestPaths = new double[adjacency.length][adjacency.length];

        Assert.assertArrayEquals(expectedAdjacency, adjacency);
        Assert.assertArrayEquals(expectedStrongestPaths, strongestPaths);
    }

    @Ignore
    @Test
    public void expansionRandomTests() {
        final int nodes = 15;
        final double edgeProbability = 0.7;
        final double maxEdgeValue = 9.99;
        final int totalRuns = 1000000;

        for (int seed = 0; seed < totalRuns; seed++) {
            final Random random = new Random(seed);

            // create random rankings
            final int[] ranking = new int[nodes];
            for (int i = 0; i < ranking.length; i++) {
                ranking[i] = random.nextInt(nodes * nodes);
            }

            // create a random DAG
            final double[][] adjacency = new double[nodes][nodes];
            for (int source = 0; source < adjacency.length; source++) {
                for (int target = 0; target < adjacency.length; target++) {
                    if (ranking[source] < ranking[target] && random.nextDouble() < edgeProbability) {
                        adjacency[source][target] = random.nextDouble() * maxEdgeValue;
                    }
                }
            }
            final double[][] strongestPaths = new double[nodes][nodes];
            Matrices.strongestPaths(adjacency, strongestPaths);

            // add a random node, that will change the graph
            int source, target;
            do {
                source = random.nextInt(nodes);
                target = random.nextInt(nodes);
            } while (ranking[source] >= ranking[target]);

            // remember old value
            final double oldEdgeValue = adjacency[source][target];

            // expansion
            Matrices.expand(adjacency, source, target, maxEdgeValue, strongestPaths);

            // recheck by manually running the strongest paths algorithm
            final double[][] recheckSP = new double[nodes][nodes];
            Matrices.strongestPaths(adjacency, recheckSP);

            // abort if there's a difference
            if (!Arrays.deepEquals(strongestPaths, recheckSP)) {
                System.out.println("ERROR: seed = " + seed);
                // revert adjacency value
                adjacency[source][target] = oldEdgeValue;
                System.out.println(Matrices.printNumpy(adjacency));
                System.out.println("r1 = np.copy(r0)");
                System.out.printf("r1[%d, %d] = %.2f%n", source, target, maxEdgeValue);
                Assert.fail("Matrix mismatch!");
                break;
            }
        }
    }
}
