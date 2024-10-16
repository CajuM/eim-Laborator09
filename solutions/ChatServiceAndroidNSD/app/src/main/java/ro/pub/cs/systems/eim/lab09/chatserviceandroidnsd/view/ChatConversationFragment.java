package ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.R;
import ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.general.Constants;
import ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.model.Message;
import ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.networkservicediscoveryoperations.ChatClient;
import ro.pub.cs.systems.eim.lab09.chatserviceandroidnsd.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations;

public class ChatConversationFragment extends Fragment {

    private LinearLayout chatCommunicationHistoryLinearLayout = null;
    private EditText messageEditText = null;

    private ChatClient chatClient = null;

    private final SendMessageButtonClickListener sendMessageButtonClickListener = new SendMessageButtonClickListener();

    private class SendMessageButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String message = messageEditText.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "You should fill a message!", Toast.LENGTH_SHORT).show();
            } else {
                messageEditText.setText("");
                chatClient.sendMessage(message);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup parent, Bundle state) {
        return layoutInflater.inflate(R.layout.fragment_chat_conversation, parent, false);
    }

    public synchronized void appendMessage(final Message message) {
        chatCommunicationHistoryLinearLayout.post(() -> {
            TextView messageTextView = new TextView(getActivity());
            messageTextView.setText(message.getContent());
            LinearLayout.LayoutParams messageTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            switch (message.getType()) {
                case Constants.MESSAGE_TYPE_SENT:
                    messageTextView.setBackgroundResource(R.drawable.frame_border_sent_message);
                    messageTextView.setGravity(Gravity.START);
                    messageTextViewLayoutParams.gravity = Gravity.START;
                    break;
                case Constants.MESSAGE_TYPE_RECEIVED:
                    messageTextView.setBackgroundResource(R.drawable.frame_border_received_message);
                    messageTextView.setGravity(Gravity.END);
                    messageTextViewLayoutParams.gravity = Gravity.END;
                    break;
            }

            chatCommunicationHistoryLinearLayout.addView(messageTextView, messageTextViewLayoutParams);

            Space space = new Space(getActivity());
            LinearLayout.LayoutParams spaceLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            chatCommunicationHistoryLinearLayout.addView(space, spaceLayoutParams);
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        int clientPosition = arguments.getInt(Constants.CLIENT_POSITION, -1);
        int clientType = arguments.getInt(Constants.CLIENT_TYPE, -1);

        ChatActivity chatServiceActivity = (ChatActivity) getActivity();
        NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = chatServiceActivity.getNetworkServiceDiscoveryOperations();

        switch (clientType) {
            case Constants.CONVERSATION_TO_SERVER:
                chatClient = networkServiceDiscoveryOperations.getCommunicationToServers().get(clientPosition);
                break;
            case Constants.CONVERSATION_FROM_CLIENT:
                chatClient = networkServiceDiscoveryOperations.getCommunicationFromClients().get(clientPosition);
                break;
        }

        chatCommunicationHistoryLinearLayout = getActivity().findViewById(R.id.chat_communication_history_linear_layout);
        messageEditText = getActivity().findViewById(R.id.message_edit_text);

        Button sendMessageButton = getActivity().findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(sendMessageButtonClickListener);

        if (chatClient != null) {
            chatClient.setContext(chatServiceActivity);
            List<Message> conversationHistory = chatClient.getConversationHistory();
            for (Message conversation : conversationHistory) {
                appendMessage(conversation);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}