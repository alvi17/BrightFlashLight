package alvi17.brightflashlight;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by User on 12/27/2016.
 */

public class RequestPermissionDialog extends Dialog implements View.OnClickListener {
    TextView btn_no;
    private final TextView btn_ok;
    private final Context mContext;
    private View v = null;
    private TextView mTitle = null;
    private TextView mMessage = null;
    private final OnClickListenerHandlerPermission onClickListenerHandler;
    public RequestPermissionDialog(Context context, OnClickListenerHandlerPermission onClickListenerHandler) {
        super(context);
        this.onClickListenerHandler=onClickListenerHandler;
        mContext = context;
        /** 'Window.FEATURE_NO_TITLE' - Used to hide the mTitle */
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /** Design the dialog in main.xml file */
        setContentView(R.layout.request_permission_dialog);
        v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);

        btn_ok = (TextView) findViewById(R.id.btn_ok);
        mTitle = (TextView) findViewById(R.id.title);
        mMessage = (TextView) findViewById(R.id.message);
        btn_ok.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        /** When OK Button is clicked, dismiss the dialog */
        dismiss();
        onClickListenerHandler.onClickListenerOk();

    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle.setText(title);
    }
    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mTitle.setText(mContext.getResources().getString(titleId));
    }
    public void setMessage(CharSequence message) {
        mMessage.setText(message);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void setMessage(int messageId) {
        mMessage.setText(mContext.getResources().getString(messageId));
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    public interface OnClickListenerHandlerPermission {

        void onClickListenerOk();


    }
}