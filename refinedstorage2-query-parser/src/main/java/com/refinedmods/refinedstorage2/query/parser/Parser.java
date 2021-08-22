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
        while (cur != null && cur.getType() == TokenType.BIN_OP && operatorMappings.getOperator(cur).getLevel() >= minPrecedence) {
            Operator currentOp = operatorMappings.getOperator(cur);
            int nextMinPrecedence = currentOp.getAssociativity() == Associativity.LEFT ? (currentOp.getLevel() + 1) : currentOp.getLevel();

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

        if (current.getType() == TokenType.PAREN_OPEN) {
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

                if (currentAfterExpression.getType() == TokenType.PAREN_CLOSE && ")".equals(currentAfterExpression.getContent())) {
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
        if (maybeUnaryOp.getType() == TokenType.UNARY_OP) {
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
