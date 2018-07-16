package credibilitygraphs.model;

public class Matrices {
    static void closure(float[][] adjacency, float[][] closure) {
        for (int i = 0; i < adjacency.length; i++) {
            System.arraycopy(adjacency[i], 0, closure[i], 0, adjacency[0].length);
        }

        for (int k = 0; k < adjacency.length; k++) {
            for (int i = 0; i < adjacency.length; i++) {
                for (int j = 0; j < adjacency.length; j++) {
                    closure[i][j] = Math.max(closure[i][j], Math.min(closure[i][k], closure[k][j]));
                }
            }
        }
    }

    static void expand(float[][] adjacency, int src, int tgt, float val, float[][] closure) {
        adjacency[src][tgt] = val;

        for (int i = 0; i < closure.length; i++) {
            for (int j = 0; j < closure.length; j++) {
                if ((closure[i][src] > 0 || i == src) && (closure[tgt][j] > 0 || tgt == j)) {
                    if (i == src && j == tgt) {
                        closure[i][j] = Math.max(val, closure[i][j]);
                    } else {
                        closure[i][j] = Math.min(val, Math.max(closure[i][src], closure[tgt][j]));
                    }
                }
            }
        }
    }

    static void contract(float[][] adjacency, int src, int tgt, float[][] closure) {
        final float support = closure[src][tgt];

        for (int i = 0; i < closure.length; i++) {
            for (int j = 0; j < closure.length; j++) {
                if (support >= adjacency[i][j] && adjacency[i][j] > 0
                        && (closure[src][i] >= adjacency[i][j] || src == i)
                        && (closure[j][tgt] >= adjacency[i][j] || tgt == j)) {
                    adjacency[i][j] = 0;
                }
            }
        }

        /*
        for i in range(matrix.shape[0]):
            for j in range(matrix.shape[1]):
                cond = support >= new[i, j] > 0 and \
                        (closure[src, i] >= new[i, j] or src == i) and \
                        (closure[j, tgt] >= new[i, j] or tgt == j)
                if cond:
                    new[i, j] = 0
        return new
         */
    }
}
