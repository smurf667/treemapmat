package de.engehausen.treemap.mat.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.graphics.RGB;

import de.engehausen.treemap.mat.preferences.PreferenceConstants;

/**
 * Color matcher used to match string to <code>RGB</code> (color)
 * instances. This class is used for the preferences and the actual
 * run time of a tree map pane at the same time.
 * <br>It is somewhat string based, since all rules and colors need to
 * be stored in a single string in the preferences.
 */
public class ColorMatcher {

	/** list of color elements (expression->rgb "mappings") */
	protected List<ColorElement> elements;
	/** default color */
	protected RGB defaultColor;

	/**
	 * Creates the matcher with the preferences' default color.
	 */
	protected ColorMatcher() {
		this(PreferenceConstants.DEFAULT_COLOR);
	}
	
	/**
	 * Creates the matcher using the given color (a triplet of
	 * red-green-blue, 8 bits each).
	 * @param colorRGB the default color
	 */
	protected ColorMatcher(final int colorRGB) {
		this(new RGB(colorRGB>>16&0xff,
	                 colorRGB>> 8&0xff,
	                 colorRGB    &0xff));		
	}

	/**
	 * Creates the matcher using the given default color.
	 * @param colorRGB the default color
	 */
	protected ColorMatcher(final RGB color) {
		elements = new ArrayList<ColorElement>(20);
		defaultColor = color;
	}

	/**
	 * Returns the default color.
	 * @return the default color.
	 */
	public RGB getDefaultColor() {
		return defaultColor;
	}

	/**
	 * Sets the default color.
	 * @param color the default color.
	 */
	public void setDefaultColor(final RGB color) {
		defaultColor = color;
	}

	/**
	 * Sets the default color, represented as a hex string
	 * (triplet of 8-bit red-green-blue values).
	 * @param color the color string, must not be <code>null</code>.
	 */
	public void setDefaultColor(final String color) {
		final int c = Integer.parseInt(color, 16);
		setDefaultColor(new RGB(c>>16&0xff,c>>8&0xff,c&0xff));
	}

	/**
	 * Returns all color elements the matcher holds.
	 * @return the color elements of the matcher, never <code>null</code>.
	 */
	public List<ColorElement> getElements() {
		return elements;
	}

	/**
	 * Creates a color matcher from its string representation. The
	 * format of the string is:
	 * <ol>
	 * <li><i>default-color</i>
	 * <li><i>color</i> - <i>pattern</i> - <i>isRegExp</i>
	 * <li>...
	 * </ol>
	 * The separator used is the tab character (\t).
	 * @param str the string representation of the color matcher.
	 * @return a color matcher, never <code>null</code>.
	 */
	public static ColorMatcher from(final String str) {
		final ColorMatcher result = new ColorMatcher();
		final StringTokenizer tok = new StringTokenizer(str, "\t"); //$NON-NLS-1$
		result.setDefaultColor(tok.nextToken());
		while (tok.hasMoreTokens()) {
			try {
				result.elements.add(new ColorElement(Integer.parseInt(tok.nextToken(), 16), tok.nextToken(), tok.nextToken()));
			} catch (NumberFormatException e) {
				break;
			}
		}
		return result;
	}
	
	/**
	 * Converts a given color matcher into the format document in
	 * its counter-method {@link #from(String)}.
	 * @param matcher the matcher to turn into a string
	 * @return the string description of the matcher, never <code>null</code>.
	 */
	public static String to(final ColorMatcher matcher) {
		final int max = matcher.elements.size();
		final StringBuilder sb = new StringBuilder(8+max*48);
		for (int i = 0; i < max; i++) {
			final ColorElement e = matcher.elements.get(i);
			sb.append(Integer.toHexString(e.getRGBInt())).append('\t')
			  .append(e.pattern).append('\t').append(Boolean.toString(e.regexp));
			if (i < max) {
				sb.append('\t');
			}
		}
		return sb.toString();
	}

	/**
	 * Matches the given string against the first matching {@link ColorElement}
	 * in the list of color elements. If none match, the default color is returned.
	 * @param string the string to match; must not be <code>null</code>.
	 * @return the matching color, never <code>null</code>.
	 */
	public RGB match(final String string) {
		// if this turns out to be too expensive, a cache
		// might be introduced (string->rgb), since there
		// should not be too many classes 
		final int max = elements.size();
		for (int i = 0; i < max; i++) {
			final ColorElement e = elements.get(i);
			if (e.match(string)) {
				return e.getRGB();
			}
		}
		return defaultColor;
	}

	/**
	 * Color element describing a pattern/sub-string and color.
	 */
	public static class ColorElement {
		
		protected RGB rgb;
		protected String pattern;
		protected boolean regexp;
		private Matcher matcher;

		/**
		 * Creates the color element
		 * @param color the color (red-green-blue triplet of 8 bits each)
		 * @param p the pattern, must not be <code>null</code>
		 * @param isRegExp string representation of a boolean value; if "false",
		 * the the pattern is just a sub-string used in the matching.
		 */
		protected ColorElement(final int color, final String p, final String isRegExp) {
			rgb = new RGB(color>>16&0xff,
			              color>> 8&0xff,
			              color    &0xff);
			pattern = p;
			regexp = Boolean.parseBoolean(isRegExp);
		}

		/**
		 * Returns the matching color of the element.
		 * @return the matching color of the element, never <code>null</code>.
		 */
		public RGB getRGB() {
			return rgb;
		}

		/**
		 * Returns the matching color of the element.
		 * @return the matching color of the element as an int triplet of red-green-blue.
		 */
		protected int getRGBInt() {
			return rgb.red<<16|rgb.green<<8|rgb.blue;
		}

		/**
		 * Returns the pattern (sub-string) of the element.
		 * @return the pattern (sub-string) of the element.
		 */
		public String getPattern() {
			return pattern;
		}
		
		/**
		 * Indicates whether the pattern held by the element is used
		 * as a regular expression or as a simple sub-string.
		 * @return <code>true</code> if the pattern is used as a regular
		 * expression, <code>false</code> otherwise.
		 */
		public boolean isRegExp() {
			return regexp;
		}

		/**
		 * Indicates whether the given string matchers the pattern of the
		 * color element. If {@link #isRegExp()} returns <code>false</code>,
		 * the match is a simple sub-string check against the "pattern".
		 * @param str the string to match, must not be <code>null</code>.
		 * @return <code>true</code> if the string matches, <code>false</code>
		 * otherwise.
		 */
		public boolean match(final String str) {
			if (regexp) {
				if (matcher == null) {
					try {
						matcher = Pattern.compile(pattern).matcher(str);						
					} catch (PatternSyntaxException e) {
						// cannot compile pattern, don't try again,
						// just treat as substring match (which probably won't work...)
						regexp = false;
						return false;
					}
				} else {
					matcher.reset(str);
				}
				return matcher.matches();
			} else {
				return str.indexOf(pattern) >= 0;
			}
		}
	}

}
