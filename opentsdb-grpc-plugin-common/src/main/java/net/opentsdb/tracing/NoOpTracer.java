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
package net.opentsdb.tracing;

import java.util.Collections;
import java.util.Map.Entry;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.noop.NoopSpanBuilder;
import io.opentracing.propagation.Format;

/**
 * <p>Title: NoOpTracer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.tracing.NoOpTracer</code></p>
 */

public class NoOpTracer implements Tracer {
	public static final NoOpTracer INSTANCE = new NoOpTracer();
	
	private static final NoopSpanContext NOOP_SPAN_CONTEXT = new NoopSpanContext();
	
	private NoOpTracer() {}

	/**
	 * {@inheritDoc}
	 * @see io.opentracing.Tracer#scopeManager()
	 */
	@Override
	public ScopeManager scopeManager() {
		return NoopScopeManager.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 * @see io.opentracing.Tracer#activeSpan()
	 */
	@Override
	public Span activeSpan() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see io.opentracing.Tracer#buildSpan(java.lang.String)
	 */
	@Override
	public SpanBuilder buildSpan(String operationName) {
		return NoopSpanBuilder.INSTANCE; 
	}

	/**
	 * {@inheritDoc}
	 * @see io.opentracing.Tracer#inject(io.opentracing.SpanContext, io.opentracing.propagation.Format, java.lang.Object)
	 */
	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
		/* No Op */
	}

	/**
	 * {@inheritDoc}
	 * @see io.opentracing.Tracer#extract(io.opentracing.propagation.Format, java.lang.Object)
	 */
	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		return NOOP_SPAN_CONTEXT;
	}
	
	private static class NoopSpanContext implements SpanContext {

		@Override
		public Iterable<Entry<String, String>> baggageItems() {
			return Collections.emptyList();
		}
		
	}

}
