import static ReadConfiguration.getHost;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class HttpRequestUtil {

	private static final String AUTHORIZATION = "Authorization";
	private static final String SERIAL_VERSION_UID = "serialVersionUID";
	private static final String ACCEPT = "Accept";
	private static final String CONTENT_TYPE = "content-type";
	private static final String APPLICATION_JSON = "application/json;charset=utf-8";
	private static final Logger logger = Logger.getLogger(ResponseHelper.class);
	private static HttpURLConnection connection;
	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static <T> T getResponse(String requestString, String requestInfo, Class<T> responseClass) throws IOException, JsonParseException, JsonMappingException, Exception {
		return getResponse(requestString, RequestMethod.GET, null, requestInfo, responseClass, MediaType.APPLICATION_JSON);
	}

	public static <T> T getResponse(String requestString, RequestMethod method, String requestBody, String requestInfo, Class<T> responseClass, MediaType acceptMediatype) throws IOException,
			JsonParseException,
			JsonMappingException, Exception {
		try {
			if (responseClass.equals(byte[].class)) {
				return (T) IOUtils.toByteArray((getResponseStream(requestString, method, requestBody, requestInfo, acceptMediatype)));
			}
			if (responseClass.equals(String.class)) {
				BufferedReader in = new BufferedReader(new InputStreamReader(getResponseStream(requestString, method, requestBody, requestInfo, acceptMediatype)));
				String retVal = "";
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					retVal += inputLine;
				}
				in.close();
				return (T) retVal;
			}
			return objectMapper.readValue(getResponseStream(requestString, method, requestBody, requestInfo, acceptMediatype), responseClass);
		} finally {
			connection.disconnect();
		}
	}

	public static <T> Collection<T> getResponse(String requestString, RequestMethod method, String requestBody, String requestInfo, TypeReference<Collection<T>> typeReference)
			throws JsonParseException, JsonMappingException, IOException, Exception {
		try {
			return objectMapper.readValue(getResponseStream(requestString, method, requestBody, requestInfo, MediaType.APPLICATION_JSON), typeReference);
		} finally {
			connection.disconnect();
		}
	}

	private static InputStream getResponseStream(String servicePath, RequestMethod method, String requestBody, String requestInfo, MediaType acceptMediaType) throws Exception {
		logger.info("Called getResponseStream from " + servicePath + " with " + method.name());
		URL url = new URL(getHost() + servicePath);
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method.name());
		if (acceptMediaType != null) {
			connection.setRequestProperty(ACCEPT, acceptMediaType.toString());
		}

		if (requestInfo != null) {
			connection.setRequestProperty(AUTHORIZATION, getAuthorizationHeader(requestInfo));
		}

		if (!DELETE.equals(method)) {
			connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
		}
		if (requestBody != null) {
			// System.out.println("requestBody: " + requestBody);
			connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(requestBody);
			out.flush();
			out.close();
		}
		return connection.getInputStream();
	}

	private static String getAuthorizationHeader(String requestInfo) {
		String auth = requestInfo + ":";
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(encodedAuth);
	}

	public static String getJSON(Object o) throws JsonProcessingException {
		return objectMapper.writeValueAsString(o);
	}

	public static <T> T getObject(String input, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(input, clazz);
	}

	public static String createQueryString(Object dto) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String retval = "";
		if (dto == null) {
			return retval;
		}
		Class<? extends Object> clazz = dto.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName() != SERIAL_VERSION_UID && !ignored(clazz, field.getName())) {
				field.setAccessible(true);
				Object object = field.get(dto);
				if (useable(object)) {
					String string;
					string = object.toString();
					if (object instanceof Date) {
						SimpleDateFormat dateUtil =  new SimpleDateFormat("dd/MM/yyyy");
						string = dateUtil.format((Date) object);
					}
					retval += "&" + field.getName() + "=" + string;
				}
			}
		}
		if (!"".equals(retval)) {
			retval = retval.replaceFirst("&", "");
		}
		logger.info("getQueryString: " + retval);
		return retval;
	}

	protected static boolean useable(Object object) {
		if (object != null) {
			if ((object instanceof ArrayList) && ((ArrayList) object).isEmpty()) {
				return false;
			}
			return true;
		}
		return false;
	}

	protected static boolean ignored(Class<? extends Object> clazz, String field) {
		JsonIgnoreProperties annotation = clazz.getAnnotation(JsonIgnoreProperties.class);
		return null != annotation && field.equals(annotation.value()[0]);
	}

	public static final String NO_URL = "no-url";

}
