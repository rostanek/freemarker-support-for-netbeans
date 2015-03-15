package org.netbeans.freemarker.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import freemarker.core.FMParser;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

import freemarker.core.ParseException;

public class FTLParser extends Parser {

    private Snapshot snapshot;
    private FMParser freemarkerParser;
    private List<ParseException> exceptions = new ArrayList<ParseException>();

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        Reader reader = new StringReader(snapshot.getText().toString());
        freemarkerParser = new FMParser(reader);
        exceptions.clear();
        try {
            freemarkerParser.Root();
        } catch (ParseException ex) {
            if (ex.currentToken != null) {
                exceptions.add(ex);
            }
        }
    }

    @Override
    public Result getResult(Task task) {
        return new FTLParserResult(snapshot, /*freemarkerParser,*/ exceptions);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class FTLParserResult extends ParserResult {

//        private FMParser fmParser;
        private List<ParseException> exceptions;
        private boolean valid = true;

        FTLParserResult(Snapshot snapshot, /*FMParser fmParser,*/ List<ParseException> exceptions) {
            super(snapshot);
//            this.fmParser = fmParser;
            this.exceptions = exceptions;
        }

//        public FMParser getFMParser() throws org.netbeans.modules.parsing.spi.ParseException {
//            if (!valid) {
//                throw new org.netbeans.modules.parsing.spi.ParseException();
//            }
//            return fmParser;
//        }

        public List<ParseException> getExceptions() {
            return exceptions;
        }

        @Override
        protected void invalidate() {
            valid = false;
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            return new ArrayList<Error>();
        }

    }

}
