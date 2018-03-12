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
package net.opentsdb.consul;

import net.opentsdb.plugin.common.isolation.BootStartupPlugin;

/**
 * <p>Title: IsolatedConsulPlugin</p>
 * <p>Description: Isolated classpath plugin wrapper for {@link ConsulPlugin}</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.consul.IsolatedConsulPlugin</code></p>
 */

public class IsolatedConsulPlugin extends BootStartupPlugin {

	/**
	 * Creates a new IsolatedConsulPlugin
	 */
	public IsolatedConsulPlugin() {
		super("net.opentsdb.consul.ConsulPlugin");
	}

}
