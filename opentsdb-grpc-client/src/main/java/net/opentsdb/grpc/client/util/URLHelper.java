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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <p>Title: URLHelper</p>
 * <p>Description: Generic helper classes for working with URLs and related IO</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.util.URLHelper</code></p>
 */

public class URLHelper {

	/** Text line separator */
	public static final String EOL = System.getProperty("line.separator", "\n");	
	/** End of line splitter */
	public static final Pattern EOLP = Pattern.compile("$");
	/** pattern match for a shebang */
	public static final Pattern SHEBANG = Pattern.compile("#!/.*$");
	
	/** The system property to retrieve the default client connect timeout in ms.  */
	public static final String DEFAULT_CONNECT_TO = "sun.net.client.defaultConnectTimeout";
	/** The system property to retrieve the default client read timeout in ms.  */
	public static final String DEFAULT_READ_TO = "sun.net.client.defaultReadTimeout";
	/** A UTF8 charset */
	public static final Charset UTF8 = Charset.forName("UTF8");	
	
	
	/**
	 * Returns the default URL connect timeout in ms,
	 * @return the connect timeout in ms,
	 */
	protected static int defaultConnectTimeout() {
		return Integer.parseInt(System.getProperty(DEFAULT_CONNECT_TO, "0"));
	}
	
	/**
	 * Returns the default URL read timeout in ms,
	 * @return the read timeout in ms,
	 */
	protected static int defaultReadTimeout() {
		return Integer.parseInt(System.getProperty(DEFAULT_READ_TO, "0"));
	}
	
	
	/**
	 * Tests the passed object for nullness. Throws an {@link IllegalArgumentException} if the object is null 
	 * @param t  The object to test
	 * @param message The message to associate with the exception, Ignored if null
	 * @return the object passed in if not null
	 */
	public static <T> T nvl(T t, String message) {
		if(t==null) throw new IllegalArgumentException(message!=null ? message : "Null parameter");
		return t;
	}
	
	/**
	 * Reads the content of a URL as text using the default connect and read timeouts.
	 * @param url The url to get the text from
	 * @return a string representing the text read from the passed URL
	 */
	public static String getTextFromURL(URL url) {
		return getTextFromURL(url, defaultConnectTimeout(), defaultReadTimeout());
	}
	
	/**
	 * Reads all the text from the passed URL and returns it, minus any content in the text that matches the passed pattern
	 * @param url The URL to read from
	 * @param skip The optional pattern of the content to skip applied to each line of text (nothing skipped if null)
	 * @return the read and possibly filtered text
	 */
	public static String getTextFromURL(final URL url, final Pattern skip) {
		final String text = getTextFromURL(url);		
		if(skip==null) return text;
		final String[] lines = EOLP.split(text);
		final StringBuilder b = new StringBuilder(text.length());
		for(String line: lines) {
			if(!skip.matcher(line).matches()) {
				b.append(line).append(EOL);
			}
		}
		return b.toString();		
	}
	
	/**
	 * Reads all the text from the passed file and returns it, minus any content in the text that matches the passed pattern
	 * @param file The file to read from
	 * @param skip The optional pattern of the content to skip (nothing skipped if null)
	 * @return the read and possibly filtered text
	 */
	public static String getTextFromFile(final File file, final Pattern skip) {
		return getTextFromURL(toURL(file), skip);
	}
	
	/**
	 * Reads the content of a file as text using the default connect and read timeouts.
	 * @param file The file to get the text from
	 * @return a string representing the text read from the passed file
	 */
	public static String getTextFromFile(final File file) {
		return getTextFromURL(toURL(file));
	}

	
	/**
	 * Reads the content of a URL as text using the default connect and read timeouts.
	 * @param urlStr The url stringy to get the text from
	 * @return a string representing the text read from the passed URL
	 */
	public static String getTextFromURL(final CharSequence urlStr) {
		return getTextFromURL(toURL(urlStr), defaultConnectTimeout(), defaultReadTimeout());
	}
	
