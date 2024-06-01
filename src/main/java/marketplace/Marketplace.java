package marketplace;

import database.models.Product.MarketplaceEnum;
import marketplace.exceptions.MarketplaceException;
import marketplace.exceptions.UnexpectedMarketplaceException;
import marketplace.exceptions.UnexpectedUrlException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Marketplace {
    private final String name;
    private final String baseUrl;
    private final MarketplaceEnum marketplaceType;
    private final String productUrl;
    private final Document doc;

    protected Marketplace(String name, String baseUrl, MarketplaceEnum marketplaceType, String productUrl) throws MarketplaceException {
        this.name = name;
        this.baseUrl = baseUrl;
        this.marketplaceType = marketplaceType;
        this.productUrl = productUrl;
        this.doc = loadDoc();
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

    public String getProductUrl() {
        return productUrl;
    }

    public Document getDoc() {
        return doc;
    }

    private Document loadDoc() throws MarketplaceException {
        try {
            // todo http 423
            Connection connection = Jsoup.connect(productUrl);
            connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 YaBrowser/24.1.0.0 Safari/537.36");
            connection.header("Accept-Language", "en-US,en;q=0.5");
//            connection.header("Accept-Encoding", "gzip, deflate, br");
            return connection.get();
        } catch (IOException e) {
            throw new MarketplaceException();
        }
    }

    public abstract double getPrice() throws MarketplaceException;

    public abstract String getProductName() throws MarketplaceException;

    public static Marketplace getInstance(String productUrl) throws MarketplaceException {
        try {
            URL url = new URL(productUrl);
            switch (url.getHost()) {
                case "avito.ru" -> {
                    return new AvitoMarketplace(productUrl);
                }
                case "market.yandex.ru" -> {
                    return new YandexMarketMarketplace(productUrl);
                }
                default -> {
                    throw new UnexpectedMarketplaceException();
                }
            }
        } catch (MalformedURLException e) {
            throw new UnexpectedUrlException();
        }
    }
}
