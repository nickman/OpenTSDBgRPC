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
package net.opentsdb.consul.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Title: NetUtils</p>
 * <p>Description: Useful network utilities</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.consul.utils.NetUtils</code></p>
 */
public class NetUtils {
  
    private static final Set<InetAddress> LOOPBACK_ADDRESSES;
    private static final Set<String> WILDCARDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "0:0:0:0:0:0:0:0", "::", "0.0.0.0"
    )));
    
    static {      
      Set<InetAddress>  loopbacks = networkInterfaces().stream()
      .filter(nic -> {
        try {
          return nic.isLoopback();
        } catch (Exception ex) {
          return false;
        }
      })
      .map(nic -> nicAddresses(nic))
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
      LOOPBACK_ADDRESSES = Collections.unmodifiableSet(loopbacks);
    }
    
    public static boolean isWildcard(String address) {
      return address==null ? false : WILDCARDS.contains(address.trim());
    }
    
    
    
    public static Optional<String> getListenAddress() {
      return networkInterfaces().stream()
        .filter(nic -> {
          try {
            return nic.isUp() && !nic.isLoopback() && !nic.isPointToPoint() && !nic.isVirtual();
          } catch (Exception ex) {
            return false;
          }
        })
        .sorted(Comparator.comparing(NetworkInterface::getIndex))
        .map(nic -> nicAddresses(nic))
        .flatMap(Collection::stream)
        .filter(inet -> inet instanceof Inet4Address)
        .map(inet -> inet.getHostAddress())
        .findFirst();
    }
    
    /**
     * @return
     */
    public static String getListenAddressOrLoopback() {
    	try {
    		return getListenAddress().orElse(InetAddress.getLoopbackAddress().getHostAddress());
    	} catch (Exception ex) {
    		throw new IllegalArgumentException("Failed to get listening address or loopback", ex);
    	}
    }
    
    public static List<NetworkInterface> networkInterfaces() {
      try {
        List<NetworkInterface> ifaces = new ArrayList<>();
        for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
          ifaces.add(en.nextElement());
        }
        return ifaces;
      } catch (Exception ex) {
        throw new RuntimeException("Failed to list network interfaces", ex);
      }
    }
    
    public static List<InetAddress> nicAddresses(NetworkInterface nic) {
      try {
        List<InetAddress> addresses = new ArrayList<>();
        for(Enumeration<InetAddress> en = nic.getInetAddresses(); en.hasMoreElements();) {
          addresses.add(en.nextElement());
        }
        return addresses;
      } catch (Exception ex) {
        throw new RuntimeException("Failed to list network interface addresses", ex);
      }
    }
    
    public static URI[] uris(String u) {
    	if(u==null || u.trim().isEmpty()) {
    		throw new IllegalArgumentException("Null or empty URIs");
    	}
    	String[] frags = u.replace(" ", "").split(",");
    	Set<URI> uris = new HashSet<>();
    	for(String s: frags) {
    		try {
    			uris.add(new URI("tcp://" + s));
    		} catch (Exception x) {}
    	}
    	if(uris.isEmpty()) {
    		throw new IllegalArgumentException("No valid URIs found in [" + u + "]");
    	}
    	return uris.toArray(new URI[uris.size()]);
    }
    

    public static void main(String[] args) {
      System.out.println("Preferred Listener Address: " + getListenAddress());
      for(InetAddress ia : LOOPBACK_ADDRESSES) {
        System.out.println("Loopback: " + ia.getHostAddress() + ", wc: " + ia.isAnyLocalAddress() + ", local: " + ia.isSiteLocalAddress() + ", lb: " + ia.isLoopbackAddress() + ", ll: " + ia.isLinkLocalAddress());
      }
      try {
        InetAddress ia = InetAddress.getByName("0:0:0:0:0:0:0:0");
        System.out.println("Short: " + ia.getHostAddress() + ", wc: " + ia.isAnyLocalAddress() + ", local: " + ia.isSiteLocalAddress() + ", lb: " + ia.isLoopbackAddress() + ", ll: " + ia.isLinkLocalAddress());
        
      } catch (Exception ex) {
        ex.printStackTrace(System.err);
      }
    }
}

