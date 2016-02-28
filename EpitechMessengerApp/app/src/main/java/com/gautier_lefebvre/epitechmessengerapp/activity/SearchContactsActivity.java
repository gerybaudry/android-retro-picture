package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.Contact;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.AddContact;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SearchContacts;
import com.gautier_lefebvre.epitechmessengerapp.entity.UserData;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.R;

import java.util.ArrayList;

public class SearchContactsActivity extends AppActivity {
    SearchContactsActivity activity = null;
    ArrayList<UserData> searchResult = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contacts);

        activity = this;

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
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

    public void searchButton_onClick(View v) {
        EditText searchField = (EditText)this.findViewById(R.id.searchTextField);

        String searchPattern = searchField.getText().toString().trim();

        if (!searchPattern.isEmpty()) {
            SearchContacts.Request request = new SearchContacts.Request();
            request.auth_token = ApplicationData.currentUser.authToken;
            request.userId = ApplicationData.currentUser.userId;
            request.searchPattern = searchPattern;
            request.activity = this;

            new SearchContactsTask().execute(request);
        }
    }

    private void onSearchContactsTaskSuccess(final SearchContacts.Response response) {
        if (response.success) {
            ListView listView = (ListView)this.findViewById(R.id.resultListView);

            this.searchResult.clear();
            for (UserData u : response.users) {
                if (u.userId != ApplicationData.currentUser.userId) {
                    this.searchResult.add(u);
                }
            }

            if (listView.getAdapter() == null) {
                ArrayAdapter<UserData> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        this.searchResult);


                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog dialog = new AlertDialog.Builder(activity)
                                .setTitle("Add contact")
                                .setCancelable(true)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.onAddContact(searchResult.get(position));
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
                });
            } else {
                ((ArrayAdapter<UserData>)listView.getAdapter()).notifyDataSetChanged();
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }

    public void onAddContact(UserData contact) {
        // send HTTP request to add contact
        AddContact.Request request = new AddContact.Request();
        request.auth_token = ApplicationData.currentUser.authToken;
        request.userId = ApplicationData.currentUser.userId;
        request.contactId = contact.userId;
        request.activity = this;
        new AddContactTask().execute(request);
    }

    public void onAddContactTaskSuccess(AddContact.Response response) {
        if (response.success) {
            // add contact
            Contact contact = ApplicationData.getContactById(response.userId);

            if (contact == null) {
                ApplicationData.contactList.add(new Contact(response.userId, response.nickname, true));
            } else {
                contact.added = true;
            }

            // remove contact from results
            for (UserData u : this.searchResult) {
                if (u.userId == response.userId) {
                    this.searchResult.remove(u);
                    ListView listView = (ListView)this.findViewById(R.id.resultListView);
                    ((ArrayAdapter<UserData>)listView.getAdapter()).notifyDataSetChanged();
                    break;
                }
            }
        } else {
            this.showDialog("error", response.reason);
        }
    }


    public static class SearchContactsTask extends AsyncTask<SearchContacts.Request, Void, Void> {
        SearchContacts.Request request = null;
        SearchContacts.Response response = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(SearchContacts.Request... params) {
            this.request = params[0];
            try {
                SearchContacts.Response response = new SearchContacts.Response();
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
                this.request.activity.onSearchContactsTaskSuccess(this.response);
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
                ((SearchContactsActivity)this.request.activity).onAddContactTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }


}
