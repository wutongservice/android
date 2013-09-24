/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.qiupu.db;

import android.text.TextUtils;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

 

// import  static com.android.internal.util.HanziToPinyin.PINYINS;


/**
 * An object to convert Chinese character to its corresponding pinyin string.
 * For characters with multiple possible pinyin string, only one is selected
 * according to collator. Polyphone is not supported in this implementation.
 * This class is implemented to achieve the best runtime performance and minimum
 * runtime resources with tolerable sacrifice of accuracy. This implementation
 * highly depends on zh_CN ICU collation data and must be always synchronized with
 * ICU.
 *
 * Currently this file is aligned to zh.txt in ICU 4.6
 */
public class HanziToPinyin {
    private static final String TAG = "HanziToPinyin";

    // Turn on this flag when we want to check internal data structure.
    private static final boolean DEBUG = false;

    /**
     * Unihans array. Each unihans is the first one within same pinyin. Use it to determine pinyin
     * for all ~20k unihans.
     */
    public static final char[] UNIHANS = {
            '\u5475', '\u54ce', '\u5b89', '\u80ae', '\u51f9',
            '\u516b', '\u6300', '\u6273', '\u90a6', '\u5305', '\u5351', '\u5954', '\u4f3b',
            '\u5c44', '\u8fb9', '\u6807', '\u618b', '\u90a0', '\u69df', '\u7676', '\u5cec',
            '\u5693', '\u5a47', '\u98e1', '\u4ed3', '\u64cd', '\u518a', '\u5d7e', '\u564c',
            '\u53c9', '\u9497', '\u8fbf', '\u4f25', '\u6284', '\u8f66', '\u62bb', '\u67fd',
            '\u5403', '\u5145', '\u62bd', '\u51fa', '\u6b3b', '\u63e3', '\u5ddd', '\u75ae',
            '\u5439', '\u6776', '\u9034', '\u75b5', '\u5306', '\u51d1', '\u7c97', '\u6c46',
            '\u5d14', '\u90a8', '\u6413', '\u5491', '\u5927', '\u75b8', '\u5f53', '\u5200',
            '\u6dc2', '\u5f97', '\u6265', '\u706f', '\u6c10', '\u55f2', '\u7538', '\u5201',
            '\u7239', '\u4ec3', '\u4e1f', '\u4e1c', '\u5517', '\u561f', '\u5073', '\u5806',
            '\u9413', '\u591a', '\u5a40', '\u8bf6', '\u5940', '\u97a5', '\u800c', '\u53d1',
            '\u5e06', '\u65b9', '\u98de', '\u5206', '\u4e30', '\u8985', '\u4ecf', '\u7d11',
            '\u4f15', '\u65ee', '\u8be5', '\u7518', '\u5188', '\u768b', '\u6208', '\u7d66',
            '\u6839', '\u5e9a', '\u5de5', '\u52fe', '\u4f30', '\u74dc', '\u7f6b', '\u5173',
            '\u5149', '\u5f52', '\u886e', '\u5459', '\u54c8', '\u54b3', '\u9878', '\u82c0',
            '\u84bf', '\u8bc3', '\u9ed2', '\u62eb', '\u4ea8', '\u5677', '\u543d', '\u9f41',
            '\u5322', '\u82b1', '\u6000', '\u72bf', '\u5ddf', '\u7070', '\u660f', '\u5419',
            '\u4e0c', '\u52a0', '\u620b', '\u6c5f', '\u827d', '\u9636', '\u5dfe', '\u52a4',
            '\u5182', '\u52fc', '\u530a', '\u5a1f', '\u5658', '\u519b', '\u5494', '\u5f00',
            '\u520a', '\u95f6', '\u5c3b', '\u533c', '\u524b', '\u80af', '\u962c', '\u7a7a',
            '\u62a0', '\u5233', '\u5938', '\u84af', '\u5bbd', '\u5321', '\u4e8f', '\u5764',
            '\u6269', '\u5783', '\u6765', '\u5170', '\u5577', '\u635e', '\u4ec2', '\u52d2',
            '\u5844', '\u5215', '\u5006', '\u5941', '\u826f', '\u64a9', '\u5217', '\u62ce',
            '\u3007', '\u6e9c', '\u9f99', '\u779c', '\u565c', '\u5a08', '\u7567', '\u62a1',
            '\u7f57', '\u5463', '\u5988', '\u973e', '\u5ada', '\u9099', '\u732b', '\u9ebc',
            '\u6c92', '\u95e8', '\u753f', '\u54aa', '\u7720', '\u55b5', '\u54a9', '\u6c11',
            '\u540d', '\u8c2c', '\u6478', '\u54de', '\u6bea', '\u62cf', '\u5b7b', '\u56e1',
            '\u56ca', '\u5b6c', '\u8bb7', '\u9981', '\u6041', '\u80fd', '\u59ae', '\u62c8',
            '\u5b22', '\u9e1f', '\u634f', '\u60a8', '\u5b81', '\u599e', '\u519c', '\u7fba',
            '\u5974', '\u597b', '\u8650', '\u632a', '\u5594', '\u8bb4', '\u8db4', '\u62cd',
            '\u7705', '\u4e53', '\u629b', '\u5478', '\u55b7', '\u5309', '\u4e15', '\u504f',
            '\u527d', '\u6c15', '\u59d8', '\u4e52', '\u948b', '\u5256', '\u4ec6', '\u4e03',
            '\u6390', '\u5343', '\u545b', '\u6084', '\u767f', '\u4fb5', '\u9751', '\u909b',
            '\u4e18', '\u66f2', '\u5f2e', '\u7f3a', '\u590b', '\u5465', '\u7a63', '\u5a06',
            '\u60f9', '\u4eba', '\u6254', '\u65e5', '\u8338', '\u53b9', '\u5982', '\u5827',
            '\u6875', '\u95f0', '\u82e5', '\u4ee8', '\u6be2', '\u4e09', '\u6852', '\u63bb',
            '\u8272', '\u68ee', '\u50e7', '\u6740', '\u7b5b', '\u5c71', '\u4f24', '\u5f30',
            '\u5962', '\u7533', '\u5347', '\u5c38', '\u53ce', '\u4e66', '\u5237', '\u6454',
            '\u95e9', '\u53cc', '\u8c01', '\u542e', '\u5981', '\u53b6', '\u5fea', '\u635c',
            '\u82cf', '\u72fb', '\u590a', '\u5b59', '\u5506', '\u4ed6', '\u82d4', '\u574d',
            '\u94f4', '\u5932', '\u5fd1', '\u71a5', '\u5254', '\u5929', '\u4f7b', '\u5e16',
            '\u5385', '\u56f2', '\u5077', '\u92c0', '\u6e4d', '\u63a8', '\u541e', '\u6258',
            '\u6316', '\u6b6a', '\u5f2f', '\u5c2a', '\u5371', '\u586d', '\u7fc1', '\u631d',
            '\u5140', '\u5915', '\u867e', '\u4eda', '\u4e61', '\u7071', '\u4e9b', '\u5fc3',
            '\u661f', '\u51f6', '\u4f11', '\u65f4', '\u8f69', '\u75b6', '\u52cb', '\u4e2b',
            '\u6079', '\u592e', '\u5e7a', '\u8036', '\u4e00', '\u6b2d', '\u5e94', '\u54df',
            '\u4f63', '\u4f18', '\u625c', '\u9e22', '\u66f0', '\u6655', '\u531d', '\u707d',
            '\u7ccc', '\u7242', '\u50ae', '\u5219', '\u8d3c', '\u600e', '\u5897', '\u5412',
            '\u635a', '\u6cbe', '\u5f20', '\u948a', '\u8707', '\u8d1e', '\u4e89', '\u4e4b',
            '\u4e2d', '\u5dde', '\u6731', '\u6293', '\u8de9', '\u4e13', '\u5986', '\u96b9',
            '\u5b92', '\u5353', '\u5b5c', '\u5b97', '\u90b9', '\u79df', '\u94bb', '\u539c',
            '\u5c0a', '\u6628', };

