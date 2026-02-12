package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.RegexUseCase;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Stateless domain service implementing regex operations.
 * Pure Java with zero Spring dependencies.
 */
public class RegexService implements RegexUseCase {

    private static final Map<String, String> COMMON_PATTERNS = Map.ofEntries(
            Map.entry("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"),
            Map.entry("url", "^https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})(?:/[^\\s]*)?$"),
            Map.entry("ipv4", "^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"),
            Map.entry("ipv6", "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"),
            Map.entry("phone", "^\\+?\\d{1,4}[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,9}(?:[-.\\s]?\\d{1,9})*$"),
            Map.entry("date", "^\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\\d|3[01])$"),
            Map.entry("time", "^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$"),
            Map.entry("hex-color", "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$"),
            Map.entry("uuid", "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"),
            Map.entry("credit-card", "^(?:4\\d{12}(?:\\d{3})?|5[1-5]\\d{14}|3[47]\\d{13}|3(?:0[0-5]|[68]\\d)\\d{11}|6(?:011|5\\d{2})\\d{12})$")
    );

    @Override
    public Map<String, Object> testRegex(String pattern, List<String> testStrings, String flags) {
        var result = new LinkedHashMap<String, Object>();
        result.put("pattern", pattern);
        result.put("flags", flags != null ? flags : "");

        try {
            int javaFlags = parseFlags(flags);
            var compiled = Pattern.compile(pattern, javaFlags);

            var results = new ArrayList<Map<String, Object>>();
            for (var testString : testStrings) {
                var entry = new LinkedHashMap<String, Object>();
                entry.put("input", testString);

                var matcher = compiled.matcher(testString);
                boolean matches = matcher.matches();
                entry.put("matches", matches);

                if (matches) {
                    entry.put("fullMatch", matcher.group(0));
                    var groups = new ArrayList<String>();
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        groups.add(matcher.group(i));
                    }
                    entry.put("matchedGroups", groups);
                } else {
                    entry.put("fullMatch", null);
                    entry.put("matchedGroups", List.of());
                }

                results.add(entry);
            }

            result.put("results", results);
            result.put("valid", true);
        } catch (PatternSyntaxException e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            result.put("results", List.of());
        }

