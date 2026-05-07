package com.example.soccertshirts_app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.services.CloudinaryModel
import com.example.soccertshirts_app.databinding.FragmentAddEditJerseyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*

class AddEditJerseyFragment : Fragment() {

    private var _binding: FragmentAddEditJerseyBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null
    
    private val args: AddEditJerseyFragmentArgs by navArgs()
    private var existingJersey: Jersey? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivJerseyPreview.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditJerseyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CloudinaryModel.init(requireContext())

        val jerseyId = args.jerseyId
        if (!jerseyId.isNullOrEmpty()) {
            loadJerseyData(jerseyId)
            binding.btnSave.text = "Update Jersey"
        }

        binding.btnSelectImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                checkAndUploadImage()
            }
        }
    }

    private fun loadJerseyData(jerseyId: String) {
        db.collection("jerseys").document(jerseyId).get()
            .addOnSuccessListener { document ->
                existingJersey = document.toObject(Jersey::class.java)
                existingJersey?.let { jersey ->
                    binding.etTitle.setText(jersey.title)
                    binding.etTeam.setText(jersey.team)
                    binding.etYear.setText(jersey.year.toString())
                    binding.etPrice.setText(jersey.price.toString())
                    binding.etDescription.setText(jersey.description)
                    
                    if (jersey.imageUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(jersey.imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivJerseyPreview)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading jersey: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val title = binding.etTitle.text.toString().trim()
        val team = binding.etTeam.text.toString().trim()
        val yearStr = binding.etYear.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            isValid = false
        }

        if (team.isEmpty()) {
            binding.etTeam.error = "Team is required"
            isValid = false
        }

        val year = yearStr.toIntOrNull()
        if (year == null) {
            binding.etYear.error = "Valid year is required"
            isValid = false
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.etPrice.error = "Valid positive price is required"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            isValid = false
        }

        // Image validation: either a new one is selected OR an existing one exists
        if (selectedImageUri == null && (existingJersey == null || existingJersey?.imageUrl.isNullOrEmpty())) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun checkAndUploadImage() {
        val ownerId = auth.currentUser?.uid ?: ""
        if (ownerId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false
        
        selectedImageUri?.let { uri ->
            val publicId = "jersey_${UUID.randomUUID()}"
            CloudinaryModel.uploadImage(uri, publicId) { imageUrl ->
                if (imageUrl != null) {
                    saveJerseyToFirestore(imageUrl)
                } else {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            // No new image selected, use existing if available
            val existingUrl = existingJersey?.imageUrl ?: ""
            saveJerseyToFirestore(existingUrl)
        }
    }

    private fun saveJerseyToFirestore(imageUrl: String) {
        val title = binding.etTitle.text.toString().trim()
        val team = binding.etTeam.text.toString().trim()
        val year = binding.etYear.text.toString().trim().toInt()
        val price = binding.etPrice.text.toString().trim().toDouble()
        val description = binding.etDescription.text.toString().trim()
        val ownerId = auth.currentUser?.uid ?: ""

        val jerseyId = existingJersey?.id ?: db.collection("jerseys").document().id
        val createdAt = existingJersey?.createdAt ?: System.currentTimeMillis()

        val jersey = Jersey(
            id = jerseyId,
            title = title,
            team = team,
            year = year,
            price = price,
            description = description,
            imageUrl = imageUrl,
            ownerId = ownerId,
            createdAt = createdAt
        )

        db.collection("jerseys").document(jerseyId).set(jersey)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Jersey saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}