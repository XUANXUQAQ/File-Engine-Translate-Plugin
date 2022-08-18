package file.engine.translate.Plugin.threadPool;

import java.util.concurrent.*;

public class CachedThreadPool {
    private static class CachedThreadPoolBuilder {
        private static final CachedThreadPool INSTANCE = new CachedThreadPool();
    }

    public static CachedThreadPool getInstance() {
        return CachedThreadPoolBuilder.INSTANCE;
    }

    private final ExecutorService cachedThreadPool = new ThreadPoolExecutor(
            0,
            100,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>());

    public void execute(Runnable task) {
        cachedThreadPool.execute(task);
    }

    public void shutdown() {
        cachedThreadPool.shutdownNow();
    }
}
