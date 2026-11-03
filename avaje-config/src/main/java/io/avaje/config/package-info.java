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
 * <h2>Loading into System properties</h2>
 * <p>
 * If <code>config.load.systemProperties</code> is set to <code>true</code>
 * then all the properties are loaded into System properties.
 * </p>
 *
 * <h2>Re-evaluating expressions after plugins</h2>
 * <p>
 * Expressions like <code>${foo}</code> are resolved on the initial load. If they reference
 * a property supplied later by a plugin (e.g. AWS AppConfig) they remain unresolved.
 * Setting <code>config.eval.afterPlugins=true</code> re-evaluates the unresolved expressions
 * once, after all plugins have been applied.
 * </p>
 * <p>
 * This only occurs during initialisation, no change events are published and the
 * re-evaluated values are not loaded into System properties.
 * </p>
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
 * (which is in seconds). For example setting
 * <code>config.watch.period=10</code> means the files are
 * checked every every 10 seconds.
 * <p>
 * By default there is an initial delay of 60 seconds. We can
 * change this by setting <code>config.watch.delay</code>.
 * <p>
 * This can provide us a simple "feature toggle" mechanism.
 *
 * <pre>{@code
 *
 *   // we can toggle this on/off by editing the
 *   // appropriate property in the configuration file
 *   if (Config.enabled("feature.cleanup", false)) {
 *     ...
 *   }
 *
 * }</pre>
 */
package io.avaje.config;