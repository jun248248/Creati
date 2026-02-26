package com.creati.ui.main;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;

import com.creati.ui.main.WriteLogView.Draft;

import com.creati.model.LogPost;
// DB(TODO): Replace underlying repositories without changing View.

public class WriteLogController {

    private final WriteLogView view;

    public WriteLogController(WriteLogView view) {
        this.view = view;
    }

    public void onTempSaveRequested(boolean showToast) {
        Draft d = view.snapshotFromWizard(true);
        Services.DRAFTS.upsert(d); // DB(TODO)
        view.clearDirty();
        view.refreshDraftDrawerIfOpen();
        if (showToast) {
            JOptionPane.showMessageDialog(view, "임시 저장 완료!");
        }
    }

    public void onOpenDraftDrawerRequested(JFrame owner) {
        
        view.openDrawer(owner);
    }

    public void onDeleteDraftRequested(String id) {
        Services.DRAFTS.delete(id); // DB(TODO)
        view.refreshDraftDrawerIfOpen();
    }

    public void onSubmitRequested() {
        Draft d = view.snapshotFromWizard(false);
        d.isDraft = false;

        Services.DRAFTS.upsert(d); // DB(TODO)
        view.clearDirty();

        LogPost saved = view.toLogPost(d);
        
        Services.LOGS.upsert(saved);

        view.handleSubmitResult(saved);
    }

    public void onSaveAndNavigateToAiRequested() {
        if (!view.validateStepForController("meta")) return;

        Draft d = view.snapshotFromWizard(false);
        d.isDraft = false;

        Services.DRAFTS.upsert(d); // DB(TODO)

        LogPost saved = view.toLogPost(d);
        Services.LOGS.upsert(saved);

        
        // DB(TODO): 팀원이 DB 연동 시에도 logId만 유지하면 그대로 동작
        AppState.get().setSelectedLogId(saved.id);

        view.clearDirty();

        JOptionPane.showMessageDialog(view, "저장 완료! AI 분석으로 이동할게요.");

        Window w = SwingUtilities.getWindowAncestor(view);
        if (w instanceof MainFrame) {
            ((MainFrame) w).navigateToAi();
            return;
        }
        JOptionPane.showMessageDialog(view, "왼쪽 메뉴의 AI 분석 탭에서 확인할 수 있어요.");
    }
}
