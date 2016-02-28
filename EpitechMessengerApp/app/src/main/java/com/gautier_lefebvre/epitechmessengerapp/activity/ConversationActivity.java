package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.Contact;
import com.gautier_lefebvre.epitechmessengerapp.entity.Message;
import com.gautier_lefebvre.epitechmessengerapp.entity.MessageData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.GetUserData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.RefreshMessages;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SendMessage;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.helper.DPIHelper;
import com.gautier_lefebvre.epitechmessengerapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ConversationActivity extends MessageHandlingActivity {
    int _contactId;
    ConversationActivity _context;

    public Handler _handler;
    public final Runnable _refreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
        RefreshMessages.Request request = new RefreshMessages.Request();
        request.activity = _context;
        request.auth_token = ApplicationData.currentUser.authToken;
        request.userId = ApplicationData.currentUser.userId;
        new RefreshMessagesTask().execute(request);
        }
    };

    public void HomeButton_onClick(View v) {
        this.finish();
        this.startActivity(this.getHomeIntent());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        _context = this;
        this._handler = new Handler();

        Intent i = this.getIntent();
        this._contactId = i.getIntExtra("contactId", -1);
        Contact contact = ApplicationData.getContactById(this._contactId);

        if (contact == null) {
            this.finish();
            this.startActivity(this.getHomeIntent());
        } else {
            _handler.post(_refreshMessagesRunnable);

            this._gcmNotificationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                        _handler.post(_refreshMessagesRunnable);
                    }
                }
            };

            if (bar != null) {
                bar.setTitle(contact.nickname);
            }

            // set contact conversation history
            for (Message msg : contact.conversationHistory) {
                this.addMessageToScrollView(msg);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
                this.startActivity(this.getHomeIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addMessageToScrollView(Message message) {
        LinearLayout linearLayout = (LinearLayout)this.findViewById(R.id.conversationHistoryLayout);

        RelativeLayout rl = new RelativeLayout(this);

        // set rl parameters
        // add margin to relative layout
        // set match_parent/wrap_content
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                                            new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT));
        rlp.setMargins(0, 10, 0, 0);
        rl.setLayoutParams(rlp);

        TextView contentTextView = new TextView(this);
        TextView msgDate = new TextView(this);

        // set ids
        contentTextView.setId(View.generateViewId());
        msgDate.setId(View.generateViewId());

        // set text
        contentTextView.setText(message.content);
        msgDate.setText(message.date != null && !message.date.isEmpty() ?
                message.date :
                "--:--");

        int paddingPx = DPIHelper.dp2px(this, 5);

        // set padding
        contentTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        msgDate.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        // create general layout params
        RelativeLayout.LayoutParams textLP = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (message.sender == ApplicationData.currentUser) {
            // set date params
            msgDate.setLayoutParams(new RelativeLayout.LayoutParams(textLP));
            rl.addView(msgDate);

            // create text params
            RelativeLayout.LayoutParams textRealLP = new RelativeLayout.LayoutParams(textLP);
            textRealLP.addRule(RelativeLayout.ALIGN_PARENT_END);
            textRealLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textRealLP.addRule(RelativeLayout.END_OF, msgDate.getId());

            // set colors
            contentTextView.setTextColor(Color.parseColor("#000000"));
            contentTextView.setBackgroundColor(Color.parseColor("#75ff88"));

            // set params
            contentTextView.setLayoutParams(textRealLP);
            rl.addView(contentTextView);
        } else {
            // create date params
            RelativeLayout.LayoutParams dateRealLP = new RelativeLayout.LayoutParams(textLP);
            dateRealLP.addRule(RelativeLayout.ALIGN_PARENT_END);
            dateRealLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            // set date params
            msgDate.setLayoutParams(dateRealLP);

            rl.addView(msgDate);

            // create text params
            RelativeLayout.LayoutParams textRealLP = new RelativeLayout.LayoutParams(textLP);
            textRealLP.addRule(RelativeLayout.ALIGN_PARENT_START);
            textRealLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textRealLP.addRule(RelativeLayout.START_OF, msgDate.getId());

            // set colors
            contentTextView.setTextColor(Color.parseColor("#ffffff"));
            contentTextView.setBackgroundColor(Color.parseColor("#76baff"));

            // set params
            contentTextView.setLayoutParams(textRealLP);

            rl.addView(contentTextView);
        }

        linearLayout.addView(rl);

        final ScrollView scrollView = (ScrollView)this.findViewById(R.id.conversationHistory);
        if (scrollView != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    public void ButtonSend_onClick(View v) {
        EditText contentField = (EditText)this.findViewById(R.id.sendText);
        String content = contentField.getText().toString().trim();

        if (!content.isEmpty()) {
            SendMessage.Request req = new SendMessage.Request();
            req.activity = this;
            req.userId = ApplicationData.currentUser.userId;
            req.auth_token = ApplicationData.currentUser.authToken;
            req.contactId = this._contactId;
            req.content = content;
            new SendMessageTask().execute(req);

            contentField.setText("");
        }
    }

    private void onRefreshMessagesTaskSuccess(RefreshMessages.Response response) {
        if (response.success) {
            // take all new messages
            for (MessageData msgData : response.messages) {
                Contact c = ApplicationData.getContactById(msgData.contactId);
                if (c == null) {
                    // if the contact is unknown
                    // add contact
                    c = new Contact(msgData.contactId, "loading...", false);
                    ApplicationData.contactList.add(c);

                    // ask contact data (nickname)
                    GetUserData.Request request = new GetUserData.Request();
                    request.activity = this;
                    request.userId = ApplicationData.currentUser.userId;
                    request.auth_token = ApplicationData.currentUser.authToken;
                    request.contactId = msgData.contactId;
                    new GetUserDataTask().execute(request);
                }

                Message message = new Message(c, msgData.content, msgData.date);
                c.conversationHistory.add(message);

                if (c.userId == this._contactId) {
                    this.addMessageToScrollView(message);
                }
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }

    private void onGetUserDataTaskSuccess(GetUserData.Response response) {
        if (response.success) {
            Contact c = ApplicationData.getContactById(response.userId);
            if (c == null) {
                c = new Contact(response.userId, response.nickname, false);
                ApplicationData.contactList.add(c);
            } else {
                c.nickname = response.nickname;
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }


    private void onSendMessageTaskSuccess(SendMessage.Response response, SendMessage.Request request) {
        if (response.success) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Message message = new Message(ApplicationData.currentUser, request.content, sdf.format(cal.getTime()));
            Contact contact = ApplicationData.getContactById(this._contactId);
            if (contact !=  null) {
                contact.conversationHistory.add(message);
            }
            this.addMessageToScrollView(message);
        } else {
            this.showDialog("error", response.reason);
        }
    }

    public static class RefreshMessagesTask extends AsyncTask<RefreshMessages.Request, Void, Void> {
        RefreshMessages.Response response = null;
        RefreshMessages.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(RefreshMessages.Request... params) {
            this.request = params[0];
            try {
                RefreshMessages.Response response = new RefreshMessages.Response();
                ProtocolService.getInstance().sendEncryptedRequest(this.request, response);
                this.response = response;
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                ((ConversationActivity)this.request.activity).onRefreshMessagesTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }

    public static class GetUserDataTask extends AsyncTask<GetUserData.Request, Void, Void> {
        GetUserData.Response response = null;
        GetUserData.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(GetUserData.Request... params) {
            this.request = params[0];
            try {
                GetUserData.Response response = new GetUserData.Response();
                ProtocolService.getInstance().sendEncryptedRequest(this.request, response);
                this.response = response;
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                ((ConversationActivity)this.request.activity).onGetUserDataTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }

    public static class SendMessageTask extends AsyncTask<SendMessage.Request, Void, Void> {
        SendMessage.Response response = null;
        SendMessage.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(SendMessage.Request... params) {
            this.request = params[0];
            try {
                SendMessage.Response response = new SendMessage.Response();
                ProtocolService.getInstance().sendEncryptedRequest(this.request, response);
                this.response = response;
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                this.request.activity.onSendMessageTaskSuccess(this.response, this.request);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }
}
