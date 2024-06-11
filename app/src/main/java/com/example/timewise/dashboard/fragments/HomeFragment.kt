package com.example.timewise.dashboard.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewise.MapActivity
import com.example.timewise.R
import com.example.timewise.adapter.ChunksAdapter
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.databinding.FragmentHomeBinding
import com.example.timewise.model.ChunkModel
import com.example.timewise.model.HelperClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var listOfChunks: ArrayList<ChunkModel> = ArrayList()
    lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        val currentDate = System.currentTimeMillis()
        binding.calendarView.date = currentDate

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = "$dayOfMonth-${month + 1}-$year"
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val selectedDateFormat = dateFormat.parse(date)
            val formattedDate = dateFormat.format(selectedDateFormat)
            selectedDate = formattedDate
            getDataFromDatabase(selectedDate)
        }

        if (savedInstanceState == null) {
            val calendar = Calendar.getInstance()
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDateString = "$currentDayOfMonth-$currentMonth-$currentYear"
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val selectedDateFormat = dateFormat.parse(currentDateString)
            val formattedDate = dateFormat.format(selectedDateFormat)
            selectedDate = formattedDate
            getDataFromDatabase(selectedDate)
        }

        binding.ivRight.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_navigation_home_to_createChunkFragment)
        }

        binding.tvNearest.setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }

    }

    private fun getDataFromDatabase(selectedDate: String) {
        listOfChunks.clear()
        for (model : ChunkModel in databaseHelper.getChunksData(HelperClass.users?.id!!)){
            if (model.date == selectedDate){
                listOfChunks.add(model)
            }
        }

        val chunksAdapter = ChunksAdapter(listOfChunks, requireContext(), "home")
        binding.rvChunks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvChunks.adapter = chunksAdapter

        if (listOfChunks.isEmpty()){
            binding.tvNoDataFound.visibility = View.VISIBLE
            binding.rvChunks.visibility = View.GONE
        }else{
            binding.tvNoDataFound.visibility = View.GONE
            binding.rvChunks.visibility = View.VISIBLE
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var selectedDate = ""
    }

}