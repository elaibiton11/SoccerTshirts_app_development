package com.example.soccertshirts_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.FragmentAddEditJerseyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEditJerseyFragment : Fragment() {

    private var _binding: FragmentAddEditJerseyBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

        binding.btnSave.setOnClickListener {
            saveJersey()
        }
    }

    private fun saveJersey() {
        val title = binding.etTitle.text.toString()
        val team = binding.etTeam.text.toString()
        val year = binding.etYear.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()
        val ownerId = auth.currentUser?.uid ?: ""

        if (ownerId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val jerseyRef = db.collection("jerseys").document()
        val jersey = Jersey(
            id = jerseyRef.id,
            title = title,
            team = team,
            year = year,
            price = price,
            description = description,
            ownerId = ownerId
        )

        jerseyRef.set(jersey)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Jersey saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}