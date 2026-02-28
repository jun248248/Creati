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

    private static final Map<String, String> ENV = new HashMap<>();

    static {
        // 1순위: 시스템 환경변수 (서버 배포 환경)
        ENV.putAll(System.getenv());

        // 2순위: 프로젝트 루트의 .env 파일 (로컬 개발 환경)
        File envFile = findEnvFile();
        if (envFile != null && envFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    // 빈 줄, 주석(#) 무시
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq < 1) continue;
                    String key   = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    // 값에 따옴표가 있으면 제거 ("value" → value)
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                    // 시스템 환경변수가 있으면 덮어쓰지 않음
                    ENV.putIfAbsent(key, value);
                }
            } catch (Exception e) {
                System.err.println("[EnvLoader] .env 파일 읽기 실패: " + e.getMessage());
            }
        } else {
            System.out.println("[EnvLoader] .env 파일 없음 → 시스템 환경변수만 사용");
        }
    }

    /**
     * 키에 해당하는 값을 반환. 없으면 null.
     */
    public static String get(String key) {
        return ENV.get(key);
    }

    /**
     * 키에 해당하는 값을 반환. 없으면 defaultValue.
     */
    public static String get(String key, String defaultValue) {
        return ENV.getOrDefault(key, defaultValue);
    }

    /**
     * .env 파일을 찾는다.
     * 실행 위치(working directory) 기준으로 탐색.
     */
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