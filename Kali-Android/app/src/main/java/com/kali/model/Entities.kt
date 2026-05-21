package com.kali.model

import androidx.room.*

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val serverId: Int? = null,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "active",
    val synced: Boolean = false
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alertLocalId: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alertLocalId: Int,
    val filePath: String,
    val mediaType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: AlertEntity): Long

    @Query("SELECT * FROM alerts WHERE synced = 0")
    suspend fun getPending(): List<AlertEntity>

    @Query("UPDATE alerts SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markSynced(localId: Int, serverId: Int)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    suspend fun getAll(): List<AlertEntity>
}

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(loc: LocationEntity): Long

    @Query("SELECT * FROM locations WHERE synced = 0")
    suspend fun getPending(): List<LocationEntity>

    @Query("UPDATE locations SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}

@Dao
interface MediaDao {
    @Insert
    suspend fun insert(media: MediaEntity): Long

    @Query("SELECT * FROM media WHERE synced = 0")
    suspend fun getPending(): List<MediaEntity>

    @Query("UPDATE media SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
