package de.engehausen.treemap.mat.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

import de.engehausen.treemap.IColorProvider;
import de.engehausen.treemap.IRectangle;
import de.engehausen.treemap.ITreeModel;
import de.engehausen.treemap.mat.ISnapshotNode;
import de.engehausen.treemap.mat.Messages;
import de.engehausen.treemap.mat.impl.ColorMatcher;

/**
 * Color provider coloring rectangles in the tree map according to
 * rules defined by a {@link ColorMatcher}.
 */
public class ColorProvider implements IColorProvider<ISnapshotNode, Color> {

	private final Device device;
	private final Map<RGB, Color> rgb2color;
	protected ISnapshot snapshot;
	protected ColorMatcher matcher;

	/**
	 * Creates the color provider for the given device. The provider can only
	 * work properly once it has been given a <code>ColorMatcher</code> instance
	 * using {@link #updateColorMatcher(ColorMatcher)} and a snapshot object via
	 * {@link #setSnapshot(ISnapshot)}.
	 * @param d the device the provider should use, must not be <code>null</code>.
	 */
	public ColorProvider(final Device d) {
		device = d;
		rgb2color = new HashMap<RGB, Color>(32);
	}
	
	/**
	 * Updates the reference to the color matcher to use by the provider.
	 * @param aMatcher the matcher to use, must not be <code>null</code>.
	 */
	public void updateColorMatcher(final ColorMatcher aMatcher) {
		matcher = aMatcher;
	}

	/**
	 * Sets the snapshot the provider works with. This can only be done
	 * once; further calls will cause an exception.
	 * @param st the snapshot to use
	 * @throws IllegalStateException if a snapshot has already been set
	 */
	public void setSnapshot(final ISnapshot st) {
		if (snapshot != null) {
			throw new IllegalStateException(Messages.STR_ALREADY_INITIALIZED);
		}
		snapshot = st;
	}

	/**
	 * Disposes resources the provider created.
	 */
	public void dispose() {
		for (Color c : rgb2color.values()) {
			c.dispose();
		}
	}

	@Override
	// non-javadoc: see interface
	public Color getColor(final ITreeModel<IRectangle<ISnapshotNode>> model, final IRectangle<ISnapshotNode> node) {
		final int id = node.getNode().getID();
		RGB rgb;
		try {
			final String name = snapshot.getObject(id).getClazz().getName();
			rgb = matcher.match(name);
		} catch (SnapshotException e) {
			rgb = matcher.getDefaultColor();
		}
		return getColor(rgb);			
	}

	/**
	 * Returns a SWT color for the given RGB value.
	 * @param rgb the color in RGB, must not be <code>null</code>.
	 * @return the color, never <code>null</code>.
	 */
	protected Color getColor(final RGB rgb) {
		Color result = rgb2color.get(rgb);
		if (result == null) {
			result = new Color(device, rgb);
			rgb2color.put(rgb, result);
		}
		return result;
	}

}
