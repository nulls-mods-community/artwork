package nullsteam.mods.artwork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Represents a mutator that works with data of type {@code T} and applies patches of type {@code V} to that data.</p>
 *
 * @param <T> the type of data
 * @param <V> the type of patches
 */
public interface Mutator<T, V> {
	/**
	 * <p>Loads data from the given input stream.</p>
	 *
	 * @param is the input stream
	 * @return a new data instance
	 * @throws IOException if underlying stream throws such exception, or if input is malformed
	 */
	T load(InputStream is) throws IOException;

	/**
	 * <p>Saves the data to the given output stream.</p>
	 *
	 * @param instance the data instance
	 * @param os       the output stream
	 * @throws IOException if underlying stream throws such exception
	 */
	void save(T instance, OutputStream os) throws IOException;

	/**
	 * <p>Mutates the given data instance by applying the given patch.</p>
	 *
	 * @param instance the data instance to mutate
	 * @param patch    the patch (this parameter is read-only)
	 */
	void apply(T instance, V patch);

	/**
	 * <p>Merge two patches, so the base patch will also contain an extension patch.</p>
	 * <p>This means that doing the following statements MAY have the same data as a result.
	 * <ol>
	 *   <li><code>apply(D, V0)</code> followed by <code>apply(D, V1)</code></li>
	 *   <li><code>merge(V0, V1)</code> followed by <code>apply(D, V0)</code></li>
	 * </ol></p>
	 * <p>Guaranteed properties:
	 * <ol>
	 *   <li><code>merge(V0, V0)</code> leaves <code>V0</code> unchanged</li>
	 *   <li>Calling <code>merge(V0, V1)</code> a multiple times in a row has the same effect on <code>V0</code> as calling it once</li>
	 * </ol></p>
	 *
	 * @param base      the base patch to mutate
	 * @param extension the extension patch (this parameter is read-only)
	 */
	void merge(V base, V extension);
}
