
package com.github.gv2011.helloworld.exec;

/*-
 * #%L
 * helloworld-executable
 * %%
 * Copyright (C) 2014 - 2017 Vinz (https://github.com/gv2011)
 * %%
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
 * #L%
 */
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.helloworld.Greeting;
import com.github.gv2011.helloworld.GreetingService;
import com.github.gv2011.helloworld.GreetingService.GreetingType;
import com.github.gv2011.helloworld.util.ServiceUtils;

import ch.qos.logback.classic.LoggerContext;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) throws Exception {
		LOG.debug("Started to execute main method.");
		final Thread mainThread = Thread.currentThread();
		final AtomicBoolean shouldRun = new AtomicBoolean(true);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdown(mainThread, shouldRun);
		},"shutdown-hook"));
		try (GreetingService service = ServiceUtils
				.loadService(GreetingService.class)) {
			while (shouldRun.get()) {
				LOG.debug("Obtaining hello greeting.");
				final Greeting greeting = service.getGreeting(GreetingType.HELLO);
				System.out.println(greeting);
				LOG.debug("Did hello greeting. Waiting some time now.");
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(10));
					LOG.debug("Finished waiting.");
				} catch (final InterruptedException e) {
					LOG.debug("Received interrupt while waiting.");
				}
			}
			LOG.debug("Obtaining goodbye greeting.");
			final Greeting greeting = service.getGreeting(GreetingType.GOODBYE);
			System.out.println(greeting);
			LOG.debug("Did goodbye greeting.");
		}
	}

	private static void shutdown(final Thread mainThread, final AtomicBoolean shouldRun) {
		LOG.info("Shutting down.");
		shouldRun.set(false);
		mainThread.interrupt();
		LOG.debug("Waiting until main thread terminates.");
		try {
			mainThread.join(0);
		} catch (final InterruptedException e) {
			LOG.error("Interrupted while waiting for main thread to terminate.");
		}
		LOG.debug("Main thread terminated.");
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		LOG.info("Shutting down logging now, goodbye.");
		loggerContext.stop();
	}

}
