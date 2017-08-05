package com.erikdeveloper.passvault;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.passvault.util.Account;

import java.util.ArrayList;

/**
 * Created by erik.manor on 5/13/17.
 */
        /*
         * <Each expanded List is made up in the following order>
         * username
         * get password
         * get prior password
         * edit account
         * delete account
         */

public class AccountExpandableListAdapter extends BaseExpandableListAdapter {

    private ArrayList<Account> accounts;
    private Context context;
    private LayoutInflater inflater;
    private final int FIELD_COUNT = 5;
    public static final int NAME = 0;
    public static final int PASS = 1;
    public static final int OLD_PASS = 2;
    public static final int EDIT = 3;
    public static final int DELETE = 4;


    public AccountExpandableListAdapter(Context context, ArrayList<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getGroupCount() {
        return accounts.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return FIELD_COUNT;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return accounts.get(groupPosition).getName();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        switch (childPosition) {
            case NAME:
                return accounts.get(groupPosition).getUser();
            case PASS:
                return com.erikdeveloper.passvault.R.string.expandable_list_pass;
            case OLD_PASS:
                return com.erikdeveloper.passvault.R.string.expandable_list_old_pass;
            case EDIT:
                return com.erikdeveloper.passvault.R.string.expandable_list_edit;
            case DELETE:
                return com.erikdeveloper.passvault.R.string.expandable_list_delete;
            default:
                return "";
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (long)groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(com.erikdeveloper.passvault.R.layout.account_list_view_group, parent, false);
        }

        TextView accountNameView = (TextView) convertView.findViewById(com.erikdeveloper.passvault.R.id.account_list_view_name);

        Account account = (Account) accounts.get(groupPosition);
        accountNameView.setTypeface(null, Typeface.BOLD);
        accountNameView.setText(account.getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(com.erikdeveloper.passvault.R.layout.account_list_view_item, parent, false);
        }

        TextView accountItemView = (TextView) convertView.findViewById(com.erikdeveloper.passvault.R.id.account_list_view_item);

        switch (childPosition) {
            case NAME:
                accountItemView.setText("Username: " + accounts.get(groupPosition).getUser());
                break;
            case PASS:
                accountItemView.setText(com.erikdeveloper.passvault.R.string.expandable_list_pass);
                break;
            case OLD_PASS:
                accountItemView.setText(com.erikdeveloper.passvault.R.string.expandable_list_old_pass);
                break;
            case EDIT:
                accountItemView.setText(com.erikdeveloper.passvault.R.string.expandable_list_edit);
                break;
            case DELETE:
                accountItemView.setText(com.erikdeveloper.passvault.R.string.expandable_list_delete);
                break;
            default:
                accountItemView.setText("");
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        if (childPosition == 0)
            return false;
        else
            return true;
    }
}
