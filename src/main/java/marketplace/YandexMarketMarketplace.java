package marketplace;

import database.models.Product.MarketplaceEnum;
import marketplace.exceptions.MarketplaceException;
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
    public double getPrice() throws MarketplaceException {
        double price;
        Element priceDiv = getDoc().getElementsByAttributeValue("data-auto", "snippet-price-current").first();
        // todo check and throw
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
