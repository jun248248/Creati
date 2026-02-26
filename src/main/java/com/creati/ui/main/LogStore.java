package com.creati.ui.main;

import com.creati.model.LogStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.creati.model.LogPost;
// DB(TODO): Replace in-memory store with repository.

public final class LogStore {

    private static final List<LogPost> POSTS = new ArrayList<>();

    
    static {
        // DB(TODO): 초기 더미 데이터(화면 테스트용). DB 연동 시 제거/대체.

        

        
        upsert(new LogPost(
                LogPost.TYPE_LOG,
                "log_demo_inprogress_positive",
                "영상",
                "일상 / 브이로그",
                LogStatus.IN_PROGRESS,
                "쇼츠 편집 루틴 만들기",
                LocalDate.now().minusDays(1),
                false,
                "1시간 안에 컷 편집 + 자막 초안을 끝내는 루틴을 만들고 싶어요.",
                "만족해요",
                List.of("집중이 잘 됐어요", "시간 배분이 괜찮았어요"),
                "타이머를 켜두니까 딴짓이 줄었어요.",
                "",
                List.of(),
                "",
                "(1) 레퍼런스 3개 저장 → (2) 컷 편집 20분 → (3) 자막 20분 → (4) 10분 내보내기 테스트",
                "거의 비슷해요",
                "",
                "작업 시간을 먼저 쪼개면 완성까지 가는 속도가 빨라져요.",
                List.of("단축키 익히기", "템플릿 만들기"),
                "자막 스타일을 2가지로 고정해볼래요.",
                "바로 다시 시도해볼래요",
                "",
                "",
                ""
        ));

        
        upsert(new LogPost(
                LogPost.TYPE_LOG,
                "log_demo_done_regret",
                "글",
                "생산성 / 루틴 / 습관",
                LogStatus.DONE,
                "주간 회고 글 1편 발행",
                LocalDate.now().minusDays(7),
                true,
                "한 주 동안의 시도/실패를 정리해서 다음 주 계획까지 연결하고 싶어요.",
                "조금 아쉬워요",
                List.of(),
                "",
                "글 구조가 중간에 흐트러져서 핵심이 약해졌어요.",
                List.of("자료 정리 부족", "시간 부족", "집중 환경"),
                "초안이 길어지면 바로 소제목을 먼저 잡아야 하는데 그걸 놓쳤어요.",
                "(1) 메모 모으기 → (2) 초안 작성 → (3) 소제목 재배치 → (4) 교정 후 업로드",
                "많이 달라요",
                "원래는 ‘실패-원인-다음 행동’ 구조로 쓰려 했는데, 중간에 회고 감정이 길어지면서 결론이 약해졌어요.",
                "긴 글일수록 ‘결론 문장’부터 잡으면 내용이 산으로 덜 가요.",
                List.of("구조 먼저 잡기", "시간 블록 고정"),
                "초안은 30분 제한으로 쓰고, 다음 날 교정하기.",
                "바로 다시 시도해볼래요",
                "",
                "",
                ""
        ));

        
        LogStatus improve = findImproveStatusOrNull();
        if (improve != null) {
            upsert(new LogPost(
                    LogPost.TYPE_LOG,
                    "log_demo_need_improve_with_link",
                    "이미지",
                    "콘텐츠 제작 / 크리에이터 활동",
                    improve,
                    "썸네일 톤 통일 작업",
                    LocalDate.now().minusDays(3),
                    true,
                    "썸네일을 한 눈에 ‘내 채널 스타일’로 인식되게 통일하고 싶어요.",
                    "많이 아쉬워요",
                    List.of(),
                    "",
                    "폰트/색이 매번 달라서 통일감이 없어요.",
                    List.of("기준(가이드) 부재", "레퍼런스 부족"),
                    "템플릿을 만들기 전에 바로 디자인을 시작해서 흔들렸어요.",
                    "(1) 레퍼런스 수집 → (2) 팔레트 후보 3개 선정 → (3) 썸네일 2안 제작 → (4) 비교 후 선택",
                    "일부 달라요",
                    "원래는 팔레트 확정 후 제작하려 했는데, 급하게 제작부터 해서 되돌아가는 시간이 생겼어요.",
                    "기준(팔레트/폰트)을 먼저 정하면 수정 비용이 확 줄어요.",
                    List.of("팔레트 고정", "폰트 2종 고정", "템플릿 저장"),
                    "썸네일 그리드(여백/배치)도 1개로 통일하기.",
                    "조금 보완 후 진행하고 싶어요",
                    "팔레트 1개 확정 + 폰트 2종 확정 후에만 제작 시작하기.",
                    "https://example.com",
                    "썸네일 2안 중 A/B 비교한 부분만 봐주세요"
            ));
        }

        
        upsert(LogPost.newQna(
                "qna_demo_1",
                "영상",
                "일상 / 브이로그",
                "쇼츠 자막 템포를 어떻게 잡아야 할까요?",
                LocalDate.now().minusDays(2),
                "쇼츠 편집할 때 자막 템포/호흡을 잡는 기준이 궁금해요.",
                ""
        ));
    }

    private static LogStatus findImproveStatusOrNull() {
        try {
            for (LogStatus s : LogStatus.values()) {
                String n = s.name();
                
                if (n.contains("IMPROV") || n.contains("NEED") || n.contains("FIX") || n.contains("REVISE") || n.contains("UPDATE")) {
                    return s;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    private LogStore() {}

    public static synchronized List<LogPost> list() {
        return new ArrayList<>(POSTS);
    }

    public static synchronized LogPost getById(String id) {
        for (LogPost p : POSTS) {
            if (Objects.equals(p.id, id)) return p;
        }
        return null;
    }

    public static synchronized void upsert(LogPost post) {
        if (post == null) return;

        if (post.id == null || post.id.isBlank()) {
            POSTS.add(post);
            return;
        }

        for (int i = 0; i < POSTS.size(); i++) {
            if (Objects.equals(POSTS.get(i).id, post.id)) {
                POSTS.set(i, post);
                return;
            }
        }
        POSTS.add(post);
    }
}
