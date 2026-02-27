package com.creati.ui.main;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.sql.SQLException;

import com.creati.ui.main.WriteLogView.Draft;
import com.creati.dao.CategoryDao;
import com.creati.dao.InterestDao;
import com.creati.dao.LogDao;
import com.creati.dto.LogDto;
import com.creati.model.LogPost;
import com.creati.model.LogStatus;
import com.creati.model.User;
// DB(TODO): Replace underlying repositories without changing View.

public class WriteLogController {

    private final WriteLogView view;
    
    // DB
    private final LogDao logDao = new LogDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final InterestDao interestDao = new InterestDao();
    
    public WriteLogController(WriteLogView view) {
        this.view = view;
    }

    public void onTempSaveRequested(boolean showToast) {
        Draft d = view.snapshotFromWizard(true);
        Services.DRAFTS.upsert(d); // DB(TODO)
        view.clearDirty();
        view.refreshDraftDrawerIfOpen();
        if (showToast) {
            JOptionPane.showMessageDialog(view, "임시 저장 완료!");
        }
    }

    public void onOpenDraftDrawerRequested(JFrame owner) {
        
        view.openDrawer(owner);
    }

    public void onDeleteDraftRequested(String id) {
        Services.DRAFTS.delete(id); // DB(TODO)
        view.refreshDraftDrawerIfOpen();
    }
    
    //최종 제출 저장
    public void onSubmitRequested() {
        Draft d = view.snapshotFromWizard(false);
        d.isDraft = false;

        String userId = resolveUserId();
        if (userId == null || userId.isBlank()) {
            JOptionPane.showMessageDialog(view, "로그인 정보가 없어서 저장할 수 없어요.");
            return;
        }
        
        LogDto dto = toLogDto(d, userId);
        if (dto == null) return;
        
        long newId = logDao.insertLog(dto);
        if (newId <= 0) {
            JOptionPane.showMessageDialog(view, "저장 실패!");
            return;
        }
        
        try {
            // 4번째 사진: 다음 조정 포인트 -> log_adjustment_point
            java.util.List<String> adjust = new java.util.ArrayList<>();
            if (d.nextAdjustPoints != null) adjust.addAll(d.nextAdjustPoints);
            if (d.nextAdjustOther != null && !d.nextAdjustOther.trim().isEmpty()) adjust.add(d.nextAdjustOther.trim());
            logDao.insertAdjustmentPoints(newId, adjust);

            // 결과 인식에 따라:
            // 만족/괜찮 -> 2번째(잘된 부분) -> log_good_point
            // 조금/많이 아쉽 -> 3번째(영향 요인) -> log_influence_factor
            if (isPositiveReaction(d.mood)) {
                java.util.List<String> good = new java.util.ArrayList<>();
                if (d.goodPoints != null) good.addAll(d.goodPoints);
                if (d.goodOther != null && !d.goodOther.trim().isEmpty()) good.add(d.goodOther.trim());
                logDao.insertGoodPoints(newId, good);
            } else {
                java.util.List<String> factors = new java.util.ArrayList<>();
                if (d.influenceFactors != null) factors.addAll(d.influenceFactors);
                if (d.influenceOther != null && !d.influenceOther.trim().isEmpty()) factors.add(d.influenceOther.trim());
                logDao.insertInfluenceFactors(newId, factors);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "부가 항목 저장 실패: " + e.getMessage());
            // 정책: 여기서 return 할지 / 그냥 진행할지 선택
            // return;
        }
        
        d.id = String.valueOf(newId);
        
        Services.DRAFTS.upsert(d);
        LogPost saved = view.toLogPost(d);
        Services.LOGS.upsert(saved);
        AppState.get().setSelectedLogId(saved.id);
        
        view.clearDirty();
        
        view.handleSubmitResult(saved);
    }

    public void onSaveAndNavigateToAiRequested() {
        if (!view.validateStepForController("meta")) return;

        Draft d = view.snapshotFromWizard(false);
        d.isDraft = false;

        Services.DRAFTS.upsert(d); // DB(TODO)

        LogPost saved = view.toLogPost(d);
        Services.LOGS.upsert(saved);

        
        // DB(TODO): 팀원이 DB 연동 시에도 logId만 유지하면 그대로 동작
        AppState.get().setSelectedLogId(saved.id);

        view.clearDirty();

        JOptionPane.showMessageDialog(view, "저장 완료! AI 분석으로 이동할게요.");

        Window w = SwingUtilities.getWindowAncestor(view);
        if (w instanceof MainFrame) {
            ((MainFrame) w).navigateToAi();
            return;
        }
        JOptionPane.showMessageDialog(view, "왼쪽 메뉴의 AI 분석 탭에서 확인할 수 있어요.");
    }
    
