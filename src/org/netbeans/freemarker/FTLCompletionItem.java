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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        DIRECTIVE, BUILTIN
    }
    private static Map<String, FTLCompletionDocumentation> docCache = new HashMap<String, FTLCompletionDocumentation>();
    private static Map<String, CompletionDocumentation> docCache2 = new HashMap<String, CompletionDocumentation>();

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

    static FTLCompletionItem builtin(String text, int dotOffset, int caretOffset) {
        FTLCompletionItem item = new FTLCompletionItem(text, dotOffset, caretOffset);
        item.type = Type.BUILTIN;
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
        if (type != null) {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(CompletionResultSet result, Document doc, int i) {
                    if (type == Type.DIRECTIVE) {
                        result.setDocumentation(FTLCompletionItem.getDocForDirective(text));
                    } else if (type == Type.BUILTIN) {
                        result.setDocumentation(FTLCompletionItem.getDocForBuiltin(text));
                    }
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

    private static Map<String, String> builtinGroups;

    private static CompletionDocumentation getDocForBuiltin(String builtin) {
        if (builtinGroups == null) {
            initBuiltins();
        }
        builtin = builtin.replaceAll("[A-Z]", "_$0").toLowerCase(); // camelCase to underscore_case
        String page = builtinGroups.get(builtin);
        if (!docCache2.containsKey(builtin)) {
            docCache2.put(builtin, new BuiltinDocumentation(page));
        }
        return docCache2.get(builtin);
    }

    private static void initBuiltins() {
        builtinGroups = new HashMap<String, String>();
        try {
            String content = download(new URL("http://freemarker.org/docs/ref_builtins_alphaidx.html"));
            int start = content.indexOf("<ul>");
            int end = content.indexOf("</ul>", start);
            String index = content.substring(start, end);
            Pattern pat = Pattern.compile(".*<a href=\"([A-Za-z_#\\.]+)\">([a-z_]+)</a>.*");
            Matcher mat = pat.matcher(index);
            while (mat.find()) {
                String href = mat.group(1);
                String name = mat.group(2);
                builtinGroups.put(name, href);
            }
        } catch (IOException ex) {

        }
    }

    private static String download(URL url) {
        try {
            InputStream in = url.openStream();
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder(16000);
            while (in.read(buffer) > 0) {
                sb.append(new String(buffer));
            }
            return sb.toString();
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        return "";
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
                String content = download(url);
                int start = content.indexOf("<div class=\"page-content\">");
                int end = content.indexOf("<div class=\"bottom-pagers-wrapper\">", start);
                if (start > 0 && end > 0) {
                    text = content.substring(start, end);
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

    public static class BuiltinDocumentation implements CompletionDocumentation {

        private URL url;
        private String text;

        public BuiltinDocumentation(String page) {

            try {
                url = new URL("http://freemarker.org/docs/" + page);
            } catch (MalformedURLException ex) {
                url = null;
            }
        }

        @Override
        public String getText() {
            if (text == null) {
                // download documentation
                String content = download(url);
                int start = content.indexOf("<h2 class=\"content-header header-section2\" id=\"" + url.getRef());
                int end = content.indexOf("<h2", start + 60);
                if (start > 0 && end > 0) {
                    text = content.substring(start, end);
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
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }

    }
}
