package com.david;

import org.jgrapht.io.ExportException;

import java.io.IOException;

public final class App {

    static final String OBJECTS_EX2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)";
    static final String OBJECTS_EX5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)";

    public static void drawEX2() throws ExportException, IOException {
        new CredibilityGraph(OBJECTS_EX2).exportDOT("./fig-ex2.png", true);
    }

    public static void drawEX5() throws ExportException, IOException {
        new CredibilityGraph(OBJECTS_EX5).exportDOT("./fig-ex5.png", true);
    }

    public static void drawContraction() throws ExportException, IOException {
        final CredibilityGraph graph = new CredibilityGraph(OBJECTS_EX2);
        graph.reliabilityContraction("A1", "A4");
        graph.exportDOT("./fig-ex2-contracted.png", true);
    }

    public static void drawPrioritizedRevision() throws ExportException, IOException {
        final CredibilityGraph graph = new CredibilityGraph(OBJECTS_EX2);
        graph.prioritizedRevision("A4", "A1", "F3");
        graph.exportDOT("./fig-ex2-revised.png", true);
    }

    public static void main(String[] args) throws ExportException, IOException {
        // drawEX2();
        // drawEX5();
        // drawContraction();
        drawPrioritizedRevision();
    }
}
