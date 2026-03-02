package com.creati.ui.main;

import com.creati.dao.LogDao;
import com.creati.dto.MyLogListDto;
import com.creati.dto.PublicLogListDto;
import com.creati.model.LogPost;
import com.creati.model.LogStatus;
import com.creati.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DB-backed LogRepository.
 *
 * list():
 *   - 로그인 상태: 내 로그(공개 + 비밀글) 모두 반환 → AI 분석 로그 선택 시 내 글이 전부 보여야 하므로
 *   - 비로그인:   공개 로그만 반환 (기존 동작 유지)
 *   - ID 기준 중복 제거 적용
 *
 * getById(): l_id 로 단건 상세 조회 (기존과 동일)
 */
public class LogRepositoryDb implements LogRepository {

    private final LogDao logDao;

    public LogRepositoryDb(LogDao logDao) {
        this.logDao = logDao;
    }

    // ─────────────────────────────────────────────
    // 목록 조회
    // ─────────────────────────────────────────────
    @Override
    public List<LogPost> list() {
        User user = AppState.get().getCurrentUser();
        if (user != null && user.getId() != null && !user.getId().isBlank()) {
            return listMyLogs(user.getId());
        }
        return listPublicLogs();
    }

    // ─────────────────────────────────────────────
    // 단건 조회
    // ─────────────────────────────────────────────
    @Override
    public LogPost getById(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            long lId = Long.parseLong(id);
            return logDao.findPostById(lId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void upsert(LogPost post) {
        // 추후 구현
    }

    // ─────────────────────────────────────────────
    // private helpers
    // ─────────────────────────────────────────────

    /** 로그인 사용자의 로그 (공개 + 비밀) — ID 중복 제거 포함 */
    private List<LogPost> listMyLogs(String userId) {
        List<MyLogListDto> rows = logDao.findMyLogList(userId, null);
        // LinkedHashMap으로 ID 기준 중복 제거 (첫 번째 항목 유지)
        Map<Long, LogPost> dedup = new LinkedHashMap<>();
        for (MyLogListDto r : rows) {
            if (dedup.containsKey(r.getId())) continue;   // 중복 스킵
            LogStatus status = mapDbStatus(r.getResultStatus());
            LocalDate created = (r.getCreatedAt() != null)
                    ? r.getCreatedAt().toLocalDate() : LocalDate.now();
            String category = (r.getCategoryName() != null && !r.getCategoryName().isBlank())
                    ? r.getCategoryName() : "기타";
            dedup.put(r.getId(), new LogPost(
                    LogPost.TYPE_LOG,
                    String.valueOf(r.getId()),
                    category,
                    "",
                    status,
                    r.getTitle(),
                    created,
                    r.isPublic(),
                    null, null, null, null, null, null
            ));
        }
        return new ArrayList<>(dedup.values());
    }

    /** 비로그인 시 공개 로그만 — ID 중복 제거 포함 */
    private List<LogPost> listPublicLogs() {
        List<PublicLogListDto> rows = logDao.findAllPublicLogs();
        Map<Long, LogPost> dedup = new LinkedHashMap<>();
        for (PublicLogListDto r : rows) {
            if (dedup.containsKey(r.getId())) continue;
            LogStatus status = mapDbStatus(r.getResultStatus());
            LocalDate created = (r.getCreatedAt() != null)
                    ? r.getCreatedAt().toLocalDate() : LocalDate.now();
            String field = (r.getFieldName() != null && !r.getFieldName().isBlank())
                    ? r.getFieldName() : "기타";
            LogPost lp = new LogPost(
                    LogPost.TYPE_LOG,
                    String.valueOf(r.getId()),
                    field,
                    "",
                    status,
                    r.getTitle(),
                    created,
                    true,
                    null, null, null, null, null, null
            );
            lp.authorId = r.getUserId(); // ← 작성자 ID 세팅
            dedup.put(r.getId(), lp);
        }
        return new ArrayList<>(dedup.values());
    }

    private LogStatus mapDbStatus(String s) {
        if ("SUCCESS".equals(s)) return LogStatus.DONE;
        if ("FAIL".equals(s))    return LogStatus.NEEDS_IMPROVEMENT;
        return LogStatus.IN_PROGRESS;
    }
}