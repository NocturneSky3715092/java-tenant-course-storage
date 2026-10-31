package example;

/** Runnable path for a lesson asset becoming available to a subscriber. */
public final class CourseDeliveryApplication {
  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("INFRAI_API_KEY");
    if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY before running");
    String tenant = args.length > 0 ? args[0] : "algebra-cohort";
    String asset = args.length > 1 ? args[1] : "lessons/week-1/worksheet.pdf";
    TenantBucketService tenants = new TenantBucketService(new InfraiStorageClient(apiKey));
    tenants.ensureTenant(tenant);
    String urlEnvelope = tenants.deliveryUrl(tenant, asset);
    System.out.println("Subscriber update: " + tenant + " can download " + asset);
    System.out.println(urlEnvelope);
  }
}
