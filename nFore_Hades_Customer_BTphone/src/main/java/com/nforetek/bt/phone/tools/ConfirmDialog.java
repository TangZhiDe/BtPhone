package com.nforetek.bt.phone.tools;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.nforetek.bt.phone.R;

/**
 * @author tzd
 *
 */
public class ConfirmDialog extends Dialog {

    private Context context;
    private String title;
    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {

         void doConfirm();

         void doCancel();
    }

    public ConfirmDialog(Context context, String title) {
        super(context, com.nforetek.bt.phone.R.style.dialog);
        this.context = context;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(com.nforetek.bt.phone.R.layout.alertdialog_layout, null);
        setContentView(view);

        TextView alert_bt_cancel = view.findViewById(com.nforetek.bt.phone.R.id.alert_bt_cancel);
        Button alert_bt_disconnect = view.findViewById(com.nforetek.bt.phone.R.id.alert_bt_disconnect);
        TextView alert_bt_name = view.findViewById(com.nforetek.bt.phone.R.id.alert_bt_name);

        alert_bt_name.setText(title);

        alert_bt_disconnect.setOnClickListener(new clickListener());
        alert_bt_cancel.setOnClickListener(new clickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 640;
        lp.height = 330;
        lp.x = -250;
        lp.y = 50;
//        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
                case com.nforetek.bt.phone.R.id.alert_bt_disconnect:
                    clickListenerInterface.doConfirm();
                    break;
                case com.nforetek.bt.phone.R.id.alert_bt_cancel:
                    clickListenerInterface.doCancel();
                    break;
            }
        }

    };

}