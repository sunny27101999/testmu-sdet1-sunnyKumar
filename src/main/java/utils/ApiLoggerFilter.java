package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.Logger;

/**
 * ApiLoggerFilter — A RestAssured filter to capture and store the last API request and response
 * in ThreadLocal variables for later retrieval, especially useful for failure analysis.
 */
public class ApiLoggerFilter implements Filter {

    private static final Logger log = LoggerUtil.getLogger(ApiLoggerFilter.class);

    private static final ThreadLocal<String> lastRequest = new ThreadLocal<>();
    private static final ThreadLocal<String> lastResponse = new ThreadLocal<>();

    public static String getLastRequest() {
        return lastRequest.get();
    }

    public static String getLastResponse() {
        return lastResponse.get();
    }

    public static void clear() {
        lastRequest.remove();
        lastResponse.remove();
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        // Capture request details
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Method: ").append(requestSpec.getMethod()).append("\n");
        requestDetails.append("URI: ").append(requestSpec.getURI()).append("\n");
        requestDetails.append("Headers: ");
        requestSpec.getHeaders().forEach(header ->
                requestDetails.append(header.getName()).append("=").append(header.getValue()).append("; ")
        );
        requestDetails.append("\n");
        if (requestSpec.getBody() != null) {
            requestDetails.append("Body: ").append(String.valueOf(requestSpec.getBody())).append("\n");
        }
        lastRequest.set(requestDetails.toString());
        log.debug("Captured API Request: \n{}", requestDetails.toString());

        // Execute the request
        Response response = ctx.next(requestSpec, responseSpec);

        // Capture response details
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(response.getStatusCode()).append("\n");
        responseDetails.append("Headers: ");
        response.getHeaders().forEach(header ->
                responseDetails.append(header.getName()).append("=").append(header.getValue()).append("; ")
        );
        responseDetails.append("\n");
        responseDetails.append("Body: ").append(response.getBody().asString()).append("\n");
        lastResponse.set(responseDetails.toString());
        log.debug("Captured API Response: \n{}", responseDetails.toString());

        return response;
    }
}
