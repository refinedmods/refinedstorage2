package com.refinedmods.refinedstorage2.query.parser;

import com.refinedmods.refinedstorage2.query.lexer.Source;
import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenPosition;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;
import com.refinedmods.refinedstorage2.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.query.parser.node.Node;

import java.util.ArrayList;
import java.util.List;

class ParserBuilder {
    @SuppressWarnings("ConstantConditions")
    private static final TokenPosition DUMMY_POSITION = new TokenPosition(new Source("<dummy>", null), new TokenRange(0, 0, 0, 0));

    private static final ParserOperatorMappings OPERATOR_MAPPINGS = new ParserOperatorMappings()
            .addBinaryOperator("=", new Operator(0, Associativity.RIGHT))
            .addBinaryOperator("||", new Operator(1, Associativity.LEFT))
            .addBinaryOperator("&&", new Operator(2, Associativity.LEFT))
            .addBinaryOperator("+", new Operator(3, Associativity.LEFT))
            .addBinaryOperator("-", new Operator(3, Associativity.LEFT))
            .addBinaryOperator("*", new Operator(4, Associativity.LEFT))
            .addBinaryOperator("/", new Operator(4, Associativity.LEFT))
            .addBinaryOperator("^", new Operator(5, Associativity.RIGHT));

    private final List<Token> tokens = new ArrayList<>();

    ParserBuilder token(String content, TokenType type) {
        tokens.add(new Token(content, type, DUMMY_POSITION));
        return this;
    }

    List<Node> getNodes() {
        Parser parser = new Parser(tokens, OPERATOR_MAPPINGS);
        parser.parse();
        return parser.getNodes();
    }
}
