package io.avaje.config.appconfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

final class DAppConfigFetcher implements AppConfigFetcher {

  private final URI uri;
  private final HttpClient httpClient;

  DAppConfigFetcher(String uri) {
    this.uri = URI.create(uri);
    this.httpClient = HttpClient.newBuilder()
      .build();
  }

  @Override
  public AppConfigFetcher.Result fetch() throws FetchException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(uri)
      .GET()
      .build();

    try {
      HttpResponse<String> res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      String version = res.headers().firstValue("Configuration-Version").orElse(null);
      String contentType = res.headers().firstValue("Content-Type").orElse("unknown");
      String body = res.body();
      return new DResult(version, contentType, body);

    } catch (IOException | InterruptedException e) {
      throw new FetchException(e);
    }
  }

  static class Builder implements AppConfigFetcher.Builder {

    private int port = 2772;
    private String application;
    private String environment;
    private String configuration;

    @Override
    public Builder application(String application) {
      this.application = application;
      return this;
    }

    @Override
    public Builder environment(String environment) {
      this.environment = environment;
      return this;
    }

    @Override
    public Builder configuration(String configuration) {
      this.configuration = configuration;
      return this;
    }

    @Override
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    @Override
    public AppConfigFetcher build() {
      return new DAppConfigFetcher(uri());
    }

    private String uri() {
      if (configuration == null) {
        configuration = environment + "-" + application;
      }
      return "http://localhost:" + port + "/applications/"
        + application + "/environments/"
        + environment + "/configurations/"
        + configuration;
    }
  }
}
