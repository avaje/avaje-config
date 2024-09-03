package io.avaje.config.json;

import io.avaje.config.ConfigParser;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

@NullMarked
final class JsonParser implements ConfigParser {

  private static final String[] extensions = {"json"};

  private final Jsonb jsonb;

  JsonParser() {
    this.jsonb = Jsonb.builder()
      .failOnUnknown(false)
      .serializeEmpty(true)
      .serializeNulls(true)
      .build();
  }

  @Override
  public String[] supportedExtensions() {
    return extensions;
  }

  @Override
  public Map<String, String> load(Reader reader) {
    return (Map<String, String>) this.jsonb.type(Types.mapOf(String.class)).fromJson(reader);
  }

  @Override
  public Map<String, String> load(InputStream is) {
    return (Map<String, String>) this.jsonb.type(Types.mapOf(String.class)).fromJson(is);
  }
}
