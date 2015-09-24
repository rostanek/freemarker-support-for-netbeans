package org.netbeans.freemarker.braces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.freemarker.lexer.FTLTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

import static freemarker.core.FMParserConstants.*;

/**
 * Matcher for block tags only. Tags like assign are commented out, because
 * their empty-tag syntax cause problems when nested in macros or functions.
 *
 * @author Rafał Ostanek
 */
@MimeRegistration(mimeType = "text/x-ftl", service = BracesMatcherFactory.class)
public class FTLBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private final MatcherContext matcherContext;
    private int originOffset;
    private boolean forward;

//    private static final int[] tokenPairs = new int[]{ATTEMPT, END_ATTEMPT,
//        COMPRESS, END_COMPRESS, ESCAPE, END_ESCAPE, NOESCAPE, END_NOESCAPE,
//        FOREACH, END_FOREACH, FUNCTION, END_FUNCTION, IF, END_IF, LIST, END_LIST,
//        MACRO, END_MACRO, SWITCH, END_SWITCH
//    };

    public FTLBracesMatcher() {
        this(null);
    }

    private FTLBracesMatcher(MatcherContext mc) {
        matcherContext = mc;
    }

//    private int findPair(int token) {
//        
//        for (int i = 0; i < tokenPairs.length; i++) {
//            if (tokenPairs[i] == token) {
//                if ((i & 1) == 1) { // odd ?
//                    return i - 1;
//                } else {
//                    return i + 1;
//                }
//            }
//        }
//        return 0;
//    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        final TokenHierarchy localTokenHierarchy = TokenHierarchy.get(matcherContext.getDocument());
        final List<TokenSequence<? extends TokenId>> localList = getTokenSequences(localTokenHierarchy, matcherContext.getSearchOffset(), FTLTokenId.getLanguage());

        final int[] origin;
        if (!localList.isEmpty()) {
            final TokenSequence<? extends TokenId> ts = localList.get(localList.size() - 1);
            final Token<? extends TokenId> token = ts.offsetToken();

            switch (token.id().ordinal()) {
                case ELSE_IF:
                case ELSE:
//                case ASSIGN:
//                case GLOBALASSIGN:
//                case LOCALASSIGN:
                case ATTEMPT:
                case COMPRESS:
                case ESCAPE:
                case NOESCAPE:
                case FOREACH:
                case FUNCTION:
                case IF:
                case LIST:
                case MACRO:
                case SWITCH:
                    origin = new int[]{ts.offset(), ts.offset() + token.length()};
                    originOffset = origin[0];
                    forward = true;
                    break;

//                case END_ASSIGN:
//                case END_LOCAL:
//                case END_GLOBAL:
                case END_ATTEMPT:
                case END_COMPRESS:
                case END_ESCAPE:
                case END_NOESCAPE:
                case END_FOREACH:
                case END_FUNCTION:
                case END_IF:
                case END_LIST:
                case END_MACRO:
                case END_SWITCH:
                    origin = new int[]{ts.offset(), ts.offset() + token.text().toString().trim().length()};
                    originOffset = origin[0];
                    forward = false;
                    break;

                default:
                    origin = null;
            }
        } else {
            origin = null;
        }

        
        //System.out.printf("origin: %s\n", Arrays.toString(origin));
            
        
        return origin;

    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        final TokenHierarchy localTokenHierarchy = TokenHierarchy.get(matcherContext.getDocument());
        final List<TokenSequence<? extends TokenId>> localList = getTokenSequences(localTokenHierarchy, originOffset, FTLTokenId.getLanguage());

        //int[] matches = null;
        List<Integer> matches = new ArrayList<Integer>();

        if (!localList.isEmpty() && !MatcherContext.isTaskCanceled()) {
            final TokenSequence<? extends TokenId> ts = localList.get(localList.size() - 1);

            ts.move(originOffset);

            final boolean hasNext;
//            boolean sawEquals = false; // flag for non-block assign
//            boolean sawDirEnd = false; // flag for non-block assign
            if (forward) {
                hasNext = ts.moveNext();
            } else {
                hasNext = true;
            }

            if (hasNext && !MatcherContext.isTaskCanceled()) {
                int level = 0;
                theLoop:
                while ((level >= 0) && (forward ? ts.moveNext() : ts.movePrevious()) && !MatcherContext.isTaskCanceled()) {
                    final Token<? extends TokenId> token = ts.offsetToken();

                    switch (token.id().ordinal()) {
                        case ELSE_IF:
                        case ELSE:
                            if (level == 0) {
                                matches.add(ts.offset());
                                matches.add(ts.offset() + token.length());
                            }
                            break;

                        //case ASSIGN:
//                        case GLOBALASSIGN:
//                        case LOCALASSIGN:
                        case ATTEMPT:
                        case COMPRESS:
                        case ESCAPE:
                        case NOESCAPE:
                        case FOREACH:
                        case FUNCTION:
                        case IF:
                        case LIST:
                        case MACRO:
                        case SWITCH:
                            if (!forward) {
                                if (level == 0) {
                                    matches.add(ts.offset());
                                    matches.add(ts.offset() + token.length());
                                }

                                level--;
                            } else {
                                level++;
                            }

                            break;


//                        case EQUALS:
//                            if (!backward && !sawDirEnd) {
//                                sawEquals = true;
//                            }
//                            break;
//                        case DIRECTIVE_END:
//                        case EMPTY_DIRECTIVE_END:
//                            sawDirEnd = true;
//                            if (!backward && level == 0 && sawEquals) {
//                                matches.add(ts.offset());
//                                matches.add(ts.offset() + token.length());
//                                break theLoop;
//                            }
//                            break;
//                        case END_ASSIGN:
//                        case END_GLOBAL:
//                        case END_LOCAL:
                        case END_ATTEMPT:
                        case END_COMPRESS:
                        case END_ESCAPE:
                        case END_NOESCAPE:
                        case END_FOREACH:
                        case END_FUNCTION:
                        case END_IF:
                        case END_LIST:
                        case END_MACRO:
                        case END_SWITCH:
                            if (forward) {
                                if (level == 0) {
                                    matches.add(ts.offset());
                                    matches.add(ts.offset() + token.length());
                                }

                                level--;
                            } else {
                                level++;
                            }

                            break;

                        default:
                    }
                }
            }
        }

        //System.out.printf("matches %s\n", Arrays.deepToString(matches.toArray()));
        

        int[] m = null;
        if (matches.size() > 0) {
            m = new int[matches.size()];
            for (int i = 0; i < m.length; i++) {
                m[i] = matches.get(i);
            }
        }
        return m;

    }

    @Override
    public BracesMatcher createMatcher(MatcherContext mc) {
        return new FTLBracesMatcher(mc);
    }

    private static List<TokenSequence<? extends TokenId>> getTokenSequences(final TokenHierarchy<?> tokenHierarchy, final int iOriginOffset, final Language<? extends TokenId> language) {
        final List<TokenSequence<?>> localList = tokenHierarchy.embeddedTokenSequences(iOriginOffset, false);

        for (int i = localList.size() - 1; i >= 0; --i) {
            final TokenSequence<?> localTokenSequence = localList.get(i);
            if (localTokenSequence.language().mimeType().equals(language.mimeType())) {
                break;
            }

            localList.remove(i);
        }

        return new ArrayList<TokenSequence<? extends TokenId>>(localList);
    }

}
