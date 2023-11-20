import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.example.smd_a3_1.Contact

class ContactDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ContactDatabase"
        private const val TABLE_NAME = "contacts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE_NUMBER = "phone_number"
        private const val COLUMN_IMAGE_URI = "image_uri"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        println("Created")
        val createTableQuery = ("CREATE TABLE $TABLE_NAME "
                + "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_PHONE_NUMBER TEXT, "
                + "$COLUMN_IMAGE_URI TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }


    // Function to add a contact to the database
    fun addContact(contact: Contact): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, contact.name)
        values.put(COLUMN_PHONE_NUMBER, contact.phoneNumber)
        values.put(COLUMN_IMAGE_URI, contact.imageUri.toString())

        val id = db.insert(TABLE_NAME, null, values)
        //db.close()
        return id
    }

    // Function to get all contacts from the database
    fun getAllContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val query = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor: Cursor?

        cursor = db.rawQuery(query, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                    val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER))
                    val imageUri = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)))

                    val contact = Contact(id, name, phoneNumber, imageUri)
                    contactsList.add(contact)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        //db.close()
        return contactsList
    }

    // Function to update a contact in the database
    fun updateContact(contact: Contact): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, contact.name)
        values.put(COLUMN_PHONE_NUMBER, contact.phoneNumber)
        values.put(COLUMN_IMAGE_URI, contact.imageUri.toString())

        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(contact.id.toString())
        )
    }

    // Function to delete a contact from the database
    fun deleteContact(contactId: Long): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(contactId.toString())
        )
    }
}
