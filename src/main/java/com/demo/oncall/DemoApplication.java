package com.demo.oncall;

import com.newrelic.api.agent.NewRelic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController
public class DemoApplication {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    // --- Simulated User Database ---

    private static final Map<String, UserProfile> USER_DB = new LinkedHashMap<>();

    static {
        USER_DB.put("1001", new UserProfile("1001", "Alice Johnson", "alice@example.com", "123 Main St, Springfield, IL 62704"));
        USER_DB.put("1002", new UserProfile("1002", "Bob Martinez", "bob@example.com", null));
        USER_DB.put("1003", new UserProfile("1003", "Charlie Davis", "charlie@example.com", "789 Oak Ave, Portland, OR 97201"));
    }

    // --- Endpoints ---

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String homePage() {
        StringBuilder userOptions = new StringBuilder();
        for (var entry : USER_DB.entrySet()) {
            userOptions.append(String.format(
                "<option value=\"%s\">%s (ID: %s)</option>",
                entry.getKey(), entry.getValue().name, entry.getKey()
            ));
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Order Processing System</title>
                <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
                <style>
                    body { background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); min-height: 100vh; }
                    .glass { background: rgba(255,255,255,0.05); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); }
                    .pulse-red { animation: pulse-red 2s infinite; }
                    @keyframes pulse-red { 0%%, 100%% { box-shadow: 0 0 0 0 rgba(239,68,68,0.4); } 50%% { box-shadow: 0 0 0 10px rgba(239,68,68,0); } }
                </style>
            </head>
            <body class="text-white">
                <div class="max-w-2xl mx-auto py-16 px-4">
                    <div class="text-center mb-10">
                        <h1 class="text-4xl font-bold mb-2">Order Processing System</h1>
                        <p class="text-gray-400">On-Call Agent Demo &mdash; New Relic Monitored</p>
                        <span class="inline-block mt-3 px-3 py-1 bg-green-900 text-green-300 text-xs rounded-full">
                            ● New Relic APM Active
                        </span>
                    </div>

