package io.github.intellij.dlanguage.settings;

import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TextAccessor;
import com.intellij.util.messages.Topic;
import io.github.intellij.dlanguage.messagebus.ToolChangeListener;
import io.github.intellij.dlanguage.messagebus.Topics;
import io.github.intellij.dlanguage.utils.ExecUtil;
import io.github.intellij.dlanguage.utils.GuiUtil;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The "D Tools" option in: Settings -> Languages & Frameworks -> D Tools.
 */
public class DLanguageToolsConfigurable implements SearchableConfigurable {

    public static final String D_TOOLS_ID = "D Tools";
    private static final Logger LOG = Logger.getInstance(DLanguageToolsConfigurable.class);
    private final PropertiesComponent propertiesComponent;
    private final List<Tool> properties;
    // Swing components.
    private JPanel mainPanel;
    private TextFieldWithBrowseButton dubPath;
    private RawCommandLineEditor dubFlags;
    private JButton dubAutoFind;
    private JTextField dubVersion;
    private TextFieldWithBrowseButton dscannerPath;
    private RawCommandLineEditor dscannerFlags;
    private JButton dscannerAutoFind;
    private JTextField dscannerVersion;
    private TextFieldWithBrowseButton dcdPath;
    private RawCommandLineEditor dcdFlags;
    private JButton dcdAutoFind;
    private JTextField dcdVersion;
    private TextFieldWithBrowseButton dcdClientPath;
    private RawCommandLineEditor dcdClientFlags;
    private JButton dcdClientAutoFind;
    private JTextField dcdClientVersion;
    private TextFieldWithBrowseButton dFormatPath;
    private RawCommandLineEditor dFormatFlags;
    private JButton dFormatAutoFind;
    private JTextField dFormatVersion;
    private TextFieldWithBrowseButton dFixPath;
    private RawCommandLineEditor dFixFlags;
    private JButton dFixAutoFind;
    private JTextField dFixVersion;
    private TextFieldWithBrowseButton GDBPath;
    private JButton GDBAutoFind;
    private RawCommandLineEditor GDBFlags;
    private JTextField GDBVersion;
    private JTabbedPane tabbedPane1;
    private JCheckBox chkNativeCodeCompletion;
    private JCheckBox disableFormatterSyntaxErrorCheckBox;
    private TextFieldWithBrowseButton workspaceDPath;
    private JTextField workspaceDVersion;
    private JButton workspaceDAutoFind;
    private RawCommandLineEditor workspaceDFlags;

    public DLanguageToolsConfigurable(@NotNull final Project project) {
        this.propertiesComponent = PropertiesComponent.getInstance();

        final SearchableOptionsRegistrar sor = SearchableOptionsRegistrar.getInstance();
        sor.addOption("dub", null, "dub", D_TOOLS_ID, D_TOOLS_ID);
        sor.addOption("dscanner", null, "dscanner", D_TOOLS_ID, D_TOOLS_ID);
        sor.addOption("dcd", null, "dcd", D_TOOLS_ID, D_TOOLS_ID);
        sor.addOption("dfmt", null, "dfmt", D_TOOLS_ID, D_TOOLS_ID);
        sor.addOption("dfix", null, "dfix", D_TOOLS_ID, D_TOOLS_ID);
        sor.addOption("gdb", null, "gdb", D_TOOLS_ID, D_TOOLS_ID);

        properties = Arrays.asList(
            new Tool(project, "dub", ToolKey.DUB_KEY, dubPath, dubFlags,
                dubAutoFind, dubVersion, "--version", Topics.DUB_TOOL_CHANGE),
            new Tool(project, "dscanner", ToolKey.DSCANNER_KEY, dscannerPath, dscannerFlags,
                dscannerAutoFind, dscannerVersion, "--version", Topics.DSCANNER_TOOL_CHANGE),

            new Tool(project, "workspace-d", ToolKey.WORKSPACE_D_KEY, workspaceDPath, workspaceDFlags,
                workspaceDAutoFind, workspaceDVersion, "--version", Topics.WORKSPACE_D_TOOL_CHANGE),

            new Tool(project, "dcd-server", ToolKey.DCD_SERVER_KEY, dcdPath, dcdFlags,
                dcdAutoFind, dcdVersion, "--version", Topics.DCD_SERVER_TOOL_CHANGE),
            new Tool(project, "dcd-client", ToolKey.DCD_CLIENT_KEY, dcdClientPath, dcdClientFlags,
                dcdClientAutoFind, dcdClientVersion, "--version", Topics.DCD_CLIENT_TOOL_CHANGE),
            new Tool(project, "dfmt", ToolKey.DFORMAT_KEY, dFormatPath, dFormatFlags,
                dFormatAutoFind, dFormatVersion, "--version", Topics.DFMT_TOOL_CHANGE),
            new Tool(project, "dfix", ToolKey.DFIX_KEY, dFixPath, dFixFlags,
                dFixAutoFind, dFixVersion, "--version", Topics.DFIX_TOOL_CHANGE),
            new Tool(project, "gdb", ToolKey.GDB_KEY, GDBPath, GDBFlags,
                GDBAutoFind, GDBVersion, "--version", Topics.GDB_TOOL_CHANGE)
        );
    }

