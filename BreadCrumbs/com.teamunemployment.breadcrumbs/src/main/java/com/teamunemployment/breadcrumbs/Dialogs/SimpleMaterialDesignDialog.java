package com.teamunemployment.breadcrumbs.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.R;



/**
 * Created by jek40 on 24/04/2016.
 */
public class SimpleMaterialDesignDialog {

    private Context mContext;
    private String mTitle;
    private String mBody;
    private static Dialog mDialog;
    private static SimpleMaterialDesignDialog mInstance;

    private SimpleMaterialDesignDialog() {
        setUpOkButton();
    }

    public static SimpleMaterialDesignDialog Build(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.material_design);

        mInstance = new SimpleMaterialDesignDialog();
        return mInstance;
    }

    public SimpleMaterialDesignDialog SetActionWording(String word) {
        TextView actionButton = (TextView) mDialog.findViewById(R.id.header_ok_button);
        actionButton.setText(word);
        return mInstance;
    }

    public SimpleMaterialDesignDialog UseCancelButton(boolean isVisible) {
        TextView cancelButton = (TextView) mDialog.findViewById(R.id.header_cancel_button);

        if (isVisible) {
            cancelButton.setVisibility(View.VISIBLE);
            return mInstance;
        }

        cancelButton.setVisibility(View.GONE);
        return mInstance;
    }

    private void setUpOkButton() {
        mDialog.findViewById(R.id.header_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    public SimpleMaterialDesignDialog SetTitle(String title) {
        mTitle = title;
        TextView headertextView = (TextView) mDialog.findViewById(R.id.material_design_dialog_header);
        headertextView.setText(title);
        return mInstance;
    }

    public SimpleMaterialDesignDialog SetTextBody(String body) {
        mBody = body;
        TextView detailsTextView = (TextView) mDialog.findViewById(R.id.dialog_body);
        detailsTextView.setText(body);
        return mInstance;
    }

    /* <p> Show the dialog that we have built. </p>
     */
    public void Show() {
        if (mTitle == null) {
            throw new NullPointerException("Title was not set. Title must be set.");
        }
        if (mBody == null) {
            throw new NullPointerException("Body was not set. Body must be set");
        }

        mDialog.show();
    }

    public SimpleMaterialDesignDialog SetCallBack(final IDialogCallback callbackMethod) {
        mDialog.findViewById(R.id.header_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackMethod.DoCallback();
            }
        });

        return mInstance;
    }
}
