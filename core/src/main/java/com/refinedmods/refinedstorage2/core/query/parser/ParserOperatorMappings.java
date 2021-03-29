package com.refinedmods.refinedstorage2.core.query.parser;

import java.util.HashMap;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;

public class ParserOperatorMappings {
    public static final ParserOperatorMappings DEFAULT_MAPPINGS = new ParserOperatorMappings()
        .addBinaryOperator("||", new Operator(0, Associativity.LEFT))
        .addBinaryOperator("&&", new Operator(1, Associativity.LEFT))
        .addUnaryOperatorPosition("!", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition("@", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition("$", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition(">", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition(">=", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition("<", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition("<=", UnaryOperatorPosition.PREFIX)
        .addUnaryOperatorPosition("=", UnaryOperatorPosition.PREFIX);

    private final Map<String, Operator> binaryOperatorPrecedenceMap = new HashMap<>();
    private final Map<String, UnaryOperatorPosition> unaryOperatorPositions = new HashMap<>();

    public ParserOperatorMappings addBinaryOperator(String content, Operator operator) {
        binaryOperatorPrecedenceMap.put(content, operator);
        return this;
    }

    public ParserOperatorMappings addUnaryOperatorPosition(String content, UnaryOperatorPosition position) {
        unaryOperatorPositions.put(content, position);
        return this;
    }

    public Operator getOperator(Token token) {
        return binaryOperatorPrecedenceMap.get(token.getContent());
    }

    public UnaryOperatorPosition getUnaryOperatorPosition(Token token) {
        return unaryOperatorPositions.get(token.getContent());
    }
}
