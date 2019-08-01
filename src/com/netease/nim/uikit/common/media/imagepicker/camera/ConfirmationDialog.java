package com.netease.nim.uikit.common.media.imagepicker.camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.netease.nim.uikit.R;


public class ConfirmationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setMessage(R.string.permission_request).setPositiveButton(
                R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), CaptureActivity.VIDEO_PERMISSIONS,
                                                          CaptureActivity.VIDEO_PERMISSIONS_REQUEST_CODE);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        }).create();
    }
}
