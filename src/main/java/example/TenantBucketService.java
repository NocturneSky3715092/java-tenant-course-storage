package example;

import java.io.IOException;

public final class TenantBucketService {
  private final InfraiStorageClient storage;
  public TenantBucketService(InfraiStorageClient storage) { this.storage = storage; }
  public String bucketFor(String tenantId) { return "tenant-" + tenantId.toLowerCase().replaceAll("[^a-z0-9-]", "-"); }
  public void ensureTenant(String tenantId) throws IOException, InterruptedException { storage.createBucket(bucketFor(tenantId)); }
  public String deliveryUrl(String tenantId, String assetKey) throws IOException, InterruptedException {
    return storage.presignDownload(bucketFor(tenantId), assetKey);
  }
}
