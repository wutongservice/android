package com.borqs.qiupu.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class FileUtils {
	public FileUtils() {
	}

	public static String openFile(String fileName) throws IOException {
		return openFile(fileName, null);
	}

	public static String openFile(String fileName, String charset)
			throws IOException {
		StringBuffer stringBuffer = new StringBuffer();
		BufferedReader in = null;
		String line = null, line1 = null, line2 = null, line3 = null, line4 = null;
		File file = null;
		String returnString = null;
		try {
			file = new File(fileName);
			if (file.exists()) {
				if (charset == null) {
					in = new BufferedReader(new InputStreamReader(
							new FileInputStream(file)));
				} else {
					in = new BufferedReader(new InputStreamReader(
							new FileInputStream(file), charset));
				}
				while ((line = in.readLine()) != null) {
					stringBuffer.append(line).append("\r\n");
				}
				returnString = stringBuffer.toString();
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				in.close();
				in = null;
				file = null;
				stringBuffer = null;
				line = null;
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return returnString;
	}

	/**
	 * create directory
	 * 
	 * @param dir
	 * @return -1 exception,1 success,0 false,2 there is one directory of the
	 *         same name in system
	 */
	public static void createDirectory(String dir) throws IOException {
		File file = new File(dir);
		if (file.exists()) {
			if (!file.isDirectory()) {
				String message = "File "
						+ dir
						+ " exists and is not a directory. Unable to create directory";
				throw new IOException(message);
			}
		} else {
			if (!file.mkdirs()) {
				if (!file.isDirectory()) {
					String message = "Unable to create directory " + dir;
					throw new IOException(message);
				}
			}
		}

	}

	/**
	 * delete directory
	 * 
	 * @param file
	 *            file handle
	 * @return -1 exception,1 success,0 false,2 there is no one directory of the
	 *         same name in system
	 */
	public static int deleteDirectory(File file) {
		int returnValue = -1;
		try {
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (int i = 0, n = fileList.length; i < n; i++) {
						deleteDirectory(fileList[i]);
					}
					fileList = null;
				}

				if (file.delete()) {
					returnValue = 1;
				} else {
					returnValue = 0;
				}
			} else {
				returnValue = 2;
			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
		}
		return returnValue;
	}

	/**
	 * delete directory
	 * 
	 * @param dir
	 * @return -1 exception,1 success,0 false,2 there is no one directory of the
	 *         same name in system
	 */
	public static int deleteDirectory(String dir) {
		File file = null;
		int returnValue = -1;
		try {
			file = new File(dir);
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (int i = 0, n = fileList.length; i < n; i++) {
						deleteDirectory(fileList[i]);
					}
					fileList = null;
				}

				if (file.delete()) {
					returnValue = 1;
				} else {
					returnValue = 0;
				}
			} else {
				returnValue = 2;

			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
		}
		return returnValue;
	}

	/**
	 * delete file
	 * 
	 * @param fileName
	 * @return -1 exception,1 success,0 false,2 there is no one directory of the
	 *         same name in system
	 */
	public static int deleteFile(String fileName) {
		File file = null;
		int returnValue = -1;
		try {
			file = new File(fileName);
			if (file.exists()) {
				if (file.delete()) {
					returnValue = 1;
				} else {
					returnValue = 0;
				}
			} else {
				returnValue = 2;

			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
		}
		return returnValue;
	}

	/**
	 * delete files
	 * 
	 * @param fileName
	 * @return -1 exception,1 success,0 false,2 there is no one directory of the
	 *         same name in system
	 */
	public static List deleteFiles(File file, String filename) {
		ArrayList list = new ArrayList();
		try {
			if (file.exists()) {
				File[] files = file.listFiles();

				for (int i = 0; i < files.length; i++) {
					// System.out.println(files[i].getName()+"======"+files.length);
					if (files[i].getName().startsWith(filename)) {
						// System.out.println(files[i].getName());
						list.add(files[i].getName());
						files[i].delete();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		}

		return list;
	}

	/**
	 * rename file oldFile'name to newFile's name
	 * 
	 * @param fileName
	 * @param newFile
	 * @return -1 exception,1 success,0 false,2 there is no one directory of the
	 *         same name in system
	 */
	public static int renameTile(String oldFile, String newFile) {
		File file = null, file1 = null;
		int returnValue = -1;
		try {
			file = new File(oldFile);
			file1 = new File(newFile);
			if (file.exists()) {
				if (file.renameTo(file1)) {
					returnValue = 1;
				} else {
					returnValue = 0;
				}
			} else {
				returnValue = 2;

			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
			file1 = null;
		}
		return returnValue;
	}

	/**
	 * check whether the file is existed
	 * 
	 * @param fileName
	 * @return -1 exception,1 existed,0 not existed
	 */
	public static int isExisted(String fileName) {
		File file = null;
		int returnValue = -1;

		try {
			file = new File(fileName);
			if (file.exists()) {
				returnValue = 1;
			} else {
				returnValue = 0;

			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
		}
		return returnValue;
	}

	/**
	 * get file length
	 * 
	 * @param fileName
	 * @return -2 is a directory,-1 exception,0 not existed,>0 file's length
	 */
	public static long getFileLength(String fileName) {
		File file = null;
		long returnValue = -1;

		try {
			file = new File(fileName);
			if (file.exists()) {
				if (file.isDirectory()) {
					returnValue = -2;
				} else {
					returnValue = file.length();
				}
			} else {
				returnValue = 0;

			}
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		} finally {
			file = null;
		}
		return returnValue;
	}

	/**
	 * save string toSave to file filename
	 * 
	 * @param toSave
	 * @param filename
	 * @return -1 exception,1 success
	 */
	public static int saveFile(String toSave, String filename) {
		int returnValue = -1;
		try {
			BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(
					new FileOutputStream(filename));
			OutputStreamWriter outputstreamwriter = new OutputStreamWriter(
					bufferedoutputstream, "UTF-8");
			int i = toSave.length();
			outputstreamwriter.write(toSave, 0, i);
			outputstreamwriter.close();
			bufferedoutputstream.close();
			returnValue = 1;
		} catch (Exception e) {
			System.out.println("Exception:e=" + e.getMessage());
		}
		return returnValue;
	}

	/**
	 * copy file
	 * 
	 * @param source
	 * @param desc
	 * @return -1 exception,0 not existed or can't read,1 success
	 */
	public static int copyfile(String source, String desc) throws Exception {
		File file = null, file1 = null;
		byte[] buffer = new byte[1024];
		int bytes_read = 0;
		int returnValue = -1;

		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			file = new File(source);
			file1 = new File(desc);
			if (file.exists() && file.canRead()) {
				fin = new FileInputStream(file);
				fout = new FileOutputStream(file1);
				while ((bytes_read = fin.read(buffer)) != -1) {
					fout.write(buffer, 0, bytes_read);
				}
				returnValue = 1;

				try {
					if (fin != null) {
						fin.close();
						fin = null;
					}
					if (fout != null) {
						fout.close();
						fout = null;
					}
				} catch (Exception e) {
					// System.out.println("close file handle failure Exception:e="
					// + e.getMessage());
					Log.d("FileUtil", "copyFile exception:" + e.getMessage());
				}
			} else {
				returnValue = 0;
			}
		} catch (Exception e) {
			throw e;
		} finally {

			file = null;
			file1 = null;
			buffer = null;
		}
		return returnValue;
	}

	/**
	 * @param String
	 * @return true legal, false illegal
	 */
	public static boolean createNewFile(File f) {
		try {
			return f.createNewFile();
		} catch (IOException ignored) {
			return false;
		}
	}

	public static File rename(String oldName, String newName) {
		int dot = oldName.lastIndexOf(".");
		String ext = null;
		if (dot != -1) {
			ext = oldName.substring(dot); // includes "."
		}
		File f = new File(newName + ext);
		createNewFile(f);
		return f;
	}

	public static void removeFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] subfiles = file.listFiles();
				for (int i = 0; i < subfiles.length; i++) {
					removeFile(subfiles[i]);
				}
				file.delete();
			} else {
				file.delete();
			}
		}
	}

	public static byte[] getBytesFromFile(File f) {
		if (f == null) {
			return null;
		}
		try {
			FileInputStream stream = new FileInputStream(f);
			ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = stream.read(b)) != -1)
				out.write(b, 0, n);
			stream.close();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}

	public static File getFileFromBytes(byte[] b, String outputFile) {
		BufferedOutputStream stream = null;
		File file = null;
		try {
			file = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}
}
