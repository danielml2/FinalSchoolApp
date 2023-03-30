package me.danielml.finalschoolapp.activities;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.activities.login.UserLoginActivity;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class MenuHandler {

    public static void handleItemSelected(Context context, MenuItem item) {
        if(item.getItemId() == R.id.signOutMenu) {
            new FirebaseManager().signOut();
            context.startActivity(new Intent(context, UserLoginActivity.class));
        } else if(item.getItemId() == R.id.settingsMenu) {
            context.startActivity(new Intent(context, SettingsActivity.class));
        } else if(item.getItemId() == R.id.changePfpMenu) {
            context.startActivity(new Intent(context, ProfilePictureActivity.class));
        }
    }
}
