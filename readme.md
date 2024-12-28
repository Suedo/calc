# Demo of high performance sync communication using gRPC and Virtual Threads

This project uses a simple domain, that of a Calculator, to demo the performance of sync blocking api calls using gRPC
and Virtual threads
We first look at the BODMAS rule for calculating arithmetic, which is accompanied by a simple java code that would take
care of it.

Then we create a Microservice Architechture to mimic many small but frequent sync communications, and then see how much
we can optimize it

## 1. BODMAS rule

The **BODMAS** rule is a mathematical convention that specifies the order of operations for solving expressions. It
stands for:

- **B**: Brackets (solve expressions within brackets first)
- **O**: Orders (handle exponents or roots, such as squares, square roots, etc.)
- **D**: Division (perform division next, from left to right)
- **M**: Multiplication (perform multiplication, from left to right)
- **A**: Addition (handle addition, from left to right)
- **S**: Subtraction (handle subtraction, from left to right)

The operations are prioritized in this order, with brackets taking the highest precedence and addition/subtraction the
lowest. When operations of the same priority (e.g., division and multiplication) appear, solve them **left to right**.

### Example

Evaluate:  
\[ 6 + 2 \times (3^2 - 1) \div 4 \]

1. Solve the brackets:  
   \[ 3^2 - 1 = 9 - 1 = 8 \]  
   The expression becomes:  
   \[ 6 + 2 \times 8 \div 4 \]

2. Handle orders: Already done in step 1.

3. Perform division and multiplication (left to right):  
   \[ 2 \times 8 = 16 \]  
   \[ 16 \div 4 = 4 \]  
   The expression becomes:  
   \[ 6 + 4 \]

4. Perform addition:  
   \[ 6 + 4 = 10 \]

**Answer**: 10

## 2. Desired Microservice Architechture:

**Flow Diagram**

Based on the below flow diagram, we will refine the monolithic codebase under `BODMASCalculator` above in a more
microservice based architechture:

![flow diagram](flow.jpg)


---

1. **Generate Service**:

    - **REST Endpoint `/generate`**:
        - Generates and returns a **random infix expression** as a string (e.g., `3 + 5 * (2 - 1)`).
        - Accessible via Postman for manual testing.

2. **Evaluate Service**:

    - **gRPC Endpoint `/evaluate`**:
      This should also be Accessible over postman for manual testing
        - Accepts an infix expression as input.
        - Performs the following steps:
            1. Calls the **Tokenize Service** to convert the infix expression into a **list of POSTFIX tokens** (
               numbers, operators, brackets).
            2. Processes these tokens based on `evaluatePostfix` method from `BODMASCalculator` and calls **Calculator
               Service** over gRPC, to evaluate the final result, applying BODMAS rules.
        - Returns the calculated result.

3. **Tokenize Service**:

    - **REST based tokenization Endpoint `/tokenize`**:
        - Accepts an infix expression (e.g., `3 + 5 * (2 - 1)`).
        - Converts the infix expression into a **list of tokens** in the POSTFIX notation,
          like (`3`, `+`, `5`, `*`, `(`, `2`, `-`, `1`, `)`).
        - sends tokens back to the Evaluate Service.

4. **Calculator Service**:

    - **gRPC Endpoint with Basic Math Operations**:
        - Exposes operations like `add`, `subtract`, `multiply`, `divide`.
        - invoked as per logic in `evaluatePostfix` and `applyOperator` logic in `BODMASCalculator`

5. **Test Service**:
    - **REST Endpoint `/test`**:
        - Acts as the **integration service**:
            1. Calls the `/generate` REST endpoint to fetch a random infix expression.
            2. Sends this expression to the `/evaluate` gRPC endpoint.
            3. Returns the final result to the client.
        - Can be used to load test the complete system (`generate → tokenize → calculate`).

---

### **Revised Flow Recap**

1. **REST Endpoint** `/generate` produces the **infix expression**.
2. **REST Endpoint** `/tokenize` produces POSTFIX tokens list derived from the infix expression.
3. **gRPC Endpoint** `/evaluate` processes the token list, applies BODMAS, and calculates the result using the *
   *Calculator Service**.
4. **gRPC Endpoint** `add` `subtract` `multiply` `divide` actions exposed by **Calculator Service**
5. **REST Endpoint** `/test` integrates the flow for testing and benchmarking through load testing tools like jMeter or
   K6.

---

## **Exposing Prometheus Metrics for gRPC Servers**

### **Overview**

gRPC servers use HTTP/2 by default, but Prometheus requires an HTTP/1.x endpoint to scrape metrics. This document
outlines how to expose Prometheus metrics for a gRPC server by running a separate HTTP/1.x endpoint using Spring Boot
Actuator.

### **Problem**

When Prometheus attempts to scrape metrics from a gRPC server, the following error occurs:

```
io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception: Unexpected HTTP/1.x request: GET /actuator/prometheus
```

This happens because Prometheus sends HTTP/1.x requests, while the gRPC server only accepts HTTP/2 traffic. To resolve
this, we expose a dedicated HTTP/1.x endpoint for Prometheus metrics.

### **Solution**

#### **Expose a Separate HTTP/1.x Metrics Endpoint**

We run an HTTP server alongside the gRPC server specifically to expose Prometheus metrics.

#### **Steps**

##### **1. Add Dependencies**

Add the following dependencies to the `pom.xml` of your gRPC application:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

These dependencies:

- Enable Spring Boot Actuator to expose system metrics.
- Configure Prometheus metrics collection.

#### **2. Enable HTTP 1.X Metrics in `application.properties`**

Configure the HTTP server to run on a different port (`8191`) from the gRPC server's port (`8190`).

```properties
grpc.server.port=8190
# .. other properties 
management.server.port=8191
```

#### **3. Start the Application**

When the application starts:

- The gRPC server continues to listen on its original port.
- The HTTP server listens on the configured port (`8081`) and serves Prometheus metrics
  at `http://localhost:8081/actuator/prometheus`.

#### **4. Update Prometheus Configuration**

Update the `prometheus.yml` file to scrape the new metrics endpoint:

```yaml
scrape_configs:
  - job_name: 'grpc-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: [ 'host.docker.internal:8081' ]  # HTTP metrics endpoint
```

Restart Prometheus to apply the changes.

### **Verification**

1. **Access Metrics Endpoint:**

   You should see a list of Prometheus metrics when you vist `http://localhost:8191/actuator/prometheus`
   Take any metrics from this page, example `grpc_server_call_sent_total_compressed_message_size_bytes_bucket` and check
   in prometheus UI

### **Benefits**

- **Seamless Integration**: Allows Prometheus to scrape gRPC server metrics without modifying core gRPC logic.
- **Scalability**: Each gRPC server instance can expose its metrics on a separate HTTP endpoint.
- **Flexibility**: By using Spring Boot Actuator, additional metrics (e.g., health checks) can also be exposed.

### **Conclusion**

By running a lightweight HTTP server alongside the gRPC server, we can easily expose Prometheus-compatible metrics
without disrupting the gRPC application. This approach ensures compatibility with Prometheus and allows for detailed
observability of gRPC services.

--- 