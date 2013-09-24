package com.borqs.qiupu;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts.Intents.Insert;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Toast;

/**
* <p>
* Title: AddressPadMini
* </p>
*
* <p>
* Description: AddressPadMini is an iPhone style send to list and provide
* some useful functions for edit/view address
* </p>
* 
*/
public class AddressPadMini extends AutoCompleteTextView {
    private static final String TAG =  "AddressPadMini";
    // debug switcher 
    private static final boolean                    LOCAL_LOGV                   = QiupuConfig.LOGD;

	// drawing parameters
	private int									    mRadius;
	private int								    	mAscent;
	private int								    	mDescent;
	private int								    	mTop;
	private int								    	mBottom;
    private int                                     mFontHeight;
    private Bitmap                                  mRemoveIcon;

    //------------------------------------------------------------------------
	private static final int LINE_TOP_PADDING		                  =  5;
	private static final int LINE_BOTTOM_PADDING		              =  5;
	private static final int TOP_INNER_PADDING		                  =  3;
	private static final int BOTTOM_INNER_PADDING	                  =  3;
	private static final int LEFT_PADDING			                  =  3;
	private static final int RIGHT_PADDING			                  =  3;
	private static final int ADDRESS_TOP_PADDING = 4;
    
    private static final int WORKING_MODE_BUTTON_MODE                 =  1; // for portrait  screen mode 
    private static final int WORKING_MODE_PLAIN_MODE                  =  2; // for landscape screen mode 

    private static final int DECORATOR_TYPE_NONE                      = -1;
    private static final int DECORATOR_TYPE_MAIL                      =  0;
    private static final int DECORATOR_TYPE_PHONE_MAIL                =  1;

    private static final int SINGLE_LINE_HINT_STYLE_PLAIN_TEXT        =  0;
    private static final int SINGLE_LINE_HINT_STYLE_BUTTON_LIKE       =  1;

    private static final String PHONE_NUMBER_SEPARATORS               = " ()-.";
    private static final int ADDRESS_TYPE_EMAIL                       =  0;
    private static final int ADDRESS_TYPE_PHONE                       =  1;
    private static final int CONTACT_INFO_SUB_TYPE_NONE               = -1;
    private static final int CONTACT_INFO_SUB_TYPE_PHONE_LOCAL        =  2;
    private static final int CONTACT_INFO_SUB_TYPE_PHONE_SIM          =  3;
    private static final int MENU_ID_COPY                             = 100;
    private static final int MENU_ID_CUT                              = 101;
    private static final int MENU_ID_CUT_ALL                          = 102;
    private static final int MENU_ID_PASTE                            = 103;
    private static final int MENU_ID_SELECT                           = 104;
    private static final int MENU_ID_SELECT_ALL                       = 105;
    private static final int MENU_ID_EDIT                             = 106;
    private static final int MENU_ID_DELETE                           = 107;
    private static final int MENU_ID_ADD_CONTACT                      = 108;
    private static final int MENU_ID_VIEW_CONTACT                     = 109;

    /**
     * AddressSpan spanned type 
     */
    private static final int ADDRESS_PAD_SPAN_TYPE                    = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_INTERMEDIATE;

    /**
     * A placeholder is an character only for attaching span object on it, it has no actual meaning.
     * NOTICE(zhoujb): certainly any string can be a placeholder, but I recommend you do not change 
     * it, there are some bugs when use a string which contains more than one character as placeholder.
     */
    private static final String PLACE_HOLDER                          = "\u0014"; // use "%" for debug

    /**
     * Length of placeholder
     */
    private static final int PLACE_HOLDER_LEN                         = PLACE_HOLDER.length();

    /**
     * Dummy separator for split operaion
     */
    private static final String DUMMY_SEPARATOR                       = "//#sep#";
	protected static final int CONFIGURATION_CHANGED = 0;

    /**
     * All custmize options 
     */
    private Options mOptions                                          = new Options();

    /**
     * @hide
     * For history reason, I need this class to distinguish my change from origin code,
     * but until refactory finished, only a few lines are original, maybe it's better to 
     * remove it in the feature if you like.
     */
    private AddressPadMiniController  mController                     = null;

    /**
     * This interface give chance to implements how to decorate address in button mode,
     * if set a new OnDecorateAddressListener, the old one will be droped.
     */
    private OnDecorateAddressListener mDecorator                      = null; 
    
    private EnableCompletionListener mCompletion                      = null;
    
    /**
     * Interface for watching address change(Only support single watcher now)
     */
    private AddressWatcher            mAddressWatcher                 = null; // TODO(zhoujb): support multi-watcher 

    /**
     * Enable or disable context menu
     */
    private boolean mEnableContextMenu                                = true;
    
    /**
     * For update decorate text after added new address to phonebook
     */
    private AddressNode mLastAddToContact                             = null;
    
    /**
     * AddressPad working mode 
     * <li>WORKING_MODE_BUTTON_MODE: for portrait mode and landscape mode when readonly</li> 
     * <li>WORKING_MODE_PLAIN_MODE:  for landscape mode when it is not readonly</li>
     */
    private int mWorkingMode                                          = WORKING_MODE_BUTTON_MODE;
    
    /**
     * line count change watcher, we use it adjust the position of popup window
     */
    private TextWatcher  mLineChangeWatcher                           = new LineCountChangeWatcher(this);

    private boolean mAddressWatcherDisableOnInput                     = false;   
    private boolean isAlreadyEnter                                    = false;
    
