package com.creati.dto;

import java.time.LocalDateTime;

public class ReplyDto {
	
	private long rId;
    private long lId;
    private String userId;
    private String nickname;
	private String content;
    private LocalDateTime createdAt;
	public long getrId() {
		return rId;
	}
	public void setrId(long rId) {
		this.rId = rId;
	}
	public long getlId() {
		return lId;
	}
	public void setlId(long lId) {
		this.lId = lId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
