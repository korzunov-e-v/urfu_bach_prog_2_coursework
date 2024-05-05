package marketplace;

import database.models.Product.MarketplaceEnum;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OzonMarketplace extends Marketplace {

    private final Pattern pattern = Pattern.compile("^(https?://)?(www\\.ozon\\.ru/product/)(?<productId>[^/]*)(/)?$");

    OzonMarketplace() {
        super("Ozon", "https://www.ozon.ru/", MarketplaceEnum.OZON);
    }

    @Override
    public String getProductId(String productUrl) {
        Matcher matcher = pattern.matcher(productUrl);
        if (matcher.find()) {
            return matcher.group("productId");
        } else {
            return null;
        }
    }

    public ValidationStatus validateProductUrl(String productUrl) {
        Matcher matcher = pattern.matcher(productUrl);

        if (!matcher.find()) {
            return ValidationStatus.UNEXPECTED_URL;
        }

        int status;
        String response;
        try {
            URL url = new URL(productUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(true);
            response = con.getResponseMessage();
            status = con.getResponseCode();
        } catch (Exception e) {
            return ValidationStatus.ERROR;
        }
        if (status < 399) {
            return ValidationStatus.OK;
        } else if (status == 404) {
            return ValidationStatus.NO_PRODUCT;
        } else {
            System.out.println("Error fetching product url. Status " + status + ", response: " + response);
            return ValidationStatus.ERROR;
        }

    }
}