    private int mOrientation = getResources().getConfiguration().orientation;
    /**
     * This observer only for "phone_mail" decorator, it
     * will redecorated specify address after contact
	  * updated 
	  */
    private ContentObserver mContactObserver                          = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                D("CONTACT CHANGE ...");
                if(mLastAddToContact != null) {
                    if(mLastAddToContact.mSpan != null) {
                        String addr = mLastAddToContact.getData();
                        mLastAddToContact.mSpan.setDecoratedText(addr);
                        // WORKAROUND(zhoujb): I found the cursor will not adjust to the end of last span 
                        // when the decorated text shoter than origin text, so simplely move the cursor to the head.
                        setSelection(0);
                    }
                    mLastAddToContact = null;
                    invalidate();
                }
            }
        };

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * <p>Options of AddressPadMini</p>
     */
    public final class Options {
        public boolean  EnableMultiSelection  = false;   // enable multi selection            (NOT SUPPORT YET)
        public boolean  EnableTrimSpace       = true;    // enable trim space of each address (NOT SUPPORT YET)
        public boolean  EnableDuplicate       = false;   // enable duplication                (NOT SUPPORT YET)
        public String   Separator             = ",";
        public Drawable AddressBg             = null;    // background of address span 
        public Drawable AddressFg             = null;    // foreground of address span
        public Drawable AddressSBg            = null;    // background of selected address 
        public Drawable AddressSFg            = null;    // foreground of selected address
        public Drawable AddressCircleBg       = null;    // background of circle address
        public Drawable publicCircleBg        = null;    
        public int      AddressTextColor      = 0;
        public int      AddressTextSColor     = 0;
        public int      MaxSize               = 0;       // max address size                  (NOT SUPPORT YET)        
        public int      Threshold             = 0;       // completion threshold 
        public int      CacheSize             = 100;     // for duplication check
        public int      ExpandAt              = 10;      // where to start expand 
        public int      ExpandStep            = 10;       // how many address will expanded of each click 
        public int      DecoratorType         = DECORATOR_TYPE_NONE;       // inner decorator type
        public boolean  ReadOnly              = false;   // read only mode 
        public String   MoreHint              = "More:"; // hint text on expander
        public int      WorkingMode           = WORKING_MODE_BUTTON_MODE;
        public int      SingleLineHintStyle   = SINGLE_LINE_HINT_STYLE_PLAIN_TEXT;
        public boolean  DisableOritationChange= false;
        public String   Connector             = Separator+" ";
        
        public String getEscapedSeparator() {
            return "\\" + Separator;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[AddressPadMini Options]:")
                .append("\nEnableMultiSelection  = " + EnableMultiSelection)
                .append("\nEnableTrimSpace       = " + EnableTrimSpace)
                .append("\nEnableDuplicate       = " + EnableDuplicate)
                .append("\nSeparator             = " + Separator)
                .append("\nAddressBg             = " + AddressBg)
                .append("\nAddressFg             = " + AddressFg)
                .append("\nAddressSBg            = " + AddressSBg)
                .append("\nAddressSFg            = " + AddressSFg)
                .append("\nThreshold             = " + Threshold)
                .append("\nCacheSize             = " + CacheSize)
                .append("\nExpandAt              = " + ExpandAt)
                .append("\nExpandStep            = " + ExpandStep)
                .append("\nDecoratorType         = " + DecoratorType)
                .append("\nReadOnly              = " + ReadOnly)
                .append("\nMoreHint              = " + MoreHint)
                .append("\nAddressTextColor      = " + AddressTextColor)
                .append("\nAddressTextSColor     = " + AddressTextSColor)
                .append("\nSingleLineHintStyle   = " + SingleLineHintStyle)
                .append("\nDisableOritationChange= " + DisableOritationChange)
                .append("\nConnector             = " + Connector);
            return sb.toString();
        }
    }
    
    // a simple snippet of how to use AddressPad.onDecorateAddressListener
    private class simpleDecorater implements OnDecorateAddressListener {
        public String onDecorate(String address) {
            if(address.equals("zhoujb")) {
                return "GoodMan";
            } else  {
                return address;
            }
        }
    }        


    /**
     * update popup window on line change 
     */
    private class LineCountChangeWatcher implements TextWatcher {
        private View mAnchorView = null;
        private int  mLineConunt = 1;
        
        /**
         * <p>anchor view to show dropdown window</p>
         */
        LineCountChangeWatcher(View anchor) {
            mAnchorView = anchor;
        }

        public void afterTextChanged(Editable s) {
            if(mLineConunt < getLineCount()) {
              //  setDropDownVerticalOffset(mFontHeight);
                dismissDropDown();
            } else {
              //  setDropDownVerticalOffset(0);
            }              
        }
        
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // only update line counter before text change
            updateLineCount();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // NOP
        }
        
        /**
         * update line counter 
         */
        private void updateLineCount() {
            mLineConunt = getLineCount();
        }
    }
    
    /**
     * @hide
     * remove the decorate in PhoneNumberDecorater
     * 
     * e.g: decorate Zheng Yan<13581952109> as 13581952109
     *      decorate Zheng Yan<zcat1002@gmail.com> as zcat1002@gmail.com
     */
    public class RemovePhoneNumberDecorater implements OnDecorateAddressListener{
        public String onDecorate(String decoratedPhoneNumber){
            if((decoratedPhoneNumber== null)||("".equals(decoratedPhoneNumber))){
                return null;
            }
            if((decoratedPhoneNumber.contains("<"))&&(decoratedPhoneNumber.contains(">"))){
                return decoratedPhoneNumber.substring(decoratedPhoneNumber.indexOf("<")+1, decoratedPhoneNumber.length()-1);
            }else{
                return decoratedPhoneNumber;
            }
        }
    }

    /**
     * @hide
     * Phone number decorater show name and address like this:
     * e.g:
     * Zheng Yan<13581952109>
     * 
     * Zheng Yan<zcat1002@gmail.com>
     */
    public class PhoneNumberEmailDecorater implements OnDecorateAddressListener{
    	private StringBuilder nameBuidler ;
    	public void setNameString(StringBuilder names) {
    		nameBuidler = names;
    	}
        public String onDecorate(String address){
            int type;
            ContactInfo ci = null;
            if(isPhoneNumber(address)) {
                type = ADDRESS_TYPE_PHONE;
            } else {
                type = ADDRESS_TYPE_EMAIL;
            }
            
            switch(type){
            case ADDRESS_TYPE_PHONE:
                ci = getContactInfoByPhoneNumber(getContext(), address);
                break;
            case ADDRESS_TYPE_EMAIL:
                ci = getContactInfoByEmailAddress(getContext(), address);
                break;
            }
            if(ci!=null){
            	if(!TextUtils.isEmpty(ci.name)){
            		 if(nameBuidler != null) {
                     	if(nameBuidler.length() > 0) {
                     		nameBuidler.append(",");
                     	}
                     	nameBuidler.append(ci.name);
                     }
                    return ci.name + "<"+address+">";
                }else{
                	if(nameBuidler != null) {
                     	if(nameBuidler.length() > 0) {
                     		nameBuidler.append(",");
                     	}
                     	nameBuidler.append(address);
                     }
                    return "<"+address+">";
                }
            	 
            }else{
            	if(nameBuidler != null) {
                 	if(nameBuidler.length() > 0) {
                 		nameBuidler.append(",");
                 	}
                 	nameBuidler.append(address);
                 }
            	return "<"+address+">";
            }
        }
    }

    /**
     * EmailDecorater parse name and address field from email address
     * if get name, then show name, otherwise display whole address 
     * e.g: 
     * "Amas"<amas@gmail.com> will show as "Amas"
     */
    public class EmailDecorater implements OnDecorateAddressListener {
        public String onDecorate(String address) {
            ContactInfo ci = parseContactInfoFromEmailAddress(address);
            return (ci != null && !TextUtils.isEmpty(ci.name)) ? ci.name : address;
        }   
    }
    
    /**
     *  this decorator is more complex and time consumed 
     *  1. if the address is phone number, it will search phone book ,
     *      if search success, the contact name will be used for Decorator
     *      otherwise number will be used.
     *  2. if the address is email, it will search phone book,
     *     if search success, the contact name will be used, otherwise
     *     the address will be used.
     */
    public class PhoneEmailDecorater implements OnDecorateAddressListener {
        private Context mContext = null;
        public PhoneEmailDecorater(Context context) {
            mContext = context;
        }

        public String onDecorate(String address) {
            String showText = address;
            if(isPhoneNumber(address)) {
                ContactInfo ci = getContactInfoByPhoneNumber(mContext, address);
                if(!TextUtils.isEmpty(ci.name)) {
                    showText = ci.name;
                } else {
                    
                }
            } else {
                ContactInfo ci = getContactInfoByEmailAddress(mContext, address);
                if(!TextUtils.isEmpty(ci.name)) {
                    showText = ci.name;
                } 
            }
            return showText;
        } 
    }

    /**
     *  <p>Callback on draw address button</p>
     */
    public interface OnDecorateAddressListener {
        /**
         * @param address origin addresses
         * @return text which will be display 
         */
        String onDecorate(String address);
    }
    
    public interface EnableCompletionListener {
    	public void onInputCompletion();
    }
    
    
    /**
     * <p> Watch the addresses change </p>
     */
    public interface AddressWatcher {
        /**
         * On accept address 
         */
        public void onAccept(String text);
        /**
         * On input
         */        
        public void onInput(String text);
        /**
         * On delete address
         */
        public void onRemove(String text);
    }

    /**
     * Set working mode 
     */    
    private int setWorkingMode(int mode) {
        int old = mWorkingMode;
        mWorkingMode = mode;
        return old;
    }
    
    /**
     * Get working mode 
     */
    private int getWorkingMode() {
        return mWorkingMode;
    }
    
    /**
     * @return if current working mode is WORKING_MODE_BUTTON_MODE return true else false
     */
    private boolean isButtonMode() {
        return WORKING_MODE_BUTTON_MODE == mWorkingMode;
    }

    /**
     * @return if current working mode is WORKING_MODE_PLAIN_MODE return true else false
     */
    private boolean isPlainMode() {
        return WORKING_MODE_PLAIN_MODE  == mWorkingMode;
    }

    /**
     * Set OnDecorateAddressListener(Only support one)
     */
    public void setOnDecorateAddressListener(OnDecorateAddressListener listener) {
        mDecorator = listener;
    }
    
    public void setEnableCompletionListener (EnableCompletionListener listener) {
    	mCompletion = listener;
    }

    /**
     * Set AddressWatcher(Only support one)
     */
    public void setAddressWatcher(AddressWatcher watcher) {
        mAddressWatcher = watcher;
    }

    /**
     * @hide
     * Get Options object 
     */
    public Options getOptions() {
        return mOptions;
    }

    /**
     * @hide
     * Set Options object 
     * @return old options 
     */
    public Options setOptions(Options options) {
        Options old = mOptions;
        mOptions = options;
        return old;
    }

    //-------------------------------------------------------------------------
    // init
    //-------------------------------------------------------------------------
    public AddressPadMini(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public AddressPadMini(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, android.R.attr.autoCompleteTextViewStyle);
	}
   
	public AddressPadMini(Context context) {
		super(context);
		init(context, null, android.R.attr.autoCompleteTextViewStyle);
	}
	
	/**
	 * Internal initialization
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
        resetPadding();
        setThreshold(1);
        //setText("", BufferType.SPANNABLE);
        
        
		// save font metrics and calculate paddings
		TextPaint      paint = getPaint();
		FontMetricsInt fmInt = paint.getFontMetricsInt();
		mAscent  = fmInt.ascent;
		mDescent = fmInt.descent;
		mTop     = fmInt.top;
		mBottom  = fmInt.bottom;
		mRadius  = (mDescent - mAscent + TOP_INNER_PADDING + BOTTOM_INNER_PADDING) >> 1;
        mFontHeight = fmInt.bottom - fmInt.top;
        mRemoveIcon = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_acl_x));
//        setEnableMagnifier(false);
        //setDoubleClickable(false);
        setSaveEnabled(false);
//        setSelPinsEnabled(false);
        
        setFocusable(true);
		setFocusableInTouchMode(true);
        setLineSpacing(10,1);
        TypedArray sa = context.obtainStyledAttributes(attrs, R.styleable.AddressPadMini);
        mOptions.AddressBg = sa.getDrawable(R.styleable.AddressPadMini_addressBg);
        if(mOptions.AddressBg == null) {
            mOptions.AddressBg = context.getResources().getDrawable(R.drawable.chip_blue);
        }

        mOptions.AddressSBg = sa.getDrawable(R.styleable.AddressPadMini_selectedAddressBg);
        if(mOptions.AddressSBg == null) {
            mOptions.AddressSBg = context.getResources().getDrawable(R.drawable.addresspadmini_font_bg);
        }

        if(mOptions.AddressCircleBg == null) {
            mOptions.AddressCircleBg =  context.getResources().getDrawable(R.drawable.chip_green);
        }
        
        if(mOptions.publicCircleBg == null) {
            mOptions.publicCircleBg = context.getResources().getDrawable(R.drawable.chip_gray);
        }
        
        mOptions.AddressTextColor  = sa.getColor(R.styleable.AddressPadMini_addressTextColor, 
                                                 getResources().getColor(R.drawable.default_address_pad_text_color));

        mOptions.AddressTextSColor = sa.getColor(R.styleable.AddressPadMini_selectedAddressTextColor,
                                                 getResources().getColor(R.drawable.default_address_pad_selected_text_color));

        mOptions.Separator = sa.getString(R.styleable.AddressPadMini_separator);
        if(TextUtils.isEmpty(mOptions.Separator)) {
            mOptions.Separator = ",";
        }
        
//        mOptions.Threshold           = sa.getInt(R.styleable.AddressPadMini_threshold, 2);
        mOptions.Threshold           = 1;
        mOptions.ExpandAt            = sa.getInt(R.styleable.AddressPadMini_expandAt, mOptions.ExpandAt);
        mOptions.ExpandStep          = sa.getInt(R.styleable.AddressPadMini_expandStep, mOptions.ExpandStep);               
        mOptions.CacheSize           = sa.getInt(R.styleable.AddressPadMini_cacheSize, mOptions.CacheSize);
        mOptions.DecoratorType       = sa.getInt(R.styleable.AddressPadMini_decoratorType, mOptions.DecoratorType);
        mOptions.ReadOnly            = sa.getBoolean(R.styleable.AddressPadMini_readOnly, mOptions.ReadOnly);
        mOptions.MoreHint            = context.getResources().getString(R.string.hint_more);
        mOptions.SingleLineHintStyle = sa.getInt(R.styleable.AddressPadMini_singleLineHintStyle, mOptions.SingleLineHintStyle);

        sa.recycle();
        
        // select working mode
        int orientation = context.getResources().getConfiguration().orientation;
        if(!mOptions.ReadOnly) {
            switch(orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                //mOptions.WorkingMode = WORKING_MODE_PLAIN_MODE;
//                setWorkingMode(WORKING_MODE_PLAIN_MODE);
            	setWorkingMode(WORKING_MODE_BUTTON_MODE);
                D("WORKING_MODE_PLAIN_MODE");
                break;
            case Configuration.ORIENTATION_PORTRAIT: 
                //mOptions.WorkingMode = WORKING_MODE_BUTTON_MODE;
                setWorkingMode(WORKING_MODE_BUTTON_MODE);
                D("WORKING_MODE_BUTTON_MODE");
                break;
            case Configuration.ORIENTATION_SQUARE:
                break;
            }
        }
        
        mController = new AddressPadMiniController(this);
        //setMinHeight(getHeight()+getPaddingTop()+getPaddingBottom());

        // install inner decorator
        installDecorator(mOptions.DecoratorType,context);

        if(mOptions.ReadOnly) {
            setCursorVisible(false);
            disableContextMenu();
            //NOTICE(zhoujb): do not use setInputType(), it may case text buffer ineditable
            setRawInputType(InputType.TYPE_NULL);
        } else {
            addTextChangedListener(mLineChangeWatcher);
        }
	}

    public void disableContextMenu() {
        mEnableContextMenu = false;
    }
 
    public void enableContextMenu() {
        mEnableContextMenu = true;
    }

    public boolean isEnabledContextMenu() {
        return mEnableContextMenu;
    }

    /** 
     *  install inner decorator
     *  @param type -1: none | 0: email decorator (See: EmailDecorater) | 1: phone and email decorator(See: PhoneEmailDecorater)
	  *  @param context context
     */
    private void installDecorator(int type, Context context) {
        OnDecorateAddressListener d = null;
        switch(type) {
        case DECORATOR_TYPE_MAIL:
            d = new EmailDecorater();
            break;
        case DECORATOR_TYPE_PHONE_MAIL:
            d = new PhoneEmailDecorater(getContext());
            context.getContentResolver().registerContentObserver(ContactsContract.AUTHORITY_URI, true, mContactObserver);
            break;
        default:
            /* NOP */
        }
        setOnDecorateAddressListener(d);
    }

    public void clearContentObserver(Context context){
        try {
    	    context.getContentResolver().unregisterContentObserver(mContactObserver);
        } catch (Exception e) {            
        }
    }


    //TODO(zhoujb): we plan to add a common completion way
    public class Completor {
        
    }

        
    /**
     * PendingText record the position and the 
     * string where do not be entered in the current text buffer.
     */
    public final class PendingText {
        public PendingText(int position, String text) {
            mPos = position;
            mText= text;
        }
        
        public int getPosition() {
            return mPos;
        }

        public String getText() {
            return mText;
        }
        
        public boolean isEmpty() {
            return TextUtils.isEmpty(mText);
        }

        public int length() {
            return mText.length();
        }

        public int inc(int offset) {
            return mPos += offset;
        }

        public String toString() {
            return "{ "+ " AT: " + mPos + " TEXT: " + mText;
        }

        private int    mPos  = -1;
        private String mText = "";
    }

    //----------------------------------------------
    // Functions ...
    //----------------------------------------------
    @Override
    public boolean enoughToFilter() {      
        return mController == null ? false : mController.enoughToFilter();
    }

    private  void performFiltering(CharSequence text, int start, int end, int keyCode) {
        if(mController != null) {
            getFilter().filter(text.subSequence(start, end), this);
        }
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if(mController != null) {
            mController. performFiltering(text, keyCode);
        }
    }


    public class AddressPadMiniController {    
        public AddressPadMiniController(AddressPadMini ap) {
            mTextBuffer = (SpannableStringBuilder)ap.getText();            
            mOptions    = ap.getOptions();
            mSelf       = ap;
            mAdt        = new AddressAdt(mOptions.CacheSize);
            //mTextBuffer.setSpan(new LinePaddingSpan(), 0, 0, 0);
        }

      
        public PendingText[] getPendingText() {
            String[] tmp = mTextBuffer.toString().split(PLACE_HOLDER);
            ArrayList<PendingText> pt = new ArrayList<PendingText>();
            for(int i=0; i<tmp.length; ++i) {
                if(!TextUtils.isEmpty(tmp[i])) {
                    pt.add(new PendingText(i,tmp[i]));
                }
            }
            return pt.toArray(new PendingText[pt.size()]);
        }


        // shotcut for calc n place hold length
        private int L(int i){
            return i*PLACE_HOLDER_LEN;
        }
        
        /**
         * clear buffer text and reset adt
         */
        public void clearAll() {
            setText("");
            mAdt.clearAll();
        }

        private void addNewSpan(AddressNode node) {
            String data = node.getData();
            if(data != null && data.contains("#")) {
//                String tmpData = data.replace("#", "");
//                if(QiupuConfig.CIRCLE_ID_PUBLIC == Long.parseLong(tmpData)) {
//                    node.mSpan    = new AddressSpan(data,mOptions, AddressSpan.SPAN_TYPE_PUBLIC_CIRCLE);
//                }else {
                    node.mSpan    = new AddressSpan(data,mOptions, AddressSpan.SPAN_TYPE_CIRCLE, node);
//                }
            }else {
                node.mSpan    = new AddressSpan(data,mOptions, AddressSpan.SPAN_TYPE_DEFAULT, node);
            }
        }
        public int setAddresses(String penddingText) {
            // TODO:check...
            disableCompletion();
            mAdt.silentClearAll();
            mExpanedSize = 0;
            mHiddenSize = 0;
            // 1. add to adt first 
            int count=mAdt.append(penddingText, mOptions.Separator); 
            // 2. attach AddressSpan
            if(count > 0) {
                //mTextBuffer.clear();
                silentClear();
                AddressNode[] addrs = mAdt.getAllNodes();
                
                //~~~
                // TODO(zhoujb): should refactory controller, move it in 
                if(!isButtonMode()) {
                    mTextBuffer.append(mAdt.joinWith(mOptions.Separator));
                    // TODO(zhoujb): need enableCompletion here ??? 
                    enableCompletion();
                    return count;
                }
                  
                //~~~ 
                for(int i=0; i<count; ++i) {       
                    addNewSpan(addrs[i]);
//                    String data = addrs[i].getData();
//                    if(data != null && data.contains("#")) {
//                        addrs[i].mSpan    = new AddressSpan(data,mOptions, true);
//                    }else {
//                        addrs[i].mSpan    = new AddressSpan(data,mOptions, false);
//                    }
                    addrs[i].mSpanned = true;

                    //mTextBuffer.append(PLACE_HOLDER);
                    silentAppend(PLACE_HOLDER);
                    mExpanedSize++;
                    mTextBuffer.setSpan(addrs[i].mSpan,
                                        L(i),
                                        L(i)+PLACE_HOLDER_LEN,
                                        ADDRESS_PAD_SPAN_TYPE);          
                    if(mExpanedSize > mOptions.ExpandAt) {                      
                        mExpandedEnd = L(i);
                        mHiddenSize=count-i;
                        addrs[i].mSpan.setLabel(mOptions.MoreHint + mHiddenSize);
                        addrs[i].asExpander();
                        break;
                    }
                }
            }
            if(mAdt.size() == 0) { 
            	isAlreadyEnter = true;
            }
            	
            enableCompletion();
            return count;              
        }      
        
        //XXX(zhoujb): don't open this method 
        private int appendAddresses(String pendingText) {
            if(TextUtils.isEmpty(pendingText)) {
                return 0;
            }
            disableCompletion();
            if(mAdt.size() == 0) {
                return setAddresses(pendingText);
            }
            AddressNode lastNode = mAdt.getLast();
            int lastSpan = mTextBuffer.toString().lastIndexOf(PLACE_HOLDER);
            mTextBuffer.delete(lastSpan,mTextBuffer.length());
            int count = mAdt.append(pendingText,mOptions.Separator);
            if(count > 0) {
                // TODO(zhoujb): finish this 
                // if(!lastNode.mSpanned || lastNode.isExpander()) {
                    
                // } else {

                // }
            }
            enableCompletion();
            return count;
        }

        public  int insertAddresses(int index,String penddingText) {
            if(TextUtils.isEmpty(penddingText) || index > mAdt.size() || index < 0) {
                return 0;
            }
            disableCompletion();
            // 1. insert to adt first 
            int count = mAdt.insert(index,penddingText,mOptions.Separator);
            
            //D("AFTER INSEERT: " + mAdt);
            if(count > 0) {                
                AddressNode node = null;
                int i = count;
                ListIterator<AddressNode> li = mAdt.listIterator(index);
                while(li.hasNext() && i>0) {
                    node = li.next();
                    //D("INS: " + li.previousIndex() + " > " + node);
                    attachAddress(li.previousIndex(), node);
                    --i;
                }
            }
            enableCompletion();
            return count;
        }   
        
        // return n place holder
        private String PLACE_HOLDER_N(int number) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<number; ++i) {
                sb.append(PLACE_HOLDER);
            }
            return sb.toString();
        }

        /**
         * @param index
         * @param text 
         */
        public void insertText(int index, String text) {
            int insertAt = 0;
            if(index == 0) {
                if(mTextBuffer.toString().startsWith(PLACE_HOLDER)) {
                    mTextBuffer.insert(0, text);
                    return;
                } else {
                    insertAt = mTextBuffer.toString().indexOf(PLACE_HOLDER,0);
                    mTextBuffer.insert(insertAt+1, text);
                    return;
                }
            }
            
            for(int i=0; i<index; ++i) {               
                insertAt = mTextBuffer.toString().indexOf(PLACE_HOLDER,insertAt);
                insertAt++;
            }
            mTextBuffer.insert(insertAt,text);
        }

        /**
         * @hide 
         * append addresses, since this function do not expand addresses, it may case 
         * low performance
         */
        public int _appendAddresses(String pendingText) {
            if(TextUtils.isEmpty(pendingText)) {
                return 0;
            }
            return (mAdt.size() == 0) ? setAddresses(pendingText) : insertAddresses(mAdt.size(),pendingText);
        }
        
        /**
         * @hide
         * just for debug use 
         */
        private String _getAddress(int num) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<num-1; ++i) {
                sb.append(""+i+"@gmailxcom").append(",");
            }
            sb.append("end...");
            return sb.toString();
        }

        /**
         * Enter pedding text 
         */
        public  boolean tryEnterAddress() {
        	
        	if(isButtonMode()){
        		if(hasSingleLineHint()) {
                    return true;
                }
        	}
            
            if(mTextBuffer.length() == 0) {
                if(!mAdt.isEmpty()) {
                    mAdt.silentClearAll();
                }
                return false;
            }
            //~~~
            if(!isButtonMode()) {
                //TODO(zhoujb): should refactory controller, move it in 
                setAddresses(mTextBuffer.toString());
                return true;
            }
            //~~~
            if(mAdt.size() == 0 && isAlreadyEnter) {
            	return true;
            } else if(mAdt.size() == 0) {
                // 1. input all buffer 
                setAddresses(mTextBuffer.toString());
                //setAddresses(_getAddress(50));
            } else {
                PendingText[] pendingTexts = getPendingText();
                int offset = 0;
                for(PendingText p : pendingTexts) {         
                    if(!p.isEmpty()) {
                        // be careful, must record offset position after each insertion
                        int i = p.inc(offset);
                        silentDelete(i,i+p.length());
                        offset += insertAddresses(i, p.getText());
                    }
                }
            }
            D("ADT: " +mAdt);
            setStatus(STATUS_WAITTING_INPUT);
            return true;
        }
        
        /**
         * return count*text
         */
        private String fill(String text, int count) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<count; ++i) {
                sb.append(text);
            }
            return sb.toString();
        }

        // This function coupled with showSingleLineHint, please decouple
        public void forceUpdateSingleLine() {
            if(!isFocused()) {
                mSingleLineHint = false;
                showSingleLineHint();
                mSingleLineHint = true;
            }
        }

        /**
         * Show single line hint in text buffer
         */
        public void showSingleLineHint() {
            // XXX(zhoujb): maybe some one can simplify this logic, I'd like not to see it any more ...
            D("mAdt.size()=" + mAdt.size() + " hasSingleLineHint()=" + hasSingleLineHint() + " isFocused()="+isFocused() + " F: " + mPortraitMaxWidth);
            if(mAdt.size() <= 0 || hasSingleLineHint() || isFocused()) {
                return;
            }
            disableCompletion();

            String[] addrs       = getDecoratedAddressesArray(0,Math.min(mOptions.ExpandAt, getSize()), mOptions.Connector);
            String hint          = ""; 
            String numHolder     = ""+getCount();
            numHolder = fill("0", numHolder.length()+1);
            float moreTextWidth  = measureText("... "+mOptions.MoreHint+numHolder);
            int maxTextWidth     = getWidth() - getPaddingLeft() - getPaddingRight()-LEFT_PADDING; 
            if(mFixedMaxWidth) {
                maxTextWidth = mPortraitMaxWidth; 
                mFixedMaxWidth = false;
            }

            float width          = 0F;
            boolean buildHintDone=false;    
            D("MAX WIDTH: " + maxTextWidth);
            if(maxTextWidth <= 0) {
                // if maxTextWidth less than 0, it means the widget do not be layouted yet, so
                // we should delay to display it after layout
                D("DELAY SHOW SINGLE LINE HINT...");
                mDisplaySingleLineHintAfterLayout = true;
                mForceShowSingleLine = true;
                enableCompletion();
                return;
            }
            
            StringBuilder sbAddrs = new StringBuilder();                     
            for(String s : addrs) {
                sbAddrs.append(s);
            }
            width = measureText(sbAddrs.toString());
            if(width < maxTextWidth) {
                hint = sbAddrs.toString();
            } else {
                // get hint text 
                for(int i=0; i<addrs.length; ++i) {
                    String guess = "";
                    for(int j=0; j<addrs[i].length(); ++j) {
                        guess = addrs[i].substring(0, j+1);
                        float addrsWidth = measureText(hint+guess);
                        float offset = measureText(addrs[i].substring(j,Math.min(j+1, addrs[i].length())));
                        if(addrsWidth + moreTextWidth + offset > maxTextWidth) {
                            hint += guess;
                            int hidden = getCount() - i - 1;
                            if(hidden >= 1) {
                                hint += "... "+mOptions.MoreHint+hidden;
                            } else { 
                                hint = TextUtils.ellipsize(sbAddrs.toString(),getPaint(), maxTextWidth, TruncateAt.END).toString();
                            }
                            buildHintDone = true;
                            break;
                        }
                    }
                    if(buildHintDone) {
                        break;
                    } else {
                        hint += guess;
                    }
                }
            }

            if(mOptions.SingleLineHintStyle == SINGLE_LINE_HINT_STYLE_BUTTON_LIKE) {
                if(getLineCount() > 1 || mForceShowSingleLine) {
                    //mTextBuffer.clear();
                    //mTextBuffer.append(PLACE_HOLDER);
                    silentClear();
                    silentAppend(PLACE_HOLDER);
                    AddressNode node = new AddressNode(hint);
                    addNewSpan(node);
                    mTextBuffer.setSpan(node.mSpan,
                                        0,
                                        mTextBuffer.length(),
                                        ADDRESS_PAD_SPAN_TYPE);
                    mForceShowSingleLine = false;
                }
            } else {
                //mTextBuffer.clear();
                //mTextBuffer.append(hint);
                silentClear();
                silentAppend(hint);
            }
            
            // reset some attributes
            mExpanedSize = 0;
            mHiddenSize  = 0;
            mSingleLineHint = true;
            enableCompletion();
        }
        
        /**
         * Get addresses number 
         */
        public int getCount() {
            return mAdt.size();
        }

        /**
         * Meature spedify string 
         * @return pixel length of spedify string 
         */
        private float measureText(String s) {
            Paint  p = getPaint();
            return (p != null) ? p.measureText(s) : 0F;
        } 


        /**
         * If the address node do not spanned attach span object 
         */
        public void showAddresses() {
            int count = mAdt.size();
            if(count > 0 && hasSingleLineHint()) {    
                //mTextBuffer.clear();
                silentClear();
                AddressNode[] addrs = mAdt.getAllNodes();
                for(int i=0; i<count; ++i) {      
                    if(addrs[i].mSpan == null) {
                        addNewSpan(addrs[i]);
//                        addrs[i].mSpan = new AddressSpan(addrs[i].getData(),mOptions);
                    } 
                    addrs[i].mSpanned = true;
                    if(addrs[i].isExpander()) {
                        addrs[i].mSpan.clearLabel();
                        addrs[i].mIsExpander=false;
                    }

                    //mTextBuffer.append(PLACE_HOLDER);
                    silentAppend(PLACE_HOLDER);
                    mExpanedSize++;
                    mTextBuffer.setSpan(addrs[i].mSpan,
                                         L(i),
                                         L(i)+PLACE_HOLDER_LEN,
                                         ADDRESS_PAD_SPAN_TYPE);                   
                    if(mExpanedSize > mOptions.ExpandAt) {                      
                        mExpandedEnd = L(i);
                        mHiddenSize=count-i;
                        addrs[i].mSpan.setLabel(mOptions.MoreHint+mHiddenSize);
                        addrs[i].asExpander();
                        for(int j=i+1; j<count; ++j) {
                            addrs[j].mSpanned = false;
                            addrs[j].mIsExpander = false;
                        }
                        break;
                    }
                }
            }
            mSingleLineHint = false;
        }
        
        private void silentAppend(String text) {
            mAddressWatcherDisableOnInput = true;
            mTextBuffer.append(text);
            mAddressWatcherDisableOnInput = false;
        }

        private void silentInsert(int s, String text) {
            mAddressWatcherDisableOnInput = true;
            mTextBuffer.insert(s,text);
            mAddressWatcherDisableOnInput = false;
        }

        private void silentClear() {
            mAddressWatcherDisableOnInput = true;
            mTextBuffer.clear();
            mAddressWatcherDisableOnInput = false;
        }

        private void silentDelete(int s, int l) {
            mAddressWatcherDisableOnInput = true;
            mTextBuffer.delete(s,l);
            mAddressWatcherDisableOnInput = false;
        }
        
        /**
         * Fire on selection event on spedify node 
         * @param index spedify index 
         */
        public void fireOnSelectionEvent(AddressNode[] nodes, int index) {
            if(nodes[index].isExpander()) {
                expand(index, mOptions.ExpandStep);
            } else {
                mCurrendSelectedAddress  = index;
                if(mNeedClick) {
                	mController.deleteCurrentAddress();
                }
                
                //TODO not show contextMenu
//                readyToPopup();           
            }    
        
        }
        
        /**
         * Clear selected address 
         */
        public void clearCurrentSelection() {
            if(mCurrendSelectedAddress != -1) {
                AddressNode node = mAdt.get(mCurrendSelectedAddress);
                if(node != null && node.mSpan != null) {
                    node.mSpan.setSelected(false);
                }
            }
            resetPopup();
        }
 
        public void readyToPopup() {
            mReadyToPopup = true;
        }
        
        public void resetPopup() {
            mReadyToPopup = false;
        }

        public boolean isReadyToPopup() {
            return mReadyToPopup;
        }

        private int getSelectedAddressIndex() {
            // D("SELECTING: " + mCurrendSelectedAddress + " -- " + mAdt.get(mCurrendSelectedAddress));
            return mCurrendSelectedAddress;
        }
        

        //------------------------------------------------
        // +------>
        // |      x
        // |
        // V y
        //------------------------------------------------
        private boolean onActionDown(float x, float y) {
            if(mAdt.isEmpty() || mSingleLineHint) {
                return true;
            }
//            AddressNode[] all = mAdt.getAllNodes();
//            if(all == null) {
//                return true;
//            }
//            AddressSpan   as  = null;
//            
//            // hit test
//            mCurrendSelectedAddress = -1;
//            for(int i=0; i<all.length; ++i) { //<<<<<
//                as = all[i].mSpan; 
//                if(as != null) {
//                    as.setSelected(as.hitTest(x,y));
//                    if(as.isSelected()) {
//                        fireOnSelectionEvent(all,i);
//                    } else {
//
//                    }
//                }
//            }
            return true;
        }

        private boolean onActionUp(float x, float y) {
            if(isReadyToPopup()) {
                mSelf.showContextMenu();
                resetPopup();
            }
            
            AddressNode[] all = mAdt.getAllNodes();
            if(all == null) {
            	return true;
            }
            AddressSpan   as  = null;
            // hit test
            mCurrendSelectedAddress = -1;
            for(int i=0; i<all.length; ++i) { //<<<<<
            	as = all[i].mSpan; 
            	if(as != null) {
            		as.setSelected(as.hitTest(x,y));
            		if(as.isSelected()) {
            			fireOnSelectionEvent(all,i);
            		} else {
            			
            		}
            	}
            }
            return true;
        }

        public void beforeDeletionKey() {

        }
        
        public void afterDeletionKey() {
            rmAddresses();
        }
        
        public int expandAll() {
            //TODO: support this 
            return 0;
        }
        
        /**
         * @return if showing single line hint return true otherwise false
         */
        public boolean hasSingleLineHint() {
            return mSingleLineHint;
        }
        
        /**
         * install a existed node means make the node shown in buffer 
         * @param index
         * @param node address node 
         */
        private boolean attachAddress(int index, AddressNode node) {
            if(node==null || node.mSpanned) {
                return false;
            }
            //mTextBuffer.insert(index, PLACE_HOLDER);
            silentInsert(index, PLACE_HOLDER);
            addNewSpan(node);
//            node.mSpan = new AddressSpan(node.getData(),mOptions);
            node.mSpanned = true;
            mTextBuffer.setSpan(node.mSpan,
                                L(index),
                                L(index)+PLACE_HOLDER_LEN,
                                ADDRESS_PAD_SPAN_TYPE); 
            return true;
        }
        
        /**
         * Attach span object to spedify node as expander 
         * @param index 
         * @param node expander node 
         */
        private boolean attachExpander(int index, AddressNode node) {
            if(node==null || node.mSpanned) {
                return false;
            }

            mTextBuffer.insert(index, PLACE_HOLDER);
            addNewSpan(node);
//            node.mSpan = new AddressSpan(node.getData(),mOptions);
            node.asExpander();
            node.mSpan.setLabel(mOptions.MoreHint + mHiddenSize);
            mTextBuffer.setSpan(node.mSpan,
                                L(index),
                                L(index)+PLACE_HOLDER_LEN,
                                ADDRESS_PAD_SPAN_TYPE);            
            return true;
        }

        /**
         * Try to expand the spedify node, if it is not expander, nothing will happan
         * @param index spedify node 
         * @param step expand how many address node 
         */
        public int expand(final int index, int step) {
            //assert step > 1
            AddressNode next = null;
            // may existed pending text when do expand, so fisrt we need calc 
            // offset cased by them 
            int offset = mTextBuffer.getSpanStart(mAdt.get(index).mSpan) - index;            
            // D("----OFFSET----: " + offset);
            ListIterator<AddressNode> li = mAdt.listIterator(index); 
            // handle expander first 
            if(li.hasNext()) {
                next = li.next();
                next.mSpan.clearLabel();
                next.mSpan.setDecoratedText(next.getData());
                next.mIsExpander = false;
                --mHiddenSize;
            }

            int count = 1;
            while(li.hasNext() && count < step) {
                next = li.next();
                //D("next: " + next + " at: " + li.nextIndex());
                if(next == null) {
                    return count;
                } else {
                    if(!next.mSpanned) {
                        attachAddress(li.previousIndex() + offset, next);
                        --mHiddenSize;
                    } else {
                        return count;
                    }
                }
                ++count;
            }
            //D("mHiddenSize : " + mHiddenSize);
            if(li.hasNext()) {
                next = li.next();
                //D("NEXT EXPANDer : " + next);
                if(mHiddenSize > 1) {
                    attachExpander(li.previousIndex() + offset, next);
                } else {
                    attachAddress(li.previousIndex() + offset, next);
                    mHiddenSize = 0;
                }
            }
            return 0;
        }
        
        /**
         * Edit current selected address
         */
        public void editCurrentAddress() {
            edit(mCurrendSelectedAddress);
            mCurrendSelectedAddress = -1;
        }

        /**
         * Delete current selected address
         */
        public void deleteCurrentAddress() {
            String rmstr = rm(mCurrendSelectedAddress);
//            Log.d(TAG, "deleteCurrentAddress: " + rmstr);
            refreshItemUI(rmstr);
            mCurrendSelectedAddress = -1;
        }

        /**
         * Insert text after the position of cursor
         */
        public void  insertToBuffer(CharSequence text) {
            int cursor = mSelf.getSelectionStart();
            mTextBuffer.insert(cursor,text);
        }

        /**
         * Get current selected address
         */
        public String getCurrentAddress() {
            AddressNode node = getCurrentAddressNode();
            return (node != null) ? node.getData() : "";
        }
        /**
         * @hide
         */
        public String getCurrentEscapedAddress() {
            AddressNode node = getCurrentAddressNode();
            return (node != null) ? node.getEscapedData() : "";
        }

        /**
         * Get current selected address node 
         */
        public AddressNode getCurrentAddressNode() {
            return mCurrendSelectedAddress >= 0 ? mAdt.get(mCurrendSelectedAddress) : null;
        }
        
        /**
         * <p>change address button to text</p>
         */
        public void edit(int index) {
            if(index >= mAdt.size() && index < 0) {
                return;
            }
            AddressWatcher tmp = mAddressWatcher;
            mAddressWatcher = null;

            AddressNode rm = mAdt._rm(index); 
            if((rm == null)||(rm.mSpan == null)) {
                mAddressWatcher = tmp;
                return;
            }
            int s = rm.mSpan.getSpanStart();
            int e = rm.mSpan.getSpanEnd();
            String ins = rm.getData();

            // TODO(zhoujb): support edit expander
            if(rm.isExpander()) {
                mAddressWatcher = tmp;
                return;
            }

            if(isButtonMode()) {
                if(hasRightPendingText(index)) {
                    ins += mOptions.Separator;
                }
                if(hasLeftPendingText(index)){
                    ins  = mOptions.Separator + ins;
                }
                mTextBuffer.replace(s,e,ins);          
                mTextBuffer.removeSpan(rm.mSpan);
            } else {
            
            }
            mAddressWatcher = tmp;
        }

        public boolean hasRightPendingText(int index) {
            //TODO(zhoujb): add it
            return true;
        }
        
        public boolean hasLeftPendingText(int index) {
            //TODO(zhoujb): add it
            return false;
        }
        
        private int length() {
           return mTextBuffer.length();
        }
        
        /**
         * Get addresses count 
         */
        public int getSize() {
            return mAdt.size();
        }

        /**
         * Remove spedify address
         * @param index address index 
         */
        public String rm(int index) {
            if(index >= mAdt.size() && index < 0) {
                return null;
            }
            AddressNode rm = mAdt.rm(index);        
            
            if(rm == null){
                return null;
            }
            //delete form buffer then remove spanned object 
            if(isButtonMode()) {
                mTextBuffer.delete(rm.mSpan.getSpanStart(), rm.mSpan.getSpanEnd());
                mTextBuffer.removeSpan(rm.mSpan);
            } else {
                String delta = mTextBuffer.toString();
                if(!TextUtils.isEmpty(delta)) {
                    delta = delta.replace(rm.getData(),"");
                    D("DELTA: " + delta + " remove: " + rm.getData());
                    //mTextBuffer.clear();
                    //mTextBuffer.append(delta);
                    silentClear();
                    silentAppend(delta);
                }
            }
            return rm.getData();
        }

        /**
         * Update adt, remove useless data
         */
        public void rmAddresses() {
            if(mAdt.size() == 0)
                return;            
            AddressSpan [] spanned = getAllAddressSpan();
            if(spanned.length < mAdt.size()) {
                AddressNode[] nodes = mAdt.getAllNodes();
                for(AddressNode node : nodes) {
                    int flags = mTextBuffer.getSpanFlags(node.mSpan);                 
                    if(node.isExpander()) {
                        if(flags == 0) {
                            // Oops, the expander be killed, so remove all nodes after it
                            mAdt.rmAll(node);
                            mHiddenSize = 0;
                        } else {
                            // Good luck, expander still exist , break out 
                            break;
                        }
                    } else {                    
                        if(flags == 0) {
                            mAdt.rm(node);
                        }
                    }
                }
            }
        }
    
        private AddressSpan[] getExpanedAddressSpan() {
            return getAddressSpans(0, mExpandedEnd);
        }

        private AddressSpan[] getAddressSpans(int s , int e) {
            return mTextBuffer.getSpans(e, s, AddressSpan.class);
        }

        private AddressSpan[] getAllAddressSpan() {
            return mTextBuffer.getSpans(0, mTextBuffer.length(), AddressSpan.class);
        }

        /**
         * Enter pending text and get all addresses 
         * @return all addresses joined with separator
         */
        public String getAddresses() {
            // accept pedding text first
            if(isPlainMode()) {
                resetTextBuffer();
                return mTextBuffer.toString();
            }
            tryEnterAddress();
            return mAdt.joinWith(mOptions.Separator);
        }
        
        /**
         * Get all decorated addresses joined by separator
         * Notice: since it has to call OnDecorateAddressListener for each 
         * address node, it will case low performance with time consumed OnDecorateAddressListener.
         * @return decorated addresses joined with separator
         */
        public String getDecoratedAddresses() {
            return getDecoratedAddresses(0, mAdt.size());
        }
        

        /**
         * Get spedify range decorated addresses
         * @param start start index 
         * @param end end index
         * @return decorated addresses joined with separator
         */
        public String getDecoratedAddresses(int start, int end) {
            AddressNode[] nodes = mAdt.getNodes(start, end);
            StringBuilder sb = new StringBuilder();
            if(nodes != null && nodes.length > 0) {
                for(int i=0; i<nodes.length - 1; ++i) {
                    AddressNode n = nodes[i];
                    sb.append(n.getDecoratedData()).append(mOptions.Separator);
                }
                // last one 
                sb.append(nodes[nodes.length - 1].getDecoratedData());
            }
            return sb.toString();  
        }

        /**
         * Get spedify range decorated addresses
         * @param start start index 
         * @param end end index
         * @return decorated addresses string array 
         */
        public String[] getDecoratedAddressesArray(int start, int end, String suffix) {
            AddressNode[] nodes = mAdt.getNodes(start, end);
            ArrayList<String> sa = new ArrayList<String>();
            if(nodes != null) {
                for(int i=0; i<nodes.length-1 ; ++i) {
                    AddressNode n = nodes[i];
                    sa.add(n.getDecoratedData()+suffix);
                }
                AddressNode n = nodes[nodes.length-1];
                sa.add(n.getDecoratedData());
            }
            return sa.toArray(new String[sa.size()]);
        }

        /**
         * Get all address as array 
         * @return all addresses String array
         */
        public String[] getAddressesArray() {
            //B128: support landscape mode here
            if(isPlainMode()) {
                resetTextBuffer();
                return mTextBuffer.toString().split(",");
            }
            // accept pending text first
            tryEnterAddress();
            return mAdt.toStringArray();
        }
        
        /**
         * @hide 
         * remove all addresses and clear text buffer
         */
        public void clearAllAddresses() {
            mTextBuffer.removeSpan(AddressSpan.class);
            mTextBuffer.clear();
            mAdt.clearAll();
        }
       
        /**
         * TODO(zhoujb): currently we do not 
         * use any status information, but in the feature we 
         * need it to simplify the control logic
         */
        public int setStatus(int status) {
            int old = mStatus;
            mStatus = status;
            return old;
        }
        
        private boolean onTouchEvent(MotionEvent e) {

            float x = e.getX() - getPaddingLeft()
                + mSelf.computeHorizontalScrollOffset();
            float y = e.getY() - getPaddingTop()
                +  mSelf.computeVerticalScrollOffset();

            switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                onActionUp(x,y);
                break;
            case MotionEvent.ACTION_DOWN:
                onActionDown(x,y);
                break;
            default:
                return true;
            }
            return true;
        }
        
        /**
         * @hide
         * remove the decorator in the mTextBuffer
         */
        public void resetTextBuffer(){
        	if(mOptions.DecoratorType == DECORATOR_TYPE_PHONE_MAIL){
        		String[] decoratedAddrs = mTextBuffer.toString().split(mOptions.Separator);
                StringBuilder sb = new StringBuilder();
                RemovePhoneNumberDecorater rmDecorater = new RemovePhoneNumberDecorater();
                if(decoratedAddrs.length > 0){
                    String temp;
                    int type;
                    for(int i = 0; i<decoratedAddrs.length;i++){
//                        if(Mms.isEmailAddress(decoratedAddrs[i])){
//                            sb.append(decoratedAddrs[i]);
//                            sb.append(mOptions.Separator);
//                            continue;
//                        }
                        temp = rmDecorater.onDecorate(decoratedAddrs[i]);
                        if(temp ==null){
                            continue;
                        }
//                        if(isPhoneNumber(temp)){
//                            sb.append(temp);
//                        }else{
//                            sb.append(decoratedAddrs[i]);
//                        }
                        sb.append(temp);
                        sb.append(mOptions.Separator);
                    }
                }
                rmDecorater = null;
//                mTextBuffer.clear();
                silentClear();
//                mTextBuffer.append(sb.toString());
                silentAppend(sb.toString());
        	}else{
        		D("resetTextBuffer,Nothing to do here!");
        	}
        }
        
        /**
         * @hide
         * dump adt to text buffer
         */
        public void restoreTextBuffer() {
        	if(mOptions.DecoratorType == DECORATOR_TYPE_PHONE_MAIL){
        		//String text = mAdt.joinWith(mOptions.Separator);
                String[] addrs = mAdt.toStringArray(); 
                StringBuilder sb = new StringBuilder();
                PhoneNumberEmailDecorater nDecorater = new PhoneNumberEmailDecorater();
                if(addrs.length > 0){
                    String temp;
                    int type;
                    for(int i = 0;i<addrs.length;i++){
                        temp = addrs[i];
                        if (LOCAL_LOGV) {
                            Log.d("restoreTextBuffer","===================================temp="+temp);
                        }
                        //B128: to handle zy<13581952109> in landscapde mode
                        if(temp.contains("<")&&(temp.contains(">"))&&isPlainMode()){
                            sb.append(addrs[i]);
                            sb.append(mOptions.Separator);
                            continue;
                        }
//                        if(isPhoneNumber(temp)) {
//                            type = ADDRESS_TYPE_PHONE;
//                        } else {
//                            type = ADDRESS_TYPE_EMAIL;
//                        }
//                        
//                        switch(type){
//                        case ADDRESS_TYPE_PHONE:
//                            if(isPlainMode()){
//                                sb.append(nDecorater.onDecorate(temp));
//                            }
//                            break;
//                        case ADDRESS_TYPE_EMAIL:
//                                sb.append(addrs[i]);
//                            break;
//                        }
                        if(isPlainMode()){
                            sb.append(nDecorater.onDecorate(temp));
                        }
                        sb.append(mOptions.Separator);
                    }
                }
                nDecorater = null;
                silentClear();
                silentAppend(sb.toString());
        	}else{
        		String text = mAdt.joinWith(mOptions.Separator);
                mTextBuffer.clear();
                mTextBuffer.append(text);
        	}
        }
        

        private  int mPortraitMaxWidth  = 0;
        private  boolean mFixedMaxWidth = false;
        /**
         * @hide
         */
        public void onConfigurationChanged(Configuration newConfig) {
        	if (LOCAL_LOGV){
        		Log.e("xxx", "newConfig");
        	}
            //super.onConfigurationChanged(newConfig);
            if (mOrientation != newConfig.orientation) {
                mOrientation = newConfig.orientation;

                switch (newConfig.orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    setWorkingMode(WORKING_MODE_BUTTON_MODE);
                    mAdt.silentClearAll();
                    resetTextBuffer();
                    if (hasSingleLineHint())
                        mSingleLineHint = false;
                    mTextBuffer.setSpan(new LinePaddingSpan(), 0, 0, 0);
                    tryEnterAddress();
                    /*
                     * NOTICE: onConfigurationChanged be celled after layout, so
                     * at this time the maxWidth of width is maxWidth of
                     * landscape mode this may misslead show single hint, so we
                     * force adjust it here.
                     */
                    if (!isFocused()) {
                        mForceShowSingleLine = true;
                        mFixedMaxWidth = true;
                    }

                    showSingleLineHint();
                    if (mAddressWatcher != null) {
                        mAddressWatcher.onInput(getAddresses());
                    }
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    /*
                     * NOTICE: recode the maxWidth of portrait mode, see comment
                     * above
                     */
                    mPortraitMaxWidth = getWidth();
                    tryEnterAddress();
                    setWorkingMode(WORKING_MODE_PLAIN_MODE);
                    restoreTextBuffer();
                    break;
                default:
                    /* NOP */
                }
            }
        }

       /* public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // TODO(zhoujb): should be refactory
            // from oms1.5 the activity don't be destoried when rotate
            // in oms1.5 we can't get ORIENTATION change when start camera , 
            // disable this feature for working around
            if(!mOptions.DisableOritationChange) {
                int orientation = getResources().getConfiguration().orientation;
                int old;
                if(!mOptions.ReadOnly) {
                    switch(orientation) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        //XXX(zhoujb): before restoreTextBuffer, we need enter all pedding text first
                        resetTextBuffer();
                    	tryEnterAddress();   
                        if(mAddressWatcher != null) {
                            mAddressWatcher.onInput(getAddresses());
                        }
                        old = setWorkingMode(WORKING_MODE_PLAIN_MODE);
                        if(old == WORKING_MODE_BUTTON_MODE) {
                            D(" portrait --> landscape ");
                                                          
                            mLayoutParams.height = _calcHeight();
                            restoreTextBuffer();                          
                        }  
                        
                        return;
                    case Configuration.ORIENTATION_PORTRAIT: 
                    
                        old = mSelf.setWorkingMode(WORKING_MODE_BUTTON_MODE);
                        if(old == WORKING_MODE_PLAIN_MODE) {
                            D("landscape --> portrait");
                            mAdt.silentClearAll();
                            resetTextBuffer();
                            if(hasSingleLineHint())
                                mSingleLineHint = false;                          
                            tryEnterAddress();
                            showSingleLineHint();
                        }
                        if(mLayoutParams.height !=  LayoutParams.WRAP_CONTENT) {
                            mLayoutParams.height = LayoutParams.WRAP_CONTENT;
                        }
                        break;
                    case Configuration.ORIENTATION_SQUARE:
                        break;
                    }
                } else {
                    if(hasSingleLineHint())
                        mSingleLineHint = false;
                    showSingleLineHint();
                }
            } 
            // ~~~~

            
            if(isFocused()) {
                mDisplaySingleLineHintAfterLayout = false;
                mForceShowSingleLine = false;
                return;
            }

            if(mDisplaySingleLineHintAfterLayout) {
                forceUpdateSingleLine();
                mDisplaySingleLineHintAfterLayout = false;
            }
        }*/

        /**
         * Check input context and return the offset of buffer as completion start point 
         * @param buffer text buffer
         * @param cursor cursor position
         * @return 
         */
        private int getCompletionStart(String buffer, int cursor) {
            // lookup prev characters 
            // 1. try find prev separator position from cursor
            // 2. try find prev placeholder position from cursor
            // 3. return the max position
            int separator   = buffer.lastIndexOf(mOptions.Separator, cursor-1);
            int escapedseparator = buffer.lastIndexOf(mOptions.getEscapedSeparator(), cursor-1);
            // escapedseparator test 
            if(separator - escapedseparator + 1 == mOptions.getEscapedSeparator().length()) {
                separator = -1;
            }
            int placeholder = buffer.lastIndexOf(PLACE_HOLDER, cursor-1);
            return Math.max(separator, placeholder);
        }

        private String dumpText() {
            if(isPlainMode()) {
                return  mTextBuffer.toString();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(mAdt.joinWith(mOptions.Separator));

            PendingText[] pt = getPendingText();
            if(pt != null) {
                if(pt.length > 0) {
                    sb.append(mOptions.Separator);
                    for(int i=0; i<pt.length-1; ++i) {
                        sb.append(pt[i].getText());
                    }
                    sb.append(pt[pt.length-1].getText());
                }
            }
            //D("----- : " + sb.toString());
            return sb.toString();
        }

        private void onTextChanged(CharSequence text, int start, int before, int after) {
            // D("ON TEXT CHANGED: AFTER  -- " + text.toString() + " S: " + start + " B:" + before + " A:" +after);
            // XXX(zhoujb): Oops, the IME will use "before" for recording text CHANGED           
            int cursor    = getSelectionStart();
            int compStart = getCompletionStart(text.toString(), cursor);
            // address watcher callback
            //B128: in textplain mode,send mms to mail addr ,black screen ,why
            if(mAddressWatcher != null) {
                
                if(!mAddressWatcherDisableOnInput) {
                    mAddressWatcher.onInput(dumpText());
                }
            }
            
            if(isEnabledCompletion()) {
                if(cursor - compStart - 1 >= mOptions.Threshold) {
//                	setAdapter(new RecipientsAdapter(getContext()));
                    performCompletion(compStart + 1, cursor);
                } else {
                    mSelf.dismissDropDown();
                    mEnoughToFilter = false;
                }
            } else {
                mEnoughToFilter = false;
            }
        }

        public void enableCompletion() {
        	Log.d(TAG, "enableCompletion: ");
        	mEnableCompletion = true;
        	if(mCompletion != null) { 
        		mCompletion.onInputCompletion();
        	}
        }

        public void disableCompletion() {
            mEnableCompletion = false;
        }

        public boolean isEnabledCompletion() {
            return mEnableCompletion;
        }

        public boolean enoughToFilter() {
            return mEnoughToFilter;
        }

        public boolean replaceText(CharSequence text){
            int cursor = getSelectionEnd();
            int start = getCompletionStart(mTextBuffer.toString(), cursor);
            final String target  = mTextBuffer.subSequence(start+1, cursor).toString().trim();
            if(text == null)
            	text = "";
            mTextBuffer.replace(start+1, cursor, text);
            tryEnterAddress();
            return true;
        }

        protected void performFiltering(CharSequence text, int start, int end, int keyCode) {
            mSelf.getFilter().filter(text.subSequence(start, end), mSelf);
        }
        
        /**
         * @param text content of  text buffer 
         * @param keyCode key code 
         */
        public void performFiltering(CharSequence text, int keyCode) {
            if (enoughToFilter()) {
                int cursor = getSelectionEnd();
                int start = getCompletionStart(mTextBuffer.toString(), cursor);
                performFiltering(text, start+1, cursor, keyCode);
            } else {
                dismissDropDown();
                Filter f = getFilter();
                if (f != null) {
                    f.filter(null);
                }
            }
        }
        
        /**
         * if enough to completion, set the flag (See: mEnoughToFilter)
         * @param start start offset of text buffer 
         * @param end   end offset of text buffer 
         */
        private void performCompletion(int start, int end) {
            //TODO(zhoujb): optimize these code ...
//        	Log.d(TAG, "AAAAAAAAAAAAAA " + length() + " " +  mTextBuffer.length() + " start: " + start + " end: " + end);
            if(length() <= 0 || end > mTextBuffer.length()) {
                return ;
            }
            final String target  = mTextBuffer.subSequence(start, end).toString().trim();
            if(target.length() < mOptions.Threshold) {
                mEnoughToFilter = false;
                return;
            }

            mEnoughToFilter = true;
        }


        //------------------------------------------------

        
        private boolean mEnoughToFilter            = false;
        private int STATUS_WAITTING_INPUT          = 1;
        private int mExpandedEnd                   = 0;
        private int mExpanedSize                   = 0;
        private int mHiddenSize                    = 0;
        private int mCurrendSelectedAddress        = -1;
        private boolean mDisplaySingleLineHintAfterLayout = false;
        private boolean mReadyToPopup              = false;
        private int mSpannedAddress                = 0;
        private int mStatus                        = STATUS_WAITTING_INPUT;    
        private int mLastPos                       = 0;
        private SpannableStringBuilder mTextBuffer = null;
        private AddressAdt             mAdt        = null;       
        private AddressPadMini         mSelf       = null; 
        private Options                mOptions    = null;
        private boolean mSingleLineHint            = false;
        private boolean mEnableCompletion          = true;
        private boolean mForceShowSingleLine       = false;
    }
    


    public class AddressNode {
        public static final int NODE_TYPE_NORMAL   = 0;
        public static final int NODE_TYPE_EXPANDER = 1;

        public AddressNode(String data) {
            setData(data);
        }

        public AddressSpan mSpan      = null;
        public String      mData      = null;
        public boolean     mSelected  = false;
        public boolean     mSpanned   = false;
        public int         mType      = NODE_TYPE_NORMAL; // XXX(zhoujb): reserved attribute
        public boolean     mIsExpander= false;

        //TODO(zhoujb): escape the separator
        public String getData() {
            return mData;
        }
        public void setData(String s) {
            mData = s.replace(DUMMY_SEPARATOR, mOptions.getEscapedSeparator());
        }
        /**
         * @hide
		 */
        public String getEscapedData() {
            return (mData != null) ? mData.replace(mOptions.getEscapedSeparator(), mOptions.Separator) : "";
        }

        public void asExpander() {
            mIsExpander = true;
            if(mSpan != null) {
            	mSpan.isExpand = true;
            }
        }

        public boolean isExpander() {
            return mIsExpander;
        }
        
        //XXX(zhoujb): should move decorator to the AddressNode ???
        public String getDecoratedData() {
            String text = "";
            // for un spanned node, call OnDecorateAddressListener
            if(mSpan == null) {
                if(mDecorator != null) {
                    CharSequence cs = mDecorator.onDecorate(mData);
                    if(!TextUtils.isEmpty(cs)) {
                        text = cs.toString();
                    }
                } else {
                    text = mData;
                } 
            } else {
                text = mSpan.getDecoratedText();
            }
            return text;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ")
                .append("DATA:" + mData + " | ")
                .append("SPAN:" + mSpan + " <"+mSpanned+ " expander="+mIsExpander+">")
                .append(" }");
            return sb.toString();
        }
    }

    /**
     * it mantains a address list and provide 
     * some access methods
     */
    public class AddressAdt {
        public AddressAdt(int size) {
            mData = new LinkedList<AddressNode>();
            mCache = new HashMap<String, Integer>(size);
        }
        
        private void  onAddNode(AddressNode node) {
            if(mAddressWatcher != null) {
                mAddressWatcher.onAccept(node.getData());
            }
        }
        
        private void onRemoveNode(AddressNode node) {
            if(mAddressWatcher != null) {
                mAddressWatcher.onRemove(node.getData());
            }
        }
        
        /**
         * Clear all 
         */
        public void clearAll() {
            // force call address watcher 
            for(int i=0; i<size(); ++i) {
                rm(i);
            }
            mData = null;
            mData = new LinkedList<AddressNode>();
            clearCache();
        }
        /**
         * @hide
         */
        public void silentClearAll() {
            // force call address watcher 
            for(int i=0; i<size(); ++i) {
                _rm(i);
            }
            mData = null;
            mData = new LinkedList<AddressNode>();       
            clearCache();
        }
        
        /**
         * Get list iterator by index 
         */
        ListIterator<AddressNode> listIterator(int index) {
            return mData.listIterator(index);
        } 
        
        /**
         * Get index of spedify AddressNode object 
         */
        public int indexOf(Object o) {
            return mData.indexOf(o);
        }
        
        /**
         * Append given node to the last 
         * @param node
         */
        public void addLast(AddressNode node) {
            onAddNode(node);
            mData.addLast(node);
        }

        /**
         * remove the last AddressNode object
         * @return the last AddressNode object 
         */
        public AddressNode rmLast() {
            AddressNode node =  mData.removeLast();
            onRemoveNode(node);
            rmFromCache(node);
            return node;
        }
        
        /**
         * remove spedify AddressNode object by index
         * @param index
         * @return the removed AddressNode object 
         */
        public AddressNode rm(int index) {
            //TODO(zhoujb): range checking ...
            if((index >= mData.size())||(index<0)){ 
                return null;
            }
            AddressNode node =  mData.remove(index);
            onRemoveNode(node);
            rmFromCache(node);
            return node;
        }
        
        /** same with rm() but not call AddressWatcher */
        public AddressNode _rm(int index) {
            //B128: add range checking
        	if((index >= mData.size())||(index<0)){ 
                return null;
            }
            AddressNode node =  mData.remove(index);
            rmFromCache(node);
            return node;
        }

        /**
         * remove spedify AddressNode object 
         * @return return true if success
         */
        public boolean rm(AddressNode node) {
            if(mData.remove(node)) {
                onRemoveNode(node);
                rmFromCache(node);
                return true;
            }           
            return false;
        }
        
        /**
         * remove all nodes after given object 
         * @return return true if success
         */
        public boolean rmAll(AddressNode o) {
            if(!mData.contains(o)) {
                return false;
            }          
            AddressNode node = getLast();
            while(node != o) {
                rmLast();
                node = getLast();
            }
            return rm(node);
        }

        /**
         * Insert addresses 
         * @param index Spedify index to insert 
         * @param addresses Addresses text 
         * @param sep Separator
         * @return the count of inserted addresses 
         */
        public int insert(int index, String addresses, String sep) {
            String[] addrs = split(addresses);
            if (addrs == null)
                return 0;
            AddressNode node  = null;
            int count  = 0;
            int anchor  = index;
            for(String addr : addrs) {
                node = new AddressNode(addr.trim());
                if(isAcceptable(node.getData())) {
                    count++;
                    onAddNode(node);
                    mData.add(anchor++, node);
                }
            }
            return count;
        }
        
        /**
         * Append addresses
         * @param addresses Addresses text 
         * @param sep  Separator
         */
        public int append(String addresses, String sep) {
            if(TextUtils.isEmpty(addresses)) {
                return 0;
            }
            String[] addrs = split(addresses);
            if (addrs == null)
                return 0;
            AddressNode node = null;
            int count = 0;
            for(String addr : addrs) {
                node = new AddressNode(addr.trim());
                if(isAcceptable(node.getData())) { 
                    count++;                 
                    addLast(node);
                }
            }
            return count;
        }

        public String[] split(String text) {
            String escaped = text;
            if(!TextUtils.isEmpty(text)) {
                escaped = text.replace(mOptions.getEscapedSeparator(), DUMMY_SEPARATOR);
                return  escaped.split(mOptions.Separator);
            }
            return null;
        }

        /**
         * @hide
         * since insertion may occurs in the expander scope
         * we need adjust index to find real insertion position
         * ---------------------------------------------------
         * %: spanned AddressNode
         * X: unspanned AddressNode
         *   +======= this is expander index 
         *   V
         * %%%XXXX%%%
         * 0123456789
         *     ^
         *     +==== this is where we wish insert to 
         */ 
        public int adjustInsertionAnchor(int index) {
            ListIterator<AddressNode> li = listIterator(index);
            while(li.hasNext()) {
                AddressNode node = li.next();
                if(node.mSpanned) {
                    D("INS : " + li.previousIndex() + " BEFORE: " + node); 
                    return 0;
                }
            }
            D("INS AT THE END : " + size());
            return size();
        }
        
        /**
         * before we enter address to the adt, we need a 
         * chance to check wheather the address will be 
         * accepted, simplely we just check duplication 
         * address here.
         */
        private boolean isAcceptable(String data) {
            if(!TextUtils.isEmpty(data)) {
                if(!mCache.containsKey(data)) {
                    mCache.put(data,size());
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        
        /**
         * @return if not have any address return true, else false
         */
        public boolean isEmpty() {
            return size() == 0;
        }
        
        /**
         * Get the last AddressNode
         */
        public AddressNode getLast() {
            return size() <= 0 ? null : mData.get(size() - 1);
        }

        /**
         * Get the specify AddressNode by index
         * @param index 
         */
        public AddressNode get(int index) {
            return (index >= size() || index < 0) ? null : mData.get(index);
        }

        public String[] getAll() {
            return toStringArray();
        }
        
        /**
         * Get data of specify node 
         */
        public String getAddress(int index) {
            return (String)mData.get(index).getData();
        }

        /**
         * Join addresses with specify separator
         * @param index start index 
         * @param count offset
         * @param sep separator 
         */
        public String joinWith(int index, int count, String sep) {
            if(sep == null) {
                sep = "";
            }
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<count-1; ++i) {
                sb.append(getAddress(index)).append(sep);
                index++;
            }
            sb.append(getAddress(index));
            return sb.toString();
        }

        /** 
         * Join all addresses with specify separator
         * @param sep separator 
         */ 
        public String joinWith(String sep) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<size()-1; ++i) {
                sb.append(getAddress(i)).append(sep);
            }
            return (size() > 0) ? sb.append(getLast().getData()).toString() : "";
        }
        
        @Override
        public String toString() {
            return joinWith(" | ");
        }

        /**
         * Get all data of address node as String array 
         */
        public String[] toStringArray() {
            ArrayList<String> addrs = new ArrayList<String>();
            AddressNode[] nodes = getAllNodes();
            if(nodes == null) {
                return new String[0];
            }
            for(AddressNode node : nodes) {
                addrs.add(node.getData());
            }
            return addrs.toArray(new String[size()]);
        }
    
        /**
         * Get address node count 
         */
        public int size() {
            return mData.size();
        }
        
        /**
         * Get all address nodes
         */
        public AddressNode[] getAllNodes() {
            return (size() > 0) ? mData.toArray(new AddressNode[size()]) : null;
        }
        
        /**
         * <p>Get specify range of address nodes</p>
         * @param start start index
         * @param end   end index
         */
        public AddressNode[] getNodes(int start, int end) {
            ArrayList<AddressNode> nodes = new ArrayList<AddressNode>();
            for(int i=start; i<end && end <= size(); ++i) {
                nodes.add(get(i));
            }
            return nodes.toArray(new AddressNode[nodes.size()]);
        }

        /**
         * <p>Clear duplication check cache</p>
         */
        private void clearCache()  {
            mCache.clear();
        }

        private void rmFromCache(AddressNode node) {
            mCache.remove(node.getData());
        }
        
        // -----------------------------------------
        // tmp for duplication checking 
        private HashMap<String,Integer> mCache = null;
        private LinkedList<AddressNode> mData  = null;
    }


	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {   
        super.onFocusChanged(focused, direction, previouslyFocusedRect); 
        mController.clearCurrentSelection();
//        if(getText().length() <=0 && focused) {
//        	ArrayList<String> tmpArr = new ArrayList<String>();
//        	tmpArr.add("public");
//        	this.setAdapter(new PrecastRecipientsAdapter(getContext(), tmpArr));
//        	this.showDropDown();
//        }
        if(!isButtonMode()) {
            if(!focused) {
//                mController.resetTextBuffer();
//                mController.tryEnterAddress(); 
//                mController.restoreTextBuffer();
            }
            return; //TODO(zhoujb): should refactory controller, move it in 
        }
        
        if(!focused) {
//            mController.tryEnterAddress();
//            mController.showSingleLineHint();
        } else {            
            mController.clearCurrentSelection();
            mController.showAddresses();
        }       
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}
        
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!isButtonMode()) {
            return super.onKeyDown(keyCode, event);
        }

        switch(keyCode) {
        case KeyEvent.KEYCODE_ENTER:
            mController.tryEnterAddress();
            return true;
        case KeyEvent.KEYCODE_DEL:
            if(!isButtonMode()) {
                mController.tryEnterAddress();
            } else {
                mController.beforeDeletionKey();
            }
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:		
        case KeyEvent.KEYCODE_DPAD_RIGHT:		
        case KeyEvent.KEYCODE_DPAD_UP:				
        default:
            break;
        }

	   boolean ret = super.onKeyDown(keyCode, event);
       if(keyCode == KeyEvent.KEYCODE_DEL && isButtonMode()) {
           mController.afterDeletionKey(); // TODO(zhoujb): should refactory controller, move it in 
       }
       return  ret;
	}

    @Override 
	public boolean onTouchEvent(MotionEvent event) {
        if(!isButtonMode()) {
            return super.onTouchEvent(event); // TODO(zhoujb): refactory controller, move it in
        }       
        mController.onTouchEvent(event);
        invalidate();
        return super.onTouchEvent(event);
    }

    /**
     * Change padding
     */
    private void resetPadding() {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

	/**
	 * <p>
	 * Closes the drop down if present on screen.
	 * </p>
	 */
	public  void dismissDropDown() {
        super.dismissDropDown();
	}

	/**
	 * <p>Address pad will keep a padding between control border and address block.
	 * LinePaddingSpan will adjust text line height.</p>
	 * 
	 * @author luma
	 */
	private class LinePaddingSpan implements LineHeightSpan {
		public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
            //NOTICE(zhoujb): adjust FontMetrics for drawing text 
			fm.top = mTop;
			fm.bottom = mBottom;
			fm.ascent = mAscent;
			fm.descent = mDescent;
		}
	}

	/**
	 * <p>AddressPad can have more than one address block and every block is
	 * rendered by AddressSpan</p>
	 * 
	 * @author luma
	 */
    private class AddressSpan extends ReplacementSpan {
		private int				mSize;
		private CharSequence	mDecoratedAddress;
        private CharSequence    mLabel;
		private boolean			mTruncate;
		private boolean			mSelected = false;
		private RectF		  mInnerBound = new RectF();
        private int                  mMax = -1; 
        private int                mStart;
        private int                  mEnd;
        private Options         mOptions  = null;
        private int        mSpanType = 0;
        public static final int SPAN_TYPE_DEFAULT = 0;
        public static final int SPAN_TYPE_CIRCLE = 1;
        public boolean isExpand = false;
        private AddressNode mNode;
//        public static final int SPAN_TYPE_PUBLIC_CIRCLE = 2;

        public AddressSpan(String text, Options options) {
            mOptions = options;
            mLabel   = null;
            setDecoratedText(text);
        }
        
        public AddressSpan(String text, Options options, int span_type, AddressNode node) {
            mSpanType = span_type;
            mOptions = options;
            mLabel   = null;
            mNode = node;
            setDecoratedText(text);
        }

        public String getDecoratedText() {
            return mDecoratedAddress.toString();
        }

        public void setDecoratedText(String text) {
            if(mDecorator != null) {
                mDecoratedAddress = mDecorator.onDecorate(text);
                // XXX(zhoujb): the following handle is nassary ???
//                if(TextUtils.isEmpty(mDecoratedAddress)) {
//                    mDecoratedAddress = text;
//                }
            } else {
                mDecoratedAddress = text;
            }
            if(!TextUtils.isEmpty(mDecoratedAddress)) {
                mDecoratedAddress = mDecoratedAddress.toString().replace(mOptions.getEscapedSeparator(), mOptions.Separator);
            }
        }
        
        // label will display instead of decorated text on draw, it has 
        // higher priority 
        public void setLabel(String text) {
            mLabel = text;
        }

        public void clearLabel() {
            mLabel = null;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(" ("+mStart+","+mEnd+")");
            return sb.toString();
        }
        public int getSpanStart() {
            return mStart;
        }

        public int getSpanEnd() {
            return mEnd;
        }

        public boolean hitTest(float x, float y) {
            return mInnerBound.contains(x,y);
        }
        
        public void setSelected(boolean selected) {
            mSelected = selected;
        }

        public boolean isSelected() {
            return mSelected;
        }
        
		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
			/*
			 * ------------------------------------
			 * |               3                  |
			 * |   ---------------------------    |
			 * |   |           8             |    |
			 * | 1 |  5   Address Text   7   | 4  |
			 * |   |           6             |    |
			 * |   |--------------------------    |
			 * |               2                  |
			 * |----------------------------------
			 * 
			 * Above is a conceptual diagram about address block. An address block has a out most bounding box
			 * which contains a inner box. The inner box is generally the place painting occurs. In default
			 * implementation, we will draw a round rectangle in inner box. There is no spacing between two 
			 * address block's out bounding boxes.
			 * For the number labeled in above figure:
			 * 1. left padding, also can be seen as half spacing between two address blocks
			 * 2. bottom padding
			 * 3. top padding
			 * 4. right padding
			 * 5&7. radius, radius should conform to: radius = (top_inner_padding + bottom_inner_padding + text_height) / 2
			 * 6. bottom inner padding
			 * 8. top inner padding
			 */
			// calculate address bound
			
            mStart = start;
            mEnd   = end;
			int offset = (bottom - top - mRadius - mRadius) >> 1;
			mInnerBound.left = x + LEFT_PADDING;
			mInnerBound.top = top + offset;
			mInnerBound.right = x + mSize - RIGHT_PADDING ;
			mInnerBound.bottom = bottom - offset - 1;

			// save old state
			int oldColor = paint.getColor();
			paint.setTextSize(24);
			// draw background
			Drawable background = mSelected ? mOptions.AddressSBg : mOptions.AddressBg;
			if(!mSelected) {
			    if(SPAN_TYPE_CIRCLE == mSpanType) {
			        background = mOptions.AddressCircleBg;
//			    }else if(SPAN_TYPE_PUBLIC_CIRCLE == mSpanType) {
//			        background = mOptions.publicCircleBg;
			    }else {
			        background = mOptions.AddressBg;
			    }
			}
			background.setBounds(new Rect((int) mInnerBound.left, 
                                          (int) mInnerBound.top-ADDRESS_TOP_PADDING, 
                                          (int) mInnerBound.right, 
                                          (int) mInnerBound.bottom));
			background.draw(canvas);

			/*
			 * restore state and draw text
			 * we need check max text width, truncate if need
			 */
			paint.setColor(mSelected ? mOptions.AddressTextSColor : mOptions.AddressTextColor);
            CharSequence drawText = mLabel == null ? mDecoratedAddress : mLabel;
            
            if(TextUtils.isEmpty(drawText)) {
            	return;
            }
            
			if(mTruncate) {
				//int maxTextWidth = getMeasuredWidth() - LEFT_PADDING - RIGHT_PADDING
				//	- mRadius - mRadius - getPaddingRight() - getPaddingLeft();
				int maxTextWidth = getMeasuredWidth() - mRadius - 5*LEFT_PADDING- 5*RIGHT_PADDING-
					- getPaddingRight() - getPaddingLeft();
				CharSequence ellipsized = TextUtils.ellipsize(drawText, 
                                                              (TextPaint) paint, maxTextWidth, TruncateAt.MIDDLE);
				//canvas.drawText(ellipsized, 0, ellipsized.length(), mInnerBound.left + mRadius, 
                //                mInnerBound.top - mAscent, paint);
                canvas.drawText(ellipsized, 0, ellipsized.length(), mInnerBound.left+ mRadius/2, 
                                mInnerBound.top - mAscent, paint);
			} else {
				//canvas.drawText(drawText, 0, drawText.length(), mInnerBound.left + mRadius, 
                //                mInnerBound.top - mAscent, paint);
				canvas.drawText(drawText, 0, drawText.length(), mInnerBound.left+ mRadius/2, 
                                mInnerBound.top - mAscent, paint);
				
				if(mNeedClick && !isExpand) {
					Drawable tmpdraw = getResources().getDrawable(R.drawable.ic_acl_x);
					int height = (int) ((mInnerBound.bottom - mInnerBound.top - mRemoveIcon.getHeight())/2);
//					Log.d(TAG, "mInnerBound.left: " + mInnerBound.left +"  mSize : " +  mSize + " mInnerBound.top: " + mInnerBound.top +" tmpdraw.getIntrinsicWidth(): " + tmpdraw.getIntrinsicWidth() + " height: " + height );
					tmpdraw.setBounds(new Rect((int) mInnerBound.left + mSize - mRemoveIcon.getWidth() - RIGHT_PADDING - 20 , 
							(int) mInnerBound.top - ADDRESS_TOP_PADDING + height, 
							(int) mInnerBound.left + mSize - RIGHT_PADDING - 20, 
							(int) mInnerBound.top - ADDRESS_TOP_PADDING + height + mRemoveIcon.getWidth()));
					
					tmpdraw.draw(canvas);
				}
			}

			paint.setColor(oldColor);
		}

		@Override
		public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
			int max = getWidth() - 2*LEFT_PADDING - 2*RIGHT_PADDING - getPaddingRight() - getPaddingLeft();

         CharSequence drawText = mLabel == null ? mDecoratedAddress : mLabel;
         if(TextUtils.isEmpty(drawText)) {
        	 if(mNode != null) {
        		 mController.mAdt.rm(mNode);
        	 }
        	 return 0;
         }
			//mSize = (int) paint.measureText(drawText, 0, drawText.length()) 
			//	+ mRadius + mRadius + LEFT_PADDING + RIGHT_PADDING;
         if(mNeedClick && !isExpand) {
        	 mSize = (int) paint.measureText(drawText, 0, drawText.length()) + mRadius
    				 + LEFT_PADDING + RIGHT_PADDING  + mRemoveIcon.getWidth() + 20;
         }else {
        	 mSize = (int) paint.measureText(drawText, 0, drawText.length()) + mRadius
        			 + LEFT_PADDING + RIGHT_PADDING ;
         }
			mTruncate = mSize > max;
			mSize = Math.min(mSize, max);
            return mSize; 
		}
      
        public int getSize() {
            return mSize;
        }
	}

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        // D("ON TEXT CHANGED: "+text +" < s="+start +" b="+before+" a="+after+" >");
//        if(!isButtonMode()) {
//            super.onTextChanged(text, start, before, after);
//            return;// TODO(zhoujb): refactory controller, move it in
//        }   

        if(mController != null) {
            mController.onTextChanged(text, start, before, after);
        }       
        super.onTextChanged(text, start, before, after);

    }

    // TODO(zhoujb): once we supported expanding and shrinking, we can
    // control them on scroll changed.
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // D("l="+l+" t="+t+" oldl="+oldl + " oldt="+oldt);
        super.onScrollChanged(l,t,oldl,oldt);
    }

    @Override
    protected  void onSelectionChanged(int selStart, int selEnd) {
        // D("selStart="+selStart + "  selEnd="+selEnd);
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * @hide 
     * This function is for easy debugging ....
     */
    private static void D(String msg) {
        String Tag = "xxx";
        if(LOCAL_LOGV)Log.i(Tag, msg);
    }

    public boolean performLongClick() {
    	//B128: remove all LongClick menu here, Jiabo's code will left the "paste" menu
        if(!isButtonMode()) {
            return super.performLongClick(); // TODO(zhoujb): refactory controller, move it in
        }  

        // NOTICE: here we use a small trick, when long click on 
        // selected address, we disable popup context menu by 
        // single click and let's super handle popup instead.
        mController.resetPopup();  
        return (mController.hasSingleLineHint()) ? true : super.performLongClick();
    	//return false;
    }
    
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {   
        

        if(mController != null) {
//            mController.onLayout(changed,left,top,right,bottom);TODO 
        }
        super.onLayout(changed,left,top,right,bottom);
    }


    private int _calcHeight() {
        Layout l  = getLayout();
        int lines = l.getLineCount(); 
        int ld = l.getLineDescent(0);
        int la = l.getLineAscent(0);

        return ((ld-la) * lines) + getPaddingBottom() + getPaddingTop();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void replaceText(CharSequence text) {
        // D("COMPLETION: " + "'"+text+"'");
        // XXX(zhoujb): since input method on landscape mode can dismiss now, we should keep completion works
        // if(!isButtonMode()) {
        //    return; // TODO(zhoujb): refactory controller, move it in
        // }
        mController.replaceText(text);
    }
    
    //@hide
    //TODO(zhoujb): support this method 
    private int appendAddresses(String addresses) {
        return mController.appendAddresses(addresses);
    }
    
    /**
     * Set addresses
     * @param addresses addresses to be set 
     * @return the count of addresses
     */
    public int setAddresses(String addresses) {
        if(TextUtils.isEmpty(addresses)) {
            clearAll();
        }
        // disable auto completion when set address fisrt 
        int count = mController.setAddresses(addresses);
        if(this.isButtonMode()){
//        	mController.forceUpdateSingleLine();
        }
        return count;
    }

    /**
     * Get current address
     */
    public String getSelectedAddress() {
        return mController.getCurrentAddress();
    }

    /**
     * Get all addresses joined with separator
     */
    public String getAddresses() {
        return mController.getAddresses();
    }

    /**
     * Get all addresses 
     */
    public String[] getAddressesArray() {
        return mController.getAddressesArray();
    }

    /**
     * Clear all addresses
     */
    public void clearAll() {
        mController.clearAllAddresses();
    }

    /**
     * For context menu 
     */
    private class MenuHandler implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            return onAddressPadMiniContextMenuItem(item.getItemId());
        }
    }

    private boolean onAddressPadMiniContextMenuItem(int menuId) {
        ClipboardManager clip = (ClipboardManager)getContext()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence text = null;

        switch(menuId) {
//        case MENU_ID_COPY:
//            text = mController.getCurrentAddress();
//            clip.setText(text);
//            break;
        case MENU_ID_CUT:
            text = mController.getCurrentAddress();
            clip.setText(text);
            mController.deleteCurrentAddress();
            break;
        case MENU_ID_PASTE:
            text = clip.getText();
            mController.insertToBuffer(text);
            break;
        case MENU_ID_SELECT:
        case MENU_ID_SELECT_ALL:
            selectAll();
            break;
//        case MENU_ID_EDIT:
//            mController.editCurrentAddress();
//            break;
        case MENU_ID_DELETE:
            mController.deleteCurrentAddress();
            break;
        case MENU_ID_VIEW_CONTACT:
            text = mController.getCurrentAddress();
            viewContact(text.toString());
            mLastAddToContact = mController.getCurrentAddressNode();
            break;
//        case MENU_ID_ADD_CONTACT:
//            text = mController.getCurrentEscapedAddress();
//            AddContact(getContext(),text.toString());
//            mLastAddToContact = mController.getCurrentAddressNode();
//            break;
        default:
            /* NOP */ 
        }
        mController.clearCurrentSelection();
        return true;
    }
     
    /**
     * Empty check
     */
    public boolean isEmpty() {
        return mController == null ? true : mController.getCount() == 0;
    }

    /**
     * Get addresses number
     */
    public int getCount() {
        return mController == null ? 0 : mController.getCount();
    }
 
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
//        MenuHandler handler = new MenuHandler();
//        int name = 0;
//        boolean selection = true;
//
//        // menu.setHeaderTitle(com.android.internal.R.string.
//        //                     editTextMenuTitle);
//        if(canPaste()){
//            menu.add(0,MENU_ID_PASTE,0,R.string.paste)
//                .setOnMenuItemClickListener(handler);
//        }
//
//        if(canCopy()) {
//            //TODO(zhoujb): support select all 
//            // if (selection) {
//            name = R.string.copy;
//            // } else {
//            //     name = com.android.internal.R.string.copyAll;
//            // }
//           
//            menu.add(0,MENU_ID_COPY,0,R.string.copy)
//                .setOnMenuItemClickListener(handler);
//        }
//        
//        if(canCut()) {
//            if (true) {
//                name = R.string.cut;
//            } else {
//                name = R.string.cutAll;
//            }
//            menu.add(0,MENU_ID_CUT,0,name)
//                .setOnMenuItemClickListener(handler);        
//        }
//        
//        if(canSelectAll()) {
//            menu.add(0,MENU_ID_SELECT_ALL,0,R.string.selectAll)
//                .setOnMenuItemClickListener(handler); 
//        }
//
//        if(canEdit()) {
//            menu.add(0,MENU_ID_EDIT,0,R.string.address_pad_edit)
//                .setOnMenuItemClickListener(handler); 
//        }
//
//        if(canDelete()) {
//            menu.add(0,MENU_ID_DELETE,0,R.string.address_pad_delete)
//                .setOnMenuItemClickListener(handler);        
//        }
//        
//        if(canViewContact()) {
//            menu.add(0,MENU_ID_VIEW_CONTACT,0,R.string.address_pad_view_contact)
//                .setOnMenuItemClickListener(handler);           
//        } else {
//            if(canAddContact()){
//                menu.add(0,MENU_ID_ADD_CONTACT,0,R.string.address_pad_add_contact)
//                    .setOnMenuItemClickListener(handler);  
//            }
//        }
    }
    
    private boolean canAddContact() {
        return (mController.getSelectedAddressIndex() != -1) ? true : false;
    }

    private boolean canDelete() {
        return (mController.getSelectedAddressIndex() != -1) && isEnabledContextMenu() ? true : false;
    }
   
    private boolean canEdit() {
        return (mController.getSelectedAddressIndex() != -1) && isEnabledContextMenu() ? true : false;
    }

    private boolean canPaste() {
        ClipboardManager clip = (ClipboardManager)getContext()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        return clip.hasText() && isEnabledContextMenu();
    }
    
    private boolean canCopy() {
        return (mController.getSelectedAddressIndex() != -1) ? true : false;
    }

    private boolean canSelectAll() {
        return !hasSelection() && (mController.getSelectedAddressIndex() == -1) && !isEmpty() && isEnabledContextMenu();
    }

    private boolean canCut() {
        return (mController.getSelectedAddressIndex() != -1) && isEnabledContextMenu() ? true : false;
    }

    private boolean canSelectText() {
        return false;
    }

    private boolean canViewContact() { 
        String text = mController.getCurrentAddress();
        // is existed phone number ?
        // D("CURRENT ADDR: " + text);
        if(isPhoneNumber(text) && isNumberSavedInContact(text)) {
            // D("" + text + " IS PHONE NUMBER");
            return true;
        } else {
            // D("" + text + " TREAT AS MAIL");
            return isEmailAddressSavedInContact(text);
        }
    }


    private class ContactInfo {
        public ContactInfo() {
            id      = -1;
            subType = CONTACT_INFO_SUB_TYPE_NONE;
            name    = "";
            mail    = "";
        }

        public long id      = -1;
        public int  subType = CONTACT_INFO_SUB_TYPE_NONE;
        public String name  = "";
        public String mail  = "";

        public String toString() {
            return String.format("[%4d]-[sub:%d]: %s<%s>",id,subType,name,mail);
        }
    }
    
    /**
     * Send view intent to see specify address 
     * @param address
     */
    private void viewContact(String address) {
        // guess address type, we only handle two kind of address
        // once we got the address is not a phone number, we'll 
        // treat it as email address 
        int  type      = 0;

        if(isPhoneNumber(address)) {
            type = ADDRESS_TYPE_PHONE;
        } else {
            type = ADDRESS_TYPE_EMAIL;
        }
        
        ContactInfo ci = getContactInfoByType(type, address);
        //b497 borqsbt2:0007346.avoid froce close.
        if (ci.id == -1) {
        	Toast.makeText(getContext(), R.string.cannot_send_message_reason, Toast.LENGTH_SHORT).show();
        } else {
        	//b497 2010-12-16 0009445 change uri
//        	Uri uri = ContentUris.withAppendedId(People.CONTENT_URI,ci.id);
        	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ci.id);
        	getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }
    

    /**
     * if the given email address existed in phonebook, return true
     * otherwise return false
     */
    public boolean isEmailAddressSavedInContact(String address) {
        if(TextUtils.isEmpty(address)) {
            return false;
        }

        String addr = parseEmailAddress(address);
        boolean ret = false;
        Context context = getContext();

        if(TextUtils.isEmpty(addr)) {
            return false;
        }

        String pureAddress = validateAddress(addr);
        D("SEARCHING EMAIL : " + pureAddress);
        
        String name=null;
        Cursor cursor = null;
        String contactInfoSelectionArgs[] = new String[1];
        contactInfoSelectionArgs[0] = address;
        String EMAIL_SELECTION = Email.DATA + "=? AND " + Data.MIMETYPE + "='"+ Email.CONTENT_ITEM_TYPE + "'";
        try{
        	ContentResolver cr = context.getContentResolver();//TODO 
        	cursor = cr.query(Data.CONTENT_URI, new String[]{Email.DISPLAY_NAME,Phone.DISPLAY_NAME}, EMAIL_SELECTION, contactInfoSelectionArgs, null);
//            cursor = cr.query(context, context.getContentResolver(),
//                    Data.CONTENT_URI,
//                    new String[]{Email.DISPLAY_NAME,Phone.DISPLAY_NAME},
//                    EMAIL_SELECTION,
//                    contactInfoSelectionArgs,
//                    null);
            if(cursor!=null && cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndexOrThrow(Email.DISPLAY_NAME));
                if(TextUtils.isEmpty(name)){
                    name = cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME));
                }
            }
        }catch(Exception e){
            //Ignore
        }finally{
            if(cursor!=null){
                cursor.close();
            }
        }
        
        if(!TextUtils.isEmpty(name)){
            ret = true;
        }else{
            ret = false;
        }

        return ret;
    }
    
    /**
     * Get ContactInfo
     * @param type address type can be ADDRESS_TYPE_PHONE or ADDRESS_TYPE_EMAIL
     * @param address address 
     */
    public ContactInfo getContactInfoByType(int type, String address) {
        ContactInfo ci = null;
        switch(type) {
        case ADDRESS_TYPE_PHONE:
            ci    = getContactInfoByPhoneNumber(getContext(), address);
            break;
        case ADDRESS_TYPE_EMAIL:
            ci    = new ContactInfo();
            ci.id = getContactIdByEmailAddress(getContext(), address);
            break;
        default:
            /* NOP */
        }
        return ci;
    }

    /**
     * Get ContactInfo by specify number  
     * Notice: it will search both local contacts and sim card contacts
     * @param context context
     * @param number telephone number 
     * @return ContactInfo object or null
     */
    private ContactInfo getContactInfoByPhoneNumber(Context context, String number) {
        if (number == null || number.length() <= 0) {
            return null;
        }

        // Context context = getContext();
        Cursor  cursor  = null;
        ContactInfo ci  = new ContactInfo();
        
		try {
			cursor = context
					.getContentResolver()
					.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(number)),
							new String[] {
									ContactsContract.CommonDataKinds.Phone._ID,
									ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
							null, null, null);
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
				ci.id = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID));
				ci.subType = CONTACT_INFO_SUB_TYPE_PHONE_LOCAL;
				ci.name = cursor
						.getString(cursor
								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			}
		} catch(Exception e) {
            D("SEARCH LOCAL CONTACT : " + e);
        } finally {
        	if (cursor != null) {
                cursor.close(); 
        	}
        }
        return ci;
    } 
    
    /**
     * Send add contact intent 
     * @param context context
     * @param text contact method (email or telephone number)
     */
    private final void AddContact(Context context, String text) {
        int    type    = 0;
        Intent intent  = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(People.CONTENT_ITEM_TYPE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        // guess address type, we only handle two kind of address
        // once we got the address is not a phone number, we'll 
        // treat it as email address 
        type = isPhoneNumber(text) ? ADDRESS_TYPE_PHONE : ADDRESS_TYPE_EMAIL;

        if(type == ADDRESS_TYPE_PHONE) {
            intent.putExtra(Insert.PHONE, text);
        } else {
            ContactInfo ci = parseContactInfoFromEmailAddress(text);            
            intent.putExtra(Insert.NAME,  ci.name);
            intent.putExtra(Insert.EMAIL, ci.mail);
            intent.putExtra("replace_name", false);
        }
       
        context.startActivity(intent);
    }
   
    /**
     * Parse contact information from specify email address
     * @param address specify address 
     */
    public ContactInfo parseContactInfoFromEmailAddress(String address) {
        ContactInfo ci = new ContactInfo();
        if(address.matches("^.*<.*>")) {         
            int start = address.indexOf("<");
            int end   = address.indexOf(">");
            try {
                ci.mail = address.substring(start+1, end);
                ci.name = address.substring(0,start).trim().replace("\"","");
            } catch (Exception ex) {
                ci.mail = address;
                ci.name = null;
            }
        } else {
            ci.mail = address;
        }    
        return ci;
    }


    /**
     * @hide
     * Don't use Regex.PHONE_PATTERN because that is intended to
     * detect things that look like phone numbers in arbitrary text,
     * not to validate whether a given string is usable as a phone
     * number.
     *
     * Accept anything that contains nothing but digits and the
     * characters in PHONE_NUMBER_SEPARATORS, plus an optional
     * leading plus, as long as there is at least one digit.
     * Reject any other characters.
     */ 
    public static boolean isPhoneNumber(String recipient) {
        int len = recipient.length();
        int digits = 0;
            
        for (int i = 0; i < len; i++) {
            char c = recipient.charAt(i);

            if (Character.isDigit(c)) {
                digits++;
                continue;
            }
            if (PHONE_NUMBER_SEPARATORS.indexOf(c) >= 0) {
                continue;
            }
            if (c == '+' && i == 0) {
                continue;
            }
            return false;
        }
        return digits == 0 ? false : true;
    }
    
    private String validateAddress(String addr) {
        if (TextUtils.isEmpty(addr))  {
            // keep origin
            return addr; 
        }
        return addr.replace("/", "");
    }
    
    /**
     * Get ContactInfo of specify email address
     * @param address specify email addresses
     */
    private ContactInfo getContactInfoByEmailAddress(Context context, String address) {
        ContactInfo ci = new ContactInfo();
        if (LOCAL_LOGV) {
            Log.d(TAG,"==================================address="+address);
        }
        if(!TextUtils.isEmpty(address)) {
            String name=null;
            Cursor cursor = null;
            String contactInfoSelectionArgs[] = new String[1];
            contactInfoSelectionArgs[0] = address;
            String EMAIL_SELECTION = Email.DATA + "=? AND " + Data.MIMETYPE + "='"+ Email.CONTENT_ITEM_TYPE + "'";
            try{
            	ContentResolver cr = context.getContentResolver();//TODO 
            	cursor = cr.query(Data.CONTENT_URI,  new String[]{Email.DISPLAY_NAME,Phone.DISPLAY_NAME}, EMAIL_SELECTION, contactInfoSelectionArgs, null);
//                cursor = SqliteWrapper.query(context, context.getContentResolver(),
//                        Data.CONTENT_URI,
//                        new String[]{Email.DISPLAY_NAME,Phone.DISPLAY_NAME},
//                        EMAIL_SELECTION,
//                        contactInfoSelectionArgs,
//                        null);
                if(cursor!=null && cursor.moveToFirst()){
                    name = cursor.getString(cursor.getColumnIndexOrThrow(Email.DISPLAY_NAME));
                    if(TextUtils.isEmpty(name)){
                        name = cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME));
                    }
                }
            }catch(Exception e){
                //Ignore
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            if (LOCAL_LOGV) {
                Log.d(TAG,"==================================name="+name);
            }
            if(!TextUtils.isEmpty(name)){
                ci.name = name;
            }else{
                ci.name = "";
            }
            
        }
        return ci;
    }
    /**
     * Get contact id of specify address
     * @param context
     * @param address
     * @return contact id or -1
     */
    private long getContactIdByEmailAddress(Context context, String address) {
        long contactId = -1;
        String addr = parseEmailAddress(address);
        D("ADDR: " + addr);
        if (!TextUtils.isEmpty(addr)) {
            //XXX(zhoujb):  for history reason, contacts do not
            //support uri decoding, that means if we encode uri
            //which contains '/' and query, we'll get nothing,
            //if not handle such situation may case query exception,
            //so workaround by remove '/' from address
            String pureAddress = validateAddress(addr);
            //b497 2010-12-06 borqsbt2:0007346
            Uri addressUri = Uri
                .withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(pureAddress));
            Cursor c = null;
            try {
            	c = context.getContentResolver().query(addressUri, new String[]{Email.CONTACT_ID}, null, null, null);
                if (c != null && c.moveToNext()) {
                    contactId = c.getLong(0);
                }
            } catch (Exception e) {
                D("GET CONTACT ID FAILED : " + e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        return contactId;
    } 
    
    /**
     * a very simple parser function for getting email address
     * "name"<zhoujb.cn@gmail.com>
     * @parse address text 
     * @return decorated address 
     */
    private static String parseEmailAddress(String address) {
        if(TextUtils.isEmpty(address)) {
            return "";
        }
        String addressPart = null;
        if(address.matches(".*<.*>")) {         
            try {
                int start = address.indexOf("<");
                int end   = address.indexOf(">");
                addressPart = address.substring(start+1, end);
            } catch (Exception ex) {
                return address;
            }
            return addressPart;
        } else {
            return address;
        }
    }


    /**
     * @param number specify telephone number 
     * @return if given phone number existed in phone book return true otherwise false
     */
    public boolean isNumberSavedInContact(String number) {
        if (number == null || number.length() <= 0) {
            return false;
        }
        Context context = getContext();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                .query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                                                        new String[]{ ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME }, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch(Exception e) {
            D("SEARCH CONTACT FAILED : " + e);
        } finally {
        	if (cursor != null) {
                cursor.close(); 
        	}
        }
        return false;
    }
    
    /**
     * If there are separators in addresses, it may case some confuse , this function will
     * return the escaped address for inner handle
     */
    public  String getEscaped(String text) {
        String escaped = "";
        if(!TextUtils.isEmpty(text)) {
            escaped = text.replace(mOptions.Separator, mOptions.getEscapedSeparator());
        }
        return escaped;
    }   
    
    /**
     * Get separator
     */
    public String getSeparator() {
        return mOptions.Separator;
    }

    // WORKAROUND(zhoujb):
	 // this method should be called only when addresspadmini is
    // no working at readonly mode, it should only be a hook
    // which listen the configuration changed from activity side(onConfigurationChanged)
    // I can't find the root case of some layout issue(see:57089), so add
    // this workaround :(
    /**
     * @hide
     */
//    public void onConfigurationChanged(final Configuration newConfig) {
//        mOptions.DisableOritationChange = true;
//        new Thread(new Runnable() {
//			
//			public void run() {
//		        Message msg = Message.obtain();
//		        msg.what = CONFIGURATION_CHANGED;
//		        msg.obj = newConfig;
//		        mHandler.removeMessages(CONFIGURATION_CHANGED);
//		        mHandler.sendMessage(msg);
//			}
//		}, TAG).start();
//    }
    
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CONFIGURATION_CHANGED:
                mController.onConfigurationChanged((Configuration) msg.obj);
                break;
            }
        }
    };

    public boolean hasPendingSpan() {
        boolean pending = false;
        AddressSpan [] spanned = mController.getAllAddressSpan();
        if (null != spanned && spanned.length > 0) {
            final AddressSpan span = spanned[spanned.length - 1];
            if (null != span) {
                if (span.getSpanStart() == span.getSpanEnd()) {
                    pending = true;
                }
            }
        }
        return pending;
    }
    
    public boolean mNeedClick = false;
    
    public interface AddressPadNoteActionListener {
    	public void noteRemove(String notStr);
    }
    
	private static final HashMap<String, WeakReference<AddressPadNoteActionListener>> noteActionlisteners = new HashMap<String, WeakReference<AddressPadNoteActionListener>>();

	public static void registerNoteActionListener(String key,
			AddressPadNoteActionListener listener) {
		synchronized (noteActionlisteners) {
			WeakReference<AddressPadNoteActionListener> ref = noteActionlisteners
					.get(key);
			if (ref != null && ref.get() != null) {
				ref.clear();
			}
			noteActionlisteners.put(key,
					new WeakReference<AddressPadNoteActionListener>(listener));
		}
	}

	public static void unregisterNoteActionListener(String key) {
		synchronized (noteActionlisteners) {
			WeakReference<AddressPadNoteActionListener> ref = noteActionlisteners
					.get(key);
			if (ref != null && ref.get() != null) {
				ref.clear();
			}
			noteActionlisteners.remove(key);
		}
	}
	
	 private void refreshItemUI(String notStr) {
	        synchronized (noteActionlisteners) {
	            Set<String> set = noteActionlisteners.keySet();
	            Iterator<String> it = set.iterator();
	            while (it.hasNext()) {
	                String key = it.next();
	                WeakReference<AddressPadNoteActionListener> ref = noteActionlisteners.get(key);
	                if (ref != null && ref.get() != null) {
	                    ref.get().noteRemove(notStr);
	                }
	            }
	        }
	    }
}
