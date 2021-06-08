package pc.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Concurrent crawler.
 */
public class ConcurrentCrawler extends BaseCrawler {

    /**
     * The fork-join pool.
     */
    private final ForkJoinPool pool;

    /**
     * Mark seen URLs to avoid looping while crawling.
     */
    ConcurrentHashMap<URL, Boolean> seen = new ConcurrentHashMap<>();
    AtomicInteger rid = new AtomicInteger(0);

    /**
     * Constructor.
     *
     * @param threads number of threads.
     * @throws IOException if an I/O error occurs
     */
    public ConcurrentCrawler(int threads) throws IOException {
        pool = new ForkJoinPool(threads);
    }

    public static void main(String[] args) throws IOException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        String rootPath = args.length > 1 ? args[1] : "http://localhost:8123";
        ConcurrentCrawler cc = new ConcurrentCrawler(threads);
        cc.setVerboseOutput(true);
        cc.crawl(new URL(rootPath));
        cc.stop();
    }

    /**
     * Stop the crawler.
     */
    public void stop() {
        pool.shutdown();
    }

    @Override
    public void crawl(URL root) {
        long t = System.currentTimeMillis();
        log("Starting at %s", root);
        pool.invoke(new TransferAction(root));
        t = System.currentTimeMillis() - t;
        int n_transfers = seen.size();
        System.out.printf("Done: %d transfers in %d ms (%.2f transfers/s)%n", n_transfers,
                t, (1e+03 * n_transfers) / t);

    }

    private class TransferAction extends RecursiveAction {
        private final URL root;

        TransferAction(URL url) {
            this.root = url;
        }

        @Override
        protected void compute() {
            ForkJoinTask.invokeAll(createSubActions());
        }

        private List<TransferAction> createSubActions() {
            List<TransferAction> subtasks = new ArrayList<>();
            File htmlContents = download(rid.getAndIncrement(), root);
            if (htmlContents != null) {
                List<URL> links = parseLinks(root, htmlContents);
                Function<? super URL, ? extends Boolean> put_and_subtask = key -> subtasks.add(new TransferAction(key));
                for (URL link : links) {
                    seen.computeIfAbsent(link, put_and_subtask);
                }
            }
            return subtasks;
        }
    }
}
