package io.perftest.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects and calculates performance metrics during test execution
 */
public class MetricsCollector {
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
    
    private final long startTime = System.currentTimeMillis();
    private long endTime = 0;
    
    /**
     * Record a request being made
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }
    
    /**
     * Record a successful request
     */
    public void recordSuccess() {
        successfulRequests.incrementAndGet();
    }
    
    /**
     * Record a failed request
     */
    public void recordFailure() {
        failedRequests.incrementAndGet();
    }
    
    /**
     * Record an error
     */
    public void recordError() {
        errorCount.incrementAndGet();
    }
    
    /**
     * Record a response time
     * 
     * @param responseTimeMs the response time in milliseconds
     */
    public void recordResponseTime(long responseTimeMs) {
        if (responseTimeMs >= 0) {
            totalResponseTime.addAndGet(responseTimeMs);
            responseTimes.add(responseTimeMs);
        }
    }
    
    /**
     * Mark the end time of the test
     */
    public void markEndTime() {
        if (endTime == 0) {
            endTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Get the total number of requests
     * 
     * @return total requests
     */
    public int getTotalRequests() {
        return totalRequests.get();
    }
    
    /**
     * Get the number of successful requests
     * 
     * @return successful requests
     */
    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }
    
    /**
     * Get the number of failed requests
     * 
     * @return failed requests
     */
    public int getFailedRequests() {
        return failedRequests.get();
    }
    
    /**
     * Get the error count
     * 
     * @return error count
     */
    public int getErrorCount() {
        return errorCount.get();
    }
    
    /**
     * Get the success rate as a percentage
     * 
     * @return success rate (0-100)
     */
    public double getSuccessRate() {
        if (totalRequests.get() == 0) {
            return 0;
        }
        return (double) successfulRequests.get() / totalRequests.get() * 100;
    }
    
    /**
     * Get the average response time
     * 
     * @return average response time in milliseconds
     */
    public double getAverageResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        return (double) totalResponseTime.get() / responseTimes.size();
    }
    
    /**
     * Get the minimum response time
     * 
     * @return minimum response time in milliseconds
     */
    public long getMinResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        synchronized (responseTimes) {
            return Collections.min(responseTimes);
        }
    }
    
    /**
     * Get the maximum response time
     * 
     * @return maximum response time in milliseconds
     */
    public long getMaxResponseTime() {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        synchronized (responseTimes) {
            return Collections.max(responseTimes);
        }
    }
    
    /**
     * Get a specific percentile of response times
     * 
     * @param percentile the percentile to get (0-100)
     * @return the response time at the specified percentile
     */
    public long getPercentile(int percentile) {
        if (responseTimes.isEmpty() || percentile < 0 || percentile > 100) {
            return 0;
        }
        
        List<Long> sortedTimes;
        synchronized (responseTimes) {
            sortedTimes = new ArrayList<>(responseTimes);
        }
        Collections.sort(sortedTimes);
        
        int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
        if (index < 0) index = 0;
        return sortedTimes.get(index);
    }
    
    /**
     * Get the total duration of the test
     * 
     * @return duration in milliseconds
     */
    public long getDuration() {
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
    
    /**
     * Get the throughput (requests per second)
     * 
     * @return throughput
     */
    public double getThroughput() {
        long duration = getDuration();
        if (duration == 0) {
            return 0;
        }
        return (double) totalRequests.get() * 1000 / duration;
    }
    
    /**
     * Get all response times
     * 
     * @return list of response times
     */
    public List<Long> getResponseTimes() {
        synchronized (responseTimes) {
            return new ArrayList<>(responseTimes);
        }
    }
}
