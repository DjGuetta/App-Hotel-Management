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

import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    private const val CONNECTION_DATABASE = "mongodb://192.168.1.5:27017"
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
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")
        println("room db $document")

        return document
    }
    fun getRooms(hotelId: String?): List<Document> {

        if (hotelId.isNullOrBlank()) return emptyList()

        val collection = database.getCollection("Rooms")
        val id = ObjectId(hotelId) // convert string to ObjectId
        val rooms = collection.find(eq("hotelId", id)).toList() // corrected field name

        println("cuartos db: $rooms")
        return rooms
    }

    fun getRoomsUnderTheirPrices(minimunPrice: Int, maximunPrice: Int): List<Document> {
        val collection = database.getCollection("Rooms") // Rooms collection
        val collectionrooms =collection.find(
            and(
                gte("pricePerNight", minimunPrice),              // price >= minimumPrice
                lte("pricePerNight", maximunPrice)               // price <= maximumPrice
            )
        ).toList()
        return collectionrooms
    }

//    fun getDocuments(name: String?): List<Document> {
//        val collection = database.getCollection("Hoteles")
//        return collection.find(eq("name", name)).toList()
//    }
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

    fun getHotels(): Flow<List<Hotel>> = flow {
        try {
            val collection = database.getCollection("Hoteles")
            val hotels = collection.find().map { document ->
                documentToHotel(document)
            }.toList()
            emit(hotels)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo hoteles: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun getAllRooms(): Flow<List<Room>> = flow {
        try {
            val collection = database.getCollection("Rooms")
            val rooms = collection.find().map { document ->
                Room.fromDocument(document)
            }.toList()
            emit(rooms)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo habitaciones: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun getRoomsByHotel(hotelId: ObjectId): Flow<List<Room>> = flow {
        try {
            val collection = database.getCollection("Rooms")
            val rooms = collection.find(Filters.eq("hotelId", hotelId))
                .map { document -> Room.fromDocument(document) }
                .toList()
            emit(rooms)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo habitaciones del hotel: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // --- FUNCIONES DE ESCRITURA ---
    suspend fun insertHotel(document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            val result = collection.insertOne(document)
            result.wasAcknowledged() || result.insertedId != null
        } catch (e: Exception) {
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
            val result = collection.replaceOne(Filters.eq("_id", id), document)
            result.modifiedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando hotel: ${e.message}")
            false
        }
    }

    // Función segura para eliminar hotel
    suspend fun deleteHotel(id: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            val result = collection.deleteOne(Filters.eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando hotel: ${e.message}")
            false
        }
    }

    suspend fun insertRoom(document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Rooms")
            val result = collection.insertOne(document)
            result.wasAcknowledged() || result.insertedId != null
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando habitación: ${e.message}")
            false
        }
    }

    suspend fun updateRoom(id: ObjectId, document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Rooms")
            val result = collection.replaceOne(Filters.eq("_id", id), document)
            result.modifiedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando habitación: ${e.message}")
            false
        }
    }

    suspend fun updateRoomStatus(roomId: ObjectId, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Rooms")
            val filter = Filters.eq("_id", roomId)
            val update = Document("\$set", Document("status", newStatus))
            val result = collection.updateOne(filter, update)
            result.modifiedCount == 1L
        } catch (e: Exception) {
            Log.e("Database", "Error al actualizar el estado de la habitación: ${e.message}", e)
            false
        }
    }

    suspend fun deleteRoom(id: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Rooms")
            val result = collection.deleteOne(Filters.eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando habitación: ${e.message}")
            false
        }
    }

    // --- FUNCIONES COMBINADAS PARA ESTADÍSTICAS ---
    fun getHotelsWithRoomStats(): Flow<List<Pair<Hotel, RoomStats>>> = flow {
        try {
            // Obtener hoteles y habitaciones de manera síncrona
            val hotels = getHotelsSync()
            val allRooms = getAllRoomsSync()

            val hotelsWithStats = hotels.map { hotel ->
                val hotelRooms = allRooms.filter { it.hotelId == hotel._id }
                val stats = hotel.getRoomStats(hotelRooms)
                hotel to stats
            }

            emit(hotelsWithStats)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo hoteles con estadísticas: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun getHotelWithRoomStats(hotelId: ObjectId): Flow<Pair<Hotel, RoomStats>?> = flow {
        try {
            val hotel = getHotelByIdSync(hotelId)
            val rooms = getRoomsByHotelSync(hotelId)

            if (hotel != null) {
                val stats = hotel.getRoomStats(rooms)
                emit(hotel to stats)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo hotel con estadísticas: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    // --- FUNCIONES SÍNCRONAS DE RESPALDO ---
    private fun getHotelsSync(): List<Hotel> {
        return try {
            val collection = database.getCollection("Hoteles")
            collection.find().map { document ->
                documentToHotel(document)
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error síncrono obteniendo hoteles: ${e.message}")
            emptyList()
        }
    }

    private fun getAllRoomsSync(): List<Room> {
        return try {
            val collection = database.getCollection("Rooms")
            collection.find().map { document ->
                Room.fromDocument(document)
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error síncrono obteniendo habitaciones: ${e.message}")
            emptyList()
        }
    }

    private fun getRoomsByHotelSync(hotelId: ObjectId): List<Room> {
        return try {
            val collection = database.getCollection("Rooms")
            collection.find(Filters.eq("hotelId", hotelId))
                .map { document -> Room.fromDocument(document) }
                .toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error síncrono obteniendo habitaciones del hotel: ${e.message}")
            emptyList()
        }
    }

    private fun getHotelByIdSync(hotelId: ObjectId): Hotel? {
        return try {
            val collection = database.getCollection("Hoteles")
            val document = collection.find(Filters.eq("_id", hotelId)).firstOrNull()
            document?.let { documentToHotel(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error síncrono obteniendo hotel por ID: ${e.message}")
            null
        }
    }

    // --- FUNCIÓN DE CONVERSIÓN DE DOCUMENTO A HOTEL ---
    private fun documentToHotel(document: Document): Hotel {
        return try {
            Hotel(
                _id = document.getObjectId("_id"),
                name = document.getString("name") ?: "",
                address = document.getString("address") ?: "",
                city = document.getString("city") ?: "",
                state = document.getString("state") ?: "Táchira",
                location = getLocationFromDocument(document),
                description = document.getString("description") ?: "",
                amenities = document.getList("amenities", String::class.java) ?: emptyList(),
                contactEmail = document.getString("contactEmail") ?: "",
                contactPhone = document.getString("contactPhone") ?: "",
                images = document.getList("images", String::class.java) ?: emptyList(),
                isActive = document.getBoolean("isActive") ?: true,
                roomCount = document.getInteger("roomCount") ?: 0,
                availableRooms = document.getInteger("availableRooms")
                    ?: document.getInteger("roomCount") ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a hotel: ${e.message}")
            Hotel(
                name = "Error cargando hotel",
                description = "No se pudieron cargar los datos del hotel"
            )
        }
    }

    private fun getLocationFromDocument(document: Document): Location {
        return try {
            val locationDoc = document.get("location") as? Document

            if (locationDoc != null) {
                val rawCoordinates = locationDoc.getList("coordinates", Number::class.java)

                if (rawCoordinates != null && rawCoordinates.size >= 2) {
                    val coordinates = rawCoordinates.map { it.toDouble() }

                    Location(
                        type = locationDoc.getString("type") ?: "Point",
                        coordinates = coordinates
                    )
                } else {
                    Log.w(TAG, "Coordenadas no encontradas o incompletas en el documento.")
                    Location()
                }
            } else {
                Log.w(TAG, "Campo 'location' no encontrado en el documento.")
                Location()
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error crítico analizando location: ${e.message}", e)
            Location()
        }
    }
    // --- FUNCIONES PARA RESERVAS (BOOKINGS) ---

    fun getActiveBookingsByHotel(hotelId: ObjectId): Flow<List<Booking>> = flow {
        try {
            val collection = database.getCollection("Bookings")
            // Buscamos reservas que no estén finalizadas o canceladas
            val activeStatuses = listOf("CONFIRMED", "CHECKED_IN")
            val bookings = collection.find(
                Filters.and(
                    Filters.eq("hotelId", hotelId),
                    Filters.`in`("status", activeStatuses)
                )
            ).map { doc -> Booking.fromDocument(doc) }.toList()
            emit(bookings)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo reservas activas: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun insertBooking(booking: Booking): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Bookings")
            collection.insertOne(booking.toDocument())
            // Aquí también se debería actualizar el estado de la habitación a "OCUPADA"
            updateRoomStatus(booking.roomId, "OCUPADA")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando reserva: ${e.message}")
            false
        }
    }

    suspend fun updateBookingStatus(bookingId: ObjectId, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Bookings")
            val filter = Filters.eq("_id", bookingId)
            val update = Document("\$set", Document("status", newStatus))
            val result = collection.updateOne(filter, update)
            // Si la reserva se completa o cancela, liberar la habitación
            if (newStatus == "CHECKED_OUT" || newStatus == "CANCELLED") {
                val booking = collection.find(filter).first()?.let { Booking.fromDocument(it) }
                booking?.let {
                    updateRoomStatus(it.roomId, "DISPONIBLE") // o "LIMPIEZA"
                }
            }
            result.modifiedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando estado de reserva: ${e.message}")
            false
        }
    }
    // --- FUNCIÓN PARA LA PANTALLA GLOBAL DE RESERVAS ---

    fun getAllActiveBookingDetails(): Flow<List<BookingDetails>> = flow {
        try {
            Log.d(TAG, "Obteniendo TODAS las reservas activas...")
            // 1. Obtener todas las reservas activas
            val collection = database.getCollection("Bookings")
            val activeStatuses = listOf("CONFIRMED", "CHECKED_IN")
            val activeBookings = collection.find(Filters.`in`("status", activeStatuses))
                .map { Booking.fromDocument(it) }
                .toList()

            if (activeBookings.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // 2. Obtener los IDs únicos de hoteles y habitaciones de esas reservas
            val hotelIds = activeBookings.map { it.hotelId }.distinct()
            val roomIds = activeBookings.map { it.roomId }.distinct()

            // 3. Obtener los documentos de esos hoteles y habitaciones en una sola consulta
            val hotels = database.getCollection("Hoteles")
                .find(Filters.`in`("_id", hotelIds))
                .map { it.getObjectId("_id") to it.getString("name") }
                .toList().toMap()

            val rooms = database.getCollection("Rooms")
                .find(Filters.`in`("_id", roomIds))
                .map { it.getObjectId("_id") to it.getString("roomNumber") }
                .toList().toMap()

            // 4. Combinar todo en una lista de BookingDetails
            val bookingDetails = activeBookings.mapNotNull { booking ->
                val hotelName = hotels[booking.hotelId]
                val roomNumber = rooms[booking.roomId]
                if (hotelName != null && roomNumber != null) {
                    BookingDetails(
                        booking = booking,
                        hotelName = hotelName,
                        roomNumber = "Hab. $roomNumber"
                    )
                } else {
                    null // Ignorar si no se encuentra el hotel o la habitación
                }
            }

            Log.d(TAG, "Se encontraron ${bookingDetails.size} detalles de reservas.")
            emit(bookingDetails)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo detalles de reservas globales: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}
