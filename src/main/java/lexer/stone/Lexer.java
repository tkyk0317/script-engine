package stone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//----------------------------------------.
// 字句解析器クラス.
//----------------------------------------.
public class Lexer {
    public static String regexPat = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"+ "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";
    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> queue = new ArrayList<Token>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader r) {
        hasMore = true;
        reader = new LineNumberReader(r);
    }

    public Token read() throws ParseException {
        if(fillQueue(0)) {
            return queue.remove(0);
        }
        return Token.EOF;
    }

    public Token peek(int i) throws ParseException {
        if(fillQueue(i)) {
            return queue.get(i);
        }
        return Token.EOF;
    }

    private boolean fillQueue(int i) throws ParseException {
        while(i >= queue.size()) {
            if(hasMore) {
                readLine();
            }
            return false;
        }
        return true;
    }

    protected void readLine() throws ParseException {
        String line;
        try {
            line = reader.readLine();
        }
        catch (IOException e) {
            throw new ParseException(e);
        }
        if (null == line) {
            hasMore = false;
            return;
        }

        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);
        matcher.useTransparentBounds(true).useAnchoringBounds(false);
        int pos = 0;
        int endPos = line.length();
        while(pos < endPos) {
            matcher.region(pos, endPos);
            if(matcher.lookingAt()) {
                addToken(lineNo, matcher);
                pos = matcher.end();
            }
            else {
                throw new ParseException("bad token at line " + lineNo);
            }
        }
        queue.add(new IdToken(lineNo, Token.EOL));
    }

    protected void addToken(int lineNo, Matcher matcher) {
        String m = matcher.group(1);
        // 空白ではないか？
        if (m != null) {
            // コメントではないか？
            if(null == matcher.group(2)) {
                Token token;
                if(null != matcher.group(3)) {
                    // 数字
                    token = new NumToken(lineNo, Integer.parseInt(m));
                }
                else if(null != matcher.group(4)) {
                    // 文字
                    token = new StrToken(lineNo, toStringLiteral(m));
                }
                else {
                    // ID
                    token = new IdToken(lineNo, m);
                }
                queue.add(token);
            }
        }
    }

    protected String toStringLiteral(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length() - 1;
        for(int i = 1 ; i < len ; i++) {
            char c = s.charAt(i);
            if(c == '\\' && i + 1 < len) {
                int c2 = s.charAt(i + 1);
                if(c2 == '"' || c2 == '\\') {
                    c = s.charAt(++i);
                }
                else if (c2 == 'n') {
                    ++i;
                    c = '\n';
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    //-----------------------------------.
    // 数値Tokenクラス.
    //-----------------------------------.
    protected static class NumToken extends Token {
        private int value;

        protected NumToken(int line, int v) {
            super(line);
            value = v;
        }
        public boolean isNumber() { return true; }
        public String getText() { return Integer.toString(value); }
        public int getNumber() { return value; }
    }

    //-----------------------------------.
    // IDTokenクラス.
    //-----------------------------------.
    protected static class IdToken extends Token {
        private String text;

        protected IdToken(int line, String id) {
            super(line);
            text = id;
        }
        public boolean isIdentifier() { return true; }
        public String getText() { return text; }
    }

    //-----------------------------------.
    // 文字Tokenクラス.
    //-----------------------------------.
    protected static class StrToken extends Token {
        private String literal;

        protected StrToken(int line, String str) {
            super(line);
            literal = str;
        }
        public boolean isString() { return true; }
        public String getText() { return literal; }
    }
}