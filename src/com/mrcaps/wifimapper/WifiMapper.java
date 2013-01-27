package com.mrcaps.wifimapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class WifiMapper extends Activity {
    private WifiScanner scanner;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Tests.testScan(WifiMapper.this);
				Tests.testUpload(WifiMapper.this);
			}
		});
    }
}