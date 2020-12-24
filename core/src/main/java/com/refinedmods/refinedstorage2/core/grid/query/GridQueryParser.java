package com.refinedmods.refinedstorage2.core.grid.query;

import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.core.query.lexer.*;
import com.refinedmods.refinedstorage2.core.query.parser.*;
import com.refinedmods.refinedstorage2.core.query.parser.node.*;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class GridQueryParser<T> {
    public Predicate<GridStack<T>> parse(String query) throws GridQueryParserException {
        if ("".equals(query.trim())) {
            return stack -> true;
        }

        List<Token> tokens = getTokens(query);
        List<Node> nodes = getNodes(tokens);

        List<Predicate<GridStack<T>>> conditions = new ArrayList<>();
        for (Node node : nodes) {
            conditions.add(parseNode(node));
        }

        return and(conditions);
    }

    private List<Token> getTokens(String query) throws GridQueryParserException {
        try {
            Lexer lexer = new Lexer(new Source("Grid query input", query));
            lexer.registerTokenMapping("!", TokenType.UNARY_OP);
            lexer.registerTokenMapping("@", TokenType.UNARY_OP);
            lexer.registerTokenMapping("$", TokenType.UNARY_OP);
            lexer.registerTokenMapping(">", TokenType.UNARY_OP);
            lexer.registerTokenMapping(">=", TokenType.UNARY_OP);
            lexer.registerTokenMapping("<", TokenType.UNARY_OP);
            lexer.registerTokenMapping("<=", TokenType.UNARY_OP);
            lexer.registerTokenMapping("=", TokenType.UNARY_OP);
            lexer.registerTokenMapping("&&", TokenType.BIN_OP);
            lexer.registerTokenMapping("||", TokenType.BIN_OP);
            lexer.registerTokenMapping("(", TokenType.PAREN_OPEN);
            lexer.registerTokenMapping(")", TokenType.PAREN_CLOSE);
            lexer.scan();
            return lexer.getTokens();
        } catch (LexerException e) {
            throw new GridQueryParserException(e.getRange(), e.getMessage(), e);
        }
    }

    private List<Node> getNodes(List<Token> tokens) throws GridQueryParserException {
        try {
            Parser parser = new Parser(tokens);
            parser.registerBinaryOperator("||", new Operator(0, Associativity.LEFT));
            parser.registerBinaryOperator("&&", new Operator(1, Associativity.LEFT));

            parser.registerUnaryOperator("!", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator("@", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator("$", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator(">", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator(">=", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator("<", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator("<=", UnaryOperatorPosition.PREFIX);
            parser.registerUnaryOperator("=", UnaryOperatorPosition.PREFIX);

            parser.parse();
            return parser.getNodes();
        } catch (ParserException e) {
            throw new GridQueryParserException(e.getToken().getPosition().getRange(), e.getMessage(), e);
        }
    }

    private Predicate<GridStack<T>> parseNode(Node node) throws GridQueryParserException {
        if (node instanceof LiteralNode) {
            String content = ((LiteralNode) node).getToken().getContent();
            return name(content);
        } else if (node instanceof UnaryOpNode) {
            return parseUnaryOpNode((UnaryOpNode) node);
        } else if (node instanceof BinOpNode) {
            String operator = ((BinOpNode) node).getBinOp().getContent();

            if ("&&".equals(operator)) {
                return parseAndBinOpNode((BinOpNode) node);
            } else if ("||".equals(operator)) {
                return parseOrBinOpNode((BinOpNode) node);
            }
        } else if (node instanceof ParenNode) {
            return parseNode(((ParenNode) node).getNode());
        }

        throw new GridQueryParserException(node.getRange(), "Unsupported node", null);
    }

    private Predicate<GridStack<T>> parseOrBinOpNode(BinOpNode node) throws GridQueryParserException {
        return or(Arrays.asList(
            parseNode(node.getLeft()),
            parseNode(node.getRight())
        ));
    }

    private Predicate<GridStack<T>> parseAndBinOpNode(BinOpNode node) throws GridQueryParserException {
        return and(Arrays.asList(
            parseNode(node.getLeft()),
            parseNode(node.getRight())
        ));
    }

    private Predicate<GridStack<T>> parseUnaryOpNode(UnaryOpNode node) throws GridQueryParserException {
        String operator = node.getOperator().getContent();
        Node content = node.getNode();

        if ("!".equals(operator)) {
            return not(parseNode(content));
        } else if ("@".equals(operator)) {
            if (content instanceof LiteralNode) {
                return mod(((LiteralNode) content).getToken().getContent());
            } else {
                throw new GridQueryParserException(content.getRange(), "Mod filtering expects a literal", null);
            }
        } else if ("$".equals(operator)) {
            if (content instanceof LiteralNode) {
                return tag(((LiteralNode) content).getToken().getContent());
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
            return count(content, Integer::equals);
        } else {
            throw new GridQueryParserException(content.getRange(), "Unsupported unary operator", null);
        }
    }

    private Predicate<GridStack<T>> count(Node node, BiPredicate<Integer, Integer> predicate) throws GridQueryParserException {
        if (!(node instanceof LiteralNode)) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects a literal", null);
        }

        if (((LiteralNode) node).getToken().getType() != TokenType.INTEGER_NUMBER) {
            throw new GridQueryParserException(node.getRange(), "Count filtering expects an integer number", null);
        }

        int wantedCount = Integer.parseInt(((LiteralNode) node).getToken().getContent());

        return stack -> predicate.test(((ItemStack) stack.getStack()).getCount(), wantedCount);
    }

    private Predicate<GridStack<T>> mod(String name) {
        return stack -> stack.getModName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT))
            || stack.getModId().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private Predicate<GridStack<T>> tag(String name) {
        return stack -> stack.getTags()
            .stream()
            .anyMatch(tag -> tag.trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT)));
    }

    private Predicate<GridStack<T>> name(String name) {
        return stack -> stack.getName().trim().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private Predicate<GridStack<T>> and(List<Predicate<GridStack<T>>> predicates) {
        return (stack) -> {
            for (Predicate<GridStack<T>> predicate : predicates) {
                if (!predicate.test(stack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private Predicate<GridStack<T>> or(List<Predicate<GridStack<T>>> predicates) {
        return (stack) -> {
            for (Predicate<GridStack<T>> predicate : predicates) {
                if (predicate.test(stack)) {
                    return true;
                }
            }
            return false;
        };
    }

    private Predicate<GridStack<T>> not(Predicate<GridStack<T>> predicate) {
        return (stack) -> !predicate.test(stack);
    }
}
