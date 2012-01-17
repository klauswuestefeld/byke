/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package byke.tests.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class JavaModelUtility {

	public static void collectCompilationUnits(List<ICompilationUnit> result, IPackageFragmentRoot root) throws JavaModelException {
		IJavaElement[] elements = root.getChildren();
		for (int j = 0; j < elements.length; ++j) {
			IPackageFragment p = (IPackageFragment)elements[j];
			result.addAll(Arrays.asList(p.getCompilationUnits()));
		}
	}
	
	public static List<ICompilationUnit> collectCompilationUnits(IJavaProject project) throws JavaModelException {
		
		List<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		
		IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
		for (int i = 0; i < roots.length; ++i) {
			IPackageFragmentRoot root = roots[i];
			if (IPackageFragmentRoot.K_SOURCE == root.getKind()) {
				collectCompilationUnits(result, root);
			}
		}
		
		return result;
	}
	
	
	public static List<ICompilationUnit> collectCompilationUnits(IPackageFragmentRoot root) throws JavaModelException {
		List<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		collectCompilationUnits(result, root);
		return result;
	}

}
