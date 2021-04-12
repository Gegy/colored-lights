package dev.gegy.colored_lights.render.shader;

import org.apache.commons.lang3.CharUtils;

import java.util.function.Predicate;

public interface ShaderSourcePatcher {
    ShaderSourcePatcher NO = source -> source;

    String apply(String source);

    static ShaderSourcePatcher insertDeclarations(String... declarations) {
        return insertAfter(s -> s.startsWith("#version"), declarations);
    }

    static ShaderSourcePatcher insertBefore(Predicate<String> match, String... insert) {
        return new Insert(match, Insert.Shift.BEFORE, insert);
    }

    static ShaderSourcePatcher insertAfter(Predicate<String> match, String... insert) {
        return new Insert(match, Insert.Shift.AFTER, insert);
    }

    static ShaderSourcePatcher wrapCall(String targetFunction, String wrapperFunction, String... additionalArguments) {
        return new ShaderSourcePatcher.WrapCall(targetFunction, wrapperFunction, additionalArguments);
    }

    final class Insert implements ShaderSourcePatcher {
        private final Predicate<String> match;
        private final Shift shift;
        private final String[] insert;

        Insert(Predicate<String> match, Shift shift, String... insert) {
            this.match = match;
            this.shift = shift;
            this.insert = insert;
        }

        @Override
        public String apply(String source) {
            StringBuilder result = new StringBuilder(source.length());

            String[] lines = source.split("\n");
            for (String line : lines) {
                if (this.shift == Shift.AFTER) {
                    result.append(line).append("\n");
                }

                if (this.match.test(line)) {
                    for (String insert : this.insert) {
                        result.append(insert).append("\n");
                    }
                }

                if (this.shift == Shift.BEFORE) {
                    result.append(line).append("\n");
                }
            }

            return result.toString();
        }

        enum Shift {
            BEFORE,
            AFTER,
        }
    }

    final class WrapCall implements ShaderSourcePatcher {
        private final String targetFunction;
        private final String wrapperFunction;
        private final String[] additionalArguments;

        WrapCall(String targetFunction, String wrapperFunction, String... additionalArguments) {
            this.targetFunction = targetFunction;
            this.wrapperFunction = wrapperFunction;
            this.additionalArguments = additionalArguments;
        }

        @Override
        public String apply(String source) {
            int startIdx = 0;
            while (true) {
                int functionStartIdx = this.findTargetFunctionStart(source, startIdx);
                if (functionStartIdx == -1) {
                    return source;
                }

                int functionEndIdx = this.findParenthesisExit(source, functionStartIdx, 0);
                if (functionEndIdx != -1) {
                    int wrapperLength = this.wrapperFunction.length() + 2;

                    StringBuilder wrappedCall = new StringBuilder(this.wrapperFunction + "("
                            + source.substring(functionStartIdx, functionEndIdx + 1));

                    if (this.additionalArguments.length > 0) {
                        for (String argument : this.additionalArguments) {
                            wrappedCall.append(", ").append(argument);
                            wrapperLength += 2 + argument.length();
                        }
                        wrappedCall.append(")");
                    }

                    source = source.substring(0, functionStartIdx) + wrappedCall + source.substring(functionEndIdx + 1);

                    startIdx = functionEndIdx + wrapperLength + 1;
                } else {
                    startIdx = functionStartIdx + 1;
                }
            }
        }

        private int findTargetFunctionStart(String source, int fromIdx) {
            StringBuilder word = new StringBuilder();

            for (int idx = fromIdx; idx < source.length(); idx++) {
                char c = source.charAt(idx);

                if (CharUtils.isAsciiAlpha(c) || c == '_') {
                    word.append(c);
                } else if (word.length() > 0) {
                    if (word.toString().equals(this.targetFunction)) {
                        return idx - this.targetFunction.length();
                    }
                    word.setLength(0);
                }
            }

            return -1;
        }

        private int findParenthesisExit(String source, int fromIdx, int startLevel) {
            int level = startLevel;
            for (int idx = fromIdx; idx < source.length(); idx++) {
                char c = source.charAt(idx);
                if (c == '(') {
                    ++level;
                } else if (c == ')') {
                    if (--level == 0) {
                        return idx;
                    }
                }
            }
            return -1;
        }
    }
}
