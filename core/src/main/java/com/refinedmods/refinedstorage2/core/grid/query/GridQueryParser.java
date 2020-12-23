package com.refinedmods.refinedstorage2.core.grid.query;

import com.refinedmods.refinedstorage2.core.query.lexer.Lexer;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerException;
import com.refinedmods.refinedstorage2.core.query.lexer.Source;
import com.refinedmods.refinedstorage2.core.query.parser.Parser;
import com.refinedmods.refinedstorage2.core.query.parser.ParserException;
import com.refinedmods.refinedstorage2.core.query.parser.node.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class GridQueryParser<T> {
    private final GridStackDetailsProvider<T> detailsProvider;

    public GridQueryParser(GridStackDetailsProvider<T> detailsProvider) {
        this.detailsProvider = detailsProvider;
    }

    public Predicate<T> parse(String query) throws GridQueryParserException {
        if ("".equals(query.trim())) {
            return (stack) -> true;
        }

        Lexer lexer;
        try {
            lexer = new Lexer(new Source("Grid query input", query));
            lexer.scan();
        } catch (LexerException e) {
            throw new GridQueryParserException(e.getRange(), e.getMessage(), e);
        }

        Parser parser;
        try {
            parser = new Parser(lexer.getTokens());
            parser.parse();
        } catch (ParserException e) {
            throw new GridQueryParserException(e.getToken().getPosition().getRange(), e.getMessage(), e);
        }

        List<Predicate<T>> predicates = new ArrayList<>();
        for (Node node : parser.getNodes()) {
            predicates.add(parseNode(node));
        }

        return and(predicates);
    }

    private Predicate<T> parseNode(Node node) throws GridQueryParserException {
        if (node instanceof LiteralNode) {
            String content = ((LiteralNode) node).getToken().getContent();
            return name(content);
        } else if (node instanceof UnaryOpNode) {
            String operator = ((UnaryOpNode) node).getOperator().getContent();
            Node nodeInUnaryOp = ((UnaryOpNode) node).getNode();

            if ("!".equals(operator)) {
                return not(parseNode(nodeInUnaryOp));
            } else if ("@".equals(operator)) {
                if (nodeInUnaryOp instanceof LiteralNode) {
                    return mod(((LiteralNode) nodeInUnaryOp).getToken().getContent());
                } else {
                    throw new GridQueryParserException(nodeInUnaryOp.getRange(), "Mod filtering expects a literal", null);
                }
            }
        } else if (node instanceof BinOpNode) {
            String operator = ((BinOpNode) node).getBinOp().getContent();

            if ("&&".equals(operator)) {
                return and(Arrays.asList(
                    parseNode(((BinOpNode) node).getLeft()),
                    parseNode(((BinOpNode) node).getRight())
                ));
            } else if ("||".equals(operator)) {
                return or(Arrays.asList(
                    parseNode(((BinOpNode) node).getLeft()),
                    parseNode(((BinOpNode) node).getRight())
                ));
            }
        } else if (node instanceof ParenNode) {
            return parseNode(((ParenNode) node).getNode());
        }

        return (stack) -> false;
    }

    private Predicate<T> mod(String name) {
        return (stack) -> {
            GridStackDetails details = detailsProvider.getDetails(stack);

            return details.getModName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT))
                || details.getModId().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
        };
    }

    private Predicate<T> name(String name) {
        return (stack) -> detailsProvider.getDetails(stack).getName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private Predicate<T> and(List<Predicate<T>> predicates) {
        return (stack) -> {
            for (Predicate<T> predicate : predicates) {
                if (!predicate.test(stack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private Predicate<T> or(List<Predicate<T>> predicates) {
        return (stack) -> {
            for (Predicate<T> predicate : predicates) {
                if (predicate.test(stack)) {
                    return true;
                }
            }
            return false;
        };
    }

    private Predicate<T> not(Predicate<T> predicate) {
        return (stack) -> !predicate.test(stack);
    }
}
