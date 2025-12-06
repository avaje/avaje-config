package io.avaje.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class DefaultFallbacksTest {

  @Test
  void toEnvKey() {
    assertThat(DefaultFallback.toEnvKey("My")).isEqualTo("MY");
    assertThat(DefaultFallback.toEnvKey("My.Foo")).isEqualTo("MY_FOO");
    assertThat(DefaultFallback.toEnvKey("my.foo.bar")).isEqualTo("MY_FOO_BAR");
    assertThat(DefaultFallback.toEnvKey("BAR")).isEqualTo("BAR");
  }

}
