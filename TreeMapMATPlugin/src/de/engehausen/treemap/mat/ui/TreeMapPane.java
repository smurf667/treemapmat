package de.engehausen.treemap.mat.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.registry.QueryResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.ui.QueryExecution;
import org.eclipse.mat.ui.editor.AbstractEditorPane;
import org.eclipse.mat.ui.editor.MultiPaneEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.engehausen.treemap.ILabelProvider;
import de.engehausen.treemap.IRectangle;
import de.engehausen.treemap.ISelectionChangeListener;
import de.engehausen.treemap.ITreeModel;
import de.engehausen.treemap.IWeightedTreeModel;
import de.engehausen.treemap.impl.SquarifiedLayout;
import de.engehausen.treemap.mat.ISnapshotNode;
import de.engehausen.treemap.mat.ISnapshotProvider;
import de.engehausen.treemap.mat.Messages;
import de.engehausen.treemap.mat.impl.Activator;
import de.engehausen.treemap.mat.impl.ColorMatcher;
import de.engehausen.treemap.mat.preferences.PreferenceConstants;
import de.engehausen.treemap.mat.query.IResultTreeModel;
import de.engehausen.treemap.swt.TreeMap;
import de.engehausen.treemap.swt.impl.CushionRectangleRendererEx;

/**
 * An editor pane of MAT showing the heap as a tree map. The tree map can
 * be inspected by zooming in on a sub tree (left mouse click), zooming out
 * (right mouse click). Single elements can be inspected in the "list objects"
 * view of MAT by pressing shift while left-clicking a node.
 */
public class TreeMapPane extends AbstractEditorPane implements ISelectionChangeListener<ISnapshotNode>, IPropertyChangeListener, PreferenceConstants {
	
	protected TreeMap<ISnapshotNode> treeMap;
	protected ColorProvider colors;
	protected IPreferenceStore preferences;
	protected int currentObjectID;

    @Override
    // non-javadoc: see superclass
    public void initWithArgument(final Object argument) {
        if (treeMap != null && argument instanceof QueryResult) {
        	final Object subject = ((QueryResult) argument).getSubject();
        	if (subject instanceof IResultTreeModel) {
        		final IWeightedTreeModel<ISnapshotNode> model = ((IResultTreeModel) subject).getTreeModel();
            	if (model instanceof ISnapshotProvider) {
            		colors.setSnapshot(((ISnapshotProvider) model).getSnapshot());
            	}
        		treeMap.setTreeModel(model);
        		if (model instanceof ILabelProvider<?>) {
            		treeMap.setLabelProvider((ILabelProvider<ISnapshotNode>) model);
        		} else {
        			treeMap.setLabelProvider(null);
        		}
        		treeMap.redraw();
        	}
        }
    }

    /**
     * List the object with the given ID in a separate view.
     * @param id the node to show
     */
    protected void listObject(final int id) {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart part = page == null ? null : page.getActiveEditor();
        if (part instanceof MultiPaneEditor) {
        	final Object o = treeMap.getTreeModel();
        	if (o instanceof ISnapshotProvider) {
        		final ISnapshot snapshot = ((ISnapshotProvider) o).getSnapshot();
        		try {
					QueryExecution.executeCommandLine((MultiPaneEditor) part, null, "list_objects 0x"+Long.toHexString(snapshot.getObject(id).getObjectAddress())); //$NON-NLS-1$
				} catch (SnapshotException e) {
					e.printStackTrace();
				}
        	}
        }
    	
    }
    
	@Override
    // non-javadoc: see superclass
	public void createPartControl(final Composite parent) {
		treeMap = new TreeMap<ISnapshotNode>(parent, true) {
			public void mouseUp(final MouseEvent mouseevent) {
				if (mouseevent.button == 1 && (mouseevent.stateMask & SWT.SHIFT) != 0) {
					listObject(currentObjectID);
				} else {
					super.mouseUp(mouseevent);
				}
			}
		};
		treeMap.setTreeMapLayout(new SquarifiedLayout<ISnapshotNode>(8));		
		treeMap.setRectangleRenderer(new CushionRectangleRendererEx<ISnapshotNode>(160));
		treeMap.addSelectionChangeListener(this);
		preferences = Activator.getDefault().getPreferenceStore();
		preferences.addPropertyChangeListener(this);
		colors = new ColorProvider(parent.getDisplay());
		colors.updateColorMatcher(ColorMatcher.from(preferences.getString(TREEMAP_PREF_KEY)));
		treeMap.setColorProvider(colors);
	}

	@Override
    // non-javadoc: see interface
	public void propertyChange(final PropertyChangeEvent event) {		
		if (TREEMAP_PREF_KEY.equals(event.getProperty())) {
			// the color matching preferences changed; reload and redraw.
			colors.updateColorMatcher(ColorMatcher.from(event.getNewValue().toString()));
			treeMap.redraw();
		}
	}

	@Override
    // non-javadoc: see superclass
	public void dispose() {
		try {
			if (treeMap != null) {
				treeMap.removeSelectionChangeListener(this);
				treeMap = null;
			}
			if (colors != null) {
				colors.dispose();
				colors = null;
			}
			preferences.removePropertyChangeListener(this);			
		} finally {
			super.dispose();			
		}
	}


	@Override
	// non-javadoc: see interface
	public String getTitle() {
		return Messages.STR_PANE_TITLE;
	}

	@Override
	// non-javadoc: see interface
	public void selectionChanged(final ITreeModel<IRectangle<ISnapshotNode>> model, final IRectangle<ISnapshotNode> rect, final String text) {
		if (text != null) {
			treeMap.setToolTipText(text);
		}
		currentObjectID = rect.getNode().getID();
	}

}
