package org.netbeans.freemarker.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author Rafał Ostanek
 */
public class FTLTokenId implements TokenId {

    private final String name;
    private final String primaryCategory;
    private final int id;

    public FTLTokenId(String name, String primaryCategory, int id) {
        this.name = name;
        this.primaryCategory = primaryCategory;
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return id;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    /**
     * Registering the NetBeans Lexer
     * @return 
     */
    public static Language<FTLTokenId> getLanguage() {
        return new FTLLanguageHierarchy().language();
    }

    @Override
    public String toString() {
        return "FTLTokenId(" + id + " " + name + ")";
    }

}
