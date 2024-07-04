package com.refinedmods.refinedstorage.query.parser.node;

import com.refinedmods.refinedstorage.query.lexer.Token;

public record BinOpNode(Node left,
                        Node right,
                        Token binOp) implements Node {
    @Override
    public String toString() {
        return "(" + left + " " + binOp.content() + " " + right + ")";
    }
}