    /**
     * Pinyin array. Each pinyin is corresponding to unihans of same offset in the unihans array.
     */
    public static final byte[][] PINYINS = {
            { 65, 0, 0, 0, 0, 0 }, { 65, 73, 0, 0, 0, 0 }, { 65, 78, 0, 0, 0, 0 },
            { 65, 78, 71, 0, 0, 0 }, { 65, 79, 0, 0, 0, 0 }, { 66, 65, 0, 0, 0, 0 },
            { 66, 65, 73, 0, 0, 0 }, { 66, 65, 78, 0, 0, 0 }, { 66, 65, 78, 71, 0, 0 },
            { 66, 65, 79, 0, 0, 0 }, { 66, 69, 73, 0, 0, 0 }, { 66, 69, 78, 0, 0, 0 },
            { 66, 69, 78, 71, 0, 0 }, { 66, 73, 0, 0, 0, 0 }, { 66, 73, 65, 78, 0, 0 },
            { 66, 73, 65, 79, 0, 0 }, { 66, 73, 69, 0, 0, 0 }, { 66, 73, 78, 0, 0, 0 },
            { 66, 73, 78, 71, 0, 0 }, { 66, 79, 0, 0, 0, 0 }, { 66, 85, 0, 0, 0, 0 },
            { 67, 65, 0, 0, 0, 0 }, { 67, 65, 73, 0, 0, 0 },
            { 67, 65, 78, 0, 0, 0 }, { 67, 65, 78, 71, 0, 0 }, { 67, 65, 79, 0, 0, 0 },
            { 67, 69, 0, 0, 0, 0 }, { 67, 69, 78, 0, 0, 0 }, { 67, 69, 78, 71, 0, 0 },
            { 67, 72, 65, 0, 0, 0 }, { 67, 72, 65, 73, 0, 0 }, { 67, 72, 65, 78, 0, 0 },
            { 67, 72, 65, 78, 71, 0 }, { 67, 72, 65, 79, 0, 0 }, { 67, 72, 69, 0, 0, 0 },
            { 67, 72, 69, 78, 0, 0 }, { 67, 72, 69, 78, 71, 0 }, { 67, 72, 73, 0, 0, 0 },
            { 67, 72, 79, 78, 71, 0 }, { 67, 72, 79, 85, 0, 0 }, { 67, 72, 85, 0, 0, 0 },
            { 67, 72, 85, 65, 0, 0 }, { 67, 72, 85, 65, 73, 0 }, { 67, 72, 85, 65, 78, 0 },
            { 67, 72, 85, 65, 78, 71 }, { 67, 72, 85, 73, 0, 0 }, { 67, 72, 85, 78, 0, 0 },
            { 67, 72, 85, 79, 0, 0 }, { 67, 73, 0, 0, 0, 0 }, { 67, 79, 78, 71, 0, 0 },
            { 67, 79, 85, 0, 0, 0 }, { 67, 85, 0, 0, 0, 0 }, { 67, 85, 65, 78, 0, 0 },
            { 67, 85, 73, 0, 0, 0 }, { 67, 85, 78, 0, 0, 0 }, { 67, 85, 79, 0, 0, 0 },
            { 68, 65, 0, 0, 0, 0 }, { 68, 65, 73, 0, 0, 0 }, { 68, 65, 78, 0, 0, 0 },
            { 68, 65, 78, 71, 0, 0 }, { 68, 65, 79, 0, 0, 0 }, { 68, 69, 0, 0, 0, 0 },
            { 68, 69, 73, 0, 0, 0 }, { 68, 69, 78, 0, 0, 0 }, { 68, 69, 78, 71, 0, 0 },
            { 68, 73, 0, 0, 0, 0 }, { 68, 73, 65, 0, 0, 0 }, { 68, 73, 65, 78, 0, 0 },
            { 68, 73, 65, 79, 0, 0 }, { 68, 73, 69, 0, 0, 0 }, { 68, 73, 78, 71, 0, 0 },
            { 68, 73, 85, 0, 0, 0 }, { 68, 79, 78, 71, 0, 0 }, { 68, 79, 85, 0, 0, 0 },
            { 68, 85, 0, 0, 0, 0 }, { 68, 85, 65, 78, 0, 0 }, { 68, 85, 73, 0, 0, 0 },
            { 68, 85, 78, 0, 0, 0 }, { 68, 85, 79, 0, 0, 0 }, { 69, 0, 0, 0, 0, 0 },
            { 69, 73, 0, 0, 0, 0 }, { 69, 78, 0, 0, 0, 0 }, { 69, 78, 71, 0, 0, 0 },
            { 69, 82, 0, 0, 0, 0 }, { 70, 65, 0, 0, 0, 0 }, { 70, 65, 78, 0, 0, 0 },
            { 70, 65, 78, 71, 0, 0 }, { 70, 69, 73, 0, 0, 0 }, { 70, 69, 78, 0, 0, 0 },
            { 70, 69, 78, 71, 0, 0 }, { 70, 73, 65, 79, 0, 0 }, { 70, 79, 0, 0, 0, 0 },
            { 70, 79, 85, 0, 0, 0 }, { 70, 85, 0, 0, 0, 0 }, { 71, 65, 0, 0, 0, 0 },
            { 71, 65, 73, 0, 0, 0 }, { 71, 65, 78, 0, 0, 0 }, { 71, 65, 78, 71, 0, 0 },
            { 71, 65, 79, 0, 0, 0 }, { 71, 69, 0, 0, 0, 0 }, { 71, 69, 73, 0, 0, 0 },
            { 71, 69, 78, 0, 0, 0 }, { 71, 69, 78, 71, 0, 0 }, { 71, 79, 78, 71, 0, 0 },
            { 71, 79, 85, 0, 0, 0 }, { 71, 85, 0, 0, 0, 0 }, { 71, 85, 65, 0, 0, 0 },
            { 71, 85, 65, 73, 0, 0 }, { 71, 85, 65, 78, 0, 0 }, { 71, 85, 65, 78, 71, 0 },
            { 71, 85, 73, 0, 0, 0 }, { 71, 85, 78, 0, 0, 0 }, { 71, 85, 79, 0, 0, 0 },
            { 72, 65, 0, 0, 0, 0 }, { 72, 65, 73, 0, 0, 0 }, { 72, 65, 78, 0, 0, 0 },
            { 72, 65, 78, 71, 0, 0 }, { 72, 65, 79, 0, 0, 0 }, { 72, 69, 0, 0, 0, 0 },
            { 72, 69, 73, 0, 0, 0 }, { 72, 69, 78, 0, 0, 0 }, { 72, 69, 78, 71, 0, 0 },
            { 72, 77, 0, 0, 0, 0 }, { 72, 79, 78, 71, 0, 0 }, { 72, 79, 85, 0, 0, 0 },
            { 72, 85, 0, 0, 0, 0 }, { 72, 85, 65, 0, 0, 0 }, { 72, 85, 65, 73, 0, 0 },
            { 72, 85, 65, 78, 0, 0 }, { 72, 85, 65, 78, 71, 0 }, { 72, 85, 73, 0, 0, 0 },
            { 72, 85, 78, 0, 0, 0 }, { 72, 85, 79, 0, 0, 0 }, { 74, 73, 0, 0, 0, 0 },
            { 74, 73, 65, 0, 0, 0 }, { 74, 73, 65, 78, 0, 0 }, { 74, 73, 65, 78, 71, 0 },
            { 74, 73, 65, 79, 0, 0 }, { 74, 73, 69, 0, 0, 0 }, { 74, 73, 78, 0, 0, 0 },
            { 74, 73, 78, 71, 0, 0 }, { 74, 73, 79, 78, 71, 0 }, { 74, 73, 85, 0, 0, 0 },
            { 74, 85, 0, 0, 0, 0 }, { 74, 85, 65, 78, 0, 0 }, { 74, 85, 69, 0, 0, 0 },
            { 74, 85, 78, 0, 0, 0 }, { 75, 65, 0, 0, 0, 0 }, { 75, 65, 73, 0, 0, 0 },
            { 75, 65, 78, 0, 0, 0 }, { 75, 65, 78, 71, 0, 0 }, { 75, 65, 79, 0, 0, 0 },
            { 75, 69, 0, 0, 0, 0 }, { 75, 69, 73, 0, 0, 0 }, { 75, 69, 78, 0, 0, 0 },
            { 75, 69, 78, 71, 0, 0 }, { 75, 79, 78, 71, 0, 0 }, { 75, 79, 85, 0, 0, 0 },
            { 75, 85, 0, 0, 0, 0 }, { 75, 85, 65, 0, 0, 0 }, { 75, 85, 65, 73, 0, 0 },
            { 75, 85, 65, 78, 0, 0 }, { 75, 85, 65, 78, 71, 0 }, { 75, 85, 73, 0, 0, 0 },
            { 75, 85, 78, 0, 0, 0 }, { 75, 85, 79, 0, 0, 0 }, { 76, 65, 0, 0, 0, 0 },
            { 76, 65, 73, 0, 0, 0 }, { 76, 65, 78, 0, 0, 0 }, { 76, 65, 78, 71, 0, 0 },
            { 76, 65, 79, 0, 0, 0 }, { 76, 69, 0, 0, 0, 0 }, { 76, 69, 73, 0, 0, 0 },
            { 76, 69, 78, 71, 0, 0 }, { 76, 73, 0, 0, 0, 0 }, { 76, 73, 65, 0, 0, 0 },
            { 76, 73, 65, 78, 0, 0 }, { 76, 73, 65, 78, 71, 0 }, { 76, 73, 65, 79, 0, 0 },
            { 76, 73, 69, 0, 0, 0 }, { 76, 73, 78, 0, 0, 0 }, { 76, 73, 78, 71, 0, 0 },
            { 76, 73, 85, 0, 0, 0 }, { 76, 79, 78, 71, 0, 0 }, { 76, 79, 85, 0, 0, 0 },
            { 76, 85, 0, 0, 0, 0 }, { 76, 85, 65, 78, 0, 0 }, { 76, 85, 69, 0, 0, 0 },
            { 76, 85, 78, 0, 0, 0 }, { 76, 85, 79, 0, 0, 0 }, { 77, 0, 0, 0, 0, 0 },
            { 77, 65, 0, 0, 0, 0 }, { 77, 65, 73, 0, 0, 0 }, { 77, 65, 78, 0, 0, 0 },
            { 77, 65, 78, 71, 0, 0 }, { 77, 65, 79, 0, 0, 0 }, { 77, 69, 0, 0, 0, 0 },
            { 77, 69, 73, 0, 0, 0 }, { 77, 69, 78, 0, 0, 0 }, { 77, 69, 78, 71, 0, 0 },
            { 77, 73, 0, 0, 0, 0 }, { 77, 73, 65, 78, 0, 0 }, { 77, 73, 65, 79, 0, 0 },
            { 77, 73, 69, 0, 0, 0 }, { 77, 73, 78, 0, 0, 0 }, { 77, 73, 78, 71, 0, 0 },
            { 77, 73, 85, 0, 0, 0 }, { 77, 79, 0, 0, 0, 0 }, { 77, 79, 85, 0, 0, 0 },
            { 77, 85, 0, 0, 0, 0 }, { 78, 65, 0, 0, 0, 0 }, { 78, 65, 73, 0, 0, 0 },
            { 78, 65, 78, 0, 0, 0 }, { 78, 65, 78, 71, 0, 0 }, { 78, 65, 79, 0, 0, 0 },
            { 78, 69, 0, 0, 0, 0 }, { 78, 69, 73, 0, 0, 0 }, { 78, 69, 78, 0, 0, 0 },
            { 78, 69, 78, 71, 0, 0 }, { 78, 73, 0, 0, 0, 0 }, { 78, 73, 65, 78, 0, 0 },
            { 78, 73, 65, 78, 71, 0 }, { 78, 73, 65, 79, 0, 0 }, { 78, 73, 69, 0, 0, 0 },
            { 78, 73, 78, 0, 0, 0 }, { 78, 73, 78, 71, 0, 0 }, { 78, 73, 85, 0, 0, 0 },
            { 78, 79, 78, 71, 0, 0 }, { 78, 79, 85, 0, 0, 0 }, { 78, 85, 0, 0, 0, 0 },
            { 78, 85, 65, 78, 0, 0 }, { 78, 85, 69, 0, 0, 0 }, { 78, 85, 79, 0, 0, 0 },
            { 79, 0, 0, 0, 0, 0 }, { 79, 85, 0, 0, 0, 0 }, { 80, 65, 0, 0, 0, 0 },
            { 80, 65, 73, 0, 0, 0 }, { 80, 65, 78, 0, 0, 0 }, { 80, 65, 78, 71, 0, 0 },
            { 80, 65, 79, 0, 0, 0 }, { 80, 69, 73, 0, 0, 0 }, { 80, 69, 78, 0, 0, 0 },
            { 80, 69, 78, 71, 0, 0 }, { 80, 73, 0, 0, 0, 0 }, { 80, 73, 65, 78, 0, 0 },
            { 80, 73, 65, 79, 0, 0 }, { 80, 73, 69, 0, 0, 0 }, { 80, 73, 78, 0, 0, 0 },
            { 80, 73, 78, 71, 0, 0 }, { 80, 79, 0, 0, 0, 0 }, { 80, 79, 85, 0, 0, 0 },
            { 80, 85, 0, 0, 0, 0 }, { 81, 73, 0, 0, 0, 0 }, { 81, 73, 65, 0, 0, 0 },
            { 81, 73, 65, 78, 0, 0 }, { 81, 73, 65, 78, 71, 0 }, { 81, 73, 65, 79, 0, 0 },
            { 81, 73, 69, 0, 0, 0 }, { 81, 73, 78, 0, 0, 0 }, { 81, 73, 78, 71, 0, 0 },
            { 81, 73, 79, 78, 71, 0 }, { 81, 73, 85, 0, 0, 0 }, { 81, 85, 0, 0, 0, 0 },
            { 81, 85, 65, 78, 0, 0 }, { 81, 85, 69, 0, 0, 0 }, { 81, 85, 78, 0, 0, 0 },
            { 82, 65, 78, 0, 0, 0 }, { 82, 65, 78, 71, 0, 0 }, { 82, 65, 79, 0, 0, 0 },
            { 82, 69, 0, 0, 0, 0 }, { 82, 69, 78, 0, 0, 0 }, { 82, 69, 78, 71, 0, 0 },
            { 82, 73, 0, 0, 0, 0 }, { 82, 79, 78, 71, 0, 0 }, { 82, 79, 85, 0, 0, 0 },
            { 82, 85, 0, 0, 0, 0 }, { 82, 85, 65, 78, 0, 0 }, { 82, 85, 73, 0, 0, 0 },
            { 82, 85, 78, 0, 0, 0 }, { 82, 85, 79, 0, 0, 0 }, { 83, 65, 0, 0, 0, 0 },
            { 83, 65, 73, 0, 0, 0 }, { 83, 65, 78, 0, 0, 0 }, { 83, 65, 78, 71, 0, 0 },
            { 83, 65, 79, 0, 0, 0 }, { 83, 69, 0, 0, 0, 0 }, { 83, 69, 78, 0, 0, 0 },
            { 83, 69, 78, 71, 0, 0 }, { 83, 72, 65, 0, 0, 0 }, { 83, 72, 65, 73, 0, 0 },
            { 83, 72, 65, 78, 0, 0 }, { 83, 72, 65, 78, 71, 0 }, { 83, 72, 65, 79, 0, 0 },
            { 83, 72, 69, 0, 0, 0 }, { 83, 72, 69, 78, 0, 0 }, { 83, 72, 69, 78, 71, 0 },
            { 83, 72, 73, 0, 0, 0 }, { 83, 72, 79, 85, 0, 0 }, { 83, 72, 85, 0, 0, 0 },
            { 83, 72, 85, 65, 0, 0 }, { 83, 72, 85, 65, 73, 0 }, { 83, 72, 85, 65, 78, 0 },
            { 83, 72, 85, 65, 78, 71 }, { 83, 72, 85, 73, 0, 0 }, { 83, 72, 85, 78, 0, 0 },
            { 83, 72, 85, 79, 0, 0 }, { 83, 73, 0, 0, 0, 0 }, { 83, 79, 78, 71, 0, 0 },
            { 83, 79, 85, 0, 0, 0 }, { 83, 85, 0, 0, 0, 0 }, { 83, 85, 65, 78, 0, 0 },
            { 83, 85, 73, 0, 0, 0 }, { 83, 85, 78, 0, 0, 0 }, { 83, 85, 79, 0, 0, 0 },
            { 84, 65, 0, 0, 0, 0 }, { 84, 65, 73, 0, 0, 0 }, { 84, 65, 78, 0, 0, 0 },
            { 84, 65, 78, 71, 0, 0 }, { 84, 65, 79, 0, 0, 0 }, { 84, 69, 0, 0, 0, 0 },
            { 84, 69, 78, 71, 0, 0 }, { 84, 73, 0, 0, 0, 0 }, { 84, 73, 65, 78, 0, 0 },
            { 84, 73, 65, 79, 0, 0 }, { 84, 73, 69, 0, 0, 0 }, { 84, 73, 78, 71, 0, 0 },
            { 84, 79, 78, 71, 0, 0 }, { 84, 79, 85, 0, 0, 0 }, { 84, 85, 0, 0, 0, 0 },
            { 84, 85, 65, 78, 0, 0 }, { 84, 85, 73, 0, 0, 0 }, { 84, 85, 78, 0, 0, 0 },
            { 84, 85, 79, 0, 0, 0 }, { 87, 65, 0, 0, 0, 0 }, { 87, 65, 73, 0, 0, 0 },
            { 87, 65, 78, 0, 0, 0 }, { 87, 65, 78, 71, 0, 0 }, { 87, 69, 73, 0, 0, 0 },
            { 87, 69, 78, 0, 0, 0 }, { 87, 69, 78, 71, 0, 0 }, { 87, 79, 0, 0, 0, 0 },
            { 87, 85, 0, 0, 0, 0 }, { 88, 73, 0, 0, 0, 0 }, { 88, 73, 65, 0, 0, 0 },
            { 88, 73, 65, 78, 0, 0 }, { 88, 73, 65, 78, 71, 0 }, { 88, 73, 65, 79, 0, 0 },
            { 88, 73, 69, 0, 0, 0 }, { 88, 73, 78, 0, 0, 0 }, { 88, 73, 78, 71, 0, 0 },
            { 88, 73, 79, 78, 71, 0 }, { 88, 73, 85, 0, 0, 0 }, { 88, 85, 0, 0, 0, 0 },
            { 88, 85, 65, 78, 0, 0 }, { 88, 85, 69, 0, 0, 0 }, { 88, 85, 78, 0, 0, 0 },
            { 89, 65, 0, 0, 0, 0 }, { 89, 65, 78, 0, 0, 0 }, { 89, 65, 78, 71, 0, 0 },
            { 89, 65, 79, 0, 0, 0 }, { 89, 69, 0, 0, 0, 0 }, { 89, 73, 0, 0, 0, 0 },
            { 89, 73, 78, 0, 0, 0 }, { 89, 73, 78, 71, 0, 0 }, { 89, 79, 0, 0, 0, 0 },
            { 89, 79, 78, 71, 0, 0 }, { 89, 79, 85, 0, 0, 0 }, { 89, 85, 0, 0, 0, 0 },
            { 89, 85, 65, 78, 0, 0 }, { 89, 85, 69, 0, 0, 0 }, { 89, 85, 78, 0, 0, 0 },
            { 90, 65, 0, 0, 0, 0 }, { 90, 65, 73, 0, 0, 0 }, { 90, 65, 78, 0, 0, 0 },
            { 90, 65, 78, 71, 0, 0 }, { 90, 65, 79, 0, 0, 0 }, { 90, 69, 0, 0, 0, 0 },
            { 90, 69, 73, 0, 0, 0 }, { 90, 69, 78, 0, 0, 0 }, { 90, 69, 78, 71, 0, 0 },
            { 90, 72, 65, 0, 0, 0 }, { 90, 72, 65, 73, 0, 0 }, { 90, 72, 65, 78, 0, 0 },
            { 90, 72, 65, 78, 71, 0 }, { 90, 72, 65, 79, 0, 0 }, { 90, 72, 69, 0, 0, 0 },
            { 90, 72, 69, 78, 0, 0 }, { 90, 72, 69, 78, 71, 0 }, { 90, 72, 73, 0, 0, 0 },
            { 90, 72, 79, 78, 71, 0 }, { 90, 72, 79, 85, 0, 0 }, { 90, 72, 85, 0, 0, 0 },
            { 90, 72, 85, 65, 0, 0 }, { 90, 72, 85, 65, 73, 0 }, { 90, 72, 85, 65, 78, 0 },
            { 90, 72, 85, 65, 78, 71 }, { 90, 72, 85, 73, 0, 0 }, { 90, 72, 85, 78, 0, 0 },
            { 90, 72, 85, 79, 0, 0 }, { 90, 73, 0, 0, 0, 0 }, { 90, 79, 78, 71, 0, 0 },
            { 90, 79, 85, 0, 0, 0 }, { 90, 85, 0, 0, 0, 0 }, { 90, 85, 65, 78, 0, 0 },
            { 90, 85, 73, 0, 0, 0 }, { 90, 85, 78, 0, 0, 0 }, { 90, 85, 79, 0, 0, 0 }, };