                    <div class="glass rounded-2xl p-8 mb-8">
                        <h2 class="text-xl font-semibold mb-6">Submit New Order</h2>
                        <form id="orderForm" class="space-y-5">
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Customer</label>
                                <select name="userId" id="userId"
                                    class="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-3 text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                                    """ + userOptions.toString() + """
                                </select>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Product</label>
                                <select name="product" id="product"
                                    class="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-3 text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                                    <option value="Laptop Pro 16">Laptop Pro 16 — $1,299</option>
                                    <option value="Wireless Headphones">Wireless Headphones — $199</option>
                                    <option value="Mechanical Keyboard">Mechanical Keyboard — $149</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Quantity</label>
                                <input type="number" name="quantity" id="quantity" value="1" min="1" max="99"
                                    class="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-3 text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                            </div>
                            <button type="submit"
                                class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition-colors">
                                Process Order
                            </button>
                        </form>
                    </div>

                    <div id="result" class="hidden glass rounded-2xl p-8 mb-8"></div>

                    <div class="glass rounded-2xl p-6">
                        <h3 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Customer Database</h3>
                        <div class="space-y-3">
                            <div class="flex justify-between items-center p-3 bg-gray-800 rounded-lg">
                                <div><span class="font-medium">Alice Johnson</span> <span class="text-gray-500 text-sm ml-2">ID: 1001</span></div>
                                <span class="text-xs px-2 py-1 bg-green-900 text-green-300 rounded">Address: OK</span>
                            </div>
                            <div class="flex justify-between items-center p-3 bg-gray-800 rounded-lg border border-red-900">
                                <div><span class="font-medium">Bob Martinez</span> <span class="text-gray-500 text-sm ml-2">ID: 1002</span></div>
                                <span class="text-xs px-2 py-1 bg-red-900 text-red-300 rounded pulse-red">Address: MISSING</span>
                            </div>
                            <div class="flex justify-between items-center p-3 bg-gray-800 rounded-lg">
                                <div><span class="font-medium">Charlie Davis</span> <span class="text-gray-500 text-sm ml-2">ID: 1003</span></div>
                                <span class="text-xs px-2 py-1 bg-green-900 text-green-300 rounded">Address: OK</span>
                            </div>
                        </div>
                        <p class="text-xs text-gray-500 mt-4">⚠ Selecting Bob Martinez will trigger a NullPointerException (demo purpose)</p>
                    </div>
                </div>

                <script>
                document.getElementById('orderForm').addEventListener('submit', async function(e) {
                    e.preventDefault();
                    const resultDiv = document.getElementById('result');
                    resultDiv.classList.remove('hidden');
                    resultDiv.innerHTML = '<p class="text-blue-400">Processing order...</p>';

                    const params = new URLSearchParams({
                        userId: document.getElementById('userId').value,
                        product: document.getElementById('product').value,
                        quantity: document.getElementById('quantity').value
                    });

                    try {
                        const res = await fetch('/api/process-order', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: params
                        });
                        const data = await res.json();
                        if (data.status === 'success') {
                            resultDiv.innerHTML = `
                                <div class="text-green-400">
                                    <h3 class="text-lg font-semibold mb-2">✓ Order Processed Successfully</h3>
                                    <pre class="text-sm bg-gray-900 p-4 rounded-lg overflow-auto">${JSON.stringify(data, null, 2)}</pre>
                                </div>`;
                        } else {
                            resultDiv.innerHTML = `
                                <div class="text-red-400">
                                    <h3 class="text-lg font-semibold mb-2">✗ Order Processing Failed</h3>
                                    <p class="mb-2">${data.error}</p>
                                    <pre class="text-sm bg-gray-900 p-4 rounded-lg overflow-auto">${JSON.stringify(data, null, 2)}</pre>
                                </div>`;
                        }
                    } catch (err) {
                        resultDiv.innerHTML = `<p class="text-red-400">Network error: ${err.message}</p>`;
                    }
                });
                </script>
            </body>
            </html>
            """;
    }

    @PostMapping(value = "/api/process-order", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> processOrder(
            @RequestParam String userId,
            @RequestParam String product,
            @RequestParam(defaultValue = "1") int quantity) {

        log.info("Received order request: userId={}, product={}, quantity={}", userId, product, quantity);

        try {
            UserProfile user = USER_DB.get(userId);
            if (user == null) {
                log.warn("User not found: {}", userId);
                return Map.of("status", "error", "error", "User not found: " + userId);
            }

            log.info("Processing order for user: {} ({})", user.name, user.email);

            // BUG: No null-check on user.address before calling toUpperCase().
            // User "1002" (Bob Martinez) has a null address, causing a NullPointerException here.
            String shippingLabel = formatShippingLabel(user);

            String orderId = "ORD-" + System.currentTimeMillis();
            log.info("Order {} created successfully. Shipping to: {}", orderId, shippingLabel);

            NewRelic.addCustomParameter("orderId", orderId);
            NewRelic.addCustomParameter("userId", userId);
            NewRelic.addCustomParameter("product", product);

            return Map.of(
                "status", "success",
                "orderId", orderId,
                "customer", user.name,
                "product", product,
                "quantity", quantity,
                "shippingAddress", shippingLabel,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (NullPointerException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            log.error("CRITICAL: NullPointerException while processing order for userId={}. " +
                      "This indicates a data integrity issue — the user's shipping address is null. " +
                      "Product: {}, Quantity: {}. " +
                      "Source: src/main/java/com/demo/oncall/DemoApplication.java " +
                      "Method: formatShippingLabel() " +
                      "Repo: https://github.com/yuvrajsingh16/testrepo " +
                      "Stacktrace:\n{}", userId, product, quantity, stackTrace);

            NewRelic.noticeError(e, Map.of(
                "userId", userId,
                "product", product,
                "errorType", "NullPointerException",
                "component", "OrderProcessing",
                "rootCause", "User shipping address is null in database"
            ));

            return Map.of(
                "status", "error",
                "error", "NullPointerException: failed to generate shipping label for user " + userId,
                "detail", "User profile has a null shipping address. This is a known data integrity bug.",
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        return Map.of("status", "UP", "app", "oncall-demo", "version", "1.0.0");
    }

    // --- Internal Methods ---

    private String formatShippingLabel(UserProfile user) {
        // BUG: user.address can be null — no guard here.
        // This will throw NullPointerException for users with missing address data.
        String normalizedAddress = user.address.toUpperCase();
        return user.name + "\n" + normalizedAddress;
    }

    // --- Data Model ---

    static class UserProfile {
        final String id;
        final String name;
        final String email;
        final String address;

        UserProfile(String id, String name, String email, String address) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.address = address;
        }
    }
}
