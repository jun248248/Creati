package com.creati.ui.main;

import com.creati.model.LogPost;
import com.creati.ui.components.RoundedButton;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

// API(TODO): Run analysis via API in background thread.
// DB(TODO): Persist analysis records.

public class AiAnalysisView extends JPanel {

    private final MainFrame parent;

    
    private JLabel headerTitle;
    private JLabel headerSub;
    private JButton btnLoadLog;

    
    private JLabel chatHeaderLine;
    private JPanel chatList;
    private JScrollPane chatScroll;

    private JButton btnCause;
    private JButton btnRetro;
    private JButton btnRetry;
    private JButton btnSave;

    private JTextField input;
    private JButton send;
    
    private AbstractButton selectedQuickChip = null;
    private List<AbstractButton> quickChips;

    
    private CardLayout recordCards;
    private JPanel recordRoot;
    private DefaultListModel<AiAnalysisRecord> recordModel;
    private JList<AiAnalysisRecord> recordList;
    private int hoverRecordIndex = -1;

    private JLabel detailTitle;
    private JLabel detailDate;
    private JTextArea detailArea;

    
    private final EnumMap<AiAnalysisRecord.Type, AiAnalysisRecord> pending = new EnumMap<>(AiAnalysisRecord.Type.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public AiAnalysisView(MainFrame parent) {
        this.parent = Objects.requireNonNull(parent);
        UITheme.ensureInit();
        FontKit.init();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    
    public void onActivated() {
        List<LogPost> logs = Services.LOGS.list();
        long realLogCount = (logs == null) ? 0 : logs.stream().filter(p -> p != null && LogPost.TYPE_LOG.equals(p.type)).count();
        if (realLogCount == 0) {
            int r = JOptionPane.showConfirmDialog(
                    this,
                    "새 성장 로그를 작성해보시겠어요?",
                    "분석할 성장 로그가 없어요",
                    JOptionPane.YES_NO_OPTION
            );
            if (r == JOptionPane.YES_OPTION) parent.showWriteLog();
            else parent.showHome();
            return;
        }

        
        LogPost selected = AppState.get().getSelectedLog();
        if (selected == null) {
            LogPost mostRecent = Services.AI.findMostRecentLog();
            if (mostRecent != null) {
                AppState.get().setSelectedLogId(mostRecent.id);
                selected = mostRecent;
            }
        }

        pending.clear();
        chatList.removeAll();
        refreshAll();

        
        pushEttiMessage(
                "안녕하세요! 에티입니다.\n" +
                "선택한 성장 로그를 읽고 분석을 도와드릴게요.\n" +
                "아래에서 원하는 항목을 선택해 주세요!"
        );
    }

    
    
    

    private JComponent buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 18, 6, 18));

        headerTitle = new JLabel("AI 분석");
        headerTitle.setFont(UITheme.BODY_MED);
        headerTitle.setForeground(UITheme.TEXT);

