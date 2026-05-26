package com.example.soccertshirts_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soccertshirts_app.R
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.databinding.FragmentJerseyDetailsBinding
import com.example.soccertshirts_app.viewmodel.JerseyDetailsViewModel
import com.example.soccertshirts_app.viewmodel.JerseyDetailsViewModelFactory
import com.squareup.picasso.Picasso

class JerseyDetailsFragment : Fragment() {

    private var _binding: FragmentJerseyDetailsBinding? = null
    private val binding get() = _binding!!

    private var jerseyId: String? = null
    
    private val viewModel: JerseyDetailsViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        JerseyDetailsViewModelFactory(AuthRepository(), JerseyRepository(jerseyDao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jerseyId = arguments?.getString("jerseyId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJerseyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (jerseyId != null) {
            viewModel.loadJerseyDetails(jerseyId!!)
        }

        observeViewModel()

        binding.ibDetailsLike.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.ibDetailsComment.setOnClickListener {
            val bundle = Bundle().apply { putString("jerseyId", jerseyId) }
            findNavController().navigate(R.id.action_jerseyDetailsFragment_to_commentsFragment, bundle)
        }
    }

    private fun observeViewModel() {
        viewModel.jersey.observe(viewLifecycleOwner) { jersey ->
            jersey?.let {
                binding.tvDetailsTitle.text = it.title
                binding.tvDetailsTeamYear.text = "${it.team} | ${it.year}"
                binding.tvDetailsPrice.text = "$${it.price}"
                binding.tvDetailsDescription.text = it.description
                binding.tvDetailsOwnerName.text = it.ownerName.ifEmpty { "Anonymous" }
                binding.tvDetailsLikesCount.text = it.likesCount.toString()
                binding.tvDetailsCommentsCount.text = it.commentsCount.toString()

                // Like status
                binding.ibDetailsLike.setImageResource(
                    if (viewModel.isLikedByUser()) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart_outline
                )

                // Load Images
                if (it.imageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivDetailsImage)
                }

                if (it.ownerProfileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.ownerProfileImageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivDetailsOwnerProfile)
                } else {
                    binding.ivDetailsOwnerProfile.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbDetailsLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
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