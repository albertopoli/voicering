/*------------------- COPYRIGHT AND TRADEMARK INFORMATION ------------------+
 |                                                                          |
 |    VoCon (R) 3200 Embedded Development System                            |
 |                                                                          |
 |    (c) 2012 Nuance Communications, Inc. All rights reserved.             |
 |                                                                          |
 |    Nuance, the Nuance logo and VoCon are trademarks and/or registered    |
 |    trademarks of Nuance Communications, Inc. or its affiliates in the    |
 |    United States and/or other countries.                                 |
 |                                                                          |
 +--------------------------------------------------------------------------*/

package com.nuance.mainactivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nuance.singlethreadrec_basic.R;


public class MainActivity extends Activity
{
    public static native void arg(String s);
    public static native int run(String logfilename);
    public static native void btnPressed();
    public static ConditionVariable cvBlockPlayback = new ConditionVariable();

    String defaultLogFilename = "";
  final static String VERSION = "VoCon4.12";
  final static String TAG = "NuanceMainActivity";

  final android.os.Handler handler = new android.os.Handler();

  android.widget.TextView te;
  android.widget.Button restartBtn;
  android.widget.Button PTTBtn;

  static MainActivity theInstance = null;
  
  // method getAppLibs() and getAppAssets() need to be overriden
  public String[] getAppLibs() { return null; }
  public String[] getAppAssets() { return null; }
  
    private void init()
    {
	final String[] libsToLoad = getAppLibs();
	if (null == libsToLoad)
	{
		Log.e(TAG,"getAppLibs() returned null - need to override method. exit application");
		System.exit(1);
	}

  	for (final String lib : libsToLoad)
      {
	        Log.i(TAG, "Loading lib: "+lib);
	        System.loadLibrary(lib);
      }
      Log.i(TAG, "ALL LIBS LOADED!");
  }

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        defaultLogFilename = getCacheDir()+"/MainActivity.log";
        te = (TextView)findViewById(R.id.te);
        te.setMovementMethod(new ScrollingMovementMethod());
        theInstance = this;
        
        restartBtn = (android.widget.Button) findViewById(R.id.restartBtn);
        PTTBtn = (android.widget.Button) findViewById(R.id.PTTBtn);




