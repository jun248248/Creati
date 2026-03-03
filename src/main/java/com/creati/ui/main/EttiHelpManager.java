package com.creati.ui.main;

import com.creati.util.FontKit;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EttiHelpManager {

    private static final String ICON_VISIBILITY = new String(Character.toChars(0xE8F4));
    private static final String ICON_FAVORITE   = new String(Character.toChars(0xE87D));
    private static final String ICON_FITNESS    = new String(Character.toChars(0xEB43));
    private static final String ICON_COMPARE    = new String(Character.toChars(0xEB3D));
    private static final String ICON_INSIGHTS   = new String(Character.toChars(0xF092));
    private static final String ICON_GROUP      = new String(Character.toChars(0xE7EF));
    private static final String ICON_HELP       = new String(Character.toChars(0xE887));

    public enum ViewType {
        HOME, CHALLENGE, LOG_COMPARE, AI_ANALYSIS, COMMUNITY, QUESTION_WRITE
    }

    private static final Map<ViewType, List<EttiMessage>> MESSAGES = new HashMap<>();
    static {
        MESSAGES.put(ViewType.HOME, Arrays.asList(
            new EttiMessage("여기는 전체 흐름을 보는 공간이에요", "이번 달 기록이 쌓이면 패턴이 보이기 시작해요.", ICON_VISIBILITY, "default"),
            new EttiMessage("지금 상태를 가볍게 확인해볼까요?", "다음 한 걸음만 정해도 충분해요.", ICON_FAVORITE, "smile"),
            new EttiMessage("작은 기록도 의미 있어요", "흐름은 천천히 만들어져요.", null, "empathy"),
            new EttiMessage("한 달의 방향이 여기서 보여요", "조급해하지 않아도 괜찮아요.", ICON_FAVORITE, "cheer")
        ));
        MESSAGES.put(ViewType.CHALLENGE, Arrays.asList(
            new EttiMessage("지금 진행 중인 시도예요", "완벽보다 지속이 더 중요해요.", ICON_FITNESS, "cheer"),
            new EttiMessage("멈추지 않는 게 가장 큰 힘이에요", "오늘도 한 번 더 이어가볼까요?", ICON_FITNESS, "smile"),
            new EttiMessage("작게라도 계속하면 충분해요", "기록은 이미 잘하고 있어요.", null, "empathy")
        ));
        MESSAGES.put(ViewType.LOG_COMPARE, Arrays.asList(
            new EttiMessage("두 기록을 비교해볼까요?", "어떤 방식이 더 편하게 이어질지 정리해봐요.", ICON_COMPARE, "default"),
            new EttiMessage("비교는 평가가 아니에요", "다음 선택을 쉽게 만드는 과정이에요.", ICON_FAVORITE, "smile"),
            new EttiMessage("이번엔 뭐가 더 잘 맞을까요?", "에티가 구조만 정리해줄게요.", ICON_VISIBILITY, "surprised")
        ));
        MESSAGES.put(ViewType.AI_ANALYSIS, Arrays.asList(
            new EttiMessage("이번 달 흐름을 정리해볼까요?", "기록이 쌓이면 방향도 또렷해져요.", ICON_INSIGHTS, "default"),
            new EttiMessage("크게 바꾸지 않아도 괜찮아요", "한 가지에만 집중해보는 건 어때요?", null, "empathy"),
            new EttiMessage("흐름을 보면 조급함이 줄어요", "천천히 다듬어가면 돼요.", ICON_FAVORITE, "smile")
        ));
        MESSAGES.put(ViewType.COMMUNITY, Arrays.asList(
            new EttiMessage("혼자 하는 기록이 아니에요", "비슷한 고민을 하는 사람이 많아요.", ICON_GROUP, "greeting"),
            new EttiMessage("질문 하나가 방향을 바꿔요", "경험을 나누면 판단이 쉬워져요.", ICON_GROUP, "surprised"),
            new EttiMessage("함께 보면 더 또렷해져요", "서로의 흐름에서 배울 수 있어요.", ICON_VISIBILITY, "smile")
        ));
        MESSAGES.put(ViewType.QUESTION_WRITE, Arrays.asList(
            new EttiMessage("고민이 있다면 남겨볼까요?", "완벽하지 않아도 괜찮아요.", ICON_HELP, "empathy"),
            new EttiMessage("질문은 생각을 또렷하게 해줘요", "작은 물음이 큰 방향이 될 수도 있어요.", ICON_HELP, "default"),
            new EttiMessage("막히면 물어보는 것도 전략이에요", "혼자 고민하지 않아도 돼요.", ICON_FAVORITE, "cheer")
        ));
    }

    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final JLabel iconLabel;
    private Consumer<String> onEttiImageChange;

    private Timer timer;
    private int currentIndex = 0;
    private ViewType currentView = ViewType.HOME;

    public EttiHelpManager(JLabel titleLabel, JLabel descriptionLabel, JLabel iconLabel) {
        this.titleLabel = titleLabel;
        this.descriptionLabel = descriptionLabel;
        this.iconLabel = iconLabel;
        if (iconLabel != null) {
            iconLabel.setFont(FontKit.materialIcon(16f));
        }
    }

    public void setOnEttiImageChange(Consumer<String> callback) {
        this.onEttiImageChange = callback;
    }

    public void setView(ViewType view) {
        if (view == null) return;
        currentView = view;
        currentIndex = 0;
        applyCurrentMessage();
        restart();
    }

    public void start() {
        if (timer != null && timer.isRunning()) return;
        timer = new Timer(7000, e -> {
            List<EttiMessage> list = MESSAGES.get(currentView);
            if (list == null || list.isEmpty()) return;
            currentIndex = (currentIndex + 1) % list.size();
            applyCurrentMessage();
        });
        timer.setRepeats(true);
        timer.start();
    }

    public void stop() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    private void restart() { stop(); start(); }

    private void applyCurrentMessage() {
        List<EttiMessage> list = MESSAGES.get(currentView);
        if (list == null || list.isEmpty()) return;
        EttiMessage msg = list.get(currentIndex % list.size());

        titleLabel.setText(msg.title);
        descriptionLabel.setText(msg.description);

        if (iconLabel != null) {
            if (msg.icon != null) {
                iconLabel.setText(msg.icon);
                iconLabel.setVisible(true);
            } else {
                iconLabel.setText("");
                iconLabel.setVisible(false);
            }
        }

        if (onEttiImageChange != null && msg.ettiImage != null) {
            onEttiImageChange.accept(msg.ettiImage);
        }
    }
}