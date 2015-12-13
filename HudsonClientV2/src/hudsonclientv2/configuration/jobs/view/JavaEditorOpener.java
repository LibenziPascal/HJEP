package hudsonclientv2.configuration.jobs.view;

import hudsonclientv2.utils.logging.HudsonPluginLogger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

//TODO increase it
public class JavaEditorOpener {

	public void openEditor(Shell shell, String clazzName, String methodName) {
		ICompilationUnit compilationUnit = getCompilationUnit(shell, clazzName);
		IEditorPart editor = openEditor(shell, compilationUnit);
		if (editor != null) {

			try {
				IType[] types = compilationUnit.getTypes();
				for (IType type : types) {
					IMethod[] methods = type.getMethods();
					for (IMethod method : methods) {
						// System.out.println(method.getElementName());
						if (method.getElementName().equals(methodName)) {
							JavaUI.revealInEditor(editor, (IJavaElement) method);
						}

					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public void openEditor(Shell shell, String clazzName) {
		ICompilationUnit compilationUnit = getCompilationUnit(shell, clazzName);
		openEditor(shell, compilationUnit);
	}

	private IEditorPart openEditor(Shell shell, ICompilationUnit compilationUnit) {
		try {
			if (compilationUnit != null) {
				return JavaUI.openInEditor(compilationUnit);
			}
		} catch (Exception exception) {
			HudsonPluginLogger.logException(exception);
		}
		return null;
	}

	private ICompilationUnit getCompilationUnit(Shell shell, String clazzName) {
		try {
			List<ICompilationUnit> compilationUnits = getCompilationUnitsByClassName(clazzName);
			if (compilationUnits.isEmpty()) {
				// MessageDialog.openInformation(shell,
				// "Klasse nicht gefunden.", "Konnte Klasse " + clazzName +
				// " nicht finden.");
			} else if (compilationUnits.size() == 1) {
				return compilationUnits.get(0);
			} else {
				// MessageDialog.openInformation(shell, "Precision necessaire.",
				// "Plusieurs classes correspondent à " + clazzName + ".");
			}
		} catch (Exception exception) {
			HudsonPluginLogger.logException(exception);
		}
		return null;
	}

	public List<ICompilationUnit> getCompilationUnitsByClassName(String clazzName) throws Exception {
		List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();
		// Retrieve all the source roots
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (IProject project : projects) {
			System.out.println(project);
			// TODO check open here no?
			IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
			if (javaProject != null) {
				compilationUnits.addAll(getCompilationUnitsForProject(clazzName, javaProject));
			}
		}
		return compilationUnits;
	}

	private List<ICompilationUnit> getCompilationUnitsForProject(String className, IJavaProject iJavaProject) throws JavaModelException {
		List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();
		IPackageFragmentRoot[] roots = iJavaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot root : roots) {

			// Retrieve all the elements within the source root.
			for (IJavaElement iJavaElement : root.getChildren()) {

				// Test if the element is a package fragment.
				if (iJavaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					IPackageFragment iPackageFragment = (IPackageFragment) iJavaElement;

					for (ICompilationUnit iCompilationUnit : iPackageFragment.getCompilationUnits()) {

						// Recreate the package and class name to a fully
						// qualified classname.
						String cn = iCompilationUnit.getElementName().substring(0, iCompilationUnit.getElementName().indexOf('.'));

						// workarround not shure how to solve this problem yet,
						// but with Java 1.5
						// interface names could contain generics so we should
						// check for that.
						if (cn.equals(className.indexOf('<') > 0 ? className.substring(0, className.indexOf('<')) : className)) {
							compilationUnits.add(iCompilationUnit);
						}
					}
				}
			}
		}

		return compilationUnits;
	}
}