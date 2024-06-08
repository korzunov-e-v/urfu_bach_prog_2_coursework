package mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import com.mongodb.client.result.InsertOneResult;
import java.util.Date;
import java.util.Objects;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoUtil {

    private static MongoClient mongoClient;
    private static final String databaseName = "Prices";
    private static final String collectionName = "PricesSeries";

    private static MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create("mongodb://root:example@localhost:27017");
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

        Document a = collection.find(Filters.eq("product_id", productId))
                .sort(new Document("creation_at", -1)).first();

        if (a != null){
            return a.getDouble("price");
        } else {
            return null;
        }
    }

    public static double getMinPrice(long productId) {
        MongoCollection<Document> collection = getCollection();

        return Objects.requireNonNull(collection.find(Filters.eq("product_id", productId))
                .sort(new Document("price", 1))
                .first()).getDouble("price");
    }

    public static double getMaxPrice(long productId) {
        MongoCollection<Document> collection = getCollection();

        return Objects.requireNonNull(collection.find(Filters.eq("product_id", productId))
                .sort(new Document("price", -1))
                .first()).getDouble("price");
    }

}
