/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.lang.java.rule.design;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotationTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.BooleanProperty;

/**
 * Detects fields that are declared after methods, constructors, etc.
 * It was a XPath rule, but the Java version is much faster.
 * The XPath rule for reference:
 * <pre>
//ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration
[not(.//ClassOrInterfaceBodyDeclaration) or $ignoreAnonymousClassDeclarations = 'false']
[../preceding-sibling::ClassOrInterfaceBodyDeclaration
    [  count(ClassOrInterfaceDeclaration) > 0
    or count(ConstructorDeclaration) > 0
    or count(MethodDeclaration) > 0
    or count(AnnotationTypeDeclaration) > 0
    or ($ignoreEnumDeclarations = 'false' and count(EnumDeclaration) > 0)
    ]
]
 * </pre>
 */
public class FieldDeclarationsShouldBeAtStartOfClassRule extends AbstractJavaRule {

    private BooleanProperty ignoreEnumDeclarations = new BooleanProperty("ignoreEnumDeclarations", "Ignore Enum Declarations that precede fields.", true, 1.0f);
    private BooleanProperty ignoreAnonymousClassDeclarations = new BooleanProperty("ignoreAnonymousClassDeclarations", "Ignore Field Declarations, that are initialized with anonymous class declarations", true, 2.0f);

    /**
     * Initializes the rule {@link FieldDeclarationsShouldBeAtStartOfClassRule}.
     */
    public FieldDeclarationsShouldBeAtStartOfClassRule() {
        definePropertyDescriptor(ignoreEnumDeclarations);
        definePropertyDescriptor(ignoreAnonymousClassDeclarations);
    }

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        Node parent = node.jjtGetParent().jjtGetParent();
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            Node child = parent.jjtGetChild(i);
            if (child.jjtGetNumChildren() > 0) {
                if (!(child.jjtGetChild(0) instanceof ASTAnnotation) || child.jjtGetNumChildren() == 1) {
                    child = child.jjtGetChild(0);
                } else {
                    child = child.jjtGetChild(1);
                }
            }
            if (child.equals(node)) {
                break;
            }
            if (child instanceof ASTFieldDeclaration) {
                continue;
            }
            if (node.hasDescendantOfType(ASTClassOrInterfaceBodyDeclaration.class) && getProperty(ignoreAnonymousClassDeclarations).booleanValue()) {
                continue;
            }
            if (child instanceof ASTClassOrInterfaceDeclaration
                    || child instanceof ASTMethodDeclaration
                    || child instanceof ASTConstructorDeclaration
                    || child instanceof ASTAnnotationTypeDeclaration) {
                addViolation(data, node);
                break;
            }
            if (child instanceof ASTEnumDeclaration && !getProperty(ignoreEnumDeclarations).booleanValue()) {
                addViolation(data, node);
                break;
            }
        }
        return data;
    }
}
