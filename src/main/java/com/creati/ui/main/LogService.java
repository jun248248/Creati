package com.creati.ui.main;

import com.creati.dao.LogDao;
import com.creati.model.LogPost;
import java.util.List;

public class LogService {
    private final LogDao logDao = new LogDao();

    public LogService() {}

    /**
     * [추가] 컨트롤러에서 호출하는 upsert 메서드
     */
    public void upsert(LogPost log) {
        if (log == null) return;
        logDao.upsertLog(log);
    }

    public List<LogPost> list() {
        return logDao.selectAllLogs();
    }

    public LogPost getById(String id) {
        return logDao.selectLogById(id);
    }
    
    public void save(LogPost log) {
        // 내부적으로 upsert와 동일한 DB 저장 로직을 수행합니다
        upsert(log); 
    }
    
}