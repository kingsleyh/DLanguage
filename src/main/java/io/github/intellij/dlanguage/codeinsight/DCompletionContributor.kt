package io.github.intellij.dlanguage.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Function
import com.intellij.util.ProcessingContext
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.codeinsight.dcd.DCDCompletionClient
import io.github.intellij.dlanguage.codeinsight.dcd.completions.DCDModel
import io.github.intellij.dlanguage.codeinsight.render.BasicRenderer
import io.github.intellij.dlanguage.icons.DlangIcons

class DCompletionContributor : CompletionContributor()
{


    private val dcdCompletionClient = DCDCompletionClient()

    init
    {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(DLanguage),
                object : CompletionProvider<CompletionParameters>()
                {
                    public override fun addCompletions(parameters: CompletionParameters,
                                                       context: ProcessingContext,
                                                       result: CompletionResultSet)
                    {
                        val position = parameters.editor.caretModel.offset
                        val file = parameters.originalFile

                        for (model in  dcdCompletionClient.autoComplete(position, file))
                        {
                            result.addElement(BasicRenderer(model))
                        }
                    }
                }
        )
    }

    class DCDCompletionProvider : CompletionProvider<CompletionParameters>()
    {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet)
        {

        }

    }


    /**
     * Adjust the error message when no lookup is found.
     */
    override fun handleEmptyLookup(parameters: CompletionParameters, editor: Editor?): String?
    {
        return "DLanguage: no completion found."
    }

    companion object
    {

        fun createLookupElement(name: String, module: String, type: String): LookupElement
        {
            return LookupElementBuilder.create(name).withIcon(DlangIcons.FILE)
                    //                .withTailText(" (" + module + ')', true)
                    .withTypeText(type)
        }

        val stringToLookupElement:  Function<String, LookupElement> = Function { s -> LookupElementBuilder.create(s).withIcon(DlangIcons.FILE) }
    }

}
