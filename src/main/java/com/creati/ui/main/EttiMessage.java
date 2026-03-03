package com.creati.ui.main;

public class EttiMessage {
    public final String title;
    public final String description;
    public final String icon;
    public final String ettiImage; 

    public EttiMessage(String title, String description, String icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.ettiImage = "default"; 
    }

    public EttiMessage(String title, String description, String icon, String ettiImage) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.ettiImage = ettiImage;
    }
}