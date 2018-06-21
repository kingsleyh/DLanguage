package io.github.intellij.dlanguage.codeinsight.dcd.completions

enum class DCDCompletionKind(val id: String)
{
    ClassName("c"),
    InterfaceName("i"),
    StructName("s"),
    UnionName("u"),
    VariableName("v"),
    MemberVariableName("m"),
    Keyword("k"),
    Function("f"),
    EnumName("g"),
    EnumMember("e"),
    PackageName("P"),
    ModuleName("M"),
    Array("a"),
    AssociativeArray("A"),
    AliasName("l"),
    TemplateName("t"),
    MixinTemplate("T")
}
