import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.SparseArray
import androidx.core.util.set
import io.github.mbenoukaiss.bikes.models.*
import java.util.*
import kotlin.collections.HashMap

class OfflineCache(context: Context?) : SQLiteOpenHelper(context, "bikes", null, 1) {
    private var db: SQLiteDatabase? = null

    @Synchronized
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE stand (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                bikes INT,
                stands INT,
                capacity INT);
        """
        )

        db.execSQL(
            """
            CREATE TABLE station (
                number INT,
                city TEXT,
                contract_name TEXT,
                name TEXT,
                address TEXT,
                latitude DOUBLE,
                longitude DOUBLE,
                banking BOOLEAN,
                bonus BOOLEAN,
                overflow BOOLEAN,
                connected BOOLEAN,
                status TEXT,
                last_update TEXT,
                total_stands INT,
                main_stands INT,
                overflow_stands INT,
                
                FOREIGN KEY(total_stands) REFERENCES stand(id),
                FOREIGN KEY(main_stands) REFERENCES stand(id),
                FOREIGN KEY(overflow_stands) REFERENCES stand(id));
        """
        )
    }

    @Synchronized
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE station;")
        db.execSQL("DROP TABLE stand;")
        onCreate(db)
    }

    @Synchronized
    override fun close() {
        db!!.close()
    }

    @Synchronized
    fun write(cache: HashMap<String, Vector<Station>>) {
        db!!.execSQL("DELETE FROM station")
        db!!.execSQL("DELETE FROM stand")

        for ((city, stations) in cache) {
            for (station in stations) {
                val ts = ContentValues()
                ts.put("bikes", station.totalStands.availabilities.bikes)
                ts.put("stands", station.totalStands.availabilities.stands)
                ts.put("capacity", station.totalStands.capacity)

                val totalStands = db!!.insert("stand", null, ts)

                val ms = ContentValues()
                ms.put("bikes", station.mainStands.availabilities.bikes)
                ms.put("stands", station.mainStands.availabilities.stands)
                ms.put("capacity", station.mainStands.capacity)

                val mainStands = db!!.insert("stand", null, ms)

                val overflowStands = if (station.overflowStands != null) {
                    val stands = station.overflowStands!!

                    val os = ContentValues()
                    os.put("bikes", stands.availabilities.bikes)
                    os.put("stands", stands.availabilities.stands)
                    os.put("capacity", stands.capacity)

                    db!!.insert("stand", null, os)
                } else {
                    null
                }

                val s = ContentValues()
                s.put("number", station.number)
                s.put("city", city)
                s.put("contract_name", station.contractName)
                s.put("name", station.name)
                s.put("address", station.address)
                s.put("latitude", station.position.latitude)
                s.put("longitude", station.position.longitude)
                s.put("banking", station.banking)
                s.put("bonus", station.bonus)
                s.put("overflow", station.overflow)
                s.put("connected", station.connected)
                s.put("status", station.status.name)
                s.put("last_update", station.lastUpdate)
                s.put("total_stands", totalStands)
                s.put("main_stands", mainStands)
                s.put("overflow_stands", overflowStands)
                db!!.insert("station", null, s)
            }
        }

        db!!.close()
    }

    @Synchronized
    private fun stands(): SparseArray<Stand> {
        val stands = SparseArray<Stand>()

        val c = db!!.query(
            "stand",
            arrayOf(
                "id",
                "bikes",
                "stands",
                "capacity"
            ),
            null,
            null,
            null,
            null,
            null
        )

        if (c.count > 0) {
            c.moveToFirst()
            do stands[c.getInt(0)] = Stand(
                availabilities = Availabilities(
                    bikes = c.getInt(1),
                    stands = c.getInt(2)
                ),
                capacity = c.getInt(3)
            ) while (c.moveToNext())
        }

        c.close()

        return stands
    }

    @Synchronized
    fun read(): HashMap<String, Vector<Station>>? {
        val c = db!!.query(
            "station",
            arrayOf(
                "number",
                "city",
                "contract_name",
                "name",
                "address",
                "latitude",
                "longitude",
                "banking",
                "bonus",
                "overflow",
                "connected",
                "status",
                "last_update",
                "total_stands",
                "main_stands",
                "overflow_stands"
            ),
            null,
            null,
            null,
            null,
            null
        )

        if (c.count > 0) {
            val cache = HashMap<String, Vector<Station>>()
            val stands = stands()

            c.moveToFirst()
            do {
                val city = c.getString(1)
                val stations: Vector<Station> = cache.getOrPut(city) {
                    Vector<Station>()
                }

                val station = Station(
                    number = c.getInt(0),
                    contractName = c.getString(2),
                    name = c.getString(3),
                    address = c.getString(4),
                    position = Position(
                        latitude = c.getDouble(5),
                        longitude = c.getDouble(6)
                    ),
                    banking = c.getInt(7) != 0,
                    bonus = c.getInt(8) != 0,
                    overflow = c.getInt(9) != 0,
                    connected = c.getInt(10) != 0,
                    status = Status.valueOf(c.getString(11)),
                    lastUpdate = c.getString(12),
                    totalStands = stands[c.getInt(13)]!!,
                    mainStands = stands[c.getInt(14)]!!,
                    overflowStands = stands[c.getInt(15)]
                )

                stations.addElement(station)
            } while (c.moveToNext())

            db!!.close()
            db!!.close()

            return cache
        }

        c.close()
        db!!.close()

        return null
    }

    init {
        db = writableDatabase
    }

}
