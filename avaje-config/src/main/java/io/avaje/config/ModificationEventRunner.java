package io.avaje.config;

/**
 * Run the event listener notifications.
 * <p>
 * Supply this using service loading to for example run the event listener notification
 * in the background using an {@link java.util.concurrent.ExecutorService}.
 * <p>
 * The default is for event listener notification to be executed using the same thread
 * that is making the modifications to the configuration.
 */
public interface ModificationEventRunner extends ConfigExtension {

  /**
   * Run the task of notifying all the event listeners of a modification event
   * to the configuration.
   *
   * @param onChangeNotifyTask The task to be executed notifying listeners of changes
   *                           to the configuration.
   */
  void run(Runnable onChangeNotifyTask);
}
