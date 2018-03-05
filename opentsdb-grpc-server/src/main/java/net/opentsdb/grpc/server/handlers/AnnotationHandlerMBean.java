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
package net.opentsdb.grpc.server.handlers;

/**
 * <p>Title: AnnotationHandlerMBean</p>
 * <p>Description: JMX interface for {@link AnnotationHandler}</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.AnnotationHandlerMBean</code></p>
 */

public interface AnnotationHandlerMBean {
	
	/**
	 * Returns the total number of annotation retrieval requests
	 * @return the total number of annotation retrieval requests
	 */
	public long getAnnotationRequests();
	
	/**
	 * Returns the total number of annotation create requests
	 * @return the total number of annotation create requests
	 */
	public long getCreateRequests();

	/**
	 * Returns the total number of successfully created annotations
	 * @return the total number of successfully created annotations
	 */
	public long getSuccessfulCreates();
	
	/**
	 * Returns the total number of failed annotation creates
	 * @return the total number of failed annotation creates
	 */
	public long getFailedCreates();	
}
