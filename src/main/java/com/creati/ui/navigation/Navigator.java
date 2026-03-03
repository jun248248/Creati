package com.creati.ui.navigation;

import com.creati.model.LogPost;
import com.creati.ui.main.MainFrame;

public class Navigator {

	private final MainFrame frame;

	public Navigator(MainFrame frame) {
		this.frame = frame;
	}

	public void go(Route route) {
		switch (route) {
			case HOME -> frame.showHome();
			case CHALLENGE -> frame.showChallenge();
			case AI -> frame.showAi();
			case COMMUNITY -> frame.showCommunity();
			case QNA -> frame.showQna();
			case QNA_DETAIL -> frame.showQnaDetail();
			case STATS -> frame.showStats();
			case WRITE_LOG -> frame.showWriteLog();
			case LOG_DETAIL -> frame.showLogDetail();
		}
	}

	public void openLogDetail(LogPost post) {
		frame.openLogDetail(post);
	}

	public void openLogEdit(LogPost post) {
		frame.openLogEdit(post);
	}

	public void openQnaDetail(LogPost post) {
		frame.openQnaDetail(post);
	}
}
