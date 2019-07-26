package com.wdb.pdd.common.utils;

import org.apache.shiro.crypto.hash.Sha256Hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Encrypt
{
  /**
   * 传入文本内容，返回 SHA-256 串
   *
   * @param
   * @return
   */
  public String SHA256(String salt,String name,String pwd)
  {
//    String salt="meichuang11"+"123456";
    String p=new Sha256Hash(salt+pwd).toString();
    String result=new Sha256Hash(p+name).toString();

    return result;
  }

}