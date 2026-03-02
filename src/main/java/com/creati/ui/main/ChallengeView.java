package com.creati.ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.model.LogStatus;
import com.creati.ui.components.Chip;
import com.creati.util.FontKit;
import com.creati.util.UITheme;



import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.creati.model.LogPost;
public class ChallengeView extends JPanel {

	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private String query = "";
    private String selectedCategory = null; 
    private static final int LEFT_PANEL_W = 240;

    private final DefaultListModel<String> catModel = new DefaultListModel<>();
    private final JList<String> catList = new JList<>(catModel);

    private final DefaultListModel<LogItem> logModel = new DefaultListModel<>();
    private final JList<LogItem> logList = new JList<>(logModel);

    private final JLabel headerTitle = new JLabel("나의 도전");
    private final JLabel headerSub = new JLabel("분야별로 내 성장 로그를 목록으로 확인하세요.");

    private final JLabel rightTitle = new JLabel("전체");
    private final JLabel rightCount = new JLabel("0개");

    private int hoverCatIndex = -1;
    private int hoverLogIndex = -1;

    // DB: Store(LogStore)에서 로드한 LogPost를 리스트 렌더링용(LogItem)으로 변환해 담음
    private final List<LogItem> allLogs = new ArrayList<>();

    private com.creati.dto.UserDto currentUser;
    private com.creati.dao.LogDao logDao;
    private List<com.creati.dto.CategoryDto> myCategoryList = new ArrayList<>();

    public ChallengeView(com.creati.dto.UserDto user) {
    	this.currentUser = user;
        this.logDao = new com.creati.dao.LogDao();
        
        UITheme.ensureInit();
        FontKit.init();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildExplorer(), BorderLayout.CENTER);

