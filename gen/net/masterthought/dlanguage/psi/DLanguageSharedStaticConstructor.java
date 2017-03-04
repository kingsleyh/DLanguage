// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.masterthought.dlanguage.stubs.DLanguageSharedStaticConstructorStub;

public interface DLanguageSharedStaticConstructor extends StatementContainer, DNamedElement, HasVisibility, StubBasedPsiElement<DLanguageSharedStaticConstructorStub> {

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

}
