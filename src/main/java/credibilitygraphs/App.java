package credibilitygraphs;

import credibilitygraphs.core.CredibilityGraph;
import guru.nidi.graphviz.engine.Format;

import java.io.IOException;

public final class App {

    public static final String EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1)," +
            "(A2,A4,B),(A2,A4,F3),(A3,A4,F2)";
    public static final String EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G)," +
            "(L,M,E)";
    public static final String EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)";

    public static void basic() throws IOException {
        final CredibilityGraph graph = new CredibilityGraph(EXAMPLE2);
        graph.exportDOT("./fig-1", Format.PNG, true);

        graph.expansion("C", "F1", "B");
        graph.exportDOT("./fig-2-expansion", Format.PNG, true);

        graph.contraction("C", "F1");
        graph.exportDOT("./fig-3-contraction", Format.PNG, true);

        graph.nonPrioritizedRevision("A4", "A1", "B");
        graph.exportDOT("./fig-4-npr-revision", Format.PNG, true);

        graph.prioritizedRevision("A4", "A1", "B");
        graph.exportDOT("./fig-5-pr-revision", Format.PNG, true);

        final CredibilityGraph graph2 = new CredibilityGraph(EXAMPLE13);
        graph2.exportDOT("./fig-6", Format.PNG, true);
        graph2.exportGraphML("./fig-6");

        graph2.nonPrioritizedRevision("L", "H", "G");
        graph2.exportDOT("./fig-7-npr-revision", Format.PNG, true);
    }

    public static void main(String[] args) throws IOException {
        // experiences();
        // opinions();
        // informants();

        final CredibilityGraph graph = new CredibilityGraph(
                "(A, E, B), (D, B, A), (C, A, D), (B, A, E)");
        graph.exportDOT("./g1", Format.PNG, true);

        final CredibilityGraph newGraph = new CredibilityGraph(
                "(A, C, E), (C, D, B)");
        newGraph.exportDOT("./g2", Format.PNG, true);

        graph.merge(newGraph);
        graph.exportDOT("./merged", Format.PNG, true);
    }

    private static void informants() throws IOException {
        final CredibilityGraph inf = new CredibilityGraph(
                "(B, D, inf), (A, D, inf)");
        inf.graph.addVertex("C");
        inf.graph.addVertex("E");
        inf.graph.addVertex("F");
        inf.graph.addVertex("G");
        inf.exportDOT("./inf", Format.SVG, true);
    }

    private static void opinions() throws IOException {
        final CredibilityGraph op1 = new CredibilityGraph(
                "(A, C, D), (C, B, D)");
        op1.exportDOT("./ops1", Format.SVG, true);

        final CredibilityGraph op2 = new CredibilityGraph(
                "(E, F, B), (F, G, B), (A, F, B)");
        op2.exportDOT("./ops2", Format.SVG, true);
    }

    private static void experiences() throws IOException {
        final CredibilityGraph graph = new CredibilityGraph(
                "(A, B, exp), (B, C, exp)");
        graph.exportDOT("./exp", Format.SVG, true);
    }
}
