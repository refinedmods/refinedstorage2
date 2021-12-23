package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public record BinOpNode(Node left,
                        Node right,
                        Token binOp) implements Node {
    @Override
    public String toString() {
        return "(" + left + " " + binOp.content() + " " + right + ")";
    }

    @Override
    public TokenRange getRange() {
        return TokenRange.combine(left.getRange(), right.getRange());
    }
}
