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
            String msg = err.getMessage();
            debug(msg);
            int len = 1;
            if (msg.startsWith("You can't use")) {
                len = 2;
            } else if (msg.startsWith("Unknown directive")) {
                String dn = msg.substring(20, msg.indexOf("."));
                debug(dn);
                len = dn.length() + 2;
            } else if (msg.contains("is an existing directive")) {
                String dn = msg.substring(0, msg.indexOf("is an"));
                debug(dn);
                len = dn.length();
            }
            debug("fictional STATIC_TEXT_NON_WS token with length = " + len + " to recover");
            return info.tokenFactory().createToken(FTLLanguageHierarchy.getToken(FMParserConstants.STATIC_TEXT_NON_WS), len);
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
