package com.varsitycollege.featherfinder.opsc7312

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import java.util.Calendar
import java.util.concurrent.Executors

class AddObservation : AppCompatActivity() {

    private lateinit var Date: Date
    private lateinit var Time: Time

    private val PICK_IMAGE_REQUEST = 1
    private var imageString: String = ""

    private var lat: String = ""
    private var long: String = ""

    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageReference: StorageReference = storage.reference

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_observation)

        //bird guide object from Guide.kt which gets info from  SouthAfricanBirdGuide.kt
        val clickedBird = intent.getSerializableExtra("clickedBird") as Data.BirdSpecies

        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("observation")

        val datebtn: AppCompatButton = findViewById<AppCompatButton>(R.id.date_fab)
        val timebtn: AppCompatButton = findViewById<AppCompatButton>(R.id.time_fab)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val currentDate = Date()
        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)

        val theTime = findViewById<TextView>(R.id.timeview)
        theTime.text = formattedTime

        val theDate = findViewById<TextView>(R.id.dateview)
        theDate.text = formatDate(formattedDate)


        datebtn.setOnClickListener {
            showDatePickerDialog(datebtn)
        }

        timebtn.setOnClickListener {
            showTimePickerDialog()
        }

        val comnametext = findViewById<TextView>(R.id.comnametxt)
        comnametext.text = clickedBird.commonName


        val scinametext = findViewById<TextView>(R.id.scinametxt)
        scinametext.text = clickedBird.scientificName

        // Find the AutocompleteSupportFragment by its ID
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as AutocompleteSupportFragment?

        // Set the text color of the AutocompleteSupportFragment
        setAutocompleteFragmentTextColor(autocompleteFragment, Color.WHITE)


        val addImage = findViewById<Button>(R.id.add_image)
        addImage.setOnClickListener {
            addImage()
        }














        var myLatitude = ""
        var myLongitude = ""
        var locationName = ""


        //location
        if (!Places.isInitialized()){
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val autocompleteSupportFragment = (supportFragmentManager.findFragmentById(R.id.fragment) as AutocompleteSupportFragment).setPlaceFields(
            listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        )

        autocompleteSupportFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onError(p0: Status) {
                Log.e("error", p0.statusMessage.toString())
            }

            override fun onPlaceSelected(p0: Place) {
                if (p0.latLng!=null){
                    val latlng = p0.latLng
                    myLatitude = latlng.latitude.toString()
                    myLongitude = latlng.longitude.toString()
                    locationName = p0.name
                }
            }

        })





















        // Save btn to save to firestore
        val savebtn = findViewById<FloatingActionButton>(R.id.savebtn)
        savebtn.setOnClickListener {
            if (locationName.equals("")) {

                showMessageDialog("Error", "Please add the address before continuing.")

            } else {
                val time = findViewById<TextView>(R.id.timeview).text.toString()
                val date = reverseformatDate(findViewById<TextView>(R.id.dateview).text.toString())

                val comnametext = findViewById<TextView>(R.id.comnametxt)
                comnametext.text = clickedBird.commonName
                val comname = comnametext.text.toString()

                val scinametext = findViewById<TextView>(R.id.scinametxt)
                scinametext.text = clickedBird.scientificName
                val sciname = scinametext.text.toString()

                val myUser = FirebaseAuth.getInstance().currentUser
                val myemail = myUser?.email

                val myImage = findViewById<ImageView>(R.id.obsimage)


                // Check if an image is selected
                if (imageString.isNotEmpty()) {
                    // Upload the user-selected image to Firebase Storage
                    uploadImageToFirebaseStorage(imageString) { imageUrl ->
                        // Include the image URL in the observation data
                        val observationData = hashMapOf(
                            "user" to myemail,
                            "comName" to comname,
                            "sciName" to sciname,
                            "speciesCode" to clickedBird.speciesCode,
                            "location" to locationName,
                            "lat" to myLatitude,
                            "long" to myLongitude,
                            "date" to date,
                            "time" to time,
                            "imageUrl" to imageUrl  // Include the user-selected image URL
                        )


                        val confirmDialog = AlertDialog.Builder(this)
                        confirmDialog.setTitle("Confirmation")
                        confirmDialog.setMessage("Are you sure you want to add this observation?")

                        confirmDialog.setPositiveButton("Yes") { dialog, which ->
                            // Save the observation data to Firebase Realtime Database
                            val key = databaseReference.push().key
                            if (key != null) {
                                databaseReference.child(key).setValue(observationData)

                                // Save changes and display success message
                                val successDialog = AlertDialog.Builder(this)
                                successDialog.setTitle("Success")
                                successDialog.setMessage("Observation has been successfully saved.")
                                successDialog.setPositiveButton("OK") { _, _ ->
                                    // Perform any action if needed
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                successDialog.show()
                            }
                        }

                        confirmDialog.setNegativeButton("No") { dialog, which ->
                            // Perform any action if needed when the user cancels the operation
                        }

                        confirmDialog.show()
                    }

                } else {
                    // If no image is selected, upload the default image to Firebase Storage
                    uploadDefaultImageToFirebaseStorage { defaultImageUrl ->
                        // Include the default image URL in the observation data
                        val observationData = hashMapOf(
                            "user" to myemail,
                            "comName" to comname,
                            "sciName" to sciname,
                            "speciesCode" to "ostric2",
                            "location" to locationName,
                            "lat" to myLatitude,
                            "long" to myLongitude,
                            "date" to date,
                            "time" to time,
                            "imageUrl" to defaultImageUrl  // Include the default image URL
                        )

                        // Save the observation data to Firebase Realtime Database
                        val key = databaseReference.push().key
                        if (key != null) {
                            databaseReference.child(key).setValue(observationData)

                            val confirmDialog = AlertDialog.Builder(this)
                            confirmDialog.setTitle("Confirmation")
                            confirmDialog.setMessage("Are you sure you want to add this observation?")

                            confirmDialog.setPositiveButton("Yes") { dialog, which ->
                                // Save changes and display success message
                                val successDialog = AlertDialog.Builder(this)
                                successDialog.setTitle("Success")
                                successDialog.setMessage("Observation has been successfully saved.")
                                successDialog.setPositiveButton("OK") { _, _ ->
                                    // Perform any action if needed
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                successDialog.show()
                            }

                            confirmDialog.setNegativeButton("No") { dialog, which ->
                                // Perform any action if needed when the user cancels the operation
                            }

                            confirmDialog.show()

                        }
                    }
                }
            }
        }

        //bottom nav
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_item -> {
                    val intent1 = Intent(this, SouthAfricanBirdGuide::class.java)
                    startActivity(intent1)
                    true
                }
                R.id.home_item -> {
                    // Handle item2 click
                    val intent2 = Intent(this, MainActivity::class.java)
                    startActivity(intent2)
                    true
                }
                R.id.map_item -> {
                    // Handle item3 click
                    val intent3 = Intent(this, MapsActivity::class.java)
                    startActivity(intent3)
                    true
                }
                else -> false
            }
        }

    }

    private fun uploadDefaultImageToFirebaseStorage(onComplete: (String) -> Unit) {
        // Upload the default image to Firebase Storage
        val storageRef = storageReference.child("images/default.jpg")

        // You can use a drawable resource (e.g., R.drawable.defaultbird) as the default image
        val defaultImage = BitmapFactory.decodeResource(resources, R.drawable.birddefault)
        val defaultImageString = bitmapToBase64String(defaultImage)

        val imageByteArray = Base64.decode(defaultImageString, Base64.DEFAULT)
        val uploadTask = storageRef.putBytes(imageByteArray)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val defaultImageUrl = uri.toString()
                onComplete(defaultImageUrl)  // Pass the default image URL to the callback function
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Default image upload failed: ${exception.message}")
        }
    }

    private fun showMessageDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun setAutocompleteFragmentTextColor(autocompleteFragment: AutocompleteSupportFragment?, color: Int) {
        autocompleteFragment?.view?.let { view ->
            // Find the EditText inside the AutocompleteSupportFragment
            val editText =
                view.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)

            // Update the text color
            editText.setTextColor(color)
        }
    }

    private fun uploadImageToFirebaseStorage(imageString: String, onComplete: (String) -> Unit) {
        val storageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")
        val imageByteArray = Base64.decode(imageString, Base64.DEFAULT)

        val uploadTask = storageRef.putBytes(imageByteArray)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Log.d("FirebaseStorage", "Image URL: $imageUrl")
                onComplete(imageUrl)  // Pass the image URL to the callback function
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Image upload failed: ${exception.message}")
        }
    }


















    private fun addImage() {
        val edit = Dialog(this)
        edit.setContentView(R.layout.add_image)

        val captureBtn = edit.findViewById<Button>(R.id.capturebtn)
        val galleryBtn = edit.findViewById<Button>(R.id.gallerybtn)
        val removeBtn = edit.findViewById<Button>(R.id.removebtn)

        captureBtn.setOnClickListener {
            // capture action is to capture an image from media store
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // request code allows the app to know which intent is being requested
            startActivityForResult(intent, 123)
        }

        galleryBtn.setOnClickListener {
            // gallery action is to pick an image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 456)
        }

        removeBtn.setOnClickListener{
            val imageholder = findViewById<ImageView>(R.id.obsimage)
            imageholder.setImageResource(R.drawable.birddefault)
            imageString=""
            edit.dismiss()
        }

        edit.show()
    }




    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val profileImageView = findViewById<ImageView>(R.id.obsimage)

        // For capture
        if (requestCode == 123 && resultCode == RESULT_OK) {
            val bmp: Bitmap = data?.extras?.get("data") as Bitmap
            profileImageView.setImageBitmap(bmp)

            // Convert the bitmap to a Base64-encoded string
            imageString = bitmapToBase64String(bmp)

        }
        // For gallery
        else if (requestCode == 456 && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)

            // Convert the selected image URI to a Base64-encoded string
            imageString = uriToBase64String(selectedImageUri)
        }

        Log.d("Profile", "requestCode: $requestCode")
        Log.d("Profile", "resultCode: $resultCode")
        Log.d("Profile", "data: $data")
    }


    fun formatDate(inputDate: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        try {
            // Parse the input date
            val parsedDate = inputFormat.parse(inputDate)

            // Format the parsed date to the desired output format
            return outputFormat.format(parsedDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return the original input if there is an error
        return inputDate
    }

    fun reverseformatDate(inputDate: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            // Parse the input date
            val parsedDate = inputFormat.parse(inputDate)

            // Format the parsed date to the desired output format
            return outputFormat.format(parsedDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return the original input if there is an error
        return inputDate
    }



    private fun bitmapToBase64String(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uriToBase64String(uri: Uri?): String {
        val inputStream = contentResolver.openInputStream(uri!!)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream?.read(buffer).also { length = it!! } != -1) {
            byteArrayOutputStream.write(buffer, 0, length)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }



    fun addDateTo(date: String) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val parsedDate = inputFormat.parse(date)
        val formattedDateStr = outputFormat.format(parsedDate)

        Date = outputFormat.parse(formattedDateStr)

        val theDate = findViewById<TextView>(R.id.dateview)
        theDate.text = formatDate(outputFormat.format(Date).toString())
    }

    fun addTime(time: String) {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val parsedTime = inputFormat.parse(time)
        val formattedTimeStr = outputFormat.format(parsedTime)

        val formattedTime = outputFormat.parse(formattedTimeStr)
        Time = Time(formattedTime.time)

        val time = findViewById<TextView>(R.id.timeview)
        time.text = outputFormat.format(formattedTime)
    }


    fun showTimePickerDialog() {
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.show(supportFragmentManager, "timePicker")
    }
    fun showEndTimePickerDialog() {
        val timePickerFragment = EndTimePickerFragment()
        timePickerFragment.show(supportFragmentManager, "timePicker")
    }
    fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        (requireActivity() as AddObservation).addTime(selectedTime)
    }
}
class EndTimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        (requireActivity() as AddObservation).addTime(selectedTime)
    }
}
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = android.icu.util.Calendar.getInstance()
        val year = c.get(android.icu.util.Calendar.YEAR)
        val month = c.get(android.icu.util.Calendar.MONTH)
        val day = c.get(android.icu.util.Calendar.DAY_OF_MONTH)
        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireContext(), this, year, month, day)
    }
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Do something with the date chosen by the user
        val selectedDate = "$year-${month + 1}-$day"
        (requireActivity() as AddObservation).addDateTo(selectedDate)
    }
}