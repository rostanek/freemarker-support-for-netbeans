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

    private static final int[] startTags = new int[]{
        ATTEMPT,
        COMPRESS,
        ESCAPE,
        NOESCAPE,
        FOREACH,
        FUNCTION,
        IF,
        LIST,
        MACRO,
        SWITCH,
        
        UNIFIED_CALL
    };

    private static final int[] endTags = new int[]{
        END_ATTEMPT,
        END_COMPRESS,
        END_ESCAPE,
        END_NOESCAPE,
        END_FOREACH,
        END_FUNCTION,
        END_IF,
        END_LIST,
        END_MACRO,
        END_SWITCH,
        
        UNIFIED_CALL_END
    };

    public FTLBracesMatcher() {
        this(null);
    }

    private FTLBracesMatcher(MatcherContext mc) {
        matcherContext = mc;
    }

    private int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        final TokenHierarchy localTokenHierarchy = TokenHierarchy.get(matcherContext.getDocument());
        final List<TokenSequence<? extends TokenId>> localList = getTokenSequences(localTokenHierarchy, matcherContext.getSearchOffset(), FTLTokenId.getLanguage());

        final int[] origin;
        if (!localList.isEmpty()) {
            final TokenSequence<? extends TokenId> ts = localList.get(localList.size() - 1);
            final Token<? extends TokenId> token = ts.offsetToken();
            int tokenId = token.id().ordinal();

            if (indexOf(startTags, tokenId) >= 0 || tokenId == ELSE || tokenId == ELSE_IF) {
                origin = new int[]{ts.offset(), ts.offset() + token.length()};
                originOffset = origin[0];
                forward = true;
            } else if (indexOf(endTags, tokenId) >= 0) {
                origin = new int[]{ts.offset(), ts.offset() + token.text().toString().trim().length()};
                originOffset = origin[0];
                forward = false;
            } else {
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
                    int tokenId = token.id().ordinal();

                    if (indexOf(startTags, tokenId) >= 0) {
                        if (!forward) {
                            if (level == 0) {
                                matches.add(ts.offset());
                                matches.add(ts.offset() + token.length());
                            }

                            level--;
                        } else {
                            level++;
                        }
                    } else if (indexOf(endTags, tokenId) >= 0) {
                        if (forward) {
                            if (level == 0) {
                                matches.add(ts.offset());
                                matches.add(ts.offset() + token.length());
                            }

                            level--;
                        } else {
                            level++;
                        }
                    } else if (tokenId == ELSE || tokenId == ELSE_IF) {
                        if (level == 0) {
                            matches.add(ts.offset());
                            matches.add(ts.offset() + token.length());
                        }
                    }

                }
            }
        }

        System.out.printf("matches %s\n", Arrays.deepToString(matches.toArray()));
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
