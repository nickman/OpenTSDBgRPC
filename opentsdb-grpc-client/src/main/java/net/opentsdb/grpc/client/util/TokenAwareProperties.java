// This file is part of OpenTSDB.
// Copyright (C) 2010-2012  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.grpc.client.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: TokenAwareProperties</p>
 * <p>Description: {@link Properties} extension that replaces tokens with resolved values</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.util.TokenAwareProperties</code></p>
 */

public class TokenAwareProperties extends Properties {

	/**  */
	private static final long serialVersionUID = 3007418806375074091L;

	/** The prefix for a token */
	public static final String S_PREF = "${";
	/** The regex pattern to match a token */
	public static final Pattern P_PREF = Pattern.compile("\\$\\{(.*?)(?::(.*?))?\\}");

	/**
	 * Creates a new TokenAwareProperties
	 */
	public TokenAwareProperties() {
		super();
	}

	/**
	 * Creates a new TokenAwareProperties
	 * @param p The properties to initialize with
	 */
	public TokenAwareProperties(final Properties p) {
		super();
		this.putAll(p);
	}


	@Override
	public String getProperty(final String key, final String defaultValue) {
		final String v =  super.getProperty(key, defaultValue);
		return token(v);
	}

	public static String token(final String v) {
		return token(v, true);
	}

	/**
	 * Replaces <b><code>${}</code></b> tokens in the passed string, replacing them with the named
	 * system property, otherwise environmental variable with <b><code>.</code></b> (dots) replaced
	 * with <b><code>_</code></b> (underscores) and uppercased. Otherwise, the embedded default is used if present.
	 * @param v The string to detokenize
	 * @param blankOnUnResolved If true, unresolved tokens are replaced with a blank string
	 * @return the detokenized string
	 */
	public static String token(final String v, final boolean blankOnUnResolved) {
		if(v==null) return v;
		if(v.indexOf(S_PREF)!=-1) {
			final StringBuffer b = new StringBuffer();
			final Matcher m = P_PREF.matcher(v.trim());
			while(m.find()) {
				final String token = m.group(1).trim();
				String def = m.group(2);
				if(def!=null) def = def.trim();
				final String tokenValue = getSystemThenEnvProperty(token, def);
				if(tokenValue!=null) {
					m.appendReplacement(b, tokenValue);
				} else if(blankOnUnResolved) {
					m.appendReplacement(b, "");
				}
			}
			m.appendTail(b);
			return b.toString();
		} 
		return v;		
	}


	public static String getSystemThenEnvProperty(String token, String def) {
		String s = System.getProperty(token);
		if(s==null) {
			s = System.getenv(token.replace('.', '_').toUpperCase());			
		}
		if(s==null) {
			s = def;
		}
		return s;
	}

	public synchronized String toString() {
		int max = size() - 1;
		if (max == -1)
			return "{}";

		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<Object,Object>> it = entrySet().iterator();

		sb.append('{');
		for (int i = 0; ; i++) {
			Map.Entry<Object,Object> e = it.next();
			Object key = e.getKey();
			Object value = e.getValue();
			sb.append(key   == this ? "(this Map)" : key.toString());
			sb.append('=');
			sb.append(value == this ? "(this Map)" : token(value.toString()));

			if (i == max)
				return sb.append('}').toString();
			sb.append(", ");
		}
	}



}
