package byke.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Ignore;
import org.junit.Test;

import byke.InvalidElement;

public class InterClassAnalysisTest extends CodeAnalysisTest {

	@Ignore
	@Test
	public void genericConstraint1() throws Exception {
		assertDepends(
			"class A <T extends B> {}",
			"class B {}"
		);
	}

	@Ignore
	@Test
	public void genericConstraint2() throws Exception {
		assertDepends(
			"abstract class A implements Iterable<B> {}",
			"class B {}"
		);
	}

	@Test
	public void staticField() throws Exception {
		assertDepends(
			"class A { int foo = B.foo; }",
			"class B { static int foo = 42; }"
		);
	}

	@Test
	public void constructor() throws Exception {
		assertDepends(
			"class A { Object foo = new B(); }",
			"class B {}"
		);
	}

	@Test
	public void instanceField() throws Exception {
		assertDepends(
			"class A { Object foo = B.newC().field; }",
			"class B { static C newC() {return null;} } class C { int field; }"
		);
	}

	@Test
	public void staticFieldImport() throws Exception {
		assertDepends(
			"import static foopackage.B.foo; class A { int f = foo; }",
			"class B { static int foo = 42; }"
		);
	}

	@Test
	public void methodInvocation() throws Exception {
		assertDepends(
			"class A { { B.foo(); } }",
			"class B { static void foo() {} }"
		);
	}

	@Test
	public void declaredException() throws Exception {
		assertDepends(
			"class A { void foo() throws B {} }",
			"class B extends Exception {  }"
		);
	}

	private void assertDepends(String dependent, String provider) throws CoreException, JavaModelException, InvalidElement {
		ICompilationUnit unit = createCompilationUnit("A", dependent);
		createCompilationUnit("B", provider);
	
		assertDepends(unit.getParent(), "A");
	}

	
	
}
