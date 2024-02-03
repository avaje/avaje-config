package io.avaje.config;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesParserTest {

  @Test
  void supportedExtensions() {
    var parser = new PropertiesParser();
    assertThat(parser.supportedExtensions()).isEqualTo(new String[]{"properties"});
  }

  private static String input() {
    return "one.key=a\n" +
      "one.key2=b\n" +
      "## comment\n" +
      "key3 = c\n";
  }

  @Test
  void load_reader() {
    var parser = new PropertiesParser();
    Map<String, String> map = parser.load(new StringReader(input()));

    assertThat(map).hasSize(3);
    assertThat(map).containsOnlyKeys("one.key", "one.key2", "key3");
    assertThat(map).containsEntry("one.key", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("key3", "c");
  }

  @Test
  void load_inputStream() {
    var parser = new PropertiesParser();
    Map<String, String> map = parser.load(new ByteArrayInputStream(input().getBytes(StandardCharsets.UTF_8)));

    assertThat(map).hasSize(3);
    assertThat(map).containsOnlyKeys("one.key", "one.key2", "key3");
    assertThat(map).containsEntry("one.key", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("key3", "c");
  }
}
