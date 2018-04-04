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
package net.opentsdb.plugin.common.isolation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.StreamSupport;

import javax.management.MBeanServer;
import javax.management.ObjectName;



/**
 * <p>Title: IsolatedClassLoader2</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.plugin.common.isolation.IsolatedClassLoader2</code></p>
 */

public class IsolatedClassLoader2 extends ClassLoader {
	/** The child class loader */
	protected final ChildURLClassLoader childClassLoader;
	/** The JMX ObjectName to register the class loader under */
	protected final ObjectName objectName;
	
	protected static final ConcurrentHashMap<String, IsolatedClassLoader2> LOADERS = new ConcurrentHashMap<>();
	
	public static IsolatedClassLoader2 classLoader(boolean expandDirs, final URL url) {
		String key = key(url) + expandDirs;
		
		return LOADERS.computeIfAbsent(key, k -> {
			if(!expandDirs) {
				return new IsolatedClassLoader2(url);
			} else {
				if(isDirectory(url)) {
					File dir = toDirectory(url);
					return new IsolatedClassLoader2(fromDirs(dir));
				} else {
					File jar = toFile(url);
					if(jar==null) {
						throw new IllegalArgumentException("The passed URL [" + url + "] was not a directory");
					}
					return new IsolatedClassLoader2(fromDirs(jar.getParentFile()));
				}
			}
		});
	}
	
	private static <T> String key(T...items) {
		return Arrays.toString(items);
	}
	
