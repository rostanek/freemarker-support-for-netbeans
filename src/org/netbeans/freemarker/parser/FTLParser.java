package org.netbeans.freemarker.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import freemarker.core.FMParser;
import freemarker.core.ParseException;
import freemarker.template.Template;
import java.io.IOException;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;


public class FTLParser extends Parser {

    private Snapshot snapshot;
    private FMParser freemarkerParser;
	private final List<ParseException> errors = new ArrayList<ParseException>();

    @Override
    public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
		errors.clear();
        Reader reader = new StringReader(snapshot.getText().toString());
		try {
			Template tpl = new Template(snapshot.getSource().getFileObject().getNameExt(), reader);
		    freemarkerParser = new FMParser(tpl, reader, false, false);
			freemarkerParser.Root();
		} catch (ParseException ex) {
			errors.add(ex);
			//ex.printStackTrace();
		} catch (IOException ex) {
			//ex.printStackTrace();
		}
    }

    @Override
    public Result getResult (Task task) {
        return new FTLParserResult (snapshot, errors);
    }

    @Override
    public void addChangeListener (ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener (ChangeListener changeListener) {
    }

    
    public static class FTLParserResult extends ParserResult {

        private final List<ParseException> errors;
        private boolean valid = true;

        FTLParserResult (Snapshot snapshot, List<ParseException> errors) {
            super (snapshot);
            this.errors = errors;
			valid = errors.isEmpty();
        }

        public List<ParseException> getErrors() {
			return errors;
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