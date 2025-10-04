package com.example.hoteru.model

import android.content.ContentValues.TAG
import android.util.Log
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.lte

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mongodb.client.model.Filters.eq
import kotlin.text.set


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
    private const val CONNECTION_DATABASE = "mongodb://192.168.1.5:27017"


    /**
     * Name of the MongoDB database used in this project.
     */
    private const val DATABASE_NAME = "Hoteru"

    private val TAG = "MongoDBConnection"

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
    fun getRooms(hotelId: String?): List<Document> {
        val collection = database.getCollection("Rooms") // Rooms collection
        val id = ObjectId(hotelId)
        return collection.find(eq("hotel_id", id)).toList()
    }
    fun getRoomsUnderTheirPrices(minimunPrice: Double, maximunPrice: Double): List<Document> {
        val collection = database.getCollection("Rooms") // Rooms collection
        return collection.find(
            and(
                gte("price", minimunPrice),              // price >= minimumPrice
                lte("price", maximunPrice)               // price <= maximumPrice
            )
        ).toList()
    }

    fun getDocuments(name: String?): List<Document> {
        val collection = database.getCollection("Hoteles")
        return collection.find(eq("name", name)).toList()
    }
    fun getHotelsUnderRating(rating: Double): List<Document>{
        val collection = database.getCollection("Hoteles") // Rooms collection
        return collection.find(eq("rating", rating)).toList()
    }
    fun insertUserDb(user: User, password: String): Boolean {
        val usersCollection: MongoCollection<Document> = database.getCollection("users")

        // Check if email already exists
        val existing = usersCollection.find(Document("email", user.email)).firstOrNull()
        if (existing != null) {
            println("Email already registered")
            return false
        }

        // Convert User object to Document
        val doc = Document(mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "password" to password,  // For real apps, hash this!
            "phone" to user.phone,
//            "userType" to user.userType.name,
            "hotelId" to user.hotelId,
            "createdAt" to user.createdAt
        ))

        // Insert into collection
        usersCollection.insertOne(doc)
        println("User inserted successfully: ${user.email}")
        return true
    }

    fun consultUserDb(email: String, password: String): Boolean {
        val usersCollection: MongoCollection<Document> = database.getCollection("users")

        // Find the user with matching email and password
        val user = usersCollection.find(
            and(
                eq("email", email),
                eq("password", password) // In production, compare hashed password
            )
        ).firstOrNull()

        return user != null
    }

//    fun getDocuments(name: String?): List<Document> {
//        val collection = database.getCollection("Hotels")
//        return collection.find(eq("name", name)).toList()
//    }

    // Función segura para corrutinas para obtener todos los hoteles
    suspend fun getAllHotels(): List<Document> = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            collection.find().toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Función segura para insertar hotel
    suspend fun insertHotel(document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando INSERT en MongoDB...")
            val collection = database.getCollection("Hoteles")

            //Asegura que tenga campos requeridos
            if (!document.containsKey("createdAt")) {
                document["createdAt"] = System.currentTimeMillis()
            }

            if (!document.containsKey("isActive")) {
                document["isActive"] = true
            }

            if (!document.containsKey("availableRooms")) {
                val roomCount = document.getInteger("roomCount", 0)
                document["Cuartos disponibles"] = roomCount //Todas disponibles por defecto
            }

            Log.d(TAG, "Insertando documento: ${document.getString("name")}")
            Log.d(TAG, "📄 Documento completo: ${document.toJson()}")

            val result = collection.insertOne(document)

            //Mejor detección de éxito
            val sucess = result.wasAcknowledged() || result.insertedId != null

            Log.d(TAG, "Resultado de la inserción:")
            Log.d(TAG, "  - Acknowledged: ${result.wasAcknowledged()}")
            Log.d(TAG, "  - InsertedId: ${result.insertedId}")
            Log.d(TAG, "  - Éxito detectado: $sucess")

            if(!result.wasAcknowledged() && result.insertedId != null) {
                Log.d(TAG, "Hotel insertado sin 'acknowleged', pero con ID - considerando como éxito ")
            }

            sucess

            }catch(e: Exception){
                Log.e(TAG, "Error insertando hotel: ${e.message}")
                false
                }
            }

    /*
            Log.d(TAG, "Documento a insertar (sin _id): ${document.toJson()}")
            val result = collection.insertOne(document)
            Log.d(TAG, "Resultado de la inserción: ${result.wasAcknowledged()}")

            if (!result.wasAcknowledged()) {
                Log.d(TAG, "Hotel insertado correctamente, ID generado: ${result.insertedId}")
                true
            } else {
                Log.d(TAG, "Inserción no fue admitida")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error insetando hotel: ${e.message}",e)
            false
        }
    }
    */


    // Función segura para actualizar hotel
    suspend fun updateHotel(id: ObjectId, document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            val result = collection.replaceOne(eq("_id", id), document)
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    // Función segura para eliminar hotel
    suspend fun deleteHotel(id: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            val result = collection.deleteOne(eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}
