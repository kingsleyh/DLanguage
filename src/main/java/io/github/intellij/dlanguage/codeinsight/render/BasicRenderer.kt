package io.github.intellij.dlanguage.codeinsight.render

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import io.github.intellij.dlanguage.codeinsight.dcd.completions.DCDCompletionKind
import io.github.intellij.dlanguage.codeinsight.dcd.completions.DCDModel
import io.github.intellij.dlanguage.icons.DlangIcons

class BasicRenderer(internal var model: DCDModel) : LookupElement()
{
    override fun getLookupString(): String
    {
        return if (model.kind === DCDCompletionKind.Function)
            model.value + "()"
        else
            model.value
    }

    override fun renderElement(presentation: LookupElementPresentation)
    {
        presentation.icon = DlangIcons.FILE
        presentation.itemText = model.value
        presentation.isItemTextBold = true
        // if typeText is not null that means it is user code
        // else it is language specific code
        if (model.typeText != null)
            presentation.typeText = model.typeText
        else
            presentation.isItemTextBold = false

        when (model.kind)
        {
            DCDCompletionKind.ClassName -> presentation.icon = DlangIcons.NODE_CLASS
            DCDCompletionKind.InterfaceName -> presentation.icon = DlangIcons.NODE_INTERFACE
            DCDCompletionKind.StructName -> presentation.icon = DlangIcons.NODE_STRUCT
            DCDCompletionKind.UnionName -> presentation.icon = DlangIcons.NODE_UNION
            DCDCompletionKind.VariableName -> presentation.icon = DlangIcons.NODE_FIELD
            DCDCompletionKind.MemberVariableName -> presentation.icon = DlangIcons.NODE_FIELD
            DCDCompletionKind.Keyword -> presentation.icon = null
            DCDCompletionKind.Function -> presentation.icon = DlangIcons.NODE_FUNCTION
            DCDCompletionKind.EnumName -> presentation.icon = DlangIcons.NODE_ENUM
            DCDCompletionKind.EnumMember -> presentation.icon = DlangIcons.NODE_FIELD
            DCDCompletionKind.PackageName -> presentation.icon = null
            DCDCompletionKind.ModuleName -> presentation.icon = DlangIcons.MODULE
            DCDCompletionKind.Array ->
            {
            }
            DCDCompletionKind.AssociativeArray ->
            {
            }
            DCDCompletionKind.AliasName -> presentation.icon = DlangIcons.NODE_ALIAS
            DCDCompletionKind.TemplateName ->
            {
            }
            DCDCompletionKind.MixinTemplate -> presentation.icon = DlangIcons.NODE_MIXIN
        }
    }
}
