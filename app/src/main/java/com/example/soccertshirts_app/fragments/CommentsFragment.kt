package com.example.soccertshirts_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soccertshirts_app.R
import com.example.soccertshirts_app.adapters.CommentAdapter
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.databinding.FragmentCommentsBinding
import com.example.soccertshirts_app.viewmodel.CommentsViewModel
import com.example.soccertshirts_app.viewmodel.CommentsViewModelFactory

class CommentsFragment : Fragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    private var jerseyId: String? = null
    
    private val viewModel: CommentsViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        CommentsViewModelFactory(AuthRepository(), JerseyRepository(jerseyDao))
    }

    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jerseyId = arguments?.getString("jerseyId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (jerseyId == null) {
            Toast.makeText(requireContext(), "Error: Jersey ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        observeViewModel()

        viewModel.loadComments(jerseyId!!)

        binding.btnSendComment.setOnClickListener {
            val text = binding.etCommentText.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(jerseyId!!, text)
            } else {
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter(emptyList())
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            adapter.updateData(comments)
            // Scroll to bottom when new comments are loaded
            if (comments.isNotEmpty()) {
                binding.rvComments.scrollToPosition(comments.size - 1)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbCommentsLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSendComment.isEnabled = !isLoading
        }

        viewModel.commentAdded.observe(viewLifecycleOwner) { added ->
            if (added) {
                binding.etCommentText.text.clear()
                viewModel.resetCommentAdded()
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