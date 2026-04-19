package com.refinedmods.refinedstorage.common.grid.query;

import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryFilter;
import com.refinedmods.refinedstorage.common.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.query.lexer.Lexer;
import com.refinedmods.refinedstorage.query.lexer.LexerException;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.lexer.Source;
import com.refinedmods.refinedstorage.query.lexer.Token;
import com.refinedmods.refinedstorage.query.lexer.TokenType;
import com.refinedmods.refinedstorage.query.parser.Parser;
import com.refinedmods.refinedstorage.query.parser.ParserException;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage.query.parser.node.BinOpNode;
import com.refinedmods.refinedstorage.query.parser.node.LiteralNode;
import com.refinedmods.refinedstorage.query.parser.node.Node;
import com.refinedmods.refinedstorage.query.parser.node.ParenNode;
import com.refinedmods.refinedstorage.query.parser.node.UnaryOpNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class GridQueryParser {
    private static final Map<String, Set<GridResourceAttributeKey>> ATTRIBUTE_MAPPING = Map.of(
        "@", Set.of(GridResourceAttributeKeys.MOD_ID, GridResourceAttributeKeys.MOD_NAME),
        "#", Set.of(GridResourceAttributeKeys.TAGS),
        "$", Set.of(GridResourceAttributeKeys.TOOLTIP)
    );

    private final LexerTokenMappings tokenMappings;
    private final ParserOperatorMappings operatorMappings;

    public GridQueryParser(final LexerTokenMappings tokenMappings, final ParserOperatorMappings operatorMappings) {
        this.tokenMappings = tokenMappings;
        this.operatorMappings = operatorMappings;
    }

    public ResourceRepositoryFilter<GridResource> parse(final String query) throws GridQueryParserException {
        if (query.trim().isEmpty()) {
            return (repository, resource) -> true;
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

    private ResourceRepositoryFilter<GridResource> implicitAnd(final List<Node> nodes)
        throws GridQueryParserException {
        final List<ResourceRepositoryFilter<GridResource>> conditions = new ArrayList<>();
        for (final Node node : nodes) {
            conditions.add(parseNode(node));
        }
        return and(conditions);
    }

    private ResourceRepositoryFilter<GridResource> parseNode(final Node node) throws GridQueryParserException {
        return switch (node) {
            case LiteralNode literalNode -> parseLiteral(literalNode);
            case UnaryOpNode unaryOpNode -> parseUnaryOp(unaryOpNode);
            case BinOpNode binOpNode -> parseBinOp(binOpNode);
            case ParenNode parenNode -> implicitAnd(parenNode.nodes());
            default -> throw new GridQueryParserException("Unsupported node", null);
        };
    }

    private ResourceRepositoryFilter<GridResource> parseBinOp(final BinOpNode node)
        throws GridQueryParserException {
        final String operator = node.binOp().content();
        if ("&&".equals(operator)) {
            return parseAndBinOpNode(node);
        } else if ("||".equals(operator)) {
            return parseOrBinOpNode(node);
        } else {
            throw new GridQueryParserException("Unsupported operator: " + operator, null);
        }
    }

    private ResourceRepositoryFilter<GridResource> parseAndBinOpNode(final BinOpNode node)
        throws GridQueryParserException {
        return and(Arrays.asList(
            parseNode(node.left()),
            parseNode(node.right())
        ));
    }

    private ResourceRepositoryFilter<GridResource> parseOrBinOpNode(final BinOpNode node)
        throws GridQueryParserException {
        return or(Arrays.asList(
            parseNode(node.left()),
            parseNode(node.right())
        ));
    }

    private ResourceRepositoryFilter<GridResource> parseUnaryOp(final UnaryOpNode node)
        throws GridQueryParserException {
        final String operator = node.operator().content();
        final Node content = node.node();
        final ResourceRepositoryFilter<GridResource> predicate;
        if ("!".equals(operator)) {
            predicate = not(parseNode(content));
        } else if (ATTRIBUTE_MAPPING.containsKey(operator)) {
            final Set<GridResourceAttributeKey> keys = ATTRIBUTE_MAPPING.get(operator);
            if (content instanceof LiteralNode(Token token)) {
                predicate = attributeMatch(keys, token.content());
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

    private static ResourceRepositoryFilter<GridResource> count(final Node node,
                                                                final BiPredicate<Long, Long> predicate)
        throws GridQueryParserException {
        if (!(node instanceof LiteralNode)) {
            throw new GridQueryParserException("Count filtering expects a literal", null);
        }
        if (((LiteralNode) node).token().type() != TokenType.INTEGER_NUMBER) {
            throw new GridQueryParserException("Count filtering expects an integer number", null);
        }
        final long wantedCount = Long.parseLong(((LiteralNode) node).token().content());
        return (repository, resource) -> predicate.test(resource.getAmount(repository), wantedCount);
    }

    private static ResourceRepositoryFilter<GridResource> attributeMatch(
        final Set<GridResourceAttributeKey> keys,
        final String query
    ) {
        return (repository, resource) -> keys
            .stream()
            .map(resource::getAttribute)
            .flatMap(Collection::stream)
            .anyMatch(value -> normalize(value).contains(normalize(query)));
    }

    private static String normalize(final String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static ResourceRepositoryFilter<GridResource> parseLiteral(final LiteralNode node) {
        return (repository, resource) ->
            resource.getSearchableNames().stream().anyMatch(name ->
                normalize(name).contains(normalize(node.token().content())));
    }

    private static ResourceRepositoryFilter<GridResource> and(
        final List<ResourceRepositoryFilter<GridResource>> chain
    ) {
        return (repository, resource) -> {
            for (final ResourceRepositoryFilter<GridResource> predicate : chain) {
                if (!predicate.test(repository, resource)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static ResourceRepositoryFilter<GridResource> or(final List<ResourceRepositoryFilter<GridResource>> chain) {
        return (repository, resource) -> {
            for (final ResourceRepositoryFilter<GridResource> predicate : chain) {
                if (predicate.test(repository, resource)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static ResourceRepositoryFilter<GridResource> not(final ResourceRepositoryFilter<GridResource> predicate) {
        return (repository, resource) -> !predicate.test(repository, resource);
    }
}
