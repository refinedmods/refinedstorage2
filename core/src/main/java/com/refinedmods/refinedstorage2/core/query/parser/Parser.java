package com.refinedmods.refinedstorage2.core.query.parser;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.core.query.parser.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final List<Token> tokens;
    private final List<Node> nodes = new ArrayList<>();
    private final Map<String, Operator> precedenceMap = new HashMap<>();

    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;

        precedenceMap.put("+", new Operator(1, Associativity.LEFT));
        precedenceMap.put("*", new Operator(2, Associativity.LEFT));
        precedenceMap.put("^", new Operator(3, Associativity.RIGHT));
        precedenceMap.put("=", new Operator(0, Associativity.RIGHT));
    }

    public void parse() {
        while (isNotEof()) {
            Node node = parseExpression(0);

            nodes.add(node);
        }
    }

    private Node parseExpression(int minPrecedence) {
        Node lhs = parseAtom();

        Token cur = currentOrNull();
        while (cur != null && cur.getType() == TokenType.BIN_OP && getOperator(cur).getLevel() >= minPrecedence) {
            Operator currentOp = getOperator(cur);
            int nextMinPrecedence = currentOp.getAssociativity() == Associativity.LEFT ? (currentOp.getLevel() + 1) : currentOp.getLevel();

            next();

            Node rhs = parseExpression(nextMinPrecedence);

            lhs = new BinOpNode(lhs, rhs, cur);

            cur = currentOrNull();
        }

        return lhs;
    }

    private Operator getOperator(Token token) {
        return precedenceMap.get(token.getContent());
    }

    private Node parseAtom() {
        return parseParen();
    }

    private Node parseParen() {
        Token current = current();

        if (current.getType() == TokenType.PAREN_OPEN) {
            next();

            Node node = parseExpression(0);

            expect(TokenType.PAREN_CLOSE, ")");
            next();

            return new ParenNode(node);
        }

        return parsePrefixedUnaryOp();
    }

    private Node parsePrefixedUnaryOp() {
        Token maybeUnaryOp = current();
        if (maybeUnaryOp.getType() == TokenType.UNARY_OP) {
            next();
            return new UnaryOpNode(parseLiteral(), maybeUnaryOp, UnaryOpNode.Type.PREFIX);
        }

        return parseSuffixedUnaryOp();
    }

    private Node parseSuffixedUnaryOp() {
        Node node = parseLiteral();

        Token maybeUnaryOp = currentOrNull();
        if (maybeUnaryOp != null && maybeUnaryOp.getType() == TokenType.UNARY_OP) {
            if ("!".equals(maybeUnaryOp.getContent())) {
                throw new ParserException("Cannot use '!' as a suffixed unary operator", maybeUnaryOp);
            }

            next();
            node = new UnaryOpNode(node, maybeUnaryOp, UnaryOpNode.Type.SUFFIX);
        }

        return node;
    }

    private void expect(TokenType type, String content) {
        Token token = currentOrNull();
        if (token == null) {
            throw new ParserException("Expected '" + content + "'", tokens.get(tokens.size() - 1));
        }

        if (token.getType() != type || !token.getContent().equals(content)) {
            throw new ParserException("Expected '" + content + "', got '" + token.getContent() + "'", token);
        }
    }

    private Node parseLiteral() {
        Token current = current();

        if (current.getType() == TokenType.IDENTIFIER ||
            current.getType() == TokenType.FLOATING_NUMBER ||
            current.getType() == TokenType.INTEGER_NUMBER) {
            next();
            return new LiteralNode(current);
        } else {
            throw new ParserException("Unexpected token " + current.getContent(), current);
        }
    }

    private boolean isNotEof() {
        return position < tokens.size();
    }

    private void next() {
        position++;
    }

    private Token current() {
        return tokens.get(position);
    }

    private Token currentOrNull() {
        return isNotEof() ? current() : null;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
