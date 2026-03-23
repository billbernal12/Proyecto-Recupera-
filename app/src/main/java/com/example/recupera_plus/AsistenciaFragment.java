package com.example.recupera_plus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AsistenciaFragment extends Fragment {

    private RecyclerView recyclerMessages;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private DatabaseReference chatRef, respuestasRef;

    public AsistenciaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflamos el layout con título
        View view = inflater.inflate(R.layout.fragment_asistencia, container, false);

        // Cargamos el chat bot dentro del FrameLayout del fragment
        ViewGroup chatContainer = view.findViewById(R.id.chatBotContainer);
        LayoutInflater.from(getContext()).inflate(R.layout.activity_chat_bot, chatContainer, true);

        recyclerMessages = chatContainer.findViewById(R.id.recyclerMessages);
        etMessage = chatContainer.findViewById(R.id.etMessage);
        btnSend = chatContainer.findViewById(R.id.btnSend);

        adapter = new ChatAdapter();
        recyclerMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMessages.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("chatbot/messages");
        respuestasRef = database.getReference("chatbot/respuestas");

        btnSend.setOnClickListener(v -> sendMessage());

        // 🎯 Manejo de opciones de los botones del bot
        adapter.setOnOptionSelectedListener(this::handleBotOption);

        // Mostrar mensaje de bienvenida
        seedWelcomeMessage();

        return view;
    }

    private void seedWelcomeMessage() {
        Message welcome = new Message(
                "¡Hola! Soy el chat bot de Recupera+. ¿En qué puedo ayudarte hoy?",
                "bot",
                System.currentTimeMillis()
        );
        chatRef.push().setValue(welcome);
        adapter.addMessage(welcome);
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        Message userMsg = new Message(text, "user", System.currentTimeMillis());
        chatRef.push().setValue(userMsg);
        adapter.addMessage(userMsg);

        etMessage.setText("");
        generateBotReply(text);
    }

    private void handleBotOption(String option) {
        Message userMsg = new Message(option, "user", System.currentTimeMillis());
        chatRef.push().setValue(userMsg);
        adapter.addMessage(userMsg);

        generateBotReply(option);
    }

    private void generateBotReply(String userText) {
        respuestasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot data = task.getResult();
                if (data == null) return;

                String botReply = data.child("default").getValue(String.class);

                if (userText.toLowerCase().contains("historia")) {
                    botReply = data.child("historia").getValue(String.class);
                } else if (userText.toLowerCase().contains("diagnost")) {
                    botReply = data.child("diagnostico").getValue(String.class);
                } else if (userText.toLowerCase().contains("contact")) {
                    botReply = data.child("contacto").getValue(String.class);
                }

                if (botReply != null) {
                    Message botMsg = new Message(botReply, "bot", System.currentTimeMillis());
                    chatRef.push().setValue(botMsg);
                    adapter.addMessage(botMsg);
                }
            }
        });
    }
}
