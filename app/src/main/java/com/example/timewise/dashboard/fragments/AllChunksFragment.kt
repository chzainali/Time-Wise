package com.example.timewise.dashboard.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewise.adapter.ChunksAdapter
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.databinding.FragmentAllChunksBinding
import com.example.timewise.model.ChunkModel
import com.example.timewise.model.HelperClass
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Locale

class AllChunksFragment : Fragment() {
    private var _binding: FragmentAllChunksBinding? = null
    private val binding get() = _binding!!
    var listOfChunks: MutableList<ChunkModel> = ArrayList()
    lateinit var databaseHelper: DatabaseHelper
    val STORAGE_PERMISSION_REQUEST_CODE = 111
    var chunksAdapter : ChunksAdapter ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAllChunksBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        getDataFromDatabase()

        binding.tvExport.setOnClickListener {
            if (listOfChunks.size > 0){
                requestStoragePermission()
            } else {
                Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_SHORT).show()
            }
        }


        // Set up the SearchView listener for filtering products
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    chunksAdapter?.setChunkList(listOfChunks)
                } else {
                    filter(newText)
                }
                return false
            }
        })

    }

    private fun getDataFromDatabase() {
        listOfChunks.clear()
        listOfChunks = databaseHelper.getChunksData(HelperClass.users?.id!!).toMutableList()

        chunksAdapter = ChunksAdapter(listOfChunks, requireContext(), "all")
        binding.rvChunks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvChunks.adapter = chunksAdapter

        if (listOfChunks.isEmpty()){
            binding.tvNoDataFound.visibility = View.VISIBLE
            binding.rvChunks.visibility = View.GONE
            binding.searchBar.visibility = View.GONE
        }else{
            binding.tvNoDataFound.visibility = View.GONE
            binding.rvChunks.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
        }

    }

    private fun exportDataToCSV(listOfChunks: List<ChunkModel>, context: Context) {
        val fileName = "chunks_data.csv"
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(folder, fileName)

        try {
            val fileWriter = FileWriter(file)

            // Write the CSV header
            fileWriter.append("ID,User ID,Title,Activity Name,Date,Start Time,End Time,Is Reminder,Notes,Tags,Images\n")

            // Write the chunk data
            for (chunk in listOfChunks) {
                val tags = chunk.tags.joinToString(separator = "|")
                val images = chunk.images.joinToString(separator = "|")
                fileWriter.append("${chunk.id},${chunk.userId},${chunk.title},${chunk.activityName},${chunk.date},${chunk.startTime},${chunk.endTime},${chunk.isReminder},${chunk.notes},$tags,$images\n")
            }

            fileWriter.flush()
            fileWriter.close()

            // Show a message indicating successful export
            Toast.makeText(context, "Data exported to $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun filter(text: String) {
        val filteredList: MutableList<ChunkModel> = java.util.ArrayList<ChunkModel>()
        for (model in listOfChunks) {
            if (model.title?.lowercase(Locale.ROOT)
                    ?.contains(text.lowercase(Locale.getDefault())) == true
            ) {
                filteredList.add(model)
            }
        }
        chunksAdapter?.setChunkList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted
            exportDataToCSV(listOfChunks, requireContext())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                exportDataToCSV(listOfChunks, requireContext())
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}