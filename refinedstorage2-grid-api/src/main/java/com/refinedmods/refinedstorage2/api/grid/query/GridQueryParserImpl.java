package com.refinedmods.refinedstorage2.api.grid.query;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage2.query.lexer.Lexer;
import com.refinedmods.refinedstorage2.query.lexer.LexerException;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.lexer.Source;
import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.query.parser.Parser;
import com.refinedmods.refinedstorage2.query.parser.ParserException;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.query.parser.node.BinOpNode;
import com.refinedmods.refinedstorage2.query.parser.node.LiteralNode;
import com.refinedmods.refinedstorage2.query.parser.node.Node;
import com.refinedmods.refinedstorage2.query.parser.node.ParenNode;
import com.refinedmods.refinedstorage2.query.parser.node.UnaryOpNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridQueryParserImpl implements GridQueryParser {
    private final LexerTokenMappings tokenMappings;
    private final ParserOperatorMappings operatorMappings;
    private final Map<String, Set<GridResourceAttributeKey>> unaryOperatorToAttributeKeyMapping;

    public GridQueryParserImpl(final LexerTokenMappings tokenMappings,
                               final ParserOperatorMappings operatorMappings,
                               final Map<String, Set<GridResourceAttributeKey>> unaryOperatorToAttributeKeyMapping) {
        this.tokenMappings = tokenMappings;
        this.operatorMappings = operatorMappings;
        this.unaryOperatorToAttributeKeyMapping = unaryOperatorToAttributeKeyMapping;
    }

    @Override
    public Predicate<AbstractGridResource> parse(final String query) throws GridQueryParserException {
        if ("".equals(query.trim())) {
            return resource -> true;
        }
        final List<Token> tokens = getTokens(query);
        final List<Node> nodes = getNodes(tokens);
        return implicitAnd(nodes);
    }

    private List<Token> getTokens(final String query) throws GridQueryParserException {
        try {
            final Lexer lexer = new Lexer(new Source("Grid query input", query), tokenMappings);
            lexer.scan();
            return lexer.getTokens();
        } catch (LexerException e) {
            throw new GridQueryParserException(e.getMessage(), e);
        }
    }

    private List<Node> getNodes(final List<Token> tokens) throws GridQueryParserException {
        try {
            final Parser parser = new Parser(tokens, operatorMappings);
            parser.parse();
            return parser.getNodes();
        } catch (ParserException e) {
            throw new GridQueryParserException(e.getMessage(), e);
        }
    }

    private Predicate<AbstractGridResource> implicitAnd(final List<Node> nodes) throws GridQueryParserException {
        final List<Predicate<AbstractGridResource>> conditions = new ArrayList<>();
        for (final Node node : nodes) {
            conditions.add(parseNode(node));
        }
        return and(conditions);
    }

    private Predicate<AbstractGridResource> parseNode(final Node node) throws GridQueryParserException {
        if (node instanceof LiteralNode literalNode) {
            return parseLiteral(literalNode);
        } else if (node instanceof UnaryOpNode unaryOpNode) {
            return parseUnaryOp(unaryOpNode);
        } else if (node instanceof BinOpNode binOpNode) {
            return parseBinOp(binOpNode);
        } else if (node instanceof ParenNode parenNode) {
            return implicitAnd(parenNode.nodes());
        }
        throw new GridQueryParserException("Unsupported node", null);
    }

    private Predicate<AbstractGridResource> parseBinOp(final BinOpNode node) throws GridQueryParserException {
        final String operator = node.binOp().content();
        if ("&&".equals(operator)) {
            return parseAndBinOpNode(node);
        } else if ("||".equals(operator)) {
            return parseOrBinOpNode(node);
        } else {
            throw new GridQueryParserException("Unsupported operator: " + operator, null);
        }
    }

    private Predicate<AbstractGridResource> parseAndBinOpNode(final BinOpNode node) throws GridQueryParserException {
        return and(Arrays.asList(
            parseNode(node.left()),
            parseNode(node.right())
        ));
    }

    private Predicate<AbstractGridResource> parseOrBinOpNode(final BinOpNode node) throws GridQueryParserException {
        return or(Arrays.asList(
            parseNode(node.left()),
            parseNode(node.right())
        ));
    }

    private Predicate<AbstractGridResource> parseUnaryOp(final UnaryOpNode node) throws GridQueryParserException {
        final String operator = node.operator().content();
        final Node content = node.node();
        final Predicate<AbstractGridResource> predicate;

        if ("!".equals(operator)) {
            predicate = not(parseNode(content));
        } else if (unaryOperatorToAttributeKeyMapping.containsKey(operator)) {
            final Set<GridResourceAttributeKey> keys = unaryOperatorToAttributeKeyMapping.get(operator);
            if (content instanceof LiteralNode literalNode) {
                predicate = attributeMatch(keys, literalNode.token().content());
            } else {
                throw new GridQueryParserException("Expected a literal", null);
            }
        } else if (">".equals(operator)) {
            predicate = count(content, (actualCount, wantedCount) -> actualCount > wantedCount);
        } else if (">=".equals(operator)) {
            predicate = count(content, (actualCount, wantedCount) -> actualCount >= wantedCount);
        } else if ("<".equals(operator)) {
            predicate = count(content, (actualCount, wantedCount) -> actualCount < wantedCount);
        } else if ("<=".equals(operator)) {
            predicate = count(content, (actualCount, wantedCount) -> actualCount <= wantedCount);
        } else if ("=".equals(operator)) {
            predicate = count(content, Long::equals);
        } else {
            throw new GridQueryParserException("Unsupported unary operator", null);
        }
        return predicate;
    }

    private static Predicate<AbstractGridResource> count(final Node node,
                                                         final BiPredicate<Long, Long> predicate)
        throws GridQueryParserException {
        if (!(node instanceof LiteralNode)) {
            throw new GridQueryParserException("Count filtering expects a literal", null);
        }

        if (((LiteralNode) node).token().type() != TokenType.INTEGER_NUMBER) {
            throw new GridQueryParserException("Count filtering expects an integer number", null);
        }

        final long wantedCount = Long.parseLong(((LiteralNode) node).token().content());

        return resource -> predicate.test(resource.getResourceAmount().getAmount(), wantedCount);
    }

    private static Predicate<AbstractGridResource> attributeMatch(final Set<GridResourceAttributeKey> keys,
                                                                  final String query) {
        return resource -> keys
            .stream()
            .map(resource::getAttribute)
            .flatMap(Collection::stream)
            .anyMatch(value -> normalize(value).contains(normalize(query)));
    }

    private static String normalize(final String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static Predicate<AbstractGridResource> parseLiteral(final LiteralNode node) {
        return resource -> normalize(resource.getName()).contains(normalize(node.token().content()));
    }

    private static Predicate<AbstractGridResource> and(final List<Predicate<AbstractGridResource>> chain) {
        return resource -> {
            for (final Predicate<AbstractGridResource> predicate : chain) {
                if (!predicate.test(resource)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Predicate<AbstractGridResource> or(final List<Predicate<AbstractGridResource>> chain) {
        return resource -> {
            for (final Predicate<AbstractGridResource> predicate : chain) {
                if (predicate.test(resource)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Predicate<AbstractGridResource> not(final Predicate<AbstractGridResource> predicate) {
        return resource -> !predicate.test(resource);
    }
}
