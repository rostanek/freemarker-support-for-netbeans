package org.netbeans.freemarker.lexer;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LanguageProvider.class)
public class HTMLEmbeddingLanguageProvider extends LanguageProvider {
    
    Language ftlLanguage;

    @Override
    public Language<?> findLanguage(String mimeType) {
        if (mimeType.equals("text/x-ftl")) {
            return HTMLTokenId.language();
        }

        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
            LanguagePath languagePath, InputAttributes inputAttributes) {

        initilizeLanguage();

        if (languagePath.mimePath().equals("text/html")) {
            if (token.id().name().equals("TEXT")) {
                LanguageEmbedding languageEmbedding = LanguageEmbedding.create(ftlLanguage, 0, 0, false);
                return languageEmbedding;
            }
        }

        return null;
    }

    private void initilizeLanguage() {
        if (ftlLanguage == null) {

            ftlLanguage = MimeLookup.getLookup(
                    "text/x-ftl").lookup(Language.class);
        }
    }

}