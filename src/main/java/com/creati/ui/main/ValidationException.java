package com.creati.ui.main;

/**
 * [COLLAB] View에서 입력값 검증/비즈니스 로직을 분리하기 위한 공용 예외.
 * - View: 메시지 표시/포커스 이동만 담당
 * - Controller/Service: 검증 실패 시 ValidationException throw
 */
public class ValidationException extends RuntimeException {

    public enum Field {
        TITLE,
        CONTENT
    }

    public final Field field;

    public ValidationException(Field field, String message) {
        super(message);
        this.field = field;
    }
}
