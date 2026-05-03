package com.example.soccertshirts_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: JerseyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchJerseys()

        binding.btnAddJersey.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditJerseyFragment)
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = JerseyAdapter(emptyList())
        binding.rvJerseys.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJerseys.adapter = adapter
    }

    private fun fetchJerseys() {
        db.collection("jerseys")
            .get()
            .addOnSuccessListener { result ->
                val jerseys = result.toObjects(Jersey::class.java)
                adapter.updateData(jerseys)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}