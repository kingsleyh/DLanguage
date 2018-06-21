package io.github.intellij.dlanguage.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import io.github.intellij.dlanguage.codeinsight.dcd.WorkspaceD;
import io.github.intellij.dlanguage.module.DlangModuleType;
import io.github.intellij.dlanguage.settings.ToolKey;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class RestartWorkspaceD extends AnAction implements DumbAware {
    private static final Logger LOG = Logger.getInstance(RestartWorkspaceD.class);

    @Override
    public void update(@NotNull final AnActionEvent e) {
        e.getPresentation().setEnabled(enabled(e));
    }

    private static boolean enabled(@NotNull final AnActionEvent e) {
        final Project project = getEventProject(e);
        if (project == null) return false;
        final String cdcServerPath = ToolKey.DCD_SERVER_KEY.getPath();
        return cdcServerPath != null && !cdcServerPath.isEmpty() && DlangModuleType.findModules(project).size() > 0;
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final String prefix = "Unable to restart workspace-d - ";
        final Project project = e.getProject();
        if (project == null) { displayError(e, prefix + "No active project."); return; }
        final Collection<Module> modules = DlangModuleType.findModules(project);
        final int size = modules.size();
        if (size == 0) displayError(e, prefix + "No DLanguage modules are used in this project.");
        else if (size == 1) doRestart(e, modules.iterator().next());
        else showModuleChoicePopup(e, project, modules);
    }

    private static void showModuleChoicePopup(@NotNull final AnActionEvent e, final Project project, final Collection<Module> modules) {
        final JList<Module> list = new JBList<>(JBList.createDefaultListModel(modules));
        final JBPopup popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle("Restart dcd-server for module")
                .setItemChoosenCallback(() -> doRestart(e, (Module) list))
                .createPopup();
        popup.showCenteredInCurrentWindow(project);
    }

    private static void doRestart(@NotNull final AnActionEvent e, @NotNull final Module module) {
        final WorkspaceD workspaceD = module.getComponent(WorkspaceD.class);
        if (workspaceD == null) displayError(e, "Could not find module component for workspace-d.");
        else workspaceD.restart();
    }

    private static void displayError(@NotNull final AnActionEvent e, @NotNull final String message) {
        final String groupId = e.getPresentation().getText();
        Notifications.Bus.notify(new Notification(
                groupId, "Restart workspace-d", message, NotificationType.ERROR), getEventProject(e));
        LOG.warn(message);
    }
}
