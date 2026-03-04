package com.creati.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 프로젝트 루트의 .env 파일에서 환경변수를 로드하는 유틸리티.
 *
 * 사용법:
 *   String apiKey = EnvLoader.get("GEMINI_API_KEY");
 *
 * .env 파일 형식:
 *   GEMINI_API_KEY=AIza...
 *   OTHER_KEY=value
 */
public class EnvLoader {

    private static final Map<String, String> SYS_ENV = new HashMap<>(System.getenv());

    /**
     * 키를 조회할 때마다 .env 파일을 다시 읽어 반환합니다.
     * 우선순위: 시스템 환경변수 > .env 파일
     */
    public static String get(String key) {
        // 1순위: 시스템 환경변수
        if (SYS_ENV.containsKey(key)) return SYS_ENV.get(key);

        // 2순위: .env 파일 (매번 새로 읽음)
        File envFile = findEnvFile();
        if (envFile == null || !envFile.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 1) continue;
                String k = line.substring(0, eq).trim();
                if (!k.equals(key)) continue;
                String value = line.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (Exception e) {
            System.err.println("[EnvLoader] .env 파일 읽기 실패: " + e.getMessage());
        }
        return null;
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return v != null ? v : defaultValue;
    }

  
    private static File findEnvFile() {
        // 이클립스 실행 시 working directory = 프로젝트 루트
        File f = new File(".env");
        if (f.exists()) return f;

        // 혹시 다른 경로면 한 단계 위도 확인
        f = new File("../.env");
        if (f.exists()) return f;

        return null;
    }

    private EnvLoader() {}
}