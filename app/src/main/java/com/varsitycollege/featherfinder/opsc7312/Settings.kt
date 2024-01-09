package com.varsitycollege.featherfinder.opsc7312

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.IOException
import java.util.*
import android.Manifest
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
// Import necessary Firebase components
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class Settings : AppCompatActivity() {

    private lateinit var kmSwitch: SwitchCompat
    private lateinit var milesSwitch: SwitchCompat
    private var isKmSwitchOn: Boolean = false
    private var isMilesSwitchOn: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    //profile image code
    private lateinit var profileImageView: ImageView
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var imageUri: Uri? = null  // Declare imageUri as a class variable
    private var selectedImageUri: Uri? = null

    // Firebase Storage reference
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference.child("profile_images")

        //General settings switch button code. Only one switch can be on at a time
        kmSwitch = findViewById(R.id.kmSwitch)
        milesSwitch = findViewById(R.id.milesSwitch)

        sharedPreferences = getSharedPreferences("SwitchSettings", Context.MODE_PRIVATE)

        kmSwitch.isChecked = sharedPreferences.getBoolean("isKmSwitchOn", false)
        milesSwitch.isChecked = sharedPreferences.getBoolean("isMilesSwitchOn", false)

        kmSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If kmSwitch is checked, milesSwitch should be unchecked
                milesSwitch.isChecked = false
                isMilesSwitchOn = false
                isKmSwitchOn = true
            } else {
                // If kmSwitch is unchecked, milesSwitch should be checked
                milesSwitch.isChecked = true
                isMilesSwitchOn = true
                isKmSwitchOn = false
            }
        }

        milesSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If milesSwitch is checked, kmSwitch should be unchecked
                kmSwitch.isChecked = false
                isKmSwitchOn = false
                isMilesSwitchOn = true
            } else {
                // If milesSwitch is unchecked, kmSwitch should be checked
                kmSwitch.isChecked = true
                isKmSwitchOn = true
                isMilesSwitchOn = false
            }
        }

        //save settings button
        val sharedPref = getSharedPreferences("MySettings", Context.MODE_PRIVATE)

        val saveButton = findViewById<Button>(R.id.savebtn)

        saveButton.setOnClickListener {

            // Save the download URL to the Firebase Realtime Database only when the "Save" button is clicked
            if (selectedImageUri != null) {
                uploadProfileImage(selectedImageUri!!)
            }

            val kmSwitch = findViewById<SwitchCompat>(R.id.kmSwitch)
            val milesSwitch = findViewById<SwitchCompat>(R.id.milesSwitch)

            if (kmSwitch.isChecked) {
                sharedPreferences.edit().putBoolean("isKmSwitchOn", true).apply()
                sharedPreferences.edit().putBoolean("isMilesSwitchOn", false).apply()
            } else if (milesSwitch.isChecked) {
                sharedPreferences.edit().putBoolean("isMilesSwitchOn", true).apply()
                sharedPreferences.edit().putBoolean("isKmSwitchOn", false).apply()

            }

            // Store the current state of the switches in SharedPreferences
            with(sharedPref.edit()) {
                putBoolean("kmSwitch", kmSwitch.isChecked)
                putBoolean("milesSwitch", milesSwitch.isChecked)
                apply()
            }

            // Notify the user that changes have been saved
            Toast.makeText(this, "Changes have been saved", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //sign out button
        val signOutButton = findViewById<Button>(R.id.logoutbtn)
        signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, welcome::class.java)
            startActivity(intent)
        }



        // Add the following code inside your onCreate method to handle the button click
        val deleteAccountButton = findViewById<Button>(R.id.deleteaccountbtn)
        deleteAccountButton.setOnClickListener()
        {
            // Show a confirmation dialog before deleting the account
            showDeleteAccountDialog()
        }


        //profile image code
        // Find the ImageView for the profile image
        profileImageView = findViewById(R.id.profileimg)

        // Find the "Edit Picture" button
        val editProfileButton = findViewById<Button>(R.id.editProfilebtn)

        // Set an OnClickListener to handle the button click
        editProfileButton.setOnClickListener {
            showImagePickerDialog()
        }

        //retrieve profile details code
        // Assuming you have Firebase Authentication initialized
        val currentUser = Firebase.auth.currentUser

        // Check if the user is signed in
        if (currentUser != null) {
            // Access user information
            val email = currentUser.email
            val displayName =
                currentUser.displayName  // You might have set the display name during registration

            // Now, set these values to your TextViews
            val emailTextView = findViewById<TextView>(R.id.emailtxt)
            val nameTextView = findViewById<TextView>(R.id.nametxt)
            //val surnameTextView = findViewById<TextView>(R.id.surnametxt)

            emailTextView.text = email
            nameTextView.text =
                displayName  // You can replace this with the actual field name in your Firebase database
            // Retrieve and set the surname in a similar way

            // Retrieve profile image URL and load into ImageView
            val userId = currentUser.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            val imageReference = databaseReference.child(userId).child("profileImageUrl")

            imageReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the image URL from the snapshot
                    val imageUrl = snapshot.getValue(String::class.java)

                    // Check if the activity is still valid
                    if (!isDestroyed) {
                        // Load the image into the ImageView using Glide
                        if (!imageUrl.isNullOrBlank()) {
                            Glide.with(this@Settings)
                                .load(imageUrl)
                                .into(profileImageView)
                        }
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                    Toast.makeText(
                        this@Settings,
                        "Error retrieving image: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        //password change code
        val changePasswordButton = findViewById<Button>(R.id.button5)
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    // Inside your Settings activity

    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Delete") { dialog, _ ->
                // Call the function to delete the user account
                deleteAccount()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteAccount() {
        // Get the current user
        val user = Firebase.auth.currentUser

        // Check if the user is signed in
        if (user != null) {
            // Delete the user account
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Account deleted successfully, navigate to the welcome screen or another appropriate screen
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT)
                            .show()


                        val database = FirebaseDatabase.getInstance()
                        val databaseReference = database.getReference("observation")

                        databaseReference.orderByChild("user")
                            .equalTo(user.email)
                            .limitToLast(3)  // Limit the result to the last 3 observations
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val observationsList = ArrayList<Data.Observation>()

                                    for (dataSnapshot in dataSnapshot.children) {
                                        val theObservation = dataSnapshot.getValue(Data.Observation::class.java)
                                        if (theObservation != null) {
                                            theObservation.documentID = dataSnapshot.key.toString() // Access the unique key as the document ID
                                            observationsList.add(theObservation)
                                        }
                                    }

                                    if (observationsList.isEmpty()) {
                                        // Handle the case when there are no observations with the specific email.
                                        // You can display a message to the user or take appropriate action.
                                    } else {
                                        for (observationSnapshot in dataSnapshot.children) {
                                            observationSnapshot.ref.removeValue()
                                        }
                                    }

                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Toast.makeText(this@Settings, databaseError.toString(), Toast.LENGTH_SHORT).show()
                                }
                            })






                    val intent = Intent(this, welcome::class.java)
                        startActivity(intent)
                    } else {
                        // If the deletion fails, display an error message
                        Toast.makeText(
                            this,
                            "Failed to delete account: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }




    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhotoFromCamera()
                    1 -> choosePhotoFromGallery()
                    // 2 is for "Cancel," do nothing in this case
                }
            }

        val dialog = builder.create()
        dialog.show()
    }



    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }





    private fun takePhotoFromCamera() {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            // Create an Intent to open the camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Create a file to save the captured image
            val imageFile = createImageFile()

            // Check if the file was created successfully
            if (imageFile != null) {
                imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)

                // Set the output file for the camera
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                // Start the camera activity
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                // Handle the case where imageFile is null
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Handle the result when the camera activity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // If the imageUri is not null, set the captured image to the profileImageView
                    if (imageUri != null) {
                        profileImageView.setImageURI(imageUri)
                        selectedImageUri = imageUri  // Store the captured image URI
                    } else {
                        // Handle the case where imageUri is null
                        Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    // If the data is not null, get the selected image URI from the data intent
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        // Set the selected image to the profileImageView
                        profileImageView.setImageURI(selectedImageUri)
                    } else {
                        // Handle the case where selectedImageUri is null
                        Toast.makeText(this, "Error selecting image from gallery", Toast.LENGTH_SHORT).show()
                    }
                }
                // Handle other request codes if needed
            }
        }
    }
    // Upload the profile image to Firebase Storage
    private fun uploadProfileImage(imageUri: Uri) {
        // Get the user ID
        val user = Firebase.auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            // Create a reference to the profile image in Firebase Storage
            val profileImageRef = storageRef.child("${userId}.jpg")

            // Upload the file to Firebase Storage
            val uploadTask = profileImageRef.putFile(imageUri)

            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the download URL to the Firebase Realtime Database
                    saveImageUrlToDatabase(userId, uri.toString())
                }
            }.addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save the image URL to the Firebase Realtime Database
    private fun saveImageUrlToDatabase(userId: String, imageUrl: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        databaseReference.child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Image URL saved to database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Toast.makeText(this, "Failed to save image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Create a file to save the captured image
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


    //profile password change code
    // Inside your Settings activity

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.change_password_dialog, null)

        // Get references to the EditTexts in your dialog layout
        val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.editTextOldPassword)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        builder.setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("Change") { dialog, _ ->
                // Get the text from the EditTexts
                val oldPassword = oldPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                // Check if new password and confirm password match
                if (newPassword == confirmPassword) {
                    // Change the password using Firebase Authentication
                    changePassword(oldPassword, newPassword)
                } else {
                    // Display an error message if new password and confirm password do not match
                    Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        // Get the current user
        val user = Firebase.auth.currentUser

        // Reauthenticate the user with their old password
        val credential = EmailAuthProvider.getCredential(user?.email.orEmpty(), oldPassword)
        user?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Change the password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updatePasswordTask ->
                            if (updatePasswordTask.isSuccessful) {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to change password: ${updatePasswordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Failed to reauthenticate: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}