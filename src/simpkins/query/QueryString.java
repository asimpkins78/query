package simpkins.query;

import java.util.Random;

public class QueryString implements CharSequence {
    private final String source;
    private static final QueryString empty = new QueryString("");

    public QueryString(String source) {
        this.source = source;
    }

    public static QueryString of(String source) {
        return new QueryString(source);
    }

    public int length() {
        return source.length();
    }

    public boolean matches(String regex) {
        return source.matches(regex);
    }

    public QueryString substringFrom(String start) {
        return substringFrom(start, false);
    }

    public QueryString substringFrom(String start, boolean includeStart) {
        int index = source.indexOf(start);
        if (index == -1) return this;
        if (!includeStart) index += start.length();
        return new QueryString(source.substring(index));
    }

    public QueryString substringTo(String end) {
        return substringTo(end, false);
    }

    public QueryString substringTo(String end, boolean includeEnd) {
        int index = source.indexOf(end);
        if (index == -1) return this;
        if (includeEnd) index += end.length();
        return new QueryString(source.substring(0, index));
    }

    public QueryString substringFromLast(String start) {
        return substringFromLast(start, false);
    }

    public QueryString substringFromLast(String start, boolean includeStart) {
        int index = source.lastIndexOf(start);
        if (index == -1) return this;
        if (!includeStart) index += start.length();
        return new QueryString(source.substring(index));
    }

    public QueryString substringToLast(String end) {
        return substringToLast(end, false);
    }

    public QueryString substringToLast(String end, boolean includeEnd) {
        int index = source.lastIndexOf(end);
        if (index == -1) return this;
        if (includeEnd) index += end.length();
        return new QueryString(source.substring(0, index));
    }

    public QueryString skip(int length) {
        if (length <= 0) return this;
        if (source.length() <= length) return empty;
        return new QueryString(source.substring(length));
    }

    public QueryString skipLast(int length) {
        if (length <= 0) return this;
        if (source.length() <= length) return empty;
        return new QueryString(source.substring(0, source.length() - length));
    }

    public QueryString take(int length) {
        if (source.length() <= length) return this;
        if (length <= 0) return empty;
        return new QueryString(source.substring(0, length));
    }

    public QueryString takeLast(int length) {
        if (source.length() <= length) return this;
        if (length <= 0) return empty;
        return new QueryString(source.substring(source.length() - length));
    }

    public QueryString pad(int length, char padding) {
        if (length <= 0) return this;
        StringBuilder sb = new StringBuilder(source);
        for (int i = 0; i < length; i++)
            sb.append(padding);
        return new QueryString(sb.toString());
    }

    public QueryString padTo(int length, char padding) {
        if (length <= source.length()) return this;
        StringBuilder sb = new StringBuilder(source);
        for (int i = source.length(); i < length; i++)
            sb.append(padding);
        return new QueryString(sb.toString());
    }

    public QueryString padFromStart(int length, char padding) {
        if (length <= 0) return this;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(padding);
        return new QueryString(sb.append(source).toString());
    }

    public QueryString padFromStartTo(int length, char padding) {
        if (length <= source.length()) return this;
        StringBuilder sb = new StringBuilder();
        for (int i = source.length(); i < length; i++)
            sb.append(padding);
        return new QueryString(sb.append(source).toString());
    }

    public QueryString reverse() {
        return new QueryString(new StringBuilder(source).reverse().toString());
    }

    public QueryString shuffle() {
        char[] characters = source.toCharArray();
        Random random = new Random();
        char buffer;
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            if (randomIndex == i) continue;
            buffer = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = buffer;
        }
        return new QueryString(new String(characters));
    }

    public QueryString combine(String string) {
        return new QueryString(source.concat(string));
    }

    public QueryString combineAfter(String string) {
        return new QueryString(string.concat(source));
    }

    public QueryString insert(int insertIndex, String string) {
        return new QueryString(new StringBuilder(source).insert(insertIndex, string).toString());
    }

    public QueryString insertInto(int insertIndex, String string) {
        return new QueryString(new StringBuilder(string).insert(insertIndex, source).toString());
    }

    public QueryString capitalize() {
        if (source.isEmpty()) return this;
        return new QueryString(source.substring(0, 1).toUpperCase() + source.substring(1));
    }

    public QueryString uncapitalize() {
        if (source.isEmpty()) return this;
        return new QueryString(source.substring(0, 1).toLowerCase() + source.substring(1));
    }

    public boolean isBlank() {
        return source == null || source.trim().isEmpty();
    }

    public boolean isNotBlank() {
        return !isBlank();
    }

    //
    // String methods
    //

    public boolean contains(CharSequence s) {
        return source.contains(s);
    }

    public QueryString substring(int beginIndex) {
        return new QueryString(source.substring(beginIndex));
    }

    public QueryString substring(int beginIndex, int endIndex) {
        return new QueryString(source.substring(beginIndex, endIndex));
    }

    public QueryString replace(char oldChar, char newChar) {
        return new QueryString(source.replace(oldChar, newChar));
    }

    public QueryString replace(CharSequence target, CharSequence replacement) {
        return new QueryString(source.replace(target, replacement));
    }

    public QueryString replaceAll(String regex, String replacement) {
        return new QueryString(source.replaceAll(regex, replacement));
    }

    public QueryString replaceFirst(String regex, String replacement) {
        return new QueryString(source.replaceFirst(regex, replacement));
    }

    public QueryString trim() {
        return new QueryString(source.trim());
    }

    public Query<String> split(String regex) {
        return Query.from(source.split(regex));
    }

    public Query<String> split(String regex, int limit) {
        return Query.from(source.split(regex, limit));
    }

    public QueryString toLowerCase() {
        return new QueryString(source.toLowerCase());
    }

    public QueryString toUpperCase() {
        return new QueryString(source.toUpperCase());
    }

    public char[] toCharArray() {
        return source.toCharArray();
    }

    public Query<Character> toCharacters() {
        return Query.from(source.toCharArray());
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (source == null || obj == null)
            return source == null && obj == null;
        return (obj instanceof QueryString || obj instanceof String)
                && source.equals(obj.toString());
    }

    @Override
    public String toString() {
        return source;
    }

    @Override
    public char charAt(int index) {
        return source.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return source.subSequence(start, end);
    }
}
