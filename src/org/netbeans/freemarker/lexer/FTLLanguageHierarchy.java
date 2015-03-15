package org.netbeans.freemarker.lexer;

import freemarker.core.FMParserConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Rafał Ostanek
 */
public class FTLLanguageHierarchy extends LanguageHierarchy<FTLTokenId> {

    private static List<FTLTokenId> tokens;
    private static Map<Integer, FTLTokenId> idToToken;

    private static void init() {
        tokens = Arrays.<FTLTokenId>asList(new FTLTokenId[]{
                    new FTLTokenId("EOF", "keyword", FMParserConstants.EOF),
                    new FTLTokenId("BLANK", "keyword", FMParserConstants.BLANK),
                    new FTLTokenId("START_TAG", "keyword", FMParserConstants.START_TAG),
                    new FTLTokenId("END_TAG", "keyword", FMParserConstants.END_TAG),
                    new FTLTokenId("CLOSE_TAG1", "keyword", FMParserConstants.CLOSE_TAG1),
                    new FTLTokenId("CLOSE_TAG2", "keyword", FMParserConstants.CLOSE_TAG2),
                    new FTLTokenId("ATTEMPT", "directive", FMParserConstants.ATTEMPT),
                    new FTLTokenId("RECOVER", "directive", FMParserConstants.RECOVER),
                    new FTLTokenId("IF", "directive", FMParserConstants.IF),
                    new FTLTokenId("ELSE_IF", "directive", FMParserConstants.ELSE_IF),
                    new FTLTokenId("LIST", "directive", FMParserConstants.LIST),
                    new FTLTokenId("FOREACH", "directive", FMParserConstants.FOREACH), // deprecated
                    new FTLTokenId("SWITCH", "directive", FMParserConstants.SWITCH),
                    new FTLTokenId("CASE", "directive", FMParserConstants.CASE),
                    new FTLTokenId("ASSIGN", "directive", FMParserConstants.ASSIGN),
                    new FTLTokenId("GLOBALASSIGN", "directive", FMParserConstants.GLOBALASSIGN),
                    new FTLTokenId("LOCALASSIGN", "directive", FMParserConstants.LOCALASSIGN),
                    new FTLTokenId("INCLUDE","directive",FMParserConstants._INCLUDE),
                    new FTLTokenId("IMPORT", "directive", FMParserConstants.IMPORT),
                    new FTLTokenId("FUNCTION", "directive", FMParserConstants.FUNCTION),
                    new FTLTokenId("MACRO", "directive", FMParserConstants.MACRO),
                    new FTLTokenId("TRANSFORM", "directive", FMParserConstants.TRANSFORM), // deprecated
                    new FTLTokenId("VISIT", "directive", FMParserConstants.VISIT),
                    new FTLTokenId("STOP", "directive", FMParserConstants.STOP),
                    new FTLTokenId("RETURN", "directive", FMParserConstants.RETURN),
                    new FTLTokenId("CALL", "directive", FMParserConstants.CALL), // deprecated
                    new FTLTokenId("SETTING", "directive", FMParserConstants.SETTING),
                    new FTLTokenId("COMPRESS", "directive", FMParserConstants.COMPRESS),
                    new FTLTokenId("COMMENT", "directive", FMParserConstants.COMMENT), // deprecated
                    new FTLTokenId("TERSE_COMMENT", "comment", FMParserConstants.TERSE_COMMENT),
                    new FTLTokenId("NOPARSE", "directive", FMParserConstants.NOPARSE),
                    new FTLTokenId("END_IF", "directive", FMParserConstants.END_IF),
                    new FTLTokenId("END_LIST", "directive", FMParserConstants.END_LIST),
                    new FTLTokenId("END_RECOVER", "directive", FMParserConstants.END_RECOVER),
                    new FTLTokenId("END_ATTEMPT", "directive", FMParserConstants.END_ATTEMPT),
                    new FTLTokenId("END_FOREACH", "directive", FMParserConstants.END_FOREACH), // deprecated
                    new FTLTokenId("END_LOCAL", "directive", FMParserConstants.END_LOCAL),
                    new FTLTokenId("END_GLOBAL", "directive", FMParserConstants.END_GLOBAL),
                    new FTLTokenId("END_ASSIGN", "directive", FMParserConstants.END_ASSIGN),
                    new FTLTokenId("END_FUNCTION", "directive", FMParserConstants.END_FUNCTION),
                    new FTLTokenId("END_MACRO", "directive", FMParserConstants.END_MACRO),
                    new FTLTokenId("END_COMPRESS", "directive", FMParserConstants.END_COMPRESS),
                    new FTLTokenId("END_TRANSFORM", "directive", FMParserConstants.END_TRANSFORM), // deprecated
                    new FTLTokenId("END_SWITCH", "directive", FMParserConstants.END_SWITCH),
                    new FTLTokenId("ELSE", "directive", FMParserConstants.ELSE),
                    new FTLTokenId("BREAK", "directive", FMParserConstants.BREAK),
                    new FTLTokenId("SIMPLE_RETURN", "directive", FMParserConstants.SIMPLE_RETURN),
                    new FTLTokenId("HALT", "keyword", FMParserConstants.HALT),
                    new FTLTokenId("FLUSH", "directive", FMParserConstants.FLUSH),
                    new FTLTokenId("TRIM", "directive", FMParserConstants.TRIM),
                    new FTLTokenId("LTRIM", "directive", FMParserConstants.LTRIM),
                    new FTLTokenId("RTRIM", "directive", FMParserConstants.RTRIM),
                    new FTLTokenId("NOTRIM", "directive", FMParserConstants.NOTRIM),
                    new FTLTokenId("DEFAUL", "directive", FMParserConstants.DEFAUL),
                    new FTLTokenId("SIMPLE_NESTED", "directive", FMParserConstants.SIMPLE_NESTED),
                    new FTLTokenId("NESTED", "directive", FMParserConstants.NESTED),
                    new FTLTokenId("SIMPLE_RECURSE", "directive", FMParserConstants.SIMPLE_RECURSE),
                    new FTLTokenId("RECURSE", "directive", FMParserConstants.RECURSE),
                    new FTLTokenId("FALLBACK", "directive", FMParserConstants.FALLBACK),
                    new FTLTokenId("ESCAPE", "directive", FMParserConstants.ESCAPE),
                    new FTLTokenId("END_ESCAPE", "directive", FMParserConstants.END_ESCAPE),
                    new FTLTokenId("NOESCAPE", "directive", FMParserConstants.NOESCAPE),
                    new FTLTokenId("END_NOESCAPE", "directive", FMParserConstants.END_NOESCAPE),
                    new FTLTokenId("UNIFIED_CALL", "directive", FMParserConstants.UNIFIED_CALL),
                    new FTLTokenId("UNIFIED_CALL_END", "directive", FMParserConstants.UNIFIED_CALL_END),
                    new FTLTokenId("FTL_HEADER", "directive", FMParserConstants.FTL_HEADER),
                    new FTLTokenId("TRIVIAL_FTL_HEADER", "directive", FMParserConstants.TRIVIAL_FTL_HEADER),
                    new FTLTokenId("UNKNOWN_DIRECTIVE", "errors", FMParserConstants.UNKNOWN_DIRECTIVE),
                    new FTLTokenId("STATIC_TEXT_WS", "default", FMParserConstants.STATIC_TEXT_WS), //new FTLTokenId("WHITESPACE", "keyword", FMParserConstants.WHITESPACE),
                    new FTLTokenId("STATIC_TEXT_NON_WS", "default", FMParserConstants.STATIC_TEXT_NON_WS),//new FTLTokenId("PRINTABLE_CHARS", "braces", FMParserConstants.PRINTABLE_CHARS),
                    new FTLTokenId("STATIC_TEXT_FALSE_ALARM", "default", FMParserConstants.STATIC_TEXT_FALSE_ALARM),//new FTLTokenId("FALSE_ALERT", "braces", FMParserConstants.FALSE_ALERT),
                    new FTLTokenId("DOLLAR_INTERPOLATION_OPENING", "interpolation", FMParserConstants.DOLLAR_INTERPOLATION_OPENING),//new FTLTokenId("OUTPUT_ESCAPE", "braces", FMParserConstants.OUTPUT_ESCAPE),
                    new FTLTokenId("HASH_INTERPOLATION_OPENING", "interpolation", FMParserConstants.HASH_INTERPOLATION_OPENING),
//                    new FTLTokenId("NUMERICAL_ESCAPE", "braces", FMParserConstants.NUMERICAL_ESCAPE),
                    new FTLTokenId("WHITESPACE", "whitespace", FMParserConstants.WHITESPACE),
//                    new FTLTokenId("A_74", "braces", FMParserConstants.A_74),
//                    new FTLTokenId("A_75", "braces", FMParserConstants.A_75),
//                    new FTLTokenId("A_76", "braces", FMParserConstants.A_76),
//                    new FTLTokenId("A_77", "braces", FMParserConstants.A_77),
//                    new FTLTokenId("A_78", "braces", FMParserConstants.A_78),
//                    new FTLTokenId("A_79", "braces", FMParserConstants.A_79),
                    new FTLTokenId("ESCAPED_CHAR", "keyword", FMParserConstants.ESCAPED_CHAR),
                    new FTLTokenId("STRING_LITERAL", "string", FMParserConstants.STRING_LITERAL),
                    new FTLTokenId("UNCLOSED_STRING_LITERAL", "errors", FMParserConstants.UNCLOSED_STRING_LITERAL),
                    new FTLTokenId("RAW_STRING", "literal", FMParserConstants.RAW_STRING),
                    new FTLTokenId("FALSE", "keyword", FMParserConstants.FALSE),
                    new FTLTokenId("TRUE", "keyword", FMParserConstants.TRUE),
                    new FTLTokenId("INTEGER", "number", FMParserConstants.INTEGER),
                    new FTLTokenId("DECIMAL", "number", FMParserConstants.DECIMAL),
                    new FTLTokenId("DOT", "operator", FMParserConstants.DOT),
                    new FTLTokenId("DOT_DOT", "operator", FMParserConstants.DOT_DOT),
                    new FTLTokenId("DOT_DOT_LESS", "operator", FMParserConstants.DOT_DOT_LESS),
                    new FTLTokenId("DOT_DOT_ASTERISK", "operator", FMParserConstants.DOT_DOT_ASTERISK),
                    new FTLTokenId("BUILT_IN", "operator", FMParserConstants.BUILT_IN),
                    new FTLTokenId("EXISTS", "operator", FMParserConstants.EXISTS),
                    new FTLTokenId("EQUALS", "operator", FMParserConstants.EQUALS),
                    new FTLTokenId("DOUBLE_EQUALS", "operator", FMParserConstants.DOUBLE_EQUALS),
                    new FTLTokenId("NOT_EQUALS", "operator", FMParserConstants.NOT_EQUALS),
                    new FTLTokenId("LESS_THAN", "operator", FMParserConstants.LESS_THAN),
                    new FTLTokenId("LESS_THAN_EQUALS", "operator", FMParserConstants.LESS_THAN_EQUALS),
                    new FTLTokenId("ESCAPED_GT", "operator", FMParserConstants.ESCAPED_GT),
                    new FTLTokenId("ESCAPED_GTE", "operator", FMParserConstants.ESCAPED_GTE),
                    new FTLTokenId("PLUS", "operator", FMParserConstants.PLUS),
                    new FTLTokenId("MINUS", "operator", FMParserConstants.MINUS),
                    new FTLTokenId("TIMES", "operator", FMParserConstants.TIMES),
                    new FTLTokenId("DOUBLE_STAR", "operator", FMParserConstants.DOUBLE_STAR),
                    new FTLTokenId("ELLIPSIS", "operator", FMParserConstants.ELLIPSIS),
                    new FTLTokenId("DIVIDE", "operator", FMParserConstants.DIVIDE),
                    new FTLTokenId("PERCENT", "operator", FMParserConstants.PERCENT),
                    new FTLTokenId("AND", "operator", FMParserConstants.AND),
                    new FTLTokenId("OR", "operator", FMParserConstants.OR),
                    new FTLTokenId("EXCLAM", "operator", FMParserConstants.EXCLAM),
                    new FTLTokenId("COMMA", "separator", FMParserConstants.COMMA),
                    new FTLTokenId("SEMICOLON", "separator", FMParserConstants.SEMICOLON),
                    new FTLTokenId("COLON", "separator", FMParserConstants.COLON),
                    new FTLTokenId("OPEN_BRACKET", "braces", FMParserConstants.OPEN_BRACKET),
                    new FTLTokenId("CLOSE_BRACKET", "braces", FMParserConstants.CLOSE_BRACKET),
                    new FTLTokenId("OPEN_PAREN", "braces", FMParserConstants.OPEN_PAREN),
                    new FTLTokenId("CLOSE_PAREN", "braces", FMParserConstants.CLOSE_PAREN),
                    new FTLTokenId("OPENING_CURLY_BRACKET", "braces", FMParserConstants.OPENING_CURLY_BRACKET),
                    new FTLTokenId("CLOSING_CURLY_BRACKET", "braces", FMParserConstants.CLOSING_CURLY_BRACKET),
                    new FTLTokenId("IN", "keyword", FMParserConstants.IN),
                    new FTLTokenId("AS", "keyword", FMParserConstants.AS),
                    new FTLTokenId("USING", "keyword", FMParserConstants.USING),
                    new FTLTokenId("ID", "identifier", FMParserConstants.ID),
                    new FTLTokenId("OPEN_MISPLACED_INTERPOLATION", "interpolation", FMParserConstants.OPEN_MISPLACED_INTERPOLATION),
                    new FTLTokenId("NON_ESCAPED_ID_START_CHAR", "identifier", FMParserConstants.NON_ESCAPED_ID_START_CHAR),
                    new FTLTokenId("ESCAPED_ID_CHAR", "identifier", FMParserConstants.ESCAPED_ID_CHAR),
                    new FTLTokenId("ID_START_CHAR", "identifier", FMParserConstants.ID_START_CHAR),
                    new FTLTokenId("ASCII_DIGIT", "keyword", FMParserConstants.ASCII_DIGIT),
                    new FTLTokenId("DIRECTIVE_END", "directive", FMParserConstants.DIRECTIVE_END),
                    new FTLTokenId("EMPTY_DIRECTIVE_END", "directive", FMParserConstants.EMPTY_DIRECTIVE_END),
                    new FTLTokenId("NATURAL_GT", "braces", FMParserConstants.NATURAL_GT),
                    new FTLTokenId("NATURAL_GTE", "braces", FMParserConstants.NATURAL_GTE),
                    new FTLTokenId("TERMINATING_WHITESPACE", "keyword", FMParserConstants.TERMINATING_WHITESPACE),
                    new FTLTokenId("TERMINATING_EXCLAM", "keyword", FMParserConstants.TERMINATING_EXCLAM),
                    new FTLTokenId("TERSE_COMMENT_END", "comment", FMParserConstants.TERSE_COMMENT_END),
                    new FTLTokenId("MAYBE_END", "comment", FMParserConstants.MAYBE_END),
                    new FTLTokenId("KEEP_GOING", "comment", FMParserConstants.KEEP_GOING),
                    new FTLTokenId("LONE_LESS_THAN_OR_DASH", "keyword", FMParserConstants.LONE_LESS_THAN_OR_DASH),
                    
                    
                    
                    
        });
        idToToken = new HashMap<Integer, FTLTokenId>();
        for (FTLTokenId token : tokens) {
            idToToken.put(token.ordinal(), token);
        }
    }

    static synchronized FTLTokenId getToken(int id) {
        if (idToToken == null) {
            init();
        }
        return idToToken.get(id);
    }

    @Override
    protected synchronized Collection<FTLTokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        return tokens;
    }

    @Override
    protected synchronized Lexer<FTLTokenId> createLexer(LexerRestartInfo<FTLTokenId> info) {
        return new FTLLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-ftl";
    }

}
