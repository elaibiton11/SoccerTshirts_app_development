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
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.databinding.FragmentHomeBinding
import com.example.soccertshirts_app.viewmodel.AuthViewModel
import com.example.soccertshirts_app.viewmodel.AuthViewModelFactory
import com.example.soccertshirts_app.viewmodel.HomeViewModel
import com.example.soccertshirts_app.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: JerseyAdapter
    
    private val homeViewModel: HomeViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        val repository = JerseyRepository(jerseyDao)
        HomeViewModelFactory(repository)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
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
        observeViewModels()
        
        homeViewModel.loadJerseys()

        binding.btnAddJersey.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditJerseyFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = authViewModel.currentUserId
        adapter = JerseyAdapter(
            jerseys = emptyList(),
            currentUserId = currentUserId,
            onEditClick = { jersey ->
                val action = HomeFragmentDirections.actionHomeFragmentToAddEditJerseyFragment(jersey.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { jersey ->
                homeViewModel.deleteJersey(jersey)
            },
            onLikeClick = { jersey ->
                homeViewModel.toggleLike(jersey)
            }
        )
        binding.rvJerseys.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJerseys.adapter = adapter
    }

    private fun observeViewModels() {
        homeViewModel.jerseys.observe(viewLifecycleOwner) { jerseys ->
            adapter.updateData(jerseys)
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                homeViewModel.clearError()
            }
        }

        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (!isLoggedIn) {
                findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}