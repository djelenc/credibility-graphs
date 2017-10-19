package com.david;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public final class HelloJGraphT {
    public static void main(String[] args) {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        Integer one = 1;
        Integer two = 2;
        Integer three = 3;

        g.addVertex(one);
        g.addVertex(two);
        g.addVertex(three);

        g.addEdge(two, one);
        g.addEdge(two, three);

        System.out.println(g.toString());
    }
}
