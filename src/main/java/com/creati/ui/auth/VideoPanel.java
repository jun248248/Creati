package com.creati.ui.auth;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class VideoPanel extends JPanel {

    private final JFXPanel jfx = new JFXPanel();
    private MediaPlayer player;

    public VideoPanel(Path videoPath) {
        super(new BorderLayout());
        add(jfx, BorderLayout.CENTER);
        Platform.runLater(() -> init(videoPath));
    }

    private void init(Path videoPath) {
        Media media = new Media(videoPath.toUri().toString());
        player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setAutoPlay(true);
        
     // 재생 속도 조절 (0.8배속)
        player.setRate(0.8);

        MediaView view = new MediaView(player);

        view.setPreserveRatio(true);

        Group root = new Group(view);
        Scene scene = new Scene(root);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());
        root.setClip(clip);

        player.setOnReady(() -> {
            double vw = media.getWidth();
            double vh = media.getHeight();
            view.setViewport(new Rectangle2D(0, 0, vw / 2.0, vh));
        });

        scene.heightProperty().addListener((obs, oldV, newV) -> {
            view.setFitHeight(newV.doubleValue());
            view.setLayoutX(0);
        });

        scene.widthProperty().addListener((obs, oldV, newV) -> view.setLayoutX(0));

        jfx.setScene(scene);
    }

    public void stop() {
        Platform.runLater(() -> {
            if (player != null) player.stop();
        });
    }
}
