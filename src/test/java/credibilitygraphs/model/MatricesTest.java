package credibilitygraphs.model;

import org.junit.Assert;
import org.junit.Test;


public class MatricesTest {

    @Test
    public void closure() {
        final float[][] adjacency = new float[][]{
                {0f, 2f, 2f, 2f, 0f},
                {0f, 0f, 3f, 0f, 1f},
                {0f, 0f, 0f, 0f, 2f},
                {0f, 0f, 3f, 0f, 4f},
                {0f, 0f, 0f, 0f, 0f}
        };
        final float[][] closure = new float[adjacency.length][adjacency.length];
        final float[][] expected = new float[][]{
                {0f, 2f, 2f, 2f, 2f},
                {0f, 0f, 3f, 0f, 2f},
                {0f, 0f, 0f, 0f, 2f},
                {0f, 0f, 3f, 0f, 4f},
                {0f, 0f, 0f, 0f, 0f}
        };

        Matrices.closure(adjacency, closure);

        Assert.assertArrayEquals(expected, closure);
    }

    @Test
    public void expansion() {
        final float[][] adjacency = new float[][]{
                {0, 2, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 2, 0},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };
        final float[][] closure = new float[adjacency.length][adjacency.length];
        Matrices.closure(adjacency, closure);

        Matrices.expand(adjacency, 1, 3, 2f, closure);

        Assert.assertEquals(adjacency[1][3], 2f, 0.0001);
        final float[][] expectedClosure = new float[][]{
                {0, 2, 1, 2, 2},
                {0, 0, 1, 2, 2},
                {0, 0, 0, 2, 2},
                {0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedClosure, closure);
    }

    @Test
    public void contraction() {
        final float[][] adjacency = new float[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 1, 2, 0, 1.25f},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 1.5f, 0, 1, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0}
        };
        final float[][] closure = new float[adjacency.length][adjacency.length];
        Matrices.closure(adjacency, closure);

        Matrices.contract(adjacency, 0, 4, closure);

        final float[][] expectedAdjacency = new float[][]{
                {0, 2, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 1.25f},
                {0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };

        Assert.assertArrayEquals(expectedAdjacency, adjacency);
    }
}
