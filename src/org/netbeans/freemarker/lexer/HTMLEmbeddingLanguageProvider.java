package org.netbeans.freemarker.lexer;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.freemarker.panel.FTLPanel;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LanguageProvider.class)
public class HTMLEmbeddingLanguageProvider extends LanguageProvider {

    @Override
    public Language<?> findLanguage(String mimeType) {
        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
            LanguagePath languagePath, InputAttributes inputAttributes) {

        String embeddedMime = NbPreferences.forModule(FTLPanel.class).get("embeddedMime", "text/html");

        Language<?> embeddedLanguage = MimeLookup.getLookup(embeddedMime).lookup(Language.class);

        if (embeddedLanguage != null && languagePath.mimePath().equals("text/x-ftl")) {
            if (token.id().name().startsWith("STATIC_TEXT")) {
                return LanguageEmbedding.create(embeddedLanguage, 0, 0, true);
            }
        }
        return null;
    }

}