	/**
	 * Reads the content of a URL as a char array using the default connect and read timeouts.
	 * @param url The url to get the text from
	 * @return a char array representing the text read from the passed URL
	 */
	public static char[] getCharsFromURL(URL url) {
		return getTextFromURL(url, defaultConnectTimeout(), defaultReadTimeout()).toCharArray();
	}
	
	/**
	 * Reads the content of a URL as a char array using the default connect and read timeouts.
	 * @param urlStr The url stringy to get the text from
	 * @return a char array representing the text read from the passed URL
	 */
	public static char[] getCharsFromURL(final CharSequence urlStr) {
		return getCharsFromURL(toURL(urlStr));
	}
	
	
	/**
	 * Reads the content of a URL as text
	 * @param url The url to get the text from
	 * @param timeout The connect and read timeout in ms.
	 * @return a string representing the text read from the passed URL
	 */
	public static String getTextFromURL(URL url, int timeout) {
		return getTextFromURL(url, timeout, defaultReadTimeout());
	}
	
	/**
	 * Reads the content of a URL as a StringBuilder
	 * @param url The url to get the chars from
	 * @param cTimeout The connect timeout in ms.
	 * @param rTimeout The read timeout in ms.
	 * @return a StringBuilder representing the characters read from the passed URL
	 */
	public static StringBuilder getStrBuffFromURL(final URL url, final int cTimeout, final int rTimeout) {
		StringBuilder b = new StringBuilder();
		InputStreamReader isr = null;
		InputStream is = null;
		URLConnection connection = null;
		try {
			connection = url.openConnection();
			connection.setConnectTimeout(cTimeout);
			connection.setReadTimeout(rTimeout);
			connection.connect();
			is = connection.getInputStream();
			isr = new InputStreamReader(is, UTF8);
			final char[] cbuff = new char[128];
			int charsRead = -1;
			while((charsRead = isr.read(cbuff))!=-1) {
				b.append(cbuff, 0, charsRead);
			}
			return b;			
		} catch (Exception e) {
			throw new RuntimeException("Failed to read source of [" + url + "]", e);
		} finally {
			if(isr!=null) try { isr.close(); } catch (Exception e) {/* No Op */}
			if(is!=null) try { is.close(); } catch (Exception e) {/* No Op */}
		}		
	}
	
	/**
	 * Reads the content of a URL as a StringBuilder using the default read timeout
	 * @param url The url to get the chars from
	 * @param cTimeout The connect timeout in ms.
	 * @return a StringBuilder representing the characters read from the passed URL
	 */
	public static StringBuilder getStrBuffFromURL(final URL url, final int cTimeout) {
		return getStrBuffFromURL(url, cTimeout, defaultReadTimeout());
	}
	
	/**
	 * Reads the content of a URL as a StringBuilder using the default connect and read timeout
	 * @param url The url to get the chars from
	 * @return a StringBuilder representing the characters read from the passed URL
	 */
	public static StringBuilder getStrBuffFromURL(final URL url) {
		return getStrBuffFromURL(url, defaultConnectTimeout(), defaultReadTimeout());
	}
	
	/**
	 * Reads the content of a URL as text
	 * @param url The url to get the text from
	 * @param cTimeout The connect timeout in ms.
	 * @param rTimeout The read timeout in ms.
	 * @return a string representing the text read from the passed URL
	 */
	public static String getTextFromURL(URL url, int cTimeout, int rTimeout) {
		return getStrBuffFromURL(url, cTimeout, rTimeout).toString();
	}
	
	/**
	 * Returns the URL for the passed file
	 * @param file the file to get the URL for
	 * @return a URL for the passed file
	 */
	public static URL toURL(File file) {
		try {
			return nvl(file, "Passed file was null").toURI().toURL();
		} catch (Exception e) {
			throw new RuntimeException("Failed to get URL for file [" + file + "]", e);
		}
	}
	
