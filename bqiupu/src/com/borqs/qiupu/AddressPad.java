package com.borqs.qiupu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Filter.FilterListener;
import android.view.inputmethod.InputMethodManager;

import com.borqs.qiupu.R;
/**
 * @hide
 *
 * <p>
 * Title: AddressPad
 * </p>
 * 
 * <p>
 * Description: AddressPad is a iPhone style send to list.
 * </p>
 * 
 * @author luma
 */
public class AddressPad extends EditText implements FilterListener {
	// some predefined drawing constants
	private static final int						LINE_TOP_PADDING		= 5;
	private static final int						LINE_BOTTOM_PADDING		= 5;
	private static final int						TOP_INNER_PADDING		= 3;
	private static final int						BOTTOM_INNER_PADDING	= 3;
	private static final int						LEFT_PADDING			= 3;
	private static final int						RIGHT_PADDING			= 3;

	// hint view id
	private static final int					HINT_VIEW_ID			= 0x17;

	// spannable string builder, core of styled text supporting
	private SpannableStringBuilder				mBuilder;
	private SpannableSnapshot					mSnapshot;

	// drawing parameters
	private int									mRadius;
	private int									mAscent;
	private int									mDescent;
	private int									mTop;
	private int									mBottom;

	// some configurable parameters
	private String								mTitle;
	private int									mTitleColor;
	private int									mTextColor;
	private int									mSelectedTextColor;
	private Drawable							mAddressBackground;
	private Drawable							mSelectedAddressBackground;
	private Drawable							mAuxiliaryButton;
	private Drawable							mAuxiliaryButtonSelected;
	private int									mAuxiliaryButtonHeight;
	private int									mAuxiliaryButtonWidth;

	// true if you want to show an add button in the right mBottom
	private boolean								mAuxiliaryButtonVisible;
	private boolean								mTouchInButton;

	// true if only allow select one address, default is true
	private boolean								mSingleSelection;
	
	// true if allow duplicated address
	private boolean								mAllowDuplicated;

	// true if you want to keep white space in address string, default is false
	private boolean								mKeepWhitespace;

	// how many address is selected
	private int									mSelectionCount;
	
	// custom input separator
	private String                              mInputSeparator;
	
	// readonly addresspad 
	private boolean                             mReaderMode;

    // if true: the address will be shown as block insdead of text 
    private boolean                             mKeepFullMode;

	// for listeners
	private OnSelectionListener					mOnSelectionListener;
	private OnValidationListener				mOnValidationListener;
	private OnAddressKeyListener				mOnAddressKeyListener;
	private AddressDecorator					mAddressDecorator;

	// track touch location
	private float								mTouchX;
	private float								mTouchY;

	// for auto completion
	private ListAdapter							mDropDownAdapter;
	private Filter								mDropDownFilter;
	private int									mThreshold;
	private PopupWindow							mPopup;
	private DropDownListView					mDropDownList;
	private CharSequence						mHintText;
	private int									mHintResource;
	private Drawable							mDropDownListHighlight;
	private AdapterView.OnItemClickListener		mDropDownItemClickListener;
	private AdapterView.OnItemSelectedListener	mDropDownItemSelectedListener;
	private DropDownItemClickListener			mBuiltInDropDownItemClickListener;
	private String                              mformerText;
	private boolean                             mSilentInputMethod       = false;
	private boolean						        mShowContextMenuOnClick  = true; 
	private boolean                             mPopupContextMenuReady   = false;
    private boolean                             mEnableLongClickDeletion = true;
	// when we keep full mode, we need this snapshot hold the ellipsized addresses and show them as one address span when focus out
    private AddressSpan					        mSingleLineSnapshot = null; 
    private boolean                             mSingleLineSnapshotMode = false;	

	public AddressPad(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public AddressPad(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, android.R.attr.autoCompleteTextViewStyle);
	}

	public AddressPad(Context context) {
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
		// Set Focusable manually
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		// set initial text
		setText("\u0014", BufferType.SPANNABLE);
		mBuilder = (SpannableStringBuilder) getText();
		mBuilder.setSpan(new LinePaddingSpan(), 0, 0, 0);
		mBuilder.setSpan(new TitleSpan(), 0, 1, 0);
		
		// save font metrics and calculate paddings
		TextPaint paint = getPaint();
		FontMetricsInt fmInt = paint.getFontMetricsInt();
		mAscent = fmInt.ascent;
		mDescent = fmInt.descent;
		mTop = fmInt.top;
		mBottom = fmInt.bottom;
		mRadius = (mDescent - mAscent + TOP_INNER_PADDING + BOTTOM_INNER_PADDING) >> 1;
		
		/*
		 * initialize variables which can be load from XML
		 */
		
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.AddressPad);
		mTitleColor = getResources().getColor(R.drawable.default_address_pad_title_color);
		
		mTitle = "";//styledAttrs.getString(R.styleable.AddressPad_titleText);
		if(mTitle == null)
			mTitle = "";//context.getString(android.R.string.default_address_pad_title);
		
		mAddressBackground = styledAttrs.getDrawable(R.styleable.AddressPad_addressBackground);
		if(mAddressBackground == null)
			mAddressBackground = context.getResources().getDrawable(R.drawable.address_pad_address_bg);
		
		mSelectedAddressBackground = styledAttrs.getDrawable(R.styleable.AddressPad_selectedAddressBackground);
		if(mSelectedAddressBackground == null)
			mSelectedAddressBackground = context.getResources().getDrawable(R.drawable.list_selector_background);
		
		//mAuxiliaryButton = styledAttrs.getDrawable(R.styleable.AddressPad_auxiliaryButton);
		//if(mAuxiliaryButton == null)
		//	mAuxiliaryButton = context.getResources().getDrawable(R.drawable.address_pad_add);
		                                                                                  
		mAuxiliaryButtonSelected = styledAttrs.getDrawable(R.styleable.AddressPad_auxiliaryButtonSelected);
		if(mAuxiliaryButtonSelected == null)
//			mAuxiliaryButtonSelected = context.getResources().getDrawable(R.drawable.ic_allapp_add_shortcut);
		
		mKeepWhitespace = styledAttrs.getBoolean(R.styleable.AddressPad_keepWhitespace, false);
		
		mAuxiliaryButtonVisible = styledAttrs.getBoolean(R.styleable.AddressPad_auxiliaryButtonVisible, false);
		mAuxiliaryButtonVisible = false;
        
		mReaderMode = styledAttrs.getBoolean(R.styleable.AddressPad_addresspadCursorVisible, false);
         mSilentInputMethod = mReaderMode ? true : false;
		mKeepFullMode = styledAttrs.getBoolean(R.styleable.AddressPad_keepFullMode, false);
		
		mAllowDuplicated = styledAttrs.getBoolean(R.styleable.AddressPad_allowDuplicated, false);
		
        mShowContextMenuOnClick = styledAttrs.getBoolean(R.styleable.AddressPad_showContextMenuOnClick, true);
        mEnableLongClickDeletion =  styledAttrs.getBoolean(R.styleable.AddressPad_enableLongClickDeletion, true);
        
	    if (styledAttrs.hasValue(R.styleable.AddressPad_inputSeparator)) {
            mInputSeparator = styledAttrs.getString(R.styleable.AddressPad_inputSeparator).trim();
            if ("".equals(mInputSeparator)) {
                mInputSeparator = null;
            }
        }
	      
		mSingleSelection = !styledAttrs.getBoolean(R.styleable.AddressPad_multiSelection, false);
		styledAttrs.recycle();
		
		mTextColor = 
		mSelectedTextColor = getResources().getColor(R.drawable.default_address_pad_selected_text_color);
		styledAttrs.recycle();

		mThreshold = 2;
		mHintText = "";
		mDropDownListHighlight = null;//styledAttrs.getDrawable(com.android.internal.R.styleable.AutoCompleteTextView_dropDownSelector);		
		
		// initialize other variables
		mAuxiliaryButtonWidth =  30;//mAuxiliaryButton.getIntrinsicWidth() > 0 ? mAuxiliaryButton.getIntrinsicWidth() : 30;
		mAuxiliaryButtonHeight = 30;//mAuxiliaryButton.getIntrinsicHeight() > 0 ? mAuxiliaryButton.getIntrinsicHeight() : 30;
		mSelectionCount = 0;
		mOnValidationListener = new DefaultOnValidationListener();
		mPopup = new PopupWindow(context, attrs, android.R.attr.autoCompleteTextViewStyle);
		mBuiltInDropDownItemClickListener = new DropDownItemClickListener();
		mHintResource = R.layout.address_pad_simple_dropdown_hint;
		
		// install default listeners
		setOnLongClickListener(new DefaultOnLongClickListener());

