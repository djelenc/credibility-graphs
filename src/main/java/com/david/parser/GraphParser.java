package com.david.parser;// Generated from Graph.g4 by ANTLR 4.6

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
            RULE_stat = 0, RULE_tuples = 1, RULE_objects = 2;
    public static final String[] ruleNames = {
            "stat", "tuples", "objects"
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
        public TuplesContext tuples() {
            return getRuleContext(TuplesContext.class, 0);
        }

        public ObjectsContext objects() {
            return getRuleContext(ObjectsContext.class, 0);
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
        try {
            setState(8);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(6);
                    tuples();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(7);
                    objects();
                }
                break;
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

    public static class TuplesContext extends ParserRuleContext {
        public List<TerminalNode> NODE() {
            return getTokens(GraphParser.NODE);
        }

        public TerminalNode NODE(int i) {
            return getToken(GraphParser.NODE, i);
        }

        public TuplesContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_tuples;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof GraphVisitor) return ((GraphVisitor<? extends T>) visitor).visitTuples(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TuplesContext tuples() throws RecognitionException {
        TuplesContext _localctx = new TuplesContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_tuples);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(10);
                match(T__0);
                setState(11);
                match(NODE);
                setState(12);
                match(T__1);
                setState(13);
                match(NODE);
                setState(14);
                match(T__2);
                setState(23);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__1) {
                    {
                        {
                            setState(15);
                            match(T__1);
                            setState(16);
                            match(T__0);
                            setState(17);
                            match(NODE);
                            setState(18);
                            match(T__1);
                            setState(19);
                            match(NODE);
                            setState(20);
                            match(T__2);
                        }
                    }
                    setState(25);
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

    public static class ObjectsContext extends ParserRuleContext {
        public List<TerminalNode> NODE() {
            return getTokens(GraphParser.NODE);
        }

        public TerminalNode NODE(int i) {
            return getToken(GraphParser.NODE, i);
        }

        public ObjectsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_objects;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof GraphVisitor) return ((GraphVisitor<? extends T>) visitor).visitObjects(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ObjectsContext objects() throws RecognitionException {
        ObjectsContext _localctx = new ObjectsContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_objects);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(26);
                match(T__0);
                setState(27);
                match(NODE);
                setState(28);
                match(T__1);
                setState(29);
                match(NODE);
                setState(30);
                match(T__1);
                setState(31);
                match(NODE);
                setState(32);
                match(T__2);
                setState(43);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__1) {
                    {
                        {
                            setState(33);
                            match(T__1);
                            setState(34);
                            match(T__0);
                            setState(35);
                            match(NODE);
                            setState(36);
                            match(T__1);
                            setState(37);
                            match(NODE);
                            setState(38);
                            match(T__1);
                            setState(39);
                            match(NODE);
                            setState(40);
                            match(T__2);
                        }
                    }
                    setState(45);
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
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\7\61\4\2\t\2\4\3" +
                    "\t\3\4\4\t\4\3\2\3\2\5\2\13\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3" +
                    "\3\3\3\7\3\30\n\3\f\3\16\3\33\13\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4" +
                    "\3\4\3\4\3\4\3\4\3\4\3\4\7\4,\n\4\f\4\16\4/\13\4\3\4\2\2\5\2\4\6\2\2\60" +
                    "\2\n\3\2\2\2\4\f\3\2\2\2\6\34\3\2\2\2\b\13\5\4\3\2\t\13\5\6\4\2\n\b\3" +
                    "\2\2\2\n\t\3\2\2\2\13\3\3\2\2\2\f\r\7\3\2\2\r\16\7\6\2\2\16\17\7\4\2\2" +
                    "\17\20\7\6\2\2\20\31\7\5\2\2\21\22\7\4\2\2\22\23\7\3\2\2\23\24\7\6\2\2" +
                    "\24\25\7\4\2\2\25\26\7\6\2\2\26\30\7\5\2\2\27\21\3\2\2\2\30\33\3\2\2\2" +
                    "\31\27\3\2\2\2\31\32\3\2\2\2\32\5\3\2\2\2\33\31\3\2\2\2\34\35\7\3\2\2" +
                    "\35\36\7\6\2\2\36\37\7\4\2\2\37 \7\6\2\2 !\7\4\2\2!\"\7\6\2\2\"-\7\5\2" +
                    "\2#$\7\4\2\2$%\7\3\2\2%&\7\6\2\2&\'\7\4\2\2\'(\7\6\2\2()\7\4\2\2)*\7\6" +
                    "\2\2*,\7\5\2\2+#\3\2\2\2,/\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\7\3\2\2\2/-\3" +
                    "\2\2\2\5\n\31-";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}