package org.shotrush.atom.core;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.shotrush.atom.Atom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class PerformanceMonitor {
    private final JavaPlugin plugin;
    private final ConcurrentHashMap<String, MetricData> metrics;
    private final AtomicLong totalTicks;
    private long lastReportTime;
    
    public PerformanceMonitor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.metrics = new ConcurrentHashMap<>();
        this.totalTicks = new AtomicLong(0);
        this.lastReportTime = System.currentTimeMillis();
    }
    
    public void startMonitoring() {
        Atom.getInstance().getSchedulerManager().runGlobalTimer(() -> {
            totalTicks.incrementAndGet();
            
            if (System.currentTimeMillis() - lastReportTime > 60000) {
                reportMetrics();
                lastReportTime = System.currentTimeMillis();
            }
        }, 1, 1);
    }
    
    public void recordMetric(String name, long durationNanos) {
        metrics.computeIfAbsent(name, k -> new MetricData()).record(durationNanos);
    }
    
    public TimedOperation startOperation(String name) {
        return new TimedOperation(name, this);
    }
    
    private void reportMetrics() {
        if (metrics.isEmpty()) return;
        
        plugin.getLogger().info("=== Performance Report ===");
        metrics.forEach((name, data) -> {
            double avgMs = data.getAverageMs();
            long count = data.getCount();
            plugin.getLogger().info(String.format("%s: %.3fms avg (%d calls)", name, avgMs, count));
        });
        
        long displayCount = Atom.getInstance().getDisplayManager().getDisplayGroups().estimatedSize();
        long animationCount = Atom.getInstance().getDisplayManager().getAnimations().estimatedSize();
        long interactionCount = Atom.getInstance().getInteractionManager().getInteractions().estimatedSize();
        
        plugin.getLogger().info(String.format("Active: %d displays, %d animations, %d interactions", 
            displayCount, animationCount, interactionCount));
        
        metrics.clear();
    }
    
    public static class MetricData {
        private final AtomicLong totalNanos = new AtomicLong(0);
        private final AtomicLong count = new AtomicLong(0);
        
        public void record(long nanos) {
            totalNanos.addAndGet(nanos);
            count.incrementAndGet();
        }
        
        public double getAverageMs() {
            long c = count.get();
            return c == 0 ? 0 : (totalNanos.get() / (double) c) / 1_000_000.0;
        }
        
        public long getCount() {
            return count.get();
        }
    }
    
    public static class TimedOperation implements AutoCloseable {
        private final String name;
        private final PerformanceMonitor monitor;
        private final long startTime;
        
        public TimedOperation(String name, PerformanceMonitor monitor) {
            this.name = name;
            this.monitor = monitor;
            this.startTime = System.nanoTime();
        }
        
        @Override
        public void close() {
            long duration = System.nanoTime() - startTime;
            monitor.recordMetric(name, duration);
        }
    }
}
