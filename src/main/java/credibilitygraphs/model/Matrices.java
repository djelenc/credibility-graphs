package credibilitygraphs.model;

public class Matrices {
    /**
     * Computes transitive closure over given adjacency matrix and stores the result into closure parameter.
     */
    static void closure(double[][] adjacency, double[][] closure) {
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

    /**
     * Computes transitive closure over given adjacency matrix and stores the result into closure parameter.
     */
    static void closure(boolean[][] adjacency, boolean[][] closure) {
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
    }

    /**
     * Expands given adjacency matrix with edge between source and target of value val.
     * Dynamically updates the corresponding closure matrix.
     */
    static void expand(double[][] adjacency, int source, int target, double val, double[][] closure) {
        if (closure[target][source] > 0) {
            // skip the KB supports the contrary
            System.err.printf("Aborted expansion [%d < %d, %.2f], because existing KB contains [%d < %d, %.2f]",
                    source, target, val, target, source, closure[target][source]);
            return;
        }

        // OLD: adjacency[source][target] = val;
        adjacency[source][target] += val;

        for (int i = 0; i < closure.length; i++) {
            for (int j = 0; j < closure.length; j++) {
                if ((closure[i][source] > 0 || i == source) && (closure[target][j] > 0 || target == j)) {
                    if (i == source && j == target) {
                        closure[i][j] = Math.max(val, closure[i][j]);
                    } else {
                        closure[i][j] = Math.min(val, Math.max(closure[i][source], closure[target][j]));
                    }
                }
            }
        }
    }

    /**
     * Removes all paths from from source to target. The adjacency and the closure matrices are dynamically updated.
     */
    static void contract(double[][] adjacency, int source, int target, double[][] closure) {
        final double support = closure[source][target];

        for (int i = 0; i < closure.length; i++) {
            for (int j = 0; j < closure.length; j++) {
                if (support >= adjacency[i][j]
                        && adjacency[i][j] > 0
                        && (closure[source][i] >= adjacency[i][j] || source == i)
                        && (closure[j][target] >= adjacency[i][j] || target == j)) {
                    adjacency[i][j] = 0;
                }
            }
        }

        // The closure update is naively implemented by rerunning the closure procedure in O(n^3)
        // It should be done dynamically in O(n^2). Not sure how to implement it just yet.
        closure(adjacency, closure);
    }

    static void printMatrix(double[][] matrix) {
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
}
