package org.netbeans.freemarker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author rostanek
 */
public class FTLCompletionItem implements CompletionItem {

    static enum Type {

        DIRECTIVE
    }
    private static Map<String, FTLCompletionDocumentation> docCache = new HashMap<String, FTLCompletionDocumentation>();
    private static Map<String, String> dirMap = new HashMap<String, String>() {
        {
            put("recover", "attempt");
            put("noescape", "escape");
            put("noEscape", "escape");
            put("noParse", "noparse");
            put("return", "function");
            put("else", "if");
            put("elseif", "if");
            put("elseIf", "if");
            put("items", "list");
            put("sep", "list");
            put("nested", "macro");
            put("case", "switch");
            put("default", "switch");
            put("break", "switch");
            put("lt", "t");
            put("rt", "t");
        }
    };

    private Type type;
    private String text;
    private String suffix = "";
    private int caretOffset;
    private int dotOffset;

    private static ImageIcon fieldIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/freemarker/icon.png"));

    public FTLCompletionItem(String text, int dotOffset, int caretOffset) {
        this.text = text;
        this.caretOffset = caretOffset;
        this.dotOffset = dotOffset;
    }

    public FTLCompletionItem(String text, String suffix, int dotOffset, int caretOffset) {
        this.text = text;
        this.suffix = suffix;
        this.caretOffset = caretOffset;
        this.dotOffset = dotOffset;
    }

    static FTLCompletionItem directive(String text, int dotOffset, int caretOffset) {
        FTLCompletionItem item = new FTLCompletionItem(text, dotOffset, caretOffset);
        item.type = Type.DIRECTIVE;
        return item;
    }

    @Override
    public void defaultAction(JTextComponent jtc) {
        try {
            StyledDocument doc = (StyledDocument) jtc.getDocument();
            doc.remove(dotOffset, caretOffset - dotOffset);
            doc.insertString(dotOffset, text + suffix, null);
            if (text.equals("if")) {
                doc.insertString(dotOffset + 2, " >\n\n</#if>\n", null);
            }
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
    }

    @Override
    public int getPreferredWidth(Graphics grphcs, Font font) {
        return CompletionUtilities.getPreferredWidth(text, null, grphcs, font);
    }

    @Override
    public void render(Graphics grphcs, Font font, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(fieldIcon, text, null, grphcs, font, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        if (type == Type.DIRECTIVE) {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(CompletionResultSet result, Document doc, int i) {
                    result.setDocumentation(FTLCompletionItem.getDocForDirective(text));
                    result.finish();
                }
            });
        }
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }

    private static FTLCompletionDocumentation getDocForDirective(String directive) {
        String page = directive;
        if (dirMap.containsKey(directive)) {
            page = dirMap.get(directive);
        }
        if (!docCache.containsKey(page)) {
            docCache.put(page, new FTLCompletionDocumentation(page));
        }
        return docCache.get(page);
    }

    public static class FTLCompletionDocumentation implements CompletionDocumentation {

        private URL url;
        private String text;

        public FTLCompletionDocumentation(String text) {
            try {
                url = new URL("http://freemarker.org/docs/ref_directive_" + text + ".html");
            } catch (MalformedURLException ex) {
                url = null;
            }
        }

        public FTLCompletionDocumentation(URL url) {
            this.url = url;
        }

        @Override
        public String getText() {
            if (text == null) {
                // download documentation
                try {
                    InputStream in = url.openStream();
                    byte[] buffer = new byte[1024];
                    StringBuilder sb = new StringBuilder(16000);
                    while (in.read(buffer) > 0) {
                        sb.append(new String(buffer));
                    }
                    int start = sb.indexOf("<div class=\"page-content\">");
                    int end = sb.indexOf("<div class=\"bottom-pagers-wrapper\">", start);
                    if (start > 0 && end > 0) {
                        text = sb.substring(start, end);
                    }
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
            return text;
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public CompletionDocumentation resolveLink(String string) {
            if (!string.startsWith("#")) {
                if (string.startsWith("ref_directive_")) {
                    return FTLCompletionItem.getDocForDirective(string.substring(14, string.lastIndexOf(".")));
                }
                try {
                    return new FTLCompletionDocumentation(new URL("http://freemarker.org/docs/" + string));
                } catch (MalformedURLException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }

    }
}
