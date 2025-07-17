package io.avaje.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReloadTest {

  @Test
  void test_reload() {
    System.setProperty("Rock", "Man");
    assertThat(Config.get("Rock")).isEqualTo("Man");
    System.setProperty("Rock", "Lee");
    Config.reload();
    assertThat(Config.get("Rock")).isEqualTo("Lee");
  }
}
