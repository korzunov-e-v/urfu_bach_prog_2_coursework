package mongo;

import com.mongodb.MongoException;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import com.mongodb.client.result.InsertOneResult;
import java.util.Date;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

public class MongoUtil {

    private static MongoClient mongoClient;
    private static final String connectionUrl = "mongodb://root:example@cw2-mongo:27017";
    private static final String databaseName = "Prices";
    private static final String collectionName = "PricesSeries";

    private static MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(connectionUrl);
        }
        return mongoClient;
    }

    private static MongoDatabase getMongoDatabase() {
        return getMongoClient().getDatabase(databaseName);
    }

    private static MongoCollection<Document> getCollection() {
        if (!collectionExists(collectionName)) {
            createCollection(collectionName);
        }
        return getMongoDatabase().getCollection(collectionName);
    }

    private static boolean collectionExists(final String collectionName) {
        ListCollectionNamesIterable it = getMongoDatabase().listCollectionNames();
        for (final String name : it) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    private static void createCollection(String collectionName) {
        Bson price = Filters.type("price", BsonType.DOUBLE);
        Bson validator = Filters.and(price);
        ValidationOptions validationOptions = new ValidationOptions().validator(validator);
        getMongoDatabase().createCollection(collectionName, new CreateCollectionOptions().validationOptions(validationOptions));
    }

    public static InsertOneResult addRecord(long productId, String status, double price) {
        MongoCollection<Document> collection = getCollection();
        Document document = new Document();
        document.put("product_id", productId);
        document.put("status", status);
        document.put("price", price);
        document.put("created_at", new Date());
        return collection.insertOne(document);
    }

    public static Double getCurrentPrice(long productId) {
        MongoCollection<Document> collection = getCollection();

        Document doc = collection.find(Filters.eq("product_id", productId))
                .sort(new Document("created_at", -1)).first();

        if (doc != null){
            return doc.getDouble("price");
        } else {
            return null;
        }
    }

    public static Double getMinPrice(long productId) {
        MongoCollection<Document> collection = getCollection();

        Document doc = collection.find(Filters.eq("product_id", productId))
                .sort(new Document("price", 1)).first();

        if (doc != null){
            return doc.getDouble("price");
        } else {
            return null;
        }
    }

    public static Double getMaxPrice(long productId) {
        MongoCollection<Document> collection = getCollection();

        Document doc = collection.find(Filters.eq("product_id", productId))
                .sort(new Document("price", -1)).first();

        if (doc != null){
            return doc.getDouble("price");
        } else {
            return null;
        }
    }

    public static void resetProduct(long productId) {
        MongoCollection<Document> collection = getCollection();

        Bson query = eq("product_id", productId);

        try {
            collection.deleteMany(query);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

}
