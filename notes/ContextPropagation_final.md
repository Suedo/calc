## Micrometer based manual context propagation:

https://stackoverflow.com/a/78765658/2715083

In the @Configuration class, the `virtualThreadExecutor` method is modified to use the `ContextExecutorService` wrapper.

```java
        this.executorService =ContextExecutorService.

wrap(
        Executors.newVirtualThreadPerTaskExecutor(),
                (Supplier<ContextSnapshot>)()->ContextSnapshotFactory.

builder().

build().

captureAll()
        );
```

This ensures that the `traceId` (or any other context value) is correctly propagated across thread boundaries.