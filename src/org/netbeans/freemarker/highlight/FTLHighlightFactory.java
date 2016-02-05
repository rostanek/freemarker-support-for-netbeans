package org.netbeans.freemarker.highlight;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author rostanek
 */
@MimeRegistration(mimeType = "text/x-ftl", service = HighlightsLayerFactory.class)
public class FTLHighlightFactory implements HighlightsLayerFactory {

    public static FTLHighlighter getHighlighter(Document doc) {
        FTLHighlighter highlighter = (FTLHighlighter) doc.getProperty(FTLHighlighter.class);
        if (highlighter == null) {
            doc.putProperty(FTLHighlighter.class, highlighter = new FTLHighlighter(doc));
        }
        return highlighter;
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
            HighlightsLayer.create(
            FTLHighlighter.class.getName(),
            ZOrder.CARET_RACK.forPosition(2000),
            true,
            getHighlighter(context.getDocument()).getHighlightsBag())
        };
    }

}
