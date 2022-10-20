package com.lv.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lv.reggie.common.R;
import com.lv.reggie.entity.User;
import com.lv.reggie.service.UserService;
import com.lv.reggie.utils.SMSUtils;
import com.lv.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    public UserService userService;

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 发送手机短信
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {

        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            //需要将生成的验证码保存到Session
            //session.setAttribute(phone, code);

            //优化---将生成的验证码缓存到redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登陆
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);
        //优化---从Redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)) {
            //如果能够比对成功，说明登陆成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            //判断当前手机号对应的用户是否为新用户，如果是新用户自动完成注册
            if(user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);

                //随机生成姓名
                int min = 1;
                int max = 8;
                Random random = new Random();
                int value = random.nextInt(max + min) + min;
                switch (value) {
                    case 1:
                        user.setName("张三");
                        break;
                    case 2:
                        user.setName("李四");
                        break;
                    case 3:
                        user.setName("王五");
                        break;
                    case 4:
                        user.setName("赵六");
                        break;
                    case 5:
                        user.setName("孙七");
                        break;
                    case 6:
                        user.setName("周八");
                        break;
                    case 7:
                        user.setName("吴九");
                        break;
                    default:
                        user.setName("郑十");
                        break;
                }
                switch (value%2) {
                    case 0:
                        user.setSex("男");
                        break;
                    case 1:
                        user.setSex("女");
                        break;
                }

                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            //如果用户登陆成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);
            return R.success(user);

        }

        return R.error("登陆失败");
    }
}
