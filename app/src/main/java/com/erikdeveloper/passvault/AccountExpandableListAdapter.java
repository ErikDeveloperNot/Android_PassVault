package com.erikdeveloper.passvault;

import android.content.Context;
import android.graphics.Color;
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
    private final int FIELD_COUNT = 6;
    private final int FIELD_COUNT_INACTIVE = 3;
    public static final int NAME = 0;
    public static final int PASS = 1;
    public static final int OLD_PASS = 2;
    public static final int LAUNCH_URL = 3;
    public static final int EDIT = 4;
    public static final int DELETE = 5;
    public static final int INACTIVE_MSG = 0;
    public static final int INACTIVE_EDIT = 1;
    public static final int INACTIVE_DELETE = 2;


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

        if (accounts.get(groupPosition).isValidEncryption()) {
            return FIELD_COUNT;
        } else {
            return FIELD_COUNT_INACTIVE;
        }
    }


    @Override
    public Object getGroup(int groupPosition) {
        return accounts.get(groupPosition).getName();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        if (accounts.get(groupPosition).isValidEncryption()) {
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
                case LAUNCH_URL:
                    return "Copy Password and Launch Browser";
                default:
                    return "";
            }
        } else {
            switch (childPosition) {
                case INACTIVE_MSG:
                    return R.string.expandable_list_inactive_message;
                case INACTIVE_EDIT:
                    return R.string.expandable_List_recover_account;
                case INACTIVE_DELETE:
                    return R.string.expandable_list_delete;
                default:
                    return "";
            }
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

        if (account.isValidEncryption()) {
            accountNameView.setText(account.getName());
            accountNameView.setTextColor(Color.BLACK);
        } else {
            accountNameView.setText(account.getName() + "**");
            accountNameView.setTextColor(Color.LTGRAY);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(com.erikdeveloper.passvault.R.layout.account_list_view_item, parent, false);
        }

        TextView accountItemView = (TextView) convertView.findViewById(com.erikdeveloper.passvault.R.id.account_list_view_item);
        accountItemView.setTextColor(Color.GRAY);
        accountItemView.setTypeface(null, Typeface.NORMAL);

        if (accounts.get(groupPosition).isValidEncryption()) {
            switch (childPosition) {
                case NAME:
                    accountItemView.setText("Username: " + accounts.get(groupPosition).getUser());
                    accountItemView.setTextColor(Color.BLACK);
                    //accountItemView.setTypeface(null, Typeface.BOLD);
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
                case LAUNCH_URL:
                    accountItemView.setText("Copy Password and Launch Browser");

                    if (accounts.get(groupPosition).getUrl().equalsIgnoreCase("http://")) {
                        accountItemView.setTextColor(Color.LTGRAY);
                        accountItemView.setTypeface(null, Typeface.ITALIC);
                    }

                    break;
                default:
                    accountItemView.setText("");
            }
        } else {
            switch (childPosition) {
                case INACTIVE_MSG:
                    accountItemView.setText(R.string.expandable_list_inactive_message);
                    accountItemView.setTextColor(Color.BLACK);
                    accountItemView.setTypeface(null, Typeface.ITALIC);
                    break;
                case INACTIVE_EDIT:
                    accountItemView.setText(R.string.expandable_List_recover_account);
                    accountItemView.setTextColor(Color.DKGRAY);
                    break;
                case INACTIVE_DELETE:
                    accountItemView.setText(com.erikdeveloper.passvault.R.string.expandable_list_delete);
                    accountItemView.setTextColor(Color.DKGRAY);
                    break;
                default:
                    accountItemView.setText("");
            }
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
