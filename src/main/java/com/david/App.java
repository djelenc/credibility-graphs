package com.david;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ExportException;

import java.io.IOException;
import java.util.Map;

import static com.david.CredibilityOrders.*;

public final class App {

    public static void drawTuples() throws ExportException, IOException {
        final Graph<String, DefaultEdge> graph = parseTuples(
                "(C, D), (C, E), (D, F), (E, F), (E, G), (H, I)");
        exportDOT(graph, "./fig-tuples.png", false);

    }

    public static void drawObjects() throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(
                "(C, D, A), (C, E, B), (D, F, A), (E, F, B), (E, G, A), (H, I, B)");
        final Graph<String, ReporterEdge> graph = merge(graphs);

        exportDOT(graph, "./fig-objects.png", true);
    }

    public static void drawEX2() throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(
                "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)");
        final Graph<String, ReporterEdge> graph = merge(graphs);

        exportDOT(graph, "./fig-ex2.png", true);
    }

    public static void main(String[] args) throws ExportException, IOException {
        //drawTuples();
        //drawObjects();
        drawEX2();
    }
}
