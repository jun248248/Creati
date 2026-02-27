package com.creati.ui.main;

// DB(TODO): Swap repositories without changing UI layer.

public final class Services {

    private Services() {}

    public static final LogService LOGS = new LogService(new LogRepositoryInMemory());
    public static final QnaService QNA = new QnaService(new QnaRepositoryInMemory());
    public static final DraftService DRAFTS = new DraftService(new DraftRepositoryInMemory());

    public static final SocialService SOCIAL = new SocialService(new SocialRepositoryInMemory());

    public static final AiAnalysisStore AI_STORE = new AiAnalysisStore();
    public static final AiAnalysisService AI = new AiAnalysisService(AI_STORE);
    
    // API(TODO): 실제 AI API 연동 시 AiAnalysisService 내부에서 호출하도록 교체
    // DB(TODO): 분석 결과 저장을 DB로 옮길 때는 AiAnalysisStore 구현만 교체하면 됨
    
}
