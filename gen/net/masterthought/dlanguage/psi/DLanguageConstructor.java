// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi;

import net.masterthought.dlanguage.psi.interfaces.*;
import net.masterthought.dlanguage.psi.interfaces.containers.StatementContainer;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.masterthought.dlanguage.stubs.DLanguageConstructorStub;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;

public interface DLanguageConstructor extends StatementContainer, DNamedElement, HasVisibility, HasTemplateArguments, HasArguments, StubBasedPsiElement<DLanguageConstructorStub> {

  @Nullable
  DLanguageConstructorTemplate getConstructorTemplate();

  @Nullable
  DLanguageFunctionBody getFunctionBody();

  @Nullable
  DLanguageMemberFunctionAttributes getMemberFunctionAttributes();

  @Nullable
  DLanguageParameters getParameters();

  @Nullable
  PsiElement getKwThis();

  @Nullable
  PsiElement getOpScolon();

  @NotNull
  String getName();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiReference getReference();

  @Nullable
  PsiElement setName(String newName);

  @NotNull
  ItemPresentation getPresentation();

}
