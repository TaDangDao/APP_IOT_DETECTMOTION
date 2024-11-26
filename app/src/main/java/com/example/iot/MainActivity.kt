package com.example.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iot.ui.theme.IOTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : ComponentActivity() {

        private lateinit var recyclerView: RecyclerView
        private lateinit var emptyTextView: TextView
        private lateinit var adapter: MotionAdapter
        private val motionList = mutableListOf<Motion>()
        private var isAdmin = false // Biến xác định vai trò người dùng
       private lateinit var database: DatabaseReference
    private lateinit var btnLogout: Button
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val currentUser = FirebaseAuth.getInstance().currentUser
            setContentView(R.layout.activity_main)
            recyclerView = findViewById(R.id.recyclerView)
            emptyTextView = findViewById(R.id.emptyTextView)
            database = FirebaseDatabase.getInstance().reference
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = MotionAdapter(motionList, isAdmin) { motionId -> deleteMotion(motionId) }
            findViewById<RecyclerView>(R.id.recyclerView).apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = this@MainActivity.adapter
            }
            val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            recyclerView.addItemDecoration(dividerItemDecoration)
            fetchAllData()
            btnLogout = findViewById(R.id.btnLogout)

            // Gán sự kiện click cho nút Logout
            btnLogout.setOnClickListener {
                logoutUser()
            }
            currentUser?.let { user ->
                val userRef = database.child("users").child(user.uid)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.getValue(User::class.java)
                        if (userData != null) {
                            isAdmin = userData.role == "Admin" // Kiểm tra vai trò admin
                            val deviceId = userData.deviceId     // Lấy deviceId của người dùng
                            if (isAdmin) {
                                fetchAllData() // Nếu là admin, lấy tất cả dữ liệu
                            } else {
                                Log.d("deviceID", deviceId)
                                fetchData(deviceId) // Nếu là người dùng thường, chỉ lấy dữ liệu của thiết bị họ
                            }
                        } else {
                            Log.e("MainActivity", "User data not found!")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MainActivity", "Error fetching user data", error.toException())
                    }
                })
            }
        }

    private fun fetchAllData() {
        val dbRef = FirebaseDatabase.getInstance().getReference("motions")

        // Lắng nghe sự thay đổi từ Realtime Database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                motionList.clear() // Xóa danh sách cũ
                for (childSnapshot in snapshot.children) {
                    val motion = childSnapshot.getValue(Motion::class.java)
                    if (motion != null) {
                        motionList.add(motion)
                    }
                }
                updateUI() // Cập nhật giao diện
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDatabase", "Error getting data", error.toException())
            }
        })
    }
    private fun fetchData(deviceId: String) {
        // Trỏ tới nút dữ liệu dựa trên id của thiết bị
        val dbRef = FirebaseDatabase.getInstance().getReference("motions")

        // Lắng nghe sự thay đổi từ Realtime Database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                motionList.clear() // Xóa danh sách cũ
                for (childSnapshot in snapshot.children) {
                    val motion = childSnapshot.getValue(Motion::class.java)
                    if (motion != null) {
                        if(motion.id==deviceId) {
                            motionList.add(motion) // Thêm dữ liệu vào danh sách
                        }
                    }
                }
                updateUI() // Cập nhật giao diện
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDatabase", "Error getting data", error.toException())
            }
        })
    }



    private fun updateUI() {
            if (motionList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyTextView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
            }
            adapter.notifyDataSetChanged()
        }
    private fun logoutUser() {
        // Đăng xuất Firebase Auth
        FirebaseAuth.getInstance().signOut()

        // Chuyển hướng đến LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Xóa stack để tránh quay lại MainActivity
        startActivity(intent)

        // Hiển thị thông báo (tùy chọn)
        finish()
    }
    private fun deleteMotion(motionId: String) {
        if (!isAdmin) {
            Toast.makeText(this, "Chỉ Admin mới có quyền xóa!", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("motions").child(motionId).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi khi xóa dữ liệu!", Toast.LENGTH_SHORT).show()
        }
    }
    }

