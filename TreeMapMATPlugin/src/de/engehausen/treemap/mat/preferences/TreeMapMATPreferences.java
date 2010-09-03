package de.engehausen.treemap.mat.preferences;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.engehausen.treemap.mat.Messages;
import de.engehausen.treemap.mat.impl.Activator;
import de.engehausen.treemap.mat.impl.ColorMatcher;
import de.engehausen.treemap.mat.impl.ColorMatcher.ColorElement;

/**
 * Preferences page for TreeMapMAT. This is a bit ugly, but does more or
 * less what it is supposed to.
 */
public class TreeMapMATPreferences extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants, Listener, MouseListener {
	
	private static final String BLANK = " "; //$NON-NLS-1$
	private static final String TRUE = Boolean.toString(true);
	
	protected Table table;
	protected TableEditor editor;
	protected Button buttonAdd;
	protected Button buttonDelete;
	protected Button buttonDefaultColor;
	protected ColorBox colorBox;
	
	protected ColorMatcher matcher;

	/**
	 * Creates the preferences page.
	 */
	public TreeMapMATPreferences() {
		super();
		setDescription(Messages.STR_PREF_DESC);
	}
	
	@Override
	// non-javadoc: see superclass
	public void init(final IWorkbench workbench) {
		final IPreferenceStore p = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(p);		
		final String prefs = p.getString(TREEMAP_PREF_KEY);
		if (prefs != null && prefs.length()>0) {
			matcher = ColorMatcher.from(prefs);
		} else {
			matcher = ColorMatcher.from("a0a0a0\te20802\tjava.lang.String\tfalse"); //$NON-NLS-1$
		}
		if (matcher.getDefaultColor() == null) {
			matcher.setDefaultColor(DEFAULT_COLOR);			
		}
	}

	@Override
	// non-javadoc: see superclass
	public void dispose() {
		if (table != null) {
			table.dispose();
			table = null;
		}
		if (buttonAdd != null) {
			buttonAdd.dispose();
			buttonAdd = null;
		}
		if (buttonDelete != null) {
			buttonDelete.dispose();
			buttonDelete = null;
		}
		if (buttonDefaultColor != null) {
			buttonDefaultColor.dispose();
			buttonDefaultColor = null;
		}
		super.dispose();
	}

