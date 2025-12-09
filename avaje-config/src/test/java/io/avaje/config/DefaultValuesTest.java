package io.avaje.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class DefaultValuesTest {

  @Test
  void toEnvKey() {
    assertThat(DefaultValues.toEnvKey("My")).isEqualTo("MY");
    assertThat(DefaultValues.toEnvKey("My.Foo")).isEqualTo("MY_FOO");
    assertThat(DefaultValues.toEnvKey("my.foo.bar")).isEqualTo("MY_FOO_BAR");
    assertThat(DefaultValues.toEnvKey("BAR")).isEqualTo("BAR");
    assertThat(DefaultValues.toEnvKey("my.foo-bar")).isEqualTo("MY_FOOBAR");
  }

}
