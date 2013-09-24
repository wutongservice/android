package com.borqs.account.login.ui;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.login.R;
import com.borqs.account.login.impl.AccountBasicProfile;
import com.borqs.account.login.impl.AccountOperator;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.SimpleTask;
import com.borqs.account.login.util.Utility;


public class AccountDetailActivity extends Activity implements View.OnClickListener,
                CompoundButton.OnCheckedChangeListener{
    private final int TAKE_PHOTO_FROM_CARMER = 1;
    private final int TAKE_PHOTO_FROM_FILE = 2;
    private final int CROP_PHOTO_FILE = 3;
    public final String IMAGE_UNSPECIFIED = "image/*";
    public final String TEMP_PHOTO_FILE = "acn_phtemp.jpg";
    
    private ImageView mProfileImage;
    private EditText mPasswordTv;
    private EditText mUserNameTv;
    private EditText mPhoneTv;
    private TextView mBirthdayTv;
    private EditText mAddressTv;
    
    private AccountService mAccountService;
    private SimpleTask mChangeProfileTask;
    private static Bitmap mPhoto;
       
    private static IOnProfileChanged mProfileChangeListener;
    
    public static void actionShow(Activity from, IOnProfileChanged profileEditListener){
        mPhoto = null;
        mProfileChangeListener = profileEditListener;
        Intent intent = new Intent(from, AccountDetailActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_userdetail);
        mAccountService = new AccountService(this);
        mAccountService.loadData(null);
        initView();
    }
    
    @Override
    public void onDestroy(){
        //mPhoto = null;
        if (mProfileChangeListener != null){
            mProfileChangeListener.onProfileChanged(null);
        }
        if(mChangeProfileTask != null && !mChangeProfileTask.isCancelled()){
            mChangeProfileTask.cancel(true);
        }
        super.onDestroy();
    }

    private void initView(){
        // title
        TextView title = ((TextView)findViewById(R.id.acl_titlebar_tv));
        title.setText(R.string.acl_user_detail_title);
        
        // profile image
        mProfileImage = (ImageView)findViewById(R.id.acl_edit_photo);        
        mProfileImage.setOnClickListener(this);
        if (mPhoto != null){
            mProfileImage.setImageBitmap(mPhoto);
        }
        
         //login id
        String loginId = mAccountService.getLoginId();
        TextView tvLoginId = ((TextView)findViewById(R.id.acl_detail_user_id_tv));
        tvLoginId.setText(loginId);
        
        //show passord check box
        CheckBox chkbox = (CheckBox)findViewById(R.id.acl_show_pwd_box);
        chkbox.setOnCheckedChangeListener(this);
        
        mPasswordTv = ((EditText)findViewById(R.id.acl_register_passwd_tv));
        mUserNameTv = ((EditText)findViewById(R.id.acl_detail_user_name_tv));
        mAddressTv = ((EditText)findViewById(R.id.acl_user_address_tv));
        
        mPhoneTv = ((EditText)findViewById(R.id.acl_user_phone_tv));
        if (Utility.isValidPhoneNumber(loginId)){
            // use login phone as default phone number
            mPhoneTv.setText(loginId);
            mPhoneTv.setEnabled(false);
            mPhoneTv.setTextColor(R.color.acl_darker_gray);
        }
        
        //birthday
        LinearLayout layout = (LinearLayout)findViewById(R.id.acl_user_birthday_layout);
        layout.setOnClickListener(this);
        mBirthdayTv = ((TextView)findViewById(R.id.acl_user_birthday_tv));
        
        //done btn
        Button btn = (Button)findViewById(R.id.acl_detail_done_btn);
        btn.setOnClickListener(this);
    }
    
    @Override
    public void onBackPressed(){
        
    }
    
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.acl_detail_done_btn){
            onDone();
        } else if (v.getId() == R.id.acl_user_birthday_layout){
            showDatePicker();
        } /*else if (v.getId() == R.id.acl_edit_photo){
            showPhotoMenu();
        }*/
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        if (isChecked){
            mPasswordTv.setTransformationMethod(null);
        } else{
            mPasswordTv.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }
    
    public void showDatePicker(){        
        Calendar cal = Calendar.getInstance();
        DatePickerDialog  dlg = 
        new DatePickerDialog(this, 
                new OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, 
                            int year, int monthOfYear, int dayOfMonth) {
                        mBirthdayTv.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }}, 
                  cal.get(Calendar.YEAR), 
                  cal.get(Calendar.MONTH), 
                  cal.get(Calendar.DAY_OF_MONTH));
        dlg.setTitle(R.string.acl_set_birthday);
        dlg.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0){
            return;
        }
        
        // take photo
        if (requestCode == TAKE_PHOTO_FROM_CARMER){
            // Settings file save path here on directory
            File picture = new File(Environment.getExternalStorageDirectory()
                                    + File.separator + TEMP_PHOTO_FILE);            
            startPhotoZoom(Uri.fromFile(picture));
        }
        
        // To read album scaling picture
        if ((requestCode == TAKE_PHOTO_FROM_FILE)&& (data != null)){
            startPhotoZoom(data.getData());
        }
        
        // result
        if ((requestCode == CROP_PHOTO_FILE)&& (data != null)){
            Bundle extras = data.getExtras();
            if (extras != null){
                mPhoto = extras.getParcelable("data");
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //mPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                mProfileImage.setImageBitmap(mPhoto);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY is ratio of width and height
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY after cutting the width and height of the picture
        intent.putExtra("outputX", 64);
        intent.putExtra("outputY", 64);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_PHOTO_FILE);
    }

    
    private void showPhotoMenu(){
        String[] items = new String[] {
                getString(R.string.acl_image_from_camera),
                getString(R.string.acl_image_from_file) };
        AlertDialog.Builder itemsBuilder = new AlertDialog.Builder(this);        
        itemsBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            doTakePhoto();// from camera  
                        } else {
                            doPickPhotoFromFile();// from  gallery
                        }
                    }
                });
        itemsBuilder.create().show();
    }
    
    private void doTakePhoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, 
                       Uri.fromFile(new File(Environment.getExternalStorageDirectory(), 
                                             TEMP_PHOTO_FILE)));
        startActivityForResult(intent, TAKE_PHOTO_FROM_CARMER);
    }
    
    private void doPickPhotoFromFile(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                             IMAGE_UNSPECIFIED);
        startActivityForResult(intent, TAKE_PHOTO_FROM_FILE);
    }

    private void onDone() {   
        if (!validateValues()){
            Toast.makeText(this, 
                    R.string.acl_invalid_input_content, 
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            AccountBasicProfile profile = new AccountBasicProfile();
            profile.setPassword(String.valueOf(mPasswordTv.getText()));
            profile.setUserDispName(String.valueOf(mUserNameTv.getText()));
            profile.setUserPhone(String.valueOf(mPhoneTv.getText()));
            profile.setUserBirthday(String.valueOf(mBirthdayTv.getText()));
            profile.setUserAddress(String.valueOf(mAddressTv.getText()));
            profile.setUserPhoto(mPhoto);

            if(Utility.isValidEmailAddress(mAccountService.getLoginId())){
                profile.setUserEmail(mAccountService.getLoginId());
            }
            changeProfile(profile);
        }
    }
    
    private boolean validateValues(){
        boolean res = true;
        
        if (!Utility.isValidPassword(String.valueOf(mPasswordTv.getText()))){
            mPasswordTv.setError(getString(R.string.acl_error_input_pwd));
            res = false;
        }
        
        if (!Utility.isValidUserDispName(String.valueOf(mUserNameTv.getText()))){
            mUserNameTv.setError(getString(R.string.acl_input_error_required));
            res = false;
        }
        
        if (!Utility.isValidPhoneNumber(String.valueOf(mPhoneTv.getText()))){
            mPhoneTv.setError(getString(R.string.acl_error_input_phone));
            res = false;
        }
                
        return res;
    }

    private void changeProfile(final AccountBasicProfile profile)
    {
        final AccountOperator op = new AccountOperator(this);
        mChangeProfileTask = new SimpleTask();
        AccountHelper.showProgressDialog(this, getString(R.string.acl_changing_profile_info));
        mChangeProfileTask.execute(new Runnable(){
                    @Override
                    public void run() {
                        mChangeProfileTask.setBresult(op.changeProfileInfo(profile));           
                    }},
            new Runnable(){
                @Override
                public void run() {
                    AccountHelper.closeProgressDialog();
                    if(!mChangeProfileTask.getBresult()){
                        AccountHelper.showInfoDialog(AccountDetailActivity.this, 
                                getString(R.string.acl_change_profile_failed), 
                                op.getError());
                    } else {
                        mPhoto = null;
                        finish();
                    }
                }});
    }
    
    public static interface IOnProfileChanged{
        public void onProfileChanged(AccountBasicProfile profile);
    }
}

