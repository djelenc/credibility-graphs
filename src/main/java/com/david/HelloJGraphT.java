package com.david;

import com.david.parser.GraphLexer;
import com.david.parser.GraphParser;
import com.david.parser.Visitor;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.StringComponentNameProvider;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.*;
import java.util.Map;
import java.util.Set;

public final class HelloJGraphT {

    public static <K, V> void cycles(Graph<K, V> g) {
        final CycleDetector<K, V> cycleDetector = new CycleDetector<>(g);

        if (cycleDetector.detectCycles()) {
            System.out.println("Cycles detected.");
            final Set<K> cycles = cycleDetector.findCycles();

            for (K cycle : cycles) {
                System.out.printf("Cycle involving: %s: %s%n", cycle, cycleDetector.findCyclesContainingVertex(cycle));
            }
        } else {
            System.out.println("No cycles. Order:");
            final TopologicalOrderIterator<K, V> iterator = new TopologicalOrderIterator<>(g);
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
        }
    }

    public static void main1(String[] args) throws ExportException, IOException {
        final Graph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("b", "e");
        g.addEdge("e", "a");
        g.addEdge("e", "a");


        // cycles(g);
        final DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
                new StringComponentNameProvider<>(), null, null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.exportGraph(g, baos);
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());

        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File("./graph.png"));
    }

    public static void main2(String[] args) throws ExportException, IOException {
        final Graph<String, DefaultEdge> g = getGraph(
                "(C, D), (C, E), (D, F), (E, F), (E, G), (H, I)").get("");

        // cycles(g);
        final DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
                new StringComponentNameProvider<>(), null, null);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.exportGraph(g, baos);
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());

        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File("./graph.png"));
    }

    public static void main(String[] args) throws ExportException, IOException {
        final Map<String, Graph<String, DefaultEdge>> graphs = getGraph(
                "(C, D,A), (C, E,B), (D, F,A), (E, F,B), (E, G,A), (H, I,B)");

        // cycles(g);

        for (Map.Entry<String, Graph<String, DefaultEdge>> graph : graphs.entrySet()) {
            final DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
                    new StringComponentNameProvider<>(), null, null);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exporter.exportGraph(graph.getValue(), baos);
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());

            final MutableGraph mutableGraph = Parser.read(is);
            mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

            Graphviz.fromGraph(mutableGraph)
                    .render(Format.PNG)
                    .toFile(new File("./node" + graph.getKey() + ".png"));
        }
    }

    public static Map<String, Graph<String, DefaultEdge>> getGraph(String graph) {
        final ANTLRInputStream ais = new ANTLRInputStream(graph);
        final GraphLexer lexer = new GraphLexer(ais);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final GraphParser parser = new GraphParser(tokens);
        final ParseTree tree = parser.stat();
        final Visitor v = new Visitor();
        return v.visit(tree);
    }
}
