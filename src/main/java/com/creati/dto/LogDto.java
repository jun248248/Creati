package com.creati.dto;

import java.time.LocalDateTime;
import java.util.List;

/* 필요시 Dto 분리*/
public class LogDto {
	
	 private Long id;                // l_id (BIGINT, PK, AUTO_INCREMENT)
	    private String userId;          // u_id (VARCHAR)

	    private String title;           // l_title (VARCHAR)

	    private Long interestId;        // i_id (BIGINT)  - 관심사/interest FK로 쓰는 경우
	    private Long categoryId;        // c_id (BIGINT)  - category FK

	    private String resultStatus;    // l_result_status (ENUM: 'SUCCESS', ...)
	    private Boolean isPublic;       // l_is_public (TINYINT(1))
	    private Boolean isDraft;        // l_is_draft (TINYINT(1))

	    private String contentUrl;      // l_content_url (VARCHAR)

	    private String goal;            // l_goal (TEXT)
	    private String resultRating;    // l_result_rating (ENUM: 'VERY_SATISFIED', ...)
	    private String process;         // l_process (TEXT)

	    private String planDifference;  // l_plan_difference (ENUM: 'SIMILAR', ...)
	    private String difference;      // l_difference (TEXT, NULL 가능)
	    private String reflection;      // l_reflection (TEXT)

	    private String nextPlanType;    // next_plan_type (VARCHAR)
	    private String retryCondition;  // retry_condition (TEXT, NULL 가능)

	    private LocalDateTime createdAt; // created_at (DATETIME)

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Long getInterestId() {
			return interestId;
		}

		public void setInterestId(Long interestId) {
			this.interestId = interestId;
		}

		public Long getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Long categoryId) {
			this.categoryId = categoryId;
		}

		public String getResultStatus() {
			return resultStatus;
		}

		public void setResultStatus(String resultStatus) {
			this.resultStatus = resultStatus;
		}

		public Boolean getIsPublic() {
			return isPublic;
		}

		public void setIsPublic(Boolean isPublic) {
			this.isPublic = isPublic;
		}

		public Boolean getIsDraft() {
			return isDraft;
		}

		public void setIsDraft(Boolean isDraft) {
			this.isDraft = isDraft;
		}

		public String getContentUrl() {
			return contentUrl;
		}

		public void setContentUrl(String contentUrl) {
			this.contentUrl = contentUrl;
		}

		public String getGoal() {
			return goal;
		}

		public void setGoal(String goal) {
			this.goal = goal;
		}

		public String getResultRating() {
			return resultRating;
		}

		public void setResultRating(String resultRating) {
			this.resultRating = resultRating;
		}

		public String getProcess() {
			return process;
		}

		public void setProcess(String process) {
			this.process = process;
		}

		public String getPlanDifference() {
			return planDifference;
		}

		public void setPlanDifference(String planDifference) {
			this.planDifference = planDifference;
		}

		public String getDifference() {
			return difference;
		}

		public void setDifference(String difference) {
			this.difference = difference;
		}

		public String getReflection() {
			return reflection;
		}

		public void setReflection(String reflection) {
			this.reflection = reflection;
		}

		public String getNextPlanType() {
			return nextPlanType;
		}

		public void setNextPlanType(String nextPlanType) {
			this.nextPlanType = nextPlanType;
		}

		public String getRetryCondition() {
			return retryCondition;
		}

		public void setRetryCondition(String retryCondition) {
			this.retryCondition = retryCondition;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}
	
	
}
