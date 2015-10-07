import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileUtil {

	public String getFile(String fileName) throws IOException {
		return IOUtils.toString(getFileAsStream(fileName), "UTF-8");
	}

	public InputStream getFileAsStream(String fileName) {
		return this.getClass().getClassLoader().getResourceAsStream(fileName);
	}

}
