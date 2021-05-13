package com.refinedmods.refinedstorage2.core.query.parser;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class ParserOperatorMappings {
    public static final ParserOperatorMappings DEFAULT_MAPPINGS = new ParserOperatorMappings()
            .addBinaryOperator("||", new Operator(0, Associativity.LEFT))
            .addBinaryOperator("&&", new Operator(1, Associativity.LEFT));

    private final Map<String, Operator> binaryOperatorPrecedenceMap = new HashMap<>();

    public ParserOperatorMappings addBinaryOperator(String content, Operator operator) {
        binaryOperatorPrecedenceMap.put(content, operator);
        return this;
    }

    public Operator getOperator(Token token) {
        return binaryOperatorPrecedenceMap.get(token.getContent());
    }
}
