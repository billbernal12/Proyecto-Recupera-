package com.example.recupera_plus;

import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private OnOptionSelectedListener onOptionSelectedListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 0 ? R.layout.item_message_user : R.layout.item_message_bot;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.textViewMessage.setText(msg.getText());

        // Hacer que los enlaces sean clicables
        holder.textViewMessage.setAutoLinkMask(Linkify.WEB_URLS);
        holder.textViewMessage.setMovementMethod(LinkMovementMethod.getInstance());

        // Solo mostramos botones si el mensaje es del bot y es el primero
        if (msg.getSender().equals("bot") && position == 0) {
            if (holder.btnGenerarHistoria != null)
                holder.btnGenerarHistoria.setVisibility(View.VISIBLE);
            if (holder.btnDiagnosticar != null)
                holder.btnDiagnosticar.setVisibility(View.VISIBLE);
            if (holder.btnContactar != null)
                holder.btnContactar.setVisibility(View.VISIBLE);
        } else {
            if (holder.btnGenerarHistoria != null)
                holder.btnGenerarHistoria.setVisibility(View.GONE);
            if (holder.btnDiagnosticar != null)
                holder.btnDiagnosticar.setVisibility(View.GONE);
            if (holder.btnContactar != null)
                holder.btnContactar.setVisibility(View.GONE);
        }

        // Listeners seguros (solo existen en layout del bot)
        if (holder.btnGenerarHistoria != null) {
            holder.btnGenerarHistoria.setOnClickListener(v -> {
                if (onOptionSelectedListener != null)
                    onOptionSelectedListener.onOptionSelected("historia");
            });
        }

        if (holder.btnDiagnosticar != null) {
            holder.btnDiagnosticar.setOnClickListener(v -> {
                if (onOptionSelectedListener != null)
                    onOptionSelectedListener.onOptionSelected("diagnostico");
            });
        }

        if (holder.btnContactar != null) {
            holder.btnContactar.setOnClickListener(v -> {
                if (onOptionSelectedListener != null)
                    onOptionSelectedListener.onOptionSelected("contacto");
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSender().equals("user") ? 0 : 1;
    }

    // 🔹 ViewHolder flexible (detecta si tiene botones o no)
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        Button btnGenerarHistoria, btnDiagnosticar, btnContactar;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);

            // Solo inflamos los botones si es el layout del bot
            if (viewType == 1) {
                btnGenerarHistoria = itemView.findViewById(R.id.btnGenerarHistoria);
                btnDiagnosticar = itemView.findViewById(R.id.btnDiagnosticar);
                btnContactar = itemView.findViewById(R.id.btnContactar);
            }
        }
    }

    // Listener de opciones del bot
    public interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }

    public void setOnOptionSelectedListener(OnOptionSelectedListener listener) {
        this.onOptionSelectedListener = listener;
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}