package com.david.parser;

import com.david.CredibilityObject;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Iterator;

public class Visitor extends GraphBaseVisitor<Graph<String, CredibilityObject>> {
    @Override
    public Graph<String, CredibilityObject> visitStat(GraphParser.StatContext ctx) {
        final Graph<String, CredibilityObject> graph = new DirectedMultigraph<>(CredibilityObject.class);

        final Iterator<TerminalNode> iterator = ctx.NODE().iterator();

        while (iterator.hasNext()) {
            final TerminalNode left = iterator.next();
            final TerminalNode right = iterator.next();
            final TerminalNode reporter = iterator.next();

            graph.addVertex(left.getText());
            graph.addVertex(right.getText());
            graph.addEdge(
                    left.getText(),
                    right.getText(),
                    new CredibilityObject(left.getText(), right.getText(), reporter.getText()));
        }

        return graph;
    }
}
