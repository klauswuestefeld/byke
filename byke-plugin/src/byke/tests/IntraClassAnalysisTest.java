package byke.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import byke.InvalidElement;

public class IntraClassAnalysisTest extends CodeAnalysisTest {

	@Test
	public void methodCallsMethod() throws Exception {
		assertDepIsDependent("void dep() { foo(); } void foo() {}");
	}
	

	@Test
	public void externalMethodCallsAreIgnored() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { void main() { valid(); B.invalid(); } void valid(){} }");
		createCompilationUnit("B", "class B { static void invalid() {} }");
		assertDepends(a, "main");
	}

	
	@Test
	public void methodReadsField() throws Exception {
		assertDepIsDependent("int foo; void dep() { System.out.print(this.foo); }");
	}

	
	@Test
	public void methodCallsConstructor() throws Exception {
		assertDepIsDependent("void dep() { new A(); }");
	}

	
	@Test
	public void constructorCallsMethod() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { A() { main(); } static void main() {} }");
		assertDepends(a, "A");
	}

	
	@Test
	public void initializerCallsMethod() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { { main(); } static void main() {} }");
		assertDepends(a, "<init>");
	}

	
	@Test
	public void fieldDependsOnMethodThatAssignsIt() throws Exception {
		assertDepIsDependent("int dep; void main() { dep = 3; }");
	}

	
	@Test
	public void thisFieldDependsOnMethodThatAssignsIt() throws Exception {
		assertDepIsDependent("int dep; void foo() { this.dep = 3; }");
	}

	
	@Test
	public void staticFieldDependsOnMethodThatAssignsIt() throws Exception {
		assertDepIsDependent("static int dep; void foo() { A.dep = 3; }");
	}

	
	@Test
	public void fieldDeclarationDependsOnRightHandSide() throws Exception {
		assertDepIsDependent("int dep=calc(); int calc() { return 3; }");
	}

	
	@Test
	public void fieldAssignmentDependsOnRightHandSide() throws Exception {
		assertDepIsDependent("int dep; void foo(){dep=calc();}; int calc() { return 3; }");
	}

	
	@Test
	public void localVariablesDoNotAppearInGraph() throws Exception {
		assertDepIsDependent("int dep; void main() { dep = 3; int invalid = 3; }");
	}

	
	@Test
	public void localVariableProvidersAreTransitive() throws Exception {
		assertDepIsDependent("int dep; void main() { int local = calc(); dep = local; } int calc() { return 3; }");
	}

	
	
	private void assertDepIsDependent(String body) throws CoreException, InvalidElement {
		ICompilationUnit a = createCompilationUnit("A", "class A { " + body + " }");
		assertDepends(a, "dep");
	}

}
