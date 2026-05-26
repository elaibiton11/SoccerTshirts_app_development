package com.example.soccertshirts_app.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soccertshirts_app.R
import com.example.soccertshirts_app.data.local.AppDatabase
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.data.services.CloudinaryModel
import com.example.soccertshirts_app.databinding.FragmentAddEditJerseyBinding
import com.example.soccertshirts_app.viewmodel.AddEditJerseyViewModel
import com.example.soccertshirts_app.viewmodel.AddEditJerseyViewModelFactory
import com.squareup.picasso.Picasso

class AddEditJerseyFragment : Fragment() {

    private var _binding: FragmentAddEditJerseyBinding? = null
    private val binding get() = _binding!!
    
    private var jerseyId: String? = null
    private var selectedImageUri: Uri? = null

    private val viewModel: AddEditJerseyViewModel by viewModels {
        val jerseyDao = AppDatabase.getDatabase(requireContext()).jerseyDao()
        val repository = JerseyRepository(jerseyDao)
        AddEditJerseyViewModelFactory(repository)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivJerseyPreview.setImageURI(it)
        }
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
        _binding = FragmentAddEditJerseyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CloudinaryModel.init(requireContext())

        if (!jerseyId.isNullOrEmpty()) {
            viewModel.loadJersey(jerseyId!!)
        } else {
            // Set default country for new jersey
            val defaultCountry = "England"
            binding.actvCountry.setText(defaultCountry, false)
            viewModel.fetchTeams(defaultCountry)
        }

        binding.btnSelectImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveJersey()
        }

        binding.actvCountry.setOnItemClickListener { parent, _, position, _ ->
            val selectedCountry = parent.getItemAtPosition(position) as String
            binding.actvTeam.setText("", false) // Clear previous team selection
            viewModel.fetchTeams(selectedCountry)
        }

        observeViewModel()
    }

    private fun saveJersey() {
        val title = binding.etTitle.text.toString().trim()
        val team = binding.actvTeam.text.toString().trim()
        val country = binding.actvCountry.text.toString().trim()
        val year = binding.etYear.text.toString().trim().toIntOrNull()
        val price = binding.etPrice.text.toString().trim().toDoubleOrNull()
        val description = binding.etDescription.text.toString().trim()

        viewModel.saveJersey(title, team, country, year, price, description, selectedImageUri)
    }

    private fun observeViewModel() {
        viewModel.countries.observe(viewLifecycleOwner) { countries ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, countries)
            binding.actvCountry.setAdapter(adapter)
        }

        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teams)
            binding.actvTeam.setAdapter(adapter)
        }

        viewModel.jersey.observe(viewLifecycleOwner) { jersey ->
            jersey?.let {
                binding.etTitle.setText(it.title)
                binding.actvCountry.setText(it.country, false)
                binding.actvTeam.setText(it.team, false)
                binding.etYear.setText(it.year.toString())
                binding.etPrice.setText(it.price.toString())
                binding.etDescription.setText(it.description)
                binding.btnSave.text = "Update Jersey"
                
                if (it.imageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivJerseyPreview)
                }
            }
        }

        viewModel.isSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(requireContext(), "Jersey saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
