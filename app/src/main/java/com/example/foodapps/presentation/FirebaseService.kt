package com.example.foodapps.presentation

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {
    private val db = FirebaseFirestore.getInstance()

    fun listenForTasks(onTasksReceived: (List<Task>) -> Unit) {
        db.collection("tasks").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { doc ->
                    val task = doc.toObject(Task::class.java)
                    task?.copy(id = doc.id)
                }
                onTasksReceived(tasks)
            }
        }
    }
    fun addTask(task: Task, onComplete: () -> Unit) {
        db.collection("tasks").add(task)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Task added successfully")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error adding task: ", e)
            }
    }


    fun updateTask(taskId: String, isCompleted: Boolean) {
        db.collection("tasks").document(taskId).update("isCompleted", isCompleted)
    }

    fun deleteTask(taskId: String, onComplete: () -> Unit = {}) {
        db.collection("tasks").document(taskId).delete()
            .addOnSuccessListener {
                Log.d("FirebaseService", "Task deleted successfully")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error deleting task: ", e)
            }
    }
}
