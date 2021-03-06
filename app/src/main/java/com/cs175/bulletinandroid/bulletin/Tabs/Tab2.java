package com.cs175.bulletinandroid.bulletin.Tabs;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cs175.bulletinandroid.bulletin.BulletinSingleton;
import com.cs175.bulletinandroid.bulletin.ConversationResponse;
import com.cs175.bulletinandroid.bulletin.ItemResponse;
import com.cs175.bulletinandroid.bulletin.OnRequestListener;
import com.cs175.bulletinandroid.bulletin.R;
import com.cs175.bulletinandroid.bulletin.Response;

/**
 * Created by chenyulong on 12/4/16.
 */
public class Tab2 extends Fragment implements OnRequestListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private boolean processingMessages;
    private ListView contentListView;

    private Typeface font;

    private TextView mainHeaderTextView;
    private TextView subHeaderTextView;

    private BulletinSingleton singleton = BulletinSingleton.getInstance();

    public void changeFont(TextView text){
        text.setTypeface(font);
    }

    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.tab2, container, false);

        font = Typeface.createFromAsset(getActivity().getAssets(), "Fonts/SF-UI-Display-Light.otf");

        mainHeaderTextView = (TextView) view.findViewById(R.id.mainHeaderTextView);
        subHeaderTextView = (TextView) view.findViewById(R.id.subHeaderTextView);

        changeFont(mainHeaderTextView);
        changeFont(subHeaderTextView);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.scrollControl);

        swipeRefresh.setOnRefreshListener(this);

        processingMessages = false;

        contentListView = (ListView) view.findViewById(R.id.contentListView);
        contentListView.setOnItemClickListener(this);

        refreshMessages();
        return view;
    }



    public void refreshMessages(){
        if (processingMessages == false){
            processingMessages = true;
            singleton.getAPI().getConverations(this);
        }
    }

    @Override
    public void onResponseReceived(RequestType type, Response response) {
        processingMessages = false;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResponsesReceived(RequestType type, int resCode, Response[] response) {
        if(type == RequestType.GetConversations) {
            if (resCode == 200) {
                final MessageItemAdapter adapter = new MessageItemAdapter(getContext(), (ConversationResponse[]) response);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        contentListView.setAdapter(adapter);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                swipeRefresh.setRefreshing(false);
            }
        });
        processingMessages = false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent conversationIntent = new Intent(getActivity(), ConversationActivity.class);
        MessageItemAdapter adapter = (MessageItemAdapter) adapterView.getAdapter();
        ConversationResponse conversation = (ConversationResponse) adapter.getItem(i);

        String userId = singleton.getUserResponse().get_id();
        if(userId.equals(conversation.getUserWith())){
            conversationIntent.putExtra("userName", conversation.getUserStartName());
        }else{
            conversationIntent.putExtra("userName", conversation.getUserWithName());
        }

        conversationIntent.putExtra("conversationId", conversation.get_id());


        startActivity(conversationIntent);
    }

    @Override
    public void onRefresh() {
        if (processingMessages == true) swipeRefresh.setRefreshing(false);
        refreshMessages();
    }
}
