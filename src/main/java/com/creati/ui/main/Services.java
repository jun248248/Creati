package com.creati.ui.main;

import com.creati.dao.LogDao;
import com.creati.dao.ReactionDao;
import com.creati.dao.ReplyDao;

public final class Services {

	private Services() {
	}

	public static final LogService LOGS = new LogService(new LogRepositoryDb(new LogDao()));
	public static final DraftService DRAFTS = new DraftService(new DraftRepositoryInMemory());

	public static final SocialService SOCIAL = new SocialService(
			new SocialRepositoryDb(
				    new ReplyDao(),
				    new ReactionDao(),
				    new LogDao()
				));

	// AI 분석: AiAnalysisStore(DB 구현) + AiAnalysisService(Gemini API 연동)
	public static final AiAnalysisService AI =
			new AiAnalysisService(new AiAnalysisStore());
}