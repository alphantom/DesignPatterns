package ChainOfResponsibility;

import java.security.MessageDigest;
import java.util.Base64;

public class Main {
    /**
     * It represents a handler and has two methods: one for handling requests and other for combining handlers
     */
    @FunctionalInterface
    interface RequestHandler {
        public Request handle(Request request);
        default RequestHandler combine(RequestHandler otherHandler) {
            return (request) -> otherHandler.handle(this.handle(request));
        }
    }

    /**
     * Accepts a request and returns new request with data wrapped in the tag <transaction>...</transaction>
     */
    final static RequestHandler wrapInTransactionTag =
            (req) -> new Request(String.format("<transaction>%s</transaction>", req.getData()));

    /**
     * Accepts a request and returns a new request with calculated digest inside the tag <digest>...</digest>
     */
    final static RequestHandler createDigest =
            (req) -> {
                String digest = "";
                try {
                    final MessageDigest md5 = MessageDigest.getInstance("MD5");
                    final byte[] digestBytes = md5.digest(req.getData().getBytes("UTF-8"));
                    digest = new String(Base64.getEncoder().encode(digestBytes));
                } catch (Exception ignored) { }
                return new Request(req.getData() + String.format("<digest>%s</digest>", digest));
            };

    /**
     * Accepts a request and returns a new request with data wrapped in the tag <request>...</request>
     */
    final static RequestHandler wrapInRequestTag =
            (req) -> new Request(String.format("<request>%s</request>", req.getData()));

    /**
     * It should represents a chain of responsibility combined from another handlers.
     * Result looks like <request><transaction>test</transaction><digest>MIOv6aGZeqU/nLDCrywDqA==</digest></request>
     */
    final static RequestHandler commonRequestHandler = (wrapInTransactionTag.combine(createDigest)).combine(wrapInRequestTag);
    /**
     * Immutable class for representing requests.
     * If you need to change the request data then create new request.
     */
    static class Request {
        private final String data;

        public Request(String requestData) {
            this.data = requestData;
        }

        public String getData() {
            return data;
        }
    }

    public static void main(String[] args) {
        Request request = new Request("test");
        System.out.println(wrapInRequestTag.handle(request).getData());
        System.out.println(wrapInTransactionTag.handle(request).getData());
        System.out.println(createDigest.handle(request).getData());
        System.out.println(commonRequestHandler.handle(request).getData());
    }
}
