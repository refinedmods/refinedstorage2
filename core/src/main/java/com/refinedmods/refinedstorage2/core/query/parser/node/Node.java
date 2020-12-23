package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public interface Node {
    TokenRange getRange();
}