    /** First and last Chinese character with known Pinyin according to zh collation */
    private static final String FIRST_PINYIN_UNIHAN = "\u963F";
    private static final String LAST_PINYIN_UNIHAN = "\u84D9";
    /** The first Chinese character in Unicode block */
    private static final char FIRST_UNIHAN = '\u3400';
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);

    private static HanziToPinyin sInstance;
    private final boolean mHasChinaCollator;

    public static class Token {
        /**
         * Separator between target string for each source char
         */
        public static final String SEPARATOR = " ";

        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final int UNKNOWN = 3;

        public static final int DUOYIN_MASK_BIT = 0x10; 
        public static final int TYPE_MASK_BIT = 0x0F;

        public Token() {		 
        }
	
	public String toString(){
			return "source:" + source + " target:" + target[0] + " / "
					+ target[1] + " type:" + type + " hanzi:" + isHanzi();
	}

	public void addDefaultTarget(String str){
		if(target != null && !TextUtils.isEmpty(str)){
			target[0] = str;
		}
	}
	public void addAdditionalTarget(String str){
		if(target != null && !TextUtils.isEmpty(str)){
			target[1] = str;
		}
		markAsDuoyin();
	}

	private void markAsDuoyin(){
		if( getType() == PINYIN ) {
			type |= DUOYIN_MASK_BIT ;
		} 	
	}

	public int getType(){
		return (type&TYPE_MASK_BIT) ;
	}
	

	public boolean isDuoyin(){
		return (type&DUOYIN_MASK_BIT) == DUOYIN_MASK_BIT;	
	} 
	public boolean isHanzi(){
		return	getType() == PINYIN;
	}	
        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target[0] = target;
        }

        /**
         * Type of this token, ASCII, PINYIN or UNKNOWN.
         */
        public int type;
        /**
         * Original string before translation.
         */
        public String source;
        /**
         * Translated string of source. For Han, target is corresponding Pinyin.
         * Otherwise target is original string in source.
         */
        public String []target = new String[2];
    }

    protected HanziToPinyin(boolean hasChinaCollator) {
        mHasChinaCollator = hasChinaCollator;
    }

    public static boolean hasChinese=false;
    public static HanziToPinyin getInstance() {
        synchronized (HanziToPinyin.class) {
            if (sInstance != null) {
                return sInstance;
            }
            // Check if zh_CN collation data is available
            final Locale locale[] = Collator.getAvailableLocales();
            for (int i = 0; i < locale.length; i++) {
                if (locale[i].equals(Locale.CHINA)) {
                    // Do self validation just once.
                    if (DEBUG) {
                        Log.d(TAG, "Self validation. Result: " + doSelfValidation());
                    }
                    sInstance = new HanziToPinyin(true);
                    hasChinese = true;
                    return sInstance;
                }
            }
            Log.w(TAG, "There is no Chinese collator, HanziToPinyin is disabled");
            sInstance = new HanziToPinyin(false);
            return sInstance;
        }
    }

    /**
     * Validate if our internal table has some wrong value.
     *
     * @return true when the table looks correct.
     */
    private static boolean doSelfValidation() {
        char lastChar = UNIHANS[0];
        String lastString = Character.toString(lastChar);
        for (char c : UNIHANS) {
            if (lastChar == c) {
                continue;
            }
            final String curString = Character.toString(c);
            int cmp = COLLATOR.compare(lastString, curString);
            if (cmp >= 0) {
                Log.e(TAG, "Internal error in Unihan table. " + "The last string \"" + lastString
                        + "\" is greater than current string \"" + curString + "\".");
                return false;
            }
            lastString = curString;
        }
        return true;
    }

    public Token getToken(char character) {
	// Log.d("mixiao","getToken" + character + " value:" + Integer.toHexString(character));
    	
        final String FIRST_PINYIN_UNIHAN = "\u963F";
        final String LAST_PINYIN_UNIHAN = "\u84D9";
        /** The first Chinese character in Unicode block */
        final char FIRST_UNIHAN = '\u3400';
    	
    	
        Token token = new Token();
        
        
        if( isSpecial(character)){ // handle the char ourselves . some pinyin 's order is wrong by using the 
        	//default method , so let's handle them ourselves
        	Log.d("HanziToPinyin" , "it's a special char , let's handle our self");
        	return handleSpecial(character) ; 
        }
        
        
        final String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        int cmp;
        if (character < 256) {
            token.type = Token.LATIN;
            token.target[0] = letter;
            return token;
        } else if (character < FIRST_UNIHAN) {
            token.type = Token.UNKNOWN;
            token.target[0] = letter;
            return token;
        } else {
            cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                token.type = Token.UNKNOWN;
                token.target[0] = letter;
                return token;
            } else if (cmp == 0) {
                token.type = Token.PINYIN;
                offset = 0;
            } else {
                cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
                if (cmp > 0) {
                    token.type = Token.UNKNOWN;
                    token.target[0] = letter;
                    return token;
                } else if (cmp == 0) {
                    token.type = Token.PINYIN;
                    offset = UNIHANS.length - 1;
                }
            }
        }

        token.type = Token.PINYIN;
        if (offset < 0) {
            int begin = 0;
            int end = UNIHANS.length - 1;
            while (begin <= end) {
                offset = (begin + end) / 2;
                final String unihan = Character.toString(UNIHANS[offset]);
                cmp = COLLATOR.compare(letter, unihan);
                if (cmp == 0) {
                    break;
                } else if (cmp > 0) {
                    begin = offset + 1;
                } else {
                    end = offset - 1;
                }
            }
        }
        if (cmp < 0) {
            offset--;
        }
        StringBuilder pinyin = new StringBuilder();
        for (int j = 0; j < PINYINS[offset].length && PINYINS[offset][j] != 0; j++) {
            pinyin.append((char)PINYINS[offset][j]);
        }
        token.addDefaultTarget(pinyin.toString());

		final String duoyin = checkDuoyin(character);
		
		if(duoyin != null && token!=null ){
		    token.addAdditionalTarget(duoyin.toUpperCase());
		}	

        return token;
    }

	private final Token handleSpecial(final char character) {
		Token token = new Token() ;
		token.type= Token.PINYIN;
		token.source = Character.toString(character);
		
		if(character == '\u6c88'){
			token.target[0] = "SHEN";

			
		} else if(character == '\u7fdf') {
			token.target[0] = "ZHAI" ;	
			
		} else if(character == '\u7231') {
			token.target[0] = "AI" ;	
	
		
				
	    }else{
	    	throw new RuntimeException("the special char doesn't find in the handleSpecial which defines in the isSpecial");
	    }
		
		return token ; 
	}

	private boolean isSpecial(final char character) {
		// TODO Auto-generated method stub
		final char c = character;

		if (c == '\u6c88' // 沈
			|| c == '\u7fdf' // 翟
			|| c == '\u7231' // 爱

		) {
			return true;
		}

		return false;
	}

	private String checkDuoyin(char ch){

	    final int unicode = (int)ch;

	    switch(unicode)  {
		    case 0x963f :
			    return "e";
		    case 0x814c :
			    return "an";
			    //B	
			    //case 0x6252 : return "pa2";
			    //case 0x628a : return "ba4";
		    case 0x8180 : return "pang";
				  //case 0x868c : return "beng4";
		    case 0x8584 : return "bao";
		    case 0x5821 : return "pu";
				  //case 0x66b4 : return "pu4";
				  //case 0x80cc : return "bei1";
				  //case 0x5954 : return "ben4";
				  //case 0x81c2 : return "bei5";
				  //case 0x8f9f :  return "bi4"; //
				  //case 0x6241 : return "pian1";
		    case 0x4fbf : return "pian"; //
				  //case 0x9aa0 : return "piao4";
				  //case 0x5c4f : return "bing3";
				  //case 0x5265 : return "bao1";
				  //case 0x6cca : return "po1";
				  //case 0x4f2f : return "bai3";
				  //case 0x7c38 : return "bo4";
				  //case 0x818a : return "bo5";
		    case 0x535c : return "bo";
				  /////C

		    case 0x53c2 : return "shen";
		    case 0x85cf : return "zang";
		    case 0x66fe : return "zeng";//
		    case 0x5dee : return "chai"; //
				  //case 0x5239 : return "cha4"; // 
				  // case 0x7985 : return "shan4";
				  //case 0x98a4 : return "zhan4";
				  //case 0x573a : return "chang2";
		    case 0x671d : return "zhao";
				  //case 0x5632 : return "zhao1";
		    case 0x8f66 : return "ju";
				  //case 0x79f0 : return "chen4";
				  //case 0x4e58 : return "sheng4";
		    case 0x76db : return "cheng";
				  //case 0x6f84 : return "deng4";
				  //case 0x5319 : return "shi5";
				  //case 0x51b2 : return "chong4";
		    case 0x91cd : return "chong";
		    case 0x81ed : return "xiu";
				  //case 0x5904 : return "chu3";
		    case 0x755c : return "xu";
		    case 0x4f20 : return "zhuan";
				  //case 0x521b : return "chuang1";
				  //case 0x7ef0 : return "chao1";
		    case 0x4f3a : return "ci";
				  //case 0x679e : return "zong1";
				  //case 0x6512 : return "cuan2";
				  //case 0x64ae : return "zuo3";

				  /////D
				  //case 0x7b54 : return "da1";
		    case 0x5927 : return "da";
				  //case 0x902e : return "dai3";
		    case 0x5355 : return "dan";
		    case 0x5f39 : return "dan";
				  //case 0x5f53 : return "dang4";
				  //case 0x5012 : return "dao4";
		    case 0x63d0 : return "di";
		    case 0x5f97 : return "dei";
		    case 0x7684 : return "di";
		    case 0x90fd : return "du";
				  //case 0x6387 : return          NULL;
				  //case 0x5ea6 : return "duo2";
				  //case 0x56e4 : return "dun4"; 

				  //F
				  //case 0x53d1 : return "fa4";
				  //case 0x574a : return "fang1";
				  //case 0x5206 : return "fen4";
				  //case 0x7f1d : return "feng4";
				  //case 0x670d : return "fu4"; 
				  //G
				  //case 0x6746 : return "gan3";
				  //case 0x845b : return "ge2";
				  //case 0x9769 : return "ji2";
				  //case 0x5408 : return "ge3";
		    case 0x7ed9 : return "ji";
				  //case 0x66f4 : return "geng4";
				  //case 0x9888 : return "geng3";
				  //case 0x4f9b : return "gong4";
				  //case 0x67b8 : return "ju3";
				  //case 0x4f30 : return "gu4";
				  //case 0x9aa8 : return "gu1";
				  //case 0x8c37 : return              NULL; 
				  //case 0x51a0 : return "guan1";
				  //case 0x6867 : return "gui4";
				  //case 0x8fc7 : return "guo1"; 


				  //H

			/*
			 * 0x867e 0x54c8 0x6c57 0x5df7 0x542d 0x53f7 0x548c 0x8c89 0x559d
			 * 0x6a2a 0x8679 0x5212 0x6643
			 */
		    case  0x4f1a : return "kuai";
				   /* 0x6df7 0x54c4 0x8c41*/


				   //J
		    case 0x5947 : return "ji";
				  /*
				     0x7f09 
				     0x51e0 
				     0x6d4e 
				     0x7eaa 
				     0x5048 
				     0x7cfb 
				   */
		    case 0x8304 : return "jia";
				  //0x5939 0x5047 0x95f4 0x5c06 0x56bc 0x4fa5 
		    case 0x89d2 : return "jue";
				  //0x811a 0x527f  0x6559
		    case  0x6821 : return "jiao";
		    case  0x89e3 : return "xie";
				   //0x7ed3 0x82a5 0x85c9 0x77dc 0x77dc 0x4ec5 
		    case 0x52b2 : return "jing"; 
		    case 0x9f9f : return "jun";
				  //0x5480 0x77e9 0x83cc


				  //K
		    case 0x5361 : return "qia"; 
				  // 0x770b 0x5777 0x58f3 0x53ef 0x514b 0x7a7a 0x6e83

				  //L
			// 0x84dd 0x70d9 0x52d2 0x64c2 0x7d2f 0x8821 0x4fe9 0x91cf 0x8e09
			// 0x6f66 0x6dcb 0x998f 0x954f 0x788c 0x7b3c 0x507b 0x9732
			// case 0x634b : return "lv4";
				  // 0x7eff 
				  //case 0x7edc : return "la4"; 



				  ///M
				  //case 0x8109 : return "mo4"; 
				  //case 0x57cb : return "man2";
				  //0x8513 0x6c13 0x8499 0x772f 0x9761 
		    case 0x79d8 : return "bi";
		    case 0x6ccc : return "bi";
		    case 0x6a21 : return "mu";
				  //case 0x6469 : return "ma1";
		    case 0x7f2a : return "mou";


				  ///N
				  //0x96be 0x5b81
				  //case  0x5f04 : return "long4";
				  //0x759f 
		    case 0x5a1c : return "nuo";
				  //0x62e7


				  /////P
				  //0x6392
		    case  0x8feb : return "pai";
				   //case 0x80d6 : return "pan2"; 
				   //0x5228 0x70ae 0x55b7 0x7247 0x5288 0x7f25 0x6487 0x4ec6 
		    case 0x6734 : return "pu";
		    case  0x7011 : return "bao";
		    case 0x66dd: return "pu";

				 //////////Q
				 //case 0x6816: return "xi1";
				 //case 0x8e4a: return "xi1";
				 //case 0x7a3d: return "qi2";
				 // 0x8368 0x6b20 0x956a
		    case  0x5f3a: return "jiang";
				  // 0x6084 0x7fd8 0x5207 0x8d84 
		    case 0x4eb2: return "qing";
				 // 0x66f2 0x96c0

				 /////////R
				 //0x4efb

				 ////////S
				 //0x6563 0x4e27 
		    case 0x8272 : return "shai" ; 
				  //0x585e 0x715e 
		    case 0x53a6 : return "xia"; 
				  //0x6749 0x82eb 
		    case 0x6298 : return "she";
				  //0x820d 
		    case 0x4ec0 : return "shen";
				  //0x845a 0x8bc6 
		    case 0x4f3c : return "shi";
		    case  0x719f : return "shou";
				   // 0x87ab 0x8bf4 0x6570 0x9042 0x7f29


				   //////T
				   //0x6c93 0x82d4 
		    case 0x8c03 : return "tiao" ; 
				  // 0x5e16


				  /////////W
				  //0x74e6 0x5729 0x59d4 0x5c3e 
		    case 0x5c09 : return "wei";
				  // 0x4e4c


				  ///////X
		    case 0x5413 : return "he" ;
				  // 0x9c9c 0x89c1 
				  //case 0x7ea4 : return "qian4";
				  // 0x76f8 
		    case 0x884c : return "hang";
		    case 0x7701 : return "xing";
		    case 0x5bbf : return "xiu"; 
				  //case 0x524a : return "xiao1";
				  //case 0x8840 : return "xie3";
				  //a0x718f


				  /////Y

				  //0x54d1 
		    case 0x6bb7 : return "yan" ; 
				  // 0x54bd 0x94a5 0x53f6
		    case  0x827e: return "yi";
		    case  0x4e50 : return "yue";
				  // 0x5e94 0x4f63 0x71a8 0x4e0e 0x5401 0x6655 0x7b60


				  ///Z///
				  //0x8f7d 0x62e9 0x624e
				  //case  0x8f67 : return "zha2";
				  //0x7c98 0x6da8 
		    case 0x7740 : return "zhao";
			// 0x6b63 0x6b96 0x4e2d 0x79cd 0x8f74 0x5c5e 0x8457 0x8f6c 0x5e62
			// 0x7efc 0x94bb 0x67de 0x4f5c





		    default:
				  return null;
	    }
	}
    /**
     * Convert the input to a array of tokens. The sequence of ASCII or Unknown characters without
     * space will be put into a Token, One Hanzi character which has pinyin will be treated as a
     * Token. If these is no China collator, the empty token array is returned.
     */
    public ArrayList<Token> get(final String input) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        if (!mHasChinaCollator || TextUtils.isEmpty(input)) {
            // return empty tokens.
            return tokens;
        }
        final int inputLength = input.length();
        final StringBuilder sb = new StringBuilder();
        int tokenType = Token.LATIN;
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        for (int i = 0; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == ' ') {
                if (sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
            } else if (character < 256) {
                if (tokenType != Token.LATIN && sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = Token.LATIN;
                sb.append(character);
            } else if (character < FIRST_UNIHAN) {
                if (tokenType != Token.UNKNOWN && sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = Token.UNKNOWN;
                sb.append(character);
            } else {
                Token t = getToken(character);
                if (t.type == Token.PINYIN || t.isDuoyin()) {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(t);
                    tokenType = Token.PINYIN;
                } else {
                    if (tokenType != t.type && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = t.type;
                    sb.append(character);
                }
            }
        }
        if (sb.length() > 0) {
            addToken(sb, tokens, tokenType);
        }
        return tokens;
    }

    private void addToken(
            final StringBuilder sb, final ArrayList<Token> tokens, final int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }
}
