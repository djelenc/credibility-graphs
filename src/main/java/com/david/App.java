package com.david;

import guru.nidi.graphviz.engine.Format;
import org.jgrapht.io.ExportException;

import java.io.IOException;

public final class App {

    static final String EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1)," +
            "(A2,A4,B),(A2,A4,F3),(A3,A4,F2)";
    static final String EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G)," +
            "(L,M,E)";
    static final String EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)";

    public static void drawEX2() throws ExportException, IOException {
        new CredibilityGraph(EXAMPLE2).exportDOT("./fig-ex2", Format.PNG);
    }

    public static void drawEX5() throws ExportException, IOException {
        new CredibilityGraph(EXAMPLE5).exportDOT("./fig-ex5", Format.PNG);
    }

    public static void drawEX13() throws ExportException, IOException {
        new CredibilityGraph(EXAMPLE13).exportDOT("./fig-ex13", Format.PNG);
    }

    public static void drawNPRevision13() throws ExportException, IOException {
        final CredibilityGraph graph = new CredibilityGraph(EXAMPLE13);
        graph.nonPrioritizedRevision("L", "H", "G");
        graph.exportDOT("./fig-ex13-npr", Format.SVG);
    }

    public static void drawContraction() throws ExportException, IOException {
        final CredibilityGraph graph = new CredibilityGraph(EXAMPLE2);
        graph.reliabilityContraction("A1", "A4");
        graph.exportDOT("./fig-ex2-contracted", Format.PNG);
    }

    public static void drawPrioritizedRevision() throws ExportException, IOException {
        final CredibilityGraph graph = new CredibilityGraph(EXAMPLE2);
        graph.prioritizedRevision("A4", "A1", "F3");
        graph.exportDOT("./fig-ex2-revised", Format.PNG);
    }

    public static void main(String[] args) throws ExportException, IOException {
        // drawEX2();
        // drawEX5();
        // drawEX13();
        drawNPRevision13();
        // drawContraction();
        // drawPrioritizedRevision();
    }
}
