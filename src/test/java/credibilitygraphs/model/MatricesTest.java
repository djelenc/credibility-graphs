package credibilitygraphs.model;

import org.junit.Assert;
import org.junit.Test;


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

        Matrices.expand(adjacency, 1, 3, 2d, strongestPaths);

        Assert.assertEquals(adjacency[1][3], 2d, 0.0001);
        final double[][] expectedStrongestPaths = new double[][]{
                {0, 2, 1, 2, 2},
                {0, 0, 1, 2, 2},
                {0, 0, 0, 2, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedStrongestPaths, strongestPaths);
    }

    @Test
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

        final double[][] expectedStrongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, expectedStrongestPaths);

        Matrices.expand(adjacency, 1, 0, 3d, strongestPaths);

        Assert.assertArrayEquals(expectedStrongestPaths, strongestPaths);
    }

    @Test
    public void contractionOK() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 1, 2, 0, 1.25d},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 1.5f, 0, 1, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0}
        };
        final double[][] strongestPaths = new double[adjacency.length][adjacency.length];
        Matrices.strongestPaths(adjacency, strongestPaths);

        Matrices.contract(adjacency, 0, 4, strongestPaths);

        final double[][] expectedAdjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 1.25d},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        final double[][] expectedStrongestPaths = new double[][]{
                {0, 2, 0, 2, 0, 1.25d},
                {0, 0, 0, 2, 0, 1.25d},
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
}
