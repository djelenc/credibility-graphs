package credibilitygraphs.parser;// Generated from Graph.g4 by ANTLR 4.6

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GraphParser extends Parser {
    static {
        RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, NODE = 4, WS = 5;
    public static final int
            RULE_stat = 0;
    public static final String[] ruleNames = {
            "stat"
    };

    private static final String[] _LITERAL_NAMES = {
            null, "'('", "','", "')'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, null, null, null, "NODE", "WS"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "Graph.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public GraphParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class StatContext extends ParserRuleContext {
        public List<TerminalNode> NODE() {
            return getTokens(GraphParser.NODE);
        }

        public TerminalNode NODE(int i) {
            return getToken(GraphParser.NODE, i);
        }

        public StatContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_stat;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof GraphVisitor) return ((GraphVisitor<? extends T>) visitor).visitStat(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StatContext stat() throws RecognitionException {
        StatContext _localctx = new StatContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_stat);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2);
                match(T__0);
                setState(3);
                match(NODE);
                setState(4);
                match(T__1);
                setState(5);
                match(NODE);
                setState(6);
                match(T__1);
                setState(7);
                match(NODE);
                setState(8);
                match(T__2);
                setState(19);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__1) {
                    {
                        {
                            setState(9);
                            match(T__1);
                            setState(10);
                            match(T__0);
                            setState(11);
                            match(NODE);
                            setState(12);
                            match(T__1);
                            setState(13);
                            match(NODE);
                            setState(14);
                            match(T__1);
                            setState(15);
                            match(NODE);
                            setState(16);
                            match(T__2);
                        }
                    }
                    setState(21);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static final String _serializedATN =
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\7\31\4\2\t\2\3\2" +
                    "\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\7\2\24\n\2\f" +
                    "\2\16\2\27\13\2\3\2\2\2\3\2\2\2\30\2\4\3\2\2\2\4\5\7\3\2\2\5\6\7\6\2\2" +
                    "\6\7\7\4\2\2\7\b\7\6\2\2\b\t\7\4\2\2\t\n\7\6\2\2\n\25\7\5\2\2\13\f\7\4" +
                    "\2\2\f\r\7\3\2\2\r\16\7\6\2\2\16\17\7\4\2\2\17\20\7\6\2\2\20\21\7\4\2" +
                    "\2\21\22\7\6\2\2\22\24\7\5\2\2\23\13\3\2\2\2\24\27\3\2\2\2\25\23\3\2\2" +
                    "\2\25\26\3\2\2\2\26\3\3\2\2\2\27\25\3\2\2\2\3\25";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}