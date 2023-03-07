package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ps.common.R;
import com.ps.entity.User;
import com.ps.service.UserService;
import com.ps.utils.SMSUtils;
import com.ps.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        String phone = (String) session.getAttribute("phone");
        session.removeAttribute(phone);
        session.removeAttribute("phone");
        return R.success("登出用户成功");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        String phone = (String) map.get("phone");
        String code = (String) map.get("code");

        String realCode = (String) session.getAttribute(phone);

        if(realCode != null && realCode.equals(code)){
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);
            User user = userService.getOne(lqw);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("phone", phone);
            session.setAttribute("user", user.getId());
            return R.success(user);
        }

        return R.error("验证码错误，登陆失败");
    }

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //得到前端传来的手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code=" + code);
            //对手机号发送验证码
            //SMSUtils.sendMessage("Ps学习", "SMS_272435660", "13854115829", code);
            session.setAttribute(phone, code);
            return R.success("发送验证码成功");
        }
        return R.error("发送验证码失败");
    }

}
