package io.avaje.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

final class FileWatch {

  private static final Logger log = LoggerFactory.getLogger(FileWatch.class);

  private final Configuration configuration;
  private final YamlLoader yamlLoader;
  private final List<Entry> files;
  private final long delay;
  private final long period;

  FileWatch(Configuration configuration, List<File> loadedFiles, YamlLoader yamlLoader) {
    this.configuration = configuration;
    this.delay = configuration.getLong("config.watch.delay", 60);
    this.period = configuration.getInt("config.watch.period", 10);
    this.yamlLoader = yamlLoader;
    this.files = initFiles(loadedFiles);
    if (files.isEmpty()) {
      log.error("No files to watch?");
    } else {
      configuration.schedule(delay * 1000, period * 1000, this::check);
    }
  }

  @Override
  public String toString() {
    return "Watch[period:" + period + " delay:" + delay + " files:" + files + "]";
  }

  private List<Entry> initFiles(List<File> loadedFiles) {
    List<Entry> entries = new ArrayList<>(loadedFiles.size());
    for (File loadedFile : loadedFiles) {
      entries.add(new Entry(loadedFile));
    }
    return entries;
  }

  boolean changed() {
    for (Entry file : files) {
      if (file.changed()) {
        return true;
      }
    }
    return false;
  }

  void check() {
    for (Entry file : files) {
      if (file.reload()) {
        log.debug("reloading configuration from {}", file);
        if (file.isYaml()) {
          reloadYaml(file);
        } else {
          reloadProps(file);
        }
      }
    }
  }

  private void reloadProps(Entry file) {
    Properties properties = new Properties();
    try (InputStream is = file.inputStream()) {
      properties.load(is);
      put(properties);
    } catch (Exception e) {
      log.error("Unexpected error reloading config file " + file, e);
    }
  }

  private void put(Properties properties) {
    Enumeration<?> enumeration = properties.propertyNames();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String property = properties.getProperty(key);
      configuration.setProperty(key, property);
    }
  }

  private void reloadYaml(Entry file) {
    if (yamlLoader == null) {
      log.error("Unexpected - no yamlLoader to reload config file " + file);
    } else {
      try (InputStream is = file.inputStream()) {
        yamlLoader.load(is).forEach(configuration::setProperty);
      } catch (Exception e) {
        log.error("Unexpected error reloading config file " + file, e);
      }
    }
  }

  private static class Entry {
    private final File file;
    private final boolean yaml;
    private long lastMod;

    Entry(File file) {
      this.file = file;
      this.lastMod = file.lastModified();
      this.yaml = isYaml(file.getName());
    }

    @Override
    public String toString() {
      return file.toString();
    }

    boolean isYaml() {
      return yaml;
    }

    private boolean isYaml(String name) {
      final String lowerName = name.toLowerCase();
      return lowerName.endsWith(".yaml") || lowerName.endsWith(".yml");
    }

    boolean reload() {
      if (!changed()) {
        return false;
      }
      lastMod = file.lastModified();
      return true;
    }

    boolean changed() {
      return file.lastModified() > lastMod;
    }

    InputStream inputStream() {
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
