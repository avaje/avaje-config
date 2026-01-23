package io.avaje.config;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class InitialLoadContextTest {

  String userHome = System.getProperty("user.home");

  @Test
  void toFile_when_userHomePath() {
    File file = InitialLoadContext.toFile("~/blah");
    assertThat(file.getAbsolutePath()).isEqualTo(userHome + "/blah");
  }

  @Test
  void toFile_when_simplePath() {
    File file = InitialLoadContext.toFile("foo");
    File expectedMatch = new File("foo");
    assertThat(file.getAbsolutePath()).isEqualTo(expectedMatch.getAbsolutePath());
  }

  @Test
  void toFile_when_pathWithSub() {
    File file = InitialLoadContext.toFile("foo/blah");
    File expectedMatch = new File("foo/blah");
    assertThat(file.getAbsolutePath()).isEqualTo(expectedMatch.getAbsolutePath());
  }

}
