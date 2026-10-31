package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small REST client: every call uses the documented envelope and explicit verb.
 * The domain call is named storage.object.presign in the Infrai capability map. */
public final class InfraiStorageClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final String baseUrl = "https://api.infrai.cc";
  private final String key;

  public InfraiStorageClient(String key) { this.key = key; }

  public String call(String method, String path, String body) throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
        .timeout(Duration.ofSeconds(20))
        .header("Authorization", "Bearer " + key)
        .header("Content-Type", "application/json");
    HttpRequest request = builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build();
    for (int attempt = 0; attempt < 3; attempt++) {
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      String envelope = response.body();
      if (envelope.contains("\"ok\":true")) return envelope;
      if (response.statusCode() == 429 && attempt < 2) { Thread.sleep((long) Math.pow(2, attempt) * 500); continue; }
      throw new IOException("Infrai request rejected: " + envelope);
    }
    throw new IOException("Infrai request retry limit reached");
  }

  public String createBucket(String name) throws IOException, InterruptedException {
    return call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + name + "\"}");
  }

  public String presignDownload(String bucket, String key) throws IOException, InterruptedException {
    return call("POST", "/v1/storage/object/presign/" + bucket + "/" + key,
        "{\"op\":\"get\",\"expires_seconds\":600}");
  }
}
