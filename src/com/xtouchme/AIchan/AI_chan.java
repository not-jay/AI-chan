package com.xtouchme.AIchan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xtouchme.AIchan.sms.SMSResponder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class AI_chan extends Activity {

	//TODO set-up options file
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ai_chan);
		
		list = (ListView) findViewById(R.id.list);
		
		String[][] data = new String[][] {
				{"Cancel SMS Alarms", "\"I'll cancel all running sms countdowns~\""},
				{"SMS Responder Settings", "\"I'll help you reply to those pesky sms messages\""},
				{"Call Responder Settings", "\"I'll help you on calls you can't pick up\""} //TODO for 0.01f
				//If it is possible, have android be an answering machine and record the call
				//If not, then just have the AI send the 'busy' message and decline the call
		};
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		for(String[] s : data) {
			Map<String, String> map = new HashMap<String, String>(2);
			map.put("item", s[0]);
			map.put("desc", s[1]);
			items.add(map);
		}
		
		list.setAdapter(new SimpleAdapter(this, items,
										  R.layout.listview_layout, //android.R.layout.simple_expandable_list_item_2,
										  new String[] {"item", "desc"},
										  new int[] {R.id.item1, R.id.item2}));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Class<?> selected = null;
				Log.v("Clicked %d - pos %d", view.getId(), position);
				
				switch(position) {
				case 0:
					Toast.makeText(AI_chan.this, "Cancelling all alarms", Toast.LENGTH_SHORT).show();
					Log.v("Cancelling all alarms via App");
					SMSResponder.cancelAllAlarms();
					Toast.makeText(AI_chan.this, "All alarms cancelled", Toast.LENGTH_SHORT).show();
					break;
				case 1:
					try {
						selected = Class.forName("com.xtouchme.AIchan.sms.SMSPreferences");
						startActivity(new Intent(AI_chan.this, selected));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					break;
				default:
					Toast.makeText(AI_chan.this, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
}
