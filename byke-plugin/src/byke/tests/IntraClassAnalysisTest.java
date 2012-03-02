package byke.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Ignore;
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
		assertDepIsDependent("int foo; void dep() { int a = this.foo; }");
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
		assertDepends(a, "{initializer}");
	}

	@Test
	public void fieldDependsOnAssignmentByMethod() throws Exception {
		assertDepIsDependent("int dep; void main() { dep = 3; }");
	}

	@Test
	public void thisFieldDependsOnAssignmentByMethod() throws Exception {
		assertDepIsDependent("int dep; void main() { this.dep = 3; }");
	}

	@Test
	public void staticFieldDependsOnMethodThatAssignsIt() throws Exception {
		assertDepIsDependent("static int dep; void foo() { A.dep = 3; }");
	}

	@Ignore
	@Test
	public void fieldDeclarationDependsOnMethod() throws Exception {
		assertDepIsDependent("int a, dep=calc(); int calc() { return 3; }");
	}

	
	private void assertDepIsDependent(String body) throws CoreException, InvalidElement {
		ICompilationUnit a = createCompilationUnit("A", "class A { " + body + " }");
		assertDepends(a, "dep");
	}

}
