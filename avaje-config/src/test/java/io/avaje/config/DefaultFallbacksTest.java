package io.avaje.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class DefaultFallbacksTest {

  @Test
  void toEnvKey() {
    assertThat(DefaultFallbacks.toEnvKey("My")).isEqualTo("MY");
    assertThat(DefaultFallbacks.toEnvKey("My.Foo")).isEqualTo("MY_FOO");
    assertThat(DefaultFallbacks.toEnvKey("my.foo.bar")).isEqualTo("MY_FOO_BAR");
    assertThat(DefaultFallbacks.toEnvKey("BAR")).isEqualTo("BAR");
  }

}