 // =========================
    // Draft -> LogDto 매핑
    // =========================
    private LogDto toLogDto(Draft d, String userId) {
        LogDto dto = new LogDto();

        dto.setUserId(userId);
        dto.setTitle(d.title);

        // i_id / c_id
        // i_id: 지금 Draft에 "interestId"가 없어서 일단 0(or NULL) 처리.
        //       프로젝트에서 interest를 쓰면 여기에서 선택값 -> i_id로 매핑해줘야 함.
        long iId = interestDao.ensureInterestId(d.field); // 또는 d에 들어있는 관심분야 텍스트 변수
        if (iId <= 0) {
            JOptionPane.showMessageDialog(view, "관심분야 처리 실패");
            return null; // 또는 throw
        }
        dto.setInterestId(iId);

        // c_id: Draft.category(텍스트) -> category 테이블의 c_id로 바꿔 넣어야 함
        // Long cId = categoryDao.ensureCategoryIdByName(d.category);  // (추천)
        // dto.setCategoryId(cId != null ? cId : 0L);

     // ✅ 카테고리 문구 -> category.c_id 조회/생성 후 숫자 저장
        long cId = categoryDao.ensureCategoryId(d.category);
        dto.setCategoryId(cId);

        String rs = mapResultStatus(d.status);
        dto.setResultStatus(rs);
        dto.setIsPublic(d.isPublic);
        dto.setIsDraft(d.isDraft);

        dto.setContentUrl(d.linkUrl);

        // log 테이블 본문 매핑
        dto.setGoal(d.goalText);                // l_goal
        dto.setResultRating(mapMoodToRating(d.mood)); // l_result_rating (DB ENUM 문자열로)
        dto.setProcess(d.processText);          // l_process

        dto.setPlanDifference(mapPlanGap(d.planGapLevel)); // l_plan_difference (DB ENUM 문자열로)
        dto.setDifference(d.planGapDetail);     // l_difference
        dto.setReflection(d.learningText);      // l_reflection

        dto.setNextPlanType(d.nextPlan);        // next_plan_type
        dto.setRetryCondition(d.retryCondition);// retry_condition

        return dto;
    }
    
    private boolean isPositiveReaction(String mood) {
        if (mood == null) return false;
        return switch (mood.trim()) {
            case "만족해요", "괜찮아요" -> true;
            default -> false; // 조금 아쉬워요, 많이 아쉬워요
        };
    }
    
    private String mapMoodToRating(String moodKorean) {
        if (moodKorean == null) return null;

        // 예시 매핑 (너 DB ENUM이 다르면 여기만 바꾸면 됨)
        return switch (moodKorean.trim()) {
            case "만족해요" -> "VERY_SATISFIED";
            case "괜찮아요" -> "SATISFIED";
            case "조금 아쉬워요" -> "SLIGHTLY_DISAPPOINTED";
            case "많이 아쉬워요" -> "VERY_DISAPPOINTED";
            default -> null;
        };
    }
    
    private String mapPlanGap(String planGapKorean) {
        if (planGapKorean == null) return null;

        return switch (planGapKorean.trim()) {
            case "거의 비슷해요" -> "SIMILAR";
            case "일부 달라요" -> "PARTIAL_DIFF";
            case "많이 달라요" -> "VERY_DIFF";
            default -> null;
        };
    }
    
    private String mapResultStatus(LogStatus s) {
        if (s == null) return null;

        return switch (s) {
            case DONE -> "SUCCESS";
            case NEEDS_IMPROVEMENT -> "FAIL";
            case IN_PROGRESS -> "ONGOING";
        };
    }
    
    private String resolveUserId() {
    	User u = AppState.get().getCurrentUser();
        if (u == null) return null;
        System.out.println(u.getId());
        // ✅ 여기서 User가 들고 있는 "DB의 u_id" 필드/게터를 리턴
        // 보통은 getUserId(), getId(), getUid() 중 하나일 가능성이 큼
        return u.getId();   // ← 네 User 클래스의 실제 getter로 바꿔
    }
}
