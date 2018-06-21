/*
 * Copyright (c) 2017 Patrick Scheibe
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.halirutan.mathematica.errorreporting;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import io.github.intellij.dlanguage.settings.DLanguageToolsConfigurable;
import io.github.intellij.dlanguage.settings.ToolKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collects information about the running IDEA and the error
 */
public class IdeaInformationProxy {

    /**
     * To be used by the custom ErrorReportSubmitter to provide anonymised feedback about errors
     * @param error GitHubErrorBean
     * @param application Application
     * @param appInfo ApplicationInfoEx
     * @param namesInfo ApplicationNamesInfo
     * @param pluginDescriptor PluginDescriptor
     * @return a map of info about the IDE and the plugin and the error
     */
    @NotNull
    public static Map<String, String> getKeyValuePairs(final GitHubErrorBean error,
        final Application application,
        final ApplicationInfoEx appInfo,
        final ApplicationNamesInfo namesInfo,
        final PluginDescriptor pluginDescriptor) {
        final Map<String, String> params = new LinkedHashMap<>(50);

        params.put("error.description", error.getDescription());

        params.put("Plugin Name", error.getPluginName());
        params.put("Plugin Version", error.getPluginVersion());

        params.put("Plugin Id", pluginDescriptor.getPluginId().getIdString());

        params.putAll(getPluginInfo(appInfo, pluginDescriptor, namesInfo));

        params.put("Last Action", error.getLastAction());
        params.put("error.message", error.getMessage());
        params.put("error.stacktrace", error.getStackTrace());
        params.put("error.hash", error.getExceptionHash());

        for (final Attachment attachment : error.getAttachments()) {
            params.put("attachment.name", attachment.getName());
            params.put("attachment.value", attachment.getEncodedBytes());
        }

        return anonymise(params);
    }

    /**
     * @param appInfo an ApplicationInfoEx
     * @param pluginDescriptor a PluginDescriptor
     * @param applicationNamesInfo an ApplicationNamesInfo
     * @return a map of info about the IDE and the plugin that can be used for reporting usage info or errors
     */
    @NotNull
    public static Map<String, String> getPluginInfo(@NotNull final ApplicationInfoEx appInfo,
                                                    @Nullable final PluginDescriptor pluginDescriptor,
                                                    @NotNull final ApplicationNamesInfo applicationNamesInfo) {
        final Map<String, String> params = new HashMap<>();

        params.put("OS Name", SystemInfo.OS_NAME);
        params.put("Java version", SystemInfo.JAVA_VERSION);
        params.put("Java vm vendor", SystemInfo.JAVA_VENDOR);

        params.put("App Name", applicationNamesInfo.getProductName());
        params.put("App Full Name", applicationNamesInfo.getFullProductName());
        params.put("Is EAP", Boolean.toString(appInfo.isEAP()));
        params.put("App Build", appInfo.getBuild().asString());
        params.put("App Version", appInfo.getFullVersion());

        if(pluginDescriptor != null) {
            final IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginDescriptor.getPluginId());
            if (plugin != null) {
                params.put("Plugin Name FallBack", plugin.getName());
                params.put("Plugin Version Fallback", plugin.getVersion());
            }
        }

        final String dubPath = ToolKey.DUB_KEY.getPath();
        if (dubPath != null) {
            final String dubVersion = DLanguageToolsConfigurable
                .getVersion(dubPath, "--version");
            params.put("Dub Version", dubVersion);
        }

        final String workspaceDPath = ToolKey.WORKSPACE_D_KEY.getPath();
        if (workspaceDPath != null) {
            final String workspaceDVersion = DLanguageToolsConfigurable
                .getVersion(workspaceDPath, "--version");
            params.put("WorkspaceD Version", workspaceDVersion);
        }

        final String dcdServerPath = ToolKey.DCD_SERVER_KEY.getPath();
        if (dcdServerPath != null) {
            final String dcdServerVersion = DLanguageToolsConfigurable
                .getVersion(dcdServerPath, "--version");
            params.put("DCD Server Version", dcdServerVersion);
        }

        final String dcdClientPath = ToolKey.DCD_CLIENT_KEY.getPath();
        if (dcdClientPath != null) {
            final String dcdClientVersion = DLanguageToolsConfigurable
                .getVersion(dcdClientPath, "--version");
            params.put("DCD Client Version", dcdClientVersion);
        }

        final String DScannerPath = ToolKey.DSCANNER_KEY.getPath();
        if (DScannerPath != null) {
            final String DScannerVersion = DLanguageToolsConfigurable
                .getVersion(DScannerPath, "--version");
            params.put("DScanner Version", DScannerVersion);
        }

        final String gdbPath = ToolKey.GDB_KEY.getPath();
        if (gdbPath != null) {
            final String GDBVersion = DLanguageToolsConfigurable
                .getVersion(gdbPath, "--version");
            params.put("GDB Version", GDBVersion);
        }

        if (ToolKey.DFORMAT_KEY.getPath() != null) {
            final String DFormatVersion = DLanguageToolsConfigurable
                .getVersion(ToolKey.DFORMAT_KEY.getPath(), "--version");
            params.put("D Format Version", DFormatVersion);
        }

        if (ToolKey.DFIX_KEY.getPath() != null) {
            final String DFixVersion = DLanguageToolsConfigurable
                .getVersion(ToolKey.DFIX_KEY.getPath(), "--version");
            params.put("DFix Version", DFixVersion);
        }

        return anonymise(params);
    }

    // remove user name or project name from all values in the map
    private static Map<String, String> anonymise(@NotNull final Map<String, String> params) {
        final String userName = System.getProperty("user.name");
        final List<String> projects = Arrays.stream(ProjectManager.getInstance().getOpenProjects())
            .map(
                Project::getName).collect(Collectors.toList());

        for (final String key : params.keySet()) {
            String val = params.get(key);
            if (val != null) {
                for (final String projectName : projects) {
                    val = val.replaceAll(projectName, "<removed>");
                }
                val = val.replaceAll(userName, "<removed>");
                params.put(key, val);
            }
        }
        return params;
    }
}
