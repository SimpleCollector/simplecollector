package io.github.simplecollector.api;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MessageOnlyErrorAttributes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = new LinkedHashMap<String, Object>();

		Integer status = (Integer) requestAttributes.getAttribute("javax.servlet.error.status_code", RequestAttributes.SCOPE_REQUEST);
		if (status != null)
			errorAttributes.put("status", status);
		Throwable throwable = getError(requestAttributes);
		errorAttributes.put("message", throwable.getMessage());

		return errorAttributes;
	}
	
}