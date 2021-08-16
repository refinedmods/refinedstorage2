package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public record BinOpNode(Node left,
                        Node right,
                        Token binOp) implements Node {
    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public Token getBinOp() {
        return binOp;
    }

    @Override
    public String toString() {
        return "(" + left + " " + binOp.getContent() + " " + right + ")";
    }

    @Override
    public TokenRange getRange() {
        return TokenRange.combine(left.getRange(), right.getRange());
    }
}
