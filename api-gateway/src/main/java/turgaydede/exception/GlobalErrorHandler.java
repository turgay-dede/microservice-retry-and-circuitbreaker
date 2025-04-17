package turgaydede.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus httpStatus = determineHttpStatus(ex);
        String customMessage = determineMessage(ex, httpStatus);

        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", System.currentTimeMillis());
        errorAttributes.put("path", exchange.getRequest().getPath().value());
        errorAttributes.put("status", httpStatus.value());
        errorAttributes.put("error", httpStatus.getReasonPhrase());
        errorAttributes.put("message", customMessage);

        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorAttributes);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap("Internal Error".getBytes(StandardCharsets.UTF_8))));
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return toHttpStatus(rse.getStatusCode());
        }

        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("503")) return HttpStatus.SERVICE_UNAVAILABLE;
            if (message.contains("404")) return HttpStatus.NOT_FOUND;
            if (message.contains("500")) return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus toHttpStatus(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus httpStatus) {
            return httpStatus;
        } else {
            try {
                return HttpStatus.valueOf(statusCode.value());
            } catch (Exception e) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }

    private String determineMessage(Throwable ex, HttpStatus status) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";

        if (ex instanceof ResponseStatusException && message.contains("Unable to find instance")) {
            String serviceName = extractServiceName(message);
            return "Şu mikroservise ulaşılamıyor: " + serviceName + ". Lütfen daha sonra tekrar deneyiniz.";
        }

        if (message.contains("connection refused")) {
            return "Sunucu bağlantısı reddetti. Servis çalışmıyor olabilir.";
        }

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Sunucuya ulaşırken zaman aşımına uğradı.";
        }

        if (status == HttpStatus.NOT_FOUND) {
            return "İstenilen kaynak bulunamadı.";
        }

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Servis şu anda kullanılamıyor.";
        }

        return "Beklenmeyen bir hata oluştu. Lütfen sistem yöneticisine başvurun.";
    }


    private String extractServiceName(String message) {
        String prefix = "Unable to find instance for ";
        try {
            int index = message.indexOf(prefix);
            if (index != -1) {
                return message
                        .substring(index + prefix.length())
                        .replaceAll("[\"']", "") // tırnakları temizle
                        .trim();
            }
        } catch (Exception ignored) {}
        return "bilinmeyen-servis";
    }



}
