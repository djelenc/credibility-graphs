package com.david.parser;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Iterator;

public class Visitor extends GraphBaseVisitor<Graph<String, DefaultEdge>> {
    @Override
    public Graph<String, DefaultEdge> visitStat(GraphParser.StatContext ctx) {
        final Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        final Iterator<TerminalNode> iterator = ctx.NODE().iterator();

        while (iterator.hasNext()) {
            final TerminalNode left = iterator.next();
            final TerminalNode right = iterator.next();

            g.addVertex(left.getText());
            g.addVertex(right.getText());
            g.addEdge(left.getText(), right.getText());
        }

        return g;
    }
}