	/**
	 * Handles various events occurring on the preferences page, such
	 * as adding/deleting color matching rules.
	 */
	public void handleEvent(final Event event) {
		final Widget source = event.widget;
		if (source == buttonAdd) {
			final TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, BLANK);
			final Color c = new Color(source.getDisplay(), matcher.getDefaultColor());
			item.setBackground(0, c); 
			c.dispose();
			item.setText(1, Messages.STR_PATTERN_OR_SUBSTR);
			item.setText(2, Boolean.toString(true));
		} else if (source == buttonDefaultColor) {
			final ColorDialog dialog = new ColorDialog(source.getDisplay().getActiveShell(), SWT.NONE);
			dialog.setRGB(matcher.getDefaultColor());
			final RGB rgb = dialog.open();
			if (rgb != null) {
				matcher.setDefaultColor(rgb);
				colorBox.setColor(rgb);
			}
		} else if (source == buttonDelete) {
			deleteSelectedIndices();
		} else if (source == table) {
			if (event.keyCode == SWT.DEL) {
				deleteSelectedIndices();
			}
		}
	}

	/**
	 * Find the table item click with the mouse.
	 * @param event the mouse event
	 * @param info an array of at least size one to indicate the column that was clicked
	 * @return the clicked table item, or <code>null</code>.
	 */
	protected TableItem findItem(final MouseEvent event, final int[] info) {
		final Rectangle clientArea = table.getClientArea ();
		final int max = table.getItemCount();
		int index = table.getTopIndex();
		while (index < max) {
			boolean visible = false;
			final TableItem item = table.getItem(index);
			for (int i=0; i < 3; i++) {
				final Rectangle rect = item.getBounds(i);
				if (rect.contains(event.x, event.y)) {
					info[0] = i;
					return item;
				}
				if (!visible) {
					visible = rect.intersects(clientArea);
				}
			}
			if (!visible) return null;
			index++;
		}
		return null;
	}

	/**
	 * Handle the in-place editors of the color matching rules table.
	 * @param event the mouse event activating the table
	 */
	protected void handleEditor(final MouseEvent event) {
		final int idx[] = new int[1];
		final TableItem item = findItem(event, idx);		
		if (item != null) {
			if (idx[0] == 0) {
				// color field
				final ColorDialog dialog = new ColorDialog(item.getDisplay().getActiveShell(), SWT.NONE);
				dialog.setRGB(item.getBackground(0).getRGB());
				final RGB rgb = dialog.open();
				if (rgb != null) {
					final Color c = new Color(item.getDisplay(), rgb);
					item.setBackground(0, c);
					c.dispose();
				}				
			} else if (idx[0] == 1) {
				// pattern field
				resetEditor();
				final Text newEditor = new Text(table, SWT.NONE);
				
				newEditor.setText(item.getText(1));
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(final ModifyEvent me) {
						final Text text = (Text)editor.getEditor();
						editor.getItem().setText(1, text.getText());
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, 1);
			} else if (idx[0] == 2) {
				// is reg exp checkbox
				resetEditor();
				final Button newEditor = new Button(table, SWT.CHECK);
				final String text = item.getText(2);
				newEditor.setText(text);
				newEditor.setSelection(Boolean.parseBoolean(text));
				newEditor.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(final SelectionEvent e) {
						editor.getItem().setText(2, Boolean.toString(newEditor.getSelection()));
					}
				});
				newEditor.setFocus();
				editor.setEditor(newEditor, item, 2);
			} else {
				clearEditor();
			}
		} else {
			// click outside
			clearEditor();
		}
		fixButtons();
	}

	/**
	 * Set the buttons enabled/disabled according to state.
	 */
	private void fixButtons() {
		final boolean noEditor = editor.getEditor() == null;
		setValid(noEditor);
		buttonDelete.setEnabled(noEditor);
	}

	/**
	 * Remove the currently active editor.
	 */
	private void resetEditor() {
		final Control currentEditor = editor.getEditor();
		if (currentEditor != null) {
			currentEditor.dispose();				
			editor.setEditor(null);
		}
	}

	/**
	 * Remove the currently active editor and deselect any potential
	 * table selection.
	 */
	private void clearEditor() {
		resetEditor();
		table.deselectAll();
	}

	/**
	 * Delete the selected indices from the color matching rules table.
	 */
	protected void deleteSelectedIndices() {
		final int idx[] = table.getSelectionIndices();
		if (idx != null && idx.length>0) {
			table.remove(idx);
		}
	}

	@Override
	// non-javadoc: see superclass
	protected Control createContents(final Composite parent) {
        final Composite root = new Composite(parent, SWT.NULL);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        root.setLayout(layout);
        root.setFont(parent.getFont());

        // create table
		table = new Table(root, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.KeyUp, this);
		table.addMouseListener(this);
		final GridData mainData = new GridData();
		mainData.grabExcessVerticalSpace = true;
		mainData.grabExcessHorizontalSpace = true;
		mainData.verticalAlignment = GridData.FILL;
		mainData.horizontalAlignment = GridData.FILL;
		table.setLayoutData(mainData);

		final Composite panel = new Composite(root, SWT.NULL);
        final GridLayout playout = new GridLayout();
        playout.numColumns = 1;
        playout.marginHeight = 0;
        playout.marginWidth = 0;
        panel.setLayout(playout);
        final GridData panelData = new GridData();
        panelData.verticalAlignment = SWT.TOP;
        panel.setLayoutData(panelData);

        final GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;

		buttonAdd = new Button(panel, SWT.NONE);
		buttonAdd.setText(Messages.STR_ADD);
		buttonAdd.setLayoutData(buttonData);
		buttonAdd.addListener(SWT.MouseUp, this);
		buttonDelete = new Button(panel, SWT.NONE);
		buttonDelete.setText(Messages.STR_DELETE);
		buttonDelete.setLayoutData(buttonData);
		buttonDelete.addListener(SWT.MouseUp, this);
		buttonDefaultColor = new Button(panel, SWT.NONE);
		buttonDefaultColor.setText(Messages.STR_DEFAULT_COLOR);
		buttonDefaultColor.setLayoutData(buttonData);
		buttonDefaultColor.addListener(SWT.MouseUp, this);

		colorBox = new ColorBox(panel);

		// table headers
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(Messages.STR_COLOR);
		col.setWidth(48);
		col = new TableColumn(table, SWT.NONE);
		col.setText(Messages.STR_PATTERN);
		col.setWidth(256);
		col = new TableColumn(table, SWT.NONE);
		col.setText(Messages.STR_REGULAR_EXPRESSION);
		col.setWidth(56);
		
		fillTable(parent.getDisplay());

		editor = new TableEditor(table);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		
        return root;
	}

	/**
	 * Fill the color matching rules table with data.
	 * @param d the device to use for creating colors in the table, must not be <code>null</code>.
	 */
	protected void fillTable(final Device d) {
		final List<ColorElement> elements = matcher.getElements();
		final int max = elements.size();
		for (int i = 0; i < max; i++) {
			final ColorElement element = elements.get(i);
			final TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, BLANK);
			final Color col = new Color(d, element.getRGB());
			item.setBackground(0, col);
			col.dispose();
			item.setText(1, element.getPattern());
			item.setText(2, Boolean.toString(element.isRegExp()));
		}
		colorBox.setColor(matcher.getDefaultColor());
	}

	@Override
	// non-javadoc: see superclass
	public boolean performOk() {
		final boolean result;
		if (isValid()) {
			if (checkExpressions()) {
				final IPreferenceStore p = getPreferenceStore();
				p.setValue(TREEMAP_PREF_KEY, tableToString());
				result = true;				
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Verify that all expression set in the table are valid.
	 * In particular, the regular expression must compile; if one doesn't
	 * it is flagged with a red background and the changes cannot be
	 * stored.
	 * @return <code>true</code> if all expressions are valid, <code>false</code> otherwise.
	 */
	protected boolean checkExpressions() {
		final int max = table.getItemCount();
		for (int i = 0; i < max; i++) {
			final TableItem item = table.getItem(i);
			if (TRUE.equals(item.getText(2))) {
				final String regexp = item.getText(1);
				try {
					Pattern.compile(regexp);
				} catch (PatternSyntaxException e) {
					item.setBackground(1, item.getDisplay().getSystemColor(SWT.COLOR_RED));
					clearEditor();
					return false;
				}
				item.setBackground(1, item.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			}
		}
		return true;
	}
	
	@Override
	// non-javadoc: see superclass
	protected void performDefaults() {
		matcher = ColorMatcher.from(DEFAULT_COLOR_RULES);
		matcher.setDefaultColor(DEFAULT_COLOR);
		table.removeAll();
		fillTable(getControl().getDisplay());
		super.performDefaults();
	}

	/**
	 * Convert the values of the color matching rules table into
	 * a string compatible with the {@link ColorMatcher}.
	 * @return a string compatible with the {@link ColorMatcher}.
	 */
	protected String tableToString() {
		final int max = table.getItemCount();		
		final StringBuilder sb = new StringBuilder(8+max*48);
		sb.append(Integer.toHexString(rgbToInt(matcher.getDefaultColor()))).append('\t');
		for (int i = 0; i < max; i++) {
			final TableItem item = table.getItem(i);
			sb.append(Integer.toHexString(rgbToInt(item.getBackground(0).getRGB())))
			  .append('\t').append(item.getText(1))
			  .append('\t').append(item.getText(2));
			if (i < max) {
				sb.append('\t');
			}
		}
		return sb.toString();
	}

	/**
	 * Convert RGB to int.
	 * @param rgb the rgb value, must not be <code>null</code>.
	 * @return the int representation of the RGB value.
	 */
	protected int rgbToInt(final RGB rgb) {
		return rgb.red<<16|rgb.green<<8|rgb.blue;
	}

	/**
	 * Color box to show the default color.
	 */
	private static class ColorBox extends Canvas implements PaintListener {
		
		protected RGB rgb;
		
		/**
		 * Creates the box in the given parent.
		 * @param parent the parent of the box.
		 */
		public ColorBox(final Composite parent) {
			super(parent, SWT.NO_BACKGROUND);
			addPaintListener(this);
		}

		@Override
		public void paintControl(final PaintEvent e) {
			final Rectangle r = getBounds();
			if (rgb != null) {
				final Color c = new Color(getDisplay(), rgb);
				e.gc.setBackground(c);
				c.dispose();
				e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				e.gc.fillRectangle(1, 1, r.width-2, r.height-2);
				e.gc.drawRectangle(0, 0, r.width-1, r.height-1);
			}
		}

		/**
		 * Sets the color the box should show.
		 * @param anRGB the color the box should show, must not be <code>null</code>.
		 */
		public void setColor(final RGB anRGB) {
			if (anRGB != null) {
				rgb = anRGB;
				redraw();
			}
		}
		
	}

	@Override
	// non-javadoc: see interface
	public void mouseDoubleClick(final MouseEvent e) {
		handleEditor(e);
	}

	@Override
	// non-javadoc: see interface
	public void mouseDown(final MouseEvent e) {
		// ignore
	}

	@Override
	// non-javadoc: see interface
	public void mouseUp(final MouseEvent e) {
		resetEditor();
		fixButtons();
	}

}