        return result;
    }

    @Override
    public Map<String, Object> explainRegex(String pattern) {
        var result = new LinkedHashMap<String, Object>();
        result.put("pattern", pattern);

        try {
            Pattern.compile(pattern);
            result.put("valid", true);

            var components = new ArrayList<Map<String, String>>();
            int i = 0;
            while (i < pattern.length()) {
                i = parseComponent(pattern, i, components);
            }

            result.put("components", components);
        } catch (PatternSyntaxException e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            result.put("components", List.of());
        }

        return result;
    }

    @Override
    public Map<String, Object> buildRegex(String description) {
        var result = new LinkedHashMap<String, Object>();
        result.put("description", description);

        var normalized = description.toLowerCase().trim();

        String matchedPattern = null;
        String matchedKey = null;

        for (var entry : COMMON_PATTERNS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                matchedPattern = entry.getValue();
                matchedKey = entry.getKey();
                break;
            }
        }

        if (matchedPattern != null) {
            result.put("pattern", matchedPattern);
            result.put("type", matchedKey);
            result.put("found", true);
            result.put("suggestion", "Common pattern for '" + matchedKey + "' matched from the built-in library.");
        } else {
            result.put("pattern", null);
            result.put("type", null);
            result.put("found", false);
            result.put("suggestion",
                    "No built-in pattern found for '" + description + "'. "
                    + "Available patterns: " + String.join(", ", COMMON_PATTERNS.keySet().stream().sorted().toList())
                    + ". Try using one of these keywords or provide a more specific description.");
        }

        return result;
    }

    @Override
    public Map<String, Object> optimizeRegex(String pattern) {
        var result = new LinkedHashMap<String, Object>();
        result.put("pattern", pattern);

        try {
            Pattern.compile(pattern);
            result.put("valid", true);

            var suggestions = new ArrayList<Map<String, String>>();

            // Check for redundant character classes [0-9] -> \d
            if (pattern.contains("[0-9]")) {
                suggestions.add(Map.of(
                        "type", "redundant-char-class",
                        "description", "Replace [0-9] with \\d for brevity.",
                        "original", "[0-9]",
                        "replacement", "\\d"
                ));
            }

            // Check for redundant [a-zA-Z0-9_] -> \w
            if (pattern.contains("[a-zA-Z0-9_]")) {
                suggestions.add(Map.of(
                        "type", "redundant-char-class",
                        "description", "Replace [a-zA-Z0-9_] with \\w for brevity.",
                        "original", "[a-zA-Z0-9_]",
                        "replacement", "\\w"
                ));
            }

            // Check for redundant [ \t\n\r\f] -> \s
            if (pattern.contains("[ \\t\\n\\r\\f]") || pattern.contains("[ \\t\\r\\n]")) {
                suggestions.add(Map.of(
                        "type", "redundant-char-class",
                        "description", "Replace whitespace character class with \\s for brevity.",
                        "original", "whitespace character class",
                        "replacement", "\\s"
                ));
            }

            // Detect unnecessary capturing groups -> suggest non-capturing
            detectUnnecessaryCapturingGroups(pattern, suggestions);

            // Detect single-char alternations that could be character classes: a|b|c -> [abc]
            detectSingleCharAlternations(pattern, suggestions);

            // Detect greedy quantifiers that might benefit from lazy versions
            detectGreedyQuantifiers(pattern, suggestions);

            result.put("suggestions", suggestions);
            result.put("optimized", !suggestions.isEmpty());
        } catch (PatternSyntaxException e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            result.put("suggestions", List.of());
            result.put("optimized", false);
        }

        return result;
    }

    @Override
    public Map<String, Object> convertRegex(String pattern, String toFormat) {
        var result = new LinkedHashMap<String, Object>();
        result.put("originalPattern", pattern);
        result.put("targetFormat", toFormat);

        var normalizedFormat = toFormat.toLowerCase().trim();

        switch (normalizedFormat) {
            case "javascript" -> {
                result.put("convertedPattern", "/" + pattern + "/");
                result.put("notes", List.of(
                        "JavaScript uses /pattern/flags notation.",
                        "Add flags after the closing slash (e.g., /pattern/gi for global + case-insensitive).",
                        "JavaScript does not support lookbehind in older engines."
                ));
            }
            case "python" -> {
                result.put("convertedPattern", "r'" + pattern + "'");
                result.put("notes", List.of(
                        "Python uses raw string notation r'...' to avoid double-escaping backslashes.",
                        "Use re.compile() for repeated usage.",
                        "Named groups use (?P<name>...) syntax in Python."
                ));
            }
            case "java" -> {
                var javaPattern = pattern.replace("\\", "\\\\");
                result.put("convertedPattern", "\"" + javaPattern + "\"");
                result.put("notes", List.of(
                        "Java requires double-escaping backslashes in string literals.",
                        "Use Pattern.compile() to compile the pattern.",
                        "Named groups use (?<name>...) syntax in Java."
                ));
            }
            case "pcre" -> {
                result.put("convertedPattern", "/" + pattern + "/");
                result.put("notes", List.of(
                        "PCRE (Perl Compatible Regular Expressions) uses /pattern/flags notation.",
                        "PCRE supports named groups with (?P<name>...) or (?<name>...) syntax.",
                        "PCRE supports atomic groups (?>...) and possessive quantifiers.",
                        "PCRE supports recursive patterns with (?R) or (?n)."
                ));
            }
            default -> {
                result.put("convertedPattern", pattern);
                result.put("notes", List.of(
                        "Unknown format '" + toFormat + "'. Returning original pattern.",
                        "Supported formats: java, python, javascript, pcre."
                ));
            }
        }

        return result;
    }

    // ======================== Private helpers ========================

    /**
     * Parses flag characters (e.g. "im") into Java Pattern flag constants.
     */
    private int parseFlags(String flags) {
        if (flags == null || flags.isBlank()) {
            return 0;
        }
        int result = 0;
        for (char c : flags.toCharArray()) {
            result |= switch (c) {
                case 'i' -> Pattern.CASE_INSENSITIVE;
                case 'm' -> Pattern.MULTILINE;
                case 's' -> Pattern.DOTALL;
                case 'x' -> Pattern.COMMENTS;
                case 'u' -> Pattern.UNICODE_CASE;
                default -> 0;
            };
        }
        return result;
    }

    /**
     * Parses the next regex component starting at position i, adds it to the components list,
     * and returns the new position.
     */
    private int parseComponent(String pattern, int i, List<Map<String, String>> components) {
        char c = pattern.charAt(i);

        switch (c) {
            case '^' -> {
                components.add(Map.of("component", "^", "explanation", "Start of string anchor"));
                return i + 1;
            }
            case '$' -> {
                components.add(Map.of("component", "$", "explanation", "End of string anchor"));
                return i + 1;
            }
            case '.' -> {
                return handleQuantifier(pattern, i, ".", "Any character (except newline)", components);
            }
            case '\\' -> {
                return parseEscapeSequence(pattern, i, components);
            }
            case '[' -> {
                return parseCharacterClass(pattern, i, components);
            }
            case '(' -> {
                return parseGroup(pattern, i, components);
            }
            case '*' -> {
                components.add(Map.of("component", "*", "explanation", "Zero or more of the preceding element (greedy)"));
                return i + 1;
            }
            case '+' -> {
                components.add(Map.of("component", "+", "explanation", "One or more of the preceding element (greedy)"));
                return i + 1;
            }
            case '?' -> {
                components.add(Map.of("component", "?", "explanation", "Zero or one of the preceding element (optional)"));
                return i + 1;
            }
            case '{' -> {
                return parseQuantifier(pattern, i, components);
            }
            case '|' -> {
                components.add(Map.of("component", "|", "explanation", "Alternation (OR)"));
                return i + 1;
            }
            default -> {
                return handleQuantifier(pattern, i, String.valueOf(c), "Literal character '" + c + "'", components);
            }
        }
    }

    /**
     * Handles a base component and checks if it is followed by a quantifier.
     */
    private int handleQuantifier(String pattern, int startIdx, String baseComponent, String baseExplanation,
                                  List<Map<String, String>> components) {
        int nextIdx = startIdx + baseComponent.length();

        if (nextIdx < pattern.length()) {
            char next = pattern.charAt(nextIdx);
            if (next == '{') {
                int closeBrace = pattern.indexOf('}', nextIdx);
                if (closeBrace > nextIdx) {
                    var quantifier = pattern.substring(nextIdx, closeBrace + 1);
                    var fullComponent = baseComponent + quantifier;
                    var explanation = baseExplanation + " " + describeQuantifier(quantifier);
                    components.add(Map.of("component", fullComponent, "explanation", explanation));
                    return closeBrace + 1;
                }
            }
        }

        components.add(Map.of("component", baseComponent, "explanation", baseExplanation));
        return nextIdx;
    }

    /**
     * Describes a quantifier like {n}, {n,}, {n,m}.
     */
    private String describeQuantifier(String quantifier) {
        var inner = quantifier.substring(1, quantifier.length() - 1);
        if (inner.contains(",")) {
            var parts = inner.split(",", -1);
            if (parts[1].isBlank()) {
                return "(at least " + parts[0].trim() + " times)";
            }
            return "(between " + parts[0].trim() + " and " + parts[1].trim() + " times)";
        }
        return "(exactly " + inner.trim() + " times)";
    }

    /**
     * Parses an escape sequence starting with \.
     */
    private int parseEscapeSequence(String pattern, int i, List<Map<String, String>> components) {
        if (i + 1 >= pattern.length()) {
            components.add(Map.of("component", "\\", "explanation", "Incomplete escape sequence"));
            return i + 1;
        }

        char next = pattern.charAt(i + 1);
        var component = "\\" + next;
        var explanation = switch (next) {
            case 'd' -> "Digit character [0-9]";
            case 'D' -> "Non-digit character [^0-9]";
            case 'w' -> "Word character [a-zA-Z0-9_]";
            case 'W' -> "Non-word character [^a-zA-Z0-9_]";
            case 's' -> "Whitespace character";
            case 'S' -> "Non-whitespace character";
            case 'b' -> "Word boundary";
            case 'B' -> "Non-word boundary";
            case 'n' -> "Newline";
            case 't' -> "Tab";
            case 'r' -> "Carriage return";
            default -> "Escaped literal '" + next + "'";
        };

        return handleQuantifier(pattern, i, component, explanation, components);
    }

    /**
     * Parses a character class [...].
     */
    private int parseCharacterClass(String pattern, int i, List<Map<String, String>> components) {
        int closeBracket = findClosingBracket(pattern, i);
        if (closeBracket == -1) {
            components.add(Map.of("component", "[", "explanation", "Unclosed character class"));
            return i + 1;
        }

        var charClass = pattern.substring(i, closeBracket + 1);
        var inner = charClass.substring(1, charClass.length() - 1);
        var negated = inner.startsWith("^");
        var explanation = negated
                ? "Character class (negated): any character NOT in " + inner.substring(1)
                : "Character class: any character in " + inner;

        return handleQuantifier(pattern, i, charClass, explanation, components);
    }

    /**
     * Finds the closing bracket ] accounting for escaped brackets.
     */
    private int findClosingBracket(String pattern, int openPos) {
        int i = openPos + 1;
        // If first character after [ is ], it's a literal ]
        if (i < pattern.length() && pattern.charAt(i) == '^') {
            i++;
        }
        if (i < pattern.length() && pattern.charAt(i) == ']') {
            i++;
        }
        while (i < pattern.length()) {
            if (pattern.charAt(i) == '\\' && i + 1 < pattern.length()) {
                i += 2;
                continue;
            }
            if (pattern.charAt(i) == ']') {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Parses a group (...) including special groups (?:...), (?=...), (?!...), (?<=...), (?<!...).
     */
    private int parseGroup(String pattern, int i, List<Map<String, String>> components) {
        int closeParen = findClosingParen(pattern, i);
        if (closeParen == -1) {
            components.add(Map.of("component", "(", "explanation", "Unclosed group"));
            return i + 1;
        }

        var group = pattern.substring(i, closeParen + 1);
        var inner = group.substring(1, group.length() - 1);
        String explanation;

        if (inner.startsWith("?:")) {
            explanation = "Non-capturing group: " + inner.substring(2);
        } else if (inner.startsWith("?=")) {
            explanation = "Positive lookahead: asserts that what follows matches " + inner.substring(2);
        } else if (inner.startsWith("?!")) {
            explanation = "Negative lookahead: asserts that what follows does NOT match " + inner.substring(2);
        } else if (inner.startsWith("?<=")) {
            explanation = "Positive lookbehind: asserts that what precedes matches " + inner.substring(3);
        } else if (inner.startsWith("?<!")) {
            explanation = "Negative lookbehind: asserts that what precedes does NOT match " + inner.substring(3);
        } else if (inner.startsWith("?<") && inner.indexOf('>') > 2) {
            var nameEnd = inner.indexOf('>');
            var name = inner.substring(2, nameEnd);
            explanation = "Named capturing group '" + name + "': " + inner.substring(nameEnd + 1);
        } else {
            explanation = "Capturing group: " + inner;
        }

        return handleQuantifier(pattern, i, group, explanation, components);
    }

    /**
     * Finds the closing parenthesis ) accounting for nested groups and escapes.
     */
    private int findClosingParen(String pattern, int openPos) {
        int depth = 0;
        int i = openPos;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == '[') {
                int closeBracket = findClosingBracket(pattern, i);
                if (closeBracket != -1) {
                    i = closeBracket + 1;
                    continue;
                }
            }
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    /**
     * Parses a quantifier {n}, {n,}, {n,m}.
     */
    private int parseQuantifier(String pattern, int i, List<Map<String, String>> components) {
        int closeBrace = pattern.indexOf('}', i);
        if (closeBrace == -1) {
            components.add(Map.of("component", "{", "explanation", "Literal '{' (unclosed quantifier)"));
            return i + 1;
        }

        var quantifier = pattern.substring(i, closeBrace + 1);
        components.add(Map.of("component", quantifier, "explanation", describeQuantifier(quantifier)));
        return closeBrace + 1;
    }

    /**
     * Detects unnecessary capturing groups and suggests non-capturing groups.
     */
    private void detectUnnecessaryCapturingGroups(String pattern, List<Map<String, String>> suggestions) {
        var matcher = Pattern.compile("\\((?!\\?)").matcher(pattern);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (count > 0) {
            suggestions.add(Map.of(
                    "type", "unnecessary-capture-group",
                    "description", "Found " + count + " capturing group(s). If you don't need to extract the matched content, use non-capturing groups (?:...) instead for better performance.",
                    "original", "(...)",
                    "replacement", "(?:...)"
            ));
        }
    }

    /**
     * Detects single-character alternation patterns like a|b|c that could be [abc].
     */
    private void detectSingleCharAlternations(String pattern, List<Map<String, String>> suggestions) {
        var singleCharAlt = Pattern.compile("(?:^|\\|)([^|\\\\])(?:\\|([^|\\\\]))+(?:$|\\|)");
        var matcher = singleCharAlt.matcher(pattern);
        if (matcher.find()) {
            var matched = matcher.group(0);
            // Remove leading/trailing pipes if present
            var trimmed = matched.replaceAll("^\\||\\|$", "");
            var chars = trimmed.replace("|", "");
            suggestions.add(Map.of(
                    "type", "alternation-to-char-class",
                    "description", "Alternation of single characters can be replaced with a character class for better performance.",
                    "original", trimmed,
                    "replacement", "[" + chars + "]"
            ));
        }
    }

    /**
     * Detects greedy quantifiers followed by patterns that might benefit from lazy versions.
     */
    private void detectGreedyQuantifiers(String pattern, List<Map<String, String>> suggestions) {
        // Pattern: .* or .+ not followed by ? (already lazy)
        var matcher = Pattern.compile("\\.[*+](?!\\?)").matcher(pattern);
        if (matcher.find()) {
            suggestions.add(Map.of(
                    "type", "greedy-quantifier",
                    "description", "Found greedy quantifier '" + matcher.group() + "'. Consider using lazy version ('" + matcher.group() + "?') if you want the shortest match, which can improve performance in some cases.",
                    "original", matcher.group(),
                    "replacement", matcher.group() + "?"
            ));
        }
    }
}
