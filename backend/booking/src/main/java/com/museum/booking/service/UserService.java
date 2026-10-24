package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.SysUser;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台用户管理服务。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> page(int page, int size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords().stream().map(UserVO::from).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void updateStatus(Long id, Integer status, Long operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "非法的用户状态");
        }
        SysUser target = requireUser(id);
        if (id.equals(operatorId) && status == 0) {
            throw new BusinessException(400, "不能禁用自己的账号");
        }
        if ("ADMIN".equals(target.getRole()) && status == 0) {
            long otherAdmins = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getRole, "ADMIN")
                    .eq(SysUser::getStatus, 1)
                    .ne(SysUser::getId, id));
            if (otherAdmins == 0) {
                throw new BusinessException(409, "系统至少需要保留一个启用状态的管理员");
            }
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status));
    }

    public void resetPassword(Long id, String newPassword) {
        requireUser(id);
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword)));
    }

    public UserVO create(AdminDtos.UserCreateForm form) {
        long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, form.username().trim()));
        if (exists > 0) {
            throw new BusinessException(409, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(form.username().trim());
        user.setPassword(passwordEncoder.encode(form.password()));
        user.setRealName(form.realName().trim());
        user.setPhone(form.phone().trim());
        user.setEmail(form.email());
        user.setRole(form.role());
        user.setStatus(1);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public UserVO get(Long id) {
        return UserVO.from(requireUser(id));
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
}