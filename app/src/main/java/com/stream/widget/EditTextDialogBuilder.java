package com.stream.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/3/31 0031.
 */

public class EditTextDialogBuilder extends AlertDialog.Builder {

    private TextInputLayout mTextInputLayout;
    private EditText mTextInputEditText;

    public EditTextDialogBuilder(@NonNull Context context, String hint) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext_builder, null);
        setView(view);

        mTextInputLayout = (TextInputLayout) view.findViewById(R.id.edit_text_layout);
        mTextInputEditText = (TextInputEditText) view.findViewById(R.id.edit_text);
        mTextInputLayout.setHint(hint);
    }

    public String getText() {
        return mTextInputEditText.getText().toString();
    }

    public void setError(CharSequence error) {
        mTextInputLayout.setError(error);
    }
}
