//
// Translated by CS2J (http://www.cs2j.com): 03/07/2013 12:06:59
//

package com.opg.my.surveys.lite.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.opg.my.surveys.lite.BuildConfig.AES_KEY;


///////////////////////////////////////////////////////////////////////////////
//
//Copyright (c) 2016 OnePoint Global Ltd. All rights reserved.
//
//This code is licensed under the OnePoint Global License.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
//
///////////////////////////////////////////////////////////////////////////////

/**
 * AES256 class uses the AES algorithm with a provided 256 bit key and a random
 * 128 bit IV to meet PCI standards The IV is randomly generated before each
 * encryption and encoded with the final encrypted string
 */
public class Aes256 {

    /************************Aes256******/
    public static final String CIPHER_KEY = "AES/CBC/PKCS5Padding";
    public static final String AES = "AES";
    public static final String UTF_16LE = "UTF-16LE";
    /*public static final String METHOD_GET_MEDIA_SESSION_ID =  "method=getmedia&sessionid=";
    public static final String MEDIA_ID_EQUAL = "&mediaId=";
    public static final String UTF_8= "UTF-8";
    public static final String MEDIA_TYPE_EQUAL = "&mediaType=";
    public static final String WIDTH_100_HEIGHT_100 = "&width=100&height=100";
    public static final String WIDTH_EQUAL = "&width=";
    public static final String HEIGHT_EQUAL =  "&height=";
    public static final String EMPTY_STRING = "";
    public static final String ZERO        = "0";*/

    // Symmetric algorithm interface is used to store the AES service provider

    /**
     * Decrypts a string with AES algorithm
     *
     * @param secureText
     *            Encrypted string with IV prefix
     *
     * @return Decrypted string
     */
    public static String decrypt(String secureText) throws Exception {
        return decrypt(Base64ConvertedKey(), Base64ConvertedArray(secureText));
    }

    // Return decrypted bytes as a string
    /**
     * Encrypts a string with AES algorithm
     *
     * @param plainText
     *            String to encrypt
     *
     * @return Encrypted string with IV prefix
     */
    public static String encrypt(String plainText) throws Exception {
        return encrypt(Base64ConvertedKey(), getByteInLittleIndian(plainText));
    }

    /** Encryption Operation done inside here */
    @SuppressLint("TrulyRandom")
    public static String encrypt(byte[] keybytes, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(keybytes, AES);
        Cipher cipher = Cipher.getInstance(CIPHER_KEY);

        /** To get the Random 16 Byte IV */
        byte[] iv = generateIv();
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
        byte[] encrypted = cipher.doFinal(data);

        /** Appending IV before the encrypted */
        byte[] finalData = concatenateByteArrays(ivspec.getIV(), encrypted);

        /** Converting the byte array into the MyBase 64 */
        return Base64.encodeToString(finalData, 0);
    }

    /** Decryption Operation done inside here */
    public static String decrypt(byte[] keybytes, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(keybytes, AES);
        byte[] temp = encrypted;
        byte[] getIv = new byte[16];
        byte[] cipherByte = new byte[temp.length - 16];

        /** Getting the 16 Byte Iv from the encrypted string */
        System.arraycopy(temp, 0, getIv, 0, getIv.length);

        /** Getting the encrypted Byte is to decrypt */
        System.arraycopy(temp, 16, cipherByte, 0, temp.length - 16);

        IvParameterSpec ivspec = new IvParameterSpec(getIv);
        Cipher cipher = Cipher.getInstance(CIPHER_KEY);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
        byte[] decrypted = cipher.doFinal(cipherByte);

        /** returning the Unicode String */
        return new String(decrypted, UTF_16LE);
    }

    /** Returning the Random IV of 16 Byte */
    protected static byte[] generateIv() throws NoSuchAlgorithmException {
        Random ranGen = new SecureRandom();
        byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
        ranGen.nextBytes(aesKey);
        return aesKey;
    }

    /** Appending IV before the Encrypted Data */
    protected static byte[] concatenateByteArrays(byte[] IV, byte[] encrypted) {
        byte[] result = new byte[IV.length + encrypted.length];
        System.arraycopy(IV, 0, result, 0, IV.length);
        System.arraycopy(encrypted, 0, result, IV.length, encrypted.length);
        return result;
    }

