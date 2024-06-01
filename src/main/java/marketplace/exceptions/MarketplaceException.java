package marketplace.exceptions;

public class MarketplaceException extends Exception {
    public MarketplaceException() {
    }

    public MarketplaceException(String message) {
        super(message);
    }

    public MarketplaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MarketplaceException(Throwable cause) {
        super(cause);
    }
}
