# freemarker-support-for-netbeans
FreeMarker support for NetBeans including syntax coloring, parsing etc. new features will be added soon.

known problems:
- unclosed string throws lexical error
- square bracket syntax - newline after ftl header is not tokenized because of eatNewline() in FTL.jj
- because of changing whitespaces from SKIP to TOKEN parser throws exception when whitespace is found