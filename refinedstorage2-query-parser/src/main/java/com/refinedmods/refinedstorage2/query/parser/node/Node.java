package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public interface Node {
    // TODO - Add tests for these ranges.
    TokenRange getRange();
}