	private static URL[] fromDirs(File...dirs) {
		Set<URL> urls = new HashSet<URL>();
		for(File f : dirs) {
			if(f.isDirectory()) {
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(f.toPath())) {
					StreamSupport.stream(stream.spliterator(), false)
						.map(p -> p.toFile())
						.filter(file -> file.isFile() && file.getName().endsWith(".jar"))
						.map(file -> toURL(file))
						.forEach(url -> urls.add(url));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				if(f.isFile() && f.getName().endsWith(".jar")) {
					urls.add(toURL(f));
				}
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}
	
	private static URL toURL(File f) {
		try {
			return f.toURI().toURL();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create URL from file [" + f + "]", ex);
		}
	}
	
	private static boolean isFile(URL url) {
		return toFile(url) != null;
	}
	
	private static boolean isDirectory(URL url) {
		return toDirectory(url) != null;
	}

	
	private static File toFile(URL url) {
		File f = new File(url.getFile());
		if(f.isFile() && f.canRead()) {
			return f;
		}
		return null;
	}
	
	private static File toDirectory(URL url) {
		File f = new File(url.getFile());
		if(f.isDirectory()) {
			return f;
		}
		return null;
	}
	
	
	/**
	 * Creates a new IsolatedClassLoader
	 * @param clazz The class to derive the source classloader URL from
	 * @param objectName The JMX ObjectName to register the management interface with. Ignored if null.
	 */
	private IsolatedClassLoader2(final Class<?> clazz, final String objectName) {
		if(clazz==null) throw new IllegalArgumentException("The passed class was null");
		final ProtectionDomain pd = clazz.getProtectionDomain();
		if(pd==null) throw new IllegalArgumentException("The passed class [" + clazz.getName() + "] had a null ProtectionDomain");
		final CodeSource cs = pd.getCodeSource();
		if(cs==null) throw new IllegalArgumentException("The passed class [" + clazz.getName() + "] had a null CodeSource");
		final URL url = cs.getLocation();
		if(url==null) throw new IllegalArgumentException("The passed class [" + clazz.getName() + "] had a null code source location");
		childClassLoader = new ChildURLClassLoader(new URL[]{url}, new FindClassClassLoader(this.getParent()) );
		ObjectName tmp = null;
		try {
			if(objectName!=null && !objectName.trim().isEmpty()) {
				tmp = new ObjectName(objectName.trim());
			}
		} catch (Exception ex) {
			tmp = null;
		}
		this.objectName = tmp;
	}
	
	/**
	 * Creates a new IsolatedClassLoader
	 * @param objectName The JMX ObjectName to register the management interface with. Ignored if null.
	 * @param urls The classpath the loader will load from
	 */
	private IsolatedClassLoader2(final ObjectName objectName, final URL... urls) {
		super(Thread.currentThread().getContextClassLoader());
		this.objectName = objectName;
		childClassLoader = new ChildURLClassLoader( urls, new FindClassClassLoader(this.getParent()) );
		try {
			if(this.objectName!=null) {
				final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
				if(server.isRegistered(this.objectName)) {
					server.unregisterMBean(this.objectName);
				}
				server.registerMBean(this, this.objectName);
			}
		} catch (Exception ex) {
			System.err.println("Failed to register IsolatedClassLoader MBean [" + this.objectName + "]. Stack trace follows...");
			ex.printStackTrace(System.err);
		}
	}
	
	/**
	 * Creates a new IsolatedClassLoader
	 * @param urls The classpath the loader will load from
	 */
	private IsolatedClassLoader2(final URL...urls) {
		this(null, urls);
	}	
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.utils.classload.IsolatedClassLoaderMBean#getURLs()
	 */
	public URL[] getURLs() {
		return childClassLoader.getURLs();
	}
	
	
	
    /**
     * Appends the specified URL to the list of URLs to search for
     * classes and resources.
     * <p>
     * If the URL specified is <code>null</code> or is already in the
     * list of URLs, or if this loader is closed, then invoking this
     * method has no effect.
     *
     * @param url the URL to be added to the search path of URLs
     */
	public void addURL(final URL url) {
		childClassLoader.addURL(url);
	}
	
	/**
	 * Returns the designated JMX ObjectName
	 * @return the designated JMX ObjectName or null if one was not assigned
	 */
	public ObjectName getObjectName() {
		return objectName;
	}
	
	/**
	 * System out format logger
	 * @param fmt The message format
	 * @param args The message args
	 */
	public static void log(final Object fmt, final Object...args) {
		System.out.println("[CSF-IsolatedClassLoader]" + String.format(fmt.toString(), args));
	}	
	/**
	 * System err format logger
	 * @param fmt The message format
	 * @param args The message args
	 */
	public static void loge(final Object fmt, final Object...args) {
		System.err.println("[CSF-IsolatedClassLoader]" + String.format(fmt.toString(), args));
	}
	
	
  /**
   * {@inheritDoc}
   * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
   */
  @Override
  protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
      try {
//    	  log("Loading [%s], resolve [%s]", name, resolve);
          // first we try to find a class inside the child classloader
          return childClassLoader.findClass(name);
      } catch( ClassNotFoundException e ) {
          // didn't find it, try the parent
          return super.loadClass(name, resolve);
      }
  }
  
  public String toString() {
	  final StringBuilder b = new StringBuilder("Core IsolatedClassLoader [");
	  for(URL url: childClassLoader.getURLs()) {
		  b.append("\n\t").append(url);
	  }
	  return b.append("\n]").toString();
  }
  
  
  /**
 * {@inheritDoc}
 * @see java.lang.ClassLoader#findResource(java.lang.String)
 */
@Override
  public URL findResource(final String name) {
  	final URL url = childClassLoader.findResource(name);  	
  	return url;
  }
  
  /**
 * {@inheritDoc}
 * @see java.lang.ClassLoader#getResource(java.lang.String)
 */
@Override
  public URL getResource(String name) {
  	final URL url = childClassLoader.getResource(name);
  	return url;
  }
  
  /**
 * {@inheritDoc}
 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
 */
@Override
  public InputStream getResourceAsStream(final String name) {
  	return childClassLoader.getResourceAsStream(name);
  }

  /**
   * This class delegates (child then parent) for the findClass method for a URLClassLoader.
   * We need this because findClass is protected in URLClassLoader
   */
  private static class ChildURLClassLoader extends URLClassLoader {
      /** The real parent class loader */
    private FindClassClassLoader realParent;
    /** The context to be used when loading classes and resources */
    private final AccessControlContext acc;
    /** A map of the class contents of the URLs */
    private final ConcurrentHashMap<String, ByteBuffer> jarItems = new ConcurrentHashMap<String, ByteBuffer>();
    /** A map of the resource contents of the URLs */
    private final ConcurrentHashMap<String, URL> resourceItems = new ConcurrentHashMap<String, URL>();
    
    
    private final Map<String, ProtectionDomain> protectionDomains = new ConcurrentHashMap<String, ProtectionDomain>();
    
    private Permissions permissions = new Permissions();

    /**
     * Creates a new ChildURLClassLoader
     * @param urls The URLs comprising the isolated classpath
     * @param realParent The real parent classloader
     */
    public ChildURLClassLoader( URL[] urls, FindClassClassLoader realParent ) {    		
    	super(urls, null);
    	permissions.add(new AllPermission());
//    	log("ChildURLClassLoader Inited with [%s]", Arrays.toString(urls));
        this.realParent = realParent;
        acc = AccessController.getContext();
        for(final URL url: urls) {
        	final CodeSource cs = new CodeSource(url, (Certificate[])null);
        	final ProtectionDomain pd = new ProtectionDomain(cs, permissions);
    		InputStream is = null;
    		JarInputStream jis = null;        	
        	try {
        		is = url.openStream();
        		jis = new JarInputStream(is);
        		JarEntry je = null;
    			while((je = jis.getNextJarEntry())!=null) {
    				try {    					
	    				if(je.isDirectory()) continue;
	    				final String rezName = je.getName();
	    				if(jarItems.containsKey(rezName)) continue;
	    				if(!rezName.endsWith(".class")) {
	    					resourceItems.put(rezName, new URL("jar:" + url + "!/" + rezName));
	    					continue;
	    				}
	    				byte[] byteCode = load(jis);
//	    				log("Entry [%s], size: %s", rezName, byteCode.length);
	    				final ByteBuffer bb = ByteBuffer.allocateDirect(byteCode.length);
	    				bb.put(byteCode);
	    				bb.flip();
	    				byteCode = null;
	    				jarItems.put(rezName, bb);
	    				protectionDomains.put(rezName, pd);
    				} finally {
    					try { jis.closeEntry(); } catch (Exception x) {/* No Op */}
    				}
    			}        		
        	} catch (Exception ex) {
        		loge("Failed in load of [%s]: %s", url, ex.toString());
        	} finally {
    			if(jis != null) try { jis.close(); } catch (Exception x) {/* No Op */}
    			if(is != null) try { is.close(); } catch (Exception x) {/* No Op */}
    		}
        }
    }
    
    private static byte[] load(final JarInputStream jis) throws Exception {
    	ByteArrayOutputStream baos = null;
    	try {
    		baos = new ByteArrayOutputStream(1024);
    		byte[] buff = new byte[1024];
    		int bytesRead = 0;
    		while((bytesRead = jis.read(buff))!=-1) {
    			baos.write(buff, 0, bytesRead);
    		}
    		return baos.toByteArray();
    	} finally {
    		if(baos != null) try { baos.close(); } catch (Exception x) {/* No Op */}
    	}
    }
    
    /**
     * Finds and loads the class with the specified name from the URL search
     * path. Any URLs referring to JAR files are loaded and opened as needed
     * until the class is found.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found,
     *            or if the loader is closed.
     */
    protected Class<?> _findClass(final String name) throws ClassNotFoundException {
        try {
                        final String path = name.replace('.', '/').concat(".class");
                        ByteBuffer bb = jarItems.get(path);
                        ProtectionDomain pd = protectionDomains.get(path);
                        if(bb!=null && pd!=null) {
//                        	log("Found cached bb for [%s]", path);
                        	jarItems.remove(path);
                        	protectionDomains.remove(path);
                        	return defineClass(name, bb, pd);
                        }
//						log("[%s] not found in cache", path);
						throw new ClassNotFoundException(path);
        } catch (Exception pae) {
            throw new ClassNotFoundException(name);
        }
    	
//        try {
//            return AccessController.doPrivileged(
//                new PrivilegedExceptionAction<Class<?>>() {
//                    public Class<?> run() throws ClassNotFoundException {
//                        final String path = name.replace('.', '/').concat(".class");
//                        ByteBuffer bb = jarItems.get(path);
//                        ProtectionDomain pd = protectionDomains.get(path);
//                        if(bb!=null && pd!=null) {
////                        	log("Found cached bb for [%s]", path);
//                        	jarItems.remove(path);
//                        	protectionDomains.remove(path);
//                        	return defineClass(name, bb, pd);
//                        }
////						log("[%s] not found in cache", path);
//						throw new ClassNotFoundException(path);
//                    }
//                }, acc);
//        } catch (java.security.PrivilegedActionException pae) {
//            throw (ClassNotFoundException) pae.getException();
//        }
    }    
    
    @Override
    public URL findResource(final String name) {
    	URL url = null;
    	if(resourceItems.containsKey(name)) {
    		url = resourceItems.get(name);
    	} else {
    		url = super.findResource(name);
    	}
    	return url;
    }
    
    @Override
    public InputStream getResourceAsStream(final String name) {
    	final URL url = findResource(name);    	
    	try {
    		return url==null ? null : url.openStream();
    	} catch (Exception x) {
    		return null;
    	}
    }
    

      /**
     * {@inheritDoc}
     * @see java.net.URLClassLoader#findClass(java.lang.String)
     */
    @Override
      public Class<?> findClass(String name) throws ClassNotFoundException {
//    	log("ChildURLClassLoader.findClass(%s)", name);
      	Class<?> loaded = super.findLoadedClass(name);
        if( loaded != null ) return loaded;	        	
          try {
              // first try to use the URLClassLoader findClass
              return _findClass(name);
          }  catch( ClassNotFoundException e ) {
              // if that fails, we ask our real parent classloader to load the class (we give up)
              return realParent.loadClass(name);
          }
      }
      
    /**
     * {@inheritDoc}
     * @see java.net.URLClassLoader#addURL(java.net.URL)
     */
    @Override
	public void addURL(final URL url) {
    	  super.addURL(url);
      }
  }
  
  /**
   * This class allows me to call findClass on a classloader
   */
  private static class FindClassClassLoader extends ClassLoader {
      public FindClassClassLoader(ClassLoader parent) {
          super(parent);
      }

      @Override
      public Class<?> findClass(String name) throws ClassNotFoundException {
          return super.findClass(name);
      }
  }
  

}

