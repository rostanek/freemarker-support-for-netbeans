package org.netbeans.freemarker.highlight;

import freemarker.core.FMParserConstants;
import java.awt.Color;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author rostanek
 */
public class FTLHighlighter implements CaretListener {
    private static final AttributeSet defaultColors =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(236, 235, 163));
    
    private final OffsetsBag bag;
    
    private JTextComponent comp;
    private final WeakReference<Document> weakDoc;
    
    private final RequestProcessor rp;
    private final static int REFRESH_DELAY = 100;
    private RequestProcessor.Task lastRefreshTask;

    public FTLHighlighter(Document doc) {
        rp = new RequestProcessor(FTLHighlighter.class);
        bag = new OffsetsBag(doc);
        weakDoc = new WeakReference<Document>(doc);
        DataObject dobj = NbEditorUtilities.getDataObject(weakDoc.get());
        if (dobj != null) {
            EditorCookie pane = dobj.getLookup().lookup(EditorCookie.class);
            
            JEditorPane[] panes = pane.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                comp = panes[0];
                comp.addCaretListener(this);
            }
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        bag.clear();
        setupAutoRefresh();
    }

    public void setupAutoRefresh() {
        if (lastRefreshTask == null) {
            lastRefreshTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    Document doc = weakDoc.get();
                    if (doc != null) {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument) doc).readLock();
                        }
                        try {
                            TokenHierarchy th = TokenHierarchy.get(doc);
                            TokenSequence ts = th.tokenSequence();
                            ts.move(comp.getCaretPosition());
                            ts.moveNext();
                            Token token = ts.token();
                            if (token.id().ordinal() == FMParserConstants.ID) {
                                String text = token.text().toString();
                                ts.moveStart();
                                while (ts.moveNext()) {
                                    token = ts.token();
                                    if (token.id().ordinal() == FMParserConstants.ID && token.length() == text.length()) {
                                        if (token.text().toString().equals(text)) {
                                            bag.addHighlight(ts.offset(), ts.offset() + token.length(), defaultColors);
                                        }
                                    }
                                }
                            }
                        } finally {
                            if (doc instanceof AbstractDocument) {
                                ((AbstractDocument) doc).readUnlock();
                            }
                        }
                    }

                }
            });
        }
        lastRefreshTask.schedule(REFRESH_DELAY);
    }

    public OffsetsBag getHighlightsBag() {
        return bag;
    }

}
