package io.github.intellij.dlanguage.codeinsight.dcd.completions

interface Completion
{
    fun completionType(): String

    fun completionText(): String
}
