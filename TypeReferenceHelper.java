
import java.util.Collection;
import com.fasterxml.jackson.core.type.TypeReference;

public class TypeReferenceHelper {

	public final static TypeReference<Collection<String>> stringListTypeReference = new TypeReference<Collection<String>>() {
	};
}
