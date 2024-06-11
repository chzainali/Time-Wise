package com.example.timewise.dashboard.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.timewise.R
import com.example.timewise.adapter.PicturesAdapter
import com.example.timewise.adapter.TagsAdapter
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.databinding.FragmentCreateChunkBinding
import com.example.timewise.model.ChunkModel
import com.example.timewise.model.HelperClass
import com.example.timewise.notification.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.log

class CreateChunkFragment : Fragment() {
    lateinit var binding: FragmentCreateChunkBinding
    var listOfTags: ArrayList<String> = ArrayList()
    var listOfPictures: ArrayList<String> = ArrayList()
    var task: String = ""
    var date: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var activityType: String = ""
    var customActivity: String = ""
    var isReminder: String = ""
    var notes: String = ""
    var repeat: String = ""
    lateinit var databaseHelper: DatabaseHelper
    var chunkModel: ChunkModel? = null
    private val NOTIFICATION_ID = 1
    var tagsAdapter: TagsAdapter? = null
    var picturesAdapter: PicturesAdapter? = null
    var progressDialog: ProgressDialog? = null
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            chunkModel = arguments?.getSerializable("data") as ChunkModel?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCreateChunkBinding.inflate(layoutInflater)
        return binding.root
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (chunkModel != null) {
            listOfTags = chunkModel!!.tags
            listOfPictures = chunkModel!!.images
            activityType = chunkModel!!.activityName.toString()
            isReminder = chunkModel!!.isReminder.toString()
            binding.cbReminder.isChecked = isReminder == "Enable"
            activityTypeSelection()
            binding.etTask.setText(chunkModel?.title)
            binding.tvDate.text = chunkModel?.date
            startTime = chunkModel?.startTime.toString()
            endTime = chunkModel?.endTime.toString()
            date = chunkModel?.date.toString()
            binding.tvTime.text = "$startTime - $endTime"
            binding.tvDate.visibility = View.VISIBLE
            binding.tvTime.visibility = View.VISIBLE
            binding.cvRepeat.visibility = View.GONE
            if (isReminder == "Enable") {
                binding.cbReminder.visibility = View.VISIBLE
            }
            if (chunkModel?.notes?.isNotEmpty() == true) {
                binding.etNotes.visibility = View.VISIBLE
            }
            binding.etNotes.setText(chunkModel?.notes)
            binding.tvDelete.visibility = View.VISIBLE
            binding.tvCreate.text = "Update"
        }

        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setTitle(getString(R.string.app_name))
        progressDialog?.setMessage("Please wait...")
        progressDialog?.setCancelable(false)

        databaseHelper = DatabaseHelper(requireContext())

        binding.ivBack.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        activityTypeSelection()
        setAdapters()

