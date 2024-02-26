package com.craftinginterpreters.jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int startPos = 0;
    private int currentPos = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("and", TokenType.AND),
            Map.entry("class", TokenType.CLASS),
            Map.entry("else", TokenType.ELSE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("for", TokenType.FOR),
            Map.entry("fun", TokenType.FUN),
            Map.entry("if", TokenType.IF),
            Map.entry("nil", TokenType.NIL),
            Map.entry("or", TokenType.OR),
            Map.entry("print", TokenType.PRINT),
            Map.entry("return", TokenType.RETURN),
            Map.entry("super", TokenType.SUPER),
            Map.entry("this", TokenType.THIS),
            Map.entry("true", TokenType.TRUE),
            Map.entry("var", TokenType.VAR),
            Map.entry("while", TokenType.WHILE)
    );

    
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            startPos = currentPos;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char ch = advance();
        switch (ch) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;

            case '/': {
                if (match('/')) {
                    skipLineComment();
                } else if (match('*')) {
                    skipBlockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            }

            // Ignore whitespace.
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n': line++; break;

            case '"': string(); break;

            default: {
                if (isDigit(ch)) {
                    number();
                } else if (isAlpha(ch)) {
                    identifier();
                } else {
                    Jlox.error(line, "Unexpected character"); break;
                }
                break;
            }
        }
    }

    private void skipLineComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private void skipBlockComment() {
        for (int nesting = 1; nesting > 0; advance()) {
            if (peek() == '\0') {
                Jlox.error(line, "Unterminated block comment.");
                return;
            }

            if (peek() == '/' && peek(1) == '*') {
                advance();
                nesting++;
            } else if (peek() == '*' && peek(1) == '/' ) {
                advance();
                nesting--;
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(startPos, currentPos);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;

        addToken(TokenType.IDENTIFIER);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Jlox.error(line, "Unterminated string.");
            return;
        }

        // Closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(startPos + 1, currentPos - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peek(1))) {
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(
                TokenType.NUMBER,
                Double.parseDouble(source.substring(startPos, currentPos))
        );
    }

    private boolean match(final char expected) {
        if (peek() != expected) return false;
        currentPos++;
        return true;
    }

    private boolean isAtEnd() {
        return currentPos >= source.length();
    }
    private boolean isAtEnd(final int offset) {
        return currentPos + offset >= source.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(currentPos);
    }
    private char peek(final int offset) {
        if (isAtEnd(offset)) return '\0';
        return source.charAt(currentPos + offset);
    }

    private boolean isAlpha(final char ch) {
        return (
            (ch >= 'a' && ch <= 'z') ||
            (ch >= 'A' && ch <= 'Z') ||
            (ch == '_')
        );
    }

    private boolean isAlphaNumeric(final char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private boolean isDigit(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    private char advance() {
        return source.charAt(currentPos++);
    }

    private void addToken(final TokenType type) {
        addToken(type, null);
    }
    private void addToken(final TokenType type, final Object literal) {
        String text = source.substring(startPos, currentPos);
        tokens.add(new Token(type, text, literal, line));
    }

}
