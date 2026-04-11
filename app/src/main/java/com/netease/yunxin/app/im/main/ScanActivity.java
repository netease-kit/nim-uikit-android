// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.zxing.client.android.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;

/** */
public class ScanActivity extends Activity {
  private CaptureManager capture;
  private DecoratedBarcodeView barcodeScannerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(this, com.netease.yunxin.app.im.R.string.network_error, Toast.LENGTH_SHORT)
          .show();
      finish();
      return;
    }

    barcodeScannerView = initializeContent();

    capture = new CaptureManager(this, barcodeScannerView);
    capture.initializeFromIntent(getIntent(), savedInstanceState);
    capture.decode();
  }

  /**
   * Override to use a different layout.
   *
   * @return the DecoratedBarcodeView
   */
  protected DecoratedBarcodeView initializeContent() {
    setContentView(R.layout.zxing_capture);
    return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (capture != null) {
      capture.onResume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (capture != null) {
      capture.onPause();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (capture != null) {
      capture.onDestroy();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (capture != null) {
      capture.onSaveInstanceState(outState);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    if (capture != null) {
      capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return (barcodeScannerView != null && barcodeScannerView.onKeyDown(keyCode, event))
        || super.onKeyDown(keyCode, event);
  }
}
