package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;
import com.refinedmods.refinedstorage2.core.query.parser.ParserException;

public class UnaryOpNode implements Node {
    private final Node node;
    private final Token operator;
    private final Type type;
    public UnaryOpNode(Node node, Token operator, Type type) {
        this.node = node;
        this.operator = operator;
        this.type = type;
    }

    public Node getNode() {
        return node;
    }

    public Token getOperator() {
        return operator;
    }

    public Type getType() {
        return type;
    }

    @Override
    public TokenRange getRange() {
        switch (type) {
            case PREFIX:
                return TokenRange.combine(operator.getPosition().getRange(), node.getRange());
            case SUFFIX:
                return TokenRange.combine(node.getRange(), operator.getPosition().getRange());
            default:
                throw new RuntimeException("Unexpected token range");
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case PREFIX:
                return operator.getContent() + node.toString();
            case SUFFIX:
                return node.toString() + operator.getContent();
            default:
                throw new ParserException("Unhandled unary operator type " + type, operator);
        }
    }

    public enum Type {
        PREFIX,
        SUFFIX
    }
}
