package com.endcareerai.platform.dto.request;

import lombok.Data;

/**
 * 岗位投递请求DTO，包含是否授权企业查看画像
 */
@Data
public class JobApplyRequest {
    private boolean grantAuthToEnterprise = true;
}
