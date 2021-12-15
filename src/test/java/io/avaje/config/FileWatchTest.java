package io.avaje.config;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class FileWatchTest {

  @Test
  void test_when_notChanged() {

    CoreConfiguration config = newConfig();
    List<File> files = files();
    final FileWatch watch = new FileWatch(config, files, new YamlLoaderSnake());

    assertThat(config.size()).isEqualTo(2);
    // not touched
    watch.check();
    // no reload
    assertThat(config.size()).isEqualTo(2);
  }

  @Test
  void test_check_whenTouched_expect_loaded() {

    CoreConfiguration config = newConfig();
    List<File> files = files();
    final FileWatch watch = new FileWatch(config, files, new YamlLoaderSnake());

    assertThat(config.size()).isEqualTo(2);
    assertThat(config.get("one", null)).isNull();

    touchFiles(files);
    // check after touch means files loaded
    watch.check();

    // properties loaded as expected
    final int size0 = config.size();
    assertThat(size0).isGreaterThan(2);
    assertThat(config.get("one", null)).isEqualTo("a");
    assertThat(config.getInt("my.size", 42)).isEqualTo(17);
    assertThat(config.getBool("c.active", false)).isTrue();
  }

  @Test
  void test_check_whenTouchedScheduled_expect_loaded() {

    CoreConfiguration config = newConfig();
    List<File> files = files();
    for (File file : files) {
      if (!file.exists()) {
        fail("File " + file.getAbsolutePath() + " does not exist?");
      }
    }
    final FileWatch watch = new FileWatch(config, files, new YamlLoaderSnake());
    System.out.println(watch);

    // assert not loaded
    assertThat(config.size()).isEqualTo(2);
    // touch but scheduled check not run yet
    touchFiles(files);
    // wait until scheduled check has been run
    sleep(3000);

    // properties loaded as expected
    assertThat(config.size()).isGreaterThan(2);
    assertThat(config.get("one", null)).isEqualTo("a");
    assertThat(config.getInt("my.size", 42)).isEqualTo(17);
    assertThat(config.getBool("c.active", false)).isTrue();
  }

  @Test
  void test_check_whenFileWritten() throws Exception {

    CoreConfiguration config = newConfig();
    List<File> files = files();

    final FileWatch watch = new FileWatch(config, files, new YamlLoaderSnake());
    touchFiles(files);
    watch.check();

    // properties loaded as expected
    final int size0 = config.size();
    assertThat(size0).isGreaterThan(2);
    assertThat(config.get("one", null)).isEqualTo("a");

    writeContent("one=NotA");
    sleep(20);
    //assertThat(watch.changed()).isTrue();
    watch.check();
    assertThat(watch.changed()).isFalse();
    if ("a".equals(config.get("one", null))) {
      sleep(20);
    }
    assertThat(config.get("one", null)).isEqualTo("NotA");

    writeContent("one=a");
    sleep(20);
    watch.check();
    assertThat(config.get("one", null)).isEqualTo("a");
  }

  private void writeContent(String content) throws IOException {
    sleep(20);
    File aProps = new File("./src/test/resources/watch/a.properties");
    if (!aProps.exists()) {
      throw new IllegalStateException("a.properties does not exist?");
    }
    FileWriter fw = new FileWriter(aProps);
    fw.write(content);
    fw.flush();
    fw.close();
    if (!aProps.setLastModified(System.currentTimeMillis())) {
      System.err.println("setLastModified not successful");
    }
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private CoreConfiguration newConfig() {
    final Properties properties = new Properties();
    properties.setProperty("config.watch.delay", "1");
    properties.setProperty("config.watch.period", "1");
    return new CoreConfiguration(properties);
  }

  private List<File> files() {
    List<File> files = new ArrayList<>();
    files.add(new File("./src/test/resources/watch/a.properties"));
    files.add(new File("./src/test/resources/watch/b.yaml"));
    files.add(new File("./src/test/resources/watch/c.yml"));
    return files;
  }

  private void touchFiles(List<File> files) {
    sleep(50);
    for (File file : files) {
      if (!file.setLastModified(System.currentTimeMillis())) {
        System.err.println("touch setLastModified not successful");
      }
    }
  }
}
