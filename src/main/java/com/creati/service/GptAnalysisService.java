package com.creati.service;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class GptAnalysisService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public String analyzeDummy() throws Exception {
        // DB 연결 전이므로 임시 데이터를 보냄. DB 연결 후 프롬프트 수정 필요 #Todo
        String prompt = "너는 숙련된 크리에이터 컨설턴트야. 사용자가 유튜브 쇼츠 채널 성장이 더뎌서 고민이야. "
                      + "실패 원인 카테고리, 요약, 재도전 플랜을 포함해서 응답해줘.";
        
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"contents\": [{\"parts\":[{\"text\": \"" + prompt + "\"}]}]}";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return formatResponse(response.body());
    }

    private String formatResponse(String response) {
        try {
            // 1. 텍스트 추출 로직 (유지)
            String searchTarget = "\"text\": \"";
            int startPos = response.indexOf(searchTarget) + searchTarget.length();
            int endPos = response.indexOf("\"", startPos);
            String result = response.substring(startPos, endPos);

            // 2. 특수문자 및 공백 정화
            result = result.replace("\\n", "\n").replace("\\\"", "\"");
            result = result.replace("**", ""); // 강조 표시 제거
            
            // 3. 들여쓰기 및 지저분한 공백 제거 (핵심!)
            // 각 줄의 맨 앞에 있는 모든 공백과 탭을 삭제합니다.
            StringBuilder cleaned = new StringBuilder();
            for (String line : result.split("\n")) {
                String trimmedLine = line.trim(); // 앞뒤 공백 제거
                if (!trimmedLine.isEmpty()) {
                    // 불렛 포인트(-)가 있는 줄은 한 칸 띄워서 가독성 확보
                    if (trimmedLine.startsWith("-")) {
                        cleaned.append("  ").append(trimmedLine).append("\n\n");
                    } else {
                        cleaned.append(trimmedLine).append("\n\n");
                    }
                }
            }
            
            return cleaned.toString().trim();
        } catch (Exception e) { 
            return "데이터 가공 중 오류 발생"; 
        }
    }
}