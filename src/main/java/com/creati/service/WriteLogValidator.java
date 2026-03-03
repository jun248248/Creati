package com.creati.service;

// [COLLAB] NOTE: Keep validation rules out of View.

public final class WriteLogValidator {

    public static final class Result {
        public final boolean ok;
        public final String message;
        public final Runnable onFailFocus;

        private Result(boolean ok, String message, Runnable onFailFocus) {
            this.ok = ok;
            this.message = message;
            this.onFailFocus = onFailFocus;
        }
    }

    private WriteLogValidator() {}

    public static Result ok() {
        return new Result(true, null, null);
    }

    public static Result fail(String message, Runnable onFailFocus) {
        return new Result(false, message, onFailFocus);
    }

    public static Result validateMeta(
            String title,
            String field,
            boolean isOtherSelected,
            String otherFieldText,
            String category,
            Object statusSelected,
            Runnable focusTitle,
            Runnable focusOtherField
    ) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) return fail("제목을 입력해 주세요.", focusTitle);

        String f = field == null ? "" : field.trim();
        if (f.isEmpty()) return fail("분야를 선택해 주세요.", null);

        if (isOtherSelected) {
            String other = otherFieldText == null ? "" : otherFieldText.trim();
            if (other.isEmpty()) return fail("기타 분야를 입력해 주세요.", focusOtherField);
        }

        String c = category == null ? "" : category.trim();
        if (c.isEmpty()) return fail("카테고리를 선택해 주세요.", null);

        if (statusSelected == null) return fail("현재 상태를 선택해 주세요.", null);

        return ok();
    }

    public static Result validateRequiredText(String text, String message, Runnable focus) {
        String v = text == null ? "" : text.trim();
        if (v.isEmpty()) return fail(message, focus);
        return ok();
    }

    public static Result validateRequiredSelection(String selectedText, String message, Runnable focus) {
        if (selectedText == null) return fail(message, focus);
        String v = selectedText.trim();
        if (v.isEmpty()) return fail(message, focus);
        return ok();
    }

    public static Result validatePlanGapDetail(String gapSelection, String detailText, String message, Runnable focus) {
        if (gapSelection == null) return ok();

        String gap = gapSelection;
        boolean needsDetail = gap.contains("일부") || gap.contains("많이");
        if (!needsDetail) return ok();

        String d = detailText == null ? "" : detailText.trim();
        if (d.isEmpty()) return fail(message, focus);

        return ok();
    }
}
