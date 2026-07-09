import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Shared scaffolding for the render benchmarks. Deliberately free of any
 * dependency upon the game, so that the one file compiles against both the pre-
 * and post-refactor source trees.
 *
 * @author dylan
 */
public final class BenchSupport {

  private BenchSupport() {
  }

  public static String ms(long nanos) {
    return String.format("%.2f", nanos / 1e6);
  }

  private static long percentile(List<Long> sorted, double p) {
    if (sorted.isEmpty()) {
      return 0;
    }
    int i = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
    return sorted.get(Math.min(Math.max(i, 0), sorted.size() - 1));
  }

  private static long sum(List<Long> xs) {
    long t = 0;
    for (long x : xs) {
      t += x;
    }
    return t;
  }

  /**
   * Times a cycle in isolation, with the JIT warmed. Reports nanoseconds per
   * iteration on stdout as a key=value pair.
   */
  public static void throughput(String key, Runnable cycle, int warmup, int iters) {
    for (int i = 0; i < warmup; ++i) {
      cycle.run();
    }
    long t0 = System.nanoTime();
    for (int i = 0; i < iters; ++i) {
      cycle.run();
    }
    System.out.println(key + "=" + (System.nanoTime() - t0) / iters);
  }

  /**
   * Drives the render cycle from a Swing Timer on the EDT, while an off-thread
   * scheduler posts a synthetic keystroke to the EDT every inputPollMs and
   * times how long it waits before it runs.
   *
   * <p>
   * The paint and the keystroke dispatch on the one thread, so a paint which
   * overruns its period delays every keystroke queued behind it. That is
   * precisely CheerpJ's problem reproduced on the desktop: the browser
   * multiplexes every Java thread onto its own single thread, so paint time is
   * taken directly out of input handling.</p>
   *
   * <p>
   * The latency is measured from the moment the keystroke is posted to the
   * moment the EDT runs it, which is exactly the delay a player feels. It is
   * measured off-thread on purpose: a Swing Timer would coalesce, and its
   * period is floored by the platform's timer granularity, some 15.6ms on
   * Windows, which would swamp the figure being sought.</p>
   *
   * <p>
   * Run under -Xint -XX:ActiveProcessorCount=1 to inflate the paint cost into
   * the regime CheerpJ inhabits. Under a warm JIT the paint occupies so little
   * of the frame period that the EDT is idle when the keystroke arrives, and
   * every latency reads as zero.</p>
   */
  public static void edtContention(String prefix, Runnable cycle, int frameMs,
          int inputPollMs, int seconds) throws Exception {

    final List<Long> paints = Collections.synchronizedList(new ArrayList<>());
    final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    final CountDownLatch done = new CountDownLatch(1);

    ScheduledExecutorService keys = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "synthetic-input");
      t.setDaemon(true);
      return t;
    });

    SwingUtilities.invokeAndWait(() -> {
      Timer frame = new Timer(frameMs, e -> {
        long t0 = System.nanoTime();
        cycle.run();
        paints.add(System.nanoTime() - t0);
      });
      Timer stop = new Timer(seconds * 1000, e -> {
        frame.stop();
        done.countDown();
      });
      stop.setRepeats(false);
      frame.start();
      stop.start();
    });

    keys.scheduleAtFixedRate(() -> {
      final long posted = System.nanoTime();
      SwingUtilities.invokeLater(() -> latencies.add(System.nanoTime() - posted));
    }, 0, inputPollMs, TimeUnit.MILLISECONDS);

    done.await();
    keys.shutdownNow();

    long wallNs = seconds * 1_000_000_000L;
    long paintNs = sum(paints);

    List<Long> p = new ArrayList<>(paints);
    List<Long> l = new ArrayList<>(latencies);
    Collections.sort(p);
    Collections.sort(l);

    System.out.println(prefix + "frames=" + p.size() + "/" + (seconds * 1000 / frameMs));
    System.out.println(prefix + "paint_mean_ms=" + ms(p.isEmpty() ? 0 : paintNs / p.size()));
    System.out.println(prefix + "paint_p99_ms=" + ms(percentile(p, 99)));
    System.out.println(prefix + "edt_busy_pct=" + String.format("%.1f", 100.0 * paintNs / wallNs));
    System.out.println(prefix + "input_lat_p50_ms=" + ms(percentile(l, 50)));
    System.out.println(prefix + "input_lat_p99_ms=" + ms(percentile(l, 99)));
    System.out.println(prefix + "input_lat_max_ms=" + ms(l.isEmpty() ? 0 : l.get(l.size() - 1)));
  }
}
