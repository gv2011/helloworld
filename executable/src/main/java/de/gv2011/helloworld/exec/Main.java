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
package de.gv2011.helloworld.exec;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import de.gv2011.helloworld.Greeting;
import de.gv2011.helloworld.GreetingService;
import de.gv2011.helloworld.GreetingService.GreetingType;
import de.gv2011.helloworld.util.ServiceUtils;

public class Main {

	public static void main(String[] args) throws Exception {
		final Thread mainThread = Thread.currentThread();
		final AtomicBoolean shouldRun = new AtomicBoolean(true);
		Runtime.getRuntime().addShutdownHook(new Thread(()->{shutdown(mainThread, shouldRun);}));
		try (GreetingService service = ServiceUtils.loadService(GreetingService.class)) {
			while(shouldRun.get()){
				Greeting greeting = service.getGreeting(GreetingType.HELLO);
				System.out.println(greeting);
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(10));
				} catch (InterruptedException e) {}
			}
			Greeting greeting = service.getGreeting(GreetingType.GOODBYE);
			System.out.println(greeting);
		}
	}
	
	private static void shutdown(Thread mainThread, AtomicBoolean shouldRun) {
		try {
			shouldRun.set(false);
			mainThread.interrupt();
			mainThread.join(0);
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
