import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import eu.cec.press.prd.util.file.FileUtil;

public class ReadConfiguration {

	private static final String SERVICE_HOST = "service.host";
	private static final String PROPERTIES_FILE_NAME = "test.properties";
	private static Properties props;

	static {
		try {
			props = new Properties();
			props.load(new FileUtil().getFileAsStream(PROPERTIES_FILE_NAME));
		} catch (Exception e) {
		}
	}

	static String getHost() throws FileNotFoundException, IOException {
		return props.getProperty(SERVICE_HOST);
	}
}
