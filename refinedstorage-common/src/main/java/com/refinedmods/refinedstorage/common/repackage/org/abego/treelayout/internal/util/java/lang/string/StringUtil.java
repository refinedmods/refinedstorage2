/*
 * [The "BSD license"]
 * Copyright (c) 2011, abego Software GmbH, Germany (http://www.abego.org)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the abego Software GmbH nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.internal.util.java.lang.string;

public class StringUtil {
    /**
     * Returns a quoted version of a given string, i.e. as a Java String
     * Literal.
     *
     * @param s          [nullable] the string to quote
     * @param nullResult [default="null"] the String to be returned for null values.
     * @return the nullResult when s is null, otherwise s as a quoted string
     * (i.e. Java String Literal)
     *
     */
    public static String quote(String s, String nullResult) {
        if (s == null) {
            return nullResult;
        }
        StringBuffer result = new StringBuffer();
        result.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\b': {
                    result.append("\\b");
                    break;
                }
                case '\f': {
                    result.append("\\f");
                    break;
                }
                case '\n': {
                    result.append("\\n");
                    break;
                }
                case '\r': {
                    result.append("\\r");
                    break;
                }
                case '\t': {
                    result.append("\\t");
                    break;
                }
                case '\\': {
                    result.append("\\\\");
                    break;
                }
                case '"': {
                    result.append("\\\"");
                    break;
                }
                default: {
                    if (c < ' ' || c >= '\u0080') {
                        String n = Integer.toHexString(c);
                        result.append("\\u");
                        result.append("0000".substring(n.length()));
                        result.append(n);
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        result.append('"');
        return result.toString();
    }

    /**
     * see {@link #quote(String, String)}
     *
     * @param s the string to quote
     * @return nullable
     */
    public static String quote(String s) {
        return quote(s, "null");
    }
}
