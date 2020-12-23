package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;

public class BinOpNode implements Node {
    private final Node left;
    private final Node right;
    private final Token binOp;

    public BinOpNode(Node left, Node right, Token binOp) {
        this.left = left;
        this.right = right;
        this.binOp = binOp;
    }

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
}
