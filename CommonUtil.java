import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommonUtil {

	public static <T> Collection<T> safeCollection(Collection<T> other) {
		return other == null ? Collections.EMPTY_LIST : other;
	}

	public static <T, K> Map<T, K> safeMap(Map<T, K> other) {
		return other == null ? new HashMap<T, K>() : other;
	}

	public static String safeString(String string) {
		return string == null ? "" : string;
	}

	public static Date nvl(Date input) {
		return input == null ? new Date() : input;
	}

	public static String nvl(Enum input) {
		return input == null ? "" : input.name();
	}
}
