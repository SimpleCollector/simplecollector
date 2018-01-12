package io.github.simplecollector.api;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ApiServerException extends RuntimeException {
	private static final long serialVersionUID = -6960795930713708104L;

	public ApiServerException() {
		super();
	}

	public ApiServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiServerException(String message) {
		super(message);
	}

	public ApiServerException(Throwable cause) {
		super(cause);
	}

}
