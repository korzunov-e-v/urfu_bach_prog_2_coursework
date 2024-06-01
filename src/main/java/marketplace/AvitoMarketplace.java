package marketplace;

import database.models.Product.MarketplaceEnum;
import marketplace.exceptions.MarketplaceException;
import marketplace.exceptions.UnexpectedUrlException;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvitoMarketplace extends Marketplace {

    private final Pattern patternUrl = Pattern.compile("^(https?://)?(www\\.avito\\.ru/).*$");
    private final Pattern patternPrice = Pattern.compile("^(?<price>\\d*)\\w*");

    AvitoMarketplace(String productUrl) throws MarketplaceException {
        super("Avito", "https://www.avito.ru/", MarketplaceEnum.AVITO, productUrl);
    }

    @Override
    public double getPrice() throws MarketplaceException {
        double price;
        Element priceDiv = getDoc().getElementsByAttributeValue("itemprop", "price").first();
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
        Element productNameDiv = getDoc().getElementsByAttributeValue("itemprop", "name").first();
        productName = productNameDiv.text();
        return productName;
    }
}