		// initialize padding for add button
		resetPadding();
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);

		if(focused) {
			setFullMode(true);
			int curPos = getSelectionStart();
			if(curPos < 1) {
				setSelection(mBuilder.length());
			}
		} else {
		    dismissDropDown();
			// if there is any pending text, treat it as an address block
			String pendingText = getPendingText();
			if(pendingText.length() > 0) {
				int len = mBuilder.length();
				enterAddresses(len - pendingText.length(), len);
			}

            // TODO(zhoujb): the single line string dos not work well, sometimes it will be eatten by trucate
            // operation, we need make it better.
			if(mSelectionCount > 0) {
				AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
				clearSelection(spans);
				adjustCursorVisibility();
			}

		    setFullMode(false);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(isPopupShowing()) {
			boolean consumed = mDropDownList.onKeyUp(keyCode, event);
			if(consumed) {
				switch(keyCode) {
					/*
					 * if the list accepts the key events and the key event
					 * was a click, the text view gets the mSelected item
					 * from the drop down as its content
					 */
					case KeyEvent.KEYCODE_ENTER:
					case KeyEvent.KEYCODE_DPAD_CENTER:
						performCompletion();
						return true;
				}
			}
		}
		
		/*
		 * If has selection, trigger address key event
		 * If no select, check cursor position and return if it is 1
		 */
		if(mSelectionCount > 0) {
			if(mOnAddressKeyListener != null) {
				String[] addresses = getSelectedAddresses();
				mOnAddressKeyListener.onAddressKeyUp(this, addresses, keyCode, event);
			}
		} else {
			int curPos = getSelectionStart();
			if(curPos < 1) {
			    setSelection(mBuilder.length());
				return true;
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	public void deleteSelectedAddressPan()
    {
        /*
         * if currently has selection, remove the mSelected address if currently
         * no selection, try to find a leading space span at cursor position if
         * leading space span is found, select last address
         */
        if (mSelectionCount > 0) {
            AddressSpan[] selected = getSelectedAddressSpans();
            for (AddressSpan span : selected) {
                span.mSelected = false;
                mSelectionCount--;

                // for span removed, we also trigger a selection event for it
                fireSelectionEvent(span);

                // remove it
                int start = mBuilder.getSpanStart(span);
                int end = mBuilder.getSpanEnd(span);
                mBuilder.removeSpan(span);
                mBuilder.delete(start, end);
            }

            // adjust kinds of status such as selection, cursor, selection count
            setSelection(mBuilder.length());
            adjustCursorVisibility();
        }
    }
	
	public void edit() {
		if (mInputSeparator == null) {
			mInputSeparator = ",";
		}
		String singleLine = getAddresses(mInputSeparator);
		clearAddresses();
		mBuilder.append(singleLine);
		requestFocus();
		setSelection(mBuilder.length());
		mSelectionCount = 0;
		adjustCursorVisibility();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// when the drop down is shown, we drive it directly
		if(isPopupShowing()) {
			/*
			 * special case for the back key, we do not even try to send it
			 * to the drop down list but instead, consume it immediately
			 */
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				dismissDropDown();
				return true;
			} else if(keyCode != KeyEvent.KEYCODE_SPACE) {
				/*
				 * the key events are forwarded to the list in the drop down view
				 * note that ListView handles space but we don't want that to happen
				 */
				boolean consumed = mDropDownList.onKeyDown(keyCode, event);

				if(consumed) {
					switch(keyCode) {
						/*
						 * avoid passing the focus from the text view to the
						 * next component
						 */
						case KeyEvent.KEYCODE_ENTER:
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_DPAD_DOWN:
						case KeyEvent.KEYCODE_DPAD_UP:
							return true;
					}
				} else {
					int index = mDropDownList.getSelectedItemPosition();
					switch(keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if(index == 0) {
								return true;
							}
							break;
							
						/*
						 * when the selection is at the mBottom, we block the
						 * event to avoid going to the next focusable widget
						 */
						case KeyEvent.KEYCODE_DPAD_DOWN:
							Adapter adapter = mDropDownList.getAdapter();
							if(index == adapter.getCount() - 1) {
								return true;
							}
							break;
					}
				}
			}
		} else {
			// check cursor position, 1 is reserved for mTitle
			int curPos = getSelectionStart();
			if(curPos < 1) {
			    setSelection(mBuilder.length());
				return true;
			}

			switch(keyCode) {
				case KeyEvent.KEYCODE_ENTER:
					// get current cursor position and text length
					int length = mBuilder.length();
					
					// if has selection, trigger address key event
					if(mSelectionCount > 0) {
						if(mOnAddressKeyListener != null) {
							mOnAddressKeyListener.onAddressKeyDown(this, getSelectedAddresses(), keyCode, event);
							return true;
						}
					}

					/*
					 * if cursor is at the end of text, then check leading space span
					 * if a leading space span exist at the end, don't insert span again
					 * if no leading space span at the end and non-spanned text is greater
					 * than zero, insert a span
					 */
					if(curPos >= length) {
						AddressSpan[] spaceSpans = mBuilder.getSpans(0, length, AddressSpan.class);
						if(spaceSpans.length == 0) {
							if(length > 1) {
								enterAddresses(1, curPos);
							}
						} else {
							int lastEnd = mBuilder.getSpanEnd(spaceSpans[spaceSpans.length - 1]);
							if(lastEnd < curPos) {
								enterAddresses(lastEnd, curPos);
							}
						}
					}
					return true;
				case KeyEvent.KEYCODE_DEL:
					if(curPos == 0 || mReaderMode)
						return true;

					/*
					 * if currently has selection, remove the mSelected address
					 * if currently no selection, try to find a leading space span at cursor position
					 * if leading space span is found, select last address
					 */
					if(mSelectionCount > 0) {
						AddressSpan[] selected = getSelectedAddressSpans();
						for(AddressSpan span : selected) {
							span.mSelected = false;
							mSelectionCount--;

							// for span removed, we also trigger a selection event for it
							fireSelectionEvent(span);

							// remove it
							int start = mBuilder.getSpanStart(span);
							int end = mBuilder.getSpanEnd(span);
							mBuilder.removeSpan(span);
							mBuilder.delete(start, end);
                            //TODO(zhoujb): remember that the snapshot also contains the same copy,
                            //  we should remove it too, but please look at SpannableSnapshot's 
                            //  implementation, It's very urgly to do such work, for ez, I just rebuild 
                            //  snapshot 
                            buildSnapshot();
						}

						// adjust kinds of status such as selection, cursor, selection count
						setSelection(mBuilder.length());
						adjustCursorVisibility();
						return true;
					} else {
						if(curPos == 1) {
							return true;
						}
		                   
						AddressSpan[] spaceSpans = mBuilder.getSpans(curPos - 1, curPos, AddressSpan.class);
						if(spaceSpans.length > 0) {
							spaceSpans[0].mSelected = true;
							mSelectionCount = 1;
							adjustCursorVisibility();
							fireSelectionEvent(spaceSpans[0]);
							return true;
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
				    // XXX: modify by zhoujb
					if(curPos == 1 || getSelectionEnd() == 1)
						return true;

					/*
					 * if currently has selection, find first selection and select previous address
					 * 
					 * if currently no selection
					 * 1. get end of last address
					 * 2. if cursor exceeds last end, do nothing
					 * 3. if cursor is at the end of last address, select last address
					 */
					AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
					if(spans.length == 0)
						break;
					if(mSelectionCount > 0) {
						for(int i = 0; i < spans.length; i++) {
							if(spans[i].mSelected) {
								if(i > 0) {
									spans[i - 1].mSelected = true;
									spans[i].mSelected = false;

									// fire event
									fireSelectionEvent(spans[i - 1]);
									fireSelectionEvent(spans[i]);
								}

								for(i++; i < spans.length; i++) {
									if(spans[i].mSelected) {
										spans[i].mSelected = false;

										// fire event
										fireSelectionEvent(spans[i]);
									}
								}
							}
						}
						mSelectionCount = 1;
						invalidate();
						return true;
					} else {
						int end = mBuilder.getSpanEnd(spans[spans.length - 1]);
						if(curPos == end) {
							spans[spans.length - 1].mSelected = true;
							mSelectionCount = 1;
							adjustCursorVisibility();
							invalidate();

							// fire event
							fireSelectionEvent(spans[spans.length - 1]);
							return true;
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					/*
					 * if currently has selection, find last selection and select next one
					 * if selected address is last one, cancel selection and show cursor
					 * if currently no selection, no action
					 */
					spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
					if(spans.length == 0)
						break;
					if(mSelectionCount > 0) {
						if(mSelectionCount > 1 || !spans[spans.length - 1].mSelected) {
							for(int i = 0; i < spans.length; i++) {
								if(spans[i].mSelected) {
									if(mSelectionCount == 1) {
										if(i < spans.length - 1) {
											spans[i + 1].mSelected = true;
											spans[i].mSelected = false;

											// fire event
											fireSelectionEvent(spans[i + 1]);
											fireSelectionEvent(spans[i]);

											i++;
										}
									} else {
										spans[i].mSelected = false;

										// fire event
										fireSelectionEvent(spans[i]);
									}
									mSelectionCount--;
								}
							}
							mSelectionCount = 1;
						} else {
							int end = mBuilder.getSpanEnd(spans[spans.length - 1]);
							spans[spans.length - 1].mSelected = false;
							mSelectionCount = 0;
							setSelection(end);
							adjustCursorVisibility();

							// fire event
							fireSelectionEvent(spans[spans.length - 1]);
						}
						invalidate();
						return true;
					}
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
					/*
					 * if currently has selection, find first selection
					 * 1. if first selection is at first line, no action
					 * 2. if first selection is not at first line, get center point and 
					 * subtract line height to get new center point
					 * 3. find the address span according to new center point
					 * 
					 * if currently no selection
					 * 1. get x from offset
					 * 2. subtract line height
					 * 3. find the address span
					 */
					if(mSelectionCount > 0) {
						// get center point
						AddressSpan firstSpan = getFirstSelectedAddressSpan();
						float cX = firstSpan.mInnerBound.centerX();
						float cY = firstSpan.mInnerBound.centerY();
						
						// check line number
						Layout layout = getLayout();
						int line = layout.getLineForVertical((int)cY);
						if(line == 0) {
							View prev = getParent().focusSearch(this, View.FOCUS_UP);
							if(prev != null) {
								prev.requestFocus();
							}
							return true;
						}

						// subtract line height and find
						FontMetricsInt fm = getPaint().getFontMetricsInt();
						cY -= fm.bottom - fm.top;
						spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
						for(AddressSpan span : spans) {
							if(span != firstSpan && span.mInnerBound.contains(cX, cY)) {
								clearSelection(spans);
								span.mSelected = true;
								mSelectionCount = 1;
								invalidate();

								// fire event
								fireSelectionEvent(span);
								return true;
							}
						}

						// if not found, use intersect instead contains
						RectF desiredBound = new RectF(firstSpan.mInnerBound);
						desiredBound.top -= fm.bottom - fm.top;
						desiredBound.bottom -= fm.bottom - fm.top;
						for(AddressSpan span : spans) {
							if(span != firstSpan && span.mInnerBound.intersect(desiredBound)) {
								clearSelection(spans);
								span.mSelected = true;
								mSelectionCount = 1;
								invalidate();

								// fire event
								fireSelectionEvent(span);
								return true;
							}
						}

						// still not found? select last address in previous line
						line--;
						AddressSpan[] prevSpans = mBuilder.getSpans(0, getLayout().getLineEnd(line), AddressSpan.class);
						if(prevSpans.length > 0) {
							clearSelection(spans);
							prevSpans[prevSpans.length - 1].mSelected = true;
							mSelectionCount = 1;
							invalidate();

							// fire event
							fireSelectionEvent(prevSpans[prevSpans.length - 1]);
						} else {
							// impossible, really can execute to here?
						}
						return true;
					} else {
						// check line number
						Layout layout = getLayout();
						int line = layout.getLineForOffset(curPos);
						if(line == 0)
							return true;
						
						float x = layout.getPrimaryHorizontal(curPos);
						int offset = layout.getOffsetForHorizontal(line - 1, x);

						spans = mBuilder.getSpans(0, curPos, AddressSpan.class);
						for(int i = spans.length - 1; i >= 0; i--) {
							int start = mBuilder.getSpanStart(spans[i]);
							int end = mBuilder.getSpanEnd(spans[i]);
							if(spans[i].mInnerBound.contains(x, spans[i].mInnerBound.top) || start <= offset && offset < end) {
								spans[i].mSelected = true;
								mSelectionCount = 1;
								adjustCursorVisibility();
								invalidate();

								// fire event
								fireSelectionEvent(spans[i]);
								return true;
							}
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					/*
					 * if currently has selection
					 * 1. find last selection
					 * 2. get center point
					 * 3. find address at next line
					 * 
					 * if currently no selection, do nothing
					 */

					if(mSelectionCount > 0) {
						AddressSpan lastSpan = getLastSelectedAddressSpan();
						float cX = lastSpan.mInnerBound.centerX();
						float cY = lastSpan.mInnerBound.centerY();
						
						// check line number
						Layout layout = getLayout();
						int line = layout.getLineForVertical((int)cY);
						if(line == layout.getLineCount() - 1) {
							View next = getParent().focusSearch(this, View.FOCUS_DOWN);
							if(next != null) {
								next.requestFocus();								
							}
							return true;
						}

						// add line height and find
						FontMetricsInt fm = getPaint().getFontMetricsInt();
						cY += fm.bottom - fm.top;
						spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
						for(AddressSpan span : spans) {
							if(span != lastSpan && span.mInnerBound.contains(cX, cY)) {
								clearSelection(spans);
								span.mSelected = true;
								mSelectionCount = 1;
								invalidate();

								// fire event
								fireSelectionEvent(span);
								return true;
							}
						}

						// if not found, use intersect instead contains
						RectF desiredBound = new RectF(lastSpan.mInnerBound);
						desiredBound.top += fm.bottom - fm.top;
						desiredBound.bottom += fm.bottom - fm.top;
						for(AddressSpan span : spans) {
							if(span != lastSpan && span.mInnerBound.intersect(desiredBound)) {
								clearSelection(spans);
								span.mSelected = true;
								mSelectionCount = 1;
								invalidate();

								// fire event
								fireSelectionEvent(span);
								return true;
							}
						}

						// still not found? try to select same offset in next line
						line++;
						int offset = layout.getOffsetForHorizontal(line, cX);
						AddressSpan[] nextSpans = mBuilder.getSpans(layout.getLineStart(line), layout.getLineEnd(line), AddressSpan.class);
						for(AddressSpan span : nextSpans) {
							int start = mBuilder.getSpanStart(span);
							int end = mBuilder.getSpanEnd(span);
							if(start <= offset && offset < end) {
								clearSelection(spans);
								span.mSelected = true;
								mSelectionCount = 1;
								invalidate();

								// fire event
								fireSelectionEvent(span);
								return true;
							}
						}

						// still not found, just clear selection
						clearSelection(spans);
						setSelection(offset);
						adjustCursorVisibility();
						invalidate();
						return true;
					}
					break;
				default:
					// don't accept some keys if some addresses are selected
					if(mSelectionCount > 0) {
					    if(keyCode == KeyEvent.KEYCODE_MENU ||
					            keyCode == KeyEvent.KEYCODE_BACK) {
					        break;
					    } else {
							if(mOnAddressKeyListener != null) {
								mOnAddressKeyListener.onAddressKeyDown(this, getSelectedAddresses(), keyCode, event);
							}
							
							return true;
						}
					}
					break;
			}
		}

		// when text is changed, inserted or deleted, we attempt to show
		// the drop down
//		boolean openBefore = isPopupShowing();
//		String oldText = getPendingText();
//
//		// call super
//		boolean handled = super.onKeyDown(keyCode, event);
//
//		// if the list was open before the keystroke, but closed afterwards,
//		// then something in the keystroke processing (an input filter perhaps)
//		// called performCompletion() and we shouldn't do any more processing.
//		if(openBefore && !isPopupShowing()) {
//			return handled;
//		}
//
//		String text = getPendingText();
//		if(!oldText.equals(text)) {
//			int newCount = text.length();
//
//			// the drop down is shown only when a minimum number of characters
//			// was typed in the text view
//			if(newCount >= mThreshold) {
//				if(mDropDownFilter != null)
//					mDropDownFilter.filter(text, this);
//			} else {
//				// drop down is automatically dismissed when enough characters
//				// are deleted from the text view
//				dismissDropDown();
//				if(mDropDownFilter != null)
//					mDropDownFilter.filter(null);
//			}
//			return true;
//		}
//
//		return handled;
		
		return super.onKeyDown(keyCode, event);
	}

	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // we need a hit test here to avoid selecting title of AddressPad   
                // we always have one TitleSpan
                
                TitleSpan titleSpan = mBuilder.getSpans(0, 1, TitleSpan.class)[0];

                if(titleSpan.size + getPaddingLeft() >= event.getX()) {
                    return true;
                }
                break;
			case MotionEvent.ACTION_UP:
				// hit test for add button first
				if(mTouchInButton && mAuxiliaryButtonVisible && mOnSelectionListener != null) {
					mTouchInButton = false;
					mOnSelectionListener.onButtonClicked(this);
					invalidate();
					return true;
				}
				//XXX(zhoujb): pop up context menue
				if (mPopupContextMenuReady) {                  
					showContextMenu();
					mPopupContextMenuReady = false;
                	}
				break;
			case MotionEvent.ACTION_DOWN:
				mTouchX = event.getX() - getPaddingLeft();
				mTouchY = event.getY() - getPaddingTop();

                
				// hit test for add button first
				mTouchInButton = false;
				if(mAuxiliaryButtonVisible && mOnSelectionListener != null) {
					int left = getMeasuredWidth() - RIGHT_PADDING - mAuxiliaryButtonHeight;
					int top = getMeasuredHeight() - mAuxiliaryButtonHeight
							- ((getLayout().getLineBottom(0) - getLayout().getLineTop(0) - mAuxiliaryButtonHeight) >> 1);
					mTouchInButton = left <= mTouchX && mTouchX < left + mAuxiliaryButtonWidth && top <= mTouchY && mTouchY < top + mAuxiliaryButtonHeight;
					if(mTouchInButton) {
						invalidate(left, top, left + mAuxiliaryButtonWidth, top + mAuxiliaryButtonHeight);
						return true;
					}
				}


				if (!mReaderMode) {
                  		mSilentInputMethod = false;
				}
				// get all address span
				AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);

				// hit test
				boolean foundOne = false;
				/* XXX(zhoujb): BE CAUTION
				* we should catch the event on single line address span snd drop it.
				* such thing can be done by check the globle flag "mSingleLineSnapshotMode", if true, that means
				* the single line snapshot is there, we should make it out of hit testing for
				* avoiding to fire any event on it(single line address is not real address at all, right?)
				*/
				for(int i = mSingleLineSnapshotMode ? 1 : 0; i < spans.length; i++) {
					if(spans[i].mInnerBound.contains(mTouchX, mTouchY)) {
						foundOne = true;
						if(mSingleSelection) {
                            mPopupContextMenuReady = true;
							if(!spans[i].mSelected) {
								for(int j = i + 1; j < spans.length; j++) {
									if(spans[j].mSelected) {
										spans[j].mSelected = false;
										mSelectionCount--;
										// fire event
										fireSelectionEvent(spans[j]);
										break;
									}
								}

								spans[i].mSelected = true;
								mSelectionCount++;

								// fire event
								fireSelectionEvent(spans[i]);
							}
							mSilentInputMethod = true;
                            //BORQS_EXT comment
                            //TODO: invoke right interface for this line in CupCake
							//InputMethodManager.finishInput(this);
						} else {
							spans[i].mSelected = !spans[i].mSelected;
							mSelectionCount += spans[i].mSelected ? 1 : -1;

							// fire event
							fireSelectionEvent(spans[i]);
						}

						break;
					} else {
						if(mSingleSelection) {
							if(spans[i].mSelected) {
								spans[i].mSelected = false;
								mSelectionCount--;

								// fire event
								fireSelectionEvent(spans[i]);
							}
						}
					}
				}

				if(!foundOne && !mSingleSelection)
					clearSelection(spans);

				break;
		}

		boolean ret = super.onTouchEvent(event);
		adjustCursorVisibility();
		if(mSelectionCount == 0) {
			if(!isCursorInPendingText())
				setSelection(mBuilder.length());
		}
		return ret;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// draw add button
		if(/*mAuxiliaryButtonVisible*/false) {
			Drawable button = mTouchInButton ? mAuxiliaryButtonSelected : mAuxiliaryButton;
			int left = getMeasuredWidth() - RIGHT_PADDING - mAuxiliaryButtonHeight;
			int top = getMeasuredHeight() - mAuxiliaryButtonHeight - ((getLayout().getLineBottom(0) - getLayout().getLineTop(0) - mAuxiliaryButtonHeight) >> 1);
			button.setBounds(left, top, left + mAuxiliaryButtonWidth, top + mAuxiliaryButtonHeight);
			button.draw(canvas);
		}
	}

	/**
	 * Trigger selection event
	 * 
	 * @param span the address span mSelected or unselected
	 */
	private void fireSelectionEvent(AddressSpan span) {
		if(mOnSelectionListener != null)
			mOnSelectionListener.onAddressSelectionChanged(this, getAddress(span), span.mSelected);
	}

	/**
	 * Change visibility of cursor
	 */
	private void adjustCursorVisibility() {
	    if (mReaderMode) {
	        setCursorVisible(false);
	    } else {
	        setCursorVisible(mSelectionCount == 0);
	    }
	}

	/**
	 * Check whether cursor is after last address block
	 * 
	 * @return true if cursor is after last address block
	 */
	private boolean isCursorInPendingText() {
		AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);

		// first is mTitle, should skip
		int start = 1;
		if(spans.length > 0)
			start = mBuilder.getSpanEnd(spans[spans.length - 1]);

		int cursor = getSelectionStart();
		return start <= cursor;
	}

	/**
	 * Remove whitespace between specified range
	 * 
	 * @param start start offset to search whitespace
	 * @param end end offset to end searching, exclusive
	 * @return count of whitespace removed
	 */
	private int clearWhitespace(int start, int end) {
		int wsCount = 0;
		char[] chars = getAddressChars(start, end);
		for(int i = chars.length - 1; i >= 0; i--) {
			if(chars[i] == ' ') {
				wsCount++;
				mBuilder.delete(start + i, start + i + 1);
			}
		}
		return wsCount;
	}

	/**
	 * <p>Remove all addresses</p>
	 */
	private void clearAddresses() {
		// reset builder
		AddressSpan[] spans = mBuilder.getSpans(1, mBuilder.length(), AddressSpan.class);
		for(AddressSpan span : spans) {
			mBuilder.removeSpan(span);
		}
		mBuilder.delete(1, mBuilder.length());
		
		mSnapshot = null;
	}

	/**
	 * Get mSelected address spans
	 * 
	 * @return array of mSelected address spans
	 */
	private AddressSpan[] getSelectedAddressSpans() {
		AddressSpan[] selected = new AddressSpan[mSelectionCount];
		AddressSpan[] all = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
		for(int i = 0, j = 0; i < all.length && j < mSelectionCount; i++) {
			if(all[i].mSelected)
				selected[j++] = all[i];
		}
		return selected;
	}

	/**
	 * Clear selection
	 * 
	 * @param spans array of address span
	 */
	private void clearSelection(AddressSpan[] spans) {
		clearSelection(spans, true);
	}

	/**
	 * Clear selection
	 * 
	 * @param spans array of address span
	 * @param fireEvent true if should trigger selection event
	 */
	private void clearSelection(AddressSpan[] spans, boolean fireEvent) {
		for(AddressSpan span : spans) {
			if(span.mSelected) {
				span.mSelected = false;

				// fire event
				if(fireEvent)
					fireSelectionEvent(span);
			}
		}
		mSelectionCount = 0;
	}

	/**
	 * Get first mSelected address block
	 * 
	 * @return first address span mSelected, or null if no selection
	 */
	private AddressSpan getFirstSelectedAddressSpan() {
		AddressSpan[] all = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
		for(int i = 0; i < all.length; i++) {
			if(all[i].mSelected) {
				return all[i];
			}
		}
		return null;
	}

	/**
	 * Get last mSelected address block
	 * 
	 * @return last address span mSelected, or null if no selection
	 */
	private AddressSpan getLastSelectedAddressSpan() {
		AddressSpan[] all = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
		for(int i = all.length - 1; i >= 0; i--) {
			if(all[i].mSelected) {
				return all[i];
			}
		}
		return null;
	}

    /**
     * <p>Make one or more address from specified range. If the new address is valid, then
     * it is added. If it is not valid, the range of character will be removed.</p>
     * 
     * @param start the start offset of address string
     * @param end the end offset of address string
     */
    private void enterAddresses(int start, int end) {
        if(mInputSeparator == null) {
            enterAddress(start, end);
        } else {
            String address = getAddress(start, end);
            int sepLen = mInputSeparator.length();
            int from = 0;
            int deleteFrom = start;
            int index = address.indexOf(mInputSeparator);
            while(index != -1) {
                // enter an address
                int delta = enterAddress(deleteFrom, deleteFrom + index - from);
                
                // delete input separator from spannable string
                deleteFrom += index - from + delta;
                mBuilder.delete(deleteFrom, deleteFrom + sepLen);
                
                // find next input separator
                from = index + sepLen;
                index = address.indexOf(mInputSeparator, from);
            }
            
            // last segment
            if(from < address.length()) {
                enterAddress(deleteFrom, deleteFrom + address.length() - from);
            }
        }
    }
    
    /**
     * <p>Create an address in specified range.</p>
     * 
     * @param start start offset to create address
     * @param end end offset to create address, exclusive
     * @return Any number. If returned number is positive, it means some 
     *      characters are inserted. If returned number is negative, it
     *      means some characters are removed.
     */
    private int enterAddress(int start, int end) {
        // length must be larger than 1
        if(end <= start) {
            return 0;
        }
        
        // remove whitespace or not
        int delta = 0;
        if(!mKeepWhitespace) {
            delta = -clearWhitespace(start, end);
            end += delta;
        }
        
        // get address string
        String address = getAddress(start, end);
        
        // valid it
        CharSequence validAddress = null;
        if(mOnValidationListener != null) {
            validAddress = mOnValidationListener.onValidation(this, address);
        }

        // add it or remove it
        boolean valid = validAddress != null && !"".equals(validAddress);
        if(valid) {
            if(address.equals(validAddress)) {
                //XXX(zhoujb): attacted AddressSpan here
                mBuilder.setSpan(new AddressSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                delta += validAddress.length() - end + start;
                mBuilder.replace(start, end, validAddress);
                //XXX(zhoujb): attacted AddressSpan here
                mBuilder.setSpan(new AddressSpan(), start, start + validAddress.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            delta -= end - start;
            mBuilder.delete(start, end);
        }

        // return
        return delta;
    }
    
	/**
	 * Save current state of spannable string mBuilder to a mSnapshot object so
	 * that we can restore it later. We don't save mTitle placeholder
	 */
	private void buildSnapshot() {
		int length = mBuilder.length();
		mSnapshot = new SpannableSnapshot();
		mSnapshot.chars = new char[length];
		mBuilder.getChars(1, length, mSnapshot.chars, 1);
		mSnapshot.spans = mBuilder.getSpans(1, length, AddressSpan.class);
		mSnapshot.spanStarts = new int[mSnapshot.spans.length];
		mSnapshot.spanEnds = new int[mSnapshot.spans.length];
		for(int i = 0; i < mSnapshot.spans.length; i++) {
			mSnapshot.spanStarts[i] = mBuilder.getSpanStart(mSnapshot.spans[i]);
			mSnapshot.spanEnds[i] = mBuilder.getSpanEnd(mSnapshot.spans[i]);
		}
	}

	/**
	 * Restore all spans in mSnapshot
	 * 
	 * @param snapshot mSnapshot object
	 */
	private void restoreSnapshot(SpannableSnapshot snapshot) {
		mBuilder.replace(1, mBuilder.length(), new String(snapshot.chars, 1, snapshot.chars.length - 1));
		for(int i = 0; i < snapshot.spans.length; i++) {
			mBuilder.setSpan(snapshot.spans[i], snapshot.spanStarts[i], snapshot.spanEnds[i], 0);
		}
	}

	/**
	 * Delete all content except mTitle
	 * 
	 * @param snapshot the mSnapshot saved before
	 */
	private void clearSpannable(SpannableSnapshot snapshot) {
		for(AddressSpan style : snapshot.spans) {
			mBuilder.removeSpan(style);
		}
		mBuilder.delete(1, mBuilder.length());
	}
    
    private  boolean isNeedSinglelineSnpshot(SpannableSnapshot snapshot) {
	    // first ensure at least one span
        if(snapshot == null || snapshot.spans.length == 0) {
            return false;
        }    
    
		// get mTitle width
		TitleSpan[] titleSpans = mBuilder.getSpans(0, 1, TitleSpan.class);
		int titleWidth = titleSpans[0].size;

		// get max width
		int maxWidth = getMeasuredWidth() - titleWidth - RIGHT_PADDING - LEFT_PADDING - getPaddingRight() - getPaddingLeft();
		if(maxWidth <= 0) {
		    maxWidth = Math.max(maxWidth, 320);
		}

        AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
        int snapshotWidth = 0;
        for(AddressSpan as : spans) {
            snapshotWidth += as.getSize();
        }
        
        Log.i("xxx", "snapshotWidth = " + snapshotWidth +  "max = " + maxWidth);
        
        return snapshotWidth > maxWidth;
    }

	/**
	 * Build a string which contains all addresses in single line. If the width
	 * is not enough, text will be ellipsized at the end.
	 * 
	 * @param snapshot build single line string based on a mSnapshot
	 * @return single line address string
	 */
	private CharSequence buildSingleLineString(SpannableSnapshot snapshot) {
	    // first ensure at least one span
        if(snapshot == null || snapshot.spans.length == 0) {
            return "";
        }
	    
		// get mTitle width
		TitleSpan[] titleSpans = mBuilder.getSpans(0, 1, TitleSpan.class);
		int titleWidth = titleSpans[0].size;

		// get max width
		int maxWidth = getMeasuredWidth() - titleWidth - RIGHT_PADDING - getPaddingRight() - getPaddingLeft();
		if(maxWidth <= 0) {
		    maxWidth = Math.max(maxWidth, 320);
		}
		
		// construct an address array because user may set a decorator
		CharSequence[] addresses = new CharSequence[snapshot.spans.length];
		for(int i = 0; i < snapshot.spans.length; i++) {
			if(mAddressDecorator == null) {
				addresses[i] = new String(snapshot.chars, snapshot.spanStarts[i], snapshot.spanEnds[i] - snapshot.spanStarts[i]);
			} else {
				addresses[i] = mAddressDecorator.decorateAddress(this, 
						new String(snapshot.chars, snapshot.spanStarts[i], snapshot.spanEnds[i] - snapshot.spanStarts[i]), false);
			}
		}

		// special check for 1 address
		TextPaint paint = getPaint();
		if(snapshot.spans.length == 1) {
		    return TextUtils.ellipsize(addresses[0], paint, maxWidth, TruncateAt.END).toString();
		}

		// destination string looks like " address1, address2 & 4 more..."
		StringBuilder buffer = new StringBuilder();
		buffer.append(' ');
		for(int i = 0; i < snapshot.spans.length; i++) {
			buffer.append(addresses[i]);
			if(i < snapshot.spans.length - 1) {
				buffer.append(", ");
			}
		}
		
		// delete from last until the width is not larger than max width
		int deleteFrom = buffer.length() - addresses[snapshot.spans.length - 1].length() - 2;
		for(int i = snapshot.spans.length - 2, more = 1; i >= -1; i--, more++) {
			if(paint.measureText(buffer.toString()) > maxWidth) {
				int len = i == -1 ? 0 : addresses[i].length();
				
				buffer.delete(deleteFrom, buffer.length());
				
				if(i != -1) {
					buffer.append(" &");
				}
				buffer.append(' ').append(more).append(" more...");
				
				deleteFrom -= len;
				if(i > 0) {
					deleteFrom -= 2;
				} else {
					deleteFrom--;
				}
			} else {
				break;
			}
		}
		
		// still larger?
		if(paint.measureText(buffer.toString()) > maxWidth) {
			return TextUtils.ellipsize(buffer.toString(), paint, maxWidth, TruncateAt.END).toString();
		}

		return buffer.toString();
	}

    /**
     * Change padding according to some flags
     */
    private void resetPadding() {
        if (mAuxiliaryButtonVisible) {
            setPadding(getPaddingLeft(), getPaddingTop(), mAuxiliaryButtonWidth + RIGHT_PADDING
                    + getPaddingRight(), getPaddingBottom());
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    /**
     * <p>
     * Displays the drop down on screen.
     * </p>
     */
	private void showDropDown() {
		int height = buildDropDown();
		mPopup.setHeight(height);
		mPopup.setWidth(getMeasuredWidth() - /*mPaddingLeft*/5  + LEFT_PADDING + RIGHT_PADDING);
		mPopup.showAsDropDown(this, 0, 0);
	}

	/**
	 * <p>
	 * Builds the mPopup window's content and returns the height the mPopup should
	 * have. Returns -1 when the content already exists.
	 * </p>
	 * 
	 * @return the content's height or -1 if content already exists
	 */
	private int buildDropDown() {
		ViewGroup dropDownView;
		int otherHeights = 0;

		if(mDropDownList == null) {
			Context context = getContext();

			mDropDownList = new DropDownListView(context);
			//mDropDownList.setSelector(mDropDownListHighlight);
			mDropDownList.setAdapter(mDropDownAdapter);
			mDropDownList.setVerticalFadingEdgeEnabled(true);
			mDropDownList.setOnItemClickListener(mBuiltInDropDownItemClickListener);

			if(mDropDownItemSelectedListener != null) {
				mDropDownList.setOnItemSelectedListener(mDropDownItemSelectedListener);
			}

			dropDownView = mDropDownList;

			View hintView = getHintView(context);
			if(hintView != null) {
				// if an hint has been specified, we accomodate more space for it and
				// add a text view in the drop down menu, at the mBottom of the list
				LinearLayout hintContainer = new LinearLayout(context);
				hintContainer.setOrientation(LinearLayout.VERTICAL);

				LinearLayout.LayoutParams dropDownParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 0, 1.0f);
				LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				hintContainer.addView(dropDownView, dropDownParams);
				hintContainer.addView(hintView, hintParams);

				// measure the hint's height to find how much more vertical space
				// we need to add to the drop down's height
				int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST);
				int heightSpec = MeasureSpec.UNSPECIFIED;
				hintView.measure(widthSpec, heightSpec);

				otherHeights = hintView.getMeasuredHeight() + hintParams.topMargin + hintParams.bottomMargin;

				dropDownView = hintContainer;
			}

			mPopup.setContentView(dropDownView);
		} else {
			dropDownView = (ViewGroup) mPopup.getContentView();
			View view = dropDownView.findViewById(HINT_VIEW_ID);
			if(view != null) {
				LinearLayout.LayoutParams hintParams = (LinearLayout.LayoutParams) view.getLayoutParams();
				otherHeights = view.getMeasuredHeight() + hintParams.topMargin + hintParams.bottomMargin;
			}
		}

		// Max height available on the screen for a mPopup anchored to us
		int maxHeight = mPopup.getMaxAvailableHeight(this);
		otherHeights += dropDownView.getPaddingTop() + dropDownView.getPaddingBottom();
		return mDropDownList._measureHeightOfChildren(MeasureSpec.UNSPECIFIED, 0, 2, maxHeight - otherHeights, 2) + otherHeights;
	}

	/**
	 * <p>
	 * Performs the text completion by converting the mSelected item from the
	 * drop down list into a string, replacing the text box's content with this
	 * string and finally dismissing the drop down menu.
	 * </p>
	 */
	public void performCompletion() {
		performCompletion(null, -1, -1);
	}

	private void performCompletion(View selectedView, int position, long id) {
	    
		if(isPopupShowing()) {
			Object selectedItem;
			if(position == -1) {
				selectedItem = mDropDownList.getSelectedItem();
			} else {
				selectedItem = mDropDownAdapter.getItem(position);
			}
			
			dismissDropDown();
			
			replaceText(convertSelectionToString(selectedItem));

			if(mDropDownItemClickListener != null) {
				final DropDownListView list = mDropDownList;

				if(selectedView == null || position == -1) {
					selectedView = list.getSelectedView();
					position = list.getSelectedItemPosition();
					id = list.getSelectedItemId();
				}
				mDropDownItemClickListener.onItemClick(list, selectedView, position, id);
			}
		}

	}

	/**
	 * <p>
	 * Performs the text completion by replacing the current text by the
	 * mSelected item. Subclasses should override this method to avoid replacing
	 * the whole content of the edit box.
	 * </p>
	 * 
	 * @param text the mSelected suggestion in the drop down list
	 */
	private void replaceText(CharSequence text) {
		AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);

		// first is mTitle, should skip
		int start = 1;
		if(spans.length > 0) {
			start = mBuilder.getSpanEnd(spans[spans.length - 1]);
		}

		// replace
		mBuilder.replace(start, mBuilder.length(), text);
		
	}

	/**
	 * <p>
	 * Closes the drop down if present on screen.
	 * </p>
	 */
	private void dismissDropDown() {
		mPopup.dismiss();
	}

	/**
	 * <p>
	 * Indicates whether the mPopup menu is showing.
	 * </p>
	 * 
	 * @return true if the mPopup menu is showing, false otherwise
	 */
	public boolean isPopupShowing() {
		return mPopup.isShowing();
	}

	/**
	 * <p>
	 * Converts the mSelected item from the drop down list into a sequence of
	 * character that can be used in the edit box.
	 * </p>
	 * 
	 * @param selectedItem the item mSelected by the user for completion
	 * 
	 * @return a sequence of characters representing the mSelected suggestion
	 */
	protected CharSequence convertSelectionToString(Object selectedItem) {
		return mDropDownFilter.convertResultToString(selectedItem);
	}

	/**
	 * Get hint view
	 * 
	 * @param context context
	 * @return hint view object
	 */
	private View getHintView(Context context) {
		if(mHintText != null && mHintText.length() > 0) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final TextView hintView = (TextView) inflater.inflate(mHintResource, null).findViewById(android.R.id.text1);
			hintView.setText(mHintText);
			hintView.setId(HINT_VIEW_ID);
			return hintView;
		} else {
			return null;
		}
	}
	
	/**
	 * Hit test. Find address span by location
	 * 
	 * @param x x location
	 * @param y y location
	 * @return address span, or null if not found
	 */
	private AddressSpan hitTest(float x, float y) {
		// get all address span
		AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);

		// hit test
		for(AddressSpan span : spans) {
			if(span.mInnerBound.contains(x, y)) {
				return span;
			}
		}
		return null;
	}
	
	/**
	 * Get address string from an address span
	 * 
	 * @param span address span
	 * @return address string
	 */
	private String getAddress(AddressSpan span) {
		if(span == null) {
			return "";
		}
		
		int start = mBuilder.getSpanStart(span);
		int end = mBuilder.getSpanEnd(span);
		return getAddress(start, end);
	}
	
	/**
	 * Get character array according to range
	 * 
	 * @param start start offset
	 * @param end end offset
	 * @return char array
	 */
	private char[] getAddressChars(int start, int end) {
		char[] chars = new char[end - start];
		mBuilder.getChars(start, end, chars, 0);
		return chars;
	}
	
	/**
	 * Get address string according to range
	 * 
	 * @param start start offset
	 * @param end end offset
	 * @return string
	 */
	private String getAddress(int start, int end) {
		if (end > start) {
			char[] chars = new char[end - start];
			mBuilder.getChars(start, end, chars, 0);
			return new String(chars);
		} else {
			return "";
		}
	}

	/**
	 * Get address string which is in inputting but not decorated as an address
	 * block
	 * 
	 * @return the address string user is inputting
	 */
	public String getPendingText() {
		AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);

		// first is mTitle, should skip
		int start = 1;
		if(spans.length > 0) {
			start = mBuilder.getSpanEnd(spans[spans.length - 1]);
		}

		// create pending string
		int len = mBuilder.length() - start;
		if(len <= 0) {
			return "";
		} else {
			return getAddress(start, mBuilder.length());
		}
	}

	/**
     * <p>Get all addresses entered as an array</p>
     * 
     * @return an array of address string
     */
    public String[] getAddresses() {
        boolean focused = hasFocus();
        if(!focused && mSnapshot == null) {
            return new String[0];
        }
        
        char[] chars = null;
        AddressSpan[] spans = focused ? mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class)
                : mSnapshot.spans;
        String[] addresses = new String[spans.length];
        
        for(int i = 0; i < spans.length; i++) {
            int start = focused ? mBuilder.getSpanStart(spans[i]) : mSnapshot.spanStarts[i];
            int end = focused ? mBuilder.getSpanEnd(spans[i]) : mSnapshot.spanEnds[i];
            if(chars == null || end - start > chars.length) {
                chars = new char[end - start];
            }
            if(focused) {
                mBuilder.getChars(start, end, chars, 0);
                addresses[i] = new String(chars, 0, end - start);
            } else {
                addresses[i] = new String(mSnapshot.chars, start, end - start);
            }
        }
        return addresses;
    }
    
    /**
     * <p>Get all selected addresses as an array</p>
     * 
     * @return an array of selected address strings
     */
    public String[] getSelectedAddresses() {
        boolean focused = hasFocus();
        if(!focused && mSnapshot == null) {
            return new String[0];
        }
        
        char[] chars = null;
        AddressSpan[] spans = focused ? mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class)
                : mSnapshot.spans;
        String[] addresses = new String[mSelectionCount];
        
        for(int i = 0, j = 0; i < spans.length && j < mSelectionCount; i++) {
            if(spans[i].mSelected) {
                int start = focused ? mBuilder.getSpanStart(spans[i]) : mSnapshot.spanStarts[i];
                int end = focused ? mBuilder.getSpanEnd(spans[i]) : mSnapshot.spanEnds[i];
                if(chars == null || end - start > chars.length) {
                    chars = new char[end - start];
                }
                if(focused) {
                    mBuilder.getChars(start, end, chars, 0);
                    addresses[j++] = new String(chars, 0, end - start);
                } else {
                    addresses[j++] = new String(mSnapshot.chars, start, end - start);
                }
            }
        }
        return addresses;
    }
    
    /**
     * <p>Get all addresses entered as a single string separated by specified
     * delimiter</p>
     * 
     * @param delimiter delimiter to separate addresses
     * @return a single string of address string, which is separated
     *      by <code>delimiter</code>
     */
    public String getAddresses(String delimiter) {
        boolean focused = hasFocus();
        if(!focused && mSnapshot == null) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        char[] chars = null;
        AddressSpan[] spans = focused ? mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class)
                : mSnapshot.spans;
        
        for(int i = 0; i < spans.length; i++) {
            int start = focused ? mBuilder.getSpanStart(spans[i]) : mSnapshot.spanStarts[i];
            int end = focused ? mBuilder.getSpanEnd(spans[i]) : mSnapshot.spanEnds[i];
            if(chars == null || end - start > chars.length) {
                chars = new char[end - start];
            }
            if(focused) {
                mBuilder.getChars(start, end, chars, 0);
                builder.append(chars, 0, end - start);
            } else {
                builder.append(mSnapshot.chars, start, end - start);
            }
            if(i < spans.length - 1) {
                builder.append(delimiter);
            }
        }
        
        if (focused) {
            String pendingText = getPendingText();
            if(pendingText.length() > 0) {
                if (spans.length > 0) {
                    builder.append(delimiter);
                }
                builder.append(pendingText);
            }
        }
        
        return builder.toString();
    }
    
    /**
     * <p>Get all selected addresses entered as a single string separated by specified
     * delimiter</p>
     * 
     * @param delimiter delimiter to separate addresses
     * @return a single string of selected address string, which is separated
     *      by <code>delimiter</code>
     */
    public String getSelectedAddresses(String delimiter) {
        boolean focused = hasFocus();
        if(!focused && mSnapshot == null) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        char[] chars = null;
        AddressSpan[] spans = focused ? mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class)
                : mSnapshot.spans;
        
        for(int i = 0, j = 0; i < spans.length && j < mSelectionCount; i++) {
            if(spans[i].mSelected) {
                int start = focused ? mBuilder.getSpanStart(spans[i]) : mSnapshot.spanStarts[i];
                int end = focused ? mBuilder.getSpanEnd(spans[i]) : mSnapshot.spanEnds[i];
                if(chars == null || end - start > chars.length) {
                    chars = new char[end - start];
                }
                if(focused) {
                    mBuilder.getChars(start, end, chars, 0);
                    builder.append(chars, 0, end - start);
                } else {
                    builder.append(mSnapshot.chars, start, end - start);
                }
                if(j++ < mSelectionCount) {
                    builder.append(delimiter);
                }
            }
        }
        return builder.toString();
    }
	
	/**
	 * <p>Set addresses string, the <code>addresses</code> can be separated
	 * by specified delimiter</p>
	 * 
	 * @param addresses string of addresses
	 * @param delimiter delimiter of addresses
	 */
	public void setAddresses(String addresses, String delimiter) {
		// sanity check
		if(addresses == null) {
			return;
		}
		addresses = addresses.trim();
		if("".equals(addresses)) {
			clearAddresses();
			return;
		}
	
		// split addresses, check delimiter in case it is null
		String[] vals = null;
		if(delimiter == null) {
			vals = new String[] { addresses };
		} else {
			vals = addresses.split(delimiter);
		}
		
		// reset builder
		clearAddresses();
		
		/*
		 * append all addresses first, then install address span
		 * for every address. Be careful, there can be duplicated
		 * address.
		 */
		int from = 1;
        String spaceString = new String(" ");//this is a space string to delimiter two adress
		for(int i = 0; i < vals.length; i++) {
			vals[i] = vals[i].trim() + spaceString;
			mBuilder.append(vals[i]);
		}
        for(String val : vals) {
            int length = val.length();
            int delta = enterAddress(from, length + from);
            from += length + delta;
        }
        
		// reset other status
		mSelectionCount = 0;
		setSelection(mBuilder.length());
		adjustCursorVisibility();
		mSnapshot = null;
	    setFullMode(hasFocus());
	}

	/**
	 * <p>
	 * Changes the list of data used for auto completion. The provided list must
	 * be a filterable list adapter.
	 * </p>
	 * 
	 * @param adapter the adapter holding the auto completion data
	 * 
	 * @see android.widget.Filterable
	 * @see android.widget.ListAdapter
	 */
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		mDropDownAdapter = adapter;
		if(mDropDownAdapter != null) {
			// no inspection unchecked
			mDropDownFilter = ((Filterable) mDropDownAdapter).getFilter();
		} else {
			mDropDownFilter = null;
		}

		if(mDropDownList != null) {
			mDropDownList.setAdapter(mDropDownAdapter);
		}
	}

	/**
	 * Set display mode. In full mode, all addresses are displayed, otherwise
	 * all addresses will be displayed in single line and may be truncated
	 * 
	 * @param b true if full mode, false otherwise
	 */
	public void setFullMode(boolean b) {
        // we need removed single line span before restore the real snapshot
		if (mSingleLineSnapshot != null) {
			mBuilder.removeSpan(mSingleLineSnapshot);
			mSingleLineSnapshotMode = false;
		}
		if(b) {
			if(mSnapshot != null) {
                playAnimation();
				restoreSnapshot(mSnapshot);
			}
		} else {
			playAnimation();
			buildSnapshot();
            CharSequence singleLineText = buildSingleLineString(mSnapshot);

            if(!mKeepFullMode) {
                clearSpannable(mSnapshot);
                mBuilder.append(singleLineText);
            } else {
                String tmp = new String(mSnapshot.chars);
                // XXX(zhoujb): Notic that we need skip the index 0, that's the title line span
                if((singleLineText.length() > 0 && isNeedSinglelineSnpshot(mSnapshot)) || mReaderMode) {
                    clearSpannable(mSnapshot);
                    mBuilder.replace(1,mBuilder.length(),singleLineText);
                    mSingleLineSnapshot = new AddressSpan();
                    mBuilder.setSpan(mSingleLineSnapshot, 1, singleLineText.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mSingleLineSnapshotMode = true;
                }
            }
		}
	}
    /**
     * animate change when swith mode
     */
    private void playAnimation() {
   		if ((getParent() instanceof LinearLayout))
            ((LinearLayout)getParent()).setAnimationCacheEnabled(true);
    }
	/**
	 * Get mTitle string
	 * 
	 * @return mTitle string
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Set mTitle string
	 * 
	 * @param title mTitle string
	 */
	public void setTitle(String title) {
		// check null
		if(title == null) {
			title = "";
		}

		// if old mTitle is same as new mTitle, return
		if(this.mTitle.equals(title)) {
			return;
		}

		// redraw
		invalidate();
	}

	public int getTitleColor() {
		return mTitleColor;
	}

	public void setTitleColor(int titleColor) {
		this.mTitleColor = titleColor;
	}

	public boolean isSingleSelection() {
		return mSingleSelection;
	}

	public void setSingleSelection(boolean singleSelection) {
		this.mSingleSelection = singleSelection;

		// ensure selection count is not larger than 1, and this will not trigger selection event
		if(this.mSingleSelection && mSelectionCount > 1) {
			AddressSpan[] spans = mBuilder.getSpans(0, mBuilder.length(), AddressSpan.class);
			if(spans.length > 1) {
				for(int i = 1; i < spans.length; i++) {
					spans[i].mSelected = false;
					mSelectionCount--;
				}
				invalidate();
			}
		}
	}

	public void setOnSelectionListener(OnSelectionListener l) {
		this.mOnSelectionListener = l;
	}

	public void setOnValidationListener(OnValidationListener l) {
		this.mOnValidationListener = l;
	}

	public OnAddressKeyListener getOnAddressKeyListener() {
		return mOnAddressKeyListener;
	}

	public void setOnAddressKeyListener(OnAddressKeyListener onAddressKeyListener) {
		mOnAddressKeyListener = onAddressKeyListener;
	}

	public void onFilterComplete(int count) {
		if(count > 0) {
			if(hasFocus() && hasWindowFocus()) {
				showDropDown();
			}
		} else {
			dismissDropDown();
		}
	}

	public boolean isKeepWhitespace() {
		return mKeepWhitespace;
	}

	public void setKeepWhitespace(boolean keepWhitespace) {
		this.mKeepWhitespace = keepWhitespace;
	}
	
	public boolean isAllowDuplicated() {
		return mAllowDuplicated;
	}

	public void setAllowDuplicated(boolean allowDuplicated) {
		mAllowDuplicated = allowDuplicated;
	}
	
	public AddressDecorator getAddressDecorator() {
		return mAddressDecorator;
	}

	public void setAddressDecorator(AddressDecorator addressDecorator) {
		mAddressDecorator = addressDecorator;
	}
	
    public String getInputSeparator() {
        return mInputSeparator;
    }

    public void setInputSeparator(String inputSeparator) {
        mInputSeparator = inputSeparator;
    }
	
	/**
	 * <p>
	 * Sets the listener that will be notified when the user clicks an item in
	 * the drop down list.
	 * </p>
	 * 
	 * @param l the item click listener
	 */
	public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
		mDropDownItemClickListener = l;
	}

	/**
	 * <p>
	 * Sets the listener that will be notified when the user selects an item in
	 * the drop down list.
	 * </p>
	 * 
	 * @param l the item mSelected listener
	 */
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener l) {
		mDropDownItemSelectedListener = l;
	}

	/**
	 * <p>
	 * Returns the listener that is notified whenever the user clicks an item in
	 * the drop down list.
	 * </p>
	 * 
	 * @return the item click listener
	 */
	public AdapterView.OnItemClickListener getItemClickListener() {
		return mDropDownItemClickListener;
	}

	/**
	 * <p>
	 * Returns the listener that is notified whenever the user selects an item
	 * in the drop down list.
	 * </p>
	 * 
	 * @return the item mSelected listener
	 */
	public AdapterView.OnItemSelectedListener getItemSelectedListener() {
		return mDropDownItemSelectedListener;
	}

	/**
	 * <p>
	 * Sets the optional hint text that is displayed at the mBottom of the the
	 * matching list. This can be used as a cue to the user on how to best use
	 * the list, or to provide extra information.
	 * </p>
	 * 
	 * @param hint the text to be displayed to the user
	 * 
	 * @attr ref android.R.styleable#AutoCompleteTextView_completionHint
	 */
	public void setCompletionHint(CharSequence hint) {
		mHintText = hint;
	}

	/**
	 * Get visibility of auxiliary button
	 * 
	 * @return true if auxiliary button is visible, false otherwise
	 */
	public boolean isAuxiliaryButtonVisible() {
		return mAuxiliaryButtonVisible&&false;
	}

	/**
	 * <p>Change visibility of add button</p>
	 * 
	 * @param auxiliaryButtonVisible true if you want add button visible, false
	 *            otherwise
	 */
	public void setAuxiliaryButtonVisible(boolean auxiliaryButtonVisible) {
		auxiliaryButtonVisible = false;
		// TODO seems there is bug in TextView
		// if you call setAddButtonVisible() too fast it will throw a NPE in invalidateCursorPath()
		this.mAuxiliaryButtonVisible = auxiliaryButtonVisible;
		resetPadding();
	}
	
	/**
	 * <p>An address decorator interface. Client can provide a decorator to change
	 * the display string of an address. As its name "Decorator" suggested, it only
	 * change display string, not real address</p>
	 *
	 * @author luma
	 */
	public static interface AddressDecorator {
		/**
		 * </p>Decorate addresses. It only change display string of an address, not real
		 * address. To transform an address, set a custom OnValidationListener</p>
		 * 
		 * @param addressPad AddressPad instance
		 * @param address address to be inputed
		 * @param hasFocus true means address pad has focus, i.e., in a full mode. or
		 * 		false if it does't have focus, i.e., in a single line mode.
		 * @return Decorated address
		 */
		public CharSequence decorateAddress(AddressPad addressPad, CharSequence address, boolean hasFocus);
	}
	
	/**
	 * <p>An interface provides client a chance to validate new address. If validation
	 * listener returns null, then the address will be discarded. Client can also return
	 * a different address string so it provides another chance to transform an address.</p>
	 * 
	 * @author luma
	 */
	public static interface OnValidationListener {
		/**
		 * <p>Invoked when a string will be entered as an address, it will check
		 * address string to decide accept or reject it. It also can return a
		 * different string to transform candidate address.</p>
		 * 
		 * @param addressPad related AddressPad instance
		 * @param address address string to be accepted or rejected
		 * @return A replacement address string. Returns a null string or empty
		 * 		string to deny the candidate address
		 */
		public CharSequence onValidation(AddressPad addressPad, String address);
	}
	
	/**
	 * <p>User may press some keys after select some addresses. This interface provides
	 * a chance to handle such key events. However, it doesn't trigger event for DEL,
	 * LEFT, RIGHT, UP, BOTTOM and BACK keys.</p>
	 *
	 * @author luma
	 */
	public static interface OnAddressKeyListener {
		/**
		 * <p>Invoked when a key up event occurs on some selected addresses</p>
		 * 
		 * @param addressPad related AddressPad instance
		 * @param addresses selected addresses
		 * @param keyCode key code
		 * @param event KeyEvent
		 */
		public void onAddressKeyUp(AddressPad addressPad, String[] addresses, int keyCode, KeyEvent event);
		
		/**
		 * <p>Invoked when a key down event occurs on some selected addresses</p>
		 * 
		 * @param addressPad related AddressPad instance
		 * @param addresses selected addresses
		 * @param keyCode key code
		 * @param event KeyEvent
		 */
		public void onAddressKeyDown(AddressPad addressPad, String[] addresses, int keyCode, KeyEvent event);
	}

	/**
	 * <p>An interface provides address selection notification</p>
	 * 
	 * @author luma
	 */
	public static interface OnSelectionListener {
		/**
		 * <p>Invoked when an address is selected or unselected</p>
		 * 
		 * @param addressPad related AddressPad instance
		 * @param address address string
		 * @param selected true if the address string is mSelected, or false if
		 *            it is unselected
		 */
		public void onAddressSelectionChanged(AddressPad addressPad, String address, boolean selected);
		
		/**
		 * <p>Invoked when long press an address</p>
		 * 
		 * @param addressPad related AddressPad instance
		 * @param address address string
		 */
		public void onAddressLongClicked(AddressPad addressPad, String address);
		
		/**
		 * <p>Invoked when blank area is long pressed</p>
		 * 
		 * @param addressPad related AddressPad instance
		 */
		public void onBlankLongClicked(AddressPad addressPad);
		
		/**
		 * <p>Invoked when auxiliary button is long clicked</p>
		 * 
		 * @param addressPad related AddressPad instance
		 */
		public void onButtonLongClicked(AddressPad addressPad);

		/**
		 * <p>Invoked when right bottom button is clicked</p>
		 * 
		 * @param addressPad related AddressPad instance
		 */
		public void onButtonClicked(AddressPad addressPad);
	}

	/**
	 * <p>Default validation listener. Default implementation just compares new
	 * address with existing addresses case-insensitively.</p>
	 * 
	 * @author luma
	 */
	private class DefaultOnValidationListener implements OnValidationListener {
		public String onValidation(AddressPad addressPad, String address) {
			if(!mAllowDuplicated) {
				String[] addresses = addressPad.getAddresses();
				for(String addr : addresses) {
					if(addr.equalsIgnoreCase(address)) {
						return null;
					}
				}
			}
			return address;
		}
	}

	/**
	 * <p>Address pad will keep a padding between control border and address block.
	 * LinePaddingSpan will adjust text line height.</p>
	 * 
	 * @author luma
	 */
	private class LinePaddingSpan implements LineHeightSpan {
		public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
			fm.top = mTop - LINE_TOP_PADDING;
			fm.bottom = mBottom + LINE_BOTTOM_PADDING;
			fm.ascent = mAscent - LINE_TOP_PADDING;
			fm.descent = mDescent + LINE_BOTTOM_PADDING;
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
		private boolean			mTruncate;
		private boolean			mSelected	= false;
		private RectF			mInnerBound	= new RectF();

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
            if(mSize <= 0) {
                return;
            }
			int offset = (bottom - top - mRadius - mRadius) >> 1;
			mInnerBound.left = x + LEFT_PADDING;
			mInnerBound.top = top + offset;
			mInnerBound.right = x + mSize - RIGHT_PADDING;
			mInnerBound.bottom = bottom - offset;

			// save old state
			int oldColor = paint.getColor();

			// draw background
			Drawable background = mSelected ? mSelectedAddressBackground : mAddressBackground;
			background.setBounds(new Rect((int) mInnerBound.left, 
					(int) mInnerBound.top, (int) mInnerBound.right, (int) mInnerBound.bottom));
			background.draw(canvas);

			/*
			 * restore state and draw text
			 * we need check max text width, truncate if need
			 */
			paint.setColor(mSelected ? mSelectedTextColor : mTextColor);
			if(mTruncate) {
				int maxTextWidth = getMeasuredWidth() - LEFT_PADDING - RIGHT_PADDING
					- mRadius - mRadius - getPaddingRight() - getPaddingLeft();
				CharSequence ellipsized = TextUtils.ellipsize(mDecoratedAddress, 
						(TextPaint) paint, maxTextWidth, TruncateAt.END);
				canvas.drawText(ellipsized, 0, ellipsized.length(), x + LEFT_PADDING + mRadius, 
						top + offset + TOP_INNER_PADDING - mAscent, paint);
			} else {
				canvas.drawText(mDecoratedAddress, 0, mDecoratedAddress.length(), x + LEFT_PADDING + mRadius, 
						top + offset + TOP_INNER_PADDING - mAscent, paint);
			}

			paint.setColor(oldColor);
		}

		@Override
		public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
			// decorate address
			if(mDecoratedAddress == null) {
				mDecoratedAddress = text.subSequence(start, end).toString();
				if(mAddressDecorator != null) {
					mDecoratedAddress = mAddressDecorator.decorateAddress(AddressPad.this, mDecoratedAddress, true);
				}
			}
            // NOTICE(zhoujb): 
            // Do not get max width by calling getMeasuredWidth(), 
            // This should be used during measurement and layout calculations 
            // only. Use getWidth() to see how wide a view is after layout.
			int max = getWidth() - LEFT_PADDING - RIGHT_PADDING - getPaddingRight() - getPaddingLeft();
            // XXX(zhoujb): fbw.#11996 
            if(mMax < 0 && max > 0) {
                mMax = max;
            }
            // since the value of max greater than mMax, we know the screen has
            // been rotated and the width larger than before, in such case
            // return -1 to force a refresh  
            if(mMax > 0 && max > 0 && mMax < max) {
                return -1;
            }
            // ~~

			mSize = (int) paint.measureText(mDecoratedAddress, 0, mDecoratedAddress.length()) 
				+ mRadius + mRadius + LEFT_PADDING + RIGHT_PADDING;
			mTruncate = mSize > max;
			mSize = Math.min(mSize, max);
			return mSize;
		}
        int mMax = -1; 
        public int getSize() {
            return mSize;
        }
	}

	/**
	 * AddressPad can have a mTitle or not, and the mTitle's rendering is handled
	 * by TitleSpan
	 * 
	 * @author luma
	 */
	private class TitleSpan extends ReplacementSpan {
		public int	size;

		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
			int oldColor = paint.getColor();
			paint.setColor(mTitleColor);
			canvas.drawText(mTitle, x + LEFT_PADDING, y, paint);
			paint.setColor(oldColor);
		}

		@Override
		public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
			size = (int) paint.measureText(mTitle) + LEFT_PADDING;
			return size;
		}
	}

	/**
	 * Save important info of a spannable string builder
	 * 
	 * @author luma
	 */
	private static class SpannableSnapshot {
		public char[]			chars;
		public AddressSpan[]	spans;
		public int[]			spanStarts;
		public int[]			spanEnds;
	}

	/**
	 * <p>
	 * Wrapper class for a ListView. This wrapper hijacks the focus to make sure
	 * the list uses the appropriate drawables and states when displayed on
	 * screen within a drop down. The focus is never actually passed to the drop
	 * down; the list only looks focused.
	 * </p>
	 */
	private static class DropDownListView extends ListView {
		/**
		 * <p>
		 * Creates a new list view wrapper.
		 * </p>
		 * 
		 * @param context this view's context
		 */
		public DropDownListView(Context context) {
			super(context);
		}

		
		/**
		 * <p>
		 * Avoids jarring scrolling effect by ensuring that list elements made
		 * of a text view fit on a single line.
		 * </p>
		 * 
		 * @param position the item index in the list to get a view for
		 * @return the view for the specified item
		 */
//		@Override
//		protected View obtainView(int position) {
//			View view = super.obtainView(position);
//
//			if(view instanceof TextView) {
//				((TextView) view).setHorizontallyScrolling(true);
//			}
//
//			return view;
//		}

		/**
		 * <p>
		 * Returns the mTop padding of the currently mSelected view.
		 * </p>
		 * 
		 * @return the height of the mTop padding for the selection
		 */
		public int getSelectionPaddingTop() {
		    return getListPaddingTop();
			//return mSelectionTopPadding;
		}

		/**
		 * <p>
		 * Returns the mBottom padding of the currently mSelected view.
		 * </p>
		 * 
		 * @return the height of the mBottom padding for the selection
		 */
		public int getSelectionPaddingBottom() {
		    return getListPaddingBottom();
			//return  mSelectionBottomPadding;
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always
		 */
		@Override
		public boolean hasWindowFocus() {
			return true;
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always
		 */
		@Override
		public boolean isFocused() {
			return true;
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always
		 */
		@Override
		public boolean hasFocus() {
			return true;
		}

		/*
		 * Copied from ListView
		 */
		public int _measureHeightOfChildren(final int widthMeasureSpec, final int startPosition, int endPosition, final int maxHeight,
				int disallowPartialChildPosition) {
			// Include the padding of the list
			int returnedHeight = getListPaddingTop() + getListPaddingBottom();
			final int dividerHeight = ((getDividerHeight() > 0) && getDivider() != null) ? getDividerHeight() : 0;
			// The previous height value that was less than maxHeight and contained
			// no partial children
			int prevHeightWithoutPartialChild = 0;
			int i;
			View child;
			ViewGroup.LayoutParams lp;

			// mItemCount - 1 since endPosition parameter is inclusive
			endPosition = (endPosition == -1) ? getAdapter().getCount() - 1 : endPosition;
			if (endPosition >= getAdapter().getCount()) {
			    endPosition = getAdapter().getCount()-1;
			}
			for(i = startPosition; i <= endPosition; ++i) {
				child = getAdapter().getView(i, null, this);

				if(i > 0) {
					// Count the divider for all but one child
					returnedHeight += dividerHeight;
				}

				lp = child.getLayoutParams();
				child.measure(widthMeasureSpec, (lp != null && lp.height >= 0) ? MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY) : MeasureSpec
						.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

				returnedHeight += child.getMeasuredHeight();

				if(returnedHeight >= maxHeight) {
					// We went over, figure out which height to return.  If returnedHeight > maxHeight,
					// then the i'th position did not fit completely.
					return (disallowPartialChildPosition >= 0) // Disallowing is enabled (> -1)
							&& (i > disallowPartialChildPosition) // We've past the min pos
							&& (prevHeightWithoutPartialChild > 0) // We have a prev height
							&& (returnedHeight != maxHeight) // i'th child did not fit completely
					? prevHeightWithoutPartialChild : maxHeight;
				}

				if((disallowPartialChildPosition >= 0) && (i >= disallowPartialChildPosition)) {
					prevHeightWithoutPartialChild = returnedHeight;
				}
			}

			// At this point, we went through the range of children, and they each
			// completely fit, so return the returnedHeight
			return returnedHeight;
		}
	}

	private class DropDownItemClickListener implements AdapterView.OnItemClickListener {
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			performCompletion(v, position, id);
		}
	}

	// TODO long click doesn't work now, find out why
	private class DefaultOnLongClickListener implements OnLongClickListener {
		public boolean onLongClick(View view) {
			if(mOnSelectionListener == null) {
				return false;
			}
			
			if(mTouchInButton) {
				mOnSelectionListener.onButtonLongClicked(AddressPad.this);
			} else {
				AddressSpan span = hitTest(mTouchX, mTouchY);
				if(span == null) {
					mOnSelectionListener.onBlankLongClicked(AddressPad.this);
				} else {
					mOnSelectionListener.onAddressLongClicked(AddressPad.this, getAddress(span));
				}
			}
			
			return true;
		}
	}
	
	public void setText(CharSequence text, BufferType type) {
	    if(type != BufferType.SPANNABLE) {
	        return;
	    }
	    super.setText(text, type);
	}

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        if (text.length() <= 1) {
            mformerText = "";
            super.onTextChanged(text, start, before, after);
            return;
        }

        String newText = getPendingText();
        int count = newText.length();

        // the drop down is shown only when a minimum number of characters
        // was typed in the text view
        if (!mformerText.equals(newText)) {
            if (count == mThreshold) {
                if (mDropDownFilter != null) {
                    mDropDownFilter.filter(newText, this);
                }
            } else if (count > mThreshold) {
                if (this.isPopupShowing()) {
                    if (mDropDownFilter != null) {
                        mDropDownFilter.filter(newText, this);
                    }
                }
            } else {
                // drop down is automatically dismissed when enough characters
                // are deleted from the text view
                dismissDropDown();
                if (mDropDownFilter != null)
                    mDropDownFilter.filter(null);
            }
            mformerText = newText;
        }

        super.onTextChanged(text, start, before, after);
    }

    /* (non-Javadoc)
     * @see android.widget.EditText#getDefaultEditable()
     */
    @Override
    protected boolean getDefaultEditable() {
        // Disable input method trigger
        return mSilentInputMethod ? false : true;
    }
    /**
     * TTTTTTTTThis function is for easy debugging ....
     */
    private void D(String msg) {
        String Tag = "xxx";
        Log.i(Tag, msg);
    }

    public boolean performLongClick() {
        if(!mReaderMode && mSelectionCount > 0 && mEnableLongClickDeletion) {
            removeSelectedAddress();
            return true;
        }
        return super.performLongClick();
    }

    public void removeSelectedAddress() {
        if(mSelectionCount > 0 ) {
            mPopupContextMenuReady = false;
            AddressSpan[] selected = getSelectedAddressSpans();
            D("selected: " + selected.length);
            for(AddressSpan span : selected) {
                span.mSelected = false;
                mSelectionCount--;

                // for span removed, we also trigger a selection event for it
                fireSelectionEvent(span);

                // remove it
                int start = mBuilder.getSpanStart(span);
                int end = mBuilder.getSpanEnd(span);
                mBuilder.removeSpan(span);
                mBuilder.delete(start, end);
            }

            // adjust kinds of status such as selection, cursor, selection count
            setSelection(mBuilder.length());
            adjustCursorVisibility();  
        }
    }

}