    /**
     * Heuristically finds the version number. Current implementation is the identity function since
     * cabal plays nice.
     */
    public static String getVersion(final String cmd, final String versionFlag) {
        final @Nullable String versionOutput = ExecUtil.readCommandLine(null, cmd, versionFlag);

        if (StringUtil.isNotEmpty(versionOutput)) {
            final String version = versionOutput.split("\n")[0].trim();
            LOG.info(String.format("%s [%s]", cmd, version));
            return version;
        }
        return "";
    }

    @NotNull
    @Override
    public String getId() {
        return D_TOOLS_ID;
    }

    @Nullable
    @Override
    public Runnable enableSearch(final String option) {
        return () -> {
            if(StringUtil.isNotEmpty(option)) {
                final int tabCount = tabbedPane1.getTabCount();

                for (int i = 0; i < tabCount-1; i++) {
                    final String title = tabbedPane1.getTitleAt(i);
                    if(title.toLowerCase().contains(option.toLowerCase())) {
                        tabbedPane1.setSelectedIndex(i);
                    }
                }
            }

        };
    }

    @Nls
    @Override
    public String getDisplayName() {
        return D_TOOLS_ID;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    /**
     * Enables the apply button if anything changed.
     */
    @Override
    public boolean isModified() {
        for (final Property property : properties) {
            if (property.isModified()) {
                return true;
            }
        }
        return
            propertiesComponent.getBoolean("USE_NATIVE_CODE_COMPLETION") != chkNativeCodeCompletion
                .isSelected()
                || propertiesComponent.getBoolean("DISABLE_SYNTAX_ERROR_FORMATTER_WARNING")
                != disableFormatterSyntaxErrorCheckBox.isSelected();
    }

    /**
     * Triggered when the user pushes the apply button.
     */
    @Override
    public void apply() {
        updateVersionInfoFields();
        saveState();
    }

    /**
     * Triggered when the user pushes the cancel button.
     */
    @Override
    public void reset() {
        restoreState();
    }

    @Override
    public void disposeUIResources() {

    }

    /**
     * Updates the version info fields for all files configured.
     */
    private void updateVersionInfoFields() {
        for (final Property property : properties) {
            if (property instanceof Versioned) {
                ((Versioned) property).updateVersion();
            }
        }
    }

    /**
     * Persistent save of the current state.
     */
    private void saveState() {
        LOG.info("Saving D Tools Config");
        for (final Property property : properties) {
            property.saveState();
        }
        propertiesComponent
            .setValue("USE_NATIVE_CODE_COMPLETION", chkNativeCodeCompletion.isSelected());
        propertiesComponent.setValue("DISABLE_SYNTAX_ERROR_FORMATTER_WARNING",
            disableFormatterSyntaxErrorCheckBox.isSelected());
    }

    /**
     * Restore components to the initial state.
     */
    private void restoreState() {
        LOG.info("Restore D Tools Config");
        for (final Property property : properties) {
            property.restoreState();
        }
        if (!propertiesComponent.isValueSet("USE_NATIVE_CODE_COMPLETION")) {
            propertiesComponent.setValue("USE_NATIVE_CODE_COMPLETION", false);
        }
        if (!propertiesComponent.isValueSet("DISABLE_SYNTAX_ERROR_FORMATTER_WARNING")) {
            propertiesComponent.setValue("DISABLE_SYNTAX_ERROR_FORMATTER_WARNING", false);
        }
        chkNativeCodeCompletion
            .setSelected(propertiesComponent.getBoolean("USE_NATIVE_CODE_COMPLETION"));
        disableFormatterSyntaxErrorCheckBox
            .setSelected(propertiesComponent.getBoolean("DISABLE_SYNTAX_ERROR_FORMATTER_WARNING"));
    }

    interface Property {

        boolean isModified();

        void saveState();

        void restoreState();
    }

    interface Versioned {

        void updateVersion();
    }

    /**
     * Manages the state of a PropertyComponent and its respective field.
     */
    class PropertyField implements Property {

        public final TextAccessor field;
        public final String propertyKey;
        public String oldValue;

        PropertyField(@NotNull final String propertyKey, @NotNull final TextAccessor field) {
            this(propertyKey, field, "");
        }

        PropertyField(@NotNull final String propertyKey, @NotNull final TextAccessor field,
            @NotNull final String defaultValue) {
            this.propertyKey = propertyKey;
            this.field = field;
            this.oldValue = propertiesComponent.getValue(propertyKey, defaultValue);
            field.setText(oldValue);
        }

        public boolean isModified() {
            return !field.getText().equals(oldValue);
        }

        public void saveState() {
            propertiesComponent.setValue(propertyKey, oldValue = field.getText());
        }

        public void restoreState() {
            field.setText(oldValue);
        }
    }

    /**
     * Manages the group of fields which reside to a particular tool.
     */
    class Tool implements Property, Versioned {

        public final Project project;
        public final String command;
        public final ToolKey key;
        public final TextFieldWithBrowseButton pathField;
        public final RawCommandLineEditor flagsField;
        public final JTextField versionField;
        public final String versionParam;
        public final JButton autoFindButton;
        public final List<PropertyField> propertyFields;
        public final Topic<ToolChangeListener> topic;
        private final ToolChangeListener publisher;

        Tool(final Project project, final String command, final ToolKey key,
            final TextFieldWithBrowseButton pathField,
            final RawCommandLineEditor flagsField, final JButton autoFindButton,
            final JTextField versionField,
            final String versionParam, final Topic<ToolChangeListener> topic) {
            this.project = project;
            this.command = command;
            this.key = key;
            this.pathField = pathField;
            this.flagsField = flagsField;
            this.versionField = versionField;
            this.versionParam = versionParam;
            this.autoFindButton = autoFindButton;
            this.topic = topic;
            this.publisher = topic == null ? null : project.getMessageBus().syncPublisher(topic);

            this.propertyFields = Arrays.asList(
                new PropertyField(key.getPathKey(), pathField),
                new PropertyField(key.getFlagsKey(), flagsField));

            GuiUtil.addFolderListener(pathField, command);
            GuiUtil.addApplyPathAction(autoFindButton, pathField, command);
            updateVersion();
        }

        public void updateVersion() {
            final String pathText = pathField.getText();
            final String version =
                StringUtil.isEmpty(pathText) ? "" : getVersion(pathText, versionParam);
            versionField.setText(version);
        }

        public boolean isModified() {
            return propertyFields.parallelStream().anyMatch(PropertyField::isModified);
        }

        public void saveState() {
            if (isModified() && publisher != null) {
                publisher.onToolSettingsChanged(
                    new ToolSettings(pathField.getText(), flagsField.getText()));
            }
            for (final PropertyField propertyField : propertyFields) {
                propertyField.saveState();
            }
        }

        public void restoreState() {
            for (final PropertyField propertyField : propertyFields) {
                propertyField.restoreState();
            }
        }
    }
}
