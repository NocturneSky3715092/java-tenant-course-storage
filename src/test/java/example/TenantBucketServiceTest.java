package example;

public final class TenantBucketServiceTest {
  public static void main(String[] args) {
    TenantBucketService service = new TenantBucketService(null);
    String bucket = service.bucketFor("Course 2026/A");
    if (!"tenant-course-2026-a".equals(bucket)) throw new AssertionError(bucket);
    System.out.println("tenant bucket decision: PASS");
  }
}
