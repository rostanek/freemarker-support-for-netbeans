package org.netbeans.freemarker.lexer;

import java.io.IOException;
import java.io.InputStream;

import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

import freemarker.core.FMParserConstants;
import freemarker.core.FMParserWSTokenManager;
import freemarker.core.SimpleCharStream;
import freemarker.core.Token;
import freemarker.core.TokenMgrError;

/**
 *
 * @author Rafał Ostanek
 */
class FTLLexer implements Lexer<FTLTokenId> {

    private LexerRestartInfo<FTLTokenId> info;
    private FMParserWSTokenManager fmParserTokenManager;

    public FTLLexer(LexerRestartInfo<FTLTokenId> info) {
        this.info = info;
        final LexerInput input = info.input();
        InputStream istream = new InputStream() {

            @Override
            public int read() throws IOException {
                int result = input.read();
                //debug("read " + result);
                //if (result == LexerInput.EOF) {
                //    throw new IOException("LexerInput EOF");
                //}
                return result;
            }
        };
        SimpleCharStream stream = new SimpleCharStream(istream);
        fmParserTokenManager = new FMParserWSTokenManager(stream);
    }

    @Override
    public org.netbeans.api.lexer.Token<FTLTokenId> nextToken() {
        Token token;
        try {
            token = fmParserTokenManager.getNextToken();

        } catch (TokenMgrError err) {
            debug(err.getMessage());
            org.netbeans.api.lexer.Token<FTLTokenId> result;
            if (err.getMessage().startsWith("You can't use")) {
                result = info.tokenFactory().createToken(FTLLanguageHierarchy.getToken(FMParserConstants.STATIC_TEXT_NON_WS), 2);
                debug("fictional 2 char token to recover");
            } else {
                result = info.tokenFactory().createToken(FTLLanguageHierarchy.getToken(FMParserConstants.STATIC_TEXT_NON_WS), 1);
                debug("fictional 1 char token to recover");
            }
            return result;
        }
        FTLTokenId tokenId = FTLLanguageHierarchy.getToken(token.kind);
        //debug(token.beginLine + ":" + token.beginColumn + " " + token.endLine + ":" + token.endColumn);
        //debug(tokenId + " " + token.image);
        //if (info.input().readLength() < 1) {
        //    return null;
        //}
        if (token.kind == FMParserConstants.EOF) {
            debug(info.input().readLength());
            //while (info.input().readLength() > 0) {
            //    info.input().read();
            //}
            debug("EOF returning null");
            return null;
        }
        if ((token.kind == FMParserConstants.TERSE_COMMENT_END || token.kind == FMParserConstants.MAYBE_END) && token.image.endsWith(";")) {
            // this is because of weird hacks in FTL.jj
            debug("trimming ; at the end of token image");
            token.image = token.image.substring(0, token.image.length() - 1);
        }
        int length = token.image.length();
        //debug("length " + length + " readLength " + info.input().readLength());
        
        if (token.kind == FMParserConstants.TRIVIAL_FTL_HEADER) {
            //length++; // for \n eaten by eatNewline
        }
        return info.tokenFactory().createToken(tokenId, length);
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

	private void debug(Object s) {
		//System.out.println(s);
	}
}
