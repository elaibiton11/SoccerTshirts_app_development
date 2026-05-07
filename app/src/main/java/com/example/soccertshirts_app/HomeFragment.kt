package com.example.soccertshirts_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.databinding.FragmentHomeBinding
import com.example.soccertshirts_app.viewmodel.HomeViewModel
import com.example.soccertshirts_app.viewmodel.HomeViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: JerseyAdapter
    
    private val viewModel: HomeViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        val repository = JerseyRepository(jerseyDao)
        HomeViewModelFactory(repository)
    }

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
        observeViewModel()
        
        viewModel.loadJerseys()

        binding.btnAddJersey.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditJerseyFragment)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid
        adapter = JerseyAdapter(
            jerseys = emptyList(),
            currentUserId = currentUserId,
            onEditClick = { jersey ->
                val action = HomeFragmentDirections.actionHomeFragmentToAddEditJerseyFragment(jersey.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { jersey ->
                viewModel.deleteJersey(jersey)
            }
        )
        binding.rvJerseys.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJerseys.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.jerseys.observe(viewLifecycleOwner) { jerseys ->
            adapter.updateData(jerseys)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}