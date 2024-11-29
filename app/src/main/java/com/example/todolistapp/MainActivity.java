package com.example.todolistapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button buttonAdd;
    private ListView listViewTasks;

    private ArrayList<Task> taskList;
    private ArrayAdapter<Task> adapter;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "todoListPrefs";
    private static final String TASKS_KEY = "tasks";

    private String selectedDateTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAdd = findViewById(R.id.buttonAdd);
        listViewTasks = findViewById(R.id.listViewTasks);

        taskList = loadTasks();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        listViewTasks.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> showTaskDialog(-1));

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> showTaskDialog(position));
        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDeleteTask(position);
            return true;
        });
    }

    private void showTaskDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position == -1 ? "Add Task" : "Edit Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_task, null);
        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        Button buttonSelectDateTime = dialogView.findViewById(R.id.buttonSelectDateTime);

        if (position != -1) {
            Task task = taskList.get(position);
            editTextTask.setText(task.getName());
            textViewDateTime.setText(task.getDateTime());
        }

        buttonSelectDateTime.setOnClickListener(v -> pickDateTime(textViewDateTime));

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String taskName = editTextTask.getText().toString().trim();
            if (!taskName.isEmpty() && !selectedDateTime.isEmpty()) {
                if (position == -1) {
                    taskList.add(new Task(taskName, selectedDateTime));
                    Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                } else {
                    Task task = taskList.get(position);
                    task.setName(taskName);
                    task.setDateTime(selectedDateTime);
                    Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                saveTasks();
            } else {
                Toast.makeText(this, "Please enter task name and date/time", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void pickDateTime(TextView textViewDateTime) {
        Calendar calendar = Calendar.getInstance();

        // Hiển thị DatePickerDialog
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Hiển thị TimePickerDialog sau khi chọn ngày
            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                selectedDateTime = dateFormat.format(calendar.getTime());
                textViewDateTime.setText(selectedDateTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void confirmDeleteTask(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            taskList.remove(position);
            adapter.notifyDataSetChanged();
            saveTasks();
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void saveTasks() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> taskSet = new HashSet<>();
        for (Task task : taskList) {
            taskSet.add(task.getName() + "%%" + task.getDateTime());
        }
        editor.putStringSet(TASKS_KEY, taskSet);
        editor.apply();
    }

    private ArrayList<Task> loadTasks() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        Set<String> taskSet = sharedPreferences.getStringSet(TASKS_KEY, new HashSet<>());
        ArrayList<Task> tasks = new ArrayList<>();
        for (String taskString : taskSet) {
            String[] parts = taskString.split("%%");
            if (parts.length == 2) {
                tasks.add(new Task(parts[0], parts[1]));
            }
        }
        return tasks;
    }
}

