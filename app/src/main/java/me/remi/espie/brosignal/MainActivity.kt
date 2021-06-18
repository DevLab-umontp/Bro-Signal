package me.remi.espie.brosignal

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.gson.Gson
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private val smsManager: SmsManager = SmsManager.getDefault()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //deleteFile()
        readJSONfromFile().forEach { addToDrawer(it) }
        setBroName()
        val broName = findViewById<TextView>(R.id.broName)
        broName.doAfterTextChanged {
            saveBroName(broName.text.toString())
        }
    }

    private fun deleteFile(){
        File(applicationContext.filesDir.absolutePath + "/broname.txt").delete()
        File(applicationContext.filesDir.absolutePath + "/bros.json").delete()
    }

    private fun saveBroName(text: String) {
        val fileName = File(applicationContext.filesDir.absolutePath + "/broname.txt")
        fileName.writeText(text)
    }

    private fun setBroName(){
        val fileName = File(applicationContext.filesDir.absolutePath + "/broname.txt")
        val broName = findViewById<TextView>(R.id.broName)
        if (fileName.isFile) {
            val size: Long = fileName.length()
            if (size != 0L) {
                fileName.forEachLine {
                    //println(it)
                    broName.text = it
                }
            } else println("empty file")
        } else println("not a file")
    }

    private fun readJSONfromFile(): Array<User> {
        var userArray = arrayOf<User>()
        val fileName = File(applicationContext.filesDir.absolutePath + "/bros.json")
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //fileName.delete()
        if (fileName.isFile) {
            val size: Long = fileName.length()
            if (size != 0L) {
                fileName.forEachLine {
                    //println(it)
                    val user: User = gson.fromJson(it, User::class.java)
                    userArray = userArray.plusElement(user)
                }
            } else println("empty file")
        } else println("not a file")
        return userArray
    }

    private fun addToDrawer(user: User) {
        val drawer = findViewById<View>(R.id.broList) as LinearLayout

        val thumbnail = ImageView(this)
        if (user.contactThumbnails != "") {
            thumbnail.setImageURI(Uri.parse(user.contactThumbnails))
        } else {
            thumbnail.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_person_24))
        }
        val contactName = TextView(this)
        contactName.text = user.contactName
        val contactNumber = TextView(this)
        contactNumber.text = user.contactNumber
        val contactBin = ImageView(this)
        contactBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24))
        contactBin.setColorFilter(resources.getColor(R.color.design_default_color_error), PorterDuff.Mode.SRC_IN);

        val horizontalLayout = LinearLayout(this)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL

        val verticalLayout = LinearLayout(this)
        verticalLayout.orientation = LinearLayout.VERTICAL

        horizontalLayout.addView(thumbnail)
        verticalLayout.addView(contactName)
        verticalLayout.addView(contactNumber)
        horizontalLayout.addView(verticalLayout)
        horizontalLayout.addView(contactBin)

        horizontalLayout.gravity = Gravity.CENTER_VERTICAL

        drawer.addView(horizontalLayout)

        contactBin.setOnClickListener {
            removeJSONfromFile(applicationContext.filesDir.absolutePath + "/bros.json", user.contactID)
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                drawer.removeView(horizontalLayout)
            })
        }
    }

    private fun writeJSONtoFile(filePath: String, user: User) {
        val file = File(filePath)
        val jsonString: String = gson.toJson(user)
        if (file.length() == 0L) file.writeText(jsonString)
        else file.appendText("\n" + jsonString)
    }

    private fun removeJSONfromFile(filePath: String, userId: String?){
        var userArray = arrayOf<User>()
        val fileName = File(filePath)
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //fileName.delete()
        if (fileName.isFile) {
            val size: Long = fileName.length()
            if (size != 0L) {
                fileName.forEachLine {
                    val user: User = gson.fromJson(it, User::class.java)
                    if (user.contactID != userId) userArray = userArray.plusElement(user)
                }
                fileName.delete()
                userArray.forEach {
                    writeJSONtoFile(filePath, it)
                }
            } else println("empty file")
        } else println("not a file")
    }

    private fun checkContactPerm(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPerm() {
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this, permission, 1)
    }

    private fun checkSMSPerm(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSMSPerm() {
        val permission = arrayOf(android.Manifest.permission.SEND_SMS)
        ActivityCompat.requestPermissions(this, permission, 5)
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, 2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickContact()
        } else if (requestCode == 5 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            sendBroSignal()
        } else {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                val uri = data!!.data
                val cursor2: Cursor?
                val cursor1: Cursor = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    val contactID =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val contactThumbnails =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResult =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResult.toInt()

                    if (idResultHold == 1) {
                        cursor2 = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                            null,
                            null
                        )

                        var contactNumber = ""

                        while (cursor2!!.moveToNext()) {
                            contactNumber =
                                cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }

                        val user: User = if (contactThumbnails != null) {
                            User(
                                contactID,
                                contactName,
                                contactThumbnails,
                                contactNumber
                            )
                        } else {
                            User(
                                contactID,
                                contactName,
                                "",
                                contactNumber
                            )
                        }

                        readJSONfromFile().forEach {
                            if (it.contactID == contactID) {
                                Toast.makeText(this, "BRO déjà enregistré", Toast.LENGTH_LONG).show()
                                return
                            }
                        }

                        addToDrawer(user)
                        writeJSONtoFile(applicationContext.filesDir.absolutePath + "/bros.json", user)
                        cursor2.close()
                    }
                    cursor1.close()
                }

            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner un BRO", Toast.LENGTH_LONG).show()
        }
    }


    fun launchBroSignal(view: View) {
        if (!checkSMSPerm()) requestSMSPerm()
        else {
            sendBroSignal()
        }
    }

    private fun sendBroSignal(){
        val callBros = findViewById<ImageView>(R.id.callBros)
        callBros.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.brosignal_color))
        callBros.animate().apply {
            duration = 250
            scaleXBy(-0.1f)
            scaleYBy(-0.1f)
        }.withEndAction {
            callBros.animate().apply {
                duration = 250
                scaleXBy(0.1f)
                scaleYBy(0.1f)
            }
        }

        Thread {
            Thread.sleep(2000)
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                callBros.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.brosignal))
            })
        }.start()

        val broName = findViewById<TextView>(R.id.broName)
        var messageText = "BRO !! "
        if (broName.text.isEmpty()) messageText +="Ton BRO anonyme a besoin d'aide !"
        else messageText += "Ton BRO " + broName.text + " a besoin d'aide !"

        val userArray = readJSONfromFile()
        if (userArray.isEmpty()) Toast.makeText(this, "Vous n'avez pas de bro T_T", Toast.LENGTH_LONG).show()
        else userArray.forEach {
            //println(it.toString())
            smsManager.sendTextMessage(it.contactNumber, null, messageText, null, null);
        }
    }

    fun addBro(view: View) {
        if (checkContactPerm()) {
            pickContact()
        } else {
            requestContactPerm()
        }
    }
}