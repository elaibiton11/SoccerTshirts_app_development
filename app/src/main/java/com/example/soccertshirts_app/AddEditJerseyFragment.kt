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
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.services.CloudinaryModel
import com.example.soccertshirts_app.databinding.FragmentAddEditJerseyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddEditJerseyFragment : Fragment() {

    private var _binding: FragmentAddEditJerseyBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null

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

        binding.btnSelectImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            checkAndUploadImage()
        }
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
            saveJerseyToFirestore("")
        }
    }

    private fun saveJerseyToFirestore(imageUrl: String) {
        val title = binding.etTitle.text.toString()
        val team = binding.etTeam.text.toString()
        val year = binding.etYear.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()
        val ownerId = auth.currentUser?.uid ?: ""

        val jerseyRef = db.collection("jerseys").document()
        val jersey = Jersey(
            id = jerseyRef.id,
            title = title,
            team = team,
            year = year,
            price = price,
            description = description,
            imageUrl = imageUrl,
            ownerId = ownerId
        )

        jerseyRef.set(jersey)
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