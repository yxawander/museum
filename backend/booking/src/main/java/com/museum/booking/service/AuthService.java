package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.museum.booking.common.BusinessException;
import com.museum.booking.dto.AuthDtos;
import com.museum.booking.entity.SysUser;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.security.JwtUtil;
import com.museum.booking.vo.LoginVO;
import com.museum.booking.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 注册、登录与个人资料服务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginVO login(AuthDtos.LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username().trim()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginAt, LocalDateTime.now()));

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, UserVO.from(user));
    }

    public UserVO register(AuthDtos.RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
        long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username().trim()));
        if (exists > 0) {
            throw new BusinessException(409, "用户名已被注册");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName().trim());
        user.setPhone(request.phone().trim());
        user.setEmail(blankToNull(request.email()));
        user.setIdCard(blankToNull(request.idCard()));
        user.setRole("VISITOR");
        user.setStatus(1);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public UserVO me(Long userId) {
        return UserVO.from(requireUser(userId));
    }

    public UserVO updateProfile(Long userId, AuthDtos.ProfileRequest request) {
        SysUser user = requireUser(userId);
        user.setRealName(request.realName().trim());
        user.setPhone(request.phone().trim());
        user.setEmail(blankToNull(request.email()));
        if (request.idCard() != null && !request.idCard().isBlank()) {
            user.setIdCard(request.idCard().trim());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar().trim());
        }
        userMapper.updateById(user);
        return UserVO.from(user);
    }

    public void changePassword(Long userId, AuthDtos.PasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(400, "两次输入的新密码不一致");
        }
        SysUser user = requireUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }
        if (request.oldPassword().equals(request.newPassword())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, passwordEncoder.encode(request.newPassword())));
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}