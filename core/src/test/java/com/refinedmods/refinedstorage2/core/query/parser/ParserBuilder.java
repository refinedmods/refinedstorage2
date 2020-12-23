package com.refinedmods.refinedstorage2.core.query.parser;

import com.refinedmods.refinedstorage2.core.query.lexer.*;
import com.refinedmods.refinedstorage2.core.query.parser.node.Node;

import java.util.ArrayList;
import java.util.List;

class ParserBuilder {
    private static final TokenPosition DUMMY_POSITION = new TokenPosition(new Source("<dummy>", null), new TokenRange(0, 0, 0, 0));

    private final List<Token> tokens = new ArrayList<>();

    ParserBuilder token(String content, TokenType type) {
        tokens.add(new Token(content, type, DUMMY_POSITION));
        return this;
    }

    List<Node> getNodes() {
        Parser parser = new Parser(tokens);
        parser.parse();
        return parser.getNodes();
    }
}
