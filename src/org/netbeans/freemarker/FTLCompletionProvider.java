package org.netbeans.freemarker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 *
 * @author Rafał Ostanek
 */
@MimeRegistration(mimeType = "text/x-ftl", service = CompletionProvider.class)
public class FTLCompletionProvider implements CompletionProvider {

    private String[] directives = "assign attempt break case compress default else elseif escape fallback function flush ftl global if import include list local lt macro nested noescape noparse nt recover recurse return rt setting stop switch t visit ".split(" ");

    @Override
    public CompletionTask createTask(int queryType, JTextComponent jtc) {
        if (queryType != COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
                
                String filter = null;
                int startOffset = caretOffset - 1;
                String text = "";
                try {
                    final StyledDocument bDoc = (StyledDocument) document;
                    text = bDoc.getText(0, bDoc.getLength());
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOffset = indexOfWhite(line);
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                if (filter != null) {
                    if (filter.startsWith("<#")) {
                        filter = filter.substring(2);
                        for (String keyword : directives) {
                            if (keyword.startsWith(filter)) {
                                completionResultSet.addItem(new FTLCompletionItem(keyword, startOffset + 2, caretOffset));
                            }
                        }
                    } else if (filter.startsWith("<@")) {
                        filter = filter.substring(2);
                        Pattern pattern = Pattern.compile("<#assign\\s+(\\w+)");
                        Matcher matcher = pattern.matcher(text);
                        while (matcher.find()) {
                            String group1 = matcher.group(1);
                            if (group1.startsWith(filter)) {
                                completionResultSet.addItem(new FTLCompletionItem(group1, startOffset + 2, caretOffset));
                            }
                        }
                    }
                }
                completionResultSet.finish();
            }
        }, jtc);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        return 0;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start
                ).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
    


}
