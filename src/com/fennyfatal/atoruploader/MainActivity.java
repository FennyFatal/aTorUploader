package com.fennyfatal.atoruploader;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//Not Much here... Just an angry baby and a settings menu.
//TODO: Add a browse for torrent file dialog maybe?
public class MainActivity extends FragmentActivity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button selectTorrent = (Button) findViewById(R.id.button1);
        selectTorrent.setOnClickListener(this);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        //If we got passed an intent, let's skip over the main window and load up our directory picker.
        if (uri != null)
        {
	        String scheme = uri.getScheme();
	        if ( scheme.equalsIgnoreCase("file") || scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))
	        {
	    		Intent dirpicker = new Intent(this,DirectoryPicker.class);
	    		dirpicker.setData(uri);
	    		startActivityForResult(dirpicker, DirectoryPicker.PICK_DIRECTORY);
				return;
	        }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.action_settings:
    			//It's Settings Time!
    			startActivity(new Intent(this, SettingsActivity.class));
    		break;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    
    
	@SuppressLint("SdCardPath")
	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		//TODO Maybe add some buttons first, eh?
		default:
		}
	};

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null)
		{
			if (data.getExtras().containsKey(DirectoryPicker.CHOSEN_DIRECTORY))
			{
				new TorrentUploader(MainActivity.this).execute(new TorrentUploadARGS(data.getStringExtra(DirectoryPicker.CHOSEN_DIRECTORY), data.getData().toString()));
			}
		}
		finish();
	}

}
