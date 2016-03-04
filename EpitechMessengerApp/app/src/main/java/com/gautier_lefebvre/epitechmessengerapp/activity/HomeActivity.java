package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.Contact;
import com.gautier_lefebvre.epitechmessengerapp.entity.Message;
import com.gautier_lefebvre.epitechmessengerapp.entity.MessageData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.AddContact;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.ContactList;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.DeleteContact;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.GetUserData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.RefreshMessages;
import com.gautier_lefebvre.epitechmessengerapp.entity.UserData;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.R;

public class HomeActivity extends MessageHandlingActivity {
    HomeActivity _context;

    Handler _handler;
    final Runnable _refreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            RefreshMessages.Request request = new RefreshMessages.Request();
            request.activity = _context;
            request.auth_token = ApplicationData.currentUser.authToken;
            request.userId = ApplicationData.currentUser.userId;
            new RefreshMessagesTask().execute(request);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            _context = this;

            this._handler = new Handler();
            _handler.post(_refreshMessagesRunnable);

            // get pending messages when receiving push notification
            this._gcmNotificationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                        _handler.post(_refreshMessagesRunnable);
                    }
                }
            };

            // set nickname text view contact
//            TextView nicknameTextView = (TextView) this.findViewById(R.id.nicknameTextView);
//            nicknameTextView.setText(ApplicationData.currentUser.nickname);

            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                bar.setTitle(ApplicationData.currentUser.nickname);
            }

            // load contacts
            ContactList.Request request = new ContactList.Request();
            request.activity = this;
            request.auth_token = ApplicationData.currentUser.authToken;
            request.userId = ApplicationData.currentUser.userId;
            new ContactListTask().execute(request);
        } catch (Exception e) {
            this.showDialog("error", "Unexpected error");
        }
    }

    /**
     * Refreshes the list of contacts
     */
    private void onContactListTaskSuccess(ContactList.Response response) {
        try {
            if (response.success) {
                for (UserData contactData : response.users) {
                    boolean found = false;
                    for (Contact c : ApplicationData.contactList) {
                        if (c.userId == contactData.userId) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        ApplicationData.contactList.add(new Contact(
                                contactData.userId,
                                contactData.nickname,
                                true));
                    }
                }

                this.updateContactList();
            } else {
                this.showDialog("error", response.reason);
            }
        } catch (Exception e) {
            this.showDialog("error", "unexpected error");
        }
    }

    public void updateContactList() {
        ListView contactListView = (ListView) this.findViewById(R.id.contactListView);

        if (contactListView.getAdapter() == null) {
            // create adapter
            ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(
                    this,
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    ApplicationData.contactList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView t1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView t2 = (TextView) view.findViewById(android.R.id.text2);

                    Contact c = ApplicationData.contactList.get(position);
                    int unreadMessages = c.getUnreadMessagesNb();

                    // set primary text (nickname + nb of unread messages)
                    if (unreadMessages > 0) {
                        t1.setText(c.nickname + " (" + unreadMessages + ")");
                        t1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    } else {
                        t1.setText(c.nickname);
                        t1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    }

                    // set secondary text (hold to add or hold to remove)
                    if (!c.added) {
                        t2.setText("Touch to talk. Hold to add contact");
                    } else {
                        t2.setText("Touch to talk. Hold to remove contact");
                    }
                    return view;
                }
            };

            // set adapter to list view
            contactListView.setAdapter(adapter);

            // go to conversation on simple click
            // add onClick Event listener for items
            contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Contact contact = ApplicationData.contactList.get(position);

                    // go to contact conversation activity
                    Intent i = new Intent(_context, ConversationActivity.class);
                    i.putExtra("contactId", contact.userId);
                    _context.finish();
                    _context.startActivity(i);
                }
            });

            // ask for delete on long click
            contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    Contact c = ApplicationData.contactList.get(position);
                    if (c.added) {
                        AlertDialog dialog = new AlertDialog.Builder(_context)
                                .setTitle("Delete contact")
                                .setCancelable(true)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _context.onDeleteContactAction(ApplicationData.contactList.get(position));
                                    }
                                })
                                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setMessage("Do you wish to remove this contact ?")
                                .create();
                        dialog.show();

                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(_context)
                                .setTitle("Add contact")
                                .setCancelable(true)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _context.onAddContactAction(ApplicationData.contactList.get(position));
                                    }
                                })
                                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setMessage("Do you wish to add this contact ?")
                                .create();
                        dialog.show();
                    }
                    return true;
                }
            });
        } else {
            ((ArrayAdapter<Contact>)contactListView.getAdapter()).notifyDataSetChanged();
        }
    }

    private void onDeleteContactAction(Contact contact) {
        DeleteContact.Request request = new DeleteContact.Request();
        request.auth_token = ApplicationData.currentUser.authToken;
        request.userId = ApplicationData.currentUser.userId;
        request.contactId = contact.userId;
        request.activity = this;
        new DeleteContactTask().execute(request);
    }

    private void onDeleteContactTaskSuccess(DeleteContact.Response response) {
        if (response.success) {
            for (Contact c : ApplicationData.contactList) {
                if (c.userId == response.userId) {
                    ApplicationData.contactList.remove(c);
                    this.updateContactList();
                    break;
                }
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }

    private void onAddContactAction(Contact contact) {
        AddContact.Request request = new AddContact.Request();
        request.auth_token = ApplicationData.currentUser.authToken;
        request.userId = ApplicationData.currentUser.userId;
        request.contactId = contact.userId;
        request.activity = this;
        new AddContactTask().execute(request);
    }

    private void onAddContactTaskSuccess(AddContact.Response response) {
        if (response.success) {
            Contact c = ApplicationData.getContactById(response.userId);
            if (c != null) {
                c.added = true;
                this.updateContactList();
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }

    public void findContactsButton_onClick(View v) {
        // go to search contacts activity
        Intent i = new Intent(this, SearchContactsActivity.class);
        this.finish();
        this.startActivity(i);
    }

    public void nicknameTextView_onClick(View v) {
        // go to edit profile activity
    }

    public static class DeleteContactTask extends AsyncTask<DeleteContact.Request, Void, Void> {
        DeleteContact.Response response = null;
        DeleteContact.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(DeleteContact.Request... params) {
            this.request = params[0];
            try {
                DeleteContact.Response response = new DeleteContact.Response();
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
                this.request.activity.onDeleteContactTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }

    public static class ContactListTask extends AsyncTask<ContactList.Request, Void, Void> {
        ContactList.Response response = null;
        ContactList.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(ContactList.Request... params) {
            this.request = params[0];
            try {
                ContactList.Response response = new ContactList.Response();
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
                this.request.activity.onContactListTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }


    public static class AddContactTask extends AsyncTask<AddContact.Request, Void, Void> {
        AddContact.Request request = null;
        AddContact.Response response = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(AddContact.Request... params) {
            this.request = params[0];
            try {
                AddContact.Response response = new AddContact.Response();
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
                ((HomeActivity)this.request.activity).onAddContactTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
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
                ((HomeActivity)this.request.activity).onRefreshMessagesTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
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

                Message message = new Message(c, msgData.content, msgData.date, false);
                c.conversationHistory.add(message);
                this.updateContactList();
            }
        } else {
            this.showDialog("error", response.reason);
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
                ((HomeActivity)this.request.activity).onGetUserDataTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
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
            this.updateContactList();
        } else {
            this.showDialog("error", response.reason);
        }
    }

}
