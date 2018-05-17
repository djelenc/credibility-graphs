package credibilitygraphs.parser;

import credibilitygraphs.core.CredibilityObject;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.function.Function;

public class Visitor<N, L, CO extends CredibilityObject<N, L>> extends GraphBaseVisitor<List<CO>> {

    private final Function<String, N> nodeMaker;
    private final Function<String, L> labelMaker;
    private final TriFunction<N, N, L, CO> coMaker;

    public Visitor(Function<String, N> nodeMaker, Function<String, L> labelMaker, TriFunction<N, N, L, CO> coMaker) {
        this.nodeMaker = nodeMaker;
        this.labelMaker = labelMaker;
        this.coMaker = coMaker;
    }

    @Override
    public List<CO> visitStat(GraphParser.StatContext ctx) {
        final List<CO> list = new ArrayList<>();

        final Iterator<TerminalNode> iterator = ctx.NODE().iterator();

        while (iterator.hasNext()) {
            final TerminalNode left = iterator.next();
            final TerminalNode right = iterator.next();
            final TerminalNode reporter = iterator.next();

            final CO co = coMaker.apply(
                    nodeMaker.apply(left.getText()),
                    nodeMaker.apply(right.getText()),
                    labelMaker.apply(reporter.getText()));
            list.add(co);
        }

        return list;
    }
}
