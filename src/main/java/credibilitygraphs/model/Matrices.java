package credibilitygraphs.model;

public class Matrices {
    /**
     * Finds the strongest paths between each pair nodes given in adjacency matrix using
     * Floyd--Warshall algorithm, and stores the result into strongestPaths parameter.
     */
    static void strongestPaths(double[][] adjacency, double[][] strongestPaths) {
        for (int i = 0; i < adjacency.length; i++) {
            System.arraycopy(adjacency[i], 0, strongestPaths[i], 0, adjacency[0].length);
        }

        for (int k = 0; k < adjacency.length; k++) {
            for (int i = 0; i < adjacency.length; i++) {
                for (int j = 0; j < adjacency.length; j++) {
                    strongestPaths[i][j] = Math.max(strongestPaths[i][j], Math.min(strongestPaths[i][k], strongestPaths[k][j]));
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
     * Dynamically updates the corresponding strongestPaths matrix.
     */
    static void expand(double[][] adjacency, int source, int target, double val, double[][] strongestPaths) {
        if (strongestPaths[target][source] > 0) {
            // abort expansion; the KB supports the contrary statement
            System.err.printf("Aborted expansion [%d < %d, %.2f], because existing KB contains [%d < %d, %.2f]",
                    source, target, val, target, source, strongestPaths[target][source]);
            return;
        }

        // OLD: adjacency[source][target] = val;
        adjacency[source][target] = val;

        for (int i = 0; i < strongestPaths.length; i++) {
            for (int j = 0; j < strongestPaths.length; j++) {
                if ((strongestPaths[i][source] > 0 || i == source) && (strongestPaths[target][j] > 0 || target == j)) {
                    if (i == source && j == target) {
                        strongestPaths[i][j] = Math.max(val, strongestPaths[i][j]);
                    } else {
                        strongestPaths[i][j] = Math.min(Math.max(strongestPaths[i][source], strongestPaths[target][j]), val);
                    }
                }
            }
        }
    }

    /**
     * Removes all paths from from source to target. The adjacency and the strongest paths matrices are dynamically updated.
     */
    static void contract(double[][] adjacency, int source, int target, double[][] strongestPaths) {
        final double support = strongestPaths[source][target];

        for (int i = 0; i < strongestPaths.length; i++) {
            for (int j = 0; j < strongestPaths.length; j++) {
                if (support >= adjacency[i][j]
                        && adjacency[i][j] > 0
                        && (strongestPaths[source][i] >= adjacency[i][j] || source == i)
                        && (strongestPaths[j][target] >= adjacency[i][j] || target == j)) {
                    adjacency[i][j] = 0;
                }
            }
        }

        // The strongest paths are updated naively by rerunning the strongestPaths procedure in O(n^3)
        // It should be done dynamically in O(n^2). Not sure how to implement it just yet.
        strongestPaths(adjacency, strongestPaths);
    }

    static String printMatrix(double[][] matrix) {
        return printMatrix(matrix, true);
    }

    static String printMatrix(double[][] matrix, boolean indexes) {
        final StringBuilder sb = new StringBuilder();

        if (indexes) {
            sb.append(" ");
            for (int i = 0; i < matrix.length; i++) {
                sb.append(String.format("%6s", i));
            }
            sb.append(System.lineSeparator());
        }

        for (int source = 0; source < matrix.length; source++) {
            if (indexes) {
                sb.append(String.format("%3s [", source));
            } else {
                sb.append("[");
            }

            for (int target = 0; target < matrix.length; target++) {
                sb.append(String.format("%.2f", matrix[source][target]));

                if (target == matrix.length - 1) {
                    if (indexes) {
                        sb.append(String.format("] %-3s", source));
                    } else {
                        sb.append("]");
                    }
                } else {
                    sb.append(", ");
                }

            }
            sb.append(System.lineSeparator());
        }

        if (indexes) {
            sb.append(" ");

            for (int i = 0; i < matrix.length; i++) {
                sb.append(String.format("%6s", i));
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
