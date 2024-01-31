package io.avaje.aws.appconfig;

final class DResult implements AppConfigFetcher.Result {

  private final String version;
  private final String contentType;
  private final String body;
  public DResult(String version, String contentType, String body) {
    this.version = version;
    this.contentType = contentType;
    this.body = body;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public String body() {
    return body;
  }
}
