package deakin.sit.planease.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import deakin.sit.planease.R;
import deakin.sit.planease.dto.Message;
import deakin.sit.planease.home.AIChatFragment;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    List<Message> messageList;
    AIChatFragment fragment;

    public MessageAdapter(List<Message> messageList, AIChatFragment fragment) {
        this.messageList = messageList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message.isAIgenerated()) {
            holder.responseBlock.setVisibility(View.VISIBLE);
            holder.padLeftBlock.setVisibility(View.GONE);
            holder.messageBlock.setVisibility(View.GONE);

            holder.responseText.setText(message.getContent());
            if (message.getGeneratedTaskArray()!=null) {
                holder.generateTaskButton.setVisibility(View.VISIBLE);
                holder.changeMessageButton.setVisibility(View.VISIBLE);
                holder.changeMessageButton.setOnClickListener(view -> {
                    fragment.changeMessage(message);
                });
                holder.generateTaskButton.setOnClickListener(view -> {
                    fragment.generateTaskBasedOnMessage(message);
                });
            } else {
                holder.generateTaskButton.setVisibility(View.GONE);
                holder.changeMessageButton.setVisibility(View.GONE);
            }
        } else {
            holder.responseBlock.setVisibility(View.GONE);
            holder.padLeftBlock.setVisibility(View.VISIBLE);
            holder.messageBlock.setVisibility(View.VISIBLE);

            holder.usernameText.setText(message.getUsername());

            String combinedMessage = "Goal: " + message.getSelectedGoalName() + "\nDate: " + message.getSelectedGoalDate() + "\n" + message.getContent();
            holder.messageText.setText(combinedMessage);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addNewMessage(Message message) {
        this.messageList.add(message);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout responseBlock, messageBlock;
        TextView usernameText, messageText, responseText;
        View padLeftBlock;

        Button generateTaskButton, changeMessageButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            responseBlock = itemView.findViewById(R.id.responseBlock);
            messageBlock = itemView.findViewById(R.id.messageBlock);
            usernameText = itemView.findViewById(R.id.usernameText);
            messageText = itemView.findViewById(R.id.messageText);
            responseText = itemView.findViewById(R.id.responseText);

            generateTaskButton = itemView.findViewById(R.id.generateTaskButton);
            changeMessageButton = itemView.findViewById(R.id.changeMessageButton);

            padLeftBlock = itemView.findViewById(R.id.padLeftBlock);
        }
    }
}
