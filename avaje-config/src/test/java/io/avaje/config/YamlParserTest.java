package io.avaje.config;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlParserTest {

  private final YamlLoaderSnake load = new YamlLoaderSnake();

  @Test
  void basic() {
    basic(parseYaml2("/yaml/basic.yaml"));
    basic(parseYaml("/yaml/basic.yaml"));
  }

  void basic(final Map<String, String> map) {
    assertThat(map).containsOnlyKeys("name", "properties.key1", "properties.key2", "sorted.1", "sorted.2");
    assertThat(map.get("name")).isEqualTo("Name123");
    assertThat(map.get("properties.key1")).isEqualTo("value1");
    assertThat(map.get("properties.key2")).isEqualTo("value2");
    assertThat(map.get("sorted.1")).isEqualTo("one");
  }

  @Test
  void parse_singleDoc() {
    parse_singleDoc(parseYaml2("/yaml/single-doc.yaml"));
    parse_singleDoc(parseYaml("/yaml/single-doc.yaml"));
  }

  void parse_singleDoc(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("name.a.e", "name.b.t", "int", "float");
    assertThat(map.get("name.a.e")).isEqualTo("e1");
    assertThat(map.get("name.b.t")).isEqualTo("t1");
    assertThat(map.get("int")).isEqualTo("42");
    assertThat(map.get("float")).isEqualTo("3.14159");
  }

  @Test
  void parse_multipleDocs() {
    parse_multipleDocs(parseYaml2("/yaml/multiple-docs.yaml"));
    parse_multipleDocs(parseYaml("/yaml/multiple-docs.yaml"));
  }

  private void parse_multipleDocs(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("name", "some.key1", "other.key1", "other.key2");
    assertThat(map.get("name")).isEqualTo("Name123");
    assertThat(map.get("some.key1")).isEqualTo("value1");
    assertThat(map.get("other.key1")).isEqualTo("o1");
    assertThat(map.get("other.key2")).isEqualTo("o2");
  }

  @Test
  void parse_quotedValues() {
    parse_quotedValues(parseYaml2("/yaml/quoted-values.yaml"));
    parse_quotedValues(parseYaml("/yaml/quoted-values.yaml"));
  }

  void parse_quotedValues(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("a", "a1", "b", "b1", "c", "c1", "e", "e1", "d", "d1");
    assertThat(map.get("a")).isEqualTo("unquoted value");
    assertThat(map.get("a1")).isEqualTo("unquoted value with comment");
    assertThat(map.get("b")).isEqualTo("single quote");
    assertThat(map.get("b1")).isEqualTo("single quote with comment");
    assertThat(map.get("c")).isEqualTo("double quote");
    assertThat(map.get("c1")).isEqualTo("double quote with comment");
    assertThat(map.get("d")).isEqualTo(" single quote with spaces ");
    assertThat(map.get("d1")).isEqualTo(" single quote with spaces + comment ");
    assertThat(map.get("e")).isEqualTo(" double quote with spaces ");
    assertThat(map.get("e1")).isEqualTo(" double quote with spaces + comment ");
  }

  @Test
  void parse_quotedKeys() {
    parse_quotedKeys(parseYaml2("/yaml/quoted-keys.yaml"));
    parse_quotedKeys(parseYaml("/yaml/quoted-keys.yaml"));
  }

  private void parse_quotedKeys(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("a-1", "a-2", "b.a-1", "b.a-2");
    assertThat(map.get("a-1")).isEqualTo("v0");
    assertThat(map.get("a-2")).isEqualTo("v1");
    assertThat(map.get("b.a-1")).isEqualTo("v2");
    assertThat(map.get("b.a-2")).isEqualTo("v3");
  }

  @Test
  void parse_multiLine() {
    parse_multiLine(parseYaml2("/yaml/multi-line.yaml"));
    parse_multiLine(parseYaml("/yaml/multi-line.yaml"));
  }

  private void parse_multiLine(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("a0", "a1", "a2", "a3", "a4", "n1.k0", "n1.k1");
    assertThat(map.get("n1.k1")).isEqualTo("kv1");
    assertThat(map.get("n1.k0")).isEqualTo("a line0\n" +
      "b line1\n" +
      "a other\n");
    assertThat(map.get("a0")).isEqualTo("a line0\n" +
      "b line1\n");
    assertThat(map.get("a1")).isEqualTo("a line0\n" +
      "b line1\n");
    assertThat(map.get("a2")).isEqualTo("a line0\n" +
      "b line1");
    assertThat(map.get("a3")).isEqualTo("a line0\n" +
      "\n" +
      "b line1");
    assertThat(map.get("a4")).isEqualTo("a line0\n" +
      "b line1\n" +
      "\n" +
      "c line1\n" +
      "\n");
  }

  @Test
  void parse_multiLine_empty() {
    parse_multiLine_empty(parseYaml2("/yaml/multi-line-empty.yaml"));
    parse_multiLine_empty(parseYaml("/yaml/multi-line-empty.yaml"));
  }

  private void parse_multiLine_empty(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("a0", "a1", "a2", "a3", "a4");
    assertThat(map.get("a0")).isEqualTo("");
    assertThat(map.get("a1")).isEqualTo("");
    assertThat(map.get("a2")).isEqualTo("");
    assertThat(map.get("a3")).isEqualTo("");
    assertThat(map.get("a4")).isEqualTo("\n" +
      "\n" +
      "\n");
  }

  @Test
  void parse_keyComments() {
    parse_keyComments(parseYaml2("/yaml/key-comment.yaml"));
    parse_keyComments(parseYaml("/yaml/key-comment.yaml"));
  }

  private void parse_keyComments(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("k1", "k2", "k3", "k4");
    assertThat(map.get("k1")).isEqualTo("v1");
    assertThat(map.get("k2")).isEqualTo("v2");
    assertThat(map.get("k3")).isEqualTo("v3");
    assertThat(map.get("k4")).isEqualTo("v4");
  }

  @Test
  void parse_multi_line_implicit() {
    parse_multi_line_implicit(parseYaml2("/yaml/multi-line-implicit.yaml"));
    parse_multi_line_implicit(parseYaml("/yaml/multi-line-implicit.yaml"));
  }

  private void parse_multi_line_implicit(Map<String, String> map) {
    assertThat(map).containsOnlyKeys("k0.a", "k1", "k2");
    assertThat(map.get("k0.a")).isEqualTo("v0");
    assertThat(map.get("k1")).isEqualTo("aa bb cc");
    assertThat(map.get("k2")).isEqualTo("dd ee ff");
  }

  @Test
  void parse_top_vals() {
    assertThatThrownBy(() -> parseYaml("/yaml/err-top-vals.yaml"))
      .hasMessageContaining("line: 2");
    assertThatThrownBy(() -> parseYaml2("/yaml/err-top-vals.yaml"))
      .hasMessageContaining("line 4");
  }

  @Test
  void parse_err_req_key2() {
    assertThatThrownBy(() -> parseYaml2("/yaml/err-require-topkey.yaml"))
      .hasMessageContaining("line 5");
    assertThatThrownBy(() -> parseYaml("/yaml/err-require-topkey.yaml"))
      .hasMessageContaining("line:5");
  }


  private Map<String, String> parseYaml2(String s) {
    return load.load(res(s));
  }

  private Map<String, String> parseYaml(String s) {
    YamlLoaderSimple parser = new YamlLoaderSimple();
    return parser.load(res(s));
  }

  private InputStream res(String path) {
    return YamlLoaderSimple.class.getResourceAsStream(path);
  }
}
