package io.avaje.config.appconfig;

interface AppConfigFetcher {

  static AppConfigFetcher.Builder builder() {
    return new DAppConfigFetcher.Builder();
  }

  Result fetch() throws FetchException;

  class FetchException extends Exception {

    public FetchException(Exception e) {
      super(e);
    }
  }

  interface Result {

    String version();

    String contentType();

    String body();
  }

  interface Builder {

    AppConfigFetcher.Builder application(String application);

    AppConfigFetcher.Builder environment(String environment);

    AppConfigFetcher.Builder configuration(String configuration);

    AppConfigFetcher.Builder port(int port);

    AppConfigFetcher build();
  }

}
