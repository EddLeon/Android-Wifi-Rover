/* Based on the following OpenSource 
 * http://hoodaandroid.blogspot.ca/2012/10/vertical-seek-bar-or-slider-in-android.html
 * http://www.itsamples.com/tiny-telnet-android.html
 */

package com.eddl.rover;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	public static int REQUEST_OPEN = 0;
    public static int REQUEST_HISTORY = 1; 
    final Context context = this;
    private boolean mIsConnected = false;
    private String mStrHost = "";
    private String mStrPort = "";
    boolean tgl=true;
    
    Handler mHandler = null;
    Socket mSocket = null;
    Thread mThread = null;
    TextView mTextViewContent;
    ScrollView mScrollViewContent;
    int vel1, vel2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	final TextView sliderText1 = (TextView)findViewById(R.id.verticalSeekbarText1);
	final TextView sliderText2 = (TextView)findViewById(R.id.verticalSeekbarText2);
	sliderText1.setTextSize(48);
	sliderText2.setTextSize(48);
	VerticalSeekBar verticalSeebar1 = (VerticalSeekBar)findViewById(R.id.verticalSeekbar1);
	VerticalSeekBar verticalSeebar2 = (VerticalSeekBar)findViewById(R.id.verticalSeekbar2);
	this.mHandler = new Handler();
	
	this.mTextViewContent = (TextView)findViewById(R.id.textViewContent);
	this.mScrollViewContent = (ScrollView)findViewById(R.id.scrollViewContent);
	this.mScrollViewContent.setSmoothScrollingEnabled(true);
	
	ConnectDialog();

		verticalSeebar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				vel1=2*(progress-255/2);
				sliderText1.setText(""+vel1);
					PostCommand();
			}
		}); 
		verticalSeebar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				vel2=2*(progress-255/2);
				sliderText2.setText(""+vel2);
					PostCommand();
			}
		});
	}
	
	
	private void ConnectDialog(){
    	LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.connect, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(promptsView);

		final EditText edIP = (EditText) promptsView.findViewById(R.id.editTextIP);
		final EditText edPort = (EditText) promptsView.findViewById(R.id.editTextPort);

	    SharedPreferences settings = getSharedPreferences("TinyTelnet", 0);
	    String strOldHost = settings.getString("server-ip", "");
	    String strOldPort = settings.getString("server-port", "23");
	    
	    edIP.setText(strOldHost);
	    edPort.setText(strOldPort);

		// set dialog message
		alertDialogBuilder.setCancelable(true);
		
		alertDialogBuilder.setPositiveButton("Connect",
			  new DialogInterface.OnClickListener(){
			  public void onClick(DialogInterface dialog,int id){
				   // get user input and set it to result
                   // edit text
				   String strHost = edIP.getText().toString();
				   String strPort = edPort.getText().toString();
				   
				   if(!strHost.equals("") && !strPort.equals("")){
					    SharedPreferences settings = getSharedPreferences("TinyTelnet", 0);
		    			SharedPreferences.Editor editor = settings.edit();
		    			editor.putString("server-ip", strHost);
		    			editor.putString("server-port", strPort);
		    			editor.commit();
                        
		    			//InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
		    			mStrHost = strHost;
		    			mStrPort = strPort;
		    			
		    			if(mThread != null){
		    				mThread.stop();
		    				mThread = null;
		    			}

		    			mThread = new Thread(new ClientThread());
		    			mThread.start();
				   }
			  }
        });
        
        alertDialogBuilder.setNegativeButton("Cancel", 
        		new DialogInterface.OnClickListener(){
			    public void onClick(DialogInterface dialog,int id){
			    	dialog.cancel();
			    }
        });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
    }
	
	public class ClientThread implements Runnable{
    	public void run(){
	    	int nPort = 23;
	    	try{ 
	    	    nPort = Integer.parseInt(mStrPort); 
	    	}
	    	catch(NumberFormatException nfe){
	    		nPort = 23;
	    	}
	    	
    		try{
    			Socket socket = new Socket(mStrHost, nPort);
		    	mSocket = socket;
	        
		    	InputStream streamInput = mSocket.getInputStream();
		    	mIsConnected = true;
		    	
		    	byte[] arrayOfByte = new byte[10000];
		    	while (mIsConnected){
		    		int j = 0;
		    		try{
		    			int i = arrayOfByte.length;
		    			j = streamInput.read(arrayOfByte, 0, i);
		    			if (j == -1){
		    				throw new Exception("Error while reading socket.");
		    			}
		    		}
		    		catch (Exception e0){
		    			Handler handlerException = MainActivity.this.mHandler;
		    			String strException = e0.getMessage();
    					final String strMessage = "Error while receiving from server:\r\n" + strException;
		    			Runnable rExceptionThread = new Runnable(){
		    				public void run(){
		    					Toast.makeText(context, strMessage, 3000).show();
		    				}
		    			};

		    			handlerException.post(rExceptionThread);
		    			
		    			if(strException.indexOf("reset") != -1 || strException.indexOf("rejected") != -1)
		    			{
		    				mIsConnected = false;
							try{	
								mSocket.close();	
							}
							catch (IOException e1){
								e1.printStackTrace();
							}
		    				mSocket = null;
			    			break;
		    			}
		    		}

		    		if (j == 0)
		    			continue;
		    		
		    		final String strData = new String(arrayOfByte, 0, j).replace("\r", "");
		    		Handler localHandler2 = MainActivity.this.mHandler;
		    		
		    		Runnable local2 = new Runnable(){
		    			public void run(){
		    				StringBuilder localStringBuilder1 = new StringBuilder();
		    				CharSequence localCharSequence = MainActivity.this.mTextViewContent.getText();
		    				localStringBuilder1.append(localCharSequence);
		    				localStringBuilder1.append(strData);
		    				MainActivity.this.mTextViewContent.setText(localStringBuilder1.toString());
		    				MainActivity.this.mScrollViewContent.requestLayout();
		    				
		    				Handler localHandler = MainActivity.this.mHandler;
		    				Runnable local1 = new Runnable(){
		    					public void run(){
		    						ScrollView localScrollView = MainActivity.this.mScrollViewContent;
		    						int i = MainActivity.this.mTextViewContent.getHeight();
		    						localScrollView.smoothScrollTo(0, i);
		    					}
		    				};
		    				localHandler.post(local1);
		    			}
		    		};
		    		
		    		localHandler2.post(local2);
		    	}
	
    			socket.close();
    			//mSocket = null;
   			}
    		catch (Exception e0){
    			mIsConnected = false;

    			Handler handlerException = MainActivity.this.mHandler;
    			String strException = e0.getMessage();
    			if(strException == null)
    				strException = "Connection closed";
    			else
    				strException = "Cannot connect to the Rover:\r\n" + strException;
    			
				final String strMessage = strException;
    			Runnable rExceptionThread = new Runnable(){
    				public void run(){
    					Toast.makeText(context, strMessage, 2000).show();
    				}
    			};
    			handlerException.post(rExceptionThread);
            }
    	}
    }
	
	public String getCommand(){
		String command="*";
		
		if(vel2>0){
			if(vel1>0){
				command+="w/"+vel1+"/"+vel2;
			}
			else{
				command+="a/"+(-1)*vel1+"/"+vel2;
			}
		}
		else{
			if(vel1>0){
				command+="d/"+vel1+"/"+(-1)*vel2;
			}
			else{
				command+="s/"+(-1)*vel1+"/"+(-1)*vel2;
			}
		}
		return command;
	}
	
	private void PostCommand(){
    	if(mSocket != null && mIsConnected){
	        String strCommand = getCommand();

            try{
				OutputStream streamOutput = mSocket.getOutputStream();
				
				strCommand += "\n";
		        try{
		           	byte[] arrayOutput = strCommand.getBytes("ASCII");
		            int nLen = arrayOutput.length;
		            streamOutput.write(arrayOutput, 0, nLen);
		        }
		        catch (Exception e0){
	    			Handler handlerException = MainActivity.this.mHandler;
					final String strMessage = "Error while sending:\r\n" + e0.getMessage();
	    			Runnable rExceptionThread = new Runnable()
	    			{
	    				public void run()
	    				{
	    					Toast.makeText(context, strMessage, 2000).show();
	    				}
	    			};
	    			handlerException.post(rExceptionThread);
		        }
			}
            catch (IOException e1) 
            {
				e1.printStackTrace();
			}
    	}
    	else
    	{
    		Toast.makeText(context, "Please connect first", 2000).show();
    	}
    }
	
	protected void onDestroy(){
    	super.onDestroy();

    	if(mIsConnected){
    		mIsConnected = false;
    		try{
	    		if(this.mThread != null){
	    			Thread threadHelper = this.mThread; 
	    			this.mThread = null;
	    			threadHelper.interrupt();
	    		}
	    	}
	    	catch (Exception e1){}
    	}
    }
}
