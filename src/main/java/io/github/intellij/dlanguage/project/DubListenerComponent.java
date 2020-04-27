package io.github.intellij.dlanguage.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES;

/**
 * Created by francis on 1/27/2018.
 */
public class DubListenerComponent implements StartupActivity {

    @Override
    public void runActivity(@NotNull final Project project) {
        for (final Module module : ModuleManager.getInstance(project).getModules()) {
            final VirtualFile dubFile = DubConfigFileListener.getDubFileFromModule(module);
            if (dubFile != null) {
                module.getProject()
                    .getMessageBus()
                    .connect()
                    .subscribe(VFS_CHANGES, new BulkVirtualFileListenerAdapter(new DubConfigFileListener(dubFile, project, module)));
            }
        }
    }
}
