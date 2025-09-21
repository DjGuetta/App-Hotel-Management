package com.example.hoteru.model

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters.eq
/**
 * Singleton object responsible for managing the MongoDB connection in the application.
 *
 * This object connects to a MongoDB database using the MongoDB Java driver.
 * It exposes methods to retrieve specific collections from the database.
 *
 * ## Key responsibilities:
 * - Maintain a single MongoDB client connection for the whole app.
 * - Provide access to a specific database.
 * - Allow fetching any collection by its name.
 *
 * @see MongoClient for creating a MongoDB client.
 * @see MongoDatabase for interacting with a MongoDB database.
 * @see MongoCollection for working with a MongoDB collection.
 */
object MongoDBConnection {

    /**
     * MongoDB connection string pointing to the database server.
     *
     * `10.0.2.2` is the special IP address to access the host machine from the Android Emulator.
     * The default MongoDB port `27017` is used.
     */
    private const val CONNECTION_DATABASE = "mongodb://10.0.2.2:27017"

    /**
     * Name of the MongoDB database used in this project.
     */
    private const val DATABASE_NAME = "Hoteru"

    /**
     * MongoDB client instance used to communicate with the database server.
     */
    private val client: MongoClient = MongoClients.create(CONNECTION_DATABASE)

    /**
     * MongoDB database instance representing the `Hoteru` database.
     */
    val database: MongoDatabase = client.getDatabase(DATABASE_NAME)

    /**
     * Retrieves a MongoDB collection by its name.
     *
     * @param collectionName The name of the collection to retrieve.
     * @return A [MongoCollection] of BSON [Document]s representing the collection.
     */
    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }
    fun oneDocument(collectionName: String, idDocument: String?): Document? {
        val collection = database.getCollection(collectionName)
        val id = ObjectId(idDocument)
        val document = collection.find(eq("_id", id)).firstOrNull()
        return document
    }
    fun getDocuments(name: String?): List<Document> {
        val collection = database.getCollection("Hotel")
        return collection.find(eq("name", name)).toList()
    }
}