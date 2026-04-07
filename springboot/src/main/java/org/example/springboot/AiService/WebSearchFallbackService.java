package org.example.springboot.AiService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class WebSearchFallbackService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    /**
     * 当本地知识不足时，使用公开搜索页作为兜底文本上下文。
     */
    public String searchWebSummary(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String targetUrl = "https://r.jina.ai/http://duckduckgo.com/?q=" + encoded;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }
            String body = response.body();
            if (!StringUtils.hasText(body)) {
                return "";
            }
            // 限制注入长度，避免提示词过长。
            String normalized = body.replaceAll("\\s{2,}", " ").trim();
            return normalized.length() > 1500 ? normalized.substring(0, 1500) : normalized;
        } catch (Exception e) {
            log.warn("联网检索兜底失败: {}", e.getMessage());
            return "";
        }
    }
}
