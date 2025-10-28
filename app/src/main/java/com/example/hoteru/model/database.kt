package com.example.hoteru.model

import android.util.Log
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
import com.mongodb.client.model.Sorts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.mindrot.jbcrypt.BCrypt
import java.util.Calendar


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

//    private const val CONNECTION_DATABASE = "mongodb://172.18.26.188:27017"
    private const val CONNECTION_DATABASE = "mongodb://192.168.1.7:27017"

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
    fun insertCommentDb(comment: Comment): Boolean {
        val commentsCollection: MongoCollection<Document> = database.getCollection("Comments")
        return try {
            // Convert Comment object to Document
            val doc = Document(mapOf(
                "hotelId" to comment.hotelId,
                "userName" to (comment.userName ?: "Anónimo"),
                "commentText" to comment.commentText,
                "timestamp" to comment.timestamp
            ))

            // Insert into collection
            commentsCollection.insertOne(doc)
            println("✅ Comment inserted successfully for hotelId: ${comment.hotelId}")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getComments(hotelId: String?): List<Comment> {
        val collection: MongoCollection<Document> = database.getCollection("Comments")
        return collection.find(eq("hotelId", hotelId))
            .sort(Sorts.descending("timestamp"))
            .map { doc ->
                Comment(
                    hotelId = doc.getString("hotelId"),
                    userName = doc.getString("userName") ?: "Unknown",
                    commentText = doc.getString("commentText"),
                    timestamp = doc.getDate("timestamp") ?: Date()
                )
            }.toList()
    }
    suspend fun insertUser(user: User, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userCollection = database.getCollection("Users")

            val existingUser = userCollection.find(Filters.eq("email", user.email.lowercase())).first()
            if (existingUser != null) {
                Log.w(TAG, "Intento de registrar un email que ya existe: ${user.email}")
                return@withContext false
            }

            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

            // Usamos la nueva función de extensión para un código más limpio
            val userDocument = user.toDocument(hashedPassword)

            val result = userCollection.insertOne(userDocument)
            Log.d(TAG, "Usuario insertado con éxito: ${result.insertedId}")
            result.wasAcknowledged()
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando usuario: ${e.message}", e)
            false
        }
    }

    private fun documentToUser(doc: Document): User {
        val userTypeString = doc.getString("userType")
        val userType = when (userTypeString) {
            UserType.Admin.value -> UserType.Admin
            UserType.RegisteredUser.value -> UserType.RegisteredUser
            else -> UserType.Guest
        }
        return User(
            id = doc.getString("userId"),
            email = doc.getString("email"),
            firstName = doc.getString("firstName"), // Leemos el nuevo campo
            lastName = doc.getString("lastName"),   // Leemos el nuevo campo
            userType = userType,
            phone = doc.getString("phone") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L,
            hotelId = doc.getString("hotelId")
        )
    }

    /**
     * Busca un usuario por su email y verifica su contraseña.
     * Devuelve el objeto User si las credenciales son correctas, o null si no lo son.
     */
    suspend fun findUserByCredentials(email: String, password: String): User? = withContext(Dispatchers.IO) {
        try {
            val userCollection = database.getCollection("Users")
            val userDocument = userCollection.find(Filters.eq("email", email.lowercase())).first()
                ?: return@withContext null // Si no se encuentra el email, devuelve nulo

            val hashedPassword = userDocument.getString("passwordHash")

            // Comparamos la contraseña ingresada con el hash guardado
            if (BCrypt.checkpw(password, hashedPassword)) {
                // Si la contraseña es correcta, construimos y devolvemos el objeto User
                Log.d(TAG, "Login exitoso para el usuario: $email")
                documentToUser(userDocument)
            } else {
                Log.w(TAG, "Contraseña incorrecta para el usuario: $email")
                null // Contraseña incorrecta
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error consultando usuario: ${e.message}", e)
            null
        }
    }
    suspend fun findUserById(userId: String?): User? = withContext(Dispatchers.IO) {
        try {
            val userCollection = database.getCollection("Users")
            val userDocument = userCollection.find(Filters.eq("userId", userId)).first()
                ?: return@withContext null // Si no se encuentra el usuario, devuelve nulo

            Log.d(TAG, "Usuario encontrado con id: $userId")
            documentToUser(userDocument)
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando usuario por id: ${e.message}", e)
            null
        }
    }
    suspend fun findBookingsByUserId(userId: String?): List<Booking> = withContext(Dispatchers.IO) {
        if (userId.isNullOrBlank()) return@withContext emptyList() // no valid userId provided

        try {
            val bookingCollection = database.getCollection("Bookings")
            val bookings = bookingCollection.find(Filters.eq("userId", userId))
                .sort(Sorts.descending("createdAt")) // ✅ sort by newest booking first
                .map { Booking.fromDocument(it) }
                .toList()

            if (bookings.isEmpty()) {
                Log.d(TAG, "No se encontraron reservas para el usuario con id: $userId")
            } else {
                Log.d(
                    TAG,
                    "Se encontraron ${bookings.size} reservas para el usuario con id: $userId"
                )
            }

            bookings
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando reservas por id: ${e.message}", e)
            emptyList()
        }
    }




        // --- FUNCIONES REACTIVAS SIMPLIFICADAS CON FLOWS ---

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

    fun getAllRoomDetails(): Flow<List<RoomDetails>> = flow {
        try {
            Log.d(TAG, "Obteniendo TODAS las habitaciones con detalles...")

            // 1. Obtener todas las habitaciones
            val allRooms = database.getCollection("Rooms")
                .find()
                .map { Room.fromDocument(it) }
                .toList()

            if (allRooms.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // 2. Obtener los IDs únicos de los hoteles de esas habitaciones
            val hotelIds = allRooms.map { it.hotelId }.distinct()

            // 3. Obtener los nombres de esos hoteles en una sola consulta
            val hotelsMap = database.getCollection("Hoteles")
                .find(Filters.`in`("_id", hotelIds))
                .map { it.getObjectId("_id") to it.getString("name") }
                .toList().toMap()

            // 4. Combinar la información
            val roomDetails = allRooms.mapNotNull { room ->
                val hotelName = hotelsMap[room.hotelId]
                if (hotelName != null) {
                    RoomDetails(room = room, hotelName = hotelName)
                } else {
                    // Si una habitación tiene un hotelId que no existe, la ignoramos.
                    Log.w(TAG, "No se encontró el hotel con ID: ${room.hotelId} para la habitación ${room._id}")
                    null
                }
            }

            Log.d(TAG, "Se encontraron ${roomDetails.size} detalles de habitaciones.")
            emit(roomDetails)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo detalles de habitaciones: ${e.message}")
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

    suspend fun updateHotel(id: ObjectId, document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Hoteles")
            val updateOperation = Document("\$set", document)
            val result = collection.updateOne(Filters.eq("_id", id), updateOperation)
            result.modifiedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando hotel: ${e.message}")
            false
        }
    }

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
    suspend fun deleteRoomsByHotelId(hotelId: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Rooms")
            val filter = Filters.eq("hotelId", hotelId)
            val result = collection.deleteMany(filter)
            Log.d(TAG, "Eliminación en cascada: Se eliminaron ${result.deletedCount} habitaciones para el hotel $hotelId")
            result.wasAcknowledged()
        } catch (e: Exception) {
            Log.e(TAG, "Error en cascada eliminando habitaciones: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina todas las reservas que pertenecen a un hotel específico.
     */
    suspend fun deleteBookingsByHotelId(hotelId: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Bookings")
            val filter = Filters.eq("hotelId", hotelId)
            val result = collection.deleteMany(filter)
            Log.d(TAG, "Eliminación en cascada: Se eliminaron ${result.deletedCount} reservas para el hotel $hotelId")
            result.wasAcknowledged()
        } catch (e: Exception) {
            Log.e(TAG, "Error en cascada eliminando reservas: ${e.message}", e)
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
            val updateOperation = Document("\$set", document)
            val result = collection.updateOne(Filters.eq("_id", id), updateOperation)
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

    suspend fun getHotelsByAdminId(adminId: String): List<Hotel> = withContext(Dispatchers.IO) {
        if (adminId.isBlank()) {
            return@withContext emptyList()
        }
        try {
            // Obtenemos la colección de hoteles.
            val hotelCollection = database.getCollection("Hoteles")
            // Creamos un filtro para buscar por el campo "adminId"
            val filter = Filters.eq("adminId", adminId)
            // Ejecutamos la búsqueda y mapeamos cada documento al modelo Hotel
            val hotels = hotelCollection.find(filter).map { document ->
                documentToHotel(document)
            }.toList()

            Log.d(TAG, "Encontrados ${hotels.size} hoteles para el admin ID: $adminId")
            hotels
        } catch (e: Exception) {
            Log.e("MongoDBConnection", "Error al obtener hoteles por Admin ID: ${e.message}")
            emptyList() // Devolvemos una lista vacía en caso de error
        }
    }

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
                adminId = document.getString("adminId") ?: "",
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
                    ?: document.getInteger("roomCount") ?: 0,
                checkInTime = document.getString("checkInTime") ?: "15:00",
                checkOutTime = document.getString("checkOutTime") ?: "12:00",
                rating = document.getInteger("rating") ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a hotel: ${e.message}")
            Hotel(
                _id = ObjectId(), // Evita errores si la conversión falla del todo
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

    // Obtiene todas las reservas con sus detalles (nombre de hotel, habitación y usuario)
    fun getAllBookingDetails(): Flow<List<BookingDetails>> = flow {
        Log.d(TAG, "Obteniendo todos los detalles de las reservas...")
        val bookings = database.getCollection("Bookings").find()
            .map { Booking.fromDocument(it) }
            .toList()

        if (bookings.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val hotelIds = bookings.map { it.hotelId }.distinct()
        val roomIds = bookings.map { it.roomId }.distinct()
        val userIds = bookings.map { it.userId }.distinct()

        val hotelsMap = database.getCollection("Hoteles")
            .find(Filters.`in`("_id", hotelIds))
            .map { it.getObjectId("_id") to it.getString("name") }
            .toList().toMap()

        val roomsMap = database.getCollection("Rooms")
            .find(Filters.`in`("_id", roomIds))
            .map { it.getObjectId("_id") to it.getString("roomNumber") }
            .toList().toMap()

        val usersMap = database.getCollection("Users")
            .find(Filters.`in`("id", userIds))
            .map { it.getString("id") to it.getString("name") }
            .toList().toMap()

        val details = bookings.mapNotNull { booking ->
            val hotelName = hotelsMap[booking.hotelId]
            val roomNumber = roomsMap[booking.roomId]
            val userName = usersMap[booking.userId]

            if (hotelName != null && roomNumber != null && userName != null) {
                BookingDetails(
                    booking = booking,
                    hotelName = hotelName,
                    roomNumber = "Hab. $roomNumber",
                    userName = userName // Añadido el parámetro faltante
                )
            } else {
                Log.w(TAG, "Faltan datos para la reserva ${booking._id}. Hotel: $hotelName, Habitación: $roomNumber, Usuario: $userName")
                null
            }
        }
        emit(details)
    }.flowOn(Dispatchers.IO)

    suspend fun insertBooking(booking: Booking): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Bookings")
            collection.insertOne(booking.toDocument())

            updateRoomStatus(booking.roomId, "OCUPADA")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando reserva: ${e.message}")
            false
        }
    }

    /**
     * Guarda una reserva. Si ya existe, actualiza solo los campos modificados.
     * Si es nueva, la inserta.
     */
    suspend fun saveBooking(booking: Booking): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = database.getCollection("Bookings")
            val filter = Filters.eq("_id", booking._id)

            // Revisa si ya existe un documento con ese ID
            val existingBooking = collection.find(filter).firstOrNull()

            if (existingBooking != null) {
                // --- ACTUALIZAR ---
                // Creamos una actualización no destructiva con $set
                val updates = Document("\$set", Document()
                    .append("guestName", booking.guestName)
                    .append("guestEmail", booking.guestEmail)
                    .append("guestPhone", booking.guestPhone)
                    .append("checkInDate", booking.checkInDate)
                    .append("checkOutDate", booking.checkOutDate)
                    .append("totalCost", booking.totalCost)
                    // No actualizamos el estado, ID, etc. a menos que sea necesario
                )
                val result = collection.updateOne(filter, updates)
                result.modifiedCount > 0
            } else {
                // --- INSERTAR ---
                // Si no existe, es una reserva nueva.
                collection.insertOne(booking.toDocument())
                // También actualizamos el estado de la habitación
                updateRoomStatus(booking.roomId, "OCUPADA")
            }
            true // Si no hay excepción, asumimos que fue exitoso
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando la reserva (saveBooking): ${e.message}", e)
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
                    updateRoomStatus(it.roomId, "DISPONIBLE")
                }
            }
            result.modifiedCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando estado de reserva: ${e.message}")
            false
        }
    }
    /**
     * Realiza el check-in de una reserva: cambia el estado a CHECKED_IN y guarda la fecha/hora actual.
     * También actualiza el estado de la habitación a OCUPADA.
     */
    suspend fun setBookingCheckIn(bookingId: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val bookingsCollection = database.getCollection("Bookings")
            val filter = eq("_id", bookingId)

            // Buscamos la reserva primero para obtener el roomId
            val bookingDoc = bookingsCollection.find(filter).first() ?: return@withContext false
            val booking = Booking.fromDocument(bookingDoc)

            // Creamos la operación de actualización
            val update = Document("\$set", Document()
                .append("status", "CHECKED_IN")
                .append("actualCheckIn", Date()) // Guarda la marca de tiempo actual
            )

            val result = bookingsCollection.updateOne(filter, update)

            if (result.modifiedCount > 0) {
                // Si el check-in fue exitoso, actualizamos el estado de la habitación
                updateRoomStatus(booking.roomId, "OCUPADA")
                return@withContext true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error realizando check-in en la BD: ${e.message}", e)
            false
        }
    }

    /**
     * Realiza el check-out de una reserva: cambia el estado a CHECKED_OUT y guarda la fecha/hora actual.
     * También actualiza el estado de la habitación a DISPONIBLE (o LIMPIEZA si prefieres).
     */
    suspend fun setBookingCheckOut(bookingId: ObjectId): Boolean = withContext(Dispatchers.IO) {
        try {
            val bookingsCollection = database.getCollection("Bookings")
            val filter = eq("_id", bookingId)

            // Buscamos la reserva primero para obtener el roomId
            val bookingDoc = bookingsCollection.find(filter).first() ?: return@withContext false
            val booking = Booking.fromDocument(bookingDoc)

            // Creamos la operación de actualización
            val update = Document("\$set", Document()
                .append("status", "CHECKED_OUT")
                .append("actualCheckOut", Date()) // Guarda la marca de tiempo actual
            )

            val result = bookingsCollection.updateOne(filter, update)

            if (result.modifiedCount > 0) {
                // Si el check-out fue exitoso, liberamos la habitación
                // Podrías cambiar "DISPONIBLE" por "LIMPIEZA" si quieres un paso intermedio
                updateRoomStatus(booking.roomId, "DISPONIBLE")
                return@withContext true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error realizando check-out en la BD: ${e.message}", e)
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
            val userIds = activeBookings.map { it.userId }.distinct()

            // 3. Obtener los documentos de esos hoteles y habitaciones en una sola consulta
            val hotels = database.getCollection("Hoteles")
                .find(Filters.`in`("_id", hotelIds))
                .map { it.getObjectId("_id") to it.getString("name") }
                .toList().toMap()

            val rooms = database.getCollection("Rooms")
                .find(Filters.`in`("_id", roomIds))
                .map { it.getObjectId("_id") to it.getString("roomNumber") }
                .toList().toMap()

            val users = database.getCollection("Users")
                .find(Filters.`in`("_id", userIds))
                .map { it.getString("_id") to it.getString("name") }
                .toList().toMap()

            // 4. Combinar en una lista de BookingDetails
            val bookingDetails = activeBookings.mapNotNull { booking ->
                val hotelName = hotels[booking.hotelId]
                val roomNumber = rooms[booking.roomId]
                val userName = users[booking.userId]
                if (hotelName != null && roomNumber != null && userName != null) {
                    BookingDetails(
                        booking = booking,
                        hotelName = hotelName,
                        roomNumber = "Hab. $roomNumber",
                        userName = userName
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

    // --- FUNCIÓN PARA EL DASHBOARD ---
    suspend fun getDashboardStats(): DashboardStats {
        return try {
            Log.d(TAG, "Calculando estadísticas del Dashboard...")
            coroutineScope {
                val hotelsCountDeferred = async(Dispatchers.IO) {
                    database.getCollection("Hoteles").countDocuments()
                }

                val roomsCountDeferred = async(Dispatchers.IO) {
                    database.getCollection("Rooms").countDocuments()
                }

                val reservationsTodayDeferred = async(Dispatchers.IO) {
                    // Configurar el rango de fechas para "hoy"
                    val calendar = Calendar.getInstance()
                    // Inicio del día
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val startOfToday = calendar.time
                    // Fin del día
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    val endOfToday = calendar.time

                    // Contar reservas que están activas hoy
                    val filter = Filters.and(
                        Filters.lte("checkInDate", endOfToday), // Check-in es hoy o antes
                        Filters.gte("checkOutDate", startOfToday) // Check-out es hoy o después
                    )
                    database.getCollection("Bookings").countDocuments(filter)
                }

                // Esperamos los resultados y creamos el objeto
                DashboardStats(
                    totalHotels = hotelsCountDeferred.await(),
                    totalRooms = roomsCountDeferred.await(),
                    reservationsToday = reservationsTodayDeferred.await()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculando estadísticas del Dashboard: ${e.message}", e)
            DashboardStats() // Devuelve objeto con ceros en caso de error
        }
    }
}