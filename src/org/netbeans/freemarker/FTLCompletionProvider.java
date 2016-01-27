package org.netbeans.freemarker;

import freemarker.core.FMParserConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 *
 * @author rostanek
 */
@MimeRegistration(mimeType = "text/x-ftl", service = CompletionProvider.class)
public class FTLCompletionProvider implements CompletionProvider {

    private final String[] ftlParameters = "encoding strip_whitespace strip_text strict_syntax ns_prefixes attributes".split(" ");
    private final String[] settingNames = "locale number_format boolean_format date_format time_format datetime_format time_zone sql_date_and_time_time_zone url_escaping_charset output_encoding classic_compatible".split(" ");

    private final freemarker.template.Configuration configuration;
    private final Set<String> directives;
    private final Set<String> builtins;

    public FTLCompletionProvider() {
        configuration = new freemarker.template.Configuration();
        directives = configuration.getSupportedBuiltInDirectiveNames();
        builtins = configuration.getSupportedBuiltInNames();
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent jtc) {
        if (queryType != COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
                
                int lineStartOffset;
                String filter;
                int startOffset = caretOffset - 1; // poczatek uzupelnianego slowa
                String currentLine;
                String text;
                Set<String> idents = new HashSet<String>();
                try {
                    ((AbstractDocument) document).readLock();
                    
                    TokenHierarchy th = TokenHierarchy.get(document);
                    TokenSequence ts = th.tokenSequence();
                    while (ts.moveNext()) {
                        Token token = ts.token();
                        if (token.id().ordinal() == FMParserConstants.ID) {
                            idents.add(token.text().toString());
                        }
                    }
                    
                    final StyledDocument bDoc = (StyledDocument) document;                    
                    
                    text = bDoc.getText(0, bDoc.getLength());
                    lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset); // poczatek bieżącej linii
                    currentLine = getCurrentLine(bDoc, caretOffset);
                    //System.out.println(currentLine);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    currentLine = String.valueOf(line); // tekst od poczatku linii do kursora
                    //System.out.println(currentLine);
                    final int whiteOffset = indexOfWhite(line); // ostatnia spacja
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1); // uzupelniane slowo
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                } finally {
                    ((AbstractDocument) document).readUnlock();
                }
//                try {
//                    String ftlvariable = "(<|\\[)#--\\s@ftlvariable\\sname=\"(\\w+)\"\\stype=\"([\\w\\.]+)\"\\s--(>|\\])";
//                    Pattern ftlvarpattern = Pattern.compile(ftlvariable);
//                    Matcher ftlvarmatcher = ftlvarpattern.matcher(text);
//                    while (ftlvarmatcher.find()) {
//                        String name = ftlvarmatcher.group(2);
//                        String type = ftlvarmatcher.group(3);
//                        Class<?> typeClass = Class.forName(type, false, getClass().getClassLoader());
//                        for (java.lang.reflect.Field f : typeClass.getDeclaredFields()) {
//                            System.out.println("Field: " + f.getName());
//                        }
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
                if (filter.startsWith("<#") || filter.startsWith("[#")) {
                    filter = filter.substring(2);
                    for (String keyword : directives) {
                        if (keyword.startsWith(filter)) {
                            completionResultSet.addItem(FTLCompletionItem.directive(keyword, startOffset + 2, caretOffset));
                        }
                    }
                } else if (filter.startsWith("<@") || filter.startsWith("[@")) {
                    filter = filter.substring(2);
                    Pattern pattern = Pattern.compile("(<|\\[)#assign\\s+(\\w+)");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String name = matcher.group(2);
                        if (name.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(name, startOffset + 2, caretOffset));
                        }
                    }
                    pattern = Pattern.compile("(<|\\[)#import\\s.+\\sas\\s+(\\w+)");
                    matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String hash = matcher.group(2);
                        if (hash.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(hash, startOffset + 2, caretOffset));
                        }
                    }
                } else if (filter.endsWith("?")) {
                    filter = filter.substring(1);
                    for (String builtin : builtins) {
                        if (builtin.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(builtin, startOffset + 1, caretOffset));
                        }
                    }
                }

                if (currentLine.matches("(<|\\[)#ftl\\s+[a-z]*")) {
                    filter = currentLine.substring(currentLine.lastIndexOf(' ') + 1);
                    for (String param : ftlParameters) {
                        if (param.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(param, "=", startOffset, caretOffset));
                        }
                    }
                } else if (currentLine.matches(".*(<|\\[)#setting\\s+[a-z_]*")) {
                    filter = currentLine.substring(currentLine.lastIndexOf(' ') + 1);
                    for (String param : settingNames) {
                        if (param.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(param, "=", startOffset, caretOffset));
                        }
                    }
                }
                if (currentLine.matches(".*(<#|\\$\\{).*\\?[a-z_]*$")) { // builtins only inside interpolations or directives
                    filter = currentLine.substring(currentLine.lastIndexOf("?") + 1);
                    for (String builtin : builtins) {
                        if (builtin.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(builtin, lineStartOffset + currentLine.lastIndexOf("?") + 1, caretOffset));
                        }
                    }
                }
                if (currentLine.matches(".*\\$\\{([a-z]*)")) {
                    filter = currentLine.substring(currentLine.lastIndexOf("{") + 1);
                    for (String ident : idents) {
                        if (ident.startsWith(filter)) {
                            completionResultSet.addItem(new FTLCompletionItem(ident, lineStartOffset + currentLine.lastIndexOf("{") + 1, caretOffset));
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

    /**
     * Gets current line as String, trimmed.
     * @param doc document
     * @param offset caret position
     * @return text from current line
     */
    static String getCurrentLine(StyledDocument doc, int offset) {
        try {
            Element lineElement = doc.getParagraphElement(offset);
            return doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset()).trim();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
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
