package com.refinedmods.refinedstorage2.core.grid.query;

import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.core.query.lexer.Lexer;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerException;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.lexer.Source;
import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.core.query.parser.Parser;
import com.refinedmods.refinedstorage2.core.query.parser.ParserException;
import com.refinedmods.refinedstorage2.core.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.core.query.parser.node.BinOpNode;
import com.refinedmods.refinedstorage2.core.query.parser.node.LiteralNode;
import com.refinedmods.refinedstorage2.core.query.parser.node.Node;
import com.refinedmods.refinedstorage2.core.query.parser.node.ParenNode;
import com.refinedmods.refinedstorage2.core.query.parser.node.UnaryOpNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class GridQueryParserImpl implements GridQueryParser {
    private final LexerTokenMappings tokenMappings;
    private final ParserOperatorMappings operatorMappings;

    public GridQueryParserImpl(LexerTokenMappings tokenMappings, ParserOperatorMappings operatorMappings) {
        this.tokenMappings = tokenMappings;
        this.operatorMappings = operatorMappings;
    }

    private static Predicate<GridStack<?>> implicitAnd(List<Node> nodes) throws GridQueryParserException {
        List<Predicate<GridStack<?>>> conditions = new ArrayList<>();
        for (Node node : nodes) {
            conditions.add(parseNode(node));
        }
        return and(conditions);
    }

    private static Predicate<GridStack<?>> parseNode(Node node) throws GridQueryParserException {
        if (node instanceof LiteralNode literalNode) {
            String content = literalNode.getToken().getContent();
            return name(content);
        } else if (node instanceof UnaryOpNode unaryOpNode) {
            return parseUnaryOpNode(unaryOpNode);
        } else if (node instanceof BinOpNode binOpNode) {
            String operator = binOpNode.getBinOp().getContent();

            if ("&&".equals(operator)) {
                return parseAndBinOpNode(binOpNode);
            } else if ("||".equals(operator)) {
                return parseOrBinOpNode(binOpNode);
            }
        } else if (node instanceof ParenNode parenNode) {
            return implicitAnd(parenNode.getNodes());
        }

        throw new GridQueryParserException(node.getRange(), "Unsupported node", null);
    }

    private static Predicate<GridStack<?>> parseOrBinOpNode(BinOpNode node) throws GridQueryParserException {
        return or(Arrays.asList(
                parseNode(node.getLeft()),
                parseNode(node.getRight())
        ));
    }

    private static Predicate<GridStack<?>> parseAndBinOpNode(BinOpNode node) throws GridQueryParserException {
        return and(Arrays.asList(
                parseNode(node.getLeft()),
                parseNode(node.getRight())
        ));
    }

    private static Predicate<GridStack<?>> parseUnaryOpNode(UnaryOpNode node) throws GridQueryParserException {
        String operator = node.getOperator().getContent();
        Node content = node.getNode();

        if ("!".equals(operator)) {
            return not(parseNode(content));
        } else if ("@".equals(operator)) {
            if (content instanceof LiteralNode literalNode) {
                return mod(literalNode.getToken().getContent());
            } else {
                throw new GridQueryParserException(content.getRange(), "Mod filtering expects a literal", null);
            }
        } else if ("$".equals(operator)) {
            if (content instanceof LiteralNode literalNode) {
                return tag(literalNode.getToken().getContent());
            } else {
                throw new GridQueryParserException(content.getRange(), "Tag filtering expects a literal", null);
            }
        } else if (">".equals(operator)) {
            return count(content, (actualCount, wantedCount) -> actualCount > wantedCount);
        } else if (">=".equals(operator)) {
            return count(content, (actualCount, wantedCount) -> actualCount >= wantedCount);
        } else if ("<".equals(operator)) {
            return count(content, (actualCount, wantedCount) -> actualCount < wantedCount);
        } else if ("<=".equals(operator)) {
            return count(content, (actualCount, wantedCount) -> actualCount <= wantedCount);
        } else if ("=".equals(operator)) {
            return count(content, Long::equals);
        } else {
            throw new GridQueryParserException(content.getRange(), "Unsupported unary operator", null);
        }
    }

    private static Predicate<GridStack<?>> count(Node node, BiPredicate<Long, Long> predicate) throws GridQueryParserException {
        if (!(node instanceof LiteralNode)) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects a literal", null);
        }

        if (((LiteralNode) node).getToken().getType() != TokenType.INTEGER_NUMBER) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects an integer number", null);
        }

        long wantedCount = Long.parseLong(((LiteralNode) node).getToken().getContent());

        return stack -> predicate.test(stack.getAmount(), wantedCount);
    }

    private static Predicate<GridStack<?>> mod(String name) {
        return stack -> stack.getModName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT))
                || stack.getModId().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private static Predicate<GridStack<?>> tag(String name) {
        return stack -> stack.getTags()
                .stream()
                .anyMatch(tag -> tag.trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT)));
    }

    private static Predicate<GridStack<?>> name(String name) {
        return stack -> stack.getName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private static Predicate<GridStack<?>> and(List<Predicate<GridStack<?>>> predicates) {
        return stack -> {
            for (Predicate<GridStack<?>> predicate : predicates) {
                if (!predicate.test(stack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Predicate<GridStack<?>> or(List<Predicate<GridStack<?>>> predicates) {
        return stack -> {
            for (Predicate<GridStack<?>> predicate : predicates) {
                if (predicate.test(stack)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Predicate<GridStack<?>> not(Predicate<GridStack<?>> predicate) {
        return stack -> !predicate.test(stack);
    }

    @Override
    public Predicate<GridStack<?>> parse(String query) throws GridQueryParserException {
        if ("".equals(query.trim())) {
            return stack -> true;
        }

        List<Token> tokens = getTokens(query);
        List<Node> nodes = getNodes(tokens);

        return implicitAnd(nodes);
    }

    private List<Token> getTokens(String query) throws GridQueryParserException {
        try {
            Lexer lexer = new Lexer(new Source("Grid query input", query), tokenMappings);
            lexer.scan();
            return lexer.getTokens();
        } catch (LexerException e) {
            throw new GridQueryParserException(e.getRange(), e.getMessage(), e);
        }
    }

    private List<Node> getNodes(List<Token> tokens) throws GridQueryParserException {
        try {
            Parser parser = new Parser(tokens, operatorMappings);
            parser.parse();
            return parser.getNodes();
        } catch (ParserException e) {
            throw new GridQueryParserException(e.getToken().getPosition().getRange(), e.getMessage(), e);
        }
    }
}
