package com.example.bajaj;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@SpringBootApplication
public class BajajApplication implements CommandLineRunner {

    private final WebClient webClient;

    public BajajApplication(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://bfhldevapigw.healthrx.co.in").build();
    }

    public static void main(String[] args) {
        SpringApplication.run(BajajApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // 1️⃣ Step 1 - Generate webhook
            Map<String, Object> response = webClient.post()
                    .uri("/hiring/generateWebhook/JAVA")
                    .bodyValue(Map.of(
                            "name", "Harshit Bihani",
                            "regNo", "22BAI10249",
                            "email", "harshitbihani2022@vitbhopal.ac.in"
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("GenerateWebhook Response: " + response);

            // Extract webhook + token
            String webhookUrl = (String) response.get("webhook");
            String accessToken = (String) response.get("accessToken");

            // 2️⃣ Step 2 - Prepare SQL Query (odd regNo → deposit)
            String finalSQL = "SELECT user_id, COUNT(*) AS total_transactions " +
                    "FROM transactions " +
                    "WHERE transaction_type = 'deposit' " +
                    "GROUP BY user_id;";

            // 3️⃣ Step 3 - Submit SQL query
            Map<String, Object> submitResponse = webClient.post()
                    .uri(webhookUrl)
                    .header("Authorization", accessToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("finalQuery", finalSQL))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Submit Response: " + submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
