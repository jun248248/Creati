package com.creati.ui.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.creati.dao.LogDao;
import com.creati.dto.PublicLogListDto;
import com.creati.model.LogPost;
import com.creati.model.LogStatus;

// DB-backed repository
public class LogRepositoryDb implements LogRepository {

    private final LogDao logDao;

    public LogRepositoryDb(LogDao logDao) {
        this.logDao = logDao;
    }

    @Override
    public List<LogPost> list() {
        List<PublicLogListDto> rows = logDao.findAllPublicLogs(); // ✅ 공개글만
        List<LogPost> out = new ArrayList<>();

        for (PublicLogListDto r : rows) {
            LogStatus status = mapDbStatus(r.getResultStatus());
            LocalDate created = (r.getCreatedAt() != null) ? r.getCreatedAt().toLocalDate() : LocalDate.now();

            out.add(new LogPost(
                LogPost.TYPE_LOG,
                String.valueOf(r.getId()),
                (r.getFieldName() != null && !r.getFieldName().isBlank()) ? r.getFieldName() : "기타",
                "", // subCategory
                status,
                r.getTitle(),
                created,
                true, // 공개글만

                // legacy 필드
                null, null, null, null, null, null
            ));
        }

        return out;
    }

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
        // 필요 시 추후 구현
    }

    private LogStatus mapDbStatus(String s) {
        if ("SUCCESS".equals(s)) return LogStatus.DONE;
        if ("FAIL".equals(s)) return LogStatus.NEEDS_IMPROVEMENT;
        return LogStatus.IN_PROGRESS; // ONGOING 포함
    }
}