// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi;

import net.masterthought.dlanguage.psi.interfaces.*;
import net.masterthought.dlanguage.psi.interfaces.containers.StatementContainer;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.masterthought.dlanguage.stubs.DLanguageDestructorStub;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;

public interface DLanguageDestructor extends StatementContainer, DNamedElement, HasVisibility, HasTemplateArguments, HasArguments, StubBasedPsiElement<DLanguageDestructorStub> {

  @Nullable
  DLanguageFunctionBody getFunctionBody();

  @Nullable
  DLanguageMemberFunctionAttributes getMemberFunctionAttributes();

  @NotNull
  PsiElement getKwThis();

  @NotNull
  PsiElement getOpParLeft();

  @NotNull
  PsiElement getOpParRight();

  @Nullable
  PsiElement getOpScolon();

  @NotNull
  PsiElement getOpTilda();

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
