package nullsteam.mods.artwork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Mutator<T, V> {
	T load(InputStream is) throws IOException;

	void save(T instance, OutputStream os) throws IOException;

	void merge(V base, V extension);

	void apply(T instance, V patch);
}
