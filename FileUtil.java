import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileUtil {

	public String getFile(String fileName) throws PRDException {
		try {
			return IOUtils.toString(getFileAsStream(fileName), "UTF_8");
		} catch (IOException e) {
			throw new PRDException(e);
		}
	}

	public InputStream getFileAsStream(String fileName) {
		return this.getClass().getClassLoader().getResourceAsStream(fileName);
	}

}
