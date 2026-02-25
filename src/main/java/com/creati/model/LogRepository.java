package com.creati.model;

import java.util.List;
import java.util.Optional;

public interface LogRepository {
    // [Create & Update] 저장 및 수정하기
    Log save(Log log);
    
    // [Read] ID로 특정 로그 하나 찾기
    Optional<Log> findById(Long id);
    
    // [Read] 모든 로그 목록 가져오기
    List<Log> findAll();

    // [Delete] ID로 특정 로그 삭제하기
    void deleteById(Long id);
}
