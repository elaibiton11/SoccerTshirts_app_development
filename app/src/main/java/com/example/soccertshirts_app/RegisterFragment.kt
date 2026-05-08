package com.example.soccertshirts_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.databinding.FragmentRegisterBinding
import com.example.soccertshirts_app.viewmodel.AuthViewModel
import com.example.soccertshirts_app.viewmodel.AuthViewModelFactory

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.register(email, password)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            }
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