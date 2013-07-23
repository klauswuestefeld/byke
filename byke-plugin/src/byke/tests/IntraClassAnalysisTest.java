package byke.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import byke.InvalidElement;

public class IntraClassAnalysisTest extends CodeAnalysisTest {

	@Test
	public void methodCallsMethod() throws Exception {
		assertMethodDepIsDependent("void dep() { foo(); } void foo() {}");
	}

	@Test
	public void methodCallsMethodOnAParameterizedClass() throws Exception {
		String body = "void dep() { foo(); } void foo() {}";
		ICompilationUnit a = createCompilationUnit("A", "class A<T> { " + body + " }");
		assertDepends(a, "dep()");
	}

	@Test
	public void methodWithParameterCallsMethod() throws Exception {
		assertMethodDepIsDependent("void dep() { foo(1); } void foo(int i) {}");
	}
	

	@Test
	public void externalMethodCallsAreIgnored() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { void main() { valid(); B.invalid(); } void valid(){} }");
		createCompilationUnit("B", "class B { static void invalid() {} }");
		assertDepends(a, "main()");
	}

	
	@Test
	public void methodReadsField() throws Exception {
		assertMethodDepIsDependent("int foo; void dep() { System.out.print(this.foo); }");
	}

	
	@Test
	public void methodCallsConstructor() throws Exception {
		assertMethodDepIsDependent("void dep() { new A(); }");
	}

	
	@Test
	public void constructorCallsMethod() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { A() { main(); } static void main() {} }");
		assertDepends(a, "A()");
	}

	
	@Test
	public void initializerCallsMethod() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { { main(); } static void main() {} }");
		assertDepends(a, "<init>");
	}

	
	@Test
	public void methodDependsOnField() throws Exception {
		assertMethodDepIsDependent("int foo; void dep() { foo = 3; }");
	}

	
	@Test
	public void methodDependsOnThisField() throws Exception {
		assertMethodDepIsDependent("int foo; void dep() { this.foo = 3; }");
	}

	
	@Test
	public void methodDependsOnStaticField() throws Exception {
		assertMethodDepIsDependent("static int foo; void dep() { A.foo = 3; }");
	}

	
	@Test
	public void fieldDeclarationDependsOnRightHandSide() throws Exception {
		assertFieldDepIsDependent("int dep=calc(); int 	calc() { return 3; }");
	}

	
	@Test
	public void fieldAssignmentDependsOnRightHandSide() throws Exception {
		assertFieldDepIsDependent("int dep; void foo(){dep=calc();}; int calc() { return 3; }", "calc()");
	}

	@Test
	public void methodDependsOnBothSidesOfAnAssignment() throws Exception {
		assertMethodDepIsDependent("int foo; void dep(){foo=calc();}; int calc() { return 3; }");
	}

	
	@Test
	public void localVariableProvidersAreTransitive() throws Exception {
		assertFieldDepIsDependent("int dep; void main() { int local = calc(); dep = local; } int calc() { return 3; }", "calc()");
	}

	
	@Test
	public void localVariablesDoNotAppearInGraph() throws Exception {
		assertMethodDepIsDependent("int foo; void dep() { foo = 3; int invalid = 3; }");
	}
	
	
	private void assertMethodDepIsDependent(String body, String... providers) throws CoreException, InvalidElement {
		assertIsDependent(body, "dep()", providers);
	}
	

	private void assertFieldDepIsDependent(String body, String... providers) throws CoreException, InvalidElement {
		assertIsDependent(body, "dep", providers);
	}

	
	private void assertIsDependent(String body, String dep, String... providers) throws CoreException, InvalidElement {
		ICompilationUnit a = createCompilationUnit("A", "class A { " + body + " }");
		assertDepends(a, dep, providers);
	}

}
