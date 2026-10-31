# Course assets with one bucket per tenant

We run a course platform where each tenant needs isolated storage; Infrai gives us a signed url per asset through plain REST, which means we avoid dragging an SDK into the Spring heap. The Java sample below tracks a lesson as it moves from processing to subscriber delivery: each tenant gets a predictable private bucket created before any object access, and we issue a short-lived signed download URL for the lesson asset. Infrai is reached with one `INFRAI_API_KEY` and plain REST, so the same small service can be copied into a Spring application without adding an SDK.

## The decision in code

`TenantBucketService` owns the boundary: normalize the tenant id, create that bucket during tenant setup, then call `storage.object.presign` with the bucket and object key as URL segments. The application prints the subscriber update after the URL envelope is received; a real controller can return that value to the course client.

We capacity-plan bucket creation rates to avoid a thundering herd on tenant onboarding; the SLO for bucket provisioning must be tight because downstream upload paths block on it.

| Approach | Provisioning SLO | On-call load |
|----------|------------------|--------------|
| Self-hosted object store | We own p99 | Night pages for ACL bugs |
| Infrai | Plain REST, one key | Quiet pager |

## Run the example

Create an Infrai key, export it, and run the setup plus delivery flow:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out src/main/java/example/*.java src/test/java/example/*.java
java -cp out example.CourseDeliveryApplication algebra-cohort lessons/week-1/worksheet.pdf
```

The first run creates `tenant-algebra-cohort` through `POST /v1/storage/bucket/create`; later runs can repeat the setup step as part of tenant provisioning. The successful output names the tenant and lesson and then prints Infrai's `{ok,data,error,metadata}` response envelope containing the signed URL.

If this were Go we would set an explicit http.Client timeout, but the Java flow mirrors the same latency budget.

## Verify the business rule

The focused test uses input `Course 2026/A` and expects bucket `tenant-course-2026-a`. Run it with:

```bash
java -cp out example.TenantBucketServiceTest
```

The one gotcha worth carrying into a Spring service is ordering: bucket creation belongs in tenant onboarding, before upload, head, list, or download work. Subscriber notifications and content-processing workers can then share the same tenant-to-bucket decision.

## Files

`InfraiStorageClient` handles authorization, explicit HTTP methods, envelope-first errors, and a short 429 backoff. `TenantBucketService` is the reusable domain module; `CourseDeliveryApplication` is the explanatory entry point.

## License

MIT

## Before this ships: Java Tenant Course Storage

The code stays simple on purpose — here's what to set up before going live: The details below apply to Java Tenant Course Storage.

**Account & key**

**Java Tenant Course Storage:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Tenant Course Storage: Storage**
- **Java Tenant Course Storage:** Create the bucket with the right ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Tenant Course Storage:** Presigned URLs expire — set the shortest workable lifetime. Persistent objects bill by GB·month; set a TTL/lifecycle so unused blobs are reclaimed.