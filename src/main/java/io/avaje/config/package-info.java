/**
 * Application configuration based on loading properties and yaml files.
 *
 * <h3>Examples</h3>
 * <pre>{@code
 *
 *  int port = Config.getInt("app.port", 8090);
 *
 *  String topicName = Config.get("app.topic.name");
 *
 *  List<Integer> codes = Config.getList().ofInt("my.codes", 42, 54);
 *
 * }</pre>
 *
 * <h2>File watching and reloading</h2>
 * <p>
 * We can enable watching configuration files by setting
 * <code>config.watch.enabled=true</code>. With this enabled
 * config will watch for modifications to the configuration files
 * and reload the configuration.
 * <p>
 * By default the files are checked every 60 seconds. We can
 * change this by setting the <code>config.watch.period</code>
 * (seconds). For example setting <code>config.watch.period=10</code>
 * means the files are checked every every 10 seconds.
 * <p>
 * By default there is an initial delay of 60 seconds. We can
 * change this by setting <code>config.watch.delay</code>.
 * <p>
 * This can provide us a quick "feature toggle" mechanism.
 *
 * <pre>{@code
 *
 *   // we can toggle this on/off by editing the
 *   // appropriate configuration file
 *   if (Config.enabled("feature.cleanup", false)) {
 *     ...
 *   }
 *
 * }</pre>
 */
package io.avaje.config;