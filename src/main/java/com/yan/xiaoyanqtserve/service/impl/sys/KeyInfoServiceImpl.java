package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
import com.yan.xiaoyanqtserve.common.vo.KeyInfoVo;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.AdminUserMapper;
import com.yan.xiaoyanqtserve.mapper.KeyInfoMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 密钥信息表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Service
public class KeyInfoServiceImpl extends ServiceImpl<KeyInfoMapper, KeyInfo> implements KeyInfoService {

    @Resource
    private KeyInfoMapper keyInfoMapper;

    @Resource
    private ProductTypeService productTypeService;
    @Resource
    private AdminUserMapper adminUserMapper;

    /**
     * 根据密钥值查询密钥信息
     * @param keyValue 密钥值
     * @return 密钥信息
     */
    @Nullable
    @Override
    public KeyInfo getByKeyValue(String keyValue) {
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getKeyValue, keyValue);
        return getOne(queryWrapper);
    }

    /**
     * 分页查询密钥列表
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<KeyInfo> getList(KeyInfoPageRequest pageRequest) {
        Page<KeyInfo> keyInfoPage = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());

        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(pageRequest.getKeyValue())) {
            queryWrapper.like(KeyInfo::getKeyValue, pageRequest.getKeyValue());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq(KeyInfo::getStatus, pageRequest.getStatus());
        }
        if (pageRequest.getActivated() != null) {
            queryWrapper.eq(KeyInfo::getActivated, pageRequest.getActivated());
        }
        if (pageRequest.getSelled() != null) {
            queryWrapper.eq(KeyInfo::getSelled, pageRequest.getSelled());
        }
        if (pageRequest.getProductTypeId() != null) {
            queryWrapper.eq(KeyInfo::getProductType, pageRequest.getProductTypeId());
        }

        if (StringUtils.hasText(pageRequest.getUsageType())) {
            queryWrapper.like(KeyInfo::getUsageType, pageRequest.getUsageType());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(KeyInfo::getCreatedTime);

        return this.page(keyInfoPage, queryWrapper);
    }

    /**
     * 更新密钥信息
     * @param keyInfo 密钥信息
     */
    @Override
    public void updateKeyInfo(KeyInfo keyInfo) {
        int i = keyInfoMapper.updateById(keyInfo);
        if (i <= 0) {
            throw new RuntimeException("更新密钥信息失败");
        }
    }

    /**
     * 根据密钥ID获取密钥详情
     * @param keyId 密钥ID
     * @return 密钥详情
     */
    @Override
    public KeyInfoVo getDetailKeyInfo(Long keyId) {
        KeyInfo keyInfo = keyInfoMapper.selectById(keyId);
        if (keyInfo != null) {
            KeyInfoVo keyInfoVo = new KeyInfoVo();
            BeanUtils.copyProperties(keyInfo, keyInfoVo);

            // 获取创建密钥的管理员用户名
            AdminUser adminUser = adminUserMapper.selectById(keyInfo.getCreatedBy());
            if (adminUser != null) {
                keyInfoVo.setCreatedByName(adminUser.getUsername());
            }

            // 获取密钥所属产品类型名称
            ProductType productType = productTypeService.getDetailById(keyInfo.getProductType().longValue());
            if (productType != null) {
                keyInfoVo.setProductName(productType.getProductName());
                keyInfoVo.setProductEquityTime(convertEquityTime(productType.getEquityTime()));
            }

            return keyInfoVo;
        }
        return null;
    }

    /**
     * 获取一个未使用的免费卡密
     * <p>查找 usage_type = 'free' 且未激活的卡密</p>
     * @return 免费卡密信息，如果卡密池为空则返回 null
     */
    @Override
    public KeyInfo getUnusedFreeKey() {
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getUsageType, "free")
                .eq(KeyInfo::getStatus, 1)
                .eq(KeyInfo::getActivated, 0)
                .orderByAsc(KeyInfo::getCreatedTime)
                .last("LIMIT 1");
        return getOne(queryWrapper);
    }

    /**
     * 将权益时间转换为字符串
     * @param equityTime 权益时间（单位：分钟），null值会被视为0
     * @return 权益时间字符串，如"30分钟"、"2小时"、"3天"
     */
    private String convertEquityTime(Integer equityTime) {
        // 处理null值情况
        if (equityTime == null || equityTime <= 0) {
            return "0分钟";
        }

        if (equityTime < 60) {
            return equityTime + "分钟";
        } else if (equityTime < 1440) { // 1440分钟 = 24小时
            int hours = equityTime / 60;
            int remainingMinutes = equityTime % 60;
            if (remainingMinutes > 0) {
                return hours + "小时" + remainingMinutes + "分钟";
            } else {
                return hours + "小时";
            }
        } else { // 1440分钟 = 1天
            int days = equityTime / 1440;
            int remainingMinutes = equityTime % 1440;
            if (remainingMinutes > 0) {
                int hours = remainingMinutes / 60;
                int finalMinutes = remainingMinutes % 60;
                if (finalMinutes > 0) {
                    return days + "天" + hours + "小时" + finalMinutes + "分钟";
                } else {
                    return days + "天" + hours + "小时";
                }
            } else {
                return days + "天";
            }
        }
    }
}
