package com.example.timewise.dashboard.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.timewise.R
import com.example.timewise.databinding.FragmentCreateChunkBinding
import com.example.timewise.databinding.FragmentSelectTimeBinding

class SelectTimeFragment : Fragment() {
    lateinit var binding: FragmentSelectTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSelectTimeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            val navController = Navigation.findNavController(it)
            val args = Bundle().apply {
                putString("startTime", binding.fromTime.hour.toString() + ":" + binding.fromTime.minute.toString())
                putString("endTime", binding.toTime.hour.toString() + ":" + binding.toTime.minute.toString())
            }
            navController.previousBackStackEntry?.savedStateHandle?.set("timeSelection", args)
            navController.navigateUp()
        }

    }

}