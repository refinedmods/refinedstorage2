package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.parser.ParserException;

public class UnaryOpNode implements Node {
    public enum Type {
        PREFIX,
        SUFFIX
    }

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
}
