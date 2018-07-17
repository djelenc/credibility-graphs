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
        final double[][] closure = new double[adjacency.length][adjacency.length];
        final double[][] expected = new double[][]{
                {0, 2, 2, 2, 2},
                {0, 0, 3, 0, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 3, 0, 4},
                {0, 0, 0, 0, 0}
        };

        Matrices.closure(adjacency, closure);

        Assert.assertArrayEquals(expected, closure);
    }

    @Test
    public void expansion() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 2, 0},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };
        final double[][] closure = new double[adjacency.length][adjacency.length];
        Matrices.closure(adjacency, closure);

        Matrices.expand(adjacency, 1, 3, 2d, closure);

        Assert.assertEquals(adjacency[1][3], 2d, 0.0001);
        final double[][] expectedClosure = new double[][]{
                {0, 2, 1, 2, 2},
                {0, 0, 1, 2, 2},
                {0, 0, 0, 2, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedClosure, closure);
    }

    @Test
    public void contraction1() {
        final double[][] adjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 1, 2, 0, 1.25d},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 1.5f, 0, 1, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0}
        };
        final double[][] closure = new double[adjacency.length][adjacency.length];
        Matrices.closure(adjacency, closure);

        Matrices.contract(adjacency, 0, 4, closure);

        final double[][] expectedAdjacency = new double[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 1.25d},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        final double[][] expectedClosure = new double[][]{
                {0, 2, 0, 2, 0, 1.25d},
                {0, 0, 0, 2, 0, 1.25d},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedAdjacency, adjacency);
        Assert.assertArrayEquals(expectedClosure, closure);
    }

    @Test
    public void contraction2() {
        final double[][] adjacency = new double[][]{
                {0, 1, 0},
                {0, 0, 1},
                {0, 0, 0},
        };
        final double[][] closure = new double[adjacency.length][adjacency.length];
        Matrices.closure(adjacency, closure);

        Matrices.contract(adjacency, 0, 2, closure);

        // both matrices should contain only zeros
        final double[][] expectedAdjacency = new double[adjacency.length][adjacency.length];
        final double[][] expectedClosure = new double[adjacency.length][adjacency.length];

        Assert.assertArrayEquals(expectedAdjacency, adjacency);
        Assert.assertArrayEquals(expectedClosure, closure);
    }
}
