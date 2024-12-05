## **Key Metrics Comparison**

### 1. **`http_req_blocked`**

- **What it measures**:
  The time spent in the initial phases of the HTTP request, including DNS lookup and connection establishment.

- **Low Load**:
    - `avg=9.05µs`: Requests were rarely blocked, indicating the system handled connections efficiently.
    - `max=672.25µs`: Maximum blocking time was minimal.

- **High Load**:
    - `avg=724.48µs`: A significant increase in average blocking time.
    - `max=11.2s`: Some requests were heavily delayed due to contention for resources.

- **Interpretation**:
  The system experienced high contention for sockets or DNS resolution under high load.

---

### 2. **`http_req_connecting`**

- **What it measures**:
  The time spent establishing a TCP connection. If a persistent connection is reused, this value is typically 0.

- **Low Load**:
    - `avg=2.03µs`: Almost all connections were reused, avoiding additional overhead.
    - `max=316µs`: Minimal time was needed for new connections.

- **High Load**:
    - `avg=712.43µs`: A significant increase in connection time, with some connections taking up to 11.2 seconds.

- **Interpretation**:
  Under high load, the system struggled to establish new connections, likely due to resource exhaustion.

---

### 3. **`http_req_duration`**

- **What it measures**:
  The total time taken for a request, from the moment it's sent until the response is fully received. It includes all
  phases of the HTTP transaction.

- **Low Load**:
    - `avg=7.4ms`: Responses were quick and consistent.
    - `p(95)=9.46ms`: 95% of requests completed within ~9.5ms.

- **High Load**:
    - `avg=2.11s`: A drastic slowdown, with many requests taking over 2 seconds.
    - `p(95)=3.7s`: 5% of requests took longer than 3.7 seconds.

- **Interpretation**:
  The server became significantly slower under high load, likely due to overwhelmed processing resources.

---

### 4. **`http_req_waiting`**

- **What it measures**:
  The time spent waiting for the server to send the first byte of the response (time-to-first-byte).

- **Low Load**:
    - `avg=7.34ms`: Minimal waiting time, indicating efficient server processing.
    - `p(95)=9.38ms`: Most requests were processed within 9.4ms.

- **High Load**:
    - `avg=2.11s`: Waiting time skyrocketed, with some requests delayed up to 15.43 seconds.

- **Interpretation**:
  Under high load, the server struggled to process requests, leading to long delays in response generation.

---

### 5. **`http_req_receiving`**

- **What it measures**:
  The time spent downloading the response body from the server.

- **Low Load**:
    - `avg=49.44µs`: Fast response transmission.
    - `max=556.42µs`: Even the longest transmissions were quick.

- **High Load**:
    - `avg=46.27µs`: Transmission speed remained consistent.
    - `max=4.88ms`: Slight increase in maximum time.

- **Interpretation**:
  The server was still able to transmit responses efficiently under load.

---

### 6. **`http_req_sending`**

- **What it measures**:
  The time spent sending the request payload to the server.

- **Low Load**:
    - `avg=17.4µs`: Requests were sent almost instantly.
    - `max=607.52µs`: Minimal delay in sending.

- **High Load**:
    - `avg=18.87µs`: A slight increase in average sending time.
    - `max=4.95ms`: Marginally higher delays.

- **Interpretation**:
  Network congestion slightly impacted sending times, but it wasn't a significant bottleneck.

---

### 7. **`iterations`**

- **What it measures**:
  The total number of test script executions (iterations) completed by all virtual users.

- **Low Load**:
    - `3746 iterations`: Reflects a light workload with ~75 iterations per second.
- **High Load**:
    - `93443 iterations`: Much higher workload, with ~1168 iterations per second.

- **Interpretation**:
  Despite the high number of iterations in the high-load test, the server’s response time degraded significantly.

---

### 8. **`vus` (Virtual Users)**

- **What it measures**:
  The number of concurrent virtual users (VUs) simulating real users.

- **Low Load**:
    - `min=1, max=10`: Simulated a light workload with a maximum of 10 VUs.
- **High Load**:
    - `min=2, max=10000`: Simulated a heavy workload with up to 10,000 VUs.

- **Interpretation**:
  High VUs created significant contention for server resources, leading to degraded performance.

---

### 9. **`http_req_failed`**

- **What it measures**:
  The percentage of failed requests (non-200 HTTP status codes).

- **Low Load**:
    - `0.00%`: No failed requests.
- **High Load**:
    - `1.76%`: 1650 requests failed.

- **Interpretation**:
  Server failure rates increased under high load, possibly due to resource exhaustion or timeout errors.

---

## **Key Takeaways**

- **Low Load**: The system performed exceptionally well, with minimal delays and no failures.
- **High Load**: Significant degradation occurred:
    - Blocking, connecting, and waiting times skyrocketed due to resource contention.
    - The server struggled to handle the increased number of VUs, leading to slower response times and higher failure
      rates.

Let me know if you want further clarification on any of these metrics!