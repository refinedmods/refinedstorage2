package com.refinedmods.refinedstorage.query.evaluator;

import com.refinedmods.refinedstorage.query.lexer.Lexer;
import com.refinedmods.refinedstorage.query.lexer.LexerException;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.lexer.Source;
import com.refinedmods.refinedstorage.query.lexer.Token;
import com.refinedmods.refinedstorage.query.lexer.TokenPosition;
import com.refinedmods.refinedstorage.query.lexer.TokenRange;
import com.refinedmods.refinedstorage.query.lexer.TokenType;
import com.refinedmods.refinedstorage.query.parser.Parser;
import com.refinedmods.refinedstorage.query.parser.ParserException;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage.query.parser.node.BinOpNode;
import com.refinedmods.refinedstorage.query.parser.node.LiteralNode;
import com.refinedmods.refinedstorage.query.parser.node.Node;
import com.refinedmods.refinedstorage.query.parser.node.ParenNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/*
* Evaluate a math expression inside a string.
* If there is a problem with the expression, the error is thrown as an exception
* The exception values correlate with entries inside lang files, so to display the error to the user
* create a new translation with the error value as the id.
 */

public class Evaluator {
    private final Source source;
    private final List<Node> nodes = new ArrayList<>();
    private final Map<String, Double> unitMap = Map.of(
        "k", 1e3,
        "m", 1e6,
        "g", 1e9,
        "t", 1e12,
        "p", 1e15,
        "e", 1e18
    );

    public Evaluator(final Source source) {
        if (source.content().isEmpty()) {
            throw new EvaluatorException("");
        }
        this.source = source;
        final Lexer lexer;
        try {
            lexer = new Lexer(source, LexerTokenMappings.ARITHMETIC_MAPPINGS);
            lexer.scan();
        } catch (LexerException e) {
            throw new EvaluatorException("resource_amount_input.unrecognized_char");
        }
        final Parser parser;
        try {
            parser = new Parser(applyUnits(lexer.getTokens()), ParserOperatorMappings.ARITHMETIC_MAPPINGS);
            parser.parse();
        } catch (ParserException e) {
            throw new EvaluatorException("resource_amount_input.syntax_error");
        }
        this.nodes.addAll(parser.getNodes());
    }

    private List<Token> applyUnits(final List<Token> tokens) {
        final List<Token> output = new ArrayList<>();
        for (final Token token : tokens) {
            if (token.type() == TokenType.IDENTIFIER) {
                if (output.isEmpty()) {
                    throw new EvaluatorException("resource_amount_input.no_value_before_unit");
                }
                final TokenType lastType = output.getLast().type();
                if (lastType != TokenType.INTEGER_NUMBER && lastType != TokenType.FLOATING_NUMBER) {
                    throw new EvaluatorException("resource_amount_input.no_value_before_unit");
                }
                final TokenPosition position = new TokenPosition(source, new TokenRange(0, 0, 0, 0));
                output.add(new Token("*", TokenType.BIN_OP, position));
                try {
                    final Double multiplier = unitMap.get(token.content().toLowerCase());
                    final String stringMultiplier = String.valueOf(requireNonNull(multiplier));
                    output.add(new Token(stringMultiplier, TokenType.FLOATING_NUMBER, position));
                } catch (NullPointerException e) {
                    throw new EvaluatorException("resource_amount_input.invalid_unit");
                }
                continue;
            }
            output.add(token);
        }
        return output;
    }

    public double evaluate() {
        return compute(nodes.getFirst());
    }

    private double compute(final Node operand) {
        switch (operand) {
            case BinOpNode(Node left, Node right, Token binOp) -> {
                return switch (binOp.content()) {
                    case "+" -> compute(left) + compute(right);
                    case "-" -> compute(left) - compute(right);
                    case "*" -> compute(left) * compute(right);
                    case "/" -> compute(left) / compute(right);
                    case "%" -> compute(left) % compute(right);
                    case "^" -> (Math.pow(compute(left), compute(right)));
                    default -> throw new EvaluatorException("Parser screwed up");
                };
            }
            case LiteralNode(Token token) -> {
                if (token.type() == TokenType.IDENTIFIER) {
                    throw new EvaluatorException("Parser screwed up");
                }
                return Double.parseDouble(token.content());
            }
            case ParenNode(List<Node> binOpNode) -> {
                return compute(binOpNode.getFirst());
            }
            default -> {
            }
        }
        throw new EvaluatorException("Parser screwed up");
    }
}