        loadCategories();
        catList.setSelectedIndex(0);
        applyFilter();
    }

    public void setQuery(String q) {
        this.query = (q == null) ? "" : q.trim();
        applyFilter();
    }

    public void clearSearch() {
    	loadCategories();
        setQuery("");
    }
    
    public void refresh() {
        loadCategories();
        selectedCategory = null; // 전체로 초기화
        if (catList.getModel().getSize() > 0) {
            catList.setSelectedIndex(0); // "전체" 선택
        }
        applyFilter(); // 내부에서 reloadFromStore()로 DB 재조회 + 화면 갱신
    }

    private JComponent buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 18, 6, 18));

        headerTitle.setFont(UITheme.BODY_MED);
        headerTitle.setForeground(UITheme.TEXT);

        headerSub.setFont(UITheme.CAPTION);
        headerSub.setForeground(UITheme.RGB_120_120_120);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(headerTitle);
        texts.add(Box.createVerticalStrut(6));
        texts.add(headerSub);

        wrap.add(texts, BorderLayout.WEST);
        return wrap;
    }

    private JComponent buildExplorer() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 18, 18, 18));

        JPanel leftCard = makeCardPanel();
        leftCard.setLayout(new BorderLayout(0, 10));
        leftCard.setBorder(BorderFactory.createCompoundBorder(leftCard.getBorder(), new EmptyBorder(12, 12, 12, 12)));

        JLabel leftTitle = new JLabel("분야");
        leftTitle.setFont(UITheme.BODY_MED);
        leftTitle.setForeground(UITheme.TEXT);

        setupCategoryList();

        JScrollPane catScroll = new JScrollPane(catList);
        catScroll.setBorder(BorderFactory.createEmptyBorder());
        catScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        catScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        leftCard.add(leftTitle, BorderLayout.NORTH);
        leftCard.add(catScroll, BorderLayout.CENTER);

        JPanel rightCard = makeCardPanel();
        rightCard.setLayout(new BorderLayout());
        rightCard.setBorder(BorderFactory.createCompoundBorder(rightCard.getBorder(), new EmptyBorder(12, 12, 12, 12)));

        setupLogList();

        JPanel rightTop = new JPanel();
        rightTop.setOpaque(false);
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

        rightTitle.setFont(UITheme.BODY_MED);
        rightTitle.setForeground(UITheme.TEXT);

        rightCount.setFont(UITheme.CAPTION);
        rightCount.setForeground(UITheme.RGB_120_120_120);

        titleRow.add(rightTitle);
        titleRow.add(Box.createHorizontalStrut(10));
        titleRow.add(rightCount);
        titleRow.add(Box.createHorizontalGlue());

        rightTop.add(titleRow);
        rightTop.add(Box.createVerticalStrut(10));

        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        rightCard.add(rightTop, BorderLayout.NORTH);
        rightCard.add(logScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightCard);
        leftCard.setPreferredSize(new Dimension(LEFT_PANEL_W, 0));
        leftCard.setMinimumSize(new Dimension(LEFT_PANEL_W, 0));
        leftCard.setMaximumSize(new Dimension(LEFT_PANEL_W, Integer.MAX_VALUE));
        split.setResizeWeight(0.0);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(LEFT_PANEL_W));
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(6);
        split.setContinuousLayout(true);
        split.setOpaque(false);

        wrap.add(split, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel makeCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createLineBorder(UITheme.RGB_235_235_240, 1, true));
        return p;
    }

    private void setupCategoryList() {
        catList.setFont(UITheme.BODY);
        catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catList.setFixedCellHeight(46); 
        catList.setBackground(UITheme.WHITE);

        catList.setSelectionBackground(UITheme.TRANSPARENT);
        catList.setSelectionForeground(UITheme.TEXT);
        catList.setFocusable(false);

        catList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverCatIndex = catList.locationToIndex(e.getPoint());
                catList.repaint();
            }
        });
        catList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverCatIndex = -1;
                catList.repaint();
            }
        });

        MainUiParts.installHoverTracking(catList);
		catList.setCellRenderer(MainUiParts.createLabelRowRenderer(v -> String.valueOf(v), v -> makeMaterialIconForCategory(String.valueOf(v)), 10, 12, 10, 12));

        catList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            String v = catList.getSelectedValue();
            if (v == null) return;

            selectedCategory = "전체".equals(v) ? null : v;
            applyFilter();
        });
    }

    private void loadCategories() {
        catModel.clear();
        catModel.addElement("전체"); // 기본 메뉴 추가

        // DB에서 '이 유저'가 글을 쓴 적 있는 카테고리만 가져오기
        if (currentUser != null && currentUser.getId() != null) {
            myCategoryList = logDao.findUsedCategoriesByUserId(currentUser.getId());
            for (com.creati.dto.CategoryDto cat : myCategoryList) {
                catModel.addElement(cat.getName());
            }
        }
    }

    private static Icon makeMaterialIconForCategory(String category) {
        int codePoint = switch (category) {
            case "전체" -> 0xE2C7;
            case "영상" -> 0xE02C;
            case "이미지" -> 0xE3F4;
            case "글" -> 0xE873;
            case "음악" -> 0xE405;
            case "기타" -> 0xE5D4;
            default -> 0xE2C7;
        };
        return MainUiParts.glyphIcon(codePoint, 18f, UITheme.ICON_MUTED);
    }


    private void setupLogList() {
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setFixedCellHeight(46); 
        logList.setBackground(UITheme.WHITE);

        logList.setSelectionBackground(UITheme.TRANSPARENT);
        logList.setSelectionForeground(UITheme.TEXT);
        logList.setFocusable(false);

        logList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverLogIndex = logList.locationToIndex(e.getPoint());
                logList.repaint();
            }
        });

        logList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverLogIndex = -1;
                logList.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 1) return;

                LogItem item = logList.getSelectedValue();
                if (item == null) return;

                // DB: id로 실제 LogPost를 조회해서 상세 화면으로 전달
                LogPost postToOpen = Services.LOGS.getById(item.id);
                if (postToOpen == null) {
                    JOptionPane.showMessageDialog(
                        ChallengeView.this,
                        "DB에서 상세를 찾지 못했어요.\n(id=" + item.id + ")",
                        "상세 조회 실패",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                Window w = SwingUtilities.getWindowAncestor(ChallengeView.this);
                if (w instanceof MainFrame) {
                    ((MainFrame) w).openLogDetail(postToOpen);
                    return;
                }

                JOptionPane.showMessageDialog(
                        ChallengeView.this,
                        "선택한 글:\n" + item.title,
                        "상세 보기",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        MainUiParts.installHoverTracking(logList);
        logList.setCellRenderer(MainUiParts.createRowRenderer((list, value, index, isSelected, isHover) -> {
            LogRowPanel row = new LogRowPanel();
            row.setData(value, DF);
            MainUiParts.applyRowStateBackground(row, isSelected, isHover);
            return row;
        }));
    }

    private void applyFilter() {
        reloadFromStore(); 
        logModel.clear();

        List<LogItem> filtered = new ArrayList<>();
        
        // 디버깅: allLogs에 진짜 데이터가 들어있는지 확인
        System.out.println(">>> [필터 시작] 현재 메모리상의 총 로그 수: " + allLogs.size());

        for (LogItem item : allLogs) {
            // 1. 카테고리 비교 (trim()을 추가하여 공백 제거 비교)
            boolean okCat = (selectedCategory == null) || 
                            "전체".equals(selectedCategory) || 
                            Objects.equals(item.category, selectedCategory);
            
            // 2. 검색어 비교 (공백 제거 및 대소문자 무시)
            String trimmedQuery = query.trim().toLowerCase();
            boolean okQuery = trimmedQuery.isEmpty() || 
                             (item.title != null && item.title.toLowerCase().contains(trimmedQuery));
            
            if (okCat && okQuery) {
                filtered.add(item);
            } else {
                System.out.println(">>> [필터 탈락] 제목: " + item.title + " | 사유: " + 
                                   (!okCat ? "카테고리 불일치" : "검색어 불일치"));
            }
        }

        for (LogItem item : filtered) {
            logModel.addElement(item);
        }

        rightTitle.setText(selectedCategory == null ? "전체" : selectedCategory);
        rightCount.setText(filtered.size() + "개");

        revalidate();
        repaint();
    }

    private void reloadFromStore() {
        allLogs.clear();

        // 1. 선택된 카테고리의 ID 찾기
        Long selectedCatId = null;
        if (selectedCategory != null && !selectedCategory.equals("전체")) {
            for (com.creati.dto.CategoryDto cat : myCategoryList) {
                if (cat.getName().equals(selectedCategory)) {
                    selectedCatId = cat.getId();
                    break;
                }
            }
        }

        // 2. DB에서 유저의 글 목록 가져오기
        if (currentUser != null) {
            List<com.creati.dto.MyLogListDto> dbLogs = logDao.findMyLogList(currentUser.getId(), selectedCatId);

            System.out.println("디버깅: DB에서 가져온 로그 개수 = " + (dbLogs != null ? dbLogs.size() : "null"));
            // 3. 화면에 띄울 수 있게 LogItem으로 변환
            for (com.creati.dto.MyLogListDto dto : dbLogs) {
                // DB의 결과 상태(SUCCESS, FAIL)를 화면용 상태(LogStatus)로 변환
            	LogStatus status = LogStatus.IN_PROGRESS;
            	if ("SUCCESS".equals(dto.getResultStatus())) {
            	    status = LogStatus.DONE; // 성공이면 '완료' 상태로
            	} else if ("FAIL".equals(dto.getResultStatus())) {
            	    status = LogStatus.NEEDS_IMPROVEMENT; // 실패면 '보완 필요' 상태로
            	}

                allLogs.add(new LogItem(
                    String.valueOf(dto.getId()), 
                    dto.getCategoryName(), 
                    "", // 서브 카테고리 (없음)
                    status, 
                    dto.getTitle(), 
                    dto.getCreatedAt() != null ? dto.getCreatedAt().toLocalDate() : LocalDate.now(), 
                    dto.isPublic(),
                    "", "", "", "", "", "" // 나머지 내용들은 목록에서 안 쓰이므로 빈칸
                ));
            }
        }
    }

    private static class LogItem {
        final String id;

        final String category;
        final String subCategory;
        final LogStatus status;
        final String title;
        final LocalDate createdAt;
        final boolean isPublic;

        final String whatIDid;
        final String feeling;
        final String difficulty;
        final String learning;
        final String retryPlan;
        final String link;

        LogItem(
                String id,
                String category,
                String subCategory,
                LogStatus status,
                String title,
                LocalDate createdAt,
                boolean isPublic,
                String whatIDid,
                String feeling,
                String difficulty,
                String learning,
                String retryPlan,
                String link
        ) {
            this.id = id;
            this.category = category;
            this.subCategory = subCategory;
            this.status = status;
            this.title = title;
            this.createdAt = createdAt;
            this.isPublic = isPublic;
            this.whatIDid = whatIDid;
            this.feeling = feeling;
            this.difficulty = difficulty;
            this.learning = learning;
            this.retryPlan = retryPlan;
            this.link = link;
        }
    }

    private LogPost toPost(LogItem item) {
        return new LogPost(
                LogPost.TYPE_LOG,
                item.id,
                item.category,        
                item.subCategory,
                item.status,
                item.title,
                item.createdAt,
                item.isPublic,

                null,   
                null,   
                null,   
                null,   
                null,   
                null,   
                null,   
                item.whatIDid,    
                null,   
                null,   
                item.learning,    
                null,   
                null,   
                null,   
                item.retryPlan,   
                item.link,        // linkUrl
                null    
        );
    }

    private interface HoverIndexProvider {
        int getHoverIndex();
    }

    private static class CategoryCellRenderer extends DefaultListCellRenderer {
        private final HoverIndexProvider hover;

        CategoryCellRenderer(HoverIndexProvider hover) {
            this.hover = hover;
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                     boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);

            String name = String.valueOf(value);

            c.setText(name);
            c.setFont(UITheme.BODY);
            c.setForeground(UITheme.TEXT);
            c.setBorder(new EmptyBorder(10, 12, 10, 12));

            boolean isHover = (hover.getHoverIndex() == index);

            MainUiParts.applyRowStateBackground(c, isSelected, isHover);

            c.setIcon(makeMaterialIconForCategory(name));
            c.setIconTextGap(12);

            return c;
        }

        private static String mi(int codePointHex) {
            return new String(Character.toChars(codePointHex));
        }

        private static Icon makeMaterialIconForCategory(String category) {
            String glyph = switch (category) {
                case "전체" -> mi(0xE2C7);
                case "영상" -> mi(0xE02C);
                case "이미지" -> mi(0xE3F4);
                case "글" -> mi(0xE873);
                case "음악" -> mi(0xE405);
                case "기타" -> mi(0xE5D4);
                default -> mi(0xE2C7);
            };

            return new FontIcon(glyph, FontKit.materialIcon(18f), UITheme.RGB_140_140_155);
        }
    }

    private static class LogRowRenderer implements ListCellRenderer<LogItem> {
        private final HoverIndexProvider hover;
        private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        LogRowRenderer(HoverIndexProvider hover) {
            this.hover = hover;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LogItem> list, LogItem value, int index,
                                                     boolean isSelected, boolean cellHasFocus) {
            LogRowPanel p = new LogRowPanel();

            boolean isHover = (hover.getHoverIndex() == index);
            MainUiParts.applyRowStateBackground(p, isSelected, isHover);

            p.setData(value, DF);
            return p;
        }
    }

    private static class LogRowPanel extends JPanel {
        private final Chip chip = new Chip();
        private final JLabel title = new JLabel();
        private final JLabel lock = new JLabel();
        private final JLabel date = new JLabel();

        LogRowPanel() {
		    setOpaque(true);
		    setLayout(new BorderLayout());
		    setBorder(new EmptyBorder(0, 12, 0, 12));

		    title.setFont(UITheme.BODY);
		    title.setForeground(UITheme.TEXT);

		    lock.setFont(FontKit.materialIcon(16f));
		    lock.setForeground(UITheme.RGB_120_120_130);
		    lock.setBorder(new EmptyBorder(0, 2, 0, 2));

		    date.setFont(UITheme.CAPTION);
		    date.setForeground(UITheme.RGB_130_130_140);

		    JPanel left = new JPanel();
		    left.setOpaque(false);
		    left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
		    chip.setAlignmentY(0.5f);
		    title.setAlignmentY(0.5f);
		    lock.setAlignmentY(0.5f);
		    left.add(chip);
		    left.add(Box.createHorizontalStrut(10));
		    left.add(title);
		    left.add(Box.createHorizontalStrut(6));
		    left.add(lock);

		    JPanel inner = new JPanel(new BorderLayout(12, 0));
		    inner.setOpaque(false);
		    inner.add(left, BorderLayout.CENTER);
		    inner.add(date, BorderLayout.EAST);

		    setPreferredSize(new Dimension(0, 44));
		    setLayout(new GridBagLayout());
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.fill = GridBagConstraints.HORIZONTAL;
		    gbc.weightx = 1.0;
		    setBorder(new EmptyBorder(0, 12, 0, 12));
		    add(inner, gbc);
		}

        void setData(LogItem item, DateTimeFormatter df) {
            title.setText(item.title != null ? item.title : "");
            date.setText(item.createdAt != null ? item.createdAt.format(df) : "");

            boolean isPrivate = !item.isPublic;
            lock.setVisible(isPrivate);	
            if (isPrivate) {
                lock.setText(new String(Character.toChars(0xE897))); 
            }

            LogStatus st = (item.status == null) ? LogStatus.IN_PROGRESS : item.status;
            chip.setChip(st.label, UITheme.chipBg(st), UITheme.chipFg(st));
        }
    }

    private static class FontIcon implements Icon {
        private final String text;
        private final Font font;
        private final Color color;

        FontIcon(String text, Font font, Color color) {
            this.text = text;
            this.font = font;
            this.color = color;
        }

        @Override
        public int getIconWidth() { return 22; }

        @Override
        public int getIconHeight() { return 22; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);
            g2.setColor(color);

            FontMetrics fm = g2.getFontMetrics();
            int ty = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();

            g2.drawString(text, x, ty);
            g2.dispose();
        }
    }
}