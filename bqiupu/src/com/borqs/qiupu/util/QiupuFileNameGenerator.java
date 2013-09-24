package com.borqs.qiupu.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.borqs.qiupu.cache.QiupuHelper;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;

public class QiupuFileNameGenerator extends HashCodeFileNameGenerator {

	@Override
	public String generate(String imageUri) {
		try {
			String filePath = QiupuHelper.getImageFilePath(new URL(imageUri), true);
			return new File(filePath).getName();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return super.generate(imageUri);
	}

}
