package repeat;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Repeat annotation to be applied at the test method level to indicate how many time the test method to be executed
 * repeatedly.
 * </p>
 * <p>
 * Usage:
 * <p>
 * <pre>
 * &#64;Rule
 * public RepeatRule repeatRule = new RepeatRule();
 *
 * &#64;Test
 * &#64;Repeat(10)
 * public void testRepeatTenTimes()
 * {
 *     PrintStream err = System.err;
 *     err.println(Math.random());
 * }
 * </pre>
 * </p>
 *
 * @author fappel, ttran
 * @link https://gist.github.com/fappel/8bcb2aea4b39ff9cfb6e
 * @since 1.0
 **/
@Retention(RUNTIME)
@Target(METHOD)
public @interface Repeat {
    /**
     * <p>
     * Indicate the number of time a test method should be repeated.
     * </p>
     *
     * @return number of repeats
     **/
    int value();
}