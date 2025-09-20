package com.koshpal.studentapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ChatbotActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialButton
    
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var geminiApiService: GeminiApiService
    private lateinit var sharedPreferences: SharedPreferences
    
    private val chatMessages = mutableListOf<ChatMessage>()
    private var userName: String = ""
    
    companion object {
        private const val TAG = "ChatbotActivity"
        private const val PREF_NAME = "StudentAppPrefs"
        private const val KEY_USER_NAME = "userName"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatbot)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initServices()
        initViews()
        getUserData()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        showWelcomeMessage()
    }
    
    private fun initServices() {
        geminiApiService = GeminiApiService()
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
    }
    
    private fun getUserData() {
        userName = intent.getStringExtra("USER_NAME") 
            ?: sharedPreferences.getString(KEY_USER_NAME, "Student") ?: "Student"
        
        Log.d(TAG, "User name: $userName")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "AI Assistant"
            subtitle = "24/7 Student Support"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatbotActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnSend.setOnClickListener {
            sendMessage()
        }
        
        etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun showWelcomeMessage() {
        val welcomeText = """
            Hi $userName! 👋
            
            I'm your AI assistant, here to help you 24/7 with:
            • Academic guidance and study tips
            • Career counseling advice
            • Mental health support
            • General student queries
            
            Feel free to ask me anything!
        """.trimIndent()
        
        val welcomeMessage = ChatMessage.createBotMessage(welcomeText)
        chatAdapter.addMessage(welcomeMessage)
        scrollToBottom()
    }
    
    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Clear input
        etMessage.text.clear()
        
        // Add user message
        val userMessage = ChatMessage.createUserMessage(messageText)
        chatAdapter.addMessage(userMessage)
        scrollToBottom()
        
        // Show typing indicator
        chatAdapter.addTypingIndicator()
        scrollToBottom()
        
        // Disable send button
        setSendButtonEnabled(false)
        
        // Send to Gemini API
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Sending message to Gemini API: $messageText")
                val result = geminiApiService.sendMessage(messageText)
                
                // Remove typing indicator
                chatAdapter.removeTypingIndicator()
                
                if (result.isSuccess) {
                    val botResponse = result.getOrNull() ?: "I'm sorry, I couldn't process your request."
                    val botMessage = ChatMessage.createBotMessage(botResponse)
                    chatAdapter.addMessage(botMessage)
                    Log.d(TAG, "Bot response added: $botResponse")
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "API call failed", error)
                    val errorMessage = ChatMessage.createBotMessage(
                        "I'm sorry, I'm having trouble connecting right now. Please try again in a moment."
                    )
                    chatAdapter.addMessage(errorMessage)
                }
                
                scrollToBottom()
                setSendButtonEnabled(true)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                
                // Remove typing indicator
                chatAdapter.removeTypingIndicator()
                
                val errorMessage = ChatMessage.createBotMessage(
                    "I'm sorry, something went wrong. Please try again."
                )
                chatAdapter.addMessage(errorMessage)
                scrollToBottom()
                setSendButtonEnabled(true)
            }
        }
    }
    
    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            recyclerView.smoothScrollToPosition(chatMessages.size - 1)
        }
    }
    
    private fun setSendButtonEnabled(enabled: Boolean) {
        btnSend.isEnabled = enabled
        btnSend.alpha = if (enabled) 1.0f else 0.6f
    }
}
