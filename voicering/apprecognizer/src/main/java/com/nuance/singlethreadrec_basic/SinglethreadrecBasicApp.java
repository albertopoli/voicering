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

package com.nuance.singlethreadrec_basic;

import com.nuance.mainactivity.MainActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class SinglethreadrecBasicApp extends com.nuance.mainactivity.MainActivity
{
    @Override
    public String[] getAppAssets() 
    { 
    	String[] assetsToLoad = { 
              "acmod4_900_enu_gen_car_f16_v1_0_0.dat",
              "cdd_ts.fcf",
              "vocon3200_asr.dat"
        };
        return assetsToLoad;
    }
	
    @Override
    public String[] getAppLibs()
    {
    	String[] libsToLoad = { 
    	        "vocon3200_platform", 
    	        "vocon3200_base",  
    	        "genericdca",  
    	        "vocon3200_asr", 
    	        "vocon3200_pron", 
    	        "vocon3200_sem", 
    	        "vocon3200_sem3", 
    	        "vocon3200_gram2", 
    	        "pal_core",
    	        "pal_audio",
    	        "vocon_ext_stream",
    	        "vocon_ext_heap"//,
    	        //"singlethreadrec_basic"
        };
        return libsToLoad;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
}
