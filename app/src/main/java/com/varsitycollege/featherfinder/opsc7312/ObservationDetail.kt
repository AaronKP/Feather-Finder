package com.varsitycollege.featherfinder.opsc7312

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class ObservationDetail : AppCompatActivity() {

    // Define class-level variables to store initial text values.
    private var initialCommonName: String = ""
    private var initialScientificName: String = ""
    private var initialLocation: String = ""
    private var initialDate: String = ""
    private var initialTime: String = ""
    private var imageUrl: String =""

    private val PICK_IMAGE_REQUEST = 1
    private var imageString: String = ""

    private var theDocumentID: String = ""

    private lateinit var Date: Date
    private lateinit var Time: Time

    private var myLatitude: String = ""
    private var myLongitude: String = ""
    private var locationName: String = ""

    val database = FirebaseDatabase.getInstance()
    val databaseReference = database.getReference("observation")

    val storage = Firebase.storage
    val storageReference = storage.reference


    private var imagechange: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_detail)

        // Assuming you have a reference to the ImageButton
        val returnButton = findViewById<AppCompatImageButton>(R.id.returnBtn)


        // Check the condition where you want to change the color
        val shouldChangeColor = true // Change this to your actual condition

        if (shouldChangeColor) {
            // Set color filter to white when the condition is met
            returnButton.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        } else {
            // Reset color filter to null when the condition is not met
            returnButton.clearColorFilter()
        }







        val datebtn: AppCompatButton = findViewById<AppCompatButton>(R.id.date_fab)
        val timebtn: AppCompatButton = findViewById<AppCompatButton>(R.id.time_fab)

        datebtn.setOnClickListener {
            showDatePickerDialog(datebtn)
        }

        timebtn.setOnClickListener {
            showTimePickerDialog()
        }













        // Retrieve data from Intent extras
        val observationData = intent.getSerializableExtra("observationData") as Data.Observation

        if (observationData != null) {
            // Retrieve initial text values.
            initialCommonName = observationData.comName
            initialScientificName = observationData.sciName
            initialLocation = observationData.location
            initialDate = observationData.date
            initialTime = observationData.time
            theDocumentID = observationData.documentID
            imageUrl = observationData.imageUrl

            val commonNameEditText = findViewById<TextView>(R.id.comnametxt)
            val scientificNameEditText = findViewById<TextView>(R.id.scinametxt)
            val locationEditText = findViewById<TextView>(R.id.locationEditText)
            val dateEditText = findViewById<TextView>(R.id.dateview)
            val timeEditText = findViewById<TextView>(R.id.timeview)

            val commonNameText = Editable.Factory.getInstance().newEditable(initialCommonName)
            val scientificNameText =
                Editable.Factory.getInstance().newEditable(initialScientificName)
            val locationText = Editable.Factory.getInstance().newEditable(initialLocation)
            val dateText = Editable.Factory.getInstance().newEditable(initialDate)
            val timeText = Editable.Factory.getInstance().newEditable(initialTime)

            commonNameEditText.text = commonNameText
            scientificNameEditText.text = scientificNameText
            locationEditText.text = locationText
            dateEditText.text = formatDate(dateText.toString())
            timeEditText.text = timeText

            val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment) as AutocompleteSupportFragment
            autocompleteFragment.view?.visibility = View.GONE // to hide


            // Load and display the image using Glide
            val imageView = findViewById<CircleImageView>(R.id.obsimage)
            val imageUrl = observationData.imageUrl

            // Use Glide to load the image from Firebase Storage
            Glide.with(imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.birddefault) // You can use a placeholder image
                .error(R.drawable.birddefault) // You can use an error image
                .into(imageView)


            val addImage = findViewById<AppCompatImageButton>(R.id.add_image)
            addImage.setOnClickListener {
                addImage()
                checkForChanges()
            }

            // Call the function to check for changes in text fields.
            checkForChanges()
        }








        val locationedit = findViewById<AppCompatImageButton>(R.id.selectLocationButton)
        locationedit.setOnClickListener() {
            val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment) as AutocompleteSupportFragment
            autocompleteFragment.view?.visibility = View.VISIBLE

            val locationtv = findViewById<TextView>(R.id.locationEditText)
            locationtv.setText(locationName)
            locationtv.visibility = View.GONE

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
                        locationName = p0.name
                        val locationtv = findViewById<TextView>(R.id.locationEditText)
                        locationtv.setText(locationName)
                        locationtv.visibility = View.VISIBLE

                        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment) as AutocompleteSupportFragment
                        autocompleteFragment.view?.visibility = View.GONE // to hide

                        val latlng = p0.latLng
                        myLatitude = latlng.latitude.toString()
                        myLongitude = latlng.longitude.toString()
                        locationName = p0.name
                    }
                }

            })
        }














        val save = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        save.setOnClickListener() {
            val commonNameEditText = findViewById<TextView>(R.id.comnametxt).text.toString()
            val scientificNameEditText = findViewById<TextView>(R.id.scinametxt).text.toString()
            val dateEditText = findViewById<TextView>(R.id.dateview).text.toString()
            val timeEditText = findViewById<TextView>(R.id.timeview).text.toString()

            val myUser = FirebaseAuth.getInstance().currentUser
            val myemail = myUser?.email

            var thenewlat = ""
            var thenewlong = ""
            var thenewloc = ""

            val oldloc = findViewById<TextView>(R.id.locationEditText)
            if(oldloc.text.toString().equals(observationData.location)){
                thenewlat = observationData.lat
                thenewlong = observationData.long
                thenewloc = observationData.location
            }
            else{
                thenewlat = myLatitude
                thenewlong = myLongitude
                thenewloc = locationName
            }


            if(imagechange) {
                // Upload the new image to Firebase Storage
                uploadImageToFirebaseStorage(imageString) { imageUrl ->
                    // Include the new image URL in the observation data
                    val observationData = hashMapOf(
                        "user" to myemail,
                        "comName" to commonNameEditText,
                        "sciName" to scientificNameEditText,
                        "speciesCode" to observationData.speciesCode,
                        "location" to thenewloc,
                        "lat" to thenewlat,
                        "long" to thenewlong,
                        "date" to reverseformatDate(dateEditText),
                        "time" to timeEditText,
                        "imageUrl" to imageUrl
                    )

                    val confirmDialog = AlertDialog.Builder(this)
                    confirmDialog.setTitle("Confirmation")
                    confirmDialog.setMessage("Are you sure you want to edit this observation?")

                    confirmDialog.setPositiveButton("Yes") { dialog, which ->
                        // Save changes and display success message
                        val successDialog = AlertDialog.Builder(this)
                        // Update the data in Firebase Realtime Database
                        databaseReference.child(theDocumentID).setValue(observationData)
                        successDialog.setTitle("Success")
                        successDialog.setMessage("Observation has been successfully changed.")
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

                    // Reset the imagechange flag to false
                    imagechange = false
                }
            } else {
                // The image was not changed, proceed without updating the image
                val updateMap = mapOf(
                    "user" to myemail,
                    "comName" to commonNameEditText,
                    "sciName" to scientificNameEditText,
                    "speciesCode" to observationData.speciesCode,
                    "location" to thenewloc,
                    "lat" to thenewlat,
                    "long" to thenewlong,
                    "date" to reverseformatDate(dateEditText),
                    "time" to timeEditText,
                    "imageUrl" to imageUrl
                )

                val confirmDialog = AlertDialog.Builder(this)
                confirmDialog.setTitle("Confirmation")
                confirmDialog.setMessage("Are you sure you want to edit this observation?")

                confirmDialog.setPositiveButton("Yes") { dialog, which ->
                    // Save changes and display success message
                    val successDialog = AlertDialog.Builder(this)

                    // Update the data in Firebase Realtime Database
                    databaseReference.child(theDocumentID).setValue(updateMap)
                    successDialog.setTitle("Success")
                    successDialog.setMessage("Observation has been successfully changed.")
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










        val delete = findViewById<Button>(R.id.deletebtn)
        delete.setOnClickListener() {
            // Assuming 'theDocumentID' is the ID of the observation you want to delete
            val documentReference = databaseReference.child(theDocumentID)



            val confirmDialog = AlertDialog.Builder(this)
            confirmDialog.setTitle("Confirmation")
            confirmDialog.setMessage("Are you sure you want to delete this observation?")

            confirmDialog.setPositiveButton("Yes") { dialog, which ->

                documentReference.removeValue()
                    .addOnSuccessListener {

                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error deleting document: $exception", Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            confirmDialog.setNegativeButton("No") { dialog, which ->
                // Perform any action if needed when the user cancels the operation
            }

            confirmDialog.show()

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

        //Navigate back to home. Nav arrow
        val arrowReturnbtn = findViewById<AppCompatImageButton>(R.id.returnBtn)
        arrowReturnbtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
            imagechange = true
            findViewById<FloatingActionButton>(R.id.floatingActionButton).show()
            checkForChanges()
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

        val profileImageView = findViewById<CircleImageView>(R.id.obsimage)

        if (requestCode == 123 && resultCode == RESULT_OK) {
            val bmp: Bitmap = data?.extras?.get("data") as Bitmap
            profileImageView.setImageBitmap(bmp)

            imageString = bitmapToBase64String(bmp)
            imagechange = true
            findViewById<FloatingActionButton>(R.id.floatingActionButton).show()

            checkForChanges()
        } else if (requestCode == 456 && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)

            imageString = uriToBase64String(selectedImageUri)
            imagechange = true
            findViewById<FloatingActionButton>(R.id.floatingActionButton).show()

            checkForChanges()
        }
    }

    private fun uploadImageToFirebaseStorage(imageString: String, onComplete: (String) -> Unit) {
        val storageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")
        val imageByteArray = Base64.decode(imageString, Base64.DEFAULT)

        val uploadTask = storageRef.putBytes(imageByteArray)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                onComplete(imageUrl)
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Image upload failed: ${exception.message}")
        }
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
















    private fun checkForChanges() {
        val commonNameEditText = findViewById<TextView>(R.id.comnametxt)
        val scientificNameEditText = findViewById<TextView>(R.id.scinametxt)
        val locationEditText = findViewById<TextView>(R.id.locationEditText)
        val dateEditText = findViewById<TextView>(R.id.dateview)
        val timeEditText = findViewById<TextView>(R.id.timeview)

        // Add text change listeners to the EditText fields.
        val textChangeWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Check if any of the fields contain changes.
                val isCommonNameModified = commonNameEditText.text.toString() != initialCommonName
                val isScientificNameModified =
                    scientificNameEditText.text.toString() != initialScientificName
                val isLocationModified = locationEditText.text.toString() != initialLocation
                val isDateModified = dateEditText.text.toString() != formatDate(initialDate)
                val isTimeModified = timeEditText.text.toString() != initialTime

                if (isCommonNameModified || isScientificNameModified || isLocationModified ||
                    isDateModified || isTimeModified || imagechange
                ) {
                    // If any field or the image has been modified, show the "Save" button.
                    findViewById<FloatingActionButton>(R.id.floatingActionButton).show()
                } else {
                    // If all fields match the initial values, hide the "Save" button.
                    findViewById<FloatingActionButton>(R.id.floatingActionButton).hide()
                }
            }
        }

        commonNameEditText.addTextChangedListener(textChangeWatcher)
        scientificNameEditText.addTextChangedListener(textChangeWatcher)
        locationEditText.addTextChangedListener(textChangeWatcher)
        dateEditText.addTextChangedListener(textChangeWatcher)
        timeEditText.addTextChangedListener(textChangeWatcher)
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

    fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
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
            (requireActivity() as ObservationDetail).addTime(selectedTime)
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
            (requireActivity() as ObservationDetail).addTime(selectedTime)
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
            (requireActivity() as ObservationDetail).addDateTo(selectedDate)
        }
    }
}

