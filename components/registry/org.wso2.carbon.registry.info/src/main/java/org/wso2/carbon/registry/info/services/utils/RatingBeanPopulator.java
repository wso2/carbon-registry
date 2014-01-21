package org.wso2.carbon.registry.info.services.utils;

/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.beans.RatingBean;
import org.wso2.carbon.registry.common.utils.UserUtil;

public class RatingBeanPopulator {

    public static RatingBean populate(UserRegistry userRegistry, String path) {

        RatingBean ratingBean = new RatingBean();
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            float averageRating = userRegistry.getAverageRating(resourcePath.getCompletePath());
            ratingBean.setAverageRating(averageRating);

            // round the average rating to closest 3 decimal points
            float tempRating = averageRating * 1000;
            tempRating = Math.round(tempRating);
            tempRating = tempRating / 1000;
            averageRating = tempRating;

            int userRating = userRegistry.getRating(
                    resourcePath.getCompletePath(), userRegistry.getUserName());
            ratingBean.setUserRating(userRating);

            String[] userStars = new String[5];
            for (int i = 0; i < 5; i++) {

                if (userRating >= i + 1) {
                    userStars[i] = "04";

                } else if (userRating <= i) {
                    userStars[i] = "00";

                }
            }
            ratingBean.setUserStars(userStars);

            String[] averageStars = new String[5];
            for (int i = 0; i < 5; i++) {

                if (averageRating >= i + 1) {
                    averageStars[i] = "04";

                } else if (averageRating <= i) {
                    averageStars[i] = "00";

                } else {

                    float fraction = averageRating - i;

                    if (fraction <= 0.125) {
                        averageStars[i] = "00";

                    } else if (fraction > 0.125 && fraction <= 0.375) {
                        averageStars[i] = "01";

                    } else if (fraction > 0.375 && fraction <= 0.625) {
                        averageStars[i] = "02";

                    } else if (fraction > 0.625 && fraction <= 0.875) {
                        averageStars[i] = "03";

                    } else {
                        averageStars[i] = "04";

                    }
                }
            }
            ratingBean.setAverageStars(averageStars);

            ratingBean.setVersionView(!resourcePath.isCurrentVersion());
            ratingBean.setPathWithVersion(resourcePath.getPathWithVersion());
            ratingBean.setPutAllowed(UserUtil.isPutAllowed(userRegistry.getUserName(), path, userRegistry));
            ratingBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));
        } catch (RegistryException e) {
            String msg = "Failed to get ratings information of the resource " +
                    resourcePath + ". " + e.getMessage();
            ratingBean.setErrorMessage(msg);
        }

        return ratingBean;
    }
}
