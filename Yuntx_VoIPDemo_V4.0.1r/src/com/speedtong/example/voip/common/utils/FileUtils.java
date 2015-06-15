/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.voip.common.utils;

import java.io.BufferedOutputStream;



import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;


import android.os.Environment;
import android.text.TextUtils;



/**
 * 文件工具类
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-30
 * @version 4.0
 */
public class FileUtils {
	
	/**
	 * 转换成单位
	 * @param length
	 * @return
	 */
	public static String formatFileLength(long length) {
		if (length >> 30 > 0L) {
			float sizeGb = Math.round(10.0F * (float) length / 1.073742E+009F) / 10.0F;
			return sizeGb + " GB";
		}
		if (length >> 20 > 0L) {
			return formatSizeMb(length);
		}
		if (length >> 9 > 0L) {
			float sizekb = Math.round(10.0F * (float) length / 1024.0F) / 10.0F;
			return sizekb + " KB";
		}
		return length + " B";
	}
	
	public static boolean isExistExternalStore() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	public static final String DEMO_ROOT_STORE = "voipdemo";
	public static String CALLS_RECORD_TEMP_PATH = getExternalStorePath()+  "/" +DEMO_ROOT_STORE + "/callsRecordTemp";
	public static File createCallRecordFilePath(String fileName , String ext) {
		File localFile = new File(CALLS_RECORD_TEMP_PATH , fileName + "." + ext);
		if ((!localFile.getParentFile().exists())
				&& (!localFile.getParentFile().mkdirs())) {
			localFile = null;
		}
		
		return localFile;
	}
	
	public static String getExternalStorePath() {
		if (isExistExternalStore()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}

	/**
	 * 转换成Mb单位
	 * @param length
	 * @return
	 */
	public static String formatSizeMb(long length) {
		float mbSize = Math.round(10.0F * (float) length / 1048576.0F) / 10.0F;
		return mbSize + " MB";
	}
	
	/**
	 * 检查SDCARD是否可写
	 * @return
	 */
	public static boolean checkExternalStorageCanWrite() {
		try {
			boolean mouted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			if(mouted) {
				boolean canWrite = new File(Environment.getExternalStorageDirectory().getAbsolutePath()).canWrite();
				if(canWrite) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	
	
	
	
	
	/**
	 * 是否图片
	 * @param fileName
	 * @return
	 */
	public static boolean isPic(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".bmp")
				|| lowerCase.endsWith(".png")
				|| lowerCase.endsWith(".jpg")
				|| lowerCase.endsWith(".jpeg") 
				|| lowerCase .endsWith(".gif");
	}
	
	/**
	 * 是否压缩文件
	 * @param fileName
	 * @return
	 */
	public static boolean isCompresseFile(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".rar") 
				|| lowerCase.endsWith(".zip")
				|| lowerCase.endsWith(".7z") 
				|| lowerCase.endsWith("tar")
				|| lowerCase.endsWith(".iso");
	}

	/**
	 * 是否音频
	 * @param fileName
	 * @return
	 */
	public static boolean isAudio(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".mp3")
				|| lowerCase.endsWith(".wma")
				|| lowerCase.endsWith(".mp4") 
				|| lowerCase.endsWith(".rm");
	}

	/**
	 * 是否文档
	 * @param fileName
	 * @return
	 */
	public static boolean isDocument(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".doc")
				|| lowerCase.endsWith(".docx") 
				|| lowerCase .endsWith("wps");
	}

	/**
	 * 是否Pdf
	 * @param fileName
	 * @return
	 */
	public static boolean isPdf(String fileName) {
		return DemoUtils.nullAsNil(fileName).toLowerCase().endsWith(".pdf");
	}

	/**
	 * 是否Excel
	 * @param fileName
	 * @return
	 */
	public static boolean isXls(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".xls") 
				|| lowerCase.endsWith(".xlsx");
	}

	/**
	 * 是否文本文档
	 * @param fileName
	 * @return
	 */
	public static boolean isTextFile(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".txt") 
				|| lowerCase .endsWith(".rtf");
	}
	
	/**
	 * 是否Ppt
	 * @param fileName
	 * @return
	 */
	public static boolean isPPt(String fileName) {
		String lowerCase = DemoUtils.nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".ppt") || lowerCase .endsWith(".pptx");
	}
	
	/**
	 * decode file length
	 * @param filePath
	 * @return
	 */
	public static int decodeFileLength(String filePath) {
		if(TextUtils.isEmpty(filePath)) {
			return 0;
		}
		File file = new File(filePath);
		if(!file.exists()) {
			return 0;
		}
		return (int)file.length();
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}
	
	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean checkFile(String filePath) {
		if(TextUtils.isEmpty(filePath) || !(new File(filePath).exists())) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param filePath
	 * @param seek
	 * @param length
	 * @return
	 */
	public static byte[] readFlieToByte (String filePath , int seek , int length) {
		if(TextUtils.isEmpty(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if(!file.exists()) {
			return null;
		}
		if(length == -1) {
			length = (int)file.length();
		}
		
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			byte[] bs = new byte[length];
			randomAccessFile.seek(seek);
			randomAccessFile.readFully(bs);
			randomAccessFile.close();
            return bs;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(LogUtil.getLogUtilsTag(FileUtils.class), "readFromFile : errMsg = " + e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * 拷贝文件
	 * @param fileDir
	 * @param fileName
	 * @param buffer
	 * @return
	 */
	public static int copyFile(String fileDir ,String fileName , byte[] buffer) {
		if(buffer == null) {
			return -2;
		}
		
		try {
			File file = new File(fileDir);
			if(!file.exists()) {
				file.mkdirs();
			}
			File resultFile = new File(file, fileName);
			if(!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream(resultFile, true));
			bufferedOutputStream.write(buffer);
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			return 0;
			
		} catch (Exception e) {
		}
		return -1;
	}
	

	/**
	 * 根据文件名和后缀 拷贝文件
	 * @param fileDir
	 * @param fileName
	 * @param ext
	 * @param buffer
	 * @return
	 */
	public static int copyFile(String fileDir ,String fileName , String ext , byte[] buffer) {
		return copyFile(fileDir, fileName + ext, buffer);
	}
	
	
	/**
	 * 根据后缀名判断是否是图片文件
	 * 
	 * @param type
	 * @return 是否是图片结果true or false
	 */
	public static boolean isImage(String type) {
		if (type != null
				&& (type.equals("jpg") || type.equals("gif")
						|| type.equals("png") || type.equals("jpeg")
						|| type.equals("bmp") || type.equals("wbmp")
						|| type.equals("ico") || type.equals("jpe"))) {
			return true;
		}
		return false;
	}
	
}