    public static byte[] getByteInLittleIndian(String litInd)
            throws UnsupportedEncodingException {
        return litInd.getBytes(UTF_16LE);
    }

    /**
     * To encrypt the image file
     * @param srcPath
     * @param encodedFileName
     */
    public static void encryptFile(String srcPath, String destinationPath, String encodedFileName){
        Bitmap bitmap= BitmapFactory.decodeFile(srcPath);
        // Write image data to ByteArrayOutputStream
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
        // Encrypt and save the image
        saveFile(encodeFileData(Base64ConvertedKey(),baos.toByteArray()),destinationPath,encodedFileName);
    }


    /**
     * To get the decrypted image from the encrypted image file
     * @param srcPath
     * @param decodedFileName
     */
    public static void decryptFile(String srcPath, String destinationPath, String decodedFileName){
        try {
            // Save the decrypted image
            saveFile(decodeFileData(Base64ConvertedKey(),convertFileToByteArray(srcPath)),destinationPath,decodedFileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * To get the decrypted byte array of the encrypted image file
     * @param srcPath
     * @return
     */
    public static byte[] decryptFileData(String srcPath){
        byte[] decrypted = null;
        try {
            decrypted = decodeFileData(Base64ConvertedKey(),convertFileToByteArray(srcPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return decrypted;
    }


    private static byte[] convertFileToByteArray(String filePath) throws FileNotFoundException
    {
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] byteArray = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArray;
    }

    /**
     *
     * @param key(key for encryption in bytearray form)
     * @param fileData(filedata is converted to the bytearray)
     * @return
     */
    private static byte[] encodeFileData(byte[] key, byte[] fileData)
    {
        Cipher cipher;
        byte[] encrypted=null;
        try{
            SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
            cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(fileData);
        }catch(Exception e){
            e.printStackTrace();
        }
        return encrypted;
    }

    private static byte[] decodeFileData(byte[] key, byte[] fileData)
    {
        Cipher cipher;
        byte[] decrypted=null;
        try{
            SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
            cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = cipher.doFinal(fileData);
        }catch(Exception e){
            e.printStackTrace();
        }
        return decrypted;
    }


    private static void saveFile(byte[] data, String destinationPath, String outFileName){
        FileOutputStream fos=null;
        try {
            File desFile = new File(destinationPath);
            if(!desFile.exists()){
                desFile.mkdirs();
            }
            fos=new FileOutputStream(destinationPath+ File.separator+outFileName);
            fos.write(data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally{
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

       /*public static byte[] getByteInUTF8(String litInd)
       throws UnsupportedEncodingException {
               return litInd.getBytes(UTF_8);
       }*/

       /*public static String getStringInLittleIndian(byte[] litInd)
       throws UnsupportedEncodingException {
               return new String(litInd, UTF_16LE);
       }*/

    public static byte[] Base64ConvertedKey() {
        return Base64.decode(AESKey.getKey(), 0);
    }

    public static byte[] Base64ConvertedArray(String toConvert) {
        return Base64.decode(toConvert, 0);
    }

      /* public static String getMediaBundle(String sessionId, String mediaid,
                                           String mediatype) throws Exception {
               return encryptedString(METHOD_GET_MEDIA_SESSION_ID + sessionId + MEDIA_ID_EQUAL + mediaid + MEDIA_TYPE_EQUAL + mediatype
                               + WIDTH_100_HEIGHT_100);
       }*/

       /*public static String getMediaBundleForImage(String sessionId,
                                                   String mediaid, String mediatype, int width, int hieght)
       throws Exception {
               return encryptedString(METHOD_GET_MEDIA_SESSION_ID + sessionId
                               + MEDIA_ID_EQUAL + mediaid + MEDIA_TYPE_EQUAL + mediatype + WIDTH_EQUAL
                               + width + HEIGHT_EQUAL+ hieght + EMPTY_STRING);
       }*/

       /*public static String encryptedString(String jsonasString) {
               String encrypted = null;
               try {
                       encrypted = encrypt(Base64ConvertedKey(),
                                       getByteInLittleIndian(jsonasString));
               } catch (UnsupportedEncodingException e) {
                       e.printStackTrace();
               } catch (Exception e) {
                       e.printStackTrace();
               }
               return encrypted;
       }*/

    private static class AESKey{

        private final static String getKey(){
            String KEY = AES_KEY;
            return KEY;
        }

    }
}
