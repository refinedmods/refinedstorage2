package com.refinedmods.refinedstorage2.api.grid.search.query;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
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
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridQueryParserImpl implements GridQueryParser {
    private final LexerTokenMappings tokenMappings;
    private final ParserOperatorMappings operatorMappings;

    public GridQueryParserImpl(LexerTokenMappings tokenMappings, ParserOperatorMappings operatorMappings) {
        this.tokenMappings = tokenMappings;
        this.operatorMappings = operatorMappings;
    }

    private static Predicate<GridResource<?>> implicitAnd(List<Node> nodes) throws GridQueryParserException {
        List<Predicate<GridResource<?>>> conditions = new ArrayList<>();
        for (Node node : nodes) {
            conditions.add(parseNode(node));
        }
        return and(conditions);
    }

    private static Predicate<GridResource<?>> parseNode(Node node) throws GridQueryParserException {
        if (node instanceof LiteralNode literalNode) {
            String content = literalNode.token().content();
            return name(content);
        } else if (node instanceof UnaryOpNode unaryOpNode) {
            return parseUnaryOpNode(unaryOpNode);
        } else if (node instanceof BinOpNode binOpNode) {
            String operator = binOpNode.binOp().content();

            if ("&&".equals(operator)) {
                return parseAndBinOpNode(binOpNode);
            } else if ("||".equals(operator)) {
                return parseOrBinOpNode(binOpNode);
            }
        } else if (node instanceof ParenNode parenNode) {
            return implicitAnd(parenNode.nodes());
        }

        throw new GridQueryParserException(node.getRange(), "Unsupported node", null);
    }

    private static Predicate<GridResource<?>> parseOrBinOpNode(BinOpNode node) throws GridQueryParserException {
        return or(Arrays.asList(
                parseNode(node.left()),
                parseNode(node.right())
        ));
    }

    private static Predicate<GridResource<?>> parseAndBinOpNode(BinOpNode node) throws GridQueryParserException {
        return and(Arrays.asList(
                parseNode(node.left()),
                parseNode(node.right())
        ));
    }

    private static Predicate<GridResource<?>> parseUnaryOpNode(UnaryOpNode node) throws GridQueryParserException {
        String operator = node.operator().content();
        Node content = node.node();

        if ("!".equals(operator)) {
            return not(parseNode(content));
        } else if ("@".equals(operator)) {
            if (content instanceof LiteralNode literalNode) {
                return mod(literalNode.token().content());
            } else {
                throw new GridQueryParserException(content.getRange(), "Mod filtering expects a literal", null);
            }
        } else if ("$".equals(operator)) {
            if (content instanceof LiteralNode literalNode) {
                return tag(literalNode.token().content());
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

    private static Predicate<GridResource<?>> count(Node node, BiPredicate<Long, Long> predicate) throws GridQueryParserException {
        if (!(node instanceof LiteralNode)) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects a literal", null);
        }

        if (((LiteralNode) node).token().type() != TokenType.INTEGER_NUMBER) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects an integer number", null);
        }

        long wantedCount = Long.parseLong(((LiteralNode) node).token().content());

        return resource -> predicate.test(resource.getResourceAmount().getAmount(), wantedCount);
    }

    private static Predicate<GridResource<?>> mod(String name) {
        return resource -> resource.getModName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT))
                || resource.getModId().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private static Predicate<GridResource<?>> tag(String name) {
        return resource -> resource.getTags()
                .stream()
                .anyMatch(tag -> tag.trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT)));
    }

    private static Predicate<GridResource<?>> name(String name) {
        return resource -> resource.getName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private static Predicate<GridResource<?>> and(List<Predicate<GridResource<?>>> predicates) {
        return resource -> {
            for (Predicate<GridResource<?>> predicate : predicates) {
                if (!predicate.test(resource)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Predicate<GridResource<?>> or(List<Predicate<GridResource<?>>> predicates) {
        return resource -> {
            for (Predicate<GridResource<?>> predicate : predicates) {
                if (predicate.test(resource)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Predicate<GridResource<?>> not(Predicate<GridResource<?>> predicate) {
        return resource -> !predicate.test(resource);
    }

    @Override
    public Predicate<GridResource<?>> parse(String query) throws GridQueryParserException {
        if ("".equals(query.trim())) {
            return resource -> true;
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
            throw new GridQueryParserException(e.getToken().position().range(), e.getMessage(), e);
        }
    }
}
