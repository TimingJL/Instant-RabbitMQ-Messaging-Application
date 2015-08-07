package com.karthi.rabbitmq;

import java.io.UnsupportedEncodingException;


import com.karthi.rabbitmq.R;
import com.karthi.rabbitmq.MessageConsumer.OnReceiveMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private MessageConsumer mConsumer;
	private TextView mOutput;
	private String QUEUE_NAME = "";
	private String EXCHANGE_NAME = "logs";
	private String message = "";
	private String name = "理專(Note2)";//手機主人名稱
	/*客戶機從 test -> myQueue*/

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(MainActivity.this, "RabbitMQ Chat Service!",
				Toast.LENGTH_LONG).show();

		final EditText etv1 = (EditText) findViewById(R.id.out3);
		etv1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					name = etv1.getText().toString();
					etv1.setText("");
					etv1.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});

		final EditText etv = (EditText) findViewById(R.id.out2);
		etv.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// If the event is a key-down event on the "enter" button
				if ((arg2.getAction() == KeyEvent.ACTION_DOWN)
						&& (arg1 == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					message = name + ": " + etv.getText().toString();
					new send().execute(message);
					etv.setText("");
					return true;
				}
				return false;
			}
		});

		// The output TextView we'll use to display messages
		mOutput = (TextView) findViewById(R.id.output);

		// Create the consumer
		mConsumer = new MessageConsumer("xxx.xxx.xxx.xxx", EXCHANGE_NAME, "fanout");
		new consumerconnect().execute();
		// register for messages
		mConsumer.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {

			public void onReceiveMessage(byte[] message) {
				String text = "";
				try {
					text = new String(message, "UTF8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				mOutput.append("\n" + text);
			}
		});
		
	}

	private class send extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... Message) {
			try {

				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost("xxx.xxx.xxx.xxx");

				// my internet connection is a bit restrictive so I have use an
				// external server
				// which has RabbitMQ installed on it. So I use "setUsername"
				// and "setPassword"
				factory.setUsername("username");
				factory.setPassword("password");
				factory.setVirtualHost("virtualhostname");
				factory.setPort(5672);
				System.out.println(""+factory.getHost()+factory.getPort()+factory.getRequestedHeartbeat()+factory.getUsername());
				Connection connection = factory.newConnection();
				Channel channel = connection.createChannel();
				
				channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
				String tempstr = "";
				for (int i = 0; i < Message.length; i++)
					tempstr += Message[i];

				channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null,
						tempstr.getBytes());//for 理專廣播

				channel.close();

				connection.close();

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			return null;
		}

	}

	
	private class consumerconnect extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... Message) {
			try {
				// Connect to broker
				mConsumer.connectToRabbitMQ();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			return null;
		}

	}
	
	@Override
	protected void onResume() {
		super.onPause();
		new consumerconnect().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mConsumer.dispose();
	}
	
	/** Called when the user clicks the Send button */
	public void clean(View view) {
		mOutput.setText("");
	}
}