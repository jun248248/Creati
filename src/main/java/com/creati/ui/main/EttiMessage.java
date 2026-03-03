package com.creati.ui.main;

/** 에티 도움말 메시지 (제목 + 설명 + 머티리얼 아이콘) */
public class EttiMessage {
    public final String title;
    public final String description;
    public final String icon; // 머티리얼 아이콘 문자 (null 가능)

    public EttiMessage(String title, String description, String icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
}