	/**
	 * Determines if the passed stringy represents an existing file name
	 * @param urlStr The stringy to test
	 * @return true if the passed stringy represents an existing file name, false otherwise
	 */
	public static boolean isFile(final CharSequence urlStr) {
		if(urlStr==null || urlStr.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed URL stringy was null or empty");
		return new File(token(urlStr)).exists();
	}
	
	/**
	 * Determines if the passed stringy represents an existing file name that is a file, not a directory
	 * @param urlStr The stringy to test
	 * @return true if the passed stringy represents an existing file name, false otherwise
	 */
	public static boolean isFileFile(final CharSequence urlStr) {
		if(urlStr==null || urlStr.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed URL stringy was null or empty");
		final File f = new File(token(urlStr));
		return f.exists() && f.isFile();
	}
	
	/**
	 * Determines if the passed stringy represents an existing file name that is a directory, not a file
	 * @param urlStr The stringy to test
	 * @return true if the passed stringy represents an existing directory name, false otherwise
	 */
	public static boolean isDirectory(final CharSequence urlStr) {
		if(urlStr==null || urlStr.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed URL stringy was null or empty");
		final File f = new File(token(urlStr));
		return f.exists() && f.isDirectory();
	}
	
	public static String token(final CharSequence name) {
		if(name==null || name.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed name was null or empty");
		String s = name.toString().trim();
		if(s.startsWith("~")) {
			s = System.getProperty("user.home") + s.substring(1);
		}		
		return TokenAwareProperties.token(s);
	}
	
	/**
	 * Determines if the passed stringy looks like a URL by checking for URL like symbols like <b><code>:/</code></b>
	 * @param urlStr The stringy to test
	 * @return true if it looks like, false otherwise
	 */
	public static boolean looksLikeUrl(final CharSequence urlStr) {
		if(urlStr==null || urlStr.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed URL stringy was null or empty");
		return urlStr.toString().indexOf(":/") != -1;
	}
	
	/**
	 * Creates a URL from the passed string 
	 * @param urlstr A char sequence containing a URL representation
	 * @return a URL
	 */
	public static URL toURL(final CharSequence urlstr) {
		if(urlstr==null || urlstr.toString().trim().isEmpty()) throw new IllegalArgumentException("The passed URL stringy was null or empty");
		final String urlStr = urlstr.toString().trim();
		try {
			if(isFile(urlStr)) {
//				System.out.println("URL from File (" + urlStr + "): [" + new File(urlStr.toString()).getAbsoluteFile() + "]");
				return toURL(new File(token(urlStr.toString())).getAbsoluteFile());
			}
			if(isValidURL(urlStr)) {
				return new URL(urlStr.toString().trim());
			}
			final URL url = Thread.currentThread().getContextClassLoader().getResource(urlStr.toString().trim());
			if(url!=null) return url;
//			System.err.println("NOT A File (" + urlStr + "): [" + new File(urlStr.toString()).getAbsoluteFile() + "]");
			throw new Exception("No conversion was successful");
		} catch (Exception e) {
			URL url = Thread.currentThread().getContextClassLoader().getResource(urlStr);
			if(url!=null) return url;
			throw new RuntimeException("Failed to create URL from string [" + urlStr + "]", e);
		}
	}
	
	
	public static Properties fromURL(String resource) {
		URL url = null;
		try {
			url = toURL(Objects.requireNonNull(resource, "The passed resource name was null"));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to create URL from resource:" + resource, ex);
		}
		try (InputStream is = url.openStream()) {
			Properties p = new Properties();
			p.load(is);
			return p;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to read properties from URL:" + url, ex);
		}
	}
	
	/**
	 * Creates a URI from the passed string 
	 * @param uriStr A char sequence containing a URI representation
	 * @return a URI
	 */
	public static URI toURI(CharSequence uriStr) {
		try {
			return new URI(nvl(uriStr, "Passed string was null").toString());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create URL from string [" + uriStr + "]", e);
		}
	}
	
	
	
	
	
	/**
	 * Reads the content of a URL as a byte array
	 * @param url The url to get the bytes from
	 * @return a byte array representing the text read from the passed URL
	 */
	public static byte[] getBytesFromURL(URL url) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		InputStream is = null;
		try {
			is = url.openStream();
			int bytesRead = 0;
			byte[] buffer = new byte[8092]; 
			while((bytesRead=is.read(buffer))!=-1) {
				baos.write(buffer, 0, bytesRead);
			}
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read source of [" + url + "]", e);
		} finally {
			if(is!=null) try { is.close(); } catch (Exception e) {/* No Op */}
		}
	}
	
	/**
	 * Returns the last modified time stamp for the passed URL
	 * @param url The URL to get the timestamp for
	 * @return the last modified time stamp for the passed URL
	 */
	public static long getLastModified(URL url) {
		URLConnection conn = null;
		try {
			conn = nvl(url, "Passed URL was null").openConnection();
			return conn.getLastModified();
		} catch (Exception e) {
			throw new RuntimeException("Failed to get LastModified for [" + url + "]", e);
		}
	}
	
	
	/**
	 * Determines if the passed string is a valid URL
	 * @param urlStr The URL string to test
	 * @return true if is valid, false if invalid or null
	 */
	public static boolean isValidURL(final CharSequence urlStr) {
		if(urlStr==null) return false;
		try {
			if(isFile(urlStr)) return true;			
			return urlOrNull(urlStr) != null;			
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Creates a URL from the passed stringy, or null if the stringy was null or an invalid URL representation
	 * @param urlStr The stringy to build the URL from
	 * @return a URL or null
	 */
	public static URL urlOrNull(final CharSequence urlStr) {
		try {
			return new URL(urlStr.toString().trim());
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Determines if the passed URL resolves
	 * @param url The URL to test
	 * @return true if is resolves, false otherwise
	 */
	public static boolean resolves(final URL url) {
		if(url==null) return false;
		InputStream is = null;
		try {
			is = url.openStream();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if(is!=null) try { is.close(); } catch (Exception e) {/* No Op */}
		}
	}
	
	/**
	 * Loads and returns a {@link Properties} from the passed URL
	 * @param url The URL to read from
	 * @return the read properties which will be empty if no properties were read, or
	 * any error occurred while reading.
	 */
	public static Properties readProperties(final URL url) {
		Properties p = new Properties();
		InputStream is = null;
		try {
			is = url.openStream();
			if("XML".equalsIgnoreCase(getExtension(url))) {
				p.loadFromXML(is);
			} else {
				p.load(is);
			}
		} catch (Exception e) {/* No Op */
		} finally {
			if(is!=null) try { is.close(); } catch (Exception e) {/* No Op */}
		}		
		return p;
	}
	
	/**
	 * Determines if this URL represents a writable resource.
	 * For now, only <b><code>file:</code></b> protocol will return true 
	 * (if the file exists and is writable). 
	 * @param url The URL to test for writability
	 * @return true if this URL represents a writable resource, false otherwise.
	 */
	public static boolean isWritable(CharSequence url) {
		return isWritable(toURL(url));
	}
	
	/**
	 * Determines if this URL represents a writable resource.
	 * For now, only <b><code>file:</code></b> protocol will return true 
	 * (if the file exists and is writable). 
	 * @param url The URL to test for writability
	 * @return true if this URL represents a writable resource, false otherwise.
	 */
	public static boolean isWritable(final URL url) {
		if(url==null) return false;
		if("file".equals(url.getProtocol())) {
			File file = new File(url.getFile());
			return file.exists() && file.isFile() && file.canWrite();
		}
		OutputStream os = null;
		URLConnection urlConn = null;
		try {
			urlConn = url.openConnection();
			os = urlConn.getOutputStream();
			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			if(os!=null) try { os.close(); } catch (Exception x) {/* No Op */}			
		}		
	}
	
	
	/**
	 * Writes the passed byte content to the URL origin.
	 * @param url The URL to write to
	 * @param content The content to write
	 * @param append true to append, false to replace
	 */
	public static void writeToURL(final URL url, final byte[] content, final boolean append) {
		if(!isWritable(url)) throw new RuntimeException("The url [" + url + "] is not writable", new Throwable());
		if(content==null) throw new RuntimeException("The passed content was null", new Throwable());
		File file = new File(url.getFile());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, append);
			fos.write(content);
			fos.flush();
			fos.close();
			fos = null;			
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to write to the url [" + url + "].", ioe);
		} finally {
			if(fos!=null) {
				if(fos!=null) { 
					try { fos.flush(); } catch (Exception ex) {/* No Op */}
					try { fos.close(); } catch (Exception ex) {/* No Op */}
				}
			}
		}
	}
	
	/**
	 * Writes the passed byte content to the URL origin.
	 * @param url The URL to write to
	 * @param content The content to write
	 * @param append true to append, false to replace
	 */
	public static void writeToURL(final URL url, final CharSequence content, final boolean append) {
		if(content==null) throw new RuntimeException("The passed content was null or empty");
		writeToURL(url, content.toString().getBytes(UTF8), append);
	}

	/**
	 * Writes the content read from the passed source URL to the specified file.
	 * @param source The URL to read the content from
	 * @param file The file to write to
	 * @param append true to append, false to replace
	 */
	public static void writeToFile(final URL source, final File file, final boolean append) {
		if(source==null) throw new IllegalArgumentException("The passed URL was null");
		if(file==null) throw new IllegalArgumentException("The passed file was null");
		if(!append) try { file.delete(); } catch (Exception x) {/* No Op */}
		if(!file.exists()) {
			try {
				if(!file.createNewFile()) throw new Exception();
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to create the file [" + file + "]");
			}
		}
		if(!file.canWrite()) {
			throw new IllegalArgumentException("Cannot write to the file [" + file + "]");
		}
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		final byte[] transfer = new byte[8192];
		int bytesRead = -1;
		try {
			is = source.openStream();
			bis = new BufferedInputStream(is, 8192);
			fos = new FileOutputStream(file, append);
			bos = new BufferedOutputStream(fos, 8192);
			while((bytesRead = bis.read(transfer))!=-1) {
				bos.write(transfer, 0, bytesRead);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Transfer from [" + source + "] to [" + file + "] failed", ex);
		} finally {
			if(bis!=null) try { bis.close(); } catch (Exception x) {/* No Op */}
			if(is!=null) try { is.close(); } catch (Exception x) {/* No Op */}
			if(bos!=null) try { bos.flush(); } catch (Exception x) {/* No Op */}
			if(fos!=null) try { fos.flush(); } catch (Exception x) {/* No Op */}
			if(bos!=null) try { bos.close(); } catch (Exception x) {/* No Op */}
			if(fos!=null) try { fos.close(); } catch (Exception x) {/* No Op */}
		}
		
	}
	
	
	/**
	 * Writes the content read from the passed input stream to the specified file.
	 * @param is The input stream  to read the content from
	 * @param file The file to write to
	 * @param append true to append, false to replace
	 */
	public static void writeToFile(final InputStream is, final File file, final boolean append) {
		if(is==null) throw new IllegalArgumentException("The passed input stream was null");
		if(file==null) throw new IllegalArgumentException("The passed file was null");
		if(!append) try { file.delete(); } catch (Exception x) {/* No Op */}
		if(!file.exists()) {
			try {
				if(!file.createNewFile()) throw new Exception();
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to create the file [" + file + "]");
			}
		}
		if(!file.canWrite()) {
			throw new IllegalArgumentException("Cannot write to the file [" + file + "]");
		}
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		final byte[] transfer = new byte[8192];
		int bytesRead = -1;
		try {
			bis = new BufferedInputStream(is, 8192);
			fos = new FileOutputStream(file, append);
			bos = new BufferedOutputStream(fos, 8192);
			while((bytesRead = bis.read(transfer))!=-1) {
				bos.write(transfer, 0, bytesRead);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Transfer from [" + is + "] to [" + file + "] failed", ex);
		} finally {
			try { bis.close(); } catch (Exception x) {/* No Op */}
			try { is.close(); } catch (Exception x) {/* No Op */}
			try { bos.flush(); } catch (Exception x) {/* No Op */}
			try { fos.flush(); } catch (Exception x) {/* No Op */}
			try { bos.close(); } catch (Exception x) {/* No Op */}
			try { fos.close(); } catch (Exception x) {/* No Op */}
		}
		
	}
	
	/**
	 * Writes the passed stringy content to the specified file.
	 * @param content The content to write
	 * @param file The file to write to
	 * @param append true to append, false to replace
	 */
	public static void writeToFile(final CharSequence content, final File file, final boolean append) {
		if(content==null) throw new IllegalArgumentException("The passed content was null");
		if(file==null) throw new IllegalArgumentException("The passed file was null");
		if(!append) try { file.delete(); } catch (Exception x) {/* No Op */}
		if(!file.exists()) {
			try {
				if(!file.createNewFile()) throw new Exception();
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to create the file [" + file + "]");
			}
		}
		if(!file.canWrite()) {
			throw new IllegalArgumentException("Cannot write to the file [" + file + "]");
		}
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		try {
			fw = new FileWriter(file, append);
			bw = new BufferedWriter(fw);
			bw.write(content.toString());  // FIXME: do char by char write
		} catch (Exception ex) {
			throw new RuntimeException("Failed to write to file [" + file + "]", ex);
		} finally {
			if(bw!=null) {
				try { bw.flush(); } catch (Exception x) {/* No Op */}
				try { bw.close(); } catch (Exception x) {/* No Op */}
			}
			if(fw!=null) {
				try { fw.flush(); } catch (Exception x) {/* No Op */}
				try { fw.close(); } catch (Exception x) {/* No Op */}
			}
		}
		
	}
	
	
	
	/**
	 * Returns the extension of the passed URL's file
	 * @param url The URL to get the extension of
	 * @return the file extension, or null if the file has no extension
	 */
	public static String getExtension(URL url) {
		return getExtension(url, null);
	}
	
	/**
	 * Returns the extension of the passed URL's file
	 * @param url The URL to get the extension of
	 * @param defaultValue The default value to return if there is no extension
	 * @return the file extension, or the default value if the file has no extension
	 */
	public static String getExtension(URL url, String defaultValue) {
		if(url==null) throw new RuntimeException("The passed url was null", new Throwable());
		String file = url.getFile();
		if(file.lastIndexOf(".")==-1) {
			return defaultValue;
		}
		return file.substring(file.lastIndexOf(".")+1);
	}
	
	/**
	 * Returns the extension of the passed URL's file
	 * @param url The URL to get the extension of
	 * @return the file extension, or null if the file has no extension
	 */
	public static String getExtension(CharSequence url) {
		return getExtension(url, null);
	}
	
	/**
	 * Returns the extension of the passed URL's file
	 * @param url The URL to get the extension of
	 * @param defaultValue The default value to return if there is no extension
	 * @return the file extension, or the default value if the file has no extension
	 */
	public static String getExtension(CharSequence url, String defaultValue) {
		if(url==null) throw new RuntimeException("The passed url was null", new Throwable());
		return getExtension(toURL(url), defaultValue);
	}
	
	/**
	 * Returns the extension of the passed file
	 * @param f The file to get the extension of
	 * @return the file extension, or null if the file has no extension
	 */
	public static String getFileExtension(File f) {
		return getFileExtension(f, null);
	}
	
	
	/**
	 * Returns the extension of the passed file
	 * @param f The file to get the extension of
	 * @param defaultValue The default value to return if there is no extension
	 * @return the file extension, or the default value if the file has no extension
	 */
	public static String getFileExtension(File f, String defaultValue) {
		if(f==null) throw new RuntimeException("The passed file was null", new Throwable());
		return getExtension(toURL(f), defaultValue);		
	}
	
	/**
	 * Returns the simple file name (sans extension) for the passed file
	 * @param f The file
	 * @return the simple name
	 */
	public static String getFileSimpleName(final File f) {
		if(f==null) throw new RuntimeException("The passed file was null", new Throwable());
		final String name = f.getName();
		final int index = name.indexOf('.');
		if(index==-1) return name;
		return name.substring(0, index);
	}
	
	/**
	 * Returns the extension of the passed file name
	 * @param f The file name to get the extension of
	 * @return the file extension, or null if the file has no extension
	 */
	public static String getFileExtension(String f) {
		return getFileExtension(f, null);
	}
	
	/**
	 * Returns the extension of the passed file name
	 * @param f The file name to get the extension of
	 * @param defaultValue The default value to return if there is no extension
	 * @return the file extension, or the default value if the file has no extension
	 */
	public static String getFileExtension(String f, String defaultValue) {
		if(f==null) throw new RuntimeException("The passed file was null", new Throwable());
		return getExtension(toURL(new File(f)), defaultValue);		
	}
	
	/**
	 * Computes an MD5 message digest hash on the content contained in the named URL
	 * @param fileOrUrl The name of a file or URL to read from
	 * @return the MD5 message digest hash of the content
	 */
	public static byte[] hashMD5(final CharSequence fileOrUrl) {
		return hash("MD5", fileOrUrl);
	}
	
	/**
	 * Computes a SHA message digest hash on the content contained in the named URL
	 * @param fileOrUrl The name of a file or URL to read from
	 * @return the SHA message digest hash of the content
	 */
	public static byte[] hashSHA(final CharSequence fileOrUrl) {
		return hash("SHA", fileOrUrl);
	}
	
	/**
	 * Computes a SHA256 message digest hash on the content contained in the named URL
	 * @param fileOrUrl The name of a file or URL to read from
	 * @return the SHA256 message digest hash of the content
	 */
	public static byte[] hashSHA256(final CharSequence fileOrUrl) {
		return hash("SHA256", fileOrUrl);
	}
	
	/**
	 * Computes a SHA512 message digest hash on the content contained in the named URL
	 * @param fileOrUrl The name of a file or URL to read from
	 * @return the SHA512 message digest hash of the content
	 */
	public static byte[] hashSHA512(final CharSequence fileOrUrl) {
		return hash("SHA512", fileOrUrl);
	}
	
	/**
	 * Computes a message digest hash on the content contained in the named URL
	 * @param algo The name of the message digest algorithm (e.g. <b><code>MD5</code></b> or <b><code>SHA</code></b>) 
	 * @param fileOrUrl The name of a file or URL to read from
	 * @return the message digest hash of the content
	 */
	public static byte[] hash(final String algo, final CharSequence fileOrUrl) {
		if(algo==null || algo.trim().isEmpty()) throw new IllegalArgumentException("The passed algo was null or empty");
		if(fileOrUrl==null) throw new IllegalArgumentException("The passed fileOrUrl was null");
		final String urlStr = fileOrUrl.toString().trim();
		if(urlStr.isEmpty()) throw new IllegalArgumentException("The passed fileOrUrl was empty");		
		InputStream is = null;
		BufferedInputStream bis = null;
		DigestInputStream dis = null;		
		final MessageDigest md;
		try {
			md = MessageDigest.getInstance(algo.trim());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to get MessageDigest for algorithm [" + algo + "]", ex);
		}
		try {			
			final byte[] buff = new byte[8092];
			final URL url = toURL(urlStr);
			is = url.openStream();
			bis = new BufferedInputStream(is, 8092);
			dis = new DigestInputStream(bis, md);			
			while(dis.read(buff)!=-1) {/* No Op */}
			return md.digest();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to calc " + algo + " for [" + fileOrUrl + "]", ex);
		} finally {
			if(dis!=null) try { dis.close(); } catch (Exception x) {/* No Op */}
			if(bis!=null) try { bis.close(); } catch (Exception x) {/* No Op */}
			if(bis!=null) try { bis.close(); } catch (Exception x) {/* No Op */}
		}
	}

}

