/*
 * Datart
 * <p>
 * Copyright 2021
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package datart.data.provider.base.yarn;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author suxinshuo
 * @date 2025/12/24 17:22
 */
@Slf4j
public class YarnRestClient {

    /**
     * 获取 RM 状态接口
     */
    private static final String GET_RM_STATUS_URL = "http://%s/ws/v1/cluster/info";

    /**
     * 获取应用列表接口
     */
    private static final String GET_APPS_BY_TAG_URL = "http://%s/ws/v1/cluster/apps?applicationTags=%s";

    private static final String GET_APP_DETAIL_JOB_URL = "%s/api/v1/applications/%s/jobs";
    private static final String GET_APP_DETAIL_STAGE_URL = "%s/api/v1/applications/%s/stages";


    private final List<YarnRmNode> yarnRmNodes;

    public YarnRestClient(List<YarnRmNode> yarnRmNodes) {
        this.yarnRmNodes = yarnRmNodes;
    }

    /**
     * 根据 tag 获取 yarn 应用列表
     *
     * @param tag yarn 应用标签
     * @return yarn 应用列表
     */
    public List<YarnApp> getYarnAppsByTag(String tag) {
        YarnRmNode yarnRmNode = getActivateRm();
        if (Objects.isNull(yarnRmNode)) {
            return Lists.newArrayList();
        }
        String appsUrl = String.format(GET_APPS_BY_TAG_URL, yarnRmNode.getUrl() + ":" + yarnRmNode.getPort(), tag);
        String appsData = HttpUtil.get(appsUrl);
        YarnAppsResponse yarnAppsResponse = JSONUtil.toBean(appsData, YarnAppsResponse.class);
        YarnApps apps = yarnAppsResponse.getApps();
        if (Objects.isNull(apps)) {
            return Lists.newArrayList();
        }
        return apps.getApps();
    }

    public List<? extends YarnAppJob> getYarnAppJobs(YarnApp yarnApp) {
        String trackingUrl = yarnApp.getTrackingUrl();
        if (StringUtils.endsWith(trackingUrl, "/")) {
            trackingUrl = trackingUrl.substring(0, trackingUrl.length() - 1);
        }
        String appId = yarnApp.getId();
        String jobsUrl = String.format(GET_APP_DETAIL_JOB_URL, trackingUrl, appId);
        String jobsData = HttpUtil.get(jobsUrl);
        return JSONUtil.toBean(jobsData, new TypeReference<List<YarnAppSparkJob>>() {
        }, true);
    }

    /**
     * 获取 active 的 rm 节点
     *
     * @return active 的 rm 节点
     */
    private YarnRmNode getActivateRm() {
        // 请求获取 rm 状态接口, 找到 active 的 rm 节点
        for (YarnRmNode yarnRmNode : this.yarnRmNodes) {
            String rmStatusUrl = String.format(GET_RM_STATUS_URL, yarnRmNode.getUrl() + ":" + yarnRmNode.getPort());
            // 请求获取 rm 状态接口, 找到状态为 ACTIVE 的 rm 节点
            String rmStatusData = HttpUtil.get(rmStatusUrl);
            YarnClusterInfoResponse yarnClusterInfoResponse = JSONUtil.toBean(rmStatusData, YarnClusterInfoResponse.class);
            YarnClusterInfo clusterInfo = yarnClusterInfoResponse.getClusterInfo();
            if (StringUtils.equalsIgnoreCase(clusterInfo.getState(), "ACTIVE")) {
                return yarnRmNode;
            }
        }
        return null;
    }

}
