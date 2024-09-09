package io.avaje.config.toml;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TomlParserTest {

  @Test
  void supportedExtensions() {
    var parser = new TomlParser();
    assertThat(parser.supportedExtensions()).isEqualTo(new String[]{"toml"});
  }

  private static String input() {
    return "key = \"c\"\n" +
      "\n" +
      "[one]\n" +
      "key1 = \"a\"\n" +
      "key2 = \"b\"\n" +
      "key3 = [\"a\", \"b\", \"c\"]\n" +
      "[two]\n" +
      "local_datetime = 2024-09-09T15:30:00\n" +
      "local_date = 2024-09-09\n" +
      "local_time = 15:30:00\n" +
      "offset_datetime = 2024-09-09T15:30:00+02:00";
  }

  @Test
  void load_reader() {
    var parser = new TomlParser();
    Map<String, String> map = parser.load(new StringReader(input()));

    assertThat(map).hasSize(8);
    assertThat(map).containsOnlyKeys("key",
      "one.key1", "one.key2", "one.key3",
      "two.local_datetime", "two.local_date", "two.local_time", "two.offset_datetime");

    assertThat(map).containsEntry("key", "c");

    assertThat(map).containsEntry("one.key1", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("one.key3", "a;b;c");

    assertThat(map).containsEntry("two.local_datetime", "2024-09-09T15:30");
    assertThat(map).containsEntry("two.local_date", "2024-09-09");
    assertThat(map).containsEntry("two.local_time", "15:30");
    assertThat(map).containsEntry("two.offset_datetime", "2024-09-09T15:30+02:00");
  }

  @Test
  void load_inputStream() {
    var parser = new TomlParser();
    Map<String, String> map = parser.load(new ByteArrayInputStream(input().getBytes(StandardCharsets.UTF_8)));

    assertThat(map).hasSize(8);
    assertThat(map).containsOnlyKeys("key",
      "one.key1", "one.key2", "one.key3",
      "two.local_datetime", "two.local_date", "two.local_time", "two.offset_datetime");

    assertThat(map).containsEntry("key", "c");

    assertThat(map).containsEntry("one.key1", "a");
    assertThat(map).containsEntry("one.key2", "b");
    assertThat(map).containsEntry("one.key3", "a;b;c");

    assertThat(map).containsEntry("two.local_datetime", "2024-09-09T15:30");
    assertThat(map).containsEntry("two.local_date", "2024-09-09");
    assertThat(map).containsEntry("two.local_time", "15:30");
    assertThat(map).containsEntry("two.offset_datetime", "2024-09-09T15:30+02:00");
  }
}
