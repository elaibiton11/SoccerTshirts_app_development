package com.example.soccertshirts_app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.data.services.CloudinaryModel
import com.example.soccertshirts_app.databinding.FragmentProfileBinding
import com.example.soccertshirts_app.viewmodel.ProfileViewModel
import com.example.soccertshirts_app.viewmodel.ProfileViewModelFactory
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        ProfileViewModelFactory(AuthRepository(), JerseyRepository(jerseyDao))
    }

    private lateinit var adapter: JerseyAdapter
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CloudinaryModel.init(requireContext())

        setupRecyclerView()
        observeViewModel()
        
        viewModel.loadProfile()

        binding.btnSelectProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            val username = binding.etProfileUsername.text.toString().trim()
            viewModel.updateProfile(username, selectedImageUri)
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = AuthRepository().getCurrentUser()?.uid
        adapter = JerseyAdapter(
            jerseys = emptyList(),
            currentUserId = currentUserId,
            onItemClick = { jersey ->
                val action = ProfileFragmentDirections.actionProfileFragmentToJerseyDetailsFragment(jersey.id)
                findNavController().navigate(action)
            },
            onEditClick = { jersey ->
                val action = ProfileFragmentDirections.actionProfileFragmentToAddEditJerseyFragment(jersey.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { jersey ->
                Toast.makeText(context, "Use Home screen to delete jerseys", Toast.LENGTH_SHORT).show()
            },
            onLikeClick = { jersey ->
                viewModel.toggleLike(jersey)
            },
            onCommentClick = { jersey ->
                val action = ProfileFragmentDirections.actionProfileFragmentToCommentsFragment(jersey.id)
                findNavController().navigate(action)
            }
        )
        binding.rvMyJerseys.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyJerseys.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.etProfileUsername.setText(it.username)
                if (it.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivProfileImage)
                } else {
                    binding.ivProfileImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        viewModel.userJerseys.observe(viewLifecycleOwner) { jerseys ->
            adapter.updateData(jerseys)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbProfileLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveProfile.isEnabled = !isLoading
            binding.btnSelectProfileImage.isEnabled = !isLoading
        }

        viewModel.isUpdated.observe(viewLifecycleOwner) { isUpdated ->
            if (isUpdated) {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdated()
                selectedImageUri = null
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
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