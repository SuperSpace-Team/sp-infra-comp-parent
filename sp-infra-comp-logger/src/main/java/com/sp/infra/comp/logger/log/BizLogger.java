package com.sp.infra.comp.logger.log;

import com.sp.framework.common.constant.CommonConstants;
import com.sp.infra.comp.logger.log.model.LogResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @description: 业务日志记录器
 * @author: luchao
 * @date: Created in 2/16/22 9:01 PM
 */
@Slf4j
public class BizLogger {
    private static String LOG_FORMAT = "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}";
    private static String TAG_SEPARATOR = "|";
    
    public static void logBizInfo(LogResultModel logModel) {
        String extInfos = "";
        
        try {
        if(!CollectionUtils.isEmpty(logModel.getExtLogInfos())){
            extInfos = String.join(CommonConstants.TAG_SEPARATOR, logModel.getExtLogInfos());
        }

        log.info(LOG_FORMAT,
                logModel.getDepart(),
                logModel.getLogType(),
                logModel.getMethod(),
                (logModel.getSuccess() == null ? true : logModel.getSuccess()) ? "Y" : "N",
                logModel.getCost(),
                filterNvlStr(logModel.getErrorCode()),
                filterNvlStr(logModel.getErrorMsg()),
                filterNvlStr(logModel.getRequest()),
                filterNvlStr(logModel.getResponse()),
                filterNvlStr(extInfos));
        } catch (Exception e) {
            log.error("logBizInfo error.", e);
        }
    }

    private static String filterNvlStr(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        }
        return param;
    }

    /**
     * 自定义输出
     * @param bizType
     * @param logInfos
     */
    private void logDigest(String bizType, List<String> logInfos) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(bizType).append("|");
            for (Object s : logInfos) {
                builder.append("|").append(s);
            }
            log.info(builder.toString());
        } catch (Exception e) {
            log.error("logDigest error. bizType : {} ", bizType, e);
        }
    }
}
