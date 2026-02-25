package com.creati.model;

import java.time.LocalDateTime;

public class Log {
    private Long l_id;                // 로그 번호 (PK)
    private String u_id;              // 작성자 (FK)
    private String l_title;           // 도전 제목
    private String l_content_title;   // 업로드 콘텐츠 제목
    private String l_content_url;     // 업로드 콘텐츠 url
    private Long pf_id;               // 플랫폼 (FK)
    private Long c_id;                // 도전 분야 (FK)
    private String l_try_content;     // 시도 내용
    private String l_result_status;   // 결과 (SUCCESS, FAIL, ONGOING)
    private String l_fail_result;     // 실패 결과/문제 상황
    private String l_failure_reason;  // 실패 원인
    private Boolean l_is_public;      // 공개 여부
    private Boolean l_is_draft;       // 임시저장 여부
    private Long l_view_count;        // 조회수
    private LocalDateTime l_created_at; // 작성 시간
    private LocalDateTime l_updated_at; // 수정 시간
    

    // 생성 시 기본값 설정 (명세서의 Default value 반영)
    public Log() {
        this.l_is_public = false;  // Default 0
        this.l_is_draft = false;   // Default 0
        this.l_view_count = 0L;    // Default 0
    }

    // --- Getter / Setter (모든 필드) ---
    public Long getL_id() { return l_id; }
    public void setL_id(Long l_id) { this.l_id = l_id; }

    public String getU_id() { return u_id; }
    public void setU_id(String u_id) { this.u_id = u_id; }

    public String getL_title() { return l_title; }
    public void setL_title(String l_title) { this.l_title = l_title; }

    public String getL_content_title() { return l_content_title; }
    public void setL_content_title(String l_content_title) { this.l_content_title = l_content_title; }

    public String getL_content_url() { return l_content_url; }
    public void setL_content_url(String l_content_url) { this.l_content_url = l_content_url; }

    public Long getPf_id() { return pf_id; }
    public void setPf_id(Long pf_id) { this.pf_id = pf_id; }

    public Long getC_id() { return c_id; }
    public void setC_id(Long c_id) { this.c_id = c_id; }

    public String getL_try_content() { return l_try_content; }
    public void setL_try_content(String l_try_content) { this.l_try_content = l_try_content; }

    public String getL_result_status() { return l_result_status; }
    public void setL_result_status(String l_result_status) { this.l_result_status = l_result_status; }

    public String getL_fail_result() { return l_fail_result; }
    public void setL_fail_result(String l_fail_result) { this.l_fail_result = l_fail_result; }

    public String getL_failure_reason() { return l_failure_reason; }
    public void setL_failure_reason(String l_failure_reason) { this.l_failure_reason = l_failure_reason; }

    public Boolean getL_is_public() { return l_is_public; }
    public void setL_is_public(Boolean l_is_public) { this.l_is_public = l_is_public; }

    public Boolean getL_is_draft() { return l_is_draft; }
    public void setL_is_draft(Boolean l_is_draft) { this.l_is_draft = l_is_draft; }

    public Long getL_view_count() { return l_view_count; }
    public void setL_view_count(Long l_view_count) { this.l_view_count = l_view_count; }

    public LocalDateTime getL_created_at() { return l_created_at; }
    public void setL_created_at(LocalDateTime l_created_at) { this.l_created_at = l_created_at; }

    public LocalDateTime getL_updated_at() { return l_updated_at; }
    public void setL_updated_at(LocalDateTime l_updated_at) { this.l_updated_at = l_updated_at; }

}