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
 * @author Rafał Ostanek
 */
public class FTLCompletionItem implements CompletionItem {

    private String text;
    private String suffix = "";
    private int caretOffset;
    private int dotOffset;
    
    private static ImageIcon fieldIcon =
        new ImageIcon(ImageUtilities.loadImage("org/netbeans/freemarker/icon.png"));

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
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet result, Document doc, int i) {
                result.setDocumentation(new FTLCompletionDocumentation(FTLCompletionItem.this));
                result.finish();
            }
        });
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
    
    public class FTLCompletionDocumentation implements CompletionDocumentation {

        private FTLCompletionItem item;
        private URL url;

        public FTLCompletionDocumentation(FTLCompletionItem item) {
            this.item = item;
            Map<String, String> docs = new HashMap<String, String>();
            docs.put("recover", "attempt");
            docs.put("noescape", "escape");
            docs.put("return", "function");
            docs.put("else", "if");
            docs.put("elseif", "if");
            docs.put("nested", "macro");
            docs.put("case", "switch");
            docs.put("default", "switch");
            docs.put("break", "switch");
            docs.put("lt", "t");
            docs.put("rt", "t");
            try {
                String page = item.text;
                if (docs.containsKey(page)) {
                    page = docs.get(page);
                }
                url = new URL("http://freemarker.org/docs/ref_directive_" + page + ".html");
            } catch (MalformedURLException ex) {
                url = null;
            }
        }

        public FTLCompletionDocumentation(FTLCompletionItem item, URL url) {
            this.item = item;
            this.url = url;
        }

        @Override
        public String getText() {
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
                    return sb.substring(start, end);
                }
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public CompletionDocumentation resolveLink(String string) {
            if (!string.startsWith("#")) {
                try {
                    return new FTLCompletionDocumentation(item, new URL("http://freemarker.org/docs/" + string));
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
