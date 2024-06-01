package mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import java.util.Date;
import org.bson.Document;

public class MongoUtil {

    private static MongoClient mongoClient;

    private static MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create("mongodb://root:example@localhost:27017");
        }
        return mongoClient;
    }

    private static MongoDatabase getMongoDatabase() {
        return getMongoClient().getDatabase("Prices");
    }

    private static MongoCollection<Document> getCollection() {
        return getMongoDatabase().getCollection("PricesSeries");
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
}
