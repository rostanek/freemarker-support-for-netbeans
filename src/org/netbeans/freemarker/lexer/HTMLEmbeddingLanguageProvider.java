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

    private Language<HTMLTokenId> embeddedLanguage;

    @Override
    public Language<?> findLanguage(String mimeType) {
        return null;
    }

    @Override
    public LanguageEmbedding<HTMLTokenId> findLanguageEmbedding(Token<?> token, 
        LanguagePath languagePath, InputAttributes inputAttributes) {
        initLanguage();
        
        if (languagePath.mimePath().equals("text/x-ftl")) {
            if (token.id().name().startsWith("STATIC_TEXT")) {
                return LanguageEmbedding.create(embeddedLanguage, 0, 0, true);
            }
        }
        return null;
    }

    private void initLanguage() {
        embeddedLanguage = MimeLookup.getLookup("text/html").lookup(Language.class);
        if (embeddedLanguage == null) {
            throw new NullPointerException("Can't find language for embedding");
        }
    }
    
}