// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.masterthought.dlanguage.stubs.DLanguageSharedStaticConstructorStub;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;

public interface DLanguageSharedStaticConstructor extends StatementContainer, DNamedElement, HasVisibility, HasTemplateArguments, HasArguments, StubBasedPsiElement<DLanguageSharedStaticConstructorStub> {

  @Nullable
  DLanguageFunctionBody getFunctionBody();

  @NotNull
  PsiElement getKwShared();

  @NotNull
  PsiElement getKwStatic();

  @NotNull
  PsiElement getKwThis();

  @NotNull
  PsiElement getOpParLeft();

  @NotNull
  PsiElement getOpParRight();

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