        headerSub = new JLabel("선택한 성장 로그를 바탕으로 원인 분석, 회고, 재도전 방향을 함께 정리해요.");
        headerSub.setFont(UITheme.CAPTION);
        headerSub.setForeground(UITheme.RGB_120_120_120);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(headerTitle);
        texts.add(Box.createVerticalStrut(6));
        texts.add(headerSub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        btnLoadLog = new RoundedButton("로그 불러오기");
        styleWhiteButton(btnLoadLog);
        btnLoadLog.addActionListener(e -> openLogPicker());

        actions.add(btnLoadLog);

        wrap.add(texts, BorderLayout.WEST);
        wrap.add(actions, BorderLayout.EAST);
        return wrap;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 18, 18, 18));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null);
        split.setDividerSize(8);
        split.setResizeWeight(0.50); 
        split.setContinuousLayout(true);

        split.setLeftComponent(buildChatPanel());
        split.setRightComponent(buildRecordPanel());

        body.add(split, BorderLayout.CENTER);
        return body;
    }

    private JComponent buildChatPanel() {
        JPanel card = makeCard(false);
        card.setLayout(new BorderLayout());

        
        chatHeaderLine = new JLabel("에티가 읽고 있는 로그  \u2192  (선택된 로그 없음)");
        chatHeaderLine.setFont(UITheme.BODY_MED);
        chatHeaderLine.setForeground(UITheme.TEXT);
        chatHeaderLine.setIcon(MainUiParts.glyphIcon(0xE859, 18f, UITheme.ICON_MUTED)); 
        chatHeaderLine.setIconTextGap(8);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 10, 0));
        head.add(chatHeaderLine, BorderLayout.WEST);
        card.add(head, BorderLayout.NORTH);

        chatList = new JPanel();
        chatList.setOpaque(false);
        chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));

        chatScroll = new JScrollPane(chatList);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.setOpaque(false);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(chatScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        
        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        qaRow.setOpaque(false);

        btnCause = new RoundedButton("원인 분석");
        btnRetro = new RoundedButton("회고 정리");
        btnRetry = new RoundedButton("재도전 방향");
        btnSave  = new RoundedButton("분석내용 저장");

        ((RoundedButton) btnCause).setPill(true);
        ((RoundedButton) btnRetro).setPill(true);
        ((RoundedButton) btnRetry).setPill(true);
        ((RoundedButton) btnSave).setPill(true);
        
        styleGreyButton(btnCause);
        styleGreyButton(btnRetro);
        styleGreyButton(btnRetry);
        styleGreyButton(btnSave);

        btnCause.addActionListener(e -> { setQuickSelected(btnCause); onQuickAction(AiAnalysisRecord.Type.CAUSE); });
        btnRetro.addActionListener(e -> { setQuickSelected(btnRetro); onQuickAction(AiAnalysisRecord.Type.RETRO); });
        btnRetry.addActionListener(e -> { setQuickSelected(btnRetry); onQuickAction(AiAnalysisRecord.Type.RETRY); });
        btnSave.addActionListener(e -> { setQuickSelected(btnSave); onSave(); });
        
        quickChips = List.of(btnCause, btnRetro, btnRetry, btnSave);
        installQuickChipBehavior(quickChips);

        qaRow.add(btnCause);
        qaRow.add(btnRetro);
        qaRow.add(btnRetry);
        qaRow.add(btnSave);

        
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);

        input = new JTextField();
        input.setFont(UITheme.BODY);
        input.setPreferredSize(new Dimension(0, 40));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(9, 12, 9, 12)
        ));

        send = new RoundedButton("전송");
        styleGreyButton(send);
        send.addActionListener(e -> onSendFreeText());

        inputRow.add(input, BorderLayout.CENTER);
        inputRow.add(send, BorderLayout.EAST);

        bottom.add(qaRow);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(inputRow);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildRecordPanel() {
        recordCards = new CardLayout();
        recordRoot = new JPanel(recordCards);
        recordRoot.setOpaque(false);

        
        JPanel listCard = makeCard(true);
        listCard.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("분석 기록");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        recordModel = new DefaultListModel<>();
        recordList = new JList<>(recordModel);
        recordList.setFixedCellHeight(38); 
        
        recordList.setBackground(UITheme.WHITE);
        recordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordList.setCellRenderer(new RecordRenderer());

        recordList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverRecordIndex = recordList.locationToIndex(e.getPoint());
                recordList.repaint();
            }
        });
        recordList.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoverRecordIndex = -1;
                recordList.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                int idx = recordList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                recordList.setSelectedIndex(idx);
                openRecordDetail(recordModel.getElementAt(idx));
            }
        });

        JScrollPane sp = new JScrollPane(recordList);
        sp.setBorder(null);
        sp.getViewport().setBackground(UITheme.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        listCard.add(title, BorderLayout.NORTH);
        listCard.add(sp, BorderLayout.CENTER);

        
        JPanel detailCard = makeCard(true);
        detailCard.setLayout(new BorderLayout());

        JPanel dHead = new JPanel(new BorderLayout(10, 0));
        dHead.setOpaque(false);
        dHead.setBorder(new EmptyBorder(0, 0, 10, 0));

        detailTitle = new JLabel("(분석 결과)");
        detailTitle.setFont(UITheme.BODY_MED);
        detailTitle.setForeground(UITheme.TEXT);

        detailDate = new JLabel("");
        detailDate.setFont(UITheme.CAPTION);
        detailDate.setForeground(UITheme.MUTED_TEXT);

        dHead.add(detailTitle, BorderLayout.WEST);
        dHead.add(detailDate, BorderLayout.EAST);

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(UITheme.BODY);
        detailArea.setForeground(UITheme.TEXT);
        detailArea.setBackground(UITheme.WHITE);
        detailArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane sp2 = new JScrollPane(detailArea);
        sp2.setBorder(null);
        sp2.getVerticalScrollBar().setUnitIncrement(16);

        JButton back = new RoundedButton("뒤로가기");
        styleGreyButton(back);
        back.addActionListener(e -> recordCards.show(recordRoot, "LIST"));

        JPanel dBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        dBottom.setOpaque(false);
        dBottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        dBottom.add(back);

        detailCard.add(dHead, BorderLayout.NORTH);
        detailCard.add(sp2, BorderLayout.CENTER);
        detailCard.add(dBottom, BorderLayout.SOUTH);

        recordRoot.add(listCard, "LIST");
        recordRoot.add(detailCard, "DETAIL");
        recordCards.show(recordRoot, "LIST");

        return recordRoot;
    }

    
    
    

    private void refreshAll() {
        LogPost log = AppState.get().getSelectedLog();
        String title = (log == null || log.title == null || log.title.isBlank()) ? "(제목 없음)" : log.title;
        chatHeaderLine.setText("에티가 읽고 있는 로그  \u2192  " + title);

        refreshRecordList();
        refreshAnalyzedState();
        refreshSaveButtonState();
    }

    private void refreshRecordList() {
        recordModel.clear();

        LogPost log = AppState.get().getSelectedLog();
        if (log != null) {
            List<AiAnalysisRecord> records = Services.AI.listRecords(log.id);
            if (records != null) {
                
                List<AiAnalysisRecord> sorted = records.stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing((AiAnalysisRecord r) -> r.createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .collect(Collectors.toList());
                for (AiAnalysisRecord r : sorted) recordModel.addElement(r);
            }
        }

        recordList.revalidate();
        recordList.repaint();
    }

    private void refreshSaveButtonState() {
        boolean hasPending = !pending.isEmpty();
        btnSave.setEnabled(hasPending);
    }

    private void refreshAnalyzedState() {
        LogPost log = AppState.get().getSelectedLog();
        if (log == null) {
            btnCause.setEnabled(false);
            btnRetro.setEnabled(false);
            btnRetry.setEnabled(false);
            input.setEnabled(false);
            send.setEnabled(false);
            return;
        }

        
        boolean causeDone = Services.AI.isTypeAnalyzed(log.id, AiAnalysisRecord.Type.CAUSE);
        boolean retroDone = Services.AI.isTypeAnalyzed(log.id, AiAnalysisRecord.Type.RETRO);
        boolean retryDone = Services.AI.isTypeAnalyzed(log.id, AiAnalysisRecord.Type.RETRY);

        btnCause.setEnabled(!causeDone);
        btnRetro.setEnabled(!retroDone);
        btnRetry.setEnabled(!retryDone);

        boolean allDone = Services.AI.isAnalyzed(log.id);
        input.setEnabled(!allDone);
        send.setEnabled(!allDone);
        input.setText("");

        if (allDone) {
            input.setToolTipText("이 로그는 분석이 완료됐어요. 다른 로그를 불러올까요?");
            input.setBackground(UITheme.RGB_245_245_248);
        } else {
            input.setToolTipText("자유 질문은 추후 확장 예정이에요");
            input.setBackground(UITheme.WHITE);
        }
    }

    private void onQuickAction(AiAnalysisRecord.Type type) {
        LogPost log = AppState.get().getSelectedLog();
        if (log == null) return;

        pushUserMessage(type.label + " 분석을 시작할게.");
        pushEttiMessage("잠시만 기다려줘! 열심히 분석 중이야...");

        new Thread(() -> {
            try {
                // 실제 AI 호출
                String response = Services.AI.requestAiAnalysis(log.id, type);
                SwingUtilities.invokeLater(() -> {
                    pushEttiMessage(response);
                    // 분석된 내용을 임시 저장소(pending)에 저장 (기존 UI 로직 유지)
                    pending.put(type, new AiAnalysisRecord("temp", log.id, type, "미리보기", LocalDate.now(), response));
                    refreshSaveButtonState();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> pushEttiMessage("미안, 분석 중에 오류가 났어: " + e.getMessage()));
            }
        }).start();
    }

    private void onSave() {
        LogPost log = AppState.get().getSelectedLog();
        if (log == null || pending.isEmpty()) return;

        pending.forEach((type, record) -> {
            Services.AI.save(log.id, type, record.content);
        });

        pending.clear();
        pushEttiMessage("분석 기록을 저장했어! 오른쪽 리스트에서 확인할 수 있어.");
        refreshAll(); // 기존 UI 새로고침 메서드
    }

    private void onSendFreeText() {
        String txt = input.getText().trim();
        if (txt.isBlank()) return;

        LogPost log = AppState.get().getSelectedLog();
        if (log == null) {
            JOptionPane.showMessageDialog(this, "분석할 로그를 먼저 선택해 주세요.");
            return;
        }
        if (Services.AI.isAnalyzed(log.id)) {
            JOptionPane.showMessageDialog(this, "이 로그는 분석이 완료됐어요. 다른 로그를 불러와주세요.");
            refreshAnalyzedState();
            return;
        }

        pushUserMessage(txt);
        input.setText("");

        // API(TODO): 자유 질문 응답
        pushEttiMessage("(임시) 자유 질문 응답은 추후 확장 예정이에요. 우선 아래 버튼으로 분석을 시작해볼까요?");
    }

    private void openRecordDetail(AiAnalysisRecord r) {
        if (r == null) return;
        detailTitle.setText(r.title == null ? "" : r.title);
        detailDate.setText(r.createdAt == null ? "" : r.createdAt.format(DATE_FMT));
        detailArea.setText(r.content == null ? "" : r.content);
        detailArea.setCaretPosition(0);
        recordCards.show(recordRoot, "DETAIL");
    }

    
    
    

    private void openLogPicker() {
        JDialog dialog = new JDialog(parent, "로그 불러오기", true);
        dialog.setSize(760, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        
        JLabel searchIcon = new JLabel(new String(Character.toChars(0xE8B6)));
        searchIcon.setFont(FontKit.materialIcon(18f));
        searchIcon.setForeground(UITheme.RGB_140_140_155);

        JTextField search = new JTextField();
        search.setFont(UITheme.BODY);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchIcon, BorderLayout.WEST);
        searchRow.add(search, BorderLayout.CENTER);

        JCheckBox onlyUnanalyzed = new JCheckBox("미분석만 보기", true);
        onlyUnanalyzed.setFont(UITheme.BODY);
        onlyUnanalyzed.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBorder(new EmptyBorder(14, 14, 10, 14));
        top.setOpaque(false);
        top.add(searchRow, BorderLayout.CENTER);
        top.add(onlyUnanalyzed, BorderLayout.EAST);

        DefaultListModel<LogPost> model = new DefaultListModel<>();
        JList<LogPost> list = new JList<>(model);
        list.setFixedCellHeight(38); 
        list.setBackground(MainUiParts.ROW_BG);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final int[] hover = {-1};
        list.setCellRenderer(new LogPickRenderer(hover));

        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hover[0] = list.locationToIndex(e.getPoint());
                list.repaint();
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hover[0] = -1;
                list.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0) return;
                LogPost p = model.getElementAt(idx);

                boolean fullyAnalyzed = Services.AI.isAnalyzed(p.id);
                if (fullyAnalyzed) {
                    int r = JOptionPane.showOptionDialog(
                            AiAnalysisView.this,
                            "이미 분석이 완료된 로그입니다. 결과를 열람할까요?",
                            "분석 완료",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"결과 보기", "취소"},
                            "결과 보기"
                    );
                    if (r != 0) return;

                    AppState.get().setSelectedLogId(p.id);
                    pending.clear();
                    chatList.removeAll();
                    refreshAll();
                    dialog.dispose();
                    return;
                }

                AppState.get().setSelectedLogId(p.id);
                pending.clear();
                chatList.removeAll();
                refreshAll();
                pushEttiMessage("선택한 로그 기준으로 분석을 도와드릴게요.\n아래에서 원하는 항목을 선택해 주세요.");
                dialog.dispose();
            }
        });

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.getViewport().setBackground(MainUiParts.ROW_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16); 

        JButton close = new RoundedButton("닫기");
        styleGreyButton(close);
        close.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setOpaque(false);
        bottom.add(close);

        Runnable refresh = () -> {
            String q = search.getText().trim();
            boolean only = onlyUnanalyzed.isSelected();

            List<LogPost> all = Services.LOGS.list().stream()
                    .filter(p -> p != null && LogPost.TYPE_LOG.equals(p.type))
                    .sorted(Comparator.comparing((LogPost p) -> p.createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .collect(Collectors.toList());

            if (!q.isBlank()) {
                all = all.stream().filter(p -> {
                    String t = (p.title == null) ? "" : p.title;
                    return t.contains(q);
                }).collect(Collectors.toList());
            }

            if (only) {
                all = all.stream().filter(p -> !Services.AI.isAnalyzed(p.id)).collect(Collectors.toList());
            }

            model.clear();
            for (LogPost p : all) model.addElement(p);
        };

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh.run(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh.run(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh.run(); }
        });
        onlyUnanalyzed.addActionListener(e -> refresh.run());

        dialog.getContentPane().setBackground(UITheme.BG);
        dialog.add(top, BorderLayout.NORTH);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        refresh.run();
        dialog.setVisible(true);
    }

    private class LogPickRenderer extends JPanel implements ListCellRenderer<LogPost> {
        private final TypeChip chip = new TypeChip();
        private final JLabel title = new JLabel();
        private final JLabel date = new JLabel();
        private final int[] hover;

        LogPickRenderer(int[] hover) {
            this.hover = hover;
            setOpaque(true);
            setLayout(new GridBagLayout());
            
            setBorder(new EmptyBorder(0, 12, 0, 12));

            title.setFont(UITheme.BODY);
            title.setForeground(UITheme.TEXT);

            date.setFont(UITheme.CAPTION);
            date.setForeground(UITheme.MUTED_TEXT);

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.insets = new Insets(0, 0, 0, 0);
            c.anchor = GridBagConstraints.CENTER;

            
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            add(chip, c);

            
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 8, 0, 0);
            add(title, c);

            
            c.gridx = 2;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 12, 0, 0);
            add(date, c);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LogPost> list, LogPost value, int index,
                                                     boolean isSelected, boolean cellHasFocus) {
            boolean analyzed = (value != null) && Services.AI.isAnalyzed(value.id);
            chip.setText(analyzed ? "분석완료" : "미분석");
            
            chip.setColors(analyzed ? UITheme.RGB_245_245_248 : UITheme.ACCENT_LAVENDER_BG,
                    analyzed ? UITheme.MUTED_TEXT : UITheme.ACCENT_PURPLE);

            title.setText((value == null || value.title == null || value.title.isBlank()) ? "(제목 없음)" : value.title);
            date.setText((value == null || value.createdAt == null) ? "" : value.createdAt.format(DATE_FMT));

            MainUiParts.applyRowStateBackground(this, isSelected, index == hover[0]);
            return this;
        }
    }

    
    
    

    private void pushEttiMessage(String text) {
        chatList.add(messageBubble(text, true));
        chatList.add(Box.createVerticalStrut(10));
        chatList.revalidate();
        chatList.repaint();
        scrollChatToBottom();
    }

    private void pushUserMessage(String text) {
        chatList.add(messageBubble(text, false));
        chatList.add(Box.createVerticalStrut(10));
        chatList.revalidate();
        chatList.repaint();
        scrollChatToBottom();
    }

    private JComponent messageBubble(String text, boolean isEtti) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        RoundedTextBubble bubble = new RoundedTextBubble(
                text,
                isEtti ? UITheme.YELLOW_200 : UITheme.ACCENT_LAVENDER_BG,
                isEtti ? UITheme.RGB_80_80_90 : UITheme.TEXT,
                440 
        );

        if (isEtti) {
            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(new EmptyBorder(0, 0, 0, 0));

            JLabel name = new JLabel("에티");
            name.setFont(UITheme.CAPTION);
            name.setForeground(UITheme.MUTED_TEXT);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);

            left.add(name);
            left.add(Box.createVerticalStrut(4));
            left.add(bubble);

            wrap.add(left, BorderLayout.WEST);
        } else {
            JPanel right = new JPanel();
            right.setOpaque(false);
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setBorder(new EmptyBorder(0, 0, 0, 0));

            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(bubble);

            wrap.add(right, BorderLayout.EAST);
        }
        return wrap;
    }

    private void scrollChatToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    
    private static class RoundedTextBubble extends JPanel {
        private final Color bg;

        RoundedTextBubble(String text, Color bg, Color fg, int maxWidth) {
            super(new BorderLayout());
            this.bg = bg;
            setOpaque(false);

            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setOpaque(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(UITheme.BODY);
            area.setForeground(fg);
            area.setBorder(new EmptyBorder(10, 12, 10, 12));

            
            Dimension pref = area.getPreferredSize();
            setMaximumSize(new Dimension(maxWidth, pref.height));
            setPreferredSize(new Dimension(maxWidth, pref.height));
add(area, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    
    

    private JPanel makeCard(boolean softerBg) {
        JPanel p = new JPanel();
        p.setBackground(softerBg ? UITheme.RGB_250_250_252 : UITheme.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return p;
    }

    private void styleGreyButton(AbstractButton b) {
        b.setBackground(UITheme.QUICK_CHIP_BG);
        b.setForeground(UITheme.QUICK_CHIP_FG);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setFocusPainted(false);

        if (b instanceof RoundedButton) {
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        } else {
            b.setOpaque(true);
            b.setContentAreaFilled(true);
        }
    }

    private void styleWhiteButton(AbstractButton b) {
        b.setBackground(UITheme.WHITE);
        b.setForeground(UITheme.TEXT);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setFocusPainted(false);

        if (b instanceof RoundedButton) {
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        } else {
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorder(BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true));
        }
    }
    
    private void installQuickChipBehavior(List<? extends AbstractButton> chips) {
        for (AbstractButton b : chips) {
            b.setFocusPainted(false);

            if (b instanceof RoundedButton) {
                b.setOpaque(false);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
            }

            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (b != selectedQuickChip && b.isEnabled()) {
                        b.setBackground(UITheme.QUICK_CHIP_BG_HOVER);
                        b.repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (b != selectedQuickChip && b.isEnabled()) {
                        b.setBackground(UITheme.QUICK_CHIP_BG);
                        b.repaint();
                    }
                }
            });
        }
    }

    private void setQuickSelected(AbstractButton selected) {
        selectedQuickChip = selected;

        if (quickChips != null) {
            for (AbstractButton b : quickChips) {
                if (b == selected) {
                    b.setBackground(UITheme.QUICK_CHIP_BG_SELECTED);
                } else {
                    b.setBackground(UITheme.QUICK_CHIP_BG);
                }
            }
        }
    }

    private void clearQuickSelected() {
        selectedQuickChip = null;
        if (quickChips != null) {
            for (AbstractButton b : quickChips) {
                b.setBackground(UITheme.QUICK_CHIP_BG);
            }
        }
    }

    private class RecordRenderer extends JPanel implements ListCellRenderer<AiAnalysisRecord> {
        private final TypeChip chip = new TypeChip();
        private final JLabel title = new JLabel();
        private final JLabel date = new JLabel();

        RecordRenderer() {
            setOpaque(true);
            setLayout(new GridBagLayout());
            
            setBorder(new EmptyBorder(0, 12, 0, 12));

            title.setFont(UITheme.BODY);
            title.setForeground(UITheme.TEXT);

            date.setFont(UITheme.CAPTION);
            date.setForeground(UITheme.MUTED_TEXT);

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.insets = new Insets(0, 0, 0, 0);
            
            c.anchor = GridBagConstraints.CENTER;

            
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 0, 0, 0);
            add(chip, c);

            
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 8, 0, 0);
            add(title, c);

            
            c.gridx = 2;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 12, 0, 0);
            add(date, c);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends AiAnalysisRecord> list, AiAnalysisRecord value, int index,
                                                     boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                chip.setText("");
                title.setText("");
                date.setText("");
            } else {
                
                chip.setText(value.type == null ? "" : value.type.label);
                chip.setColors(UITheme.RGB_245_245_248, UITheme.TEXT);

                title.setText(extractBaseTitle(value.title));
                date.setText(value.createdAt == null ? "" : value.createdAt.format(DATE_FMT));
            }

            
            MainUiParts.applyRowStateBackground(this, isSelected, false);
            return this;
        }

        private String extractBaseTitle(String full) {
            if (full == null) return "";
            int idx = full.indexOf(" · ");
            if (idx > 0) return full.substring(0, idx);
            return full;
        }
    }

    

    private static class TypeChip extends JComponent {
        private String text = "";
        private Color bg = UITheme.RGB_245_245_248;
        private Color fg = UITheme.TEXT;

        TypeChip() {
            setFont(UITheme.CAPTION);
            setOpaque(false);
        }

        void setText(String t) {
            text = (t == null) ? "" : t;
            revalidate();
            repaint();
        }

        void setColors(Color bg, Color fg) {
            this.bg = bg;
            this.fg = fg;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 18;
            int h = 22;
            return new Dimension(w, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 14;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = 9;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }
}
