/**
 * The MIT License
 * Copyright (c) 2014 Gustave Laville (laville@web.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.gv2011.helloworld.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gv2011.helloworld.Greeting;
import de.gv2011.helloworld.GreetingService;

public class GreetingServiceImpl implements GreetingService{
	
	private static final Logger LOG = LoggerFactory.getLogger(GreetingServiceImpl.class);
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	final Map<GreetingType,String> greetingTexts;

	public GreetingServiceImpl() {
		Map<GreetingType,String> greetingTexts = new EnumMap<>(GreetingType.class);
		greetingTexts.put(GreetingType.HELLO, "Hello world!");
		greetingTexts.put(GreetingType.GOODBYE, "Goodbye, world!");
		this.greetingTexts = Collections.unmodifiableMap(greetingTexts);
		LOG.info("Created {}.", this);
	}

	@Override
	public Greeting getGreeting(GreetingType greetingType) {
		Future<Greeting> result = executor.submit(()->createGreeting(greetingType));
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Greeting createGreeting(GreetingType greetingType) throws InterruptedException{
		String text = getText(greetingType);
		GreetingImpl greeting = new GreetingImpl(text);
		LOG.info("Created greeting of type {}.", greetingType);
		return greeting;
	}

	private String getText(GreetingType greetingType) {
		return greetingTexts.get(greetingType);
	}

	@Override
	public void close() throws Exception {
		LOG.info("Closing {}.", this);
		executor.shutdown();
		final int timeout = 1;
		while(!executor.awaitTermination(timeout, TimeUnit.SECONDS)){
			LOG.warn("Executor of {} did not shutdown after {} second, forcing shutdown.", this, timeout);
			executor.shutdownNow();
		};
		LOG.info("Closed {}.", this);
	}

}
