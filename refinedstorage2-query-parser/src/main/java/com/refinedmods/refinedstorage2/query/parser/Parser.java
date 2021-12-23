package com.refinedmods.refinedstorage2.query.parser;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.query.parser.node.BinOpNode;
import com.refinedmods.refinedstorage2.query.parser.node.LiteralNode;
import com.refinedmods.refinedstorage2.query.parser.node.Node;
import com.refinedmods.refinedstorage2.query.parser.node.ParenNode;
import com.refinedmods.refinedstorage2.query.parser.node.UnaryOpNode;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private final List<Node> nodes = new ArrayList<>();
    private final ParserOperatorMappings operatorMappings;

    private int position = 0;

    public Parser(List<Token> tokens, ParserOperatorMappings operatorMappings) {
        this.tokens = tokens;
        this.operatorMappings = operatorMappings;
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
        while (cur != null && cur.type() == TokenType.BIN_OP && operatorMappings.getOperator(cur).level() >= minPrecedence) {
            Operator currentOp = operatorMappings.getOperator(cur);
            int nextMinPrecedence = currentOp.associativity() == Associativity.LEFT ? (currentOp.level() + 1) : currentOp.level();

            next();
            if (!isNotEof()) {
                throw new ParserException("Unfinished binary operator expression", cur);
            }

            Node rhs = parseExpression(nextMinPrecedence);

            lhs = new BinOpNode(lhs, rhs, cur);

            cur = currentOrNull();
        }

        return lhs;
    }

    private Node parseAtom() {
        return parseParen();
    }

    private Node parseParen() {
        Token current = current();

        if (current.type() == TokenType.PAREN_OPEN) {
            next();
            if (!isNotEof()) {
                throw new ParserException("Unclosed parenthesis", current);
            }

            List<Node> nodesInParen = new ArrayList<>();

            while (true) {
                Node node = parseExpression(0);
                nodesInParen.add(node);

                Token currentAfterExpression = currentOrNull();
                if (currentAfterExpression == null) {
                    throw new ParserException("Expected ')'", tokens.get(tokens.size() - 1));
                }

                if (currentAfterExpression.type() == TokenType.PAREN_CLOSE && ")".equals(currentAfterExpression.content())) {
                    next();
                    break;
                }
            }

            return new ParenNode(nodesInParen);
        }

        return parseUnaryOp();
    }

    private Node parseUnaryOp() {
        Token maybeUnaryOp = current();
        if (maybeUnaryOp.type() == TokenType.UNARY_OP) {
            next();
            if (!isNotEof()) {
                throw new ParserException("Unary operator has no target", maybeUnaryOp);
            }

            return new UnaryOpNode(parseAtom(), maybeUnaryOp);
        }

        return parseLiteral();
    }

    private Node parseLiteral() {
        Token current = current();

        if (current.type() == TokenType.IDENTIFIER ||
                current.type() == TokenType.FLOATING_NUMBER ||
                current.type() == TokenType.INTEGER_NUMBER) {
            next();
            return new LiteralNode(current);
        } else {
            throw new ParserException("Unexpected token " + current.content(), current);
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
