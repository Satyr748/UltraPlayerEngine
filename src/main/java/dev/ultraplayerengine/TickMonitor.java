package dev.ultraplayerengine;

import java.util.Arrays;

/**
 * Lightweight rolling tick-time monitor.
 *
 * It only measures server tick duration. It deliberately does not change
 * VexBot AI, movement, combat, or Minecraft tick behavior.
 */
public final class TickMonitor {
    private final double[] samples;
    private int next;
    private int size;
    private long previousTickNanos;

    public TickMonitor(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity must be at least 2");
        }
        this.samples = new double[capacity];
    }

    public synchronized void recordTick() {
        long now = System.nanoTime();
        if (previousTickNanos != 0L) {
            double mspt = (now - previousTickNanos) / 1_000_000.0;
            samples[next] = mspt;
            next = (next + 1) % samples.length;
            if (size < samples.length) {
                size++;
            }
        }
        previousTickNanos = now;
    }

    public synchronized Snapshot snapshot() {
        if (size == 0) {
            return new Snapshot(0.0, 20.0, 0.0, 0);
        }

        double[] copy = new double[size];
        for (int i = 0; i < size; i++) {
            copy[i] = samples[i];
        }

        double sum = 0.0;
        for (double value : copy) {
            sum += value;
        }

        Arrays.sort(copy);
        double average = sum / size;
        int p95Index = Math.min(size - 1, (int) Math.ceil(size * 0.95) - 1);
        double p95 = copy[p95Index];
        double tps = average <= 0.0 ? 20.0 : Math.min(20.0, 1000.0 / average);

        return new Snapshot(average, tps, p95, size);
    }

    public synchronized void reset() {
        Arrays.fill(samples, 0.0);
        next = 0;
        size = 0;
        previousTickNanos = 0L;
    }

    public record Snapshot(double averageMspt, double estimatedTps, double p95Mspt, int samples) {
    }
}
