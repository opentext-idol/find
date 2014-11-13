package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;

public class PredicateCatching implements Predicate<WebDriver> {

	private final Predicate<WebDriver> basePredicate;
	private final PredicateExceptionHandler handler;

	public PredicateCatching(final Predicate<WebDriver> basePredicate, final Class<? extends Throwable> targetThrowables, final boolean output) {
		super();
		this.basePredicate = basePredicate;
		this.handler = new TrivialHandler(targetThrowables, output);
	}

	public PredicateCatching(final Predicate<WebDriver> basePredicate, final PredicateExceptionHandler handler) {
		super();
		this.basePredicate = basePredicate;
		this.handler = handler;
	}

	@Override
	public boolean apply(WebDriver driver) {
		try {
			return basePredicate.apply(driver);
		} catch (Throwable thr) {
			return handler.handle(thr);
		}
	}

	private final class TrivialHandler implements PredicateExceptionHandler {

		private final Class<? extends Throwable> tc;
		private final boolean output;

		public TrivialHandler(final Class<? extends Throwable> tc, final boolean output) {
			this.tc = tc;
			this.output = output;
		}

		@Override
		public boolean handle(Throwable thr) {
			if (tc.isInstance(thr)) {
				return output;
			} else if (thr instanceof Error) {
				throw (Error) thr;
			} else if (thr instanceof RuntimeException) {
				throw (RuntimeException) thr;
			} else {
				throw new RuntimeException("This shouldn't be possible, since thr was thrown by Predicate<WebDriver>.apply", thr);
			}
		}

	}

}
