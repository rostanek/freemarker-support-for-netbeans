package org.netbeans.freemarker.parser;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.TaskFactory;

@MimeRegistration(mimeType = "text/x-ftl", service = TaskFactory.class)
public class FTLSyntaxErrorHighlightingTaskFactory extends TaskFactory {

    @Override
    public Collection create(Snapshot snapshot) {
        return Collections.singleton(new FTLSyntaxErrorHighlightingTask());
    }

}
