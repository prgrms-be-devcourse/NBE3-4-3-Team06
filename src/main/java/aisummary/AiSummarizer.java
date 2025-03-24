package aisummary;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class AiSummarizer {
    public static void main(String[] args) throws Exception {
        String diff = Files.readString(Path.of("changes.diff"));
        String apiKey = System.getenv("OPENAI_API_KEY");

        ObjectMapper mapper = new ObjectMapper();
        String escapedDiff = mapper.writeValueAsString(diff); // JSON 문자열 escape 처리됨

        String requestBody = """
        {
          "model": "gpt-3.5-turbo",
          "messages": [
            {"role": "system", "content": "Summarize this Java code diff."},
            {"role": "user", "content": %s }
          ]
        }
        """.formatted(escapedDiff); // 여기서 escapedDiff 그대로 삽입

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("AI Summary:");
        System.out.println(response.body());
    }
}