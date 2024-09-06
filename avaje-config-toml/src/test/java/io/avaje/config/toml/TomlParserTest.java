package io.avaje.config.toml;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class TomlParserTest {

  @Test
  void supportedExtensions() {
    var parser = new TomlParser();
    assertThat(parser.supportedExtensions()).isEqualTo(new String[]{"toml"});
  }

  private static String input() {
    return "key3 = \"c\"\n" +
      "\n" +
      "[one]\n" +
      "key = \"a\"\n" +
      "key2 = \"b\"\n";
  }

  @Test
  void load_reader() {
    var parser = new TomlParser();
    Map<String, String> map = parser.load(new StringReader(input()));

    assertThat(map).hasSize(3);
    assertThat(map).containsOnlyKeys("one.key", "one.key2", "key3");
    assertThat(map).containsEntry("one.key", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("key3", "c");
  }

  @Test
  void load_inputStream() {
    var parser = new TomlParser();
    Map<String, String> map = parser.load(new ByteArrayInputStream(input().getBytes(StandardCharsets.UTF_8)));

    assertThat(map).hasSize(3);
    assertThat(map).containsOnlyKeys("one.key", "one.key2", "key3");
    assertThat(map).containsEntry("one.key", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("key3", "c");
  }
}
