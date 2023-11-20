package com.example.smd_a3_1

import ContactDatabaseHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smd_a3_1.ui.theme.SMD_A3_1Theme
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult

import android.provider.ContactsContract
import android.provider.Settings.Global
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private const val CONTACTS_PERMISSION_REQUEST_CODE = 123

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SMD_A3_1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Greeting(name = "muneeb")
                    App()
                }
            }

        }
        requestContactsPermissions()
    }
    private fun requestContactsPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            CONTACTS_PERMISSION_REQUEST_CODE
        )
    }
}


fun getContacts (context: Context) : List<Contact>{
    val contactsList = mutableListOf<Contact>()

    ActivityCompat.requestPermissions(
        context as ComponentActivity,
        arrayOf(Manifest.permission.READ_CONTACTS)
        ,0

    )

    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    )

    val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE 'A%' OR " +
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE 'B%' OR " +
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE 'C%' OR " +
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE 'D%'"

    val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

    val contentResolver = context.contentResolver
    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        sortOrder )
        ?.use {
                cursor ->
            val contactID = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val contactName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val contactNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactPhoto = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)


            while (cursor.moveToNext()){
                val id = cursor.getLong(contactID)
                val name = cursor.getString(contactName)
                val number = cursor.getString(contactNumber)
                val img = cursor.getString(contactPhoto)

                val imgURI = if (img != null) Uri.parse(img)
                else getContactUri(id)
                Log.d(name , "name")

                contactsList.add(Contact(0 , name , number , imgURI))

                val appContext = context.applicationContext

                val contactDbHelper = ContactDatabaseHelper(appContext)
                contactDbHelper.addContact(Contact(0 , name , number , imgURI));
            }
        }
    return contactsList
}


fun getContactDatabaseHelper(context: Context): ContactDatabaseHelper {
    return ContactDatabaseHelper(context.applicationContext)
}

//data class Contact(
//    val name: String,
//    val number: String,
//    val image:Uri?
//)


fun getContactUri(ID: Long): Uri {
    val contentUri = ContentUris.withAppendedId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, ID)
    return Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
}


fun OnDeleteClick(context: Context ,  contactId: Long){

    val contactDbHelper = ContactDatabaseHelper(context)

    // Delete the contact using the contact ID
    contactDbHelper.deleteContact(contactId)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContactCard(contact:Contact  , isExpanded: Boolean, onItemClick: () -> Unit) {

    val context = LocalContext.current.applicationContext;
    val scale: Float by animateFloatAsState(targetValue = if (isExpanded) 1.2f else 1f)

    Card(
        modifier = Modifier
//            .height(130.dp)
            .fillMaxWidth()
            .padding(6.dp)
            .clickable
            {
                onItemClick()
            }
            .clip(RoundedCornerShape(30.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
            .animateContentSize()
        // Adjust the corner radius as needed

    ){
        Column(modifier = Modifier

            .fillMaxSize()
            .background(Color.LightGray)
        )
        {
            Row (
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)) {
                Image(painter = painterResource(id = R.drawable.ic_person_background) ,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp, 40.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically)
                )

                Column {
                    Text(text = contact.name)
                    Text(text = contact.phoneNumber)
                }
            }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }


            if (isExpanded){

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,

                    ){


                    Button(onClick = {

                        OnDeleteClick(context , 1);

                    }) {
                        MaterialTheme {
                            Text(
                                text = "Delete",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(onClick = { /*TODO*/ }) {
                        MaterialTheme {
                            Text(
                                text = "Update",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = ("tel:${contact.phoneNumber}").toUri()
                        launcher.launch(intent)
                    }) {
                        MaterialTheme {
                            Text(
                                text = "Call Me",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = ("smsto:${contact.phoneNumber}").toUri()
                        launcher.launch(intent)
                    }) {
                        MaterialTheme {
                            Text(
                                text = "Message",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                }
            }

        }
    }


}



@Composable
fun ContactList(contacts: List<Contact> ){
    val expandedStateMap = remember { mutableStateMapOf<Contact, Boolean>() }
    LazyColumn() {
        items(contacts) { contact ->
            ContactCard(contact = contact,
                isExpanded = expandedStateMap[contact] ?: false,
                onItemClick = {
                    // Toggle the expanded state for the clicked contact
                    expandedStateMap[contact] = !(expandedStateMap[contact] ?: false)
                    expandedStateMap.filter { it.key != contact }.forEach {
                        expandedStateMap[it.key] = false // For collapsing all other if expanded
                    }

                }
            )
        }
    }
}



@Composable
fun App() {
    val context = LocalContext.current
    val contacts = remember { getContacts(context) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(5.dp)// Set the background color of the main content
    ) {
        Text(
            text = "Contacts", fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            color = Color.DarkGray
        )

        ContactList(contacts = contacts)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


//SMD_A3_1Theme
@Composable
fun GreetingPreview() {
    SMD_A3_1Theme {
        //Greeting("Android")
        App()
    }
}