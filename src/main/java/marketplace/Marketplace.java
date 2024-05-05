package marketplace;

import database.models.Product.MarketplaceEnum;
import java.net.URL;

public abstract class Marketplace {
    private final String name;
    private final String baseUrl;
    private final MarketplaceEnum marketplaceType;

    protected Marketplace(String name, String baseUrl, MarketplaceEnum marketplaceType) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.marketplaceType = marketplaceType;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public MarketplaceEnum getMarketplaceType() {
        return marketplaceType;
    }

    public abstract String getProductId(String productUrl);

    public abstract ValidationStatus validateProductUrl(String productUrl);

    public enum ValidationStatus {
        OK,
        UNEXPECTED_URL,
        NO_PRODUCT,
        ERROR,
    }

    public static Marketplace getInstance(String productUrl) {
        try {
            URL url = new URL(productUrl);

            switch (url.getHost()) {
                case "www.ozon.ru" -> {
                    return new OzonMarketplace();
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
}