        binding.rlDate.setOnClickListener {
            showDatePicker()
            binding.tvDate.visibility = View.VISIBLE
        }
        binding.rlTime.setOnClickListener {
            binding.tvTime.visibility = View.VISIBLE
            showTimePickerDialog()
        }
        binding.rlAddReminder.setOnClickListener {
            binding.cbReminder.visibility = View.VISIBLE
        }
        binding.rlTags.setOnClickListener {
            binding.rlAddTags.visibility = View.VISIBLE
        }
        binding.rlAddNotes.setOnClickListener {
            binding.etNotes.visibility = View.VISIBLE
        }
        binding.cvExercise.setOnClickListener {
            activityType = "Exercise"
            activityTypeSelection()
        }
        binding.cvGraduation.setOnClickListener {
            activityType = "Graduation"
            activityTypeSelection()
        }
        binding.cvProductivity.setOnClickListener {
            activityType = "Productivity"
            activityTypeSelection()
        }
        binding.cvAdd.setOnClickListener {
            activityType = "Custom"
            activityTypeSelection()
        }
        binding.etTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvTaskCount.text = "${s?.length ?: 0}/50"
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.btnAdd.setOnClickListener {
            val tag = binding.etTags.text.toString()
            if (tag.isEmpty()) {
                showMessage("Please enter tag first")
                return@setOnClickListener
            }
            listOfTags.add(tag)
            binding.etTags.setText("")
            tagsAdapter?.notifyDataSetChanged()
        }
        binding.rlAddImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
        binding.tvCreate.setOnClickListener {
            if (isValidated()) {
                repeat = binding.spRepeat.selectedItem.toString()
                if (activityType == "Custom") {
                    activityType = customActivity
                }
                if (chunkModel != null) {
                    val updatedModel = ChunkModel(
                        chunkModel?.id!!,
                        HelperClass.users?.id.toString(),
                        task,
                        activityType,
                        "",
                        date,
                        startTime,
                        endTime,
                        isReminder,
                        repeat,
                        notes,
                        listOfTags,
                        listOfPictures
                    )
                    databaseHelper.updateChunk(updatedModel)
                    showMessage("Successfully Updated")
                    Navigation.findNavController(it).navigateUp()
                } else {
                    progressDialog?.show()
                    val model = ChunkModel(
                        0,
                        HelperClass.users?.id.toString(),
                        task,
                        activityType,
                        "",
                        date,
                        startTime,
                        endTime,
                        isReminder,
                        repeat,
                        notes,
                        listOfTags,
                        listOfPictures
                    )
                    addEventsToCalendar(model)
                }

            }
        }
        binding.tvDelete.setOnClickListener {
            if (chunkModel != null) {
                databaseHelper.deleteChunk(chunkModel?.id!!)
                showMessage("Deleted Successfully")
                Navigation.findNavController(it).navigateUp()
            }
        }

    }

    fun getDayNameFromDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }
    }

    private fun addEventsToCalendar(chunkModel: ChunkModel) {
        val dayName = getDayNameFromDate(date)
        // Define event start and end date time
        val calendar = Calendar.getInstance()

        // Set the date based on the selected date
        val selectedDate = parseDateTime(chunkModel.date ?: "", chunkModel.startTime ?: "")
        if (selectedDate != null) {
            calendar.timeInMillis = selectedDate.timeInMillis
        } else {
            // Handle invalid date
        }

        // Set the start time
        var startHour = chunkModel.startTime?.substringBefore(":")?.toInt() ?: 0
        val startMinute =
            chunkModel.startTime?.substringAfter(":")?.substringBefore(" ")?.toInt() ?: 0
        val startAMPM = chunkModel.startTime?.substringAfterLast(" ")
        if (startAMPM != null && startAMPM.equals("PM", ignoreCase = true) && startHour != 12) {
            startHour += 12
        }
        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, startMinute)
        val eventStartDateTime = calendar.clone() as Calendar

        // Set the end time
        var endHour = chunkModel.endTime?.substringBefore(":")?.toInt() ?: 0
        val endMinute = chunkModel.endTime?.substringAfter(":")?.substringBefore(" ")?.toInt() ?: 0
        val endAMPM = chunkModel.endTime?.substringAfterLast(" ")
        if (endAMPM != null && endAMPM.equals("PM", ignoreCase = true) && endHour != 12) {
            endHour += 12
        }
        calendar.set(Calendar.HOUR_OF_DAY, endHour)
        calendar.set(Calendar.MINUTE, endMinute)
        val eventEndDateTime = calendar.clone() as Calendar
        Log.d("addEventsToCalendar", "addEventsToCalendar: "+repeat+", "+dayName)
        when (repeat) {
            "Never" -> {
                val model = ChunkModel(
                    0,
                    chunkModel.userId,
                    chunkModel.title,
                    chunkModel.activityName,
                    chunkModel.activityPicture,
                    chunkModel.date,
                    chunkModel.startTime,
                    chunkModel.endTime,
                    chunkModel.isReminder,
                    chunkModel.isRepeat,
                    chunkModel.notes,
                    chunkModel.tags,
                    chunkModel.images
                )
                val insertedId = databaseHelper.insertChunk(model)
                insertEventIntoCalendar(chunkModel, chunkModel.date ?: "", insertedId)
                progressDialog?.dismiss()
                showMessage("Created Successfully")
                Navigation.findNavController(binding.root).navigateUp()
            }
            dayName -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                val currentYear = calendar.get(Calendar.YEAR)
                val calendarYear = Calendar.getInstance()
                calendarYear.set(Calendar.YEAR, currentYear)

                for (dayOfYear in 1..calendarYear.getActualMaximum(Calendar.DAY_OF_YEAR)) {
                    calendarYear.set(Calendar.DAY_OF_YEAR, dayOfYear)
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val dateOfYear = dateFormat.format(calendarYear.time)

                    // If the day name matches the repeat day, add the task for this day
                    if (getDayNameFromDate(dateOfYear) == repeat) {
                        val model = ChunkModel(
                            0,
                            chunkModel.userId,
                            chunkModel.title,
                            chunkModel.activityName,
                            chunkModel.activityPicture,
                            dateOfYear,
                            chunkModel.startTime,
                            chunkModel.endTime,
                            chunkModel.isReminder,
                            chunkModel.isRepeat,
                            chunkModel.notes,
                            chunkModel.tags,
                            chunkModel.images
                        )
                        val insertedId = databaseHelper.insertChunk(model)
                        insertEventIntoCalendar(chunkModel, dateOfYear, insertedId)
                    }
                }
                progressDialog?.dismiss()
                showMessage("Created Successfully")
                Navigation.findNavController(binding.root).navigateUp()
            }
            else -> {
                progressDialog?.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Selected date day and the repeat day should be same",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun insertEventIntoCalendar(chunkModel: ChunkModel, date: String, chunkId: Long) {
        if (chunkModel.isReminder == "Enable"){
            scheduleNotification(chunkModel.date!!, chunkId)
        }
        val contentResolver = requireContext().contentResolver
        val eventStartDateTime = parseDateTime(date, chunkModel.startTime ?: "") ?: return
        val eventEndDateTime = parseDateTime(date, chunkModel.endTime ?: "") ?: return

        val eventValues = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, eventStartDateTime.timeInMillis)
            put(CalendarContract.Events.DTEND, eventEndDateTime.timeInMillis)
            put(CalendarContract.Events.TITLE, chunkModel.title)
            put(CalendarContract.Events.DESCRIPTION, chunkModel.notes)
            put(CalendarContract.Events.CALENDAR_ID, getCalendarId())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        val eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
        val eventId = eventUri?.lastPathSegment?.toLong() ?: -1
    }

    @SuppressLint("Range")
    private fun getCalendarId(): Long {
        // Query the calendar to get the calendar ID
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
        val cursor = requireContext().contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID))
            }
        }

        // Return -1 if calendar ID not found
        return -1
    }

    private fun setAdapters() {
        tagsAdapter = TagsAdapter(listOfTags, requireContext())
        binding.rvTags.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.rvTags.adapter = tagsAdapter

        picturesAdapter = PicturesAdapter(listOfPictures, requireContext())
        binding.rvPictures.layoutManager =
            GridLayoutManager(requireContext(), 3)
        binding.rvPictures.adapter = picturesAdapter
    }

    private fun activityTypeSelection() {

        when (activityType) {

            "" -> {

            }

            "Exercise" -> {
                binding.cvExercise.setBackgroundColor(requireActivity().getColor(R.color.mainColor))
                binding.cvGraduation.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvProductivity.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvAdd.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.etActivityType.visibility = View.GONE
            }

            "Graduation" -> {
                binding.cvGraduation.setBackgroundColor(requireActivity().getColor(R.color.mainColor))
                binding.cvExercise.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvProductivity.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvAdd.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.etActivityType.visibility = View.GONE
            }

            "Productivity" -> {
                binding.cvProductivity.setBackgroundColor(requireActivity().getColor(R.color.mainColor))
                binding.cvGraduation.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvExercise.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvAdd.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.etActivityType.visibility = View.GONE
            }

            else -> {
                binding.cvExercise.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvGraduation.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvProductivity.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.cvAdd.setBackgroundColor(requireActivity().getColor(R.color.mainColor))
                binding.etActivityType.visibility = View.VISIBLE
                if (chunkModel != null) {
                    if ((chunkModel?.activityName != "Exercise") &&
                        (chunkModel?.activityName != "Graduation") &&
                        (chunkModel?.activityName != "Productivity")
                    ) {
                        binding.etActivityType.setText(chunkModel?.activityName)
                    }
                }
            }
        }
    }

    private fun isValidated(): Boolean {
        task = binding.etTask.text.toString().trim()
        notes = binding.etNotes.text.toString().trim()
        customActivity = binding.etActivityType.text.toString().trim()

        isReminder = if (binding.cbReminder.isChecked) {
            "Enable"
        } else {
            "Disable"
        }

        if (task.isEmpty()) {
            showMessage("Please enter task")
            return false
        }
        if (activityType.isEmpty()) {
            showMessage("Please select activity type")
            return false
        }
        if (activityType == "Custom") {
            if (customActivity.isEmpty()) {
                showMessage("Please enter activity")
                return false
            }
        }

        if (date.isEmpty()) {
            showMessage("Please select date")
            return false
        }

        if (startTime.isEmpty() || endTime.isEmpty()) {
            showMessage("Please select start and end time")
            return false
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

        val startDateTime = dateFormat.parse("$date $startTime")
        val endDateTime = dateFormat.parse("$date $endTime")

        if (endDateTime.before(startDateTime)) {
            showMessage("End time must be greater than start time")
            return false
        }

        return true
    }

    private fun scheduleNotification(reminderDate: String, workoutId: Long) {
        val notificationTime: Long = parseDateTime(
            reminderDate,
            startTime
        )?.timeInMillis ?: 0

        val notificationIntent = Intent(requireContext(), AlarmReceiver::class.java)
        notificationIntent.putExtra("chunkId", workoutId)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePickerDialog() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_time_picker)

        val fromTimePicker = dialog.findViewById<TimePicker>(R.id.fromTimePicker)
        val toTimePicker = dialog.findViewById<TimePicker>(R.id.toTimePicker)
        val btnDone = dialog.findViewById<AppCompatButton>(R.id.btnDone)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        fromTimePicker.hour = hour
        fromTimePicker.minute = minute
        toTimePicker.hour = hour
        toTimePicker.minute = minute

        btnDone.setOnClickListener {
            val fromHour = fromTimePicker.hour
            val fromMinute = fromTimePicker.minute
            val toHour = toTimePicker.hour
            val toMinute = toTimePicker.minute

            val fromTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val toTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            val fromTime = fromTimeFormat.format(getTime(fromHour, fromMinute))
            val toTime = toTimeFormat.format(getTime(toHour, toMinute))

            startTime = fromTime
            endTime = toTime
            binding.tvTime.text = "$startTime - $endTime"

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getTime(hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return calendar.time
    }


    private fun parseDateTime(date: String, time: String): Calendar? {
        return try {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            calendar.time = dateFormat.parse("$date $time")
            calendar
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Show a toast message
    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                date = formattedDate
                binding.tvDate.text = formattedDate
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            listOfPictures.add(data!!.data.toString())
            picturesAdapter?.notifyDataSetChanged()
        }

    }

}