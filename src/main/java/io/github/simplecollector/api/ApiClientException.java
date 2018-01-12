package io.github.simplecollector.api;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ApiClientException extends RuntimeException {
	private static final long serialVersionUID = 7917494386680267061L;

	public ApiClientException() {
		super();
	}

	public ApiClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiClientException(String message) {
		super(message);
	}

	public ApiClientException(Throwable cause) {
		super(cause);
	}
	
	

}