        new Thread() { public void run() { MainActivity.this.run(); } }.start();
    }    

    public void run() {

        log(VERSION);

        //// Copying assets if present
        {
          final android.content.res.Resources resources = getResources();
          final android.content.res.AssetManager assets = resources.getAssets();
          int n = 0;

	final String[] assetsToLoad = getAppAssets();
	if (null == assetsToLoad)
	{
		Log.e(TAG,"getAppAssets() returned null - need to override method. exit application");
		System.exit(1);
	}

          for (final String s : assetsToLoad)
          {
            try 
            {


              final java.io.File f = new java.io.File(getCacheDir()+"/"+s);

              final java.io.InputStream fi = assets.open(s);
              final java.io.File dir = f.getParentFile();
              Log.i("AT", "D: "+dir);
              Log.i("AT", "D: "+dir.exists());
              Log.i("AT", "D: "+dir.mkdirs());

              final java.io.BufferedOutputStream fo = new java.io.BufferedOutputStream(
                new java.io.FileOutputStream(f));
              final byte[] buf = new byte[10240];
              int num = -1;
              log("Writing "+s+"...");
              while ((num = fi.read(buf)) > 0)
              {
                fo.write(buf, 0, num);
              }
              fo.close();
              fi.close();
              ++n;
            }
            catch(java.io.IOException ex) {
            	ConditionVariable cvBlock = new ConditionVariable();
            	Log.e(TAG,""+ex);
                print("ERROR: "+ex+"\n");
            	PTTBtn.setEnabled(false);
            	restartBtn.setEnabled(false);
                cvBlock.block();
            }
          }
          log("Done writing "+n+" files.\n");
        }


        // for (int i = 0; i < 15; ++i) log("."+i);

        final java.util.List<String> args = new java.util.Vector();
        String id = "<not set>";
        String logfilename = defaultLogFilename;
        boolean doExit = false;

        final android.content.Intent intent = getIntent();
        final String action = intent.getAction();

        final java.io.File fStatus = new java.io.File(logfilename + ".DONE");
        {
          log("Ensure status file ("+fStatus+") file is not there");
          if (fStatus.exists())
          {
            final boolean b = fStatus.delete();
            if (b)              
              log("... deleted.");
            else
              logW("... FAILED TO DELETE!");
          }
          else
          {
            log("... was not there.");
          }
        }

        int code = -1;
        String status = null;

        try
        {

        // Load dynamic libraries: note that an UnsatisfiedLinkError may still occur when a native method is called.
        init();
        log("All libs loaded.\n");

        for (final String arg : args)
        {
            arg(arg);
        }
        log("Running tests, id="+id+".");
        log("");

        code = run(logfilename);

        log("Finished, code="+code+", id="+id+".");

        if (null != handler && null != PTTBtn)
            handler.post(new Runnable() { public void run() {
            	restartBtn.setVisibility(View.VISIBLE);
            	PTTBtn.setVisibility(View.GONE);
            	PTTBtn.setEnabled(true);
            } });
        
        {
          status = 
            "DONE"
            +"\ncode="+code
            +"\nid="+id
            +"\nversion="+VERSION;
        }

        }
        catch(Throwable lx)
        {
          status = 
            "FAILED"
            +"\nexception="+lx;
        }

        log("Creating status file");
        try { 
          final java.io.PrintStream fo = new java.io.PrintStream(new java.io.FileOutputStream(fStatus));
          fo.println(status);
          fo.flush();
          fo.close();
          log("... done");
        }
        catch(java.io.IOException ex) { logW("... Ooops: "+ex); }


        log(status);

        if (doExit)
        {
          log("Will now exit.");
          try { Thread.sleep(2000l); } catch(InterruptedException e) { }
          System.exit(code);
        }
    }

    
  public void print(final String s) {
    if (null != handler && null != te)
    handler.post(new Runnable() { public void run() { 
      // te.setText(te.getText() + s);
      te.append(s);
    } });
  }

  public void log(String s) {
    Log.i(TAG, s);
    print(s+"\n");
  }

  public void logW(String s) {
    Log.w(TAG, s);
    print("WARNING: "+s+"\n");
  }
    
  public void logW(Exception ex) {
    logW(""+ex);
  }

  public static void print(byte[] s) {
    // Log.i(TAG, "In MainActivity.print()! "+s.length);
    if (null != theInstance)
    {
      theInstance.print(new String(s));
    }
  }

  public void playSound(String audiofile)
  {
	  
	  try {
		  MediaPlayer mp = new MediaPlayer();

		  AssetFileDescriptor afd = getBaseContext().getAssets().openFd(audiofile); 
		  mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
		  afd.close();
		  mp.prepare(); 
		  mp.start();
		  mp.setOnCompletionListener(new OnCompletionListener() {
			  
			  @Override
			  public void onCompletion(MediaPlayer mp) {
				  mp.release();
				  mp = null;
				  cvBlockPlayback.open();
			  }
		  });

	  } catch (Exception e) { }
  }
  
  // Called from main.xml
  public void doPTT(View v) 
  {
      cvBlockPlayback.close();
      HandlerThread thread = new HandlerThread("playSound");
      thread.start();
      Handler playbackHandler = new Handler(thread.getLooper());
      playbackHandler.post(new Runnable() {
          @Override
          public void run() {
        	  playSound(getCacheDir().getAbsolutePath()+"/ptt1.wav");
          }
      });
      
	  cvBlockPlayback.block();

      if (null != handler && null != PTTBtn)
          handler.post(new Runnable() { public void run() {
        	  PTTBtn.setEnabled(false);
          } });
	  
	  btnPressed();
  }
  
  public void doRestart(View v) {
      if (null != handler && null != PTTBtn)
          handler.post(new Runnable() { public void run() {
        	PTTBtn.setVisibility(View.VISIBLE);
        	PTTBtn.setEnabled(true);
          	restartBtn.setVisibility(View.GONE);
          } });

      new Thread() { public void run() { MainActivity.this.run(); } }.start();
  }



	public  void recogFinished(int n) {
		if (null != theInstance) {
		      if (null != theInstance.handler && null != theInstance.PTTBtn)
		      {
		          HandlerThread thread = new HandlerThread("playSound");
		          thread.start();
		          Handler playbackHandler = new Handler(thread.getLooper());
		          playbackHandler.post(new Runnable() {
		              @Override
		              public void run() {
		            	  theInstance.playSound(getCacheDir().getAbsolutePath()+"/ptt2.wav");
		              }
		          });
		    	  
		    	  cvBlockPlayback.block();
		    	  
		    	  theInstance.handler.post(new Runnable() { public void run() {
		        	theInstance.PTTBtn.setEnabled(true);
		          } });
		      }
		    	  
		}
	}
}
