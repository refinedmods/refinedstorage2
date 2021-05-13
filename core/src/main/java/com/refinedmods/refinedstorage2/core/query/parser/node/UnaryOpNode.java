package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public class UnaryOpNode implements Node {
    private final Node node;
    private final Token operator;

    public UnaryOpNode(Node node, Token operator) {
        this.node = node;
        this.operator = operator;
    }

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
