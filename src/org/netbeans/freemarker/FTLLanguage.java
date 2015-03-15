package org.netbeans.freemarker;

import org.netbeans.api.lexer.Language;
import org.netbeans.freemarker.lexer.FTLTokenId;
import org.netbeans.freemarker.parser.FTLParser;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Rafał Ostanek
 */
@LanguageRegistration(mimeType = "text/x-ftl")
public class FTLLanguage extends DefaultLanguageConfig {

    @Override
    public Language<FTLTokenId> getLexerLanguage() {
        return FTLTokenId.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "FTL";
    }

    @Override
    public Parser getParser() {
        return new FTLParser();
    }

}
