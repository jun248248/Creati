package com.creati.service;

import java.time.LocalDateTime;
import java.util.Optional;
import com.creati.model.Log;
import com.creati.model.LogRepository;

public class LogService {
    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    // [Create] 저장 버튼 클릭 시 호출
    public Log createLog(Log log) {
        LocalDateTime now = LocalDateTime.now();
        log.setL_created_at(now); // 작성 시간 기록
        log.setL_updated_at(now); // 생성 시 수정 시간도 기록
        return logRepository.save(log);
    }

    // [Update] 수정 후 저장 버튼 클릭 시 호출	
    public void updateLog(Long logId, Log updatedData) {
        Optional<Log> logOptional = logRepository.findById(logId);
        
        if (logOptional.isPresent()) {
            Log existingLog = logOptional.get();
            
            // 명세서 상의 모든 수정 가능 필드 교체
            existingLog.setL_title(updatedData.getL_title());
            existingLog.setL_content_title(updatedData.getL_content_title());
            existingLog.setL_content_url(updatedData.getL_content_url());
            existingLog.setL_try_content(updatedData.getL_try_content());
            existingLog.setL_result_status(updatedData.getL_result_status());
            existingLog.setL_fail_result(updatedData.getL_fail_result());
            existingLog.setL_failure_reason(updatedData.getL_failure_reason());
            existingLog.setL_is_public(updatedData.getL_is_public());
            existingLog.setL_is_draft(updatedData.getL_is_draft());
            
            // 현재 시간을 수정 시간으로 기록
            existingLog.setL_updated_at(LocalDateTime.now()); 
            
            logRepository.save(existingLog);
        }
    }
    
    // [Read] 조회
    public Optional<Log> getLogDetail(Long logId) {
        Optional<Log> log = logRepository.findById(logId);
        log.ifPresent(l -> l.setL_view_count(l.getL_view_count() + 1));
        return log;
    }
    
 // [Delete] 로그 삭제
    public boolean deleteLog(Long logId) {
        // 1. 해당 로그가 실제로 존재하는지 먼저 확인합니다.
        Optional<Log> logOptional = logRepository.findById(logId);
        
        if (logOptional.isPresent()) {
            // 2. 존재한다면 Repository를 통해 삭제를 진행합니다.
            logRepository.deleteById(logId);
            return true; // 삭제 성공
        }
        
        return false; // 해당 ID의 로그가 없어 삭제 실패
    }
}