package com.david;

import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.*;
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

    public static void main(String[] args) throws ExportException, IOException {
        final Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);

        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(2, 5);
        g.addEdge(5, 1);

        // cycles(g);
        final DOTExporter<Integer, DefaultEdge> exporter = new DOTExporter<>();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.exportGraph(g, baos);
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());

        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File("./graph.png"));
    }
}
