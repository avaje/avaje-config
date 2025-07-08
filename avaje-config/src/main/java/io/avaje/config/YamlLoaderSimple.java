package io.avaje.config;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.jspecify.annotations.NullMarked;

/**
 * Simple YAML parser for loading yaml based config.
 */
@NullMarked
final class YamlLoaderSimple implements YamlLoader {

  @Override
  public Map<String, String> load(Reader reader) {
    return new Load().load(reader);
  }

  @Override
  public Map<String, String> load(InputStream is) {
    return new Load().load(is);
  }

  private static class Load {
    enum MultiLineTrim {
      Clip,
      Strip,
      Keep,
      Implicit
    }

    enum State {
      RequireKey,
      MultiLine,
      List,
      KeyOrValue,
      RequireTopKey
    }

    private final Map<String, String> keyValues = new LinkedHashMap<>();
    private final Deque<Key> keyStack = new ArrayDeque<>();
    private final List<String> multiLines = new ArrayList<>();

    private State state = State.RequireKey;
    private MultiLineTrim multiLineTrim = MultiLineTrim.Clip;
    private int currentLine;
    private int currentIndent;
    private int multiLineIndent;

    private Map<String, String> load(InputStream is) {
      return load(new InputStreamReader(is));
    }

    private Map<String, String> load(Reader reader) {
      try (LineNumberReader lineReader = new LineNumberReader(reader)) {
        String line;
        do {
          line = lineReader.readLine();
          processLine(line);
        } while (line != null);

        return keyValues;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private void processLine(String line) {
      if (line == null) {
        checkFinalMultiLine();
      } else {
        currentLine++;
        readIndent(line);
        if (state == State.MultiLine || state == State.List) {
          processMultiLine(line);
        } else {
          processNext(line);
        }
      }
    }

    private void checkFinalMultiLine() {
      if (state == State.MultiLine) {
        addKeyVal(multiLineValue());
      }
      if (state == State.List) {
        addKeyVal(listValue());
      }
    }

    private void processMultiLine(String line) {
      if (multiLineIndent == 0) {
        if (currentIndent == 0 && !line.trim().isEmpty()) {
          multiLineEnd(line);
          return;
        }
        // first multiLine
        multiLineIndent = currentIndent;
        multiLines.add(line);
      } else if (currentIndent >= multiLineIndent || line.trim().isEmpty()) {
        multiLines.add(line);
      } else {
        // end of multiLine
        multiLineEnd(line);
      }
    }

    private void multiLineEnd(String line) {
      if (state == State.MultiLine) addKeyVal(multiLineValue());
      else {
        addKeyVal(listValue());
      }
      processNext(line);
    }

    private String listValue() {
      if (multiLines.isEmpty()) {
        return "";
      }
      multiLineTrimTrailing();
      var result =
          multiLines.stream().map(s -> s.trim().substring(1).stripLeading()).collect(joining(","));
      multiLineEnd();
      return result;
    }

    private String multiLineValue() {
      if (multiLines.isEmpty()) {
        return "";
      }
      if (multiLineTrim != MultiLineTrim.Keep) {
        multiLineTrimTrailing();
      }
      String join = multiLineTrim == MultiLineTrim.Implicit ? " " : "\n";
      StringBuilder sb = new StringBuilder();
      int lastIndex = multiLines.size() - 1;
      for (int i = 0; i <= lastIndex; i++) {
        String line = multiLines.get(i);
        if (line.length() < multiLineIndent) {
          // empty line whitespace
          sb.append("\n");
        } else {
          line = line.substring(multiLineIndent);
          if (i == lastIndex && (multiLineTrim == MultiLineTrim.Strip || multiLineTrim == MultiLineTrim.Implicit)) {
            sb.append(line);
          } else {
            sb.append(line).append(join);
          }
        }
      }
      multiLineEnd();
      return sb.toString();
    }

    private void multiLineTrimTrailing() {
      for (int i = multiLines.size(); i-- > 0; ) {
        if (!multiLines.get(i).trim().isEmpty()) {
          break;
        } else {
          multiLines.remove(i);
        }
      }
    }

    void readIndent(String line) {
      currentIndent = indent(line);
    }

    private void processNext(String line) {
      if (newDocument(line) || ignoreLine(line)) {
        return;
      }

      final int pos = line.indexOf(':');
      if (pos == -1) {
        // value on another line
        processNonKey(line);
        return;
      }
      if (state == State.RequireTopKey && currentIndent > 0) {
        throw new IllegalStateException("Require top level key at line:" + currentLine + " [" + line + "]");
      }

      // must be a key - would expect explicit multiline otherwise
      final Key key = new Key(currentIndent, trimKey(line.substring(0, pos)));
      popKeys(currentIndent);
      keyStack.push(key);

      // look at the remainder of the line
      final String remaining = line.substring(pos + 1);
      final String trimmedValue = remaining.trim();
      if (trimmedValue.startsWith("|")) {
        multilineStart(multiLineTrimMode(trimmedValue));

      } else if (trimmedValue.startsWith("-")) {
        listStart(multiLineTrimMode(trimmedValue));
      } else if (trimmedValue.isEmpty() || trimmedValue.startsWith("#")) {
        // empty or comment
        state = State.KeyOrValue;
      } else {
        // simple key value
        addKeyVal(trimValue(remaining.trim()));
      }
    }

    private MultiLineTrim multiLineTrimMode(String trimmedValue) {
      if (trimmedValue.length() == 1) {
        return MultiLineTrim.Clip;
      }
      final char ch = trimmedValue.charAt(1);
      switch (ch) {
        case '-':
          // the final line break and any trailing empty lines are excluded
          return MultiLineTrim.Strip;
        case '+':
          // the final line break and any trailing empty lines are included
          return MultiLineTrim.Keep;
        default:
          // the final line break character is included and trailing empty lines are excluded
          return MultiLineTrim.Clip;
      }
    }

    private void addKeyVal(String value) {
      keyValues.put(fullKey(), value);
      keyStack.pop();
      state = State.RequireKey;
    }

    private void processNonKey(String line) {
      if (state == State.RequireKey) {
        state = State.RequireTopKey;
        // drop this value line
        return;
      }
      if (keyStack.isEmpty()) {
        throw new IllegalStateException("Reading a value but no key at line: " + currentLine + " line[" + line + "]");
      }
      final int keyIndent = keyStack.peek().indent;
      if (currentIndent <= keyIndent) {
        throw new IllegalStateException("Value not indented enough for key " + fullKey() + " at line: " + currentLine + " line[" + line + "]");
      }
      if (line.stripLeading().charAt(0) == '-') {
        listStart(MultiLineTrim.Implicit);
      } else {
        multilineStart(MultiLineTrim.Implicit);
      }
      multiLineIndent = currentIndent;
      multiLines.add(line);
    }

    private void multilineStart(MultiLineTrim trim) {
      state = State.MultiLine;
      multiLineIndent = 0;
      multiLineTrim = trim;
    }

    private void listStart(MultiLineTrim trim) {
      state = State.List;
      multiLineIndent = 0;
      multiLineTrim = trim;
    }

    private void multiLineEnd() {
      state = State.RequireKey;
      multiLineIndent = 0;
      multiLines.clear();
    }

    private boolean newDocument(String line) {
      if (line.startsWith("---")) {
        keyStack.clear();
        return true;
      }
      return false;
    }

    private boolean ignoreLine(String line) {
      final String trimmed = line.trim();
      return trimmed.isEmpty() || trimmed.startsWith("#");
    }

    private String trimValue(String value) {
      if (value.startsWith("'")) {
        return unquoteValue('\'', value);
      }
      if (value.startsWith("\"")) {
        return unquoteValue('"', value);
      }
      int commentPos = value.indexOf('#');
      if (commentPos > -1) {
        return value.substring(0, commentPos).trim();
      }
      return value;
    }

    private String unquoteValue(char quoteChar, String value) {
      final int pos = value.lastIndexOf(quoteChar);
      return value.substring(1, pos);
    }

    private String fullKey() {
      StringJoiner fullKey = new StringJoiner(".");
      Iterator<Key> it = keyStack.descendingIterator();
      while (it.hasNext()) {
        fullKey.add(it.next().key());
      }
      return fullKey.toString();
    }

    private void popKeys(int indent) {
      while (!keyStack.isEmpty()) {
        if (keyStack.peek().indent() < indent) {
          break;
        } else {
          keyStack.pop();
        }
      }
    }

    private String trimKey(String indentKey) {
      return unquoteKey(indentKey.trim());
    }

    private String unquoteKey(String value) {
      if (value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\"")) {
        return value.substring(1, value.length() - 1);
      }
      return value;
    }

    private int indent(String line) {
      final char[] chars = line.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        if (!Character.isWhitespace(chars[i])) {
          return i;
        }
      }
      return 0;
    }

    private static class Key {
      private final int indent;
      private final String key;

      Key(int indent, String key) {
        this.indent = indent;
        this.key = key;
      }

      int indent() {
        return indent;
      }

      String key() {
        return key;
      }
    }
  }
}
