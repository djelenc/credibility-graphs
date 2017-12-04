package com.david.parser;// Generated from Graph.g4 by ANTLR 4.6

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link GraphParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface GraphVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link GraphParser#stat}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitStat(GraphParser.StatContext ctx);
}