package com.creati.model;

import java.util.*;

/*
 * LogRepository 인터페이스를 실제로 구현한 클래스
 * 현재는 메모리(HashMap)를 사용하여 데이터를 임시 저장합니다.
 */
public class LogRepositoryImpl implements LogRepository {
    
    // 데이터를 담아둘 가상의 데이터베이스 (메모리)
    private final Map<Long, Log> database = new HashMap<>();
    private long sequence = 0L; // 로그 ID 자동 증가를 위한 번호표

    @Override
    public Log save(Log log) {
        // 새 글인 경우 (ID가 없는 경우) ID 번호를 부여
        if (log.getL_id() == null) {
            log.setL_id(++sequence);
        }
        database.put(log.getL_id(), log);
        System.out.println("로그 저장 완료: " + log.getL_title()); // 콘솔 확인용
        return log;
    }

    @Override
    public Optional<Log> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Log> findAll() {
        // 저장된 모든 로그를 리스트로 반환
        return new ArrayList<>(database.values());
    }

    @Override
    public void deleteById(Long id) {
        database.remove(id);
    }
}