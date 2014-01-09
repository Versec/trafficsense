/**
 * Copyright (c) 2012 Aalto University and the authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 *  
 * Authors:
 * Chaudhary Nalin (nalin.chaudhary@aalto.fi)
 * Chao Wei (chao.wei@aalto.fi)
 */
package org.sizzlelab.contextlogger.android;

import org.sizzlelab.contextlogger.android.io.MainPipeline;
import org.sizzlelab.contextlogger.android.utils.Constants;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.Switch;
import android.app.ActionBar;
import android.view.MenuItem;

import fi.aalto.chaow.android.app.BaseFragmentActivity.OnSupportFragmentListener;

public class MainActivity extends Activity implements OnSupportFragmentListener, 
																Constants, OnSharedPreferenceChangeListener{
	
	private OnContextLoggerStatusChangeListener mLoggerStatusChangeListener = null;
	private ActionBar mActionBar = null;
	private Switch mSwitch = null;
	private View mSwitcherView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_act);
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		System.out.println("DBG: checkpoint 1");
		initUI();
		System.out.println("DBG: checkpoint 2");
		MainPipeline.getSystemPrefs(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		System.out.println("DBG: checkpoint 3");
	}

	private void initUI(){
	    // Set up the action bar.
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(ActionBar.DISPLAY_SHOW_CUSTOM);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);

        mSwitcherView = getLayoutInflater().inflate(R.layout.logging_switcher, null);
        mSwitch = (Switch) mSwitcherView.findViewById(R.id.logger_switcher);
        mSwitch.setTextOn(getString(R.string.start));
        mSwitch.setTextOff(getString(R.string.stop));
        final ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
					        		ViewGroup.LayoutParams.WRAP_CONTENT,
					                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        lp.setMargins(0, 0, 5, 0);
        mActionBar.setCustomView(mSwitcherView, lp);
		
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        LoggerPanelFragment lpf = new LoggerPanelFragment();
        mLoggerStatusChangeListener = (OnContextLoggerStatusChangeListener)lpf;
        mSwitch.setOnCheckedChangeListener((OnCheckedChangeListener)lpf);
		transaction.replace(R.id.screen_container, lpf, "loggerPanel");
		transaction.commit();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		final int itemId = item.getItemId();
		if(itemId == android.R.id.home){
			resetActionbar();
			getFragmentManager().popBackStack();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onFragmentChanged(int layoutResId, Bundle args) {
		FragmentTransaction transaction =getFragmentManager().beginTransaction();

		ListFragment listFragment = null;
		
		if(layoutResId == R.layout.logger_history){
			listFragment = new LoggerHistoryFragment();
		}
		
		if(listFragment != null){
			if(args != null){
				listFragment.setArguments(args);
			}

			if(listFragment instanceof LoggerHistoryFragment){
				transaction.replace(R.id.screen_container, listFragment, "loggerHistory"); 
				constructActionbar();
			}
			// Add to this transaction into BackStack
			transaction.addToBackStack(listFragment.getTag());
			
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// Commit this transaction
			transaction.commit();
			return;
		}
	}

	private void constructActionbar(){
		 mActionBar.setHomeButtonEnabled(true);
		 mActionBar.setDisplayHomeAsUpEnabled(true);
		 mSwitcherView.setVisibility(View.INVISIBLE);
	}
	
	private void resetActionbar(){
		Fragment f = getFragmentManager().findFragmentByTag("loggerHistory");
		if(f != null){
			if(f.isAdded()){
				 mActionBar.setHomeButtonEnabled(false);
				 mActionBar.setDisplayHomeAsUpEnabled(false);
				 mSwitcherView.setVisibility(View.VISIBLE);				
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			resetActionbar();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (MainPipeline.COUNT_KEY.equals(key)) {
			if(mLoggerStatusChangeListener != null){
				mLoggerStatusChangeListener.onLoggerCountChanged(MainPipeline.getScanCount(getApplicationContext())); 
			}
		}
	}
	
	public static interface OnContextLoggerStatusChangeListener{
		void onLoggerCountChanged(final long count);
	}
}
