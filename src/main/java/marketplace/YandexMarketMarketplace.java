package marketplace;

import bot.Constants;
import database.models.Product.MarketplaceEnum;
import java.io.IOException;
import marketplace.exceptions.MarketplaceException;
import marketplace.exceptions.NoProductException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YandexMarketMarketplace extends Marketplace {

    private final Pattern patternUrl = Pattern.compile("^(https?://)?(www\\.market.yandex\\.ru/).*$");
    private final Pattern patternPrice = Pattern.compile("(?<price>\\d+)");

    YandexMarketMarketplace(String productUrl) throws MarketplaceException {
        super("Yandex Market", "https://www.market.yandex.ru/", MarketplaceEnum.YANDEXMARKET, productUrl);
    }

    @Override
    protected Document loadDoc() throws MarketplaceException {
        try {
            Connection connection = Jsoup.connect(productUrl);
            connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 YaBrowser/24.1.0.0 Safari/537.36");
            connection.header("Accept-Language", "en-US,en;q=0.5");
            connection.header("cookie", Constants.YA_COOKIES);
            return connection.get();
        } catch (IOException e) {
            throw new MarketplaceException();
        }
    }

    @Override
    public double getPrice() throws MarketplaceException {
        double price;
        Element priceDiv = getDoc().getElementsByAttributeValue("data-auto", "snippet-price-current").first();

        if (priceDiv == null) {
            throw new NoProductException();
        }

        String priceText = priceDiv.text().replaceAll(" ", "");
        Matcher priceMatcher = patternPrice.matcher(priceText);
        priceMatcher.find();
        price = Double.parseDouble(priceMatcher.group("price"));
        return price;
    }

    @Override
    public String getProductName() throws MarketplaceException {
        // todo check and throw
        String productName = null;
        Element productNameDiv = getDoc().getElementsByAttributeValue("data-auto", "productCardTitle").first();
        productName = productNameDiv.text();
        return productName;
    }
}
