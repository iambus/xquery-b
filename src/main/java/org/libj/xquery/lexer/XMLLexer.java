package org.libj.xquery.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLLexer {

    private static final Pattern TAG_PATTERN = Pattern.compile("<\\s*/?(\\w+)(?:\\s+.*)?/?\\s*>");
    public static String parseTagName(String tag) {
        Matcher matcher = TAG_PATTERN.matcher(tag);
        if (!matcher.find()) {
            throw new RuntimeException("Invalid tag: "+tag);
        }
        return matcher.group(1);
    }
}
