package net.masterthought.dlanguage.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import net.masterthought.dlanguage.icons.DLanguageIcons;
import net.masterthought.dlanguage.psi.*;
import net.masterthought.dlanguage.psi.references.DReference;
import net.masterthought.dlanguage.stubs.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.getChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.getChildrenOfType;


/**
 * Source of the methods pointed out in DLanguage.bnf.
 */
public class DPsiImplUtil {

    // ------------- Identifier ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageIdentifier o) {
        DLanguageIdentifierStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());
        return o.getText();
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageIdentifier o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageIdentifier o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageIdentifierFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageIdentifier o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }


    public static PsiElement findParentOfType(PsiElement element, Class className) {
        if (className.isInstance(element)) {
            return element;
        } else {
            try {
                return findParentOfType(element.getParent(), className);
            } catch(Exception e){
                return null;
            }
        }

    }

    private static String getParentDeclarationDescription(DLanguageIdentifier o) {
        PsiNamedElement funcDecl = (PsiNamedElement) findParentOfType(o, DLanguageFuncDeclaration.class);
        PsiNamedElement classDecl = (PsiNamedElement) findParentOfType(o, DLanguageClassDeclaration.class);
        String description = "";
        if(funcDecl != null){
            description = " [Function] (" + funcDecl.getName() + ")";
        }
        if(classDecl != null){
            description = " [Class] (" + classDecl.getName() + ")";
        }
        return description;
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageIdentifier o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName() + getParentDeclarationDescription(o);
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Identifier ------------------ //

    // ------------- Function Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageFuncDeclaration o) {
        DLanguageFuncDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());
        return o.getIdentifier().getText();
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageFuncDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageFuncDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageFuncDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageFuncDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageFuncDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }

    public static List<DLanguageParameter> getArguments(DLanguageFuncDeclaration o) {
        return Arrays.asList(getChildrenOfType(o.getFuncDeclaratorSuffix().getParameters(), DLanguageParameter.class));
    }

    public static List<DLanguageTemplateParameter> getTemplateArguments(DLanguageFuncDeclaration o) {
        if (o.getFuncDeclaratorSuffix().getTemplateParameters() != null)
            return Arrays.asList(getChildrenOfType(o.getFuncDeclaratorSuffix().getTemplateParameters(), DLanguageTemplateParameter.class));
        return new ArrayList<>();
    }

    public static List<DLanguageProtectionAttribute> getProtection(DLanguageFuncDeclaration o) {
        return Collections.singletonList(getChildOfType(o, DLanguageProtectionAttribute.class));
    }

    // ------------- Function Definition ------------------ //

    // ------------- Class Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageClassDeclaration o) {
        DLanguageClassDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());

        if (o.getIdentifier() != null) {
            return o.getIdentifier().getText();
        } else {
            return "not found";
        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageClassDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageClassDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageClassDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageClassDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageClassDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }


    public static DLanguageProtectionAttribute getProtection(DLanguageClassDeclaration o) {
        return getChildOfType(o, DLanguageProtectionAttribute.class);
    }


    // ------------- Class Definition ------------------ //

    // ------------- Struct Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageStructDeclaration o) {
        DLanguageStructDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());

        if (o.getIdentifier() != null) {
            return o.getIdentifier().getText();
        } else {
            return "not found";
        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageStructDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageStructDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageStructDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageStructDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageStructDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }

    public static DLanguageProtectionAttribute getProtection(DLanguageStructDeclaration o) {
        return getChildOfType(o, DLanguageProtectionAttribute.class);
    }



    // ------------- Struct Definition ------------------ //

    // ------------- Template Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageTemplateDeclaration o) {
        DLanguageTemplateDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());

        if (o.getIdentifier() != null) {
            return o.getIdentifier().getText();
        } else {
            return "not found";
        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageTemplateDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageTemplateDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageTemplateDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageTemplateDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageTemplateDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Template Definition ------------------ //

    // ------------- Constructor ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageConstructor o) {
        return "this";
//        DLanguageConstructorStub stub = o.getStub();
//        if (stub != null) return StringUtil.notNullize(stub.getName());
//
//        PsiElement parent = o.getParent();
//
//
//        while (!(parent instanceof DLanguageClassDeclaration)) {
//            parent = parent.getParent();
//        }

//        return ((DLanguageClassDeclaration)parent).getName() + "constructor";
//        if (o.getIdentifier() != null) {
//            return o.getIdentifier().getText();
//        } else {
//            return "not found";
//        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageConstructor o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageConstructor o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageConstructorFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageConstructor o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageConstructor o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                String string = "";
                for (PsiElement psiElement : o.getChildren()) {
                    if(psiElement instanceof DLanguageParametersImpl)
                        string += psiElement.getText();
                }
                return o.getName() + string;
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Constructor ------------------ //

    // ------------- Destructor ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageDestructor o) {
        return "~this";
//        DLanguageDestructorStub stub = o.getStub();
//        if (stub != null) return StringUtil.notNullize(stub.getName());
//
//        PsiElement parent = o.getParent();
//
//
//        while (!(parent instanceof DLanguageClassDeclaration)) {
//            parent = parent.getParent();
//        }

//        return ((DLanguageClassDeclaration)parent).getName() + "constructor";
//        if (o.getIdentifier() != null) {
//            return o.getIdentifier().getText();
//        } else {
//            return "not found";
//        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageDestructor o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageDestructor o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageDestructorFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageDestructor o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageDestructor o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                String string = "";
                for (PsiElement psiElement : o.getChildren()) {
                    if(psiElement instanceof DLanguageParametersImpl)
                        string += psiElement.getText();
                }
                return o.getName() + string;
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Destructor ------------------ //

    // ------------- Alias Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageAliasDeclaration o) {
        DLanguageAliasDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());

        if (o.getIdentifier() != null) {
            return o.getIdentifier().getText();
        } else {
            return "not found";
        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageAliasDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageAliasDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageAliasDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageAliasDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageAliasDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }

    // ------------- Alias Definition ------------------ //

    // ------------ Module Declaration ----------------- //

    @NotNull
    public static String getName(@NotNull DLanguageModuleGlobalDeclaration o) {
        DLanguageModuleDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());

        if (o.getModuleFullyQualifiedName().getText() != null) {
            return o.getModuleFullyQualifiedName().getText();
        } else {
            return "not found";
        }
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageModuleGlobalDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageModuleGlobalDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageModuleFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageModuleGlobalDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageModuleGlobalDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }

    @Contract("null -> null")
    public static DLanguageProtectionAttribute getProtection(DLanguageModuleGlobalDeclaration o) {
        return getChildOfType(o, DLanguageProtectionAttribute.class);
    }



    // ------------ Module Declaration ----------------- //

    // ------------- Interface Definition ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageInterfaceDeclaration o) {
        DLanguageInterfaceDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());
        return o.getIdentifier().getText();
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageInterfaceDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageInterfaceDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageInterfaceDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageInterfaceDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageInterfaceDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Interface Definition ------------------ //

    // ------------- Var Declaration ------------------ //


    // ------------- Var Declaration ------------------ //

    // ------------- Labeled Statement ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageLabeledStatement o) {
        DLanguageLabeledStatementStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());
        return o.getIdentifier().getText();
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageLabeledStatement o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageLabeledStatement o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageLabeledStatementFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageLabeledStatement o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageLabeledStatement o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }
    // ------------- Labeled Statement ------------------ //

    // ------------- Mixin Declaration ------------------ //
    @NotNull
    public static String getName(@NotNull DLanguageTemplateMixinDeclaration o) {
        DLanguageTemplateMixinDeclarationStub stub = o.getStub();
        if (stub != null) return StringUtil.notNullize(stub.getName());
        return o.getIdentifier().getText();//doesn't have any one name??
    }

    @Nullable
    public static PsiElement getNameIdentifier(@NotNull DLanguageTemplateMixinDeclaration o) {
        ASTNode keyNode = o.getNode();
        return keyNode != null ? keyNode.getPsi() : null;
    }

    @Nullable
    public static PsiElement setName(@NotNull DLanguageTemplateMixinDeclaration o, @NotNull String newName) {
        PsiElement e = DElementFactory.createDLanguageVarDeclarationFromText(o.getProject(), newName);
        if (e == null) return null;
        o.replace(e);
        return o;
    }

    @NotNull
    public static PsiReference getReference(@NotNull DLanguageTemplateMixinDeclaration o) {
        return new DReference(o, TextRange.from(0, getName(o).length()));
    }

    @NotNull
    public static ItemPresentation getPresentation(final DLanguageTemplateMixinDeclaration o) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return o.getName();
            }

            /**
             * This is needed to decipher between files when resolving multiple references.
             */
            @Nullable
            @Override
            public String getLocationString() {
                final PsiFile psiFile = o.getContainingFile();
                return psiFile instanceof DLanguageFile ? ((DLanguageFile) psiFile).getModuleOrFileName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return DLanguageIcons.FILE;
            }
        };
    }

    // ------------- Mixin Declaration ------------------ //

    // -------------- Mixin Statement ------------------- //
    public DLanguageTemplateMixinDeclaration getTemplate() {

    }

    // -------------- Mixin Statement ------------------- //


}

