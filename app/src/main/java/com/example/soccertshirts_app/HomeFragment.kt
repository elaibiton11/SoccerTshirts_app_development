package com.example.soccertshirts_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.local.entity.JerseyEntity
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

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
        loadLocalData()
        fetchJerseysFromFirestore()

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

    private fun loadLocalData() {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        lifecycleScope.launch {
            val localJerseys = jerseyDao.getAllJerseys()
            if (localJerseys.isNotEmpty()) {
                val jerseyModels = localJerseys.map { entity ->
                    Jersey(
                        entity.id, entity.title, entity.team, entity.year,
                        entity.price, entity.description, entity.imageUrl, entity.ownerId,
                        entity.createdAt
                    )
                }
                adapter.updateData(jerseyModels)
            }
        }
    }

    private fun fetchJerseysFromFirestore() {
        db.collection("jerseys")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val jerseys = result.toObjects(Jersey::class.java)
                adapter.updateData(jerseys)
                saveToLocal(jerseys)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToLocal(jerseys: List<Jersey>) {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        val entities = jerseys.map { model ->
            JerseyEntity(
                model.id, model.title, model.team, model.year,
                model.price, model.description, model.imageUrl, model.ownerId,
                model.createdAt
            )
        }
        lifecycleScope.launch {
            jerseyDao.insertAll(entities)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}