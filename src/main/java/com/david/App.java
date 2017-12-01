package com.david;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ExportException;

import java.io.IOException;
import java.util.Map;

import static com.david.CredibilityOrders.*;

public final class App {

    private static final String TUPLES = "(C, D), (C, E), (D, F), (E, F), (E, G), (H, I)";
    private static final String OBJECTS1 = "(C, D, A), (C, E, B), (D, F, A), (E, F, B), (E, G, A), (H, I, B)";
    static final String OBJECTS_EX2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)";
    // static final String OBJECTS_EX2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)";
    private static final String OBJECTS_EX5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)";

    public static void drawTuples() throws ExportException, IOException {
        final Graph<String, DefaultEdge> graph = parseTuples(TUPLES);
        exportDOT(graph, "./fig-tuples.png", false);

    }

    public static void drawObjects() throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(OBJECTS1);
        final Graph<String, ReporterEdge> graph = merge(graphs);

        exportDOT(graph, "./fig-objects.png", true);
    }

    public static void drawEX2() throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(OBJECTS_EX2);
        final Graph<String, ReporterEdge> graph = merge(graphs);

        exportDOT(graph, "./fig-ex2.png", true);
    }

    public static void drawEX5() throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(OBJECTS_EX5);
        final Graph<String, ReporterEdge> graph = merge(graphs);

        exportDOT(graph, "./fig-ex5.png", true);
    }

    public static void main(String[] args) throws ExportException, IOException {
        // drawTuples();
        // drawObjects();
        // drawEX2();
        // drawEX5();
    }
}
