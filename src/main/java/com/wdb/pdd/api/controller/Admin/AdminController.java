package com.wdb.pdd.api.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wdb.pdd.api.pojo.entity.AdminAction;

import com.wdb.pdd.api.pojo.entity.Product;
import com.wdb.pdd.api.pojo.entity.ProductVip;
import com.wdb.pdd.api.pojo.entity.Vip;
import com.wdb.pdd.api.service.product.ProductService;
import com.wdb.pdd.api.service.sys.AdminActionService;
import com.wdb.pdd.api.service.sys.ProductVipService;
import com.wdb.pdd.api.service.sys.VipService;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.utils.Encrypt;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import org.apache.shiro.crypto.hash.Sha256Hash;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminActionService adminActionService;

    @Autowired
    private VipService vipService;

    @Autowired
    private ProductVipService productVipService;
    @Autowired
    private ProductService productService;

    @CheckToken
    @PostMapping("/sendMail")
    public Result<?> sendAttachmentsMail(@RequestBody Map<String, Object> req, HttpSession session) throws Exception {
        try {
//            AdminAction adminAction=new AdminAction();
            AdminAction adminAction = adminActionService.getOne(new QueryWrapper<AdminAction>().eq("username", req.get("email").toString()));


            if (adminAction != null) {
                return Result.fail("该邮箱已经注册");
            }


            JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
            senderImpl.setHost("smtp.qq.com");
//        senderImpl.setPort(587);
            senderImpl.setUsername("1341501719@qq.com");                             // 根据自己的情况,设置发件邮箱地址
            senderImpl.setPassword("ulvhbbvwtifzhbed");                    // 根据自己的情况, 设置password
            senderImpl.setDefaultEncoding("UTF-8");
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");                                 // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            senderImpl.setJavaMailProperties(prop);

            MimeMessage message = senderImpl.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("1341501719@qq.com");
//            System.out.println( req.get("email").toString());
            helper.setTo(req.get("email").toString());
            helper.setSubject("邮箱注册");
            String checkCode = String.valueOf(new Random().nextInt(899999) + 100000);
            String messages = "您的注册验证码为：" + checkCode;
            session.setAttribute("checkCode", checkCode);
            String checkCode2 = (String) session.getAttribute("checkCode");
            System.out.println(checkCode);
            System.out.println("session:" + checkCode2);
            helper.setText(messages);
            senderImpl.send(message);
            return Result.ok("发送邮件成功");
        } catch (MailSendException e) {
//            e.printStackTrace();

            return Result.fail("发送邮件失败");
        }


    }

    @CheckToken
    @PostMapping("/MailLogin")
    public Result<?> MailLogin(@RequestBody Map<String, Object> req, HttpSession session) {
        String eMail = req.get("email").toString();
        String pwd = req.get("pwd").toString();
        String code = req.get("code").toString();
        String checkCode = (String) session.getAttribute("checkCode");
        System.out.println(eMail);
        System.out.println(pwd);
        System.out.println(code);
        System.out.println(checkCode);
        Integer reguserId = UserUtils.getReguserId();
        String salt=UUID.randomUUID().toString();
        String password=new Encrypt().SHA256(salt,eMail,pwd);

        if (!code.equals(checkCode)) {
            return Result.fail("验证码错误");
        } else {
            AdminAction adminAction = new AdminAction();
            adminAction.setPassword(password);
            adminAction.setUsername(eMail);
            adminAction.setAccountLevelId(3);
            adminAction.setReguserId(reguserId);
            adminAction.setSalt(salt);
            adminActionService.save(adminAction);
        }
        return Result.ok(checkCode);
    }

    @CheckToken
    @PostMapping("/getAdminActionList")
    public Result<?> getAdminActionList() {
        Integer reguserId = UserUtils.getReguserId();
        List<AdminAction> list = adminActionService.list(new QueryWrapper<AdminAction>().eq("reguser_id", reguserId));
        return Result.ok(list);
    }


    //    @CheckToken
    @PostMapping("/sendMail2")
    public Result<?> sendAttachmentsMail2(@RequestBody Map<String, Object> req, HttpSession session) throws Exception {
        try {
//            AdminAction adminAction=new AdminAction();
            AdminAction adminAction = adminActionService.getOne(new QueryWrapper<AdminAction>().eq("username", req.get("email").toString()));


            if (adminAction == null) {
                return Result.fail("该邮箱不存在");
            }


            JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
            senderImpl.setHost("smtp.qq.com");
//        senderImpl.setPort(587);
            senderImpl.setUsername("1341501719@qq.com");                             // 根据自己的情况,设置发件邮箱地址
            senderImpl.setPassword("ulvhbbvwtifzhbed");                    // 根据自己的情况, 设置password
            senderImpl.setDefaultEncoding("UTF-8");
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");                                 // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            senderImpl.setJavaMailProperties(prop);

            MimeMessage message = senderImpl.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("1341501719@qq.com");
//            System.out.println( req.get("email").toString());
            helper.setTo(req.get("email").toString());
            helper.setSubject("邮箱注册");
            String checkCode = String.valueOf(new Random().nextInt(899999) + 100000);
            String messages = "您的注册验证码为：" + checkCode;
            session.setAttribute("checkCode2", checkCode);

            System.out.println(checkCode);

            helper.setText(messages);
            senderImpl.send(message);
            return Result.ok("发送邮件成功");
        } catch (MailSendException e) {
//            e.printStackTrace();

            return Result.fail("发送邮件失败");
        }


    }


    @PostMapping("/updatepwd")
    public Result<?> updatepwd(@RequestBody Map<String, Object> req, HttpSession session) {
        String eMail = req.get("email").toString();
        String ypwd = req.get("ypwd").toString();
        String rpwd = req.get("rpwd").toString();
        String code = req.get("code").toString();
        String checkCode = (String) session.getAttribute("checkCode2");
        System.out.println(eMail);
        System.out.println(ypwd);
        System.out.println(code);
        System.out.println(checkCode);
//        Integer reguserId = UserUtils.getReguserId();
        AdminAction adminAction = adminActionService.getOne(new QueryWrapper<AdminAction>().eq("username", req.get("email").toString()));
        if (adminAction == null) {
            return Result.fail("邮箱不存在");

        } else if (!code.equals(checkCode)) {
            return Result.fail("验证码错误");
        } else {
            String salt=UUID.randomUUID().toString();
            String password=new Encrypt().SHA256(salt,eMail,ypwd);
            adminAction.setSalt(salt);
            adminAction.setPassword(password);
            return Result.ok(adminActionService.updateById(adminAction));
        }

    }

    @GetMapping("/getvipList")
    public Result<?> vipList() {
        List<Vip> list = vipService.list();
        return Result.ok(list);
    }

    @GetMapping("/getvipInfo")
    public Result<?> vipInfo(@RequestParam Map<String, Object> req) {
        String id = req.get("id").toString();

        List<ProductVip> list = productVipService.list(new QueryWrapper<ProductVip>().eq("vip_id", id));

        List<Map<String, Object>> lists = new ArrayList<>();
        Map<String, Object> map = null;
        for (ProductVip productVip : list) {
            map = new HashMap<>();
            map.put("time", productVip.getVipTime());
            Product product = productService.getById(productVip.getProId());
             map.put("name", product.getName());
            map.put("price", product.getPrice());
            map.put("OldPrice", product.getOldPrice());
            map.put("info", product.getOtherDetailInfo());
            map.put("productid", product.getId());
            lists.add(map);
        }


        return Result.ok(lists);
    }

    /**
     * 密码加盐测试
     * @return
     */
    @GetMapping("/aaa")
    public String cc(){
//        String salt=UUID.randomUUID().toString();
//        String salt="meichuang11"+"123456";
//        String p=new  Sha256Hash(salt).toString();
//        String result=new Sha256Hash(p+"chen090017@163.com").toString();
        String salt="86842cdc-d125-4e58-92b8-5963675670df";
        String  name="chen090017@163.com";
        String pwd="123456";
//        String p=new  Sha256Hash(salt).toString();
//        String result=new Sha256Hash(p+"chen090017@163.com").toString();

     String result=new Encrypt().SHA256(salt,name,pwd);
        return  result;
    }



}
