package com.david.parser;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Visitor extends GraphBaseVisitor<Map<String, Graph<String, DefaultEdge>>> {
    @Override
    public Map<String, Graph<String, DefaultEdge>> visitStat(GraphParser.StatContext ctx) {
        final Map<String, Graph<String, DefaultEdge>> graphs = new HashMap<>();

        if (ctx.tuples() != null) {
            // parsing credibility tuples, like (A, B)
            final Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
            graphs.put("", g);

            final Iterator<TerminalNode> iterator = ctx.tuples().NODE().iterator();

            while (iterator.hasNext()) {
                final TerminalNode left = iterator.next();
                final TerminalNode right = iterator.next();

                g.addVertex(left.getText());
                g.addVertex(right.getText());
                g.addEdge(left.getText(), right.getText());
            }
        } else {
            if (ctx.objects() == null) {
                throw new IllegalArgumentException("An invalid graph string.");
            }

            // parsing credibility objects, like (A, B, C)
            final Iterator<TerminalNode> iterator = ctx.objects().NODE().iterator();

            while (iterator.hasNext()) {
                final TerminalNode left = iterator.next();
                final TerminalNode right = iterator.next();
                final TerminalNode reporter = iterator.next();

                Graph<String, DefaultEdge> g = graphs.get(reporter.getText());

                if (g == null) {
                    g = new DefaultDirectedGraph<>(DefaultEdge.class);
                    graphs.put(reporter.getText(), g);
                }

                g.addVertex(left.getText());
                g.addVertex(right.getText());
                g.addEdge(left.getText(), right.getText());
            }
        }

        return graphs;
    }
}
