package com.creati.ui.main;

import com.creati.dao.LogDao;
import com.creati.dao.ReactionDao;
import com.creati.dao.ReplyDao;
import com.creati.model.AiAnalysisStore;
import com.creati.model.DraftRepositoryInMemory;
import com.creati.model.LogRepositoryDb;
import com.creati.model.SocialRepositoryDb;
import com.creati.service.AiAnalysisService;
import com.creati.service.DraftService;
import com.creati.service.SocialService;

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

	public static final AiAnalysisService AI =
			new AiAnalysisService(new AiAnalysisStore());
}