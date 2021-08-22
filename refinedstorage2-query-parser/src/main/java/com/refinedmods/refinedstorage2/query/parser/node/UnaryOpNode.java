package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public record UnaryOpNode(Node node, Token operator) implements Node {
    public Node getNode() {
        return node;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public TokenRange getRange() {
        return TokenRange.combine(operator.getPosition().getRange(), node.getRange());
    }

    @Override
    public String toString() {
        return operator.getContent() + node.toString();
    }
}
