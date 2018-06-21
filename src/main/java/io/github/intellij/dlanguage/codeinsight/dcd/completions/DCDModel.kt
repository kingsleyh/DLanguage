package io.github.intellij.dlanguage.codeinsight.dcd.completions

class DCDModel(var value: String, id: String, type: String? = null)
{
    var kind: DCDCompletionKind

    var typeText: String? = null

    init
    {
        kind = fromId(id)
        typeText = type

    }

    fun fromId(id: String): DCDCompletionKind
    {
        when (id)
        {
            "c" -> return DCDCompletionKind.ClassName
            "i" -> return DCDCompletionKind.InterfaceName
            "s" -> return DCDCompletionKind.StructName
            "u" -> return DCDCompletionKind.UnionName
            "v" -> return DCDCompletionKind.VariableName
            "m" -> return DCDCompletionKind.MemberVariableName
            "k" -> return DCDCompletionKind.Keyword
            "f" -> return DCDCompletionKind.Function
            "g" -> return DCDCompletionKind.EnumName
            "e" -> return DCDCompletionKind.EnumMember
            "P" -> return DCDCompletionKind.PackageName
            "M" -> return DCDCompletionKind.ModuleName
            "a" -> return DCDCompletionKind.Array
            "A" -> return DCDCompletionKind.AssociativeArray
            "l" -> return DCDCompletionKind.AliasName
            "t" -> return DCDCompletionKind.TemplateName
            "T" -> return DCDCompletionKind.MixinTemplate
        }
        throw RuntimeException("Unknow DCD Completion kind: $id")
    }
